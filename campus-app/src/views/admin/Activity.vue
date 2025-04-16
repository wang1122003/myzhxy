<template>
  <div class="activity-management-container">
    <div class="page-header">
      <h2>活动管理</h2>
      <el-button type="primary" @click="handleAddActivity">
        <el-icon>
          <Plus/>
        </el-icon>
        发布活动
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchParams" @submit.prevent="handleSearch">
        <el-form-item label="关键词">
          <el-input v-model="searchParams.keyword" clearable placeholder="活动标题/地点" style="width: 250px;"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" clearable placeholder="选择状态" style="width: 120px;">
            <el-option :value="1" label="报名中"/>
            <el-option :value="2" label="进行中"/>
            <el-option :value="3" label="已结束"/>
            <el-option :value="0" label="已取消"/>
          </el-select>
        </el-form-item>
        <!-- 可以添加按时间范围筛选 -->
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 活动列表 -->
    <el-card class="activity-list-card">
      <el-table v-loading="loading" :data="activityList" style="width: 100%">
        <el-table-column label="活动标题" min-width="200" prop="title" show-overflow-tooltip/>
        <el-table-column label="活动地点" prop="location" width="150"/>
        <el-table-column label="开始时间" prop="startTime" width="180">
          <template #default="scope">{{ formatTime(scope.row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" prop="endTime" width="180">
          <template #default="scope">{{ formatTime(scope.row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="报名人数" prop="enrollmentCount" width="100"/>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" prop="publishTime" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button size="small" type="success" @click="viewEnrollments(scope.row)">报名情况</el-button>
            <el-button size="small" type="primary" @click="handleEditActivity(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteActivity(scope.row)">删除</el-button>
            <!-- 可以添加取消活动等操作 -->
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

    <!-- 添加/编辑活动对话框 (占位符) -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" top="5vh" width="70%">
      <p>添加/编辑活动表单待实现...</p>
      <!-- 表单通常包含：标题、描述(富文本)、地点、开始时间、结束时间、封面图上传等 -->
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitActivityForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看报名情况对话框 (占位符) -->
    <el-dialog v-model="enrollmentDialogVisible" title="活动报名情况" width="60%">
      <p>报名列表待实现...</p>
      <!-- 显示报名学生列表 -->
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="enrollmentDialogVisible = false">关闭</el-button>
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
  ElTable,
  ElTableColumn,
  ElTag
} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import {deleteActivity, getActivityList} from '@/api/activity';

// 修改组件名称为多词组合
defineOptions({
  name: 'ActivityManagement'
})

const loading = ref(false);
const activityList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  keyword: '',
  status: null
});

const dialogVisible = ref(false);
const dialogTitle = ref('发布活动');
const enrollmentDialogVisible = ref(false);

// 获取活动列表
const fetchActivities = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || null,
      status: searchParams.status
    };
    const res = await getActivityList(params);
    activityList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取活动列表失败", error);
    ElMessage.error("获取活动列表失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchActivities();
};

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchActivities();
};

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchActivities();
};

// 添加活动（打开对话框）
const handleAddActivity = () => {
  dialogTitle.value = '发布活动';
  // 清空表单逻辑待添加
  dialogVisible.value = true;
  ElMessage.info('发布活动功能待实现');
};

// 编辑活动（打开对话框）
const handleEditActivity = (row) => {
  dialogTitle.value = '编辑活动';
  // 填充表单逻辑待添加
  dialogVisible.value = true;
  ElMessage.info(`编辑活动 ${row.title} 功能待实现`);
};

// 删除活动
const handleDeleteActivity = (row) => {
  ElMessageBox.confirm(`确定要删除活动 ${row.title} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteActivity(row.id);
      ElMessage.success('删除成功');
      fetchActivities(); // 刷新列表
    } catch (error) {
      console.error("删除活动失败", error);
      ElMessage.error("删除活动失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 查看报名情况
const viewEnrollments = async (row) => {
  enrollmentDialogVisible.value = true;
  ElMessage.info(`查看活动 ${row.title} 报名情况功能待实现`);
  // try {
  //     loadingEnrollments.value = true;
  //     const res = await getActivityEnrollments(row.id);
  //     enrollments.value = res.data || [];
  // } catch (error) {
  //     ElMessage.error('获取报名列表失败');
  // } finally {
  //     loadingEnrollments.value = false;
  // }
};

// 提交活动表单 (添加/编辑)
const submitActivityForm = () => {
  // 表单验证和提交逻辑待实现
  dialogVisible.value = false;
  ElMessage.info('提交活动表单功能待实现');
};

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    1: '报名中',
    2: '进行中',
    3: '已结束',
    0: '已取消' // 假设 0 为取消状态
  };
  return statusMap[status] !== undefined ? statusMap[status] : '未知';
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    1: 'success', // 报名中
    2: 'warning', // 进行中
    3: 'info',    // 已结束
    0: 'danger'   // 已取消
  };
  return typeMap[status] || 'info';
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-';
  try {
    const date = new Date(timeStr);
    // 使用更完整的格式
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  } catch (e) {
    return timeStr;
  }
};

// 组件挂载后加载数据
onMounted(() => {
  fetchActivities();
});

</script>

<style scoped>
.activity-management-container {
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

.activity-list-card {
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