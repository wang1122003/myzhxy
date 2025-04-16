<template>
  <div class="activity-detail-container">
    <div class="page-header">
      <div class="back-button">
        <el-button text @click="goBack">
          <el-icon>
            <ArrowLeft/>
          </el-icon>
          返回活动列表
        </el-button>
      </div>
      <div class="header-actions">
        <el-button
            v-if="!activity.enrolled && activity.status === 1"
            :loading="joinLoading"
            type="primary"
            @click="handleJoin"
        >
          立即报名
        </el-button>
        <el-button
            v-if="activity.enrolled && activity.status === 1"
            :loading="cancelLoading"
            type="danger"
            @click="handleCancel"
        >
          取消报名
        </el-button>
      </div>
    </div>

    <el-card v-loading="loading" class="activity-detail-card" shadow="hover">
      <template v-if="activity.id">
        <div class="activity-header">
          <h1 class="activity-title">{{ activity.title }}</h1>
          <div class="activity-status">
            <el-tag v-if="activity.status === 1" type="success">报名中</el-tag>
            <el-tag v-else-if="activity.status === 2" type="warning">进行中</el-tag>
            <el-tag v-else-if="activity.status === 3" type="info">已结束</el-tag>
          </div>
        </div>

        <div class="activity-cover">
          <img :src="activity.coverImage || 'https://via.placeholder.com/800x300'" alt="活动封面"/>
        </div>

        <div class="activity-meta">
          <div class="meta-item">
            <el-icon>
              <Calendar/>
            </el-icon>
            <span>活动时间：{{ formatDate(activity.startTime) }} 至 {{ formatDate(activity.endTime) }}</span>
          </div>
          <div class="meta-item">
            <el-icon>
              <Location/>
            </el-icon>
            <span>活动地点：{{ activity.location }}</span>
          </div>
          <div class="meta-item">
            <el-icon>
              <User/>
            </el-icon>
            <span>主办方：{{ activity.organizer }}</span>
          </div>
          <div class="meta-item">
            <el-icon>
              <InfoFilled/>
            </el-icon>
            <span>活动类型：{{ getActivityTypeName(activity.type) }}</span>
          </div>
          <div class="meta-item">
            <el-icon>
              <Document/>
            </el-icon>
            <span>报名人数：{{ activity.enrolledCount || 0 }}</span>
          </div>
          <div v-if="activity.quota" class="meta-item">
            <el-icon>
              <Star/>
            </el-icon>
            <span>可获学分：{{ activity.quota }}</span>
          </div>
        </div>

        <el-divider content-position="left">活动详情</el-divider>

        <div class="activity-content" v-html="activity.description"></div>

        <div v-if="activity.attachments && activity.attachments.length > 0" class="activity-attachments">
          <el-divider content-position="left">活动资料</el-divider>
          <div v-for="(attachment, index) in activity.attachments" :key="index" class="attachment-item">
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

        <template v-if="activity.enrolled">
          <el-divider content-position="left">报名信息</el-divider>
          <div class="enrollment-info">
            <p>您已成功报名此活动！</p>
            <p>报名时间：{{ formatDateTime(activity.enrollTime) }}</p>
            <div v-if="activity.participationStatus">
              <p>参与状态：
                <el-tag
                    :type="activity.participationStatus === 'ATTENDED' ? 'success' : 'info'"
                >
                  {{
                    activity.participationStatus === 'ATTENDED' ? '已参加' :
                        activity.participationStatus === 'ABSENT' ? '未参加' : '待确认'
                  }}
                </el-tag>
              </p>
            </div>
          </div>
        </template>

        <template v-if="activity.participants && activity.participants.length > 0">
          <el-divider content-position="left">参与人员</el-divider>
          <div class="participant-list">
            <el-avatar
                v-for="participant in activity.participants"
                :key="participant.id"
                :size="50"
                :src="participant.avatar"
                class="participant-avatar"
            >
              {{ participant.name ? participant.name.substring(0, 1) : 'U' }}
            </el-avatar>
            <div v-if="activity.participants.length >= 10" class="more-participants">
              <el-button text @click="showAllParticipants">查看全部</el-button>
            </div>
          </div>
        </template>
      </template>
    </el-card>

    <!-- 参与人员对话框 -->
    <el-dialog
        v-model="participantsVisible"
        title="活动参与人员"
        width="50%"
    >
      <div class="participants-dialog">
        <div v-for="participant in activity.participants" :key="participant.id" class="participant-item">
          <el-avatar :size="40" :src="participant.avatar">
            {{ participant.name ? participant.name.substring(0, 1) : 'U' }}
          </el-avatar>
          <div class="participant-info">
            <div class="participant-name">{{ participant.name }}</div>
            <div class="participant-department">{{ participant.department || '未知院系' }}</div>
          </div>
          <div class="participant-status">
            <el-tag
                v-if="participant.status"
                :type="participant.status === 'ATTENDED' ? 'success' : 'info'"
                size="small"
            >
              {{ participant.status === 'ATTENDED' ? '已参加' : '已报名' }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {ArrowLeft, Calendar, Document, InfoFilled, Location, Star, User} from '@element-plus/icons-vue'
import {cancelJoinActivity, getActivityDetail, joinActivity} from '@/api/activity'
import {downloadFile} from '@/api/file'

const router = useRouter()
const route = useRoute()
const activityId = route.params.id

const activity = reactive({})
const loading = ref(false)
const joinLoading = ref(false)
const cancelLoading = ref(false)
const downloadLoading = ref(null)
const participantsVisible = ref(false)

// 获取活动详情
const fetchActivityDetail = async () => {
  loading.value = true
  try {
    const res = await getActivityDetail(activityId)
    Object.assign(activity, res.data)
  } catch (error) {
    console.error('获取活动详情失败', error)
    ElMessage.error('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

// 获取活动类型名称
const getActivityTypeName = (type) => {
  const typeMap = {
    'ACADEMIC': '学术讲座',
    'CULTURAL': '文化活动',
    'SPORTS': '体育赛事',
    'CAREER': '就业招聘',
    'SOCIAL': '社交活动',
    'VOLUNTEER': '志愿服务',
    'OTHER': '其他'
  }
  return typeMap[type] || '未知类型'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'

  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '-'

  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 返回活动列表
const goBack = () => {
  router.push('/student/activities')
}

// 报名活动
const handleJoin = async () => {
  try {
    await ElMessageBox.confirm(
        '确定要报名参加此活动吗？',
        '报名确认',
        {
          confirmButtonText: '确定报名',
          cancelButtonText: '取消',
          type: 'info'
        }
    )

    joinLoading.value = true
    await joinActivity(activityId)
    ElMessage.success('报名成功')
    fetchActivityDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('报名失败', error)
      ElMessage.error(error.response?.data?.message || '报名失败')
    }
  } finally {
    joinLoading.value = false
  }
}

// 取消报名
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
        '确定要取消报名此活动吗？',
        '取消报名确认',
        {
          confirmButtonText: '确定取消',
          cancelButtonText: '返回',
          type: 'warning'
        }
    )

    cancelLoading.value = true
    await cancelJoinActivity(activityId)
    ElMessage.success('已取消报名')
    fetchActivityDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消报名失败', error)
      ElMessage.error(error.response?.data?.message || '取消报名失败')
    }
  } finally {
    cancelLoading.value = false
  }
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

// 显示所有参与人员
const showAllParticipants = () => {
  participantsVisible.value = true
}

// 页面初始化
onMounted(() => {
  fetchActivityDetail()
})
</script>

<style scoped>
.activity-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.activity-detail-card {
  margin-bottom: 20px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.activity-title {
  margin: 0;
  font-size: 24px;
}

.activity-cover {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.activity-cover img {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
}

.activity-meta {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.meta-item:last-child {
  margin-bottom: 0;
}

.meta-item .el-icon {
  margin-right: 8px;
  color: #409EFF;
}

.activity-content {
  line-height: 1.8;
  margin-bottom: 20px;
}

.activity-attachments {
  margin-top: 20px;
}

.attachment-item {
  margin-bottom: 8px;
}

.enrollment-info {
  background-color: #f0f9eb;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.enrollment-info p {
  margin: 5px 0;
}

.participant-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.participant-avatar {
  margin-right: 5px;
  transition: all 0.3s;
}

.participant-avatar:hover {
  transform: scale(1.1);
}

.more-participants {
  margin-left: 10px;
}

.participants-dialog {
  max-height: 400px;
  overflow-y: auto;
}

.participant-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
}

.participant-item:last-child {
  border-bottom: none;
}

.participant-info {
  margin-left: 10px;
  flex: 1;
}

.participant-name {
  font-weight: bold;
}

.participant-department {
  color: #909399;
  font-size: 12px;
}

.participant-status {
  margin-left: 10px;
}
</style> 