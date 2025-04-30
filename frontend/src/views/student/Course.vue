<template>
  <PageContainer title="选课中心">
    <FilterForm
        :items="filterItems"
        :model="searchParams"
        @reset="handleReset"
        @search="handleSearch"
    />

    <TableView
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :action-column-config="actionColumnConfig"
        :columns="tableColumns"
        :data="courseList"
        :loading="loading"
        :total="total"
        @refresh="fetchData"
        @select-course="handleCourseAction"
        @drop-course="handleCourseAction"
        @view-detail="handleViewDetails"
    />

    <!-- 课程详情对话框 -->
    <el-dialog v-model="courseDetailVisible" :title="selectedCourse.courseName" width="650px">
      <div v-loading="loadingCourseDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程代码">{{ selectedCourse.courseCode }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{
              formatCourseType(selectedCourse.courseType)
            }}
          </el-descriptions-item>
          <el-descriptions-item label="学分">{{ selectedCourse.credit }}</el-descriptions-item>
          <el-descriptions-item label="课时">{{ selectedCourse.hours }}</el-descriptions-item>
          <el-descriptions-item label="开课学院">{{ selectedCourse.collegeName }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ selectedCourse.teacherName || '待定' }}</el-descriptions-item>
          <el-descriptions-item label="限选人数">{{
              selectedCourse.maxStudents > 0 ? selectedCourse.maxStudents : '不限'
            }}
          </el-descriptions-item>
          <el-descriptions-item label="已选人数">{{ selectedCourse.selectedCount || 0 }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="上课时间">
            <div v-if="selectedCourse.schedules && selectedCourse.schedules.length">
              <p v-for="(schedule, index) in selectedCourse.schedules" :key="index">
                {{ formatScheduleTime(schedule) }}
              </p>
            </div>
            <span v-else>待安排</span>
          </el-descriptions-item>
          <el-descriptions-item :span="2" label="课程介绍">
            <div v-html="selectedCourse.introduction || '暂无介绍'"></div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="courseDetailVisible = false">关闭</el-button>
          <el-button
              v-if="!isSelected(selectedCourse.id) && canSelectCourse(selectedCourse)"
              :loading="selectingCourse === selectedCourse.id"
              type="primary"
              @click="handleCourseAction(selectedCourse, 'select')"
          >
            选课
          </el-button>
           <el-button
               v-if="isSelected(selectedCourse.id)"
               :loading="selectingCourse === selectedCourse.id"
               type="danger"
               @click="handleCourseAction(selectedCourse, 'drop')"
           >
            退课
          </el-button>
        </span>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref, watch, h, resolveComponent} from 'vue';
import {ElMessage, ElMessageBox, ElTag, ElButton} from 'element-plus';
import {getCourseList, getStudentCourses, getCourseById} from '@/api/course'; // Corrected: Use course.js for listing and details
import {selectCourse, dropCourse} from '@/api/courseSelection'; // Corrected: Use courseSelection.js for actions
import {formatDateTime} from '@/utils/formatters';
import PageContainer from '@/components/common/EnhancedPageContainer.vue';
import TableView from '@/components/common/TableView.vue';
import FilterForm from '@/components/common/AdvancedFilterForm.vue';

// --- State ---
const loading = ref(false);
const loadingCourseDetail = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const courseList = ref([]);
const myCourseIds = ref(new Set()); // 存储已选课程 ID，查询更快
const selectingCourse = ref(null); // 正在操作的课程ID

const courseDetailVisible = ref(false);
const selectedCourse = ref({});

// 搜索参数
const searchParams = reactive({
  keyword: '',
  courseType: ''
});

// --- Options & Formatters ---
const courseTypeOptions = [
  {value: 'COMPULSORY', label: '必修课'},
  {value: 'ELECTIVE', label: '选修课'},
  {value: 'GENERAL', label: '通识课'}
];

const formatCourseType = (type) => {
  const map = {
    'COMPULSORY': '必修课',
    'ELECTIVE': '选修课',
    'GENERAL': '通识课'
  };
  return map[type] || type;
};

