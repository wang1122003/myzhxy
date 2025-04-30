<template>
  <PageContainer title="通知公告">
    <template #header-actions>
      <el-button link type="primary" @click="goBack">返回首页</el-button>
      </template>

    <TableView
          v-loading="loadingNotices"
          :data="noticeList"
          v-model="pagination"
          :height="'calc(100vh - 260px)'"
          :total="totalNotices"
          @page-change="handlePageChange"
      >
        <el-table-column
            label="标题"
            min-width="300"
            prop="title"
            show-overflow-tooltip
        />
        <el-table-column
            label="类型"
            prop="type"
            width="120"
        >
          <template #default="scope">
            <el-tag :type="noticeTypeMapComputed[scope.row.type]?.tag || 'info'">
              {{ noticeTypeMapComputed[scope.row.type]?.name || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
            label="发布人"
            prop="publisherName"
            show-overflow-tooltip
            width="150"
        />
        <el-table-column
            label="发布时间"
            prop="publishTime"
            width="180"
        >
          <template #default="scope">
            {{ formatTime(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column
            label="操作"
            width="100"
            fixed="right"
        >
          <template #default="scope">
            <el-button
                link
                type="primary"
                @click="viewNotice(scope.row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
    </TableView>

    <!-- 公告详情对话框 -->
    <el-dialog
        v-model="noticeDialogVisible"
        :title="currentNotice.title"
        append-to-body
        top="5vh"
        width="60%"
    >
      <div
          v-if="currentNotice.id"
          v-loading="loadingNoticeDetail"
      >
        <div class="notice-content">
          <div class="notice-info">
            <span>发布人：{{ currentNotice.publisherName }}</span>
            <span>发布时间：{{ formatTime(currentNotice.publishTime) }}</span>
          </div>
          <el-divider/>
          <div
              class="notice-text"
              v-html="currentNotice.content"
          />
          <div
              v-if="currentNotice.attachmentFiles && currentNotice.attachmentFiles.length"
              class="notice-attachments"
          >
            <el-divider/>
            <h4>附件：</h4>
            <ul>
              <li
                  v-for="file in currentNotice.attachmentFiles"
                  :key="file.url || file.name" 
              >
                <el-link
                    :disabled="downloadingAttachment[file.id || file.url]"
                    :loading="downloadingAttachment[file.id || file.url]" 
                    type="primary"
                    @click="downloadAttachment(file)"
                >
                  {{ file.name }}
                </el-link>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="noticeDialogVisible = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {useRouter} from 'vue-router';
import {ElMessage} from 'element-plus';
import {getNotificationById, getNotificationsPage} from '@/api/notice';
import {downloadFile} from '@/api/file';
import PageContainer from '@/components/common/EnhancedPageContainer.vue';
import TableView from '@/components/common/TableView.vue';

const router = useRouter();

// 加载状态
const loadingNotices = ref(false);
const loadingNoticeDetail = ref(false);
const downloadingAttachment = reactive({});

// 数据
const noticeList = ref([]);
const totalNotices = ref(0);
const pagination = ref({
  pageNum: 1,
  pageSize: 10
});
const currentNotice = reactive({
  id: null,
  title: '',
  publisher: '',
  publishTime: '',
  content: '',
  attachments: []
});

// 对话框可见性
const noticeDialogVisible = ref(false);

// 直接定义静态的类型映射
const noticeTypeMap = {
  "SYSTEM": {name: "系统通知", tag: "info"},
  "COURSE": {name: "教学通知", tag: "success"},
  "ACTIVITY": {name: "活动公告", tag: "warning"},
  "GENERAL": {name: "通用", tag: "danger"},
  "OTHER": {name: "其他", tag: "info"}
};

// computed 保持不变，但现在基于静态 map
const noticeTypeMapComputed = computed(() => {
  return noticeTypeMap;
});

// 获取通知列表
const fetchNotices = async () => {
  loadingNotices.value = true;
  try {
    const params = {
      page: pagination.value.pageNum,
      size: pagination.value.pageSize
      // 可以添加其他筛选参数，例如 type
    };
    // getNotificationsPage 返回的是 IPage 对象
    const res = await getNotificationsPage(params);
    // 使用 IPage 的 records 和 total 字段
    noticeList.value = res.records || [];
    totalNotices.value = res.total || 0;
  } catch (error) {
    console.error("获取通知列表失败", error);
    ElMessage.error("获取通知列表失败");
  } finally {
    loadingNotices.value = false;
  }
};

// 查看通知详情
const viewNotice = async (row) => {
  loadingNoticeDetail.value = true;
  noticeDialogVisible.value = true;
  currentNotice.id = null; // 重置详情
  try {
    const res = await getNotificationById(row.id);
    Object.assign(currentNotice, res);
  } catch (err) {
    console.error("获取通知详情失败", err);
    ElMessage.error("获取通知详情失败");
    noticeDialogVisible.value = false;
  } finally {
    loadingNoticeDetail.value = false;
  }
};

// 下载附件
const downloadAttachment = async (file) => {
  if (!file || !file.id) {
    ElMessage.warning('无效的文件信息');
    return;
  }

  downloadingAttachment[file.id || file.url] = true;
  try {
    await downloadFile(file.id);
    ElMessage.success('下载成功');
  } catch (error) {
    console.error('下载失败', error);
    ElMessage.error('下载失败');
  } finally {
    downloadingAttachment[file.id || file.url] = false;
  }
};

// 返回首页
const goBack = () => {
  router.push('/');
};

// 处理页码变化
const handlePageChange = () => {
  fetchNotices();
};

// 页面加载时获取通知列表
onMounted(() => {
  fetchNotices();
});
</script>

<style scoped>
.notice-content {
  padding: 10px;
}

.notice-info {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.notice-text {
  line-height: 1.6;
  margin: 16px 0;
}

.notice-attachments h4 {
  margin-top: 15px;
  margin-bottom: 10px;
  color: #333;
}

.notice-attachments ul {
  padding-left: 20px;
}

.notice-attachments li {
  margin-bottom: 8px;
}
</style> 