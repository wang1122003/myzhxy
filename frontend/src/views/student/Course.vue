<template>
  <PageContainer title="选课中心">
    <FilterForm
        :items="filterItems"
        :model="searchParams"
        @reset="handleReset"
        @search="handleSearch"
    />

    <!-- 我的已选课程 -->
    <el-card class="box-card my-courses-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><strong>我的已选课程</strong></span>
          <el-button
              plain
              size="small"
              type="primary"
              @click="fetchMySelectedCourses"
          >
            刷新
          </el-button>
        </div>
      </template>
      <div v-if="myCourses.length === 0" class="empty-courses">
        <el-empty description="暂无已选课程"/>
      </div>
      <div v-else class="selected-courses">
        <el-table :data="myCourses" size="small" style="width: 100%">
          <el-table-column label="课程代码" prop="courseCode" width="120"/>
          <el-table-column label="课程名称" min-width="150" prop="courseName"/>
          <el-table-column label="学分" prop="credit" width="80"/>
          <el-table-column
              :formatter="(row) => formatCourseType(row.courseType)"
              label="课程类型"
              width="100"
          />
          <el-table-column fixed="right" label="操作" width="120">
            <template #default="scope">
              <el-button
                  :loading="selectingCourse === scope.row.id"
                  link
                  size="small"
                  type="danger"
                  @click="handleCourseAction(scope.row, 'drop')"
              >
                退课
              </el-button>
              <el-button
                  link
                  size="small"
                  type="primary"
                  @click="handleViewDetails(scope.row)"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

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

    <!-- 课程详情对话框 (内联替代 CourseDetailDialog) -->
    <DialogWrapper
        v-model:visible="courseDetailVisible"
        :title="detailsLoadedCourse?.courseName || selectedCourse?.courseName || '课程详情'"
        width="650px"
    >
      <div v-loading="loadingCourseDetail">
        <el-descriptions v-if="detailsLoadedCourse" :column="2" border>
          <!-- 基本信息 -->
          <el-descriptions-item label="课程代码">{{ detailsLoadedCourse.courseCode || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{
              formatDetailCourseType(detailsLoadedCourse.courseType)
            }}
          </el-descriptions-item>
          <el-descriptions-item label="学分">{{ detailsLoadedCourse.credit || 0 }}</el-descriptions-item>
          <el-descriptions-item label="课时">{{ detailsLoadedCourse.hours || 0 }}</el-descriptions-item>

          <!-- 学生视图信息 -->
          <el-descriptions-item label="开课学院">{{ detailsLoadedCourse.collegeName || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ detailsLoadedCourse.teacherName || '待定' }}</el-descriptions-item>
          <el-descriptions-item label="限选人数">
            {{ detailsLoadedCourse.maxStudents > 0 ? detailsLoadedCourse.maxStudents : '不限' }}
          </el-descriptions-item>
          <el-descriptions-item label="已选人数">
            {{ detailsLoadedCourse.selectedCount || 0 }}
          </el-descriptions-item>

          <!-- 课程安排/上课时间 -->
          <el-descriptions-item :span="2" label="上课时间">
            <div v-if="detailsLoadedCourse.schedules && detailsLoadedCourse.schedules.length">
              <p v-for="(schedule, index) in detailsLoadedCourse.schedules" :key="index">
                {{ formatScheduleTime(schedule) }}
              </p>
            </div>
            <span v-else>待安排</span>
          </el-descriptions-item>

          <!-- 课程介绍 -->
          <el-descriptions-item :span="2" label="课程介绍">
            <div v-html="detailsLoadedCourse.introduction || '暂无介绍'"></div>
          </el-descriptions-item>
        </el-descriptions>

        <div v-else class="empty-content">
          <el-empty description="暂无课程详情"/>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
           <el-button @click="courseDetailVisible = false">关闭</el-button>
          <!-- 保留原有的 #actions 插槽内容 -->
        <el-button
            v-if="detailsLoadedCourse && !isSelected(detailsLoadedCourse.id) && canSelectCourse(detailsLoadedCourse)"
            :loading="selectingCourse === detailsLoadedCourse.id"
            type="primary"
            @click="handleCourseAction(detailsLoadedCourse, 'select')"
        >
          选课
        </el-button>
        <el-button
            v-if="detailsLoadedCourse && isSelected(detailsLoadedCourse.id)"
            :loading="selectingCourse === detailsLoadedCourse.id"
            type="danger"
            @click="handleCourseAction(detailsLoadedCourse, 'drop')"
        >
          退课
        </el-button>
        </span>
      </template>
    </DialogWrapper>

  </PageContainer>
</template>

<script setup>
import {computed, h, onMounted, reactive, ref, resolveComponent, watch} from 'vue';
import {
  ElButton, ElMessage, ElMessageBox, ElEmpty, ElTable, ElTableColumn,
  ElDescriptions, ElDescriptionsItem
} from 'element-plus';
import {getCourseList, getStudentCourses, getCourseById} from '@/api/course';
import {dropCourse, selectCourse} from '@/api/courseSelection';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import TableView from '@/views/ui/TableView.vue';
import FilterForm from '@/views/ui/AdvancedFilterForm.vue';
import DialogWrapper from '@/views/ui/DialogWrapper.vue';
import axios from 'axios';
import {useUserStore} from '@/stores/userStore';

// --- State ---
const loading = ref(false);
const loadingCourseDetail = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const courseList = ref([]);
const myCourseIds = ref(new Set()); // 存储已选课程 ID，查询更快
const myCourses = ref([]); // 存储已选课程详细信息
const selectingCourse = ref(null); // 正在操作的课程ID

const courseDetailVisible = ref(false);
const selectedCourse = ref({});
const selectedCourseId = ref(null);
const detailsLoadedCourse = ref(null);

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
    'REQUIRED': '必修课',
    'ELECTIVE': '选修课',
    'PRACTICAL': '实践课',
    'COMPULSORY': '必修课',
    'GENERAL': '通识课'
  };
  return map[type] || type;
};

