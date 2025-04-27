<template>
  <div class="file-management-container">
    <el-card class="page-container">
      <template #header>
        <div class="header">
          <span>文件管理</span>
          <!-- Add buttons for actions like cleanup etc. if needed -->
        </div>
      </template>

      <!-- Filters -->
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="文件名">
          <el-input v-model="filters.filename" clearable placeholder="输入文件名关键词"/>
        </el-form-item>
        <el-form-item label="上传者">
          <el-input v-model="filters.uploaderName" clearable placeholder="输入上传者姓名/ID"/>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="filters.fileType" clearable placeholder="选择文件类型">
            <el-option label="全部" value=""/>
            <el-option label="文档" value="document"/>
            <el-option label="图片" value="image"/>
            <el-option label="视频" value="video"/>
            <el-option label="音频" value="audio"/>
            <el-option label="压缩包" value="archive"/>
            <el-option label="其他" value="other"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="fetchFiles">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- File List Table -->
      <el-table
          :data="fileList"
          v-loading="loading"
          style="width: 100%"
          border
          size="small"
          stripe
      >
        <el-table-column
            label="文件名"
            min-width="250"
            prop="filename"
            show-overflow-tooltip
        >
          <template #default="scope">
            <el-icon style="vertical-align: middle; margin-right: 5px;">
              <Document v-if="isDocument(scope.row.filename)"/>
              <Picture v-else-if="isImage(scope.row.filename)"/>
              <Files v-else/>
            </el-icon>
            <el-link
                :href="getFilePreviewUrl(scope.row)"
                :underline="false"
                target="_blank"
                type="primary"
            >
              {{ scope.row.filename }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column
            label="大小"
            prop="fileSize"
            width="120"
        >
          <template #default="scope">{{ formatFileSize(scope.row.fileSize) }}</template>
        </el-table-column>
        <el-table-column
            label="类型"
            prop="fileType"
            width="100"
        >
          <template #default="scope">{{ getFileType(scope.row.filename) }}</template>
        </el-table-column>
        <el-table-column
            label="上传者"
            prop="uploaderName"
            width="120"
        />
        <el-table-column
            label="上传时间"
            prop="uploadTime"
            width="180"
        >
          <template #default="scope">{{ formatDateTime(scope.row.uploadTime) }}</template>
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            width="150"
        >
          <template #default="scope">
            <el-button
                :loading="downloadLoading === scope.row.id"
                size="small"
                type="primary"
                @click="handleDownload(scope.row)"
            >
              下载
            </el-button>
            <el-button
                :loading="deleteLoading === scope.row.id"
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无文件数据"/>
        </template>
      </el-table>

      <!-- Pagination -->
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
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {
  ElButton,
  ElCard,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElLink,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn
} from 'element-plus';
// Ensure ALL required icons are imported
import {Document, Files, Picture, Search} from '@element-plus/icons-vue';
// Assume API functions are correctly defined in @/api/file.js
import {deleteFile, downloadFile as apiDownloadFile, getFileStats, listDirectory} from '@/api/file';

const loading = ref(false);
const fileList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const filters = reactive({
  filename: '',
  uploaderName: '',
  fileType: ''
});
const downloadLoading = ref(null);
const deleteLoading = ref(null);

// Define the function directly in setup scope
const getFilePreviewUrl = (file) => {
  console.warn('getFilePreviewUrl needs implementation', file);
  return '#'; // Placeholder implementation
};

const isDocument = (filename) => {
  if (!filename) return false;
  const ext = filename.split('.').pop()?.toLowerCase() || '';
  return ['doc', 'docx', 'pdf', 'txt', 'xls', 'xlsx', 'ppt', 'pptx'].includes(ext);
};

const isImage = (filename) => {
  if (!filename) return false;
  const ext = filename.split('.').pop()?.toLowerCase() || '';
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext);
};

// Helper to format file size
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  if (!bytes) return '-'; // Handle null/undefined
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// Helper to get file type category from filename
const getFileType = (filename) => {
  if (!filename) return '未知';
  if (isDocument(filename)) return '文档';
  if (isImage(filename)) return '图片';
  const ext = filename.split('.').pop()?.toLowerCase() || '';
  if (['mp4', 'avi', 'mov', 'wmv'].includes(ext)) return '视频';
  if (['mp3', 'wav', 'ogg'].includes(ext)) return '音频';
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return '压缩包';
  return '其他';
};

// Helper to format date/time
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '-';
  try {
    return new Date(dateTimeString).toLocaleString();
  } catch (e) {
    return dateTimeString;
  }
};

// Fetch file list from backend
const fetchFiles = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      filename: filters.filename || undefined,
      uploaderName: filters.uploaderName || undefined,
      fileType: filters.fileType || undefined
    };
    const res = await listDirectory(params);
    if (res.code === 200 && res.data) {
      fileList.value = res.data.list || [];
      total.value = res.data.total || 0;
    } else {
      ElMessage.error(res.message || '获取文件列表失败');
      fileList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('加载文件列表失败:', error);
    fileList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// Download file
const handleDownload = async (file) => {
  if (!file || !file.id) return;
  downloadLoading.value = file.id;
  try {
    const response = await apiDownloadFile(file.id); // Adjust parameter as needed
    const blob = new Blob([response], {type: 'application/octet-stream'});
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = file.filename || `file_${file.id}`;
    document.body.appendChild(link); // Required for Firefox
    link.click();
    URL.revokeObjectURL(link.href);
    document.body.removeChild(link);
  } catch (error) {
    console.error('下载文件失败:', error);
    ElMessage.error('下载文件失败');
  } finally {
    downloadLoading.value = null;
  }
};

// Delete file
const handleDelete = (file) => {
  if (!file || !file.id) return;
  ElMessageBox.confirm(`确定要删除文件 "${file.filename}" 吗? 此操作不可恢复。`, '警告', {
    confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
  }).then(async () => {
    deleteLoading.value = file.id;
    try {
      await deleteFile(file.id);
      ElMessage.success('文件删除成功');
      fetchFiles(); // Refresh list
    } catch (error) {
      console.error('删除文件失败:', error);
    } finally {
      deleteLoading.value = null;
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// Pagination handlers
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchFiles();
};
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchFiles();
};

// Initial load
onMounted(() => {
  fetchFiles();
  getFileStats().catch(err => console.warn("Failed to load file stats", err));
});

</script>

<style scoped>
.file-management-container {
  padding: 20px;
}

.page-container {
  /* Add styles if needed */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>