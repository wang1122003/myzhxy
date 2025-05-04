<template>
  <!-- 课程信息表单 -->
  <el-form
      ref="courseFormRef"       <!-- 表单引用 -->
  v-loading="loading"       <!-- 加载状态 -->
  :model="form"            <!-- 表单数据模型 -->
  :rules="rules"           <!-- 表单验证规则 -->
  label-width="100px"       <!-- 标签宽度 -->
  >
  <!-- 课程名称表单项 -->
    <el-form-item label="课程名称" prop="courseName">
      <el-input v-model="form.courseName" placeholder="请输入课程名称"/>
    </el-form-item>
  <!-- 课程编号表单项 -->
    <el-form-item label="课程编号" prop="courseNo">
      <!-- 编辑模式下禁用课程编号输入 -->
      <el-input v-model="form.courseNo" :disabled="isEdit" placeholder="请输入课程编号"/>
    </el-form-item>
  <!-- 学分表单项 -->
    <el-form-item label="学分" prop="credits">
      <!-- 数字输入框，最小值为 0，精度为 1 位小数，步长为 0.5 -->
      <el-input-number v-model="form.credits" :min="0" :precision="1" :step="0.5" placeholder="请输入学分"/>
    </el-form-item>
  <!-- 课程类型表单项 -->
    <el-form-item label="课程类型" prop="courseType">
      <!-- 下拉选择框 -->
      <el-select v-model="form.courseType" placeholder="请选择课程类型">
        <el-option label="必修课" value="COMPULSORY"/>
        <el-option label="选修课" value="ELECTIVE"/>
        <el-option label="通识课" value="GENERAL"/>
        <!-- 如果需要更多课程类型，在此添加 -->
      </el-select>
    </el-form-item>
  <!-- 所属学院/部门表单项 -->
    <el-form-item label="学院/部门" prop="collegeId">
      <!-- 下拉选择框，支持过滤和清空 -->
      <el-select
          v-model="form.collegeId"
          :loading="loadingColleges" <!-- 学院列表加载状态 -->
      clearable                 <!-- 可清空 -->
      filterable                <!-- 可搜索 -->
          placeholder="请选择所属学院"
      >
      <!-- 遍历学院选项 -->
        <el-option
            v-for="college in collegeOptions"
            :key="college.id"        <!-- 使用学院 ID 作为 key -->
      :label="college.name"    <!-- 显示学院名称 -->
      :value="college.id"      <!-- 选中时绑定学院 ID -->
        />
      </el-select>
    </el-form-item>
  <!-- 课程描述表单项 -->
    <el-form-item label="课程描述" prop="description">
      <!-- 多行文本输入框 -->
      <el-input
          v-model="form.description"
          :rows="3"                 <!-- 显示行数 -->
          placeholder="请输入课程描述"
          type="textarea"
      />
    </el-form-item>
  </el-form>
</template>

<script setup>
import {defineExpose, defineProps, nextTick, onMounted, reactive, ref, watch} from 'vue';
import {ElForm, ElFormItem, ElInput, ElInputNumber, ElMessage, ElOption, ElSelect} from 'element-plus';
// 引入表单验证规则工具
import formRules from '@/utils/formRules';
// 引入获取学院列表的 API
import {getColleges} from '@/api/common';

// 定义组件 props
const props = defineProps({
  // 外部传入的课程数据，用于编辑场景
  courseData: {
    type: Object,
    default: () => ({})
  },
  // 是否处于编辑模式 (编辑模式下，课程编号不可修改)
  isEdit: {
    type: Boolean,
    default: false
  },
  // 控制整个表单的加载状态
  loading: {
    type: Boolean,
    default: false
  }
});

// 表单实例引用
const courseFormRef = ref(null);
// 表单数据模型 (使用 ref 包裹对象使其成为响应式)
const form = ref({
  id: null,           // 课程 ID (编辑时使用)
  courseName: '',    // 课程名称
  courseNo: '',      // 课程编号
  credits: null,      // 学分
  courseType: null,   // 课程类型 (例如: 'COMPULSORY', 'ELECTIVE')
  description: '',   // 课程描述
  collegeId: null,    // 所属学院 ID
  // teacherId: null // 教师 ID 通常应该在后端处理或由父组件单独传递，不在表单内维护
});

