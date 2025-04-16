<template>
  <div class="student-home-container">
    <el-row :gutter="20">
      <!-- 学生个人信息卡片 -->
      <el-col :span="8">
        <el-card class="user-card" shadow="hover">
          <div class="user-info">
            <el-avatar
                :size="90"
                :src="userAvatar"
                class="user-avatar"
            >
              {{ userInfo.name ? userInfo.name.substring(0, 1) : 'U' }}
            </el-avatar>
            <div class="user-details">
              <h3>{{ userInfo.name || '同学' }}</h3>
              <p>{{ userInfo.username || '学号' }}</p>
              <p>{{ userInfo.department || '院系' }} {{ userInfo.major || '专业' }}</p>
              <el-button size="small" type="primary" @click="goToProfile">个人资料</el-button>
            </div>
          </div>
        </el-card>

        <!-- 学习数据统计 -->
        <el-card class="stats-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>学习统计</span>
            </div>
          </template>
          <div class="stats-list">
            <div class="stats-item">
              <div class="stats-icon selected-courses">
                <el-icon>
                  <Reading/>
                </el-icon>
              </div>
              <div class="stats-info">
                <h4>已选课程</h4>
                <div class="stats-value">{{ stats.courseCount || 0 }}</div>
              </div>
            </div>
            <div class="stats-item">
              <div class="stats-icon credit">
                <el-icon>
                  <Collection/>
                </el-icon>
              </div>
              <div class="stats-info">
                <h4>已修学分</h4>
                <div class="stats-value">{{ stats.totalCredit || 0 }}</div>
              </div>
            </div>
            <div class="stats-item">
              <div class="stats-icon gpa">
                <el-icon>
                  <Star/>
                </el-icon>
              </div>
              <div class="stats-info">
                <h4>平均绩点</h4>
                <div class="stats-value">{{ stats.gpa || '0.00' }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 课程信息和通知 -->
      <el-col :span="16">
        <!-- 今日课程 -->
        <el-card class="today-courses-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日课程</span>
              <el-button text @click="goToSchedule">查看课表</el-button>
            </div>
          </template>
          <div v-if="todayCourses.length > 0">
            <div v-for="(course, index) in todayCourses" :key="index" class="course-item">
              <div class="course-time">{{ course.startTime }} - {{ course.endTime }}</div>
              <div class="course-info">
                <div class="course-name">{{ course.courseName }}</div>
                <div class="course-location">
                  <el-icon>
                    <Location/>
                  </el-icon>
                  {{ course.classroom }}
                </div>
              </div>
              <div class="course-status">
                <el-tag
                    :type="getCourseStatus(course).type"
                    size="small"
                >
                  {{ getCourseStatus(course).label }}
                </el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="今天没有课程"/>
        </el-card>

        <!-- 最新通知 -->
        <el-card class="notice-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最新通知</span>
              <el-button text @click="goToNotices">查看全部</el-button>
            </div>
          </template>
          <div v-if="notices.length > 0">
            <div v-for="notice in notices" :key="notice.id" class="notice-item" @click="viewNoticeDetail(notice)">
              <div class="notice-title">
                <el-tag
                    v-if="notice.type"
                    :type="getNoticeTypeTag(notice.type)"
                    class="notice-tag"
                    size="small"
                >
                  {{ getNoticeTypeName(notice.type) }}
                </el-tag>
                <span>{{ notice.title }}</span>
              </div>
              <div class="notice-date">{{ formatDate(notice.publishTime) }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无通知"/>
        </el-card>

        <!-- 近期活动 -->
        <el-card class="activity-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>校园活动</span>
              <el-button text @click="goToActivities">更多活动</el-button>
            </div>
          </template>
          <div v-if="activities.length > 0" class="activity-list">
            <div v-for="activity in activities" :key="activity.id" class="activity-item"
                 @click="viewActivityDetail(activity)">
              <img :src="activity.coverImage || 'https://via.placeholder.com/100x100'" class="activity-image"/>
              <div class="activity-info">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-time">
                  <el-icon>
                    <Calendar/>
                  </el-icon>
                  {{ formatDate(activity.startTime) }}
                </div>
                <div class="activity-location">
                  <el-icon>
                    <Location/>
                  </el-icon>
                  {{ activity.location }}
                </div>
              </div>
              <div class="activity-status">
                <el-tag v-if="activity.status === 1" size="small" type="success">报名中</el-tag>
                <el-tag v-else-if="activity.status === 2" size="small" type="warning">进行中</el-tag>
                <el-tag v-else-if="activity.status === 3" size="small" type="info">已结束</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无活动"/>
        </el-card>
      </el-col>
    </el-row>

    <!-- 通知详情对话框 -->
    <el-dialog
        v-model="noticeDetailVisible"
        title="通知详情"
        width="60%"
    >
      <template v-if="currentNotice">
        <h3>{{ currentNotice.title }}</h3>
        <div class="notice-meta">
          <span>发布人: {{ currentNotice.publisher }}</span>
          <span>发布时间: {{ formatDate(currentNotice.publishTime) }}</span>
          <span>类型: {{ getNoticeTypeName(currentNotice.type) }}</span>
        </div>
        <div class="notice-content" v-html="currentNotice.content"></div>

        <div v-if="currentNotice.attachments && currentNotice.attachments.length > 0" class="notice-attachments">
          <h4>附件:</h4>
          <div v-for="(attachment, index) in currentNotice.attachments" :key="index" class="attachment-item">
            <el-button
                link
                type="primary"
                @click="downloadAttachment(attachment)"
            >
              {{ attachment.filename }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Calendar, Collection, Location, Reading, Star} from '@element-plus/icons-vue'
import {getUserInfo} from '@/api/user'
import {getStudentSchedule} from '@/api/schedule'
import {getStudentStats} from '@/api/common'
import {getNoticeList} from '@/api/notice'
import {getActivityList} from '@/api/activity'
import {downloadFile} from '@/api/file'

const router = useRouter()
const userInfo = ref({})
const userAvatar = ref('')
const stats = reactive({
  courseCount: 0,
  totalCredit: 0,
  gpa: '0.00'
})
const todayCourses = ref([])
const notices = ref([])
const activities = ref([])
const noticeDetailVisible = ref(false)
const currentNotice = ref(null)

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
    userAvatar.value = res.data.avatar || ''
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

// 获取学生统计数据
const fetchStudentStats = async () => {
  try {
    const res = await getStudentStats()
    stats.courseCount = res.data.courseCount || 0
    stats.totalCredit = res.data.totalCredit || 0
    stats.gpa = (res.data.gpa || 0).toFixed(2)
  } catch (error) {
    console.error('获取学生统计数据失败', error)
  }
}

// 获取今日课程
const fetchTodayCourses = async () => {
  try {
    const res = await getStudentSchedule({today: true})
    todayCourses.value = res.data || []
  } catch (error) {
    console.error('获取今日课程失败', error)
  }
}

// 获取最新通知
const fetchNotices = async () => {
  try {
    const res = await getNoticeList({page: 1, size: 5})
    notices.value = res.data.list || []
  } catch (error) {
    console.error('获取通知失败', error)
  }
}

// 获取近期活动
const fetchActivities = async () => {
  try {
    const res = await getActivityList({page: 1, size: 3, status: 1})
    activities.value = res.data.list || []
  } catch (error) {
    console.error('获取活动失败', error)
  }
}

// 获取课程状态
const getCourseStatus = (course) => {
  const now = new Date()
  const currentHour = now.getHours()
  const currentMinute = now.getMinutes()
  const currentTime = currentHour * 60 + currentMinute

  const [startHour, startMinute] = course.startTime.split(':').map(Number)
  const startTimeInMinutes = startHour * 60 + startMinute

  const [endHour, endMinute] = course.endTime.split(':').map(Number)
  const endTimeInMinutes = endHour * 60 + endMinute

  if (currentTime < startTimeInMinutes) {
    return {type: 'info', label: '未开始'}
  } else if (currentTime >= startTimeInMinutes && currentTime <= endTimeInMinutes) {
    return {type: 'success', label: '进行中'}
  } else {
    return {type: 'warning', label: '已结束'}
  }
}

// 获取通知类型标签样式
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

// 获取通知类型名称
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

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'

  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 查看通知详情
const viewNoticeDetail = (notice) => {
  currentNotice.value = notice
  noticeDetailVisible.value = true
}

// 查看活动详情
const viewActivityDetail = (activity) => {
  router.push(`/student/activity/${activity.id}`)
}

// 下载附件
const downloadAttachment = async (attachment) => {
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
  }
}

// 页面导航
const goToProfile = () => {
  router.push('/student/profile')
}

const goToSchedule = () => {
  router.push('/student/schedule')
}

const goToNotices = () => {
  router.push('/student/notices')
}

const goToActivities = () => {
  router.push('/student/activities')
}

// 页面初始化时加载数据
onMounted(() => {
  fetchUserInfo()
  fetchStudentStats()
  fetchTodayCourses()
  fetchNotices()
  fetchActivities()
})
</script>

<style scoped>
.student-home-container {
  padding: 20px;
}

.user-card, .stats-card, .today-courses-card, .notice-card, .activity-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.user-card:hover, .stats-card:hover, .today-courses-card:hover, .notice-card:hover, .activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 用户信息卡片样式 */
.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}

