<template>
  <div class="notices-container">
    <div class="page-header">
      <h2>校园公告</h2>
      <div class="filter-container">
        <el-select
            v-model="filter.type"
            clearable
            placeholder="公告类型"
            style="width: 120px; margin-right: 15px"
            @change="handleFilterChange"
        >
          <el-option
              v-for="item in noticeTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
        <el-input
            v-model="filter.keyword"
            clearable
            placeholder="搜索公告标题"
            style="width: 220px"
            @input="handleFilterChange"
        >
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <el-card
        class="notice-list-card"
        shadow="hover"
    >
      <el-table
          v-loading="loading"
          :data="notices"
          style="width: 100%"
          @row-click="handleRowClick"
      >
        <el-table-column
            label="标题"
            min-width="300"
            prop="title"
        >
          <template #default="scope">
            <div class="notice-title">
              <el-tag
                  v-if="scope.row.type"
                  :type="getNoticeTypeTag(scope.row.type)"
                  class="notice-tag"
                  size="small"
              >
                {{ getNoticeTypeName(scope.row.type) }}
              </el-tag>
              <span>{{ scope.row.title }}</span>
              <el-tag
                  v-if="isNew(scope.row.publishTime)"
                  class="new-tag"
                  effect="plain"
                  size="small"
                  type="danger"
              >
                新
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column
            label="发布者"
            prop="publisher"
            width="150"
        />
        <el-table-column
            label="发布时间"
            prop="publishTime"
            width="180"
        >
          <template #default="scope">
            {{ formatDate(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            width="120"
        >
          <template #default="scope">
            <el-button
                size="small"
                type="primary"
                @click.stop="handleViewDetail(scope.row)"
            >
              查看详情
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
            :layout="'total, sizes, prev, pager, next, jumper'"
            :page-sizes="[10, 20, 50]"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 通知详情对话框 -->
    <el-dialog
        v-model="detailVisible"
        :title="currentNotice.title"
        destroy-on-close
        top="5vh"
        width="60%"
    >
      <template v-if="currentNotice.id">
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
      </template>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Document, Search} from '@element-plus/icons-vue'
import {getNoticeById, getNoticeList} from '@/api/notice'
import {downloadFile} from '@/api/file'

// 公告类型选项
const noticeTypes = [
  {label: '全部', value: ''},
  {label: '公告', value: 'ANNOUNCEMENT'},
  {label: '课程', value: 'COURSE'},
  {label: '考试', value: 'EXAM'},
  {label: '活动', value: 'ACTIVITY'},
  {label: '其他', value: 'OTHER'}
]

// 数据状态
const notices = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const filter = reactive({
  type: '',
  keyword: ''
})
const detailVisible = ref(false)
const currentNotice = reactive({})
const downloadLoading = ref(null)

// 获取公告列表
const fetchNotices = async () => {
  loading.value = true
  try {
    const res = await getNoticeList({
      page: currentPage.value,
      size: pageSize.value,
      type: filter.type,
      keyword: filter.keyword
    })
    notices.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取公告列表失败', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

// 获取公告详情
const fetchNoticeDetail = async (id) => {
  try {
    const res = await getNoticeById(id)
    Object.assign(currentNotice, res.data)
  } catch (error) {
    console.error('获取公告详情失败', error)
    ElMessage.error('获取公告详情失败')
  }
}

// 根据公告类型获取标签样式
const getNoticeTypeTag = (type) => {
  const map = {
    'ANNOUNCEMENT': '',
    'COURSE': 'success',
    'EXAM': 'danger',
    'ACTIVITY': 'warning',
    'OTHER': 'info'
  }
  return map[type] || 'info'
}

// 根据公告类型获取名称
const getNoticeTypeName = (type) => {
  const map = {
    'ANNOUNCEMENT': '公告',
    'COURSE': '课程',
    'EXAM': '考试',
    'ACTIVITY': '活动',
    'OTHER': '其他'
  }
  return map[type] || '通知'
}

// 判断是否为新公告(3天内)
const isNew = (publishTime) => {
  if (!publishTime) return false

  const now = new Date()
  const publishDate = new Date(publishTime)
  const diffTime = Math.abs(now - publishDate)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  return diffDays <= 3
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'

  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 处理筛选条件变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchNotices()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchNotices()
}

// 处理当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchNotices()
}

// 处理行点击
const handleRowClick = (row) => {
  handleViewDetail(row)
}

// 查看详情
const handleViewDetail = async (notice) => {
  detailVisible.value = true
  // 初始化当前通知对象
  Object.assign(currentNotice, notice)
  // 获取详情数据
  await fetchNoticeDetail(notice.id)
}

// 下载附件
const downloadAttachment = async (attachment) => {
  downloadLoading.value = attachment.id
  try {
    const response = await downloadFile(attachment.id)
    const blob = new Blob([response.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = attachment.filename
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('文件下载成功')
  } catch (error) {
    console.error('文件下载失败', error)
    ElMessage.error('文件下载失败')
  } finally {
    downloadLoading.value = null
  }
}

// 页面初始化
onMounted(() => {
  fetchNotices()
})
</script>

<style scoped>
.notices-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notice-list-card {
  margin-bottom: 20px;
}

.notice-title {
  display: flex;
  align-items: center;
}

.notice-tag {
  margin-right: 8px;
}

.new-tag {
  margin-left: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.notice-meta {
  margin: 15px 0;
  color: #909399;
  font-size: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.notice-content {
  margin: 20px 0;
  line-height: 1.6;
}

.notice-attachments {
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px solid #EBEEF5;
}

.notice-attachments h4 {
  margin-bottom: 10px;
}

.attachment-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.attachment-item .el-icon {
  margin-right: 5px;
}
</style> 