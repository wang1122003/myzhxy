<template>
  <div class="course-management">
    <el-card class="page-container">
      <template #header>
        <div class="header">
          <span>课程管理</span>
          <el-button :icon="Plus" type="primary" @click="handleAdd">添加课程</el-button>
        </div>
      </template>

      <!-- 筛选区域 -->
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="课程名称/编号">
          <el-input v-model="filters.keyword" clearable placeholder="请输入课程名称或编号"/>
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="filters.courseType" clearable placeholder="请选择课程类型">
            <el-option label="全部" value=""/>
            <el-option label="必修课" value="COMPULSORY"/>
            <el-option label="选修课" value="ELECTIVE"/>
            <el-option label="通识课" value="GENERAL"/>
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院">
          <el-select v-model="filters.collegeId" clearable filterable placeholder="请选择学院">
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
          <el-select v-model="filters.status" clearable placeholder="请选择课程状态">
            <el-option label="全部" value=""/>
            <el-option label="启用" value="1"/>
            <el-option label="禁用" value="0"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="fetchCourses">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 课程列表 -->
      <el-table
          :data="courseList"
          v-loading="loading"
          style="width: 100%"
      >
        <el-table-column
            label="课程名称"
            prop="courseName"
            min-width="180"
        />
        <el-table-column
            label="课程编号"
            prop="courseNo"
            width="120"
        />
        <el-table-column
            label="学分"
            prop="credits"
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
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            width="150"
        >
          <template #default="scope">
            <el-button
                :icon="Edit"
                circle
                size="small"
                type="primary"
                @click="handleEdit(scope.row)"
            />
            <el-button
                :icon="Delete"
                circle
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
            />
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无课程数据"/>
        </template>
      </el-table>

      <!-- 分页 -->
      <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          class="pagination-container"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 添加/编辑 对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="isEditMode ? '编辑课程' : '添加课程'"
        width="600px"
        @close="handleDialogClose"
    >
      <CourseForm
          ref="courseFormRef"
          :course-data="currentCourse"
          :is-edit="isEditMode"
          :loading="formLoading"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button :loading="submitting" type="primary" @click="handleSubmit">
            {{ isEditMode ? '保存' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {
  ElButton,
  ElCard,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElSwitch
} from 'element-plus';
import {Plus, Search, Edit, Delete} from '@element-plus/icons-vue';
import {addCourse, deleteCourse, getCourseList, updateCourse} from '@/api/course';
import {getTeacherList} from '@/api/user';
import {getAllTerms} from '@/api/term';

const loading = ref(false);
const courseList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  keyword: '',
  status: null,
  termId: null,
});

const dialogVisible = ref(false);
const dialogTitle = ref('添加课程');
const courseFormRef = ref(null);
const courseForm = ref({
  id: null,
  courseNo: '',
  courseName: '',
  credit: 0.0,
  courseType: null,
  collegeId: null,
  collegeName: '',
  introduction: '',
  status: 1,
});

const courseFormRules = reactive({
  courseNo: [
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
});

const isEditMode = computed(() => !!courseForm.value.id);

const teacherList = ref([]);
const termList = ref([]);

const fetchCourses = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status,
      termId: searchParams.termId || undefined,
    };
    const res = await getCourseList(params);
    courseList.value = res.data.records || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取课程列表失败", error);
    ElMessage.error("获取课程列表失败");
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
        fetchCourses();
      }
    } else {
      ElMessage.error(res.message || '获取学期列表失败');
    }
  } catch (error) {
    console.error("获取学期列表失败", error);
    ElMessage.error('获取学期列表时发生错误');
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchCourses();
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchCourses();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchCourses();
};

const resetForm = () => {
  if (courseFormRef.value) {
    courseFormRef.value.resetFields();
  }
  courseForm.value = {
    id: null,
    courseNo: '',
    courseName: '',
    credit: 0.0,
    courseType: null,
    collegeId: null,
    collegeName: '',
    introduction: '',
    status: 1,
  };
};

const handleAddCourse = () => {
  resetForm();
  dialogTitle.value = '添加课程';
  dialogVisible.value = true;
};

const handleEditCourse = (row) => {
  resetForm();
  dialogTitle.value = '编辑课程';
  courseForm.value = {...row};
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
      fetchCourses();
    } catch (error) {
      console.error("删除课程失败", error);
      ElMessage.error("删除课程失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const submitCourseForm = () => {
  courseFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEditMode.value) {
          const {id, ...updateData} = courseForm.value;
          await updateCourse(id, updateData);
          ElMessage.success('课程信息更新成功');
        } else {
          await addCourse(courseForm.value);
          ElMessage.success('课程添加成功');
        }
        dialogVisible.value = false;
        await fetchCourses();
      } catch (error) {
        console.error("提交课程表单失败", error);
      }
    } else {
      console.log('课程表单验证失败');
      return false;
    }
  });
};

const formatTime = (timeStr) => {
  if (!timeStr) return '-';
  try {
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {hour12: false});
  } catch (e) {
    return timeStr;
  }
};

const handleTermChange = () => {
  currentPage.value = 1;
  fetchCourses();
};

onMounted(() => {
  fetchTerms();
  fetchTeachers();
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
  /* 样式 */
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  text-align: right;
}
</style>