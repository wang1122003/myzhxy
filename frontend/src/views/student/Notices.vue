<template>
  <PageContainer title="校园公告">
    <FilterForm
        :items="filterItems"
        :model="filter"
        :show-add-button="false"
        @reset="handleReset"
        @search="handleSearch"
    />

    <TableView
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :action-column-config="actionColumnConfig"
        :columns="tableColumns"
        :data="notices"
        :loading="loading"
        :total="total"
        @refresh="fetchData"
        @view-detail="handleViewDetail"
    />

    <!-- 通知详情对话框 -->
    <el-dialog
        v-model="detailVisible"
        :title="currentNotice.title"
        destroy-on-close
        top="5vh"
        width="60%"
    >
      <template v-if="currentNotice.id">
        <div v-loading="loadingDetail">
          <div class="notice-meta">
            <span>发布人: {{ currentNotice.publisher }}</span>
            <span>发布时间: {{ formatDate(currentNotice.publishTime) }}</span>
            <span>类型: {{ getNoticeTypeName(currentNotice.type) }}</span>
          </div>
          <el-divider/>
          <div
              class="notice-content"
              v-html="currentNotice.content"
          />

          <div
              v-if="currentNotice.attachments && currentNotice.attachments.length > 0"
              class="notice-attachments"
          >
            <h4>附件列表:</h4>
            <div
                v-for="(attachment, index) in currentNotice.attachments"
                :key="index"
                class="attachment-item"
            >
              <el-button
                  :loading="downloadLoading === attachment.id"
                  link
                  type="primary"
                  @click="downloadAttachment(attachment)"
              >
                <el-icon>
                  <Document/>
                </el-icon>
                {{ attachment.filename }}
              </el-button>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref, h, resolveComponent} from 'vue';
import {ElMessage, ElTag, ElDialog, ElDivider, ElButton, ElIcon} from 'element-plus';
import {Document, Search} from '@element-plus/icons-vue';
// import { getNotificationsPage, getNotificationById } from '@/api/notice'; // Adjust API import
// import { downloadFile } from '@/api/file';
import {getNotificationsPage, getNotificationById} from '@/api/notice'; // Corrected: Use notice.js for list and detail
import {downloadFile} from '@/api/file'; // Corrected: Use generic file download API
import {formatDateTime} from '@/utils/formatters'; // Corrected import path
import PageContainer from '@/components/common/EnhancedPageContainer.vue';
import TableView from '@/components/common/TableView.vue';
import FilterForm from '@/components/common/AdvancedFilterForm.vue';

// --- State ---
const loading = ref(false);
const loadingDetail = ref(false);
const notices = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const filter = reactive({
  type: '',
  keyword: ''
});
const detailVisible = ref(false);
const currentNotice = ref({});
const downloadLoading = ref(null);

// --- Options & Formatters ---
const noticeTypes = [
  {label: '全部', value: ''},
  {label: '公告', value: 'ANNOUNCEMENT'},
  {label: '课程', value: 'COURSE'},
  {label: '考试', value: 'EXAM'},
  {label: '活动', value: 'ACTIVITY'},
  {label: '其他', value: 'OTHER'}
];

const getNoticeTypeName = (type) => {
  return noticeTypes.find(t => t.value === type)?.label || '未知';
};

const getNoticeTypeTag = (type) => {
  const map = {
    'ANNOUNCEMENT': '',
    'COURSE': 'success',
    'EXAM': 'danger',
    'ACTIVITY': 'warning',
    'OTHER': 'info'
  };
  return map[type] || 'info';
};

const formatDate = (date) => {
  return formatDateTime(date);
};

const isNew = (publishTime) => {
  if (!publishTime) return false;
  const oneDayAgo = new Date().getTime() - 24 * 60 * 60 * 1000;
  return new Date(publishTime).getTime() > oneDayAgo;
};

// --- Computed Properties ---

const filterItems = computed(() => [
  {
    type: 'select',
    label: '类型',
    prop: 'type',
    placeholder: '公告类型',
    options: noticeTypes,
    props: {clearable: true, style: {width: '150px'}}
  },
  {
    type: 'input',
    label: '关键词',
    prop: 'keyword',
    placeholder: '搜索标题',
    props: {clearable: true, prefixIcon: Search, style: {width: '250px'}}
  }
]);