const formatDetailCourseType = (type) => {
  const map = {
    'COMPULSORY': '必修课',
    'ELECTIVE': '选修课',
    'GENERAL': '通识课'
  };
  return map[type] || type || '未知';
};

const formatStatus = (course) => {
  if (myCourseIds.value.has(course.id)) return '已选课';
  if (course.status === 'CLOSED') return '已关闭'; // 假设后端状态
  if (course.maxStudents && course.selectedCount >= course.maxStudents) return '人数已满';

  // 检查选课窗口
  if (systemSettings.value && systemSettings.value.courseSelection) {
    const {enabled, startDate, endDate} = systemSettings.value.courseSelection;
    const now = new Date();
    const start = startDate ? new Date(startDate) : null;
    const end = endDate ? new Date(endDate) : null;

    if (!enabled) return '选课未开放';
    if (start && now < start) return '选课未开始';
    if (end && now > end) return '选课已结束';
  }
  
  return '可选课';
};

const getStatusType = (course) => {
  if (myCourseIds.value.has(course.id)) return 'success';
  if (course.status === 'CLOSED') return 'info';
  if (course.maxStudents && course.selectedCount >= course.maxStudents) return 'danger';
  return 'primary';
};

// 判断课程是否可选（包括状态、人数以及选课时间窗口）
const canSelectCourse = (course) => {
  if (myCourseIds.value.has(course.id)) return false; // 已选，不能再选
  if (course.status === 'CLOSED' || (course.maxStudents > 0 && course.selectedCount >= course.maxStudents)) return false;

  // 检查选课窗口时间
  if (systemSettings.value && systemSettings.value.courseSelection) {
    const {enabled, startDate, endDate} = systemSettings.value.courseSelection;
    const now = new Date();
    const start = startDate ? new Date(startDate) : null;
    const end = endDate ? new Date(endDate) : null;

    if (!enabled) return false; // 选课未开放
    if (start && now < start) return false; // 选课未开始
    if (end && now > end) return false; // 选课已结束
  }
  
  return true;
};

// 格式化上课时间
const formatScheduleTime = (schedule) => {
  if (!schedule) return '待安排';
  const weekdays = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const day = weekdays[schedule.weekday] || `周${schedule.weekday}`;
  const time = `${schedule.startTime?.substring(0, 5) || '?'}-${schedule.endTime?.substring(0, 5) || '?'}`;
  const weeks = `第${schedule.startWeek || '?'}-${schedule.endWeek || '?'}周`;
  const location = schedule.classroomName || '地点待定';
  return `${day} ${time} (${weeks}) @ ${location}`;
};

