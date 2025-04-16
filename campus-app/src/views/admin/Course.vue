<template>
  <div class="course-management-container">
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" @click="handleAddCourse">
        <el-icon>
          <Plus/>
        </el-icon>
        添加课程
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchParams" @submit.prevent="handleSearch">
        <el-form-item label="关键词">
          <el-input v-model="searchParams.keyword" clearable placeholder="课程代码/课程名称" style="width: 250px;"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" clearable placeholder="选择状态" style="width: 120px;">
            <el-option :value="1" label="正常"/>
            <el-option :value="0" label="禁用"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 课程列表 -->
    <el-card class="course-list-card">
      <el-table v-loading="loading" :data="courseList" style="width: 100%">
        <el-table-column label="课程代码" prop="courseCode" width="150"/>
        <el-table-column label="课程名称" min-width="200" prop="courseName"/>
        <el-table-column label="学分" prop="credit" width="80"/>
        <el-table-column label="学时" prop="hours" width="80"/>
        <!-- 可以添加院系、类型等字段 -->
        <el-table-column label="状态" prop="status" width="80">
          <template #default="scope">
            <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditCourse(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteCourse(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
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

    <!-- 添加/编辑课程对话框 (占位符) -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <p>添加/编辑课程表单待实现...</p>
      <!-- 表单通常包含：课程代码、名称、学分、学时、描述等 -->
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCourseForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {
  ElButton,
  ElCard,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
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
import {deleteCourse, getCourseList, updateCourseStatus} from '@/api/course';

// 修改组件名称为多词组合
defineOptions({
  name: 'CourseManagement'
})

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

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || null,
      status: searchParams.status
    };
    // 使用 getAllCourses 或类似的 API
    const res = await getCourseList(params);
    courseList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取课程列表失败", error);
    ElMessage.error("获取课程列表失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchCourses();
};

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchCourses();
};

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchCourses();
};

// 添加课程（打开对话框）
const handleAddCourse = () => {
  dialogTitle.value = '添加课程';
  // 清空表单逻辑待添加
  dialogVisible.value = true;
  ElMessage.info('添加课程功能待实现');
};

// 编辑课程（打开对话框）
const handleEditCourse = (row) => {
  dialogTitle.value = '编辑课程';
  // 填充表单逻辑待添加
  dialogVisible.value = true;
  ElMessage.info(`编辑课程 ${row.courseName} 功能待实现`);
};

// 删除课程
const handleDeleteCourse = (row) => {
  ElMessageBox.confirm(`确定要删除课程 ${row.courseName} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCourse(row.id);
      ElMessage.success('删除成功');
      fetchCourses(); // 刷新列表
    } catch (error) {
      console.error("删除课程失败", error);
      ElMessage.error("删除课程失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 更改课程状态
const handleStatusChange = async (row) => {
  try {
    await updateCourseStatus(row.id, row.status);
    ElMessage.success(`课程 ${row.courseName} 状态更新成功`);
  } catch (error) {
    console.error("更新课程状态失败", error);
    ElMessage.error("更新课程状态失败");
    // 状态改回去
    row.status = row.status === 1 ? 0 : 1;
  }
};

// 提交课程表单 (添加/编辑)
const submitCourseForm = () => {
  // 表单验证和提交逻辑待实现
  dialogVisible.value = false;
  ElMessage.info('提交课程表单功能待实现');
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-';
  try {
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {hour12: false});
  } catch (e) {
    return timeStr;
  }
};

// 组件挂载后加载数据
onMounted(() => {
  fetchCourses();
});

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