const tableColumns = computed(() => [
  {
    prop: 'title',
    label: '标题',
    minWidth: 300,
    slots: {
      default: (scope) => h('div', {style: 'display: flex; align-items: center;'}, [
        h(resolveComponent('ElTag'), {
          type: getNoticeTypeTag(scope.row.type),
          size: 'small',
          style: 'margin-right: 8px; flex-shrink: 0;'
        }, () => getNoticeTypeName(scope.row.type)),
        h('span', {style: 'flex-grow: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;'}, scope.row.title),
        isNew(scope.row.publishTime)
            ? h(resolveComponent('ElTag'), {
              type: 'danger',
              size: 'small',
              effect: 'plain',
              style: 'margin-left: 8px; flex-shrink: 0;'
            }, () => '新')
            : null
      ])
    }
  },
  {prop: 'publisher', label: '发布者', width: 150},
  {
    prop: 'publishTime',
    label: '发布时间',
    width: 180,
    formatter: (row) => formatDate(row.publishTime)
  },
]);

const actionColumnConfig = computed(() => ({
  width: 100,
  fixed: 'right',
  buttons: [
    {label: '查看详情', type: 'primary', link: true, event: 'view-detail'}
  ]
}));

// --- Methods ---

// Fetch notices list
const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      type: filter.type || undefined,
      keyword: filter.keyword || undefined
    };
    const res = await getNotificationsPage(params); // Corrected: Use general notice API
    // 修复数据解构方式，适应后端返回格式
    if (res && res.code === 200) {
      notices.value = res.data?.records || [];
      total.value = res.data?.total || 0;
    } else {
      console.warn('通知API返回非标准格式:', res);
      // 尝试直接使用返回的数据
      if (Array.isArray(res)) {
        notices.value = res;
        total.value = res.length;
      } else if (res && res.records) {
        notices.value = res.records;
        total.value = res.total || res.records.length;
      } else {
        notices.value = [];
        total.value = 0;
      }
    }
  } catch (error) {
    console.error('获取公告列表失败', error);
    // Error handled by interceptor
    notices.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// Handle search/filter
const handleSearch = () => {
  currentPage.value = 1;
  fetchData();
};

// Handle reset filter
const handleReset = () => {
  filter.type = '';
  filter.keyword = '';
  currentPage.value = 1;
  fetchData();
};

// Handle view detail
const handleViewDetail = async (row) => {
  detailVisible.value = true;
  loadingDetail.value = true;
  currentNotice.value = {}; // Clear previous details
  try {
    const res = await getNotificationById(row.id); // Corrected: Use general notice API
    currentNotice.value = res.data || {};
    // Ensure attachments is an array
    if (!Array.isArray(currentNotice.value.attachments)) {
      currentNotice.value.attachments = [];
    }
  } catch (error) {
    console.error('获取通知详情失败:', error);
    // Error handled by interceptor
    detailVisible.value = false; // Close dialog on error
  } finally {
    loadingDetail.value = false;
  }
};

// Download attachment
const downloadAttachment = async (attachment) => {
  downloadLoading.value = attachment.id;
  try {
    // Assuming attachment object has id and filename
    // await studentDownloadNoticeAttachment(currentNotice.value.id, attachment.id, attachment.filename); // Use student API -> Replaced below
    // Use generic downloadFile, assuming attachment.id is the identifier and we pass filename for download prompt
    await downloadFile({fileId: attachment.id}, attachment.filename);
  } catch (error) {
    console.error('下载附件失败', error);
    // Error handled by interceptor or download util
  } finally {
    downloadLoading.value = null;
  }
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchData();
});

</script>

<style scoped>
.notice-meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.notice-content {
  min-height: 200px; /* Ensure content area has some height */
  line-height: 1.6;
}

.notice-attachments {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 15px;
}
.notice-attachments h4 {
  margin-bottom: 10px;
}

.attachment-item {
  margin-bottom: 5px;
}

.dialog-footer {
  text-align: right;
}
</style> 