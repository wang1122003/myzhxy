<template>
  <PageContainer title="课程管理">
    <template #header-actions>
      <el-button type="primary" @click="handleAddCourse">
        <el-icon>
          <Plus/>
        </el-icon>
        添加课程
      </el-button>
      </template>

    <!-- 搜索和筛选 -->
    <template #filters>
      <FilterForm :model="searchParams" @reset="resetSearch" @search="handleSearch">
        <el-form-item label="课程名称/编号">
          <el-input v-model="searchParams.keyword" clearable placeholder="请输入课程名称或编号" style="width: 250px;"/>
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="searchParams.courseType" clearable placeholder="请选择课程类型" style="width: 150px;">
            <el-option label="全部" value=""/>
            <el-option label="必修课" value="COMPULSORY"/>
            <el-option label="选修课" value="ELECTIVE"/>
            <el-option label="通识课" value="GENERAL"/>
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院">
          <el-select v-model="searchParams.collegeId" clearable filterable placeholder="请选择学院"
                     style="width: 200px;">
            <el-option label="全部" value=""/>
            <el-option
                v-for="college in collegeList"
                :key="college.id"
                :label="college.collegeName"
                :value="college.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" clearable placeholder="请选择课程状态" style="width: 120px;">
            <el-option label="全部" value=""/>
            <el-option label="启用" value="1"/>
            <el-option label="禁用" value="0"/>
          </el-select>
        </el-form-item>
      </FilterForm>
    </template>

      <!-- 课程列表 -->
    <TableView
          :data="courseList"
          v-model="pagination"
          :loading="loading"
          :total="total"
          border
          stripe
          @page-change="handlePageChange"
      >
        <el-table-column
            label="课程名称"
            prop="courseName"
            min-width="180"
        />
        <el-table-column
            label="课程编号"
            prop="courseCode"
            width="120"
        />
        <el-table-column
            label="学分"
            prop="credit"
            width="80"
        />
      <el-table-column
          label="课时"
          prop="hours"
            width="80"
        />
        <el-table-column
            label="课程类型"
            prop="courseType"
            width="100"
        >
          <template #default="scope">{{ formatCourseType(scope.row.courseType) }}</template>
        </el-table-column>
        <el-table-column
            label="所属学院"
            min-width="150"
            prop="collegeName"
        />
        <el-table-column
            label="状态"
            prop="status"
            width="80"
        >
          <template #default="scope">
            <el-tag :type="scope.row.status === '1' ? 'success' : 'danger'">
              {{ scope.row.status === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            width="180"
        >
          <template #default="scope">
            <el-button
                size="small"
                type="primary"
                @click="handleEditCourse(scope.row)"
            >编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDeleteCourse(scope.row)"
            >删除
            </el-button>
          </template>
        </el-table-column>
    </TableView>

    <!-- 添加/编辑 对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="isEditMode ? '编辑课程' : '添加课程'"
        width="600px"
        @close="handleDialogClose"
    >
      <FormView
          ref="courseFormRef"
          v-loading="formLoading"
          :model="currentCourse"
          :rules="courseFormRules"
          :submitting="submitting"
          @cancel="dialogVisible = false"
          @submit="submitCourseForm"
      >
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="currentCourse.courseName" placeholder="请输入课程名称"/>
        </el-form-item>
        <el-form-item label="课程编号" prop="courseCode">
          <el-input v-model="currentCourse.courseCode" :disabled="isEditMode" placeholder="请输入课程编号"/>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="currentCourse.credit" :min="0" :precision="1" :step="0.5" placeholder="请输入学分"
                           style="width: 180px;"/>
        </el-form-item>
        <el-form-item label="课时" prop="hours">
          <el-input-number v-model="currentCourse.hours" :min="0" :precision="0" :step="1" placeholder="请输入课时"
                           style="width: 180px;"/>
        </el-form-item>
        <el-form-item label="课程类型" prop="courseType">
          <el-select v-model="currentCourse.courseType" placeholder="请选择课程类型" style="width: 100%;">
            <el-option label="必修课" value="COMPULSORY"/>
            <el-option label="选修课" value="ELECTIVE"/>
            <el-option label="通识课" value="GENERAL"/>
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select
              v-model="currentCourse.collegeId"
              clearable
              filterable
              placeholder="请选择所属学院"
              style="width: 100%;"
          >
            <el-option
                v-for="college in collegeList"
                :key="college.id"
                :label="college.collegeName"
                :value="college.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程描述" prop="introduction">
          <el-input
              v-model="currentCourse.introduction"
              :rows="3"
              placeholder="请输入课程描述"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="currentCourse.status"
              :active-value="'1'"
              :inactive-value="'0'"
              active-text="启用"
              inactive-text="禁用"
          />
        </el-form-item>
      </FormView>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {
  ElButton,
  ElCard,
  ElDialog,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElRadio,
  ElRadioGroup,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag
} from 'element-plus';
import {Delete, Edit, Plus, Search} from '@element-plus/icons-vue';
import {addCourse, deleteCourse, getCourseList, updateCourse} from '@/api/course';
import {getTeacherList} from '@/api/user';
import {getAllTerms} from '@/api/term';
import {getColleges} from '@/api/common';
import PageContainer from '@/components/common/EnhancedPageContainer.vue';
import FilterForm from '@/components/common/AdvancedFilterForm.vue';
import TableView from '@/components/common/TableView.vue';
import FormView from '@/components/common/SmartForm.vue';

const loading = ref(false);
const courseList = ref([]);
const total = ref(0);
const pagination = ref({
  pageNum: 1,
  pageSize: 10
});
const searchParams = reactive({
  keyword: '',
  courseType: '',
  collegeId: '',
  status: '',
  termId: null,
});

const dialogVisible = ref(false);
const courseFormRef = ref(null);
const currentCourse = ref({
  id: null,
  courseCode: '',
  courseName: '',
  credit: 0.0,
  hours: 0,
  courseType: null,
  collegeId: null,
  collegeName: '',
  introduction: '',
  status: '1',
});

const courseFormRules = reactive({
  courseCode: [
    {required: true, message: '请输入课程代码', trigger: 'blur'}
  ],
  courseName: [
    {required: true, message: '请输入课程名称', trigger: 'blur'}
  ],
  credit: [
    {required: true, message: '请输入学分', trigger: 'blur'},
    {type: 'number', message: '学分必须为数字值'},
    {
      validator: (rule, value, callback) => {
        if (value < 0) {
          callback(new Error('学分不能为负数'));
        } else {
          callback();
        }
      }, trigger: 'blur'
    }
  ],
  courseType: [
    {required: true, message: '请选择课程类型', trigger: 'change'}
  ],
  collegeId: [
    {required: true, message: '请选择所属学院', trigger: 'change'}
  ]
});

const isEditMode = computed(() => !!currentCourse.value.id);

const teacherList = ref([]);
const termList = ref([]);
const collegeList = ref([]);
const formLoading = ref(false);
const submitting = ref(false);

const fetchCourseList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      keyword: searchParams.keyword || undefined,
      courseType: searchParams.courseType || undefined,
      collegeId: searchParams.collegeId || undefined,
      status: searchParams.status !== null && searchParams.status !== '' ? searchParams.status : undefined,
      termId: searchParams.termId || undefined
    };
    const res = await getCourseList(params);
    if (res && res.records !== undefined && res.total !== undefined) {
      courseList.value = res.records;
      total.value = res.total;
    } else if (Array.isArray(res)) {
      console.warn('后端 /courses 返回了纯数组，分页信息可能丢失');
      courseList.value = res;
      total.value = res.length;
    } else {
      console.warn('从 /courses 获取的数据格式不符合预期:', res);
      courseList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取课程列表失败", error);
    ElMessage.error(error.message || '加载课程数据时遇到问题，请稍后重试。');
    courseList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const fetchTeachers = async () => {
  try {
    const res = await getTeacherList({userType: 'Teacher'});
    teacherList.value = res.data || [];
  } catch (error) {
    console.error("获取教师列表失败", error);
  }
};

const fetchTerms = async () => {
  try {
    const res = await getAllTerms();
    if (res.code === 200 && res.data) {
      termList.value = res.data;
      const currentTerm = res.data.find(t => t.current === 1);
      if (currentTerm && !searchParams.termId) {
        searchParams.termId = currentTerm.id;
        fetchCourseList();
      }
    } else {
      ElMessage.error(res.message || '获取学期列表失败');
    }
  } catch (error) {
    console.error("获取学期列表失败", error);
    ElMessage.error('获取学期列表时发生错误');
  }
};

const fetchColleges = async () => {
  try {
    const res = await getColleges();
    if (res.code === 200 && res.data) {
      collegeList.value = res.data;
    }
  } catch (error) {
    console.error("获取学院列表失败", error);
    ElMessage.error('获取学院列表时发生错误');
  }
};

const handleSearch = () => {
  pagination.value.pageNum = 1;
  fetchCourseList();
};

const handlePageChange = () => {
  fetchCourseList();
};

const resetSearch = () => {
  searchParams.keyword = '';
  searchParams.courseType = '';
  searchParams.collegeId = '';
  pagination.value.pageNum = 1;
  fetchCourseList();
};

const resetForm = () => {
  if (courseFormRef.value) {
    courseFormRef.value.resetFields();
  }
  currentCourse.value = {
    id: null,
    courseCode: '',
    courseName: '',
    credit: 0.0,
    hours: 0,
    courseType: null,
    collegeId: null,
    collegeName: '',
    introduction: '',
    status: '1',
  };
};

const handleAddCourse = () => {
  resetForm();
  dialogVisible.value = true;
};

const handleEditCourse = (row) => {
  resetForm();
  currentCourse.value = {...row};
  dialogVisible.value = true;
};

const handleDeleteCourse = (row) => {
  ElMessageBox.confirm(`确定要删除课程 ${row.courseName} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCourse(row.id);
      ElMessage.success('删除成功');
      fetchCourseList();
    } catch (error) {
      console.error("删除课程失败", error);
      ElMessage.error(error.message || "删除课程失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const submitCourseForm = async () => {
  submitting.value = true;
      try {
        if (isEditMode.value) {
          await updateCourse(currentCourse.value.id, currentCourse.value);
          ElMessage.success('课程更新成功');
        } else {
          await addCourse(currentCourse.value);
          ElMessage.success('课程添加成功');
        }
        dialogVisible.value = false;
        fetchCourseList();
      } catch (error) {
        console.error('提交表单失败', error);
        ElMessage.error(error.response?.data?.message || '操作失败');
      } finally {
        submitting.value = false;
      }
};

const handleDialogClose = () => {
  resetForm();
};

const formatCourseType = (type) => {
  const types = {
    'COMPULSORY': '必修课',
    'ELECTIVE': '选修课',
    'GENERAL': '通识课'
  };
  return types[type] || type;
};

onMounted(() => {
  fetchCourseList();
  fetchTeachers();
  fetchTerms();
  fetchColleges();
});

</script>

<script>
export default {
  name: 'CourseManagement'
}
</script>

<style scoped>
.course-management-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.course-list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>