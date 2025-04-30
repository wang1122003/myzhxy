<template>
  <PageContainer title="我的课程管理">
    <template #actions>
      <!-- Add FilterForm trigger button if needed -->
      <!-- Teachers might not add courses directly, depends on system design -->
      <!-- <el-button :icon="Plus" type="primary" @click="handleAdd">添加课程</el-button> -->
    </template>

    <!-- Add FilterForm definition here if needed -->
    <!--
    <FilterForm
        :model="searchParams"
        :items="filterItems"
        @search="handleSearch"
        @reset="handleReset"
        :show-add-button="false"
    />
    -->

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
    />

    <!-- 添加/编辑 对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        width="600px"
        @close="handleDialogClose"
        :title="isEditMode ? '编辑课程信息' : '添加课程'"
    >
      <!-- Use SmartForm or a dedicated CourseForm component -->
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
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="currentCourseForm.collegeId" filterable placeholder="请选择所属学院" style="width: 100%;">
            <el-option v-for="college in collegeOptions" :key="college.id" :label="college.name" :value="college.id"/>
          </el-select>
        </el-form-item>
        <!-- Add other fields like introduction if needed -->
        <el-form-item label="课程介绍" prop="introduction">
          <el-input v-model="currentCourseForm.introduction" :rows="4" placeholder="请输入课程介绍"
                    type="textarea"></el-input>
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
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElOption,
  ElSelect
} from 'element-plus';
import {Edit} from '@element-plus/icons-vue';
import {getCourseById, getTeacherCourses, updateCourse} from '@/api/course'; // Corrected: Use course.js
import {getColleges} from '@/api/common'; // Corrected: Use common.js and correct function name

const loading = ref(false);
const formLoading = ref(false);
const submitting = ref(false);
const courseList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const collegeOptions = ref([]); // For form select

// Dialog State
const dialogVisible = ref(false);
const isEditMode = ref(false);
const currentCourseForm = ref({}); // Form model separate from table row
const courseFormRef = ref(null);

// Form Rules (example)
const courseFormRules = reactive({
  courseCode: [{required: true, message: '请输入课程编号', trigger: 'blur'}],
  courseName: [{required: true, message: '请输入课程名称', trigger: 'blur'}],
  credit: [{required: true, type: 'number', message: '请输入学分', trigger: 'blur'}],
  hours: [{required: true, type: 'number', message: '请输入课时', trigger: 'blur'}],
  courseType: [{required: true, message: '请选择课程类型', trigger: 'change'}],
  collegeId: [{required: true, message: '请选择所属学院', trigger: 'change'}],
});

// --- Computed Properties ---

// TableView Columns
const tableColumns = computed(() => [
  {prop: 'courseCode', label: '课程编号', width: 120},
  {prop: 'courseName', label: '课程名称', minWidth: 180},
  {prop: 'credit', label: '学分', width: 80},
  {prop: 'hours', label: '课时', width: 80},
  {
    prop: 'courseType',
    label: '课程类型',
    width: 100,
    formatter: (row) => formatCourseType(row.courseType)
  },
  {prop: 'collegeName', label: '所属学院', minWidth: 150},
  // Add status column if applicable for teachers (e.g., '未开课', '已开课')
]);

// TableView Action Column Configuration
const actionColumnConfig = computed(() => ({
  width: 120, // Adjust width if needed
  fixed: 'right',
  buttons: [
    {label: '编辑', type: 'primary', event: 'edit-item', icon: Edit},
    // 移除删除课程按钮
  ]
}));

// --- Methods ---

// Fetch colleges for form select
const fetchColleges = async () => {
  try {
    const res = await getColleges(); // Assuming API exists
    collegeOptions.value = res.data || [];
  } catch (error) {
    console.error("获取学院列表失败:", error);
  }
};

// Fetch teacher's courses
const fetchCourses = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      // Add filters from searchParams if implemented
    };
    const res = await getTeacherCourses(params); // Corrected
    courseList.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (error) {
    console.error("获取教师课程失败:", error);
    // Error handled by interceptor
    courseList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// Format course type
const formatCourseType = (type) => {
  const typeMap = {COMPULSORY: '必修课', ELECTIVE: '选修课', GENERAL: '通识课'};
  return typeMap[type] || type;
};

// Open Add Dialog (if teachers can add)
// const handleAdd = () => {
//   isEditMode.value = false;
//   currentCourseForm.value = { credit: 0, hours: 0 }; // Reset form model
//   dialogVisible.value = true;
//   nextTick(() => courseFormRef.value?.clearValidate());
// };

// Open Edit Dialog
const handleEdit = async (row) => {
  isEditMode.value = true;
  formLoading.value = true;
  dialogVisible.value = true;
  currentCourseForm.value = {}; // Clear previous form data
  try {
    const res = await getCourseById(row.id); // Corrected
    currentCourseForm.value = {...res.data}; // Use fetched data for form
  } catch (error) {
    console.error('获取课程详情失败:', error);
    ElMessage.error('获取课程详情失败');
    dialogVisible.value = false; // Close if fetch fails
  } finally {
    formLoading.value = false;
  }
};

// Dialog close handler
const handleDialogClose = () => {
  courseFormRef.value?.resetFields(); // Reset validation and fields
  currentCourseForm.value = {};
};

// Submit form (Add or Edit)
const handleSubmit = async () => {
  if (!courseFormRef.value) return;
  try {
    const isValid = await courseFormRef.value.validate();
    if (!isValid) return;

    submitting.value = true;
    const payload = {...currentCourseForm.value};

    if (isEditMode.value) {
      await updateCourse(payload.id, payload); // Corrected
      ElMessage.success('课程信息更新成功');
    } else {
      // await teacherAddCourse(payload); // Use teacher API if applicable
      ElMessage.warning('教师添加课程功能暂未实现');
    }
    dialogVisible.value = false;
    await fetchCourses(); // Refresh the list
  } catch (error) {
    console.error("提交课程失败:", error);
    // Error handled by interceptor
  } finally {
    submitting.value = false;
  }
};

// Initial load
onMounted(() => {
  fetchColleges(); // Fetch colleges for the form select
  fetchCourses();
});

</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

/* Add other styles if needed */
</style> 