<template>
  <PageContainer :title="title">
    <template v-if="showAddButton" #header-actions>
      <el-button :icon="Plus" type="primary" @click="handleAdd">
        {{ addButtonText }}
      </el-button>
    </template>

    <FilterForm
        v-if="showFilter"
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
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="isEditMode ? '编辑课程信息' : '添加课程'"
        width="600px"
        @close="handleDialogClose"
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
        <el-form-item label="课程介绍" prop="introduction">
          <el-input v-model="currentCourseForm.introduction" :rows="4" placeholder="请输入课程介绍"
                    type="textarea"></el-input>
        </el-form-item>
        <el-form-item v-if="role === 'admin' && isEditMode" label="教师" prop="teacherId">
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
    </el-dialog>

    <!-- 使用通用课程详情对话框 -->
    <CourseDetailDialog
        v-model="detailDialogVisible"
        :course-id="currentCourseId"
        :initial-course="currentCourse"
        :role="role"
        @close="detailDialogVisible = false"
    >
      <template #actions>
        <el-button type="primary" @click="handleEdit(currentCourse)">编辑</el-button>
      </template>
    </CourseDetailDialog>
  </PageContainer>
</template>

<script setup>
import {computed, nextTick, onMounted, reactive, ref, watch} from 'vue';
import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElSelect
} from 'element-plus';
import {Delete, Edit, Plus, View} from '@element-plus/icons-vue';
import {addCourse, deleteCourse, getAllCourses, getCourseById, getTeacherCourses, updateCourse} from '@/api/course';
import {getTeacherList} from '@/api/user';
import {useUserStore} from '@/stores/userStore';
import PageContainer from '@/components/common/EnhancedPageContainer.vue';
import TableView from '@/components/common/TableView.vue';
import FilterForm from '@/components/common/AdvancedFilterForm.vue';
import CourseDetailDialog from '@/components/course/CourseDetailDialog.vue';

// 接收角色类型和配置选项
const props = defineProps({
  role: {
    type: String,
    required: true,
    validator: (value) => ['teacher', 'admin'].includes(value)
  },
  title: {
    type: String,
    default: '课程管理'
  },
  showAddButton: {
    type: Boolean,
    default: true
  },
  addButtonText: {
    type: String,
    default: '添加课程'
  },
  showFilter: {
    type: Boolean,
    default: true
  }
});

// 用户信息
const userStore = useUserStore();

// 状态
const loading = ref(false);
const formLoading = ref(false);
const submitting = ref(false);
const detailLoading = ref(false);
const teacherLoading = ref(false);
const courseList = ref([]);
const teacherList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索参数
const searchParams = reactive({
  keyword: '',
  courseType: ''
});

// 对话框状态
const dialogVisible = ref(false);
const detailDialogVisible = ref(false);
const isEditMode = ref(false);
const currentCourseForm = ref({});
const currentCourse = ref(null);
const currentCourseId = ref(null);
const courseFormRef = ref(null);

// 表单规则
const courseFormRules = reactive({
  courseCode: [{required: true, message: '请输入课程编号', trigger: 'blur'}],
  courseName: [{required: true, message: '请输入课程名称', trigger: 'blur'}],
  credit: [{required: true, type: 'number', message: '请输入学分', trigger: 'blur'}],
  hours: [{required: true, type: 'number', message: '请输入课时', trigger: 'blur'}],
  courseType: [{required: true, message: '请选择课程类型', trigger: 'change'}],
});

// 过滤器项
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

// 表格列定义
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

  // 在管理员视图中添加教师列
  if (props.role === 'admin') {
    columns.push({prop: 'teacherName', label: '教师', width: 120});
  }

  return columns;
});

// 表格操作列配置
const actionColumnConfig = computed(() => {
  const buttons = [
    {label: '查看', type: 'primary', event: 'view-detail', icon: View, link: true},
    {label: '编辑', type: 'primary', event: 'edit-item', icon: Edit}
  ];

  // 管理员可以删除课程
  if (props.role === 'admin') {
    buttons.push({label: '删除', type: 'danger', event: 'delete-item', icon: Delete});
  }

  return {
    width: 180,
    fixed: 'right',
    buttons
  };
});

