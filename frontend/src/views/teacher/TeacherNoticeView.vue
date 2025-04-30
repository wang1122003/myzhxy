<template>
  <PageContainer title="通知公告">
    <!-- No filters needed for teacher view? Add FilterForm here if required -->
    <!--
    <FilterForm
      :model="filterParams"
      :items="filterItems"
      @search="handleSearch"
      @reset="handleReset"
      :show-add-button="false"
    />
    -->

    <TableView
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :action-column-config="actionColumnConfig"
        :columns="tableColumns"
        :data="noticeList"
        :loading="loading"
        :total="total"
        @refresh="fetchNotices"
        @view-detail="handleViewDetails"
    />

    <!-- 通知详情对话框 -->
    <el-dialog v-model="detailsDialogVisible" destroy-on-close title="通知详情" top="5vh" width="70%">
      <div v-loading="loadingDetails">
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
                <el-button link type="primary" @click="downloadAttachment(file)">
                  <el-icon>
                    <Document/>
                  </el-icon>
                  {{ file.filename || '未知文件名' }}
                </el-button>
                <span class="file-size">({{ formatFileSize(file.size) }})</span>
              </li>
            </ul>
          </div>
        </div>
        <el-empty v-else description="加载通知详情失败或无数据"/>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="detailsDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

  </PageContainer>
</template>

<script setup>
import {computed, h, onMounted, ref, resolveComponent} from 'vue';
import {ElButton, ElDialog, ElDivider, ElEmpty, ElIcon, ElMessage} from 'element-plus';
import {Document} from '@element-plus/icons-vue';
import {useRoute, useRouter} from 'vue-router';
import {getNotificationById, getNotificationsPage} from '@/api/notice'; // 正确的导入
import {downloadFile} from '@/api/file'; // 正确的文件下载API
import {formatDateTime, formatFileSize} from '@/utils/formatters'; // 正确的路径
import PageContainer from '@/components/common/EnhancedPageContainer.vue';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const noticeList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const filters = ref({
  keyword: '',
  type: ''
});

const detailsDialogVisible = ref(false);
const currentNotice = ref(null);
const loadingDetails = ref(false);
const downloadLoading = ref(null); // Track which attachment is downloading

// --- Computed Properties ---
const tableColumns = computed(() => [
  {
    prop: 'title',
    label: '标题',
    minWidth: 300,
    slots: {
      // Make title clickable to view details
      default: (scope) => h('a', {
        style: 'cursor: pointer; color: var(--el-color-primary); text-decoration: none;',
        onClick: () => handleViewDetails(scope.row)
      }, scope.row.title)
    }
  },
  {
    prop: 'noticeType',
    label: '类型',
    width: 120,
    slots: {
      default: (scope) => h(resolveComponent('ElTag'), {
        type: getNoticeTypeTag(scope.row.noticeType),
        size: 'small'
      }, () => formatNoticeType(scope.row.noticeType))
    }
  },
  {prop: 'publisherName', label: '发布人', width: 150},
  {
    prop: 'publishTime',
    label: '发布时间',
    width: 180,
    formatter: (row) => formatDateTime(row.publishTime)
  },
  // Status might not be relevant for teachers if they only see PUBLISHED
  // { prop: 'status', label: '状态', width: 100, slots: { default: (scope) => ... } }
]);

const actionColumnConfig = computed(() => ({
  width: 100,
  fixed: 'right',
  buttons: [
    {label: '查看', type: 'primary', link: true, event: 'view-detail'}
  ]
}));

// --- Methods ---

// Fetch notices list
const fetchNotices = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: filters.value.keyword || undefined,
      type: filters.value.type || undefined,
      // Add teacherId if needed by backend to filter notices created by this teacher
      // publisherId: userStore.userId
    };
    const res = await getNotificationsPage(params); // Corrected
    noticeList.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (error) {
    console.error("获取通知列表失败:", error);
    // Error handled by interceptor
    noticeList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// Formatters and Tag Types (can be moved to utils)
const formatNoticeType = (type) => {
  const typeMap = {SCHOOL: '学校通知', COLLEGE: '学院通知', COURSE: '课程通知', SYSTEM: '系统消息'};
  return typeMap[type] || type || '未知类型';
};

const getNoticeTypeTag = (type) => {
  const typeMap = {SCHOOL: '', COLLEGE: 'success', COURSE: 'warning', SYSTEM: 'info'};
  return typeMap[type] || 'info';
}

// View details
const handleViewDetails = async (row) => {
  loadingDetails.value = true;
  detailsDialogVisible.value = true;
  currentNotice.value = null; // Clear previous data
  try {
    const res = await getNotificationById(row.id); // Corrected
    currentNotice.value = res.data;
    // Ensure attachments is an array
    if (currentNotice.value && !Array.isArray(currentNotice.value.attachments)) {
      currentNotice.value.attachments = [];
    }
  } catch (error) {
    console.error("获取通知详情失败:", error);
    // Error handled by interceptor
    detailsDialogVisible.value = false;
  } finally {
    loadingDetails.value = false;
  }
};

// Download attachment
const downloadAttachment = async (file) => {
  // Assuming file object has id and filename
  if (!file || !file.id || !currentNotice.value?.id) {
    ElMessage.warning('附件信息不完整，无法下载');
    return;
  }
  downloadLoading.value = file.id;
  try {
    await downloadFile({fileId: file.id}, file.filename || 'download'); // Corrected
    ElMessage.success(`开始下载: ${file.filename || '附件'}`);
  } catch (error) {
    console.error('下载附件失败:', error);
    // Error handled by interceptor or download util
  } finally {
    downloadLoading.value = null;
  }
};

// Initial load
onMounted(() => {
  fetchNotices();
});

</script>

<style scoped>
/* 移除未使用的.ql-editor选择器 */

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
  border-top: 1px solid #eee;
  padding-top: 15px;
}
.attachments-section h4 {
  margin-bottom: 10px;
}
.attachments-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.attachments-section li {
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.file-size {
  margin-left: 10px;
  font-size: 0.9em;
  color: #909399;
}

.dialog-footer {
  text-align: right;
}
</style> 