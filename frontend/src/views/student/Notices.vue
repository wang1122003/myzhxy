<template>
  <PageContainer title="通知公告">
    <FilterForm
        v-if="showFilterFlag"
        :items="filterItems"
        :model="filterParams"
        :show-add-button="canManageFlag"
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
    <DialogWrapper
        v-model:visible="detailsDialogVisible"
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
    </DialogWrapper>
  </PageContainer>
</template>

<script setup>
import {computed, h, onMounted, ref} from 'vue';
import {ElButton, ElDivider, ElEmpty, ElIcon, ElMessage, ElTag} from 'element-plus';
import {Document, Search} from '@element-plus/icons-vue';
import {useRouter} from 'vue-router';
import {getNotificationById, getNotificationsPage} from '@/api/notice';
import {downloadFile} from '@/api/file';
import {formatDateTime, formatFileSize} from '@/utils/formatters';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import TableView from '@/views/ui/TableView.vue';
import FilterForm from '@/views/ui/AdvancedFilterForm.vue';
import DialogWrapper from '@/views/ui/DialogWrapper.vue';

// Configuration for this StudentNoticesView
const currentRole = 'student';
const canManageFlag = false;
const showFilterFlag = true;
const pageAdditionalParams = {};

const router = useRouter();

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

const noticeTypeOptions = [
  {label: '全部', value: ''},
  {label: '学校通知', value: 'SCHOOL'},
  {label: '学院通知', value: 'COLLEGE'},
  {label: '课程通知', value: 'COURSE'},
  {label: '系统消息', value: 'SYSTEM'}
];

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

const fetchNotices = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: filterParams.value.keyword || undefined,
      type: filterParams.value.type || undefined,
      ...pageAdditionalParams
    };
    const res = await getNotificationsPage(params);
    if (res.data && res.data.records) {
      noticeList.value = res.data.records;
      total.value = res.data.total || 0;
    } else if (Array.isArray(res.data)) {
      noticeList.value = res.data;
      total.value = res.data.length;
    } else {
      noticeList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取通知列表失败:", error);
    ElMessage.error('获取通知列表失败');
    noticeList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const formatNoticeType = (type) => {
  const typeMap = {SCHOOL: '学校通知', COLLEGE: '学院通知', COURSE: '课程通知', SYSTEM: '系统消息'};
  return typeMap[type] || type || '未知类型';
};

const getNoticeTypeTag = (type) => {
  const typeMap = {SCHOOL: '', COLLEGE: 'success', COURSE: 'warning', SYSTEM: 'info'};
  return typeMap[type] || 'info';
};

const handleViewDetails = async (row) => {
  loadingDetails.value = true;
  detailsDialogVisible.value = true;
  currentNotice.value = null;
  try {
    const res = await getNotificationById(row.id);
    currentNotice.value = res.data;
    if (currentNotice.value && !Array.isArray(currentNotice.value.attachments)) {
      currentNotice.value.attachments = [];
    }
  } catch (error) {
    console.error("获取通知详情失败:", error);
    ElMessage.error('获取通知详情失败');
  } finally {
    loadingDetails.value = false;
  }
};

const downloadAttachment = async (file) => {
  try {
    await downloadFile(file.id, file.filename || 'downloaded_file');
    ElMessage.success(`开始下载文件: ${file.filename}`);
  } catch (error) {
    console.error("下载附件失败:", error);
    ElMessage.error('下载附件失败');
  }
};

const handleAddNotice = () => {
  // Students cannot add notices. This function is here due to merging from a common component.
  // It effectively does nothing for students.
  console.log("Add notice clicked, but students cannot manage notices.");
  ElMessage.info('您没有权限添加通知。');
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchNotices();
};

const handleReset = () => {
  filterParams.value.keyword = '';
  filterParams.value.type = '';
  currentPage.value = 1;
  fetchNotices();
};

onMounted(() => {
  fetchNotices();
});

</script>

<style scoped>
.notice-details h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 20px;
}

.meta-info {
  display: flex;
  gap: 20px;
  color: #606266;
  font-size: 14px;
  margin-bottom: 16px;
}

.notice-content {
  line-height: 1.8;
  margin-bottom: 20px;
  word-wrap: break-word;
}

.attachments-section h4 {
  margin-bottom: 10px;
  font-size: 16px;
}

.attachments-section ul {
  list-style: none;
  padding: 0;
}

.attachments-section li {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.file-size {
  margin-left: 8px;
  font-size: 13px;
  color: #909399;
}

.dialog-footer {
  text-align: right;
}
</style>