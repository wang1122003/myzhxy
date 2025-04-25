<template>
  <div class="teacher-course-management-container">
    <el-card class="page-container">
      <template #header>
        <div class="header">
          <span>我教授的课程</span>
          <el-button :icon="Plus" type="primary" @click="handleAdd">添加课程</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="courseList" style="width: 100%">
        <el-table-column label="课程名称" prop="courseName"/>
        <el-table-column label="课程编号" prop="courseNo"/>
        <el-table-column label="学分" prop="credits" width="80"/>
        <el-table-column label="课程类型" prop="courseType" width="100">
          <template #default="scope">
            {{ formatCourseType(scope.row.courseType) }}
          </template>
        </el-table-column>
        <el-table-column label="所属学院" prop="collegeName"/> <!-- Assuming backend provides collegeName -->
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button :icon="Edit" circle size="small" type="primary" @click="handleEdit(scope.row)"/>
            <el-button :icon="Delete" circle size="small" type="danger" @click="handleDelete(scope.row)"/>
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
        :title="isEditMode ? '编辑课程' : '添加课程'"
        width="600px"
        @close="handleDialogClose"
    >
      <CourseForm
          ref="courseFormRef"
          :courseData="currentCourse"
          :isEdit="isEditMode"
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
import {ref, onMounted, computed} from 'vue';
import {
  ElCard, ElButton, ElTable, ElTableColumn, ElPagination, ElDialog, ElMessage, ElMessageBox, ElEmpty
} from 'element-plus';
import {Plus, Edit, Delete} from '@element-plus/icons-vue';
import {getTeacherCourses, addCourse, updateCourse, deleteCourse, getCourseById} from '@/api/course';
import CourseForm from '@/components/course/CourseForm.vue';

const loading = ref(false);
const formLoading = ref(false);
const submitting = ref(false);
const courseList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

dialogVisible = ref(false);
const isEditMode = ref(false);
const currentCourse = ref({});
const courseFormRef = ref(null);

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      // Add any other necessary params like sorting
    };
    const res = await getTeacherCourses(params);
    if (res.code === 200 && res.data) {
      courseList.value = res.data.records || [];
      total.value = res.data.total || 0;
    } else {
      ElMessage.error(res.message || '获取课程列表失败');
      courseList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取教师课程失败:", error);
    ElMessage.error('获取课程列表时发生错误');
    courseList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 格式化课程类型 (可以移到 utils)
const formatCourseType = (type) => {
  const typeMap = {
    COMPULSORY: '必修课',
    ELECTIVE: '选修课',
    GENERAL: '通识课'
  };
  return typeMap[type] || type;
};

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1; // Reset to first page
  fetchCourses();
};
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchCourses();
};

// 打开添加对话框
const handleAdd = () => {
  isEditMode.value = false;
  currentCourse.value = {}; // Clear current data
  dialogVisible.value = true;
};

// 打开编辑对话框
const handleEdit = async (row) => {
  isEditMode.value = true;
  formLoading.value = true;
  dialogVisible.value = true;
  try {
    // Fetch full details if needed, or use row data directly if sufficient
    const res = await getCourseById(row.id);
    if (res.code === 200) {
      currentCourse.value = res.data;
    } else {
      ElMessage.error(res.message || '获取课程详情失败');
      dialogVisible.value = false;
    }
  } catch (error) {
    console.error('获取课程详情失败:', error);
    ElMessage.error('获取课程详情失败');
    dialogVisible.value = false;
  } finally {
    formLoading.value = false;
  }
};

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除课程 "${row.courseName}" 吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
      .then(async () => {
        try {
          loading.value = true; // Indicate loading state
          await deleteCourse(row.id);
          ElMessage.success('删除成功');
          // Refresh list, potentially staying on the same page or going to previous if last item deleted
          if (courseList.value.length === 1 && currentPage.value > 1) {
            currentPage.value--;
          }
          fetchCourses();
        } catch (error) {
          console.error("删除课程失败:", error);
          ElMessage.error(error?.response?.data?.message || '删除失败');
          loading.value = false; // Reset loading on error
        }
      })
      .catch(() => {
        // User cancelled
      });
};

// 处理对话框关闭
const handleDialogClose = () => {
  courseFormRef.value?.resetForm();
  currentCourse.value = {}; // Clear data when dialog closes
};

// 提交表单
const handleSubmit = async () => {
  try {
    const isValid = await courseFormRef.value?.validate();
    if (!isValid) return;

    submitting.value = true;
    const formData = courseFormRef.value?.getFormData();

    if (isEditMode.value) {
      // Update existing course
      await updateCourse(formData.id, formData);
      ElMessage.success('更新成功');
    } else {
      // Add new course
      await addCourse(formData);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    fetchCourses(); // Refresh the list
  } catch (error) {
    console.error("提交课程失败:", error);
    ElMessage.error(error?.response?.data?.message || (isEditMode.value ? '更新失败' : '添加失败'));
  } finally {
    submitting.value = false;
  }
};

// 初始化加载
onMounted(() => {
  fetchCourses();
});
</script>

<style scoped>
.teacher-course-management-container {
  padding: 20px;
}

.page-container {
  min-height: 600px; /* Adjust as needed */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end; /* Align pagination to the right */
}

.dialog-footer {
  text-align: right;
}
</style> 