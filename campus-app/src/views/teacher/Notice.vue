<template>
  <div class="notice-container">
    <div class="page-header">
      <h2>通知管理</h2>
      <el-button type="primary" @click="openAddDialog">发布通知</el-button>
    </div>

    <!-- 通知列表 -->
    <el-card class="notice-card">
      <div class="filter-container">
        <el-input
            v-model="searchQuery"
            clearable
            placeholder="搜索通知标题"
            style="width: 200px; margin-right: 10px"
            @clear="handleFilter"
            @keyup.enter="handleFilter"
        >
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>

        <el-select v-model="listQuery.type" clearable placeholder="通知类型" style="width: 130px; margin-right: 10px"
                   @change="handleFilter">
          <el-option v-for="item in noticeTypes" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>

        <el-select v-model="listQuery.status" clearable placeholder="状态" style="width: 130px; margin-right: 10px"
                   @change="handleFilter">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>

        <el-button type="primary" @click="handleFilter">筛选</el-button>
      </div>

      <el-table v-loading="listLoading" :data="noticeList" border style="width: 100%">
        <el-table-column label="标题" min-width="200" prop="title" show-overflow-tooltip/>
        <el-table-column label="类型" prop="type" width="120">
          <template #default="scope">
            <el-tag :type="getNoticeTagType(scope.row.type)">
              {{ getNoticeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="课程" prop="courseId" show-overflow-tooltip width="150">
          <template #default="scope">
            {{ scope.row.courseName || '全校通知' }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" prop="publishTime" width="160"/>
        <el-table-column align="center" label="查看数" prop="viewCount" width="100"/>
        <el-table-column align="center" label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button link type="primary" @click="viewNotice(scope.row)">查看</el-button>
            <el-button link type="primary" @click="editNotice(scope.row)">编辑</el-button>
            <el-button
                :type="scope.row.status === 1 ? 'warning' : 'success'"
                link
                @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '下架' : '发布' }}
            </el-button>
            <el-button link type="danger" @click="deleteNotice(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="listQuery.page"
            v-model:page-size="listQuery.limit"
            :page-sizes="[10, 20, 30, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑通知对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogType === 'add' ? '发布通知' : '编辑通知'"
        width="800px"
        @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题"/>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择通知类型" style="width: 100%">
            <el-option v-for="item in noticeTypes" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="发布范围" prop="scope">
          <el-radio-group v-model="form.scope">
            <el-radio :label="1">全校通知</el-radio>
            <el-radio :label="2">课程通知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.scope === 2" label="选择课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
                v-for="course in courseOptions"
                :key="course.id"
                :label="course.courseName"
                :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
              v-model="form.content"
              :rows="10"
              placeholder="请输入通知内容"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
              :action="uploadUrl"
              :before-upload="beforeUpload"
              :file-list="fileList"
              :headers="uploadHeaders"
              :limit="5"
              :on-error="handleUploadError"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :on-success="handleUploadSuccess"
              multiple
          >
            <el-button type="primary">添加附件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                文件大小不超过10MB，最多上传5个文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">保存为草稿</el-radio>
            <el-radio :label="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看通知对话框 -->
    <el-dialog
        v-model="viewDialogVisible"
        :title="currentNotice.title"
        width="700px"
    >
      <div class="notice-view">
        <div class="notice-view-header">
          <div class="notice-view-info">
            <div class="notice-item">
              <span class="notice-label">发布者：</span>
              <span>{{ currentNotice.publisherName }}</span>
            </div>
            <div class="notice-item">
              <span class="notice-label">发布时间：</span>
              <span>{{ currentNotice.publishTime }}</span>
            </div>
            <div class="notice-item">
              <span class="notice-label">通知类型：</span>
              <el-tag :type="getNoticeTagType(currentNotice.type)" size="small">
                {{ getNoticeName(currentNotice.type) }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="notice-view-content">
          <div v-html="currentNotice.content"></div>
        </div>
        <div v-if="currentNotice.attachments && currentNotice.attachments.length > 0" class="notice-view-attachments">
          <div class="attachments-title">附件：</div>
          <div
              v-for="(attachment, index) in currentNotice.attachments"
              :key="index"
              class="attachment-item"
          >
            <el-button link type="primary" @click="downloadAttachment(attachment)">
              <el-icon>
                <Download/>
              </el-icon>
              {{ attachment.name }}
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Download, Search} from '@element-plus/icons-vue'
import {
  addNotice,
  deleteNotice as deleteNoticeApi,
  getNoticeById,
  getNoticeList,
  toggleNoticeStatus,
  updateNotice
} from '@/api/notice'
import {getTeacherCourses} from '@/api/course'
import {downloadFile, getUploadHeaders} from '@/api/file'

// 通知列表数据
const noticeList = ref([])
const listLoading = ref(false)
const total = ref(0)
const searchQuery = ref('')
const listQuery = reactive({
  page: 1,
  limit: 10,
  type: '',
  status: '',
  title: '',
  courseId: ''
})

// 对话框数据
const dialogVisible = ref(false)
const dialogType = ref('add')
const viewDialogVisible = ref(false)
const currentNotice = ref({})
const fileList = ref([])

// 表单数据
const formRef = ref(null)
const form = reactive({
  id: undefined,
  title: '',
  type: 1,
  content: '',
  scope: 1,
  courseId: undefined,
  status: 1,
  attachments: []
})

// 课程选项
const courseOptions = ref([])

// 通知类型选项
const noticeTypes = [
  {value: 1, label: '普通通知'},
  {value: 2, label: '重要通知'},
  {value: 3, label: '紧急通知'},
  {value: 4, label: '活动通知'},
  {value: 5, label: '其他通知'}
]

// 状态选项
const statusOptions = [
  {value: 0, label: '草稿'},
  {value: 1, label: '已发布'}
]

// 表单验证规则
const rules = {
  title: [{required: true, message: '请输入通知标题', trigger: 'blur'}],
  type: [{required: true, message: '请选择通知类型', trigger: 'change'}],
  courseId: [{required: true, message: '请选择课程', trigger: 'change'}],
  content: [{required: true, message: '请输入通知内容', trigger: 'blur'}]
}

// 上传相关
const uploadUrl = import.meta.env.VITE_API_BASE_URL + '/file/upload'
const uploadHeaders = computed(() => getUploadHeaders())

// 获取通知标签类型
const getNoticeTagType = (type) => {
  const typeMap = {
    1: '',          // 普通通知
    2: 'warning',   // 重要通知
    3: 'danger',    // 紧急通知
    4: 'success',   // 活动通知
    5: 'info'       // 其他通知
  }
  return typeMap[type] || ''
}

// 获取通知名称
const getNoticeName = (type) => {
  const nameMap = {
    1: '普通通知',
    2: '重要通知',
    3: '紧急通知',
    4: '活动通知',
    5: '其他通知'
  }
  return nameMap[type] || '未知类型'
}

// 获取通知列表
const getNotices = async () => {
  listLoading.value = true
  try {
    const params = {
      page: listQuery.page,
      pageSize: listQuery.limit,
      type: listQuery.type || undefined,
      status: listQuery.status !== '' ? listQuery.status : undefined,
      title: searchQuery.value || undefined,
      courseId: listQuery.courseId || undefined,
      isTeacher: true // 标记为教师查询
    }

    const response = await getNoticeList(params)
    noticeList.value = response.data.list || []
    total.value = response.data.total || 0
  } catch (error) {
    console.error('获取通知列表失败', error)
    ElMessage.error('获取通知列表失败')
  } finally {
    listLoading.value = false
  }
}

// 获取教师课程列表
const getCourses = async () => {
  try {
    const response = await getTeacherCourses()
    courseOptions.value = response.data || []
  } catch (error) {
    console.error('获取课程列表失败', error)
  }
}

// 筛选处理
const handleFilter = () => {
  listQuery.page = 1
  listQuery.title = searchQuery.value
  getNotices()
}

// 分页处理
const handleSizeChange = (val) => {
  listQuery.limit = val
  getNotices()
}

const handleCurrentChange = (val) => {
  listQuery.page = val
  getNotices()
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.id = undefined
  form.title = ''
  form.type = 1
  form.content = ''
  form.scope = 1
  form.courseId = undefined
  form.status = 1
  form.attachments = []
  fileList.value = []
}

// 打开添加对话框
const openAddDialog = () => {
  resetForm()
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 查看通知
const viewNotice = async (notice) => {
  try {
    const response = await getNoticeById(notice.id)
    currentNotice.value = response.data
    viewDialogVisible.value = true
  } catch (error) {
    console.error('获取通知详情失败', error)
    ElMessage.error('获取通知详情失败')
  }
}

// 编辑通知
const editNotice = async (notice) => {
  try {
    const response = await getNoticeById(notice.id)
    const noticeData = response.data

    form.id = noticeData.id
    form.title = noticeData.title
    form.type = noticeData.type
    form.content = noticeData.content
    form.scope = noticeData.courseId ? 2 : 1
    form.courseId = noticeData.courseId
    form.status = noticeData.status

    // 处理附件
    if (noticeData.attachments && noticeData.attachments.length > 0) {
      form.attachments = noticeData.attachments.map(att => att.id)
      fileList.value = noticeData.attachments.map(att => ({
        name: att.name,
        url: att.url,
        id: att.id
      }))
    } else {
      form.attachments = []
      fileList.value = []
    }

    dialogType.value = 'edit'
    dialogVisible.value = true
  } catch (error) {
    console.error('获取通知详情失败', error)
    ElMessage.error('获取通知详情失败')
  }
}

// 切换通知状态
const toggleStatus = async (notice) => {
  try {
    await toggleNoticeStatus(notice.id)

    ElMessage.success(notice.status === 1 ? '通知已下架' : '通知已发布')
    getNotices()
  } catch (error) {
    console.error('更新通知状态失败', error)
    ElMessage.error('更新通知状态失败')
  }
}

// 删除通知
const deleteNotice = async (notice) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除该通知吗？删除后将无法恢复。',
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await deleteNoticeApi(notice.id)
    ElMessage.success('删除通知成功')
    getNotices()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除通知失败', error)
      ElMessage.error('删除通知失败')
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    const noticeData = {
      title: form.title,
      type: form.type,
      content: form.content,
      courseId: form.scope === 2 ? form.courseId : null,
      status: form.status,
      attachments: form.attachments
    }

    if (dialogType.value === 'add') {
      await addNotice(noticeData)
      ElMessage.success('发布通知成功')
    } else {
      await updateNotice(form.id, noticeData)
      ElMessage.success('更新通知成功')
    }

    dialogVisible.value = false
    getNotices()
  } catch (error) {
    console.error('提交表单失败', error)
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('操作失败，请检查表单内容')
    }
  }
}

// 下载附件
const downloadAttachment = (attachment) => {
  if (attachment && attachment.id) {
    downloadFile(attachment.id)
        .then(() => {
          ElMessage.success('附件下载成功')
        })
        .catch(() => {
          ElMessage.error('附件下载失败')
        })
  }
}

// 上传文件相关方法
const handlePreview = (file) => {
  console.log(file)
}

const handleRemove = (file, fileList) => {
  form.attachments = form.attachments.filter(id => id !== file.id)
}

const handleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    form.attachments.push(response.data.id)
    file.id = response.data.id
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  return true
}

// 页面初始化
onMounted(() => {
  getNotices()
  getCourses()
})
</script>

<style scoped>
.notice-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notice-card {
  margin-bottom: 20px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.notice-view {
  padding: 0 20px;
}

.notice-view-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.notice-view-info {
  display: flex;
  flex-wrap: wrap;
}

.notice-item {
  margin-right: 20px;
  margin-bottom: 10px;
}

.notice-label {
  color: #909399;
}

.notice-view-content {
  margin-bottom: 20px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.notice-view-attachments {
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.attachments-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.attachment-item {
  margin-bottom: 5px;
}
</style> 