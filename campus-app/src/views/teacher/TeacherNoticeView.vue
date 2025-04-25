<template>
  <div class="teacher-notice-view-container">
    <el-card class="page-container">
      <template #header>
        <div class="header">
          <span>通知公告</span>
          <!-- Teachers typically don't add notices, admins do -->
        </div>
      </template>

      <el-table v-loading="loading" :data="noticeList" style="width: 100%">
        <el-table-column label="标题" min-width="200" prop="title">
          <template #default="scope">
            <span class="notice-title" @click="handleViewDetails(scope.row)">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" prop="noticeType" width="120">
          <template #default="scope">
            {{ formatNoticeType(scope.row.noticeType) }}
          </template>
        </el-table-column>
        <el-table-column label="发布人" prop="publisherName" width="150"/>
        <!-- Assuming backend provides publisherName -->
        <el-table-column label="发布时间" prop="publishTime" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" text type="primary" @click="handleViewDetails(scope.row)">查看</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无通知数据"/>
        </template>
      </el-table>

      <!-- 分页 -->
      <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          class="pagination-container"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 通知详情对话框 -->
    <el-dialog v-model="detailsDialogVisible" title="通知详情" top="5vh" width="70%">
      <div v-if="currentNotice" class="notice-details">
        <h3>{{ currentNotice.title }}</h3>
        <div class="meta-info">
          <span>类型: {{ formatNoticeType(currentNotice.noticeType) }}</span>
          <span>发布人: {{ currentNotice.publisherName || '系统' }}</span>
          <span>发布时间: {{ formatDateTime(currentNotice.publishTime) }}</span>
        </div>
        <el-divider/>
        <div class="notice-content" v-html="currentNotice.content"></div>
        <!-- Display attachments if available -->
        <div v-if="currentNotice.attachments && currentNotice.attachments.length > 0" class="attachments-section">
          <h4>附件:</h4>
          <ul>
            <li v-for="file in currentNotice.attachments" :key="file.id">
              <a :href="file.url" target="_blank">{{ file.fileName }}</a>
              ({{ formatFileSize(file.size) }})
            </li>
          </ul>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="detailsDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue';
import {
  ElCard, ElTable, ElTableColumn, ElPagination, ElTag, ElEmpty, ElButton, ElDialog, ElDivider, ElMessage
} from 'element-plus';
import {getNotificationsPage, getNotificationById} from '@/api/notice';
import {formatDateTime, formatFileSize} from '@/utils/formatters'; // Corrected import path

const loading = ref(false);
const noticeList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const detailsDialogVisible = ref(false);
const currentNotice = ref(null);
const loadingDetails = ref(false);

// 获取通知列表
const fetchNotices = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      // Add filters if teachers should only see specific types/status
      // status: 'PUBLISHED', // Example: only show published notices
    };
    const res = await getNotificationsPage(params);
    if (res.code === 200 && res.data) {
      noticeList.value = res.data.records || [];
      total.value = res.data.total || 0;
    } else {
      ElMessage.error(res.message || '获取通知列表失败');
      noticeList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取通知列表失败:", error);
    ElMessage.error('获取通知列表时发生错误');
    noticeList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 格式化和标签类型 (可以移到 utils)
const formatNoticeType = (type) => {
  const typeMap = {SCHOOL: '学校通知', COLLEGE: '学院通知', COURSE: '课程通知', SYSTEM: '系统消息'};
  return typeMap[type] || type || '未知类型';
};
const formatStatus = (status) => {
  const statusMap = {DRAFT: '草稿', PUBLISHED: '已发布', ARCHIVED: '已归档'};
  return statusMap[status] || status || '未知状态';
};
const getStatusTagType = (status) => {
  return status === 'PUBLISHED' ? 'success' : (status === 'DRAFT' ? 'warning' : 'info');
};

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchNotices();
};
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchNotices();
};

// 查看详情
const handleViewDetails = async (row) => {
  loadingDetails.value = true;
  detailsDialogVisible.value = true;
  currentNotice.value = null; // Clear previous data
  try {
    const res = await getNotificationById(row.id);
    if (res.code === 200 && res.data) {
      currentNotice.value = res.data;
      // Assuming attachments are part of the response
      // Example: currentNotice.value.attachments = res.data.fileList || [];
    } else {
      ElMessage.error(res.message || '获取通知详情失败');
      detailsDialogVisible.value = false;
    }
  } catch (error) {
    console.error("获取通知详情失败:", error);
    ElMessage.error('获取通知详情失败');
    detailsDialogVisible.value = false;
  } finally {
    loadingDetails.value = false;
  }
};

// 初始化加载
onMounted(() => {
  fetchNotices();
});
</script>

<style scoped>
.teacher-notice-view-container {
  padding: 20px;
}

.page-container {
  min-height: 600px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.notice-title {
  cursor: pointer;
  color: var(--el-color-primary);
  text-decoration: none;
}

.notice-title:hover {
  text-decoration: underline;
}

.notice-details {
  max-height: 75vh;
  overflow-y: auto;
}

.meta-info {
  display: flex;
  gap: 20px;
  color: #888;
  font-size: 0.9em;
  margin-bottom: 15px;
}

.notice-content {
  margin-top: 15px;
  line-height: 1.6;
}

.attachments-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.attachments-section h4 {
  margin-bottom: 10px;
}

.attachments-section ul {
  list-style: none;
  padding: 0;
}

.attachments-section li {
  margin-bottom: 5px;
}

.attachments-section a {
  color: var(--el-color-primary);
  text-decoration: none;
}

.attachments-section a:hover {
  text-decoration: underline;
}
</style> 