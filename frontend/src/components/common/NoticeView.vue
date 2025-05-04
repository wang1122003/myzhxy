<template>
  <PageContainer title="通知公告">
    <FilterForm
        v-if="showFilter"
        :items="filterItems"
        :model="filterParams"
        :show-add-button="canManage"
        @add="handleAddNotice"
        @reset="handleReset"
        @search="handleSearch"
    />

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
    <el-dialog
        v-model="detailsDialogVisible"
        destroy-on-close
        title="通知详情"
        top="5vh"
        width="70%"
    >
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
import {computed, h, onMounted, ref} from 'vue';
import {ElButton, ElDialog, ElDivider, ElEmpty, ElIcon, ElMessage, ElTag} from 'element-plus';
import {Document, Search} from '@element-plus/icons-vue';
import {getNotificationById, getNotificationsPage} from '@/api/notice';
import {downloadFile} from '@/api/file';
import {formatDateTime, formatFileSize} from '@/utils/formatters';
import PageContainer from '@/components/common/EnhancedPageContainer.vue';
import TableView from '@/components/common/TableView.vue';
import FilterForm from '@/components/common/AdvancedFilterForm.vue';

// 接收角色类型 (student, teacher, admin) 和其他配置选项
const props = defineProps({
  role: {
    type: String,
    required: true,
    validator: (value) => ['student', 'teacher', 'admin'].includes(value)
  },
  canManage: {
    type: Boolean,
    default: false
  },
  showFilter: {
    type: Boolean,
    default: true
  },
  additionalParams: {
    type: Object,
    default: () => ({})
  }
});

const loading = ref(false);
const noticeList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const filterParams = ref({
  keyword: '',
  type: ''
});

const detailsDialogVisible = ref(false);
const currentNotice = ref(null);
const loadingDetails = ref(false);
const downloadLoading = ref(null); // Track which attachment is downloading

// 通知类型选项
const noticeTypeOptions = [
  {label: '全部', value: ''},
  {label: '学校通知', value: 'SCHOOL'},
  {label: '学院通知', value: 'COLLEGE'},
  {label: '课程通知', value: 'COURSE'},
  {label: '系统消息', value: 'SYSTEM'}
];

// --- Computed Properties ---
const filterItems = computed(() => [
  {
    type: 'select',
    label: '类型',
    prop: 'type',
    placeholder: '选择类型',
    options: noticeTypeOptions,
    props: {clearable: true, style: {width: '150px'}}
  },
  {
    type: 'input',
    label: '关键词',
    prop: 'keyword',
    placeholder: '搜索标题/内容',
    props: {clearable: true, prefixIcon: Search, style: {width: '220px'}}
  }
]);

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
      default: (scope) => h(ElTag, {
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
  }
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
      keyword: filterParams.value.keyword || undefined,
      type: filterParams.value.type || undefined,
      ...props.additionalParams
    };
    console.log("通知请求参数:", params, "用户角色:", props.role);
    const res = await getNotificationsPage(params);
    console.log("通知响应数据:", res);

    // 更灵活地处理不同格式的响应
    if (res.data && res.data.records) {
      noticeList.value = res.data.records;
      total.value = res.data.total || 0;
    } else if (Array.isArray(res.data)) {
      noticeList.value = res.data;
      total.value = res.data.length;
    } else if (Array.isArray(res)) {
      noticeList.value = res;
      total.value = res.length;
    } else if (res && res.records) {
      noticeList.value = res.records;
      total.value = res.total || res.records.length;
    } else {
      noticeList.value = [];
      total.value = 0;
    }
    console.log("处理后的通知列表:", noticeList.value, "总数:", total.value);
  } catch (error) {
    console.error("获取通知列表失败:", error);
    noticeList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// Formatters and Tag Types
const formatNoticeType = (type) => {
  const typeMap = {SCHOOL: '学校通知', COLLEGE: '学院通知', COURSE: '课程通知', SYSTEM: '系统消息'};
  return typeMap[type] || type || '未知类型';
};

const getNoticeTypeTag = (type) => {
  const typeMap = {SCHOOL: '', COLLEGE: 'success', COURSE: 'warning', SYSTEM: 'info'};
  return typeMap[type] || 'info';
};

// View details
const handleViewDetails = async (row) => {
  loadingDetails.value = true;
  detailsDialogVisible.value = true;
  currentNotice.value = null; // Clear previous data
  try {
    const res = await getNotificationById(row.id);
    currentNotice.value = res.data;
    // Ensure attachments is an array
    if (currentNotice.value && !Array.isArray(currentNotice.value.attachments)) {
      currentNotice.value.attachments = [];
    }
  } catch (error) {
    console.error("获取通知详情失败:", error);
    detailsDialogVisible.value = false;
  } finally {
    loadingDetails.value = false;
  }
};

// Download attachment
const downloadAttachment = async (file) => {
  if (!file || !file.id || !currentNotice.value?.id) {
    ElMessage.warning('附件信息不完整，无法下载');
    return;
  }
  downloadLoading.value = file.id;
  try {
    await downloadFile({fileId: file.id}, file.filename || 'download');
    ElMessage.success(`开始下载: ${file.filename || '附件'}`);
  } catch (error) {
    console.error('下载附件失败:', error);
  } finally {
    downloadLoading.value = null;
  }
};

// Handle search/filter
const handleSearch = () => {
  currentPage.value = 1;
  fetchNotices();
};

// Handle reset
const handleReset = () => {
  filterParams.value = {keyword: '', type: ''};
  currentPage.value = 1;
  fetchNotices();
};

// For admin only - add notice placeholder
const handleAddNotice = () => {
  if (props.canManage) {
    // This would be handled by the parent component
    // through custom event handlers
    console.log('Add notice clicked');
  }
};

// Initial load
onMounted(() => {
  fetchNotices();
});
</script>

<style scoped>
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

.file-size {
  color: #999;
  font-size: 0.9em;
  margin-left: 5px;
}
</style> 