// 判断课程是否已选
const isSelected = (courseId) => {
  return myCourseIds.value.has(courseId);
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

// 系统设置，从后端获取
const systemSettings = ref(null);

// 获取系统设置
const fetchSystemSettings = async () => {
  try {
    const response = await axios.get('/api/common/config');
    if (response.data && response.data.code === 200) {
      systemSettings.value = response.data.data;
    }
  } catch (error) {
    console.error('获取系统设置失败:', error);
  }
};

// --- Methods ---

// 获取学生已选课程列表
const fetchMySelectedCourses = async () => {
  try {
    const userStore = useUserStore();
    const userId = userStore.userId.value;
    if (!userId) {
      console.warn('无法获取用户ID，无法获取已选课程');
      return;
    }

    const myCoursesRes = await getStudentCourses();

    if (myCoursesRes && myCoursesRes.code === 200) {
      myCourses.value = myCoursesRes.data || [];
      myCourseIds.value = new Set(myCourses.value.map(c => c.id));
    } else if (Array.isArray(myCoursesRes)) {
      myCourses.value = myCoursesRes;
      myCourseIds.value = new Set(myCoursesRes.map(c => c.id));
    } else {
      myCourses.value = [];
      myCourseIds.value = new Set();
    }

    console.log('已选课程:', myCourses.value);
  } catch (error) {
    console.error('获取已选课程失败:', error);
    myCourses.value = [];
    myCourseIds.value = new Set();
  }
};

// 获取课程列表和已选课程
const fetchData = async () => {
  loading.value = true;
  try {
    // 并行获取可选课程和我的课程
    const coursesRes = await getCourseList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || undefined,
      courseType: searchParams.courseType || undefined
    });
    
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

    // 获取我的课程
    await fetchMySelectedCourses();
    
  } catch (error) {
    console.error('获取课程数据失败', error);
    courseList.value = [];
    total.value = 0;
    myCourseIds.value = new Set();
    myCourses.value = [];
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
    // 获取用户ID和当前学期信息
    const userStore = useUserStore();
    const userId = userStore.userId.value;
    const currentTerm = localStorage.getItem('currentTerm') || '2023-2024-2'; // 默认当前学期

    if (action === 'select') {
      // 可以添加选课前的确认
      await selectCourse({
        userId: userId,
        courseId: course.id,
        termInfo: currentTerm
      }); 
      ElMessage.success(`【${course.courseName}】${actionText}成功`);
    } else {
      // 退课前确认
      await ElMessageBox.confirm(`确定要退选课程【${course.courseName}】吗？`, '确认退课', {
        confirmButtonText: '确定退课',
        cancelButtonText: '取消',
        type: 'warning',
      });
      await dropCourse({
        userId: userId,
        courseId: course.id,
        termInfo: currentTerm
      });
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
      // 显示错误消息
      ElMessage.error(error.message || `${actionText}失败，请稍后再试`);
    }
  } finally {
    selectingCourse.value = null;
  }
};

// 处理查看详情
const handleViewDetails = (course) => {
  selectedCourseId.value = course.id;
  selectedCourse.value = {...course}; // 存储基础信息
  detailsLoadedCourse.value = null; // 清空旧的详细数据
  courseDetailVisible.value = true;
  loadCourseDetails(course.id); // 打开对话框时加载
};

// 新增加载详情的函数
const loadCourseDetails = async (id) => {
  if (!id) return;
  loadingCourseDetail.value = true;
  detailsLoadedCourse.value = null; // 清空旧数据
  try {
    const res = await getCourseById(id);
    if (res && res.data) {
      detailsLoadedCourse.value = res.data;
      if (!detailsLoadedCourse.value.schedules) {
        detailsLoadedCourse.value.schedules = [];
      } else if (!Array.isArray(detailsLoadedCourse.value.schedules)) {
        detailsLoadedCourse.value.schedules = [];
      }
      // 触发 loaded 事件的替代逻辑（如果需要的话，例如更新按钮状态）
      // onCourseDetailsLoaded(detailsLoadedCourse.value); // 调用之前的回调
    } else {
      ElMessage.error('加载课程详情失败');
    }
  } catch (error) {
    console.error("获取课程详情失败:", error);
    ElMessage.error('获取课程详情失败');
  } finally {
    loadingCourseDetail.value = false;
  }
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchData();
  fetchSystemSettings();
});

// 监听分页变化 (TableView 内部处理)
watch([currentPage, pageSize], fetchData, {immediate: false});

</script>

<style scoped>
/* Specific styles if needed */
.dialog-footer {
  text-align: right;
}

.my-courses-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-courses {
  padding: 20px;
  text-align: center;
}

.selected-courses {
  margin-bottom: 10px;
}
</style> 