.user-avatar {
  margin-bottom: 15px;
}

.user-details {
  text-align: center;
}

.user-details h3 {
  margin: 5px 0;
  font-size: 18px;
}

.user-details p {
  margin: 5px 0;
  color: #909399;
  font-size: 14px;
}

/* 统计数据样式 */
.stats-list {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}

.stats-item {
  display: flex;
  align-items: center;
  padding: 10px;
  width: 100%;
  margin-bottom: 10px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.stats-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  color: white;
}

.stats-icon.selected-courses {
  background-color: #409EFF;
}

.stats-icon.credit {
  background-color: #67C23A;
}

.stats-icon.gpa {
  background-color: #E6A23C;
}

.stats-info {
  flex: 1;
}

.stats-info h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #606266;
}

.stats-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

/* 今日课程样式 */
.course-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
}

.course-item:last-child {
  border-bottom: none;
}

.course-time {
  width: 120px;
  color: #606266;
  font-size: 14px;
  padding-right: 15px;
}

.course-info {
  flex: 1;
}

.course-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.course-location {
  color: #909399;
  font-size: 13px;
  display: flex;
  align-items: center;
}

.course-location .el-icon {
  margin-right: 5px;
}

.course-status {
  margin-left: 15px;
  display: flex;
  align-items: center;
}

/* 通知样式 */
.notice-item {
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notice-item:hover {
  background-color: #F5F7FA;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  flex: 1;
  display: flex;
  align-items: center;
}

.notice-tag {
  margin-right: 8px;
}

.notice-date {
  color: #909399;
  font-size: 13px;
}

/* 活动样式 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-item {
  display: flex;
  padding: 10px;
  border-radius: 4px;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s;
}

.activity-item:hover {
  background-color: #ecf5ff;
}

.activity-image {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.activity-info {
  flex: 1;
}

.activity-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.activity-time, .activity-location {
  color: #909399;
  font-size: 13px;
  display: flex;
  align-items: center;
  margin-bottom: 3px;
}

.activity-time .el-icon, .activity-location .el-icon {
  margin-right: 5px;
}

.activity-status {
  display: flex;
  align-items: flex-start;
  margin-left: 10px;
}

/* 通知详情样式 */
.notice-meta {
  margin: 15px 0;
  color: #909399;
  font-size: 14px;
  display: flex;
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
  margin-bottom: 5px;
}
</style> 