const formatStatus = (course) => {
  if (myCourseIds.value.has(course.id)) return '已选课';
  if (course.status === 'CLOSED') return '已关闭'; // 假设后端状态
  if (course.maxStudents && course.selectedCount >= course.maxStudents) return '人数已满';
  // TODO: Add check for course selection window from settings/backend
  return '可选课';
};

const getStatusType = (course) => {
  if (myCourseIds.value.has(course.id)) return 'success';
  if (course.status === 'CLOSED') return 'info';
  if (course.maxStudents && course.selectedCount >= course.maxStudents) return 'danger';
  return 'primary';
};

// 判断课程是否可选（仅判断状态和人数，时间窗口需另外考虑）
const canSelectCourse = (course) => {
  if (myCourseIds.value.has(course.id)) return false; // 已选，不能再选
  if (course.status === 'CLOSED' || (course.maxStudents > 0 && course.selectedCount >= course.maxStudents)) return false;
  // TODO: Add check for course selection window
  return true;
};

// 格式化上课时间
const formatScheduleTime = (schedule) => {
  if (!schedule) return '待安排';
  const weekdays = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const day = weekdays[schedule.weekday] || `周${schedule.weekday}`;
  const time = `${schedule.startTime?.substring(0, 5)}-${schedule.endTime?.substring(0, 5)}`;
  const weeks = `第${schedule.startWeek}-${schedule.endWeek}周`;
  const location = schedule.classroomName || '地点待定';
  return `${day} ${time} (${weeks}) @ ${location}`;
};

// --- Computed Properties for Components ---

// FilterForm 配置
const filterItems = computed(() => [
  {
    type: 'input',
    label: '关键词',
    prop: 'keyword',
    placeholder: '课程名称/代码',
    props: {clearable: true, style: {width: '200px'}}
  },
  {
    type: 'select',
    label: '课程类型',
    prop: 'courseType',
    placeholder: '选择类型',
    options: courseTypeOptions,
    props: {clearable: true, style: {width: '150px'}}
  }
]);

// TableView 列配置
const tableColumns = computed(() => [
  {prop: 'courseCode', label: '课程代码', width: 120},
  {prop: 'courseName', label: '课程名称', minWidth: 180, showOverflowTooltip: true},
  {prop: 'credit', label: '学分', width: 80},
  {prop: 'hours', label: '课时', width: 80},
  {
    prop: 'courseType',
    label: '课程类型',
    width: 100,
    formatter: (row) => formatCourseType(row.courseType)
  },
  {prop: 'collegeName', label: '所属学院', minWidth: 150, showOverflowTooltip: true},
  {
    prop: 'capacityInfo',
    label: '名额 (已选/上限)',
    width: 150,
    align: 'center',
    formatter: (row) => `${row.selectedCount || 0} / ${row.maxStudents > 0 ? row.maxStudents : '不限'}`
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    slots: {
      default: (scope) => h(resolveComponent('ElTag'), {
        type: getStatusType(scope.row),
        size: 'small'
      }, () => formatStatus(scope.row))
    }
  }
]);

// TableView 操作列配置
const actionColumnConfig = computed(() => ({
  width: 120,
  fixed: 'right',
  buttons: (
      row
  ) => {
    const actions = [];
    actions.push({label: '详情', type: 'info', link: true, event: 'view-detail'});

    if (myCourseIds.value.has(row.id)) {
      actions.push({
        label: '退课',
        type: 'danger',
        link: true,
        event: 'drop-course',
        loading: selectingCourse.value === row.id
      });
    } else if (canSelectCourse(row)) {
      actions.push({
        label: '选课',
        type: 'primary',
        link: true,
        event: 'select-course',
        loading: selectingCourse.value === row.id
      });
    }
    return actions;
  }
}));


// --- Methods ---

