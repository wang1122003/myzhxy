<template>
  <div class="course-management-container">
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button
          type="primary"
          @click="handleAddCourse"
      >
        <el-icon>
          <Plus/>
        </el-icon>
        添加课程
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form
          :inline="true"
          :model="searchParams"
          @submit.prevent="handleSearch"
      >
        <el-form-item label="关键词">
          <el-input
              v-model="searchParams.keyword"
              clearable
              placeholder="课程代码/课程名称"
              style="width: 250px;"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
              v-model="searchParams.status"
              clearable
              placeholder="选择状态"
              style="width: 120px;"
          >
            <el-option
                :value="1"
                label="正常"
            />
            <el-option
                :value="0"
                label="禁用"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              @click="handleSearch"
          >
            查询
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 课程列表 -->
    <el-card class="course-list-card">
      <el-table
          v-loading="loading"
          :data="courseList"
          style="width: 100%"
      >
        <el-table-column
            label="课程代码"
            prop="courseCode"
            width="150"
        />
        <el-table-column
            label="课程名称"
            min-width="200"
            prop="courseName"
        />
        <el-table-column
            label="学分"
            prop="credit"
            width="80"
        />
        <el-table-column
            label="学时"
            prop="hours"
            width="80"
        />
        <!-- 可以添加院系、类型等字段 -->
        <el-table-column
            label="创建时间"
            prop="createTime"
            width="180"
        >
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            width="150"
        >
          <template #default="scope">
            <el-button
                size="small"
                type="primary"
                @click="handleEditCourse(scope.row)"
            >
              编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDeleteCourse(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div
          v-if="total > 0"
          class="pagination-container"
      >
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑课程对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        @close="resetForm"
    >
      <el-form
          ref="courseFormRef"
          :model="courseForm"
          :rules="courseFormRules"
          label-width="100px"
      >
        <el-form-item
            label="课程代码"
            prop="courseNo"
        >
          <el-input
              v-model="courseForm.courseNo"
              :disabled="isEditMode"
              placeholder="请输入课程代码"
          />
        </el-form-item>
        <el-form-item
            label="课程名称"
            prop="courseName"
        >
          <el-input
              v-model="courseForm.courseName"
              placeholder="请输入课程名称"
          />
        </el-form-item>
        <el-form-item
            label="学分"
            prop="credit"
        >
          <el-input-number
              v-model="courseForm.credit"
              :min="0"
              :precision="1"
              :step="0.5"
              placeholder="请输入学分"
          />
        </el-form-item>
        <el-form-item
            label="所属学院ID"
            prop="collegeId"
        >
          <el-input
              v-model.number="courseForm.collegeId"
              placeholder="请输入所属学院ID (可选)"
          />
        </el-form-item>
        <el-form-item
            label="所属学院"
            prop="collegeName"
        >
          <el-input
              v-model="courseForm.collegeName"
              placeholder="请输入所属学院名称 (可选)"
          />
        </el-form-item>
        <el-form-item
            label="课程类型"
            prop="courseType"
        >
          <el-select
              v-model="courseForm.courseType"
              placeholder="选择课程类型 (可选)"
          >
            <el-option
                :value="1"
                label="必修课"
            />
            <el-option
                :value="2"
                label="选修课"
            />
          </el-select>
        </el-form-item>
        <el-form-item
            label="状态"
            prop="status"
        >
          <el-switch
              v-model="courseForm.status"
              :active-value="1"
              :inactive-value="0"
              active-text="已开课"
              inactive-text="未开课"
          />
        </el-form-item>
        <el-form-item
            label="课程简介"
            prop="introduction"
        >
          <el-input
              v-model="courseForm.introduction"
              :rows="3"
              placeholder="请输入课程简介(可选)"
              type="textarea"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              @click="submitCourseForm"
          >确定</el-button>
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
  ElSwitch,
  ElTable,
  ElTableColumn
} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import {addCourse, deleteCourse, getCourseList, updateCourse} from '@/api/course';

const loading = ref(false);
const courseList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  keyword: '',
  status: null
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

const fetchCourses = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || null,
      status: searchParams.status
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

onMounted(() => {
  fetchCourses();
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