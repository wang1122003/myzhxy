<template>
  <div class="notice-management-container">
    <div class="page-header">
      <h2>通知管理</h2>
      <el-button type="primary" @click="handleAddNotice">
        <el-icon>
          <Plus/>
        </el-icon>
        发布通知
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchParams" @submit.prevent="handleSearch">
        <el-form-item label="关键词">
          <el-input v-model="searchParams.keyword" clearable placeholder="通知标题" style="width: 250px;"/>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchParams.type" :loading="loadingNoticeTypes" clearable placeholder="选择类型"
                     style="width: 150px;">
            <el-option v-for="type in noticeTypes" :key="type.typeCode" :label="type.typeName" :value="type.typeCode"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" clearable placeholder="选择状态" style="width: 120px;">
            <el-option :value="1" label="已发布"/>
            <el-option :value="0" label="草稿"/>
            <el-option :value="2" label="已撤回"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 通知列表 -->
    <el-card class="notice-list-card">
      <el-table v-loading="loading" :data="noticeList" style="width: 100%">
        <el-table-column label="标题" min-width="300" prop="title" show-overflow-tooltip/>
        <el-table-column label="类型" prop="type" width="120">
          <template #default="scope">
            <el-tag :type="noticeTypeMapComputed[scope.row.type]?.tag || 'info'">
              {{ noticeTypeMapComputed[scope.row.type]?.name || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布人" prop="publisher" width="120"/>
        <el-table-column label="发布时间" prop="publishTime" width="180">
          <template #default="scope">{{ formatTime(scope.row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button v-if="scope.row.status === 0 || scope.row.status === 1" size="small" type="warning"
                       @click="togglePublishStatus(scope.row)">
              {{ scope.row.status === 1 ? '撤回' : '发布' }}
            </el-button>
            <el-button size="small" type="primary" @click="handleEditNotice(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteNotice(scope.row)">删除</el-button>
            <!-- 可以添加置顶等操作 -->
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

    <!-- 添加/编辑通知对话框 (占位符) -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" top="5vh" width="80%">
      <p>添加/编辑通知表单待实现...</p>
      <!-- 表单需要包含：标题、类型、内容(富文本)、附件上传等 -->
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button @click="saveDraft">存为草稿</el-button>
          <el-button type="primary" @click="submitNoticeForm">发布</el-button>
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
import {deleteNotice, getNoticeList, updateNoticeStatus} from '@/api/notice'; // 移除 addNotice, updateNotice
import {getNoticeTypes} from '@/api/common';

// 修改组件名称为多词组合
defineOptions({
  name: 'NoticeManagement'
})

const loading = ref(false);
const loadingNoticeTypes = ref(false);
const noticeList = ref([]);
const noticeTypes = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  keyword: '',
  type: '',
  status: null
});

const dialogVisible = ref(false);
const dialogTitle = ref('发布通知');

// 获取通知列表
const fetchNotices = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || null,
      type: searchParams.type || null,
      status: searchParams.status
    };
    const res = await getNoticeList(params);
    noticeList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取通知列表失败", error);
    ElMessage.error("获取通知列表失败");
  } finally {
    loading.value = false;
  }
};

// 获取通知类型
const fetchNoticeTypes = async () => {
  loadingNoticeTypes.value = true;
  try {
    const res = await getNoticeTypes();
    noticeTypes.value = res.data || [];
  } catch (error) {
    console.error("获取通知类型失败", error);
    // 此处不提示错误
  } finally {
    loadingNoticeTypes.value = false;
  }
};

// 计算通知类型映射
const noticeTypeMapComputed = computed(() => {
  const map = {};
  noticeTypes.value.forEach(type => {
    map[type.typeCode] = {name: type.typeName, tag: type.tagType || 'info'};
  });
  return map;
});

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchNotices();
};

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchNotices();
};

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchNotices();
};

// 添加通知（打开对话框）
const handleAddNotice = () => {
  dialogTitle.value = '发布通知';
  // 清空表单逻辑待添加
  dialogVisible.value = true;
  ElMessage.info('发布通知功能待实现');
};

// 编辑通知（打开对话框）
const handleEditNotice = (row) => {
  dialogTitle.value = '编辑通知';
  // 填充表单逻辑待添加
  dialogVisible.value = true;
  ElMessage.info(`编辑通知 ${row.title} 功能待实现`);
};

// 删除通知
const handleDeleteNotice = (row) => {
  ElMessageBox.confirm(`确定要删除通知 "${row.title}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteNotice(row.id);
      ElMessage.success('删除成功');
      fetchNotices(); // 刷新列表
    } catch (error) {
      console.error("删除通知失败", error);
      ElMessage.error("删除通知失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 切换发布状态 (发布/撤回)
const togglePublishStatus = async (row) => {
  const targetStatus = row.status === 1 ? 2 : 1; // 假设 1=已发布, 0=草稿, 2=已撤回
  const actionText = targetStatus === 1 ? '发布' : '撤回';
  ElMessageBox.confirm(`确定要${actionText}通知 "${row.title}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
        type: 'warning'
  }).then(async () => {
    try {
      await updateNoticeStatus(row.id, targetStatus);
      ElMessage.success(`${actionText}成功`);
      fetchNotices();
    } catch (error) {
      console.error(`${actionText}通知失败`, error);
      ElMessage.error(`${actionText}通知失败`);
    }
  }).catch(() => {
    ElMessage.info('已取消操作');
  });
};

// 保存草稿 (待实现)
const saveDraft = () => {
  dialogVisible.value = false;
  ElMessage.info('保存草稿功能待实现');
}

// 提交通知表单 (添加/编辑并发布)
const submitNoticeForm = () => {
  // 表单验证和提交逻辑待实现 (addNotice/updateNotice，并设置 status=1)
  dialogVisible.value = false;
  ElMessage.info('提交通知表单功能待实现');
};

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    1: '已发布',
    0: '草稿',
    2: '已撤回'
  };
  return statusMap[status] !== undefined ? statusMap[status] : '未知';
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    1: 'success',
    0: 'info',
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
  fetchNotices();
  fetchNoticeTypes(); // 同时获取类型用于筛选
});

</script>

<style scoped>
.notice-management-container {
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

.notice-list-card {
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