// 表单验证规则 (使用 reactive 使规则对象本身响应式)
const rules = reactive({
  courseName: [formRules.required('课程名称')],
  courseNo: [
    formRules.required('课程编号'),
    // 如果需要，可以添加课程编号格式的正则表达式验证
    // formRules.pattern(/^[A-Za-z0-9]+$/, '课程编号格式不正确')
  ],
  credits: [formRules.required('学分')],
  courseType: [formRules.required('课程类型')],
  collegeId: [formRules.required('所属学院')]
});

// 学院下拉选项列表
const collegeOptions = ref([]);
// 控制学院下拉列表的加载状态
const loadingColleges = ref(false);

/**
 * 异步获取学院列表数据
 */
const fetchColleges = async () => {
  loadingColleges.value = true; // 开始加载
  try {
    const res = await getColleges(); // 调用 API 获取学院数据
    // API 调用成功且返回的数据是数组
    if (res.code === 200 && Array.isArray(res.data)) {
      collegeOptions.value = res.data; // 更新学院选项
    } else {
      ElMessage.error(res.message || '获取学院列表失败'); // 显示错误提示
      collegeOptions.value = []; // 获取失败时清空选项
    }
  } catch (error) {
    console.error("获取学院列表失败:", error); // 在控制台打印错误
    ElMessage.error('获取学院列表时发生错误'); // 显示通用错误提示
    collegeOptions.value = []; // 清空选项
  } finally {
    loadingColleges.value = false; // 结束加载
  }
};

// 组件挂载后执行: 获取学院列表
onMounted(() => {
  fetchColleges();
});

// 监听外部传入的 courseData prop 的变化 (用于编辑场景)
watch(() => props.courseData, (newData) => {
  // 如果传入了新的数据且数据不为空对象
  if (newData && Object.keys(newData).length > 0) {
    // 使用 nextTick 确保在 DOM 更新之后再修改表单值，防止潜在问题
    nextTick(() => {
      // 遍历初始 form 定义的 key，用 newData 中对应的值覆盖 form.value
      // 这样做可以避免将 newData 中多余的属性赋值给 form
      Object.keys(form.value).forEach(key => {
        // 检查 newData 中是否存在该 key
        if (newData.hasOwnProperty(key)) {
          form.value[key] = newData[key];
        }
      });
      // 显式地设置 id (如果 newData 中存在)，因为 id 通常是关键标识符
      if (newData.id) {
        form.value.id = newData.id;
      }
    });
  } else {
    // 如果 courseData 被清空或变为空对象，则重置表单
    // 注意：如果在父组件中，编辑后不清空 courseData，这里可能不会触发重置
    // 如果需要在非编辑状态下总是清空表单，可能需要其他逻辑或父组件控制
    // resetForm(); // 这里调用 resetForm 可能会在编辑初始化时被意外触发，看具体需求
  }
}, {immediate: true, deep: true}); // immediate: 立即执行一次; deep: 深度监听对象内部变化

/**
 * 触发表单验证
 * @returns {Promise<boolean>} 返回一个 Promise，resolve 时表示验证成功，reject 时表示失败
 */
const validate = () => {
  // 调用 el-form 的 validate 方法
  return courseFormRef.value?.validate();
};

/**
 * 重置表单到初始状态
 */
const resetForm = () => {
  // 如果表单实例存在，调用 el-form 的 resetFields 方法
  if (courseFormRef.value) {
    courseFormRef.value.resetFields(); // 重置表单项为初始值并移除校验结果
  }
  // 手动将 form.value 重置为初始的空状态对象
  // 因为 resetFields 可能只重置到上一次设置的值，而不是完全清空
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

// 使用 defineExpose 将方法暴露给父组件，父组件可以通过 ref 调用这些方法
defineExpose({
  validate,    // 暴露验证方法
  resetForm,   // 暴露重置方法
  getFormData: () => form.value // 暴露一个获取当前表单数据的方法
});

</script>

<style scoped>
/* 在这里添加组件特定的样式 */

/* 可以移除未使用的样式选择器，例如之前的 .el-input-number */
</style> 