// 获取课程列表和已选课程
const fetchData = async () => {
  loading.value = true;
  try {
    // 并行获取可选课程和我的课程
    const [coursesRes, myCoursesRes] = await Promise.all([
      getCourseList({
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchParams.keyword || undefined,
        courseType: searchParams.courseType || undefined
      }),
      getStudentCourses() // 获取学生已选课程
    ]);

    // 处理可选课程数据
    if (coursesRes && coursesRes.code === 200) {
      courseList.value = coursesRes.data?.records || [];
      total.value = coursesRes.data?.total || 0;
    } else {
      console.warn('课程API返回非标准格式:', coursesRes);
      // 尝试直接使用返回的数据
      if (Array.isArray(coursesRes)) {
        courseList.value = coursesRes;
        total.value = coursesRes.length;
      } else if (coursesRes && coursesRes.records) {
        courseList.value = coursesRes.records;
        total.value = coursesRes.total || coursesRes.records.length;
      } else {
        courseList.value = [];
        total.value = 0;
      }
    }

    // 处理我的课程数据
    if (myCoursesRes && myCoursesRes.code === 200) {
      myCourseIds.value = new Set((myCoursesRes.data || []).map(c => c.id));
    } else if (Array.isArray(myCoursesRes)) {
      myCourseIds.value = new Set(myCoursesRes.map(c => c.id));
    } else {
      myCourseIds.value = new Set();
    }
  } catch (error) {
    console.error('获取课程数据失败', error);
    courseList.value = [];
    total.value = 0;
    myCourseIds.value = new Set();
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1; // 重置页码
  fetchData();
};

// 处理重置
const handleReset = () => {
  searchParams.keyword = '';
  searchParams.courseType = '';
  currentPage.value = 1;
  fetchData();
};

// 处理选课/退课动作
const handleCourseAction = async (course, actionType) => {
  // 如果 actionType 未指定 (来自按钮事件)，则根据是否已选判断
  const action = actionType || (myCourseIds.value.has(course.id) ? 'drop' : 'select');
  const actionText = action === 'select' ? '选课' : '退课';

  selectingCourse.value = course.id;
  try {
    if (action === 'select') {
      // 可以添加选课前的确认
      await selectCourse({courseId: course.id}); // Corrected function call
      ElMessage.success(`【${course.courseName}】${actionText}成功`);
    } else {
      // 退课前确认
      await ElMessageBox.confirm(`确定要退选课程【${course.courseName}】吗？`, '确认退课', {
        confirmButtonText: '确定退课',
        cancelButtonText: '取消',
        type: 'warning',
      });
      await dropCourse({courseId: course.id}); // Corrected function call
      ElMessage.success(`【${course.courseName}】${actionText}成功`);
        }
    // 操作成功后刷新列表和我的课程
    await fetchData();
    // 如果详情弹窗打开，也关闭它
    if (courseDetailVisible.value && selectedCourse.value.id === course.id) {
      courseDetailVisible.value = false;
    }
  } catch (error) {
    // ElMessageBox 的取消会 reject, 单独处理
    if (error !== 'cancel') {
      console.error(`${actionText}失败:`, error);
      // 错误消息通常由拦截器处理
    }
  } finally {
    selectingCourse.value = null;
  }
};

// 处理查看详情
const handleViewDetails = async (course) => {
  selectedCourse.value = {...course}; // 浅拷贝基础信息
  courseDetailVisible.value = true;
  loadingCourseDetail.value = true;
  try {
    // 获取更详细的信息，例如课程介绍、教师、具体安排等
    const res = await getCourseById(course.id); // Corrected function call
    selectedCourse.value = res.data; // 使用详细数据覆盖
    // 确保 schedules 存在且是数组
    if (!Array.isArray(selectedCourse.value.schedules)) {
      selectedCourse.value.schedules = [];
        }
  } catch (error) {
    console.error("获取课程详情失败:", error);
    ElMessage.error("获取课程详情失败");
    courseDetailVisible.value = false; // 获取失败则关闭
  } finally {
    loadingCourseDetail.value = false;
  }
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchData();
});

// 监听分页变化 (TableView 内部处理)
watch([currentPage, pageSize], fetchData, {immediate: false});

</script>

<style scoped>
/* Specific styles if needed */
.dialog-footer {
  text-align: right;
}
</style> 