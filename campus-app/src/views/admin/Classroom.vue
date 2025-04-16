<template>
  <div class="classroom-management-container">
    <div class="page-header">
      <h2>教室管理</h2>
      <el-button type="primary" @click="handleAddClassroom">
        <el-icon>
          <Plus/>
        </el-icon>
        添加教室
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchParams" @submit.prevent="handleSearch">
        <el-form-item label="关键词">
          <el-input v-model="searchParams.keyword" clearable placeholder="教室编号/名称" style="width: 200px;"/>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchParams.type" clearable placeholder="选择类型" style="width: 150px;">
            <el-option label="普通教室" value="normal"/>
            <el-option label="多媒体教室" value="multimedia"/>
            <el-option label="实验室" value="lab"/>
            <el-option label="会议室" value="meeting"/>
            <!-- 可以根据实际情况添加更多类型 -->
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" clearable placeholder="选择状态" style="width: 120px;">
            <el-option :value="1" label="可用"/>
            <el-option :value="0" label="维修中"/>
            <el-option :value="2" label="占用"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 教室列表 -->
    <el-card class="classroom-list-card">
      <el-table v-loading="loading" :data="classroomList" style="width: 100%">
        <el-table-column label="教室编号" prop="classroomCode" width="150"/>
        <el-table-column label="教室名称" min-width="180" prop="name"/>
        <el-table-column label="容量" prop="capacity" width="100"/>
        <el-table-column label="类型" prop="type" width="120">
          <template #default="scope">
            {{ formatClassroomType(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <!-- 可以添加位置、设备等字段 -->
        <el-table-column label="创建时间" prop="createTime" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditClassroom(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteClassroom(scope.row)">删除</el-button>
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

    <!-- 添加/编辑教室对话框 (占位符) -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <p>添加/编辑教室表单待实现...</p>
      <!-- 表单通常包含：编号、名称、容量、类型、状态、位置、设备描述等 -->
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitClassroomForm">确定</el-button>
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
import {deleteClassroom, getClassroomList} from '@/api/classroom';

// 修改组件名称为多词组合
defineOptions({
  name: 'ClassroomManagement'
})

const loading = ref(false);
const classroomList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  keyword: '',
  type: '',
  status: null
});

const dialogVisible = ref(false);
const dialogTitle = ref('添加教室');

// 获取教室列表
const fetchClassrooms = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || null,
      type: searchParams.type || null,
      status: searchParams.status
    };
    const res = await getClassroomList(params);
    classroomList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取教室列表失败", error);
    ElMessage.error("获取教室列表失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchClassrooms();
};

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchClassrooms();
};

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchClassrooms();
};

// 添加教室（打开对话框）
const handleAddClassroom = () => {
  dialogTitle.value = '添加教室';
  // 清空表单逻辑待添加
  dialogVisible.value = true;
  ElMessage.info('添加教室功能待实现');
};

// 编辑教室（打开对话框）
const handleEditClassroom = (row) => {
  dialogTitle.value = '编辑教室';
  // 填充表单逻辑待添加
  dialogVisible.value = true;
  ElMessage.info(`编辑教室 ${row.name} 功能待实现`);
};

// 删除教室
const handleDeleteClassroom = (row) => {
  ElMessageBox.confirm(`确定要删除教室 ${row.name} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteClassroom(row.id);
      ElMessage.success('删除成功');
      fetchClassrooms(); // 刷新列表
    } catch (error) {
      console.error("删除教室失败", error);
      ElMessage.error("删除教室失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 提交教室表单 (添加/编辑)
const submitClassroomForm = () => {
  // 表单验证和提交逻辑待实现
  dialogVisible.value = false;
  ElMessage.info('提交教室表单功能待实现');
};

// 格式化教室类型
const formatClassroomType = (type) => {
  const typeMap = {
    normal: '普通教室',
    multimedia: '多媒体教室',
    lab: '实验室',
    meeting: '会议室'
  };
  return typeMap[type] || type || '未知';
};

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    1: '可用',
    0: '维修中',
    2: '占用'
  };
  return statusMap[status] !== undefined ? statusMap[status] : '未知';
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    1: 'success',
    0: 'danger',
    2: 'warning'
  };
  return typeMap[status] || 'info';
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
  fetchClassrooms();
});

</script>

<style scoped>
.classroom-management-container {
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

.classroom-list-card {
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