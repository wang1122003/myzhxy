<template>
  <div class="course-resources-container">
    <el-page-header :title="`课程资源管理 - ${courseName}`" @back="goBack">
    </el-page-header>

    <el-card v-loading="loading" class="resource-card">
      <div class="card-header">
        <h3>课程资源列表</h3>
        <el-upload
            :action="uploadUrl"
            :before-upload="beforeUpload"
            :file-list="fileList"
            :headers="uploadHeaders"
            :limit="5"
            :on-error="handleUploadError"
            :on-progress="handleUploadProgress"
            :on-success="handleUploadSuccess"
            multiple
        >
          <el-button type="primary">
            <el-icon>
              <Upload/>
            </el-icon>
            上传课程资源
          </el-button>
        </el-upload>
      </div>

      <el-table :data="resources" border style="width: 100%; margin-top: 20px">
        <el-table-column label="文件名" min-width="200" prop="filename">
          <template #default="scope">
            <div class="file-name">
              <el-icon class="file-icon">
                <Document v-if="isDocument(scope.row.filename)"/>
                <Picture v-else-if="isImage(scope.row.filename)"/>
                <Files v-else/>
              </el-icon>
              <span>{{ scope.row.filename }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="大小" prop="fileSize" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="上传时间" prop="uploadTime" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.uploadTime) }}
          </template>
        </el-table-column>
        <el-table-column label="下载次数" prop="downloadCount" width="100"></el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button
                :loading="downloadingId === scope.row.id"
                size="small"
                type="primary"
                @click="handleDownload(scope.row)"
            >
              下载
            </el-button>
            <el-button
                :loading="deletingId === scope.row.id"
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pagination-container">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>

      <el-empty v-if="resources.length === 0 && !loading" description="暂无课程资源"/>
    </el-card>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {
  ElButton,
  ElCard,
  ElEmpty,
  ElIcon,
  ElMessage,
  ElMessageBox,
  ElPageHeader,
  ElPagination,
  ElTable,
  ElTableColumn,
  ElUpload
} from 'element-plus';
import {Document, Files, Picture, Upload} from '@element-plus/icons-vue';
import {deleteFile, downloadFile, getResourceList} from '@/api/file';

// 使用defineOptions定义组件选项
defineOptions({
  name: 'CourseResources'
});

const route = useRoute();
const router = useRouter();
const courseId = computed(() => route.params.courseId);
const courseName = computed(() => route.query.courseName || '未命名课程');

// 加载状态
const loading = ref(false);
const resources = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const downloadingId = ref(null);
const deletingId = ref(null);
const fileList = ref([]);
const uploadUrl = computed(() =>
    `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/campus/api'}/file/upload/course/material/${courseId.value}`
);
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}));

// 获取课程资源
const fetchResources = async () => {
  loading.value = true;
  try {
    const res = await getResourceList({
      courseId: courseId.value,
      page: currentPage.value,
      size: pageSize.value
    });
    resources.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } catch (error) {
    console.error('获取课程资源失败', error);
    ElMessage.error('获取课程资源失败');
  } finally {
    loading.value = false;
  }
};

// 上传前的检查
const beforeUpload = (file) => {
  const isLt20M = file.size / 1024 / 1024 < 20;
  if (!isLt20M) {
    ElMessage.error('文件大小不能超过20MB!');
    return false;
  }
  return true;
};

// 上传进度
const handleUploadProgress = (event, _file) => {
  console.log('上传进度:', Math.round(event.percent) + '%');
};

// 上传成功
const handleUploadSuccess = (response, file) => {
  ElMessage.success(`文件 ${file.name} 上传成功`);
  fetchResources();
  fileList.value = [];
};

// 上传失败
const handleUploadError = (error, file) => {
  console.error('上传失败', error);
  ElMessage.error(`文件 ${file.name} 上传失败`);
};

// 下载文件
const handleDownload = async (file) => {
  downloadingId.value = file.id;
  try {
    await downloadFile(file.id);
    ElMessage.success('文件下载成功');
  } catch (error) {
    console.error('文件下载失败', error);
    ElMessage.error('文件下载失败');
  } finally {
    downloadingId.value = null;
  }
};

// 删除文件
const handleDelete = (file) => {
  ElMessageBox.confirm(`确定要删除文件 ${file.filename}?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    deletingId.value = file.id;
    try {
      await deleteFile(file.id);
      ElMessage.success('文件删除成功');
      fetchResources();
    } catch (error) {
      console.error('文件删除失败', error);
      ElMessage.error('文件删除失败');
    } finally {
      deletingId.value = null;
    }
  }).catch(() => {
  });
};

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '未知';
  if (size < 1024) return size + ' B';
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB';
  if (size < 1024 * 1024 * 1024) return (size / 1024 / 1024).toFixed(1) + ' MB';
  return (size / 1024 / 1024 / 1024).toFixed(1) + ' GB';
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未知';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 判断文件类型
const isDocument = (filename) => {
  if (!filename) return false;
  const extensions = ['.doc', '.docx', '.pdf', '.xls', '.xlsx', '.ppt', '.pptx', '.txt'];
  return extensions.some(ext => filename.toLowerCase().endsWith(ext));
};

const isImage = (filename) => {
  if (!filename) return false;
  const extensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
  return extensions.some(ext => filename.toLowerCase().endsWith(ext));
};

// 返回上一页
const goBack = () => {
  router.push({name: 'TeacherCourses'});
};

// 处理分页
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchResources();
};

const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchResources();
};

onMounted(() => {
  fetchResources();
});
</script>

<style scoped>
.course-resources-container {
  padding: 20px;
}

.resource-card {
  margin-top: 20px;
  min-height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.file-name {
  display: flex;
  align-items: center;
}

.file-icon {
  margin-right: 8px;
  font-size: 18px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}
</style> 