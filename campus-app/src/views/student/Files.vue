<template>
  <div class="files-container">
    <div class="page-header">
      <h2>文件管理</h2>
      <div class="actions">
        <el-upload
            :action="uploadUrl"
            :auto-upload="true"
            :before-upload="beforeUpload"
            :headers="headers"
            :limit="1"
            :on-error="handleUploadError"
            :on-success="handleUploadSuccess"
            class="upload-button"
        >
          <el-button type="primary">
            <el-icon>
              <Upload/>
            </el-icon>
            上传文件
          </el-button>
        </el-upload>
      </div>
    </div>

    <el-tabs
        v-model="activeTab"
        @tab-click="handleTabChange"
    >
      <el-tab-pane
          label="我的文件"
          name="personal"
      >
        <div class="file-list-container">
          <el-table
              v-loading="loading"
              :data="files"
              style="width: 100%"
          >
            <el-table-column
                label="文件名"
                min-width="200"
            >
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
            <el-table-column
                label="大小"
                prop="fileSize"
                width="120"
            >
              <template #default="scope">
                {{ formatFileSize(scope.row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column
                label="类型"
                prop="fileType"
                width="120"
            >
              <template #default="scope">
                {{ getFileType(scope.row.filename) }}
              </template>
            </el-table-column>
            <el-table-column
                label="上传时间"
                prop="uploadTime"
                width="180"
            >
              <template #default="scope">
                {{ formatDate(scope.row.uploadTime) }}
              </template>
            </el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                width="200"
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
          </el-table>

          <div
              v-if="total > 0"
              class="pagination-container"
          >
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

          <el-empty
              v-if="files.length === 0 && !loading"
              description="暂无文件"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane
          label="课程资料"
          name="course"
      >
        <div class="course-resource-container">
          <div class="filter-container">
            <el-select
                v-model="courseFilter"
                clearable
                placeholder="选择课程"
                style="width: 220px"
                :loading="loadingCourses"
                filterable
                @change="handleCourseFilterChange"
            >
              <el-option
                  v-for="course in courses"
                  :key="course.id"
                  :label="course.courseName"
                  :value="course.id"
              />
            </el-select>
          </div>

          <el-table
              v-loading="resourceLoading"
              :data="resources"
              style="width: 100%"
          >
            <el-table-column
                label="文件名"
                min-width="200"
            >
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
            <el-table-column
                label="所属课程"
                prop="courseName"
                width="180"
            />
            <el-table-column
                label="上传人"
                prop="uploadUser"
                width="120"
            />
            <el-table-column
                label="大小"
                prop="fileSize"
                width="120"
            >
              <template #default="scope">
                {{ formatFileSize(scope.row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column
                label="上传时间"
                prop="uploadTime"
                width="180"
            >
              <template #default="scope">
                {{ formatDate(scope.row.uploadTime) }}
              </template>
            </el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                width="120"
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
              </template>
            </el-table-column>
          </el-table>

          <div
              v-if="resourceTotal > 0"
              class="pagination-container"
          >
            <el-pagination
                v-model:current-page="resourceCurrentPage"
                v-model:page-size="resourcePageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="resourceTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleResourceSizeChange"
                @current-change="handleResourceCurrentChange"
            />
          </div>

          <el-empty
              v-if="resources.length === 0 && !resourceLoading"
              description="暂无课程资料"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {
  ElButton,
  ElEmpty,
  ElIcon,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTabPane,
  ElTabs,
  ElUpload
} from 'element-plus';
import {Document, Files, Picture, Upload} from '@element-plus/icons-vue';
import {FILE_API} from '@/api/api-endpoints';
import {deleteFile, downloadFile, getFileList, getResourceList} from '@/api/file';
import {getStudentCourses} from '@/api/course';
import {useRouter} from 'vue-router';

const router = useRouter();

// General loading state for personal files tab
const loading = ref(false);

// 文件相关状态
const files = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const downloadLoading = ref(null)
const deleteLoading = ref(null)
const activeTab = ref('personal')

// 课程资源相关状态
const resources = ref([])
const resourceLoading = ref(false)
const resourceTotal = ref(0)
const resourceCurrentPage = ref(1)
const resourcePageSize = ref(10)
const courses = ref([])
const courseFilter = ref(null)
const loadingCourses = ref(false)

// 从 request.js 或环境变量获取 baseURL
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/campus/api';
// 在 baseURL 基础上拼接上传路径
const uploadUrl = `${baseURL}${FILE_API.UPLOAD_FILE || '/file/upload'}`;

const headers = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`,
}));

// 获取文件列表
const fetchFiles = async () => {
  loading.value = true
  try {
    const res = await getFileList({
      page: currentPage.value,
      size: pageSize.value,
      type: 'personal'
    })
    files.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取文件列表失败', error)
    ElMessage.error('获取文件列表失败')
  } finally {
    loading.value = false
  }
}

// 获取课程资源列表
const fetchCourseResources = async () => {
  resourceLoading.value = true
  try {
    const res = await getResourceList({
      page: resourceCurrentPage.value,
      size: resourcePageSize.value,
      courseId: courseFilter.value
    })
    resources.value = res.data.list || []
    resourceTotal.value = res.data.total || 0
  } catch (error) {
    console.error('获取课程资源失败', error)
    ElMessage.error('获取课程资源失败')
  } finally {
    resourceLoading.value = false
  }
}

// 获取已选课程列表
const fetchCourses = async () => {
  loadingCourses.value = true
  try {
    const res = await getStudentCourses()
    courses.value = res.data || []
  } catch (error) {
    console.error('获取课程列表失败', error)
  } finally {
    loadingCourses.value = false
  }
}

// 文件上传成功处理
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    fetchFiles()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 文件上传失败处理
const handleUploadError = (error) => {
  console.error('上传失败', error)
  ElMessage.error('上传失败，请检查网络或文件大小')
}

// 文件上传前检查
const beforeUpload = (file) => {
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!isLt20M) {
    ElMessage.error('文件大小不能超过20MB!')
    return false
  }
  return true
}

// 文件下载处理
const handleDownload = async (file) => {
  downloadLoading.value = file.id
  try {
    await downloadFile(file.id)
    ElMessage.success('文件下载成功')
  } catch (error) {
    console.error('文件下载失败', error)
    ElMessage.error('文件下载失败')
  } finally {
    downloadLoading.value = null
  }
}

// 文件删除处理
const handleDelete = (file) => {
  ElMessageBox.confirm('确定要删除该文件吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    deleteLoading.value = file.id
    try {
      await deleteFile(file.id)
      ElMessage.success('文件删除成功')
      fetchFiles()
    } catch (error) {
      console.error('文件删除失败', error)
      ElMessage.error('文件删除失败')
    } finally {
      deleteLoading.value = null
    }
  }).catch(() => {
  })
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchFiles()
}

// 分页页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchFiles()
}

// 课程资源分页大小变化
const handleResourceSizeChange = (size) => {
  resourcePageSize.value = size
  resourceCurrentPage.value = 1
  fetchCourseResources()
}

// 课程资源分页页码变化
const handleResourceCurrentChange = (page) => {
  resourceCurrentPage.value = page
  fetchCourseResources()
}

// 课程筛选处理
const handleCourseFilterChange = () => {
  resourceCurrentPage.value = 1
  fetchCourseResources()
}

// 标签页切换
const handleTabChange = (tab) => {
  if (tab.props.name === 'course') {
    if (courses.value.length === 0) {
      fetchCourses()
    }
    if (resources.value.length === 0) {
      fetchCourseResources()
    }
  } else if (tab.props.name === 'personal') {
    if (files.value.length === 0) {
      fetchFiles()
    }
  }
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (size < 1024) {
    return size + 'B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + 'KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + 'MB'
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + 'GB'
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 判断文件是否为文档
const isDocument = (filename) => {
  const extensions = ['.doc', '.docx', '.pdf', '.xls', '.xlsx', '.ppt', '.pptx', '.txt']
  return extensions.some(ext => filename.toLowerCase().endsWith(ext))
}

// 判断文件是否为图片
const isImage = (filename) => {
  const extensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
  return extensions.some(ext => filename.toLowerCase().endsWith(ext))
}

// 获取文件类型
const getFileType = (filename) => {
  if (!filename) return '未知'
  const ext = filename.split('.').pop().toLowerCase()
  const typeMap = {
    'doc': '文档',
    'docx': '文档',
    'pdf': 'PDF',
    'xls': '表格',
    'xlsx': '表格',
    'ppt': '演示文稿',
    'pptx': '演示文稿',
    'txt': '文本',
    'jpg': '图片',
    'jpeg': '图片',
    'png': '图片',
    'gif': '图片',
    'bmp': '图片',
    'webp': '图片',
    'mp4': '视频',
    'avi': '视频',
    'mov': '视频',
    'mp3': '音频',
    'wav': '音频',
    'zip': '压缩包',
    'rar': '压缩包',
    '7z': '压缩包'
  }
  return typeMap[ext] || ext.toUpperCase()
}

// 初始加载
onMounted(() => {
  fetchFiles();
  if (activeTab.value === 'course') {
    fetchCourses()
    fetchCourseResources()
  }
})
</script>

<script>
export default {
  name: 'FileManagement'
}
</script>

<style scoped>
.files-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.file-list-container, .course-resource-container {
  background-color: #fff;
  border-radius: 4px;
}

.filter-container {
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
  display: flex;
  justify-content: center;
}
</style> 