// --- 方法 ---

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || undefined,
      courseType: searchParams.courseType || undefined
    };

    console.log("课程请求参数:", params, "用户角色:", props.role);
    let res;
    if (props.role === 'teacher') {
      // 对于教师，只获取自己的课程
      // 正确获取教师ID
      let teacherId = null;
      if (userStore.userInfo && userStore.userInfo.userId) {
        teacherId = userStore.userInfo.userId;
      } else if (typeof userStore.userId === 'function') {
        teacherId = userStore.userId();
      } else {
        teacherId = userStore.userId;
      }

      // 确保teacherId是有效值
      if (teacherId !== null && teacherId !== undefined) {
        params.teacherId = teacherId;
        console.log("使用教师ID:", teacherId);
      } else {
        console.warn("无法获取有效的教师ID");
      }

      res = await getTeacherCourses(params);
    } else {
      // 对于管理员，获取所有课程
      res = await getAllCourses(params);
    }
    console.log("课程响应数据:", res);

    // 更灵活地处理不同格式的响应
    if (res.data && res.data.records) {
      courseList.value = res.data.records;
      total.value = res.data.total || 0;
    } else if (Array.isArray(res.data)) {
      courseList.value = res.data;
      total.value = res.data.length;
    } else if (Array.isArray(res)) {
      courseList.value = res;
      total.value = res.length;
    } else if (res && res.records) {
      courseList.value = res.records;
      total.value = res.total || res.records.length;
    } else {
      courseList.value = [];
      total.value = 0;
    }
    console.log("处理后的课程列表:", courseList.value, "总数:", total.value);
  } catch (error) {
    console.error("获取课程列表失败:", error);
    ElMessage.error("获取课程列表失败");
    courseList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 获取教师列表 (仅管理员使用)
const fetchTeachers = async () => {
  if (props.role !== 'admin') return;

  teacherLoading.value = true;
  try {
    console.log("正在获取教师列表...");
    const res = await getTeacherList();
    console.log("获取教师列表响应:", res);

    // 更灵活地处理响应数据结构
    if (res.data && Array.isArray(res.data)) {
      teacherList.value = res.data;
    } else if (res.data && res.data.records && Array.isArray(res.data.records)) {
      teacherList.value = res.data.records;
    } else if (Array.isArray(res)) {
      teacherList.value = res;
    } else {
      teacherList.value = [];
      console.warn("教师列表数据格式不符合预期:", res);
    }

    console.log("处理后的教师列表:", teacherList.value);
  } catch (error) {
    console.error("获取教师列表失败:", error);
    teacherList.value = [];
  } finally {
    teacherLoading.value = false;
  }
};

// 格式化课程类型
const formatCourseType = (type) => {
  const typeMap = {COMPULSORY: '必修课', ELECTIVE: '选修课', GENERAL: '通识课'};
  return typeMap[type] || type;
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchCourses();
};

// 重置搜索
const handleReset = () => {
  searchParams.keyword = '';
  searchParams.courseType = '';
  currentPage.value = 1;
  fetchCourses();
};

// 添加课程
const handleAdd = () => {
  isEditMode.value = false;
  currentCourseForm.value = {credit: 0, hours: 0};
  dialogVisible.value = true;
  nextTick(() => courseFormRef.value?.clearValidate());
};

// 查看课程详情
const handleViewDetail = (row) => {
  detailDialogVisible.value = true;
  currentCourse.value = {...row}; // 浅拷贝基础信息
  currentCourseId.value = row.id;
};

// 编辑课程
const handleEdit = async (row) => {
  isEditMode.value = true;
  formLoading.value = true;
  dialogVisible.value = true;
  currentCourseForm.value = {};

  try {
    const res = await getCourseById(row.id);
    currentCourseForm.value = {...res.data};

    // 如果是管理员，确保有教师列表可选
    if (props.role === 'admin' && teacherList.value.length === 0) {
      await fetchTeachers();
    }
  } catch (error) {
    console.error("获取课程详情失败:", error);
    ElMessage.error("获取课程详情失败");
    dialogVisible.value = false;
  } finally {
    formLoading.value = false;
  }
};

// 删除课程
const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确定要删除课程 "${row.courseName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      await deleteCourse(row.id);
      ElMessage.success('删除成功');
      fetchCourses();
    } catch (error) {
      console.error("删除课程失败:", error);
      ElMessage.error("删除课程失败");
    }
  }).catch(() => {
    // 用户取消，不做任何处理
  });
};

// 提交表单
const handleSubmit = async () => {
  if (!courseFormRef.value) return;

  await courseFormRef.value.validate(async (valid) => {
    if (!valid) return;

    submitting.value = true;
    try {
      if (isEditMode.value) {
        // 更新课程
        await updateCourse(currentCourseForm.value.id, currentCourseForm.value);
        ElMessage.success('课程更新成功');
      } else {
        // 添加课程
        // 获取教师ID
        let teacherId = null;
        if (props.role === 'teacher') {
          if (userStore.userInfo && userStore.userInfo.userId) {
            teacherId = userStore.userInfo.userId;
          } else if (typeof userStore.userId === 'function') {
            teacherId = userStore.userId();
          } else {
            teacherId = userStore.userId;
          }
        } else {
          teacherId = currentCourseForm.value.teacherId;
        }

        await addCourse({
          ...currentCourseForm.value,
          teacherId: teacherId
        });
        ElMessage.success('课程添加成功');
      }
      dialogVisible.value = false;
      fetchCourses();
    } catch (error) {
      console.error(isEditMode.value ? "更新课程失败:" : "添加课程失败:", error);
      ElMessage.error(isEditMode.value ? "更新课程失败" : "添加课程失败");
    } finally {
      submitting.value = false;
    }
  });
};

// 对话框关闭处理
const handleDialogClose = () => {
  currentCourseForm.value = {};
  courseFormRef.value?.clearValidate();
};

// --- 生命周期 ---
onMounted(() => {
  fetchCourses();
  if (props.role === 'admin') {
    fetchTeachers();
  }
});

// 当页码或页大小变化时重新获取数据
watch([currentPage, pageSize], () => {
  fetchCourses();
});
</script> 