<template>
  <el-form
      ref="courseFormRef"
      v-loading="loading"
      :model="form"
      :rules="rules"
      label-width="100px"
  >
    <el-form-item label="课程名称" prop="courseName">
      <el-input v-model="form.courseName" placeholder="请输入课程名称"/>
    </el-form-item>
    <el-form-item label="课程编号" prop="courseNo">
      <el-input v-model="form.courseNo" :disabled="isEdit" placeholder="请输入课程编号"/>
    </el-form-item>
    <el-form-item label="学分" prop="credits">
      <el-input-number v-model="form.credits" :min="0" :precision="1" :step="0.5" placeholder="请输入学分"/>
    </el-form-item>
    <el-form-item label="课程类型" prop="courseType">
      <el-select v-model="form.courseType" placeholder="请选择课程类型">
        <el-option label="必修课" value="COMPULSORY"/>
        <el-option label="选修课" value="ELECTIVE"/>
        <el-option label="通识课" value="GENERAL"/>
        <!-- Add more types if needed -->
      </el-select>
    </el-form-item>
    <el-form-item label="学院/部门" prop="collegeId">
      <el-select
          v-model="form.collegeId"
          :loading="loadingColleges"
          clearable
          filterable
          placeholder="请选择所属学院"
      >
        <el-option
            v-for="college in collegeOptions"
            :key="college.id"
            :label="college.name"
            :value="college.id"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="课程描述" prop="description">
      <el-input
          v-model="form.description"
          :rows="3"
          placeholder="请输入课程描述"
          type="textarea"
      />
    </el-form-item>
  </el-form>
</template>

<script setup>
import {ref, reactive, watch, defineProps, defineExpose, nextTick, onMounted} from 'vue';
import {
  ElForm, ElFormItem, ElInput, ElInputNumber, ElSelect, ElOption, ElMessage
} from 'element-plus';
import formRules from '@/utils/formRules';
import {getColleges} from '@/api/common';

const props = defineProps({
  courseData: { // Used for editing
    type: Object,
    default: () => ({})
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const courseFormRef = ref(null);
const form = ref({
  id: null,
  courseName: '',
  courseNo: '',
  credits: null,
  courseType: null, // e.g., 'COMPULSORY', 'ELECTIVE'
  description: '',
  collegeId: null,
  // teacherId: null // Teacher ID should likely be handled server-side or passed separately
});

const rules = reactive({
  courseName: [formRules.required('课程名称')],
  courseNo: [
    formRules.required('课程编号'),
    // Add regex validation for course number format if needed
  ],
  credits: [formRules.required('学分')],
  courseType: [formRules.required('课程类型')],
  collegeId: [formRules.required('所属学院')]
});

const collegeOptions = ref([]);
const loadingColleges = ref(false);

// Function to fetch colleges
const fetchColleges = async () => {
  loadingColleges.value = true;
  try {
    const res = await getColleges(); // Call the API
    if (res.code === 200 && Array.isArray(res.data)) {
      collegeOptions.value = res.data;
    } else {
      ElMessage.error(res.message || '获取学院列表失败');
      collegeOptions.value = []; // Reset on failure
    }
  } catch (error) {
    console.error("获取学院列表失败:", error);
    ElMessage.error('获取学院列表时发生错误');
    collegeOptions.value = [];
  } finally {
    loadingColleges.value = false;
  }
};

// Fetch colleges when the component is mounted
onMounted(() => {
  fetchColleges();
});

// Watch for external changes to courseData (when editing)
watch(() => props.courseData, (newData) => {
  if (newData && Object.keys(newData).length > 0) {
    // Use nextTick to ensure the form is updated after potential DOM updates
    nextTick(() => {
      form.value = {...newData};
    });
  } else {
    // Reset form if courseData is cleared or empty
    resetForm();
  }
}, {immediate: true, deep: true});

const validate = () => {
  return courseFormRef.value?.validate();
};

const resetForm = () => {
  if (courseFormRef.value) {
    courseFormRef.value.resetFields();
  }
  form.value = {
    id: null,
    courseName: '',
    courseNo: '',
    credits: null,
    courseType: null,
    description: '',
    collegeId: null,
  };
};

// Expose methods to the parent component
defineExpose({
  validate,
  resetForm,
  getFormData: () => form.value // Method to get current form data
});

</script>

<style scoped>
/* Add component-specific styles here if needed */

/* Removed unused selector: .el-input-number */
</style> 