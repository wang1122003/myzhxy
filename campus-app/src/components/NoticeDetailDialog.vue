<template>
  <el-dialog
      :before-close="handleClose"
      :model-value="modelValue"
      class="notice-detail-dialog"
      title="通知详情"
      top="5vh"
      width="70%"
      @open="fetchNoticeDetail"
  >
    <div v-if="loading" v-loading="loading" class="loading-container">
      <p>正在加载通知内容...</p>
    </div>
    <div v-else-if="noticeData" class="notice-content-wrapper">
      <h3 class="notice-title">{{ noticeData.title }}</h3>
      <div class="notice-meta">
        <span v-if="noticeData.publisherName"><el-icon><User/></el-icon> 发布人: {{ noticeData.publisherName }}</span>
        <span v-if="noticeData.publishTime"><el-icon><Clock/></el-icon> 发布时间: {{
            formatTime(noticeData.publishTime)
          }}</span>
        <span v-if="noticeData.type"><el-icon><CollectionTag/></el-icon> 类型: {{ getNoticeTypeName(noticeData.type) }}</span>
      </div>
      <el-divider/>
      <div class="notice-body" v-html="noticeData.content"></div>

      <div v-if="noticeData.attachments && noticeData.attachments.length > 0" class="notice-attachments">
        <el-divider content-position="left">附件</el-divider>
        <div v-for="(attachment, index) in noticeData.attachments" :key="index" class="attachment-item">
          <el-link :href="attachment.url" :underline="false" target="_blank" type="primary">
            <el-icon>
              <Paperclip/>
            </el-icon>
            {{ attachment.name }} ({{ formatBytes(attachment.size) }})
          </el-link>
          <!-- Add download button if needed -->
          <!-- <el-button link type="primary" @click="downloadAttachment(attachment)" size="small" style="margin-left: 10px;">下载</el-button> -->
        </div>
      </div>
    </div>
    <div v-else class="empty-container">
      <el-empty description="无法加载通知内容"/>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import {defineEmits, defineProps, ref, watch} from 'vue';
import {ElButton, ElDialog, ElDivider, ElEmpty, ElIcon, ElLink, ElMessage, vLoading} from 'element-plus';
import {Clock, CollectionTag, Paperclip, User} from '@element-plus/icons-vue';
import {getNoticeById} from '@/api/notice'; // Correct import
// import { downloadFile } from '@/api/file'; // Import if download functionality is needed

const props = defineProps({
  modelValue: Boolean, // Controls dialog visibility (v-model)
  noticeId: { // ID of the notice to display
    type: [Number, String],
    default: null
  }
});

const emit = defineEmits(['update:modelValue']);

const loading = ref(false);
const noticeData = ref(null);

// Fetch notice details when the dialog opens or noticeId changes
const fetchNoticeDetail = async () => {
  if (!props.noticeId) {
    noticeData.value = null;
    return;
  }
  loading.value = true;
  noticeData.value = null; // Clear previous data
  try {
    const res = await getNoticeById(props.noticeId);
    if (res.success && res.data) {
      noticeData.value = res.data;
    } else {
      ElMessage.error(res.message || '获取通知详情失败');
    }
  } catch (error) {
    console.error('获取通知详情失败:', error);
    ElMessage.error('加载通知详情时出错');
  } finally {
    loading.value = false;
  }
};

// Watch for noticeId changes while the dialog might be open
watch(() => props.noticeId, (newId) => {
  if (props.modelValue && newId) {
    fetchNoticeDetail();
  }
});

// Handle closing the dialog
const handleClose = () => {
  emit('update:modelValue', false);
};

// Format time (adjust format as needed)
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  try {
    return new Date(timeStr).toLocaleString('zh-CN', {hour12: false});
  } catch (e) {
    return timeStr;
  }
};

// Get notice type name (reuse logic or centralize)
const noticeTypeMap = {
  1: '系统通知',
  2: '院系通知',
  3: '课程通知',
  4: '活动通知',
  default: '其他通知'
};
const getNoticeTypeName = (type) => noticeTypeMap[type] || noticeTypeMap.default;

// Format file size
const formatBytes = (bytes, decimals = 2) => {
  if (!+bytes) return '0 Bytes'
  const k = 1024
  const dm = decimals < 0 ? 0 : decimals
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`
}

// Optional: Implement download function if needed
// const downloadAttachment = async (attachment) => {
//   if (!attachment || !attachment.id) return;
//   try {
//     // Assuming downloadFile returns a blob or similar
//     const response = await downloadFile(attachment.id);
//     const url = window.URL.createObjectURL(new Blob([response]));
//     const link = document.createElement('a');
//     link.href = url;
//     link.setAttribute('download', attachment.name || 'download');
//     document.body.appendChild(link);
//     link.click();
//     document.body.removeChild(link);
//     window.URL.revokeObjectURL(url);
//     ElMessage.success('附件下载开始');
//   } catch (error) {
//     console.error('下载附件失败:', error);
//     ElMessage.error('下载附件失败');
//   }
// };

</script>

<style scoped>
.notice-detail-dialog .el-dialog__body {
  padding-top: 10px;
  padding-bottom: 10px;
  max-height: 75vh; /* Limit height */
  overflow-y: auto; /* Allow scrolling */
}

.loading-container {
  min-height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.notice-content-wrapper {
  /* Add padding if needed */
}

.notice-title {
  text-align: center;
  margin-bottom: 15px;
  font-size: 1.4em;
  font-weight: bold;
}

.notice-meta {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 15px; /* Spacing between items */
  color: #888;
  font-size: 0.9em;
  margin-bottom: 20px;
}

.notice-meta span {
  display: flex;
  align-items: center;
}

.notice-meta .el-icon {
  margin-right: 5px;
}

.notice-body {
  line-height: 1.8;
  font-size: 1em;
  word-wrap: break-word;
  padding: 10px 0; /* Add some padding around the content */
}

/* Style content coming from v-html if needed */
.notice-body :deep(img) {
  max-width: 100%;
  height: auto;
}

.notice-attachments {
  margin-top: 20px;
}

.attachment-item {
  margin-bottom: 8px;
}

.attachment-item .el-link {
  font-size: 0.95em;
}

.attachment-item .el-icon {
  margin-right: 5px;
}

.empty-container {
  min-height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style> 