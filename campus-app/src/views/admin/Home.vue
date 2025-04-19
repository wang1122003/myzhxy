<template>
  <div class="admin-dashboard">
    <h2 class="dashboard-title">管理员控制台</h2>

    <!-- 数据统计卡片 -->
    <el-row :gutter="20">
      <el-col v-for="(stat, index) in statistics" :key="index" :lg="6" :md="8" :sm="12" :xs="24">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div :class="`stat-icon ${stat.color}`">
              <el-icon>
                <component :is="stat.icon"/>
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-name">{{ stat.name }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表展示 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :lg="12" :xs="24">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户分布</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <el-progress :color="'#67C23A'" :format="() => '学生'" :percentage="userDistribution.student"
                         type="dashboard"/>
            <el-progress :color="'#409EFF'" :format="() => '教师'" :percentage="userDistribution.teacher"
                         type="dashboard"/>
            <el-progress :color="'#E6A23C'" :format="() => '管理员'" :percentage="userDistribution.admin"
                         type="dashboard"/>
          </div>
        </el-card>
      </el-col>
      <el-col :lg="12" :xs="24">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
            </div>
          </template>
          <div class="system-status">
            <div class="status-item">
              <span class="status-label">系统版本</span>
              <span class="status-value">v1.0.0</span>
            </div>
            <div class="status-item">
              <span class="status-label">服务器状态</span>
              <span class="status-value"><el-tag type="success">正常</el-tag></span>
            </div>
            <div class="status-item">
              <span class="status-label">数据库状态</span>
              <span class="status-value"><el-tag type="success">正常</el-tag></span>
            </div>
            <div class="status-item">
              <span class="status-label">最后更新时间</span>
              <span class="status-value">{{ currentTime }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 通知公告和最近活动 -->
    <el-row :gutter="20" class="info-row">
      <el-col :lg="12" :xs="24">
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最新通知</span>
              <el-button text @click="goToNotices">更多</el-button>
            </div>
          </template>
          <div v-if="notices.length > 0">
            <div v-for="notice in notices" :key="notice.id" class="notice-item" @click="viewNotice(notice)">
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-meta">
                <el-tag :type="getNoticeTypeClass(notice.type)" size="small">{{
                    getNoticeTypeName(notice.type)
                  }}
                </el-tag>
                <span class="notice-time">{{ formatDate(notice.publishTime) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无通知"/>
        </el-card>
      </el-col>
      <el-col :lg="12" :xs="24">
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统日志</span>
              <el-button text>查看全部</el-button>
            </div>
          </template>
          <div class="log-list">
            <div v-for="(log, index) in systemLogs" :key="index" class="log-item">
              <div class="log-time">{{ log.time }}</div>
              <div class="log-content">
                <el-tag :type="log.type" size="small">{{ log.level }}</el-tag>
                <span class="log-message">{{ log.message }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getUserList} from '@/api/user'
import {getCourseList} from '@/api/course'
import {getClassroomList} from '@/api/classroom'
import {getActivityList} from '@/api/activity'
import {getNoticeList} from '@/api/notice'
import {getPostList} from '@/api/forum'

const router = useRouter()
const userCount = ref(0)
const courseCount = ref(0)
const classroomCount = ref(0)
const activityCount = ref(0)
const noticeCount = ref(0)
const postCount = ref(0)
const notices = ref([])
const userDistribution = ref({
  student: 60,
  teacher: 30,
  admin: 10
})

const currentTime = ref(new Date().toLocaleString())

// 模拟系统日志
const systemLogs = ref([
  {time: '2025-04-19 06:10:48', level: 'INFO', type: 'info', message: 'System started successfully'},
  {time: '2025-04-19 06:10:55', level: 'INFO', type: 'info', message: 'Database connection established'},
  {time: '2025-04-19 06:11:05', level: 'WARN', type: 'warning', message: 'Failed to load some user preferences'},
  {time: '2025-04-19 06:12:57', level: 'INFO', type: 'info', message: 'User admin logged in'},
  {
    time: '2025-04-19 06:15:15',
    level: 'ERROR',
    type: 'danger',
    message: 'Database query failed: Unknown column in field list'
  },
])

// 统计卡片数据
const statistics = computed(() => [
  {name: '用户总数', value: userCount.value, icon: 'User', color: 'blue'},
  {name: '课程总数', value: courseCount.value, icon: 'Document', color: 'green'},
  {name: '教室总数', value: classroomCount.value, icon: 'School', color: 'orange'},
  {name: '活动总数', value: activityCount.value, icon: 'Bell', color: 'purple'},
  {name: '通知总数', value: noticeCount.value, icon: 'ChatLineRound', color: 'cyan'},
  {name: '论坛帖子', value: postCount.value, icon: 'ChatDotRound', color: 'red'},
])

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 获取通知类型名称
const getNoticeTypeName = (type) => {
  const types = {
    1: '系统通知',
    2: '院系通知',
    3: '课程通知',
  }
  return types[type] || '其他'
}

// 获取通知类型对应的标签类型
const getNoticeTypeClass = (type) => {
  const types = {
    1: 'danger',
    2: 'warning',
    3: 'info',
  }
  return types[type] || 'info'
}

// 查看通知详情
const viewNotice = (notice) => {
  // 跳转到通知详情页面
  router.push(`/admin/notice/${notice.id}`)
}

// 跳转到通知管理页面
const goToNotices = () => {
  router.push('/admin/notice')
}

// 加载数据
const fetchData = async () => {
  try {
    // 获取用户数量
    const userRes = await getUserList({page: 1, size: 1})
    userCount.value = userRes.data.total || 0

// 获取课程数量
    const courseRes = await getCourseList({page: 1, size: 1})
    courseCount.value = courseRes.data.total || 0

// 获取教室数量
    const classroomRes = await getClassroomList({page: 1, size: 1})
    classroomCount.value = classroomRes.data.total || 0

// 获取活动数量
    const activityRes = await getActivityList({page: 1, size: 1})
    activityCount.value = activityRes.data.total || 0

    // 获取通知数量和最新通知
    const noticeRes = await getNoticeList({page: 1, size: 5})
    noticeCount.value = noticeRes.data.total || 0
    notices.value = noticeRes.data.list || []

// 获取帖子数量
    const postRes = await getPostList({page: 1, size: 1})
    postCount.value = postRes.data.total || 0
  } catch (error) {
    console.error('加载数据失败', error)
    ElMessage.error('加载数据失败，请刷新页面重试')
  }
}

onMounted(() => {
  fetchData()

  // 更新当前时间
  setInterval(() => {
    currentTime.value = new Date().toLocaleString()
  }, 1000)
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.dashboard-title {
  margin-bottom: 24px;
  color: #303133;
  font-weight: 500;
}

.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stat-icon.blue {
  background-color: #409EFF;
}

.stat-icon.green {
  background-color: #67C23A;
}

.stat-icon.orange {
  background-color: #E6A23C;
}

.stat-icon.red {
  background-color: #F56C6C;
}

.stat-icon.purple {
  background-color: #8E44AD;
}

.stat-icon.cyan {
  background-color: #20B2AA;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-name {
  font-size: 14px;
  color: #909399;
}

.chart-row, .info-row {
  margin-top: 20px;
}

.chart-card, .info-card {
  margin-bottom: 20px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 20px 0;
}

.system-status {
  padding: 20px 0;
}

.status-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  color: #606266;
}

.status-value {
  font-weight: 500;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: all 0.3s;
}

.notice-item:hover {
  background-color: #F5F7FA;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 14px;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.log-list {
  max-height: 300px;
  overflow-y: auto;
}

.log-item {
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.log-content {
  display: flex;
  align-items: center;
}

.log-message {
  margin-left: 8px;
  font-size: 13px;
}
</style> 