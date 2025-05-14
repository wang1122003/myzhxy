<template>
  <PageContainer :title="pageTitle">
    <template v-if="showAddButtonFlag" #header-actions>
      <el-button :icon="Plus" type="primary" @click="handleAdd">
        {{ addButtonText }}
      </el-button>
    </template>

    <FilterForm
        v-if="showFilterFlag"
        :items="filterItems"
        :model="searchParams"
        :show-add-button="false"
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
        @refresh="fetchCourses"
        @edit-item="handleEdit"
        @view-detail="handleViewDetail"
        @delete-item="handleDelete"
    />

    <!-- 添加/编辑 对话框 -->
    <DialogWrapper
        v-model:visible="dialogVisible"
        :title="isEditMode ? '编辑课程信息' : '添加课程'"
        width="600px"
        @close="handleDialogClose"
    >
      <el-form
          ref="courseFormRef"
          v-loading="formLoading"
          :model="currentCourseForm"
          :rules="courseFormRules"
          label-width="100px"
      >
        <el-form-item label="课程编号" prop="courseCode">
          <el-input v-model="currentCourseForm.courseCode" :disabled="isEditMode"
                    placeholder="请输入课程编号"></el-input>
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="currentCourseForm.courseName" placeholder="请输入课程名称"></el-input>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="currentCourseForm.credit" :min="0" :precision="1" :step="0.5"
                           controls-position="right" style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="课时" prop="hours">
          <el-input-number v-model="currentCourseForm.hours" :min="0" :precision="0" controls-position="right"
                           style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="课程类型" prop="courseType">
          <el-select v-model="currentCourseForm.courseType" placeholder="请选择课程类型" style="width: 100%;">
            <el-option label="必修课" value="COMPULSORY"/>
            <el-option label="选修课" value="ELECTIVE"/>
            <el-option label="通识课" value="GENERAL"/>
          </el-select>
        </el-form-item>
        <el-form-item label="课程介绍" prop="introduction">
          <el-input v-model="currentCourseForm.introduction" :rows="4" placeholder="请输入课程介绍"
                    type="textarea"></el-input>
        </el-form-item>
        <el-form-item v-if="currentRole === 'admin' && isEditMode" label="教师" prop="teacherId">
          <el-select
              v-model="currentCourseForm.teacherId"
              :loading="teacherLoading"
              filterable
              placeholder="请选择教师"
              style="width: 100%;"
          >
            <el-option
                v-for="teacher in teacherList"
                :key="teacher.id"
                :label="teacher.name"
                :value="teacher.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button :loading="submitting" type="primary" @click="handleSubmit">
            {{ isEditMode ? '保存更新' : '确认添加' }}
          </el-button>
        </span>
      </template>
    </DialogWrapper>

    <!-- 课程详情 对话框 (内联替代 CourseDetailDialog) -->
    <DialogWrapper
        v-model:visible="detailDialogVisible"
        :title="detailedCourseData?.courseName || '课程详情'"
        width="650px"
    >
      <div v-loading="detailLoading">
        <el-descriptions v-if="detailedCourseData" :column="2" border>
          <el-descriptions-item label="课程代码">{{ detailedCourseData.courseCode || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{
              formatDetailCourseType(detailedCourseData.courseType)
            }}
          </el-descriptions-item>
          <el-descriptions-item label="学分">{{ detailedCourseData.credit || 0 }}</el-descriptions-item>
          <el-descriptions-item label="课时">{{ detailedCourseData.hours || 0 }}</el-descriptions-item>

          <!-- 根据角色显示不同的信息 (这里 role 是 teacher) -->
          <el-descriptions-item label="授课班级">{{ detailedCourseData.className || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="课程人数">{{ detailedCourseData.studentCount || 0 }}</el-descriptions-item>

          <!-- 课程安排/上课时间 -->
          <el-descriptions-item :span="2" label="上课时间">
            <div v-if="detailedCourseData.schedules && detailedCourseData.schedules.length">
              <p v-for="(schedule, index) in detailedCourseData.schedules" :key="index">
                {{ formatScheduleTime(schedule) }}
              </p>
            </div>
            <span v-else>待安排</span>
          </el-descriptions-item>

          <!-- 课程介绍 -->
          <el-descriptions-item :span="2" label="课程介绍">
            <div v-html="detailedCourseData.introduction || '暂无介绍'"></div>
          </el-descriptions-item>
        </el-descriptions>

        <div v-else class="empty-content">
          <el-empty description="暂无课程详情"/>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <!-- 保留原有的 #actions 插槽内容 -->
          <el-button v-if="detailedCourseData" type="primary" @click="handleEdit(detailedCourseData)">编辑</el-button>
        </span>
      </template>
    </DialogWrapper>
  </PageContainer>
</template>

<script setup>
import {computed, nextTick, onMounted, reactive, ref, watch} from 'vue';
import {
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElSelect,
  ElDescriptions,
  ElDescriptionsItem,
  ElEmpty
} from 'element-plus';
import {Delete, Edit, Plus, View} from '@element-plus/icons-vue';
import {addCourse, deleteCourse, getAllCourses, getCourseById, getTeacherCourses, updateCourse} from '@/api/course';
import {getTeacherList} from '@/api/user';
import {useUserStore} from '@/stores/userStore';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue'; // Adjusted path
import TableView from '@/views/ui/TableView.vue';
import FilterForm from '@/views/ui/AdvancedFilterForm.vue';
import DialogWrapper from '@/views/ui/DialogWrapper.vue';

// Configuration for this specific view (formerly props of CourseManagementView)
const currentRole = 'teacher';
const pageTitle = '我的课程管理';
const showAddButtonFlag = false; // Renamed to avoid conflict if 'showAddButton' is a ref/reactive property elsewhere
const addButtonText = '添加课程'; // Default from original component, can be customized if needed
const showFilterFlag = true;    // Renamed for clarity

const userStore = useUserStore();
const loading = ref(false);
const formLoading = ref(false);
const submitting = ref(false);
const detailLoading = ref(false);
const teacherLoading = ref(false);
const courseList = ref([]);
const teacherList = ref([]); // Only fetched if role is admin, but keep for consistency
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const searchParams = reactive({
  keyword: '',
  courseType: ''
});

const dialogVisible = ref(false);
const detailDialogVisible = ref(false);
const isEditMode = ref(false);
const currentCourseForm = ref({});
const currentCourse = ref(null);
const currentCourseId = ref(null);
const detailedCourseData = ref(null);
const courseFormRef = ref(null);

const courseFormRules = reactive({
  courseCode: [{required: true, message: '请输入课程编号', trigger: 'blur'}],
  courseName: [{required: true, message: '请输入课程名称', trigger: 'blur'}],
  credit: [{required: true, type: 'number', message: '请输入学分', trigger: 'blur'}],
  hours: [{required: true, type: 'number', message: '请输入课时', trigger: 'blur'}],
  courseType: [{required: true, message: '请选择课程类型', trigger: 'change'}],
});

const filterItems = computed(() => [
  {
    type: 'input',
    label: '关键词',
    prop: 'keyword',
    placeholder: '课程编号/名称',
    props: {clearable: true}
  },
  {
    type: 'select',
    label: '课程类型',
    prop: 'courseType',
    placeholder: '选择类型',
    options: [
      {label: '全部', value: ''},
      {label: '必修课', value: 'COMPULSORY'},
      {label: '选修课', value: 'ELECTIVE'},
      {label: '通识课', value: 'GENERAL'}
    ],
    props: {clearable: true}
  }
]);

const tableColumns = computed(() => {
  const columns = [
    {prop: 'courseCode', label: '课程编号', width: 120},
    {prop: 'courseName', label: '课程名称', minWidth: 180},
    {prop: 'credit', label: '学分', width: 80},
    {prop: 'hours', label: '课时', width: 80},
    {
      prop: 'courseType',
      label: '课程类型',
      width: 100,
      formatter: (row) => formatCourseType(row.courseType)
    }
  ];
  if (currentRole === 'admin') {
    columns.push({prop: 'teacherName', label: '教师', width: 120});
  }
  return columns;
});

const actionColumnConfig = computed(() => ({
  label: '操作',
  width: 220,
  actions: [
    {
      name: 'ViewDetail',
      label: '详情',
      icon: View,
      type: 'info',
      permission: () => true, // All roles can view details
      handler: handleViewDetail
    },
    {
      name: 'Edit',
      label: '编辑',
      icon: Edit,
      type: 'primary',
      permission: () => currentRole === 'admin' || currentRole === 'teacher',
      handler: handleEdit
    },
    {
      name: 'Delete',
      label: '删除',
      icon: Delete,
      type: 'danger',
      permission: () => currentRole === 'admin', // Only admin can delete
      handler: handleDelete
    }
  ].filter(action => action.permission ? action.permission() : true)
}));

const formatCourseType = (type) => {
  const types = {
    COMPULSORY: '必修课',
    ELECTIVE: '选修课',
    GENERAL: '通识课'
  };
  return types[type] || type;
};

const formatDetailCourseType = (type) => {
  const map = {
    'COMPULSORY': '必修课',
    'ELECTIVE': '选修课',
    'GENERAL': '通识课'
  };
  return map[type] || type || '未知';
};

const formatScheduleTime = (schedule) => {
  if (!schedule) return '待安排';
  const weekdays = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const day = weekdays[schedule.weekday] || `周${schedule.weekday}`;
  const time = `${schedule.startTime?.substring(0, 5) || '?'}-${schedule.endTime?.substring(0, 5) || '?'}`;
  const weeks = `第${schedule.startWeek || '?'}-${schedule.endWeek || '?'}周`;
  const location = schedule.classroomName || '地点待定';
  return `${day} ${time} (${weeks}) @ ${location}`;
};

const loadCourseDetails = async (id) => {
  if (!id) return;
  detailLoading.value = true;
  detailedCourseData.value = null;
  try {
    const res = await getCourseById(id);
    if (res && res.data) {
      detailedCourseData.value = res.data;
      if (!detailedCourseData.value.schedules) {
        detailedCourseData.value.schedules = [];
      } else if (!Array.isArray(detailedCourseData.value.schedules)) {
        detailedCourseData.value.schedules = [];
      }
    } else {
      ElMessage.error('加载课程详情失败');
    }
  } catch (error) {
    console.error("获取课程详情失败:", error);
    ElMessage.error('获取课程详情失败');
  } finally {
    detailLoading.value = false;
  }
};

const fetchCourses = async () => {
  loading.value = true;
  try {
    let response;
    // 基础参数
    const baseParams = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || undefined, // 确保空字符串不传递
      courseType: searchParams.courseType || undefined // 确保空字符串不传递
    };

    // 过滤掉值为 undefined 的参数
    const params = Object.entries(baseParams).reduce((acc, [key, value]) => {
      if (value !== undefined) {
        acc[key] = value;
      }
      return acc;
    }, {});


    if (currentRole === 'teacher') {
      const teacherId = userStore.userInfo?.id;
      if (!teacherId) {
        ElMessage.error('无法获取教师ID');
        loading.value = false;
        return;
      }
      // 将 teacherId 合并到 params 中
      const teacherParams = {...params, teacherId: teacherId};
      response = await getTeacherCourses(teacherParams); // 只传递一个参数
    } else if (currentRole === 'admin') {
      response = await getAllCourses(params); // getAllCourses 调用方式不变
    } else {
      ElMessage.error('未知角色，无法获取课程');
      loading.value = false;
      return;
    }

    // 统一处理响应
    if (response && response.data) {
      const data = response.data;
      // 检查多种可能的分页格式
      if (data.records && typeof data.total === 'number') { // 格式: { data: { records: [], total } }
        courseList.value = data.records;
        total.value = data.total;
      } else if (Array.isArray(data)) { // 格式: { data: [] }
        courseList.value = data;
        total.value = data.length; // 无法获取真实 total，用当前页数量代替
      } else { // 其他或未知格式
        console.warn(`获取课程(${currentRole}) API 返回非预期格式:`, response);
        courseList.value = [];
        total.value = 0;
      }
    } else {
      console.error(`获取课程(${currentRole}) API 返回无效响应:`, response);
      ElMessage.error('获取课程列表失败');
      courseList.value = [];
      total.value = 0;
    }

  } catch (error) {
    console.error(`Error fetching courses for ${currentRole}:`, error);
    // 错误通常由请求拦截器处理，这里可以只重置状态
    ElMessage.error('获取课程列表时发生错误'); // 保留一个通用提示
    courseList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const fetchTeachers = async () => {
  if (currentRole === 'admin') { // Only fetch if admin and in edit mode needing teacher selection
    teacherLoading.value = true;
    try {
      const response = await getTeacherList({page: 1, size: 1000}); // Fetch a large list for selection
      if (response && response.data) {
        teacherList.value = response.data.records || response.data;
      }
    } catch (error) {
      console.error("Error fetching teachers:", error);
      ElMessage.error('获取教师列表失败');
    } finally {
      teacherLoading.value = false;
    }
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchCourses();
};

const handleReset = () => {
  searchParams.keyword = '';
  searchParams.courseType = '';
  currentPage.value = 1;
  fetchCourses();
};

const handleAdd = () => {
  isEditMode.value = false;
  currentCourseForm.value = {
    courseCode: '',
    courseName: '',
    credit: null,
    hours: null,
    courseType: '',
    introduction: ''
  };
  if (courseFormRef.value) {
    courseFormRef.value.resetFields();
  }
  if (currentRole === 'admin') {
    fetchTeachers(); // Fetch teachers for admin when adding
  }
  dialogVisible.value = true;
};

const handleEdit = async (course) => {
  isEditMode.value = true;
  formLoading.value = true;
  dialogVisible.value = true;

  if (currentRole === 'admin') {
    await fetchTeachers(); // Fetch teachers for admin when editing
  }
  try {
    const courseIdToFetch = course?.id || currentCourseId.value; // Use ID from param or stored ID
    if (!courseIdToFetch) {
      ElMessage.error('无效的课程ID');
      formLoading.value = false;
      dialogVisible.value = false;
      return;
    }
    const response = await getCourseById(courseIdToFetch);
    if (response && response.data) {
      currentCourseForm.value = {...response.data};
      // Ensure numeric fields are numbers
      currentCourseForm.value.credit = Number(response.data.credit) || null;
      currentCourseForm.value.hours = Number(response.data.hours) || null;
    } else {
      ElMessage.error('获取课程详情失败');
      // Fallback to potentially partial data if needed
      currentCourseForm.value = {...course};
    }
  } catch (error) {
    console.error("Error fetching course details for edit:", error);
    ElMessage.error('获取课程详情失败');
    currentCourseForm.value = {...course}; // Fallback
  } finally {
    formLoading.value = false;
  }
  // Ensure form is clear of previous validation states
  nextTick(() => {
    if (courseFormRef.value) {
      courseFormRef.value.clearValidate();
    }
  });
};

const handleViewDetail = (course) => {
  currentCourseId.value = course.id;
  currentCourse.value = course; // 存储基础信息，例如用于标题
  detailedCourseData.value = null; // 清空旧的详细数据
  detailDialogVisible.value = true;
  loadCourseDetails(course.id); // 打开对话框时加载
};

const handleDelete = async (course) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除课程 "${course.courseName}" 吗？此操作不可恢复。`,
        '警告',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
        }
    );
    submitting.value = true;
    await deleteCourse(course.id);
    ElMessage.success('课程删除成功');
    fetchCourses(); // Refresh list
  } catch (error) {
    if (error !== 'cancel') {
      console.error("Error deleting course:", error);
      ElMessage.error(error.message || '删除课程失败');
    }
  } finally {
    submitting.value = false;
  }
};

const handleSubmit = async () => {
  if (!courseFormRef.value) return;
  try {
    await courseFormRef.value.validate();
    submitting.value = true;
    formLoading.value = true;

    const payload = {...currentCourseForm.value};
    // Ensure teacherId is only included if role is admin
    if (currentRole !== 'admin') {
      delete payload.teacherId;
    } else if (payload.teacherId === '') { // Handle case where admin might clear teacher selection
      payload.teacherId = null;
    }


    if (isEditMode.value) {
      await updateCourse(payload.id, payload);
      ElMessage.success('课程更新成功');
    } else {
      // For teacher adding a course, associate with themselves
      if (currentRole === 'teacher') {
        payload.teacherId = userStore.userInfo?.id;
        if (!payload.teacherId) {
          ElMessage.error('无法获取教师ID，无法添加课程');
          submitting.value = false;
          formLoading.value = false;
          return;
        }
      }
      await addCourse(payload);
      ElMessage.success('课程添加成功');
    }
    dialogVisible.value = false;
    fetchCourses();
  } catch (error) {
    // Validation errors are handled by the form, this catches submission errors
    if (error && error.message) {
      ElMessage.error(error.message || (isEditMode.value ? '更新失败' : '添加失败'));
    } else if (typeof error === 'string' && error !== 'validate') {
      ElMessage.error(error);
    }
    // console.error("Error submitting course form:", error);
  } finally {
    submitting.value = false;
    formLoading.value = false;
  }
};

const handleDialogClose = () => {
  dialogVisible.value = false;
  if (courseFormRef.value) {
    courseFormRef.value.resetFields(); // Reset fields and validation
    courseFormRef.value.clearValidate();
  }
  currentCourseForm.value = {}; // Clear form data
};

onMounted(() => {
  fetchCourses();
  // Fetch teachers if admin and dialog might open (e.g. for add button, or if edit mode could be triggered)
  // This is handled on demand now when add/edit dialog opens for admin.
});

// Watch for changes that might require reloading data, e.g., user login
watch(() => userStore.userInfo?.id, (newId, oldId) => {
  if (newId !== oldId) {
    fetchCourses();
  }
});

</script>

<style scoped>
/* Styles from CourseManagementView.vue can be copied here or be global if applicable */
.dialog-footer {
  text-align: right;
}

/* Add any specific styles for TeacherCourseManagement.vue if needed */
</style> 