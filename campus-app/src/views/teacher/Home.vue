<template>
  <div class="teacher-dashboard">
    <div class="page-header">
      <h2>教师工作台</h2>
      <p class="welcome-text">欢迎回来，{{ userInfo.name || '老师' }}！今天是 {{ currentDate }}</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col v-for="(stat, index) in statistics" :key="index" :md="6" :sm="12" :xs="24">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div :style="{ backgroundColor: stat.color }" class="stat-icon">
              <el-icon>
                <component :is="stat.icon"/>
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-row">
      <!-- 左侧列 -->
      <el-col :md="12" :xs="24">
        <!-- 今日课程 -->
        <el-card class="dashboard-card today-courses" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日课程</span>
              <el-button text type="primary" @click="goToSchedule">查看完整课表</el-button>
            </div>
          </template>
          <div v-if="loadingCourses" class="loading-placeholder">
            <el-skeleton :rows="4" animated/>
          </div>
          <div v-else-if="todayCourses.length > 0" class="course-list">
            <div v-for="course in todayCourses" :key="course.id" class="course-item" @click="viewCourseDetail(course)">
              <div class="course-time">
                <div class="time-range">{{ course.startTime }} - {{ course.endTime }}</div>
                <div :class="getCourseStatus(course).class" class="time-status">
                  {{ getCourseStatus(course).text }}
                </div>
              </div>
              <div class="course-info">
                <div class="course-name">{{ course.courseName }}</div>
                <div class="course-location">
                  <el-icon>
                    <Location/>
                  </el-icon>
                  <span>{{ course.classroom }}</span>
                </div>
                <div v-if="course.className" class="course-class">
                  <el-icon>
                    <User/>
                  </el-icon>
                  <span>{{ course.className }} ({{ course.studentCount || 0 }}人)</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="今日没有课程安排"/>
        </el-card>
      </el-col>

      <!-- 右侧列 -->
      <el-col :md="12" :xs="24">
        <!-- 最新通知 -->
        <el-card class="dashboard-card latest-notices" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最新通知</span>
              <el-button text type="primary" @click="goToNotices">查看全部</el-button>
            </div>
          </template>
          <div v-if="loadingNotices" class="loading-placeholder">
            <el-skeleton :rows="3" animated/>
          </div>
          <div v-else-if="notices.length > 0" class="notice-list">
            <div v-for="notice in notices" :key="notice.id" class="notice-item" @click="viewNoticeDetail(notice)">
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-meta">
                <el-tag :type="getNoticeTagType(notice.type)" size="small">{{ getNoticeName(notice.type) }}</el-tag>
                <span class="notice-time">{{ formatDate(notice.publishTime) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无通知"/>
        </el-card>

        <!-- 近期截止任务 -->
        <el-card class="dashboard-card upcoming-tasks" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>近期截止任务</span>
              <el-button text type="primary" @click="goToCourses">管理课程</el-button>
            </div>
          </template>
          <div v-if="loadingTasks" class="loading-placeholder">
            <el-skeleton :rows="2" animated/>
          </div>
          <div v-else-if="upcomingTasks.length > 0" class="task-list">
            <div v-for="task in upcomingTasks" :key="task.id" class="task-item">
              <div class="task-info">
                <span class="task-course">{{ task.courseName }}</span>
                <span class="task-name">{{ task.taskName }}</span>
              </div>
              <div class="task-deadline">
                <el-icon>
                  <Clock/>
                </el-icon>
                <span>{{ formatDate(task.deadline) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无截止任务"/>
        </el-card>
      </el-col>
    </el-row>

    <!-- 通知详情对话框 -->
    <NoticeDetailDialog
        v-model="noticeDialogVisible"
        :notice-id="currentNoticeId"
    />

  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Clock, List, Location, Reading, User} from '@element-plus/icons-vue'
import {getUserInfo} from '@/api/user' // Use getUserInfo or getCurrentUser
import {getTeacherSchedule} from '@/api/schedule'
import {getNoticeList} from '@/api/notice' // 修正导入，虽然 Home 本身可能不用 getNoticeById，但引用的 Dialog 会用
import NoticeDetailDialog from '@/components/common/NoticeDetailDialog.vue' // 引入通知详情组件

const router = useRouter()

// --- State ---
const userInfo = ref({})
const currentDate = ref(new Date().toLocaleDateString('zh-CN', {
  weekday: 'long',
  year: 'numeric',
  month: 'long',
  day: 'numeric'
}))
const stats = reactive({
  totalCourses: 0,
  totalStudents: 0,
  teachingHours: 0,
  pendingGrades: 0
})
const todayCourses = ref([])
const notices = ref([])
const upcomingTasks = ref([])
const loadingCourses = ref(false)
const loadingNotices = ref(false)
const loadingTasks = ref(false)
const loadingStats = ref(false)

const noticeDialogVisible = ref(false)
const currentNoticeId = ref(null)

// --- Computed ---
const statistics = computed(() => [
  {value: stats.totalCourses, label: '授课课程数', icon: Reading, color: '#409EFF'},
  {value: stats.totalStudents, label: '学生总数', icon: User, color: '#67C23A'},
  {value: stats.teachingHours, label: '本周课时', icon: Clock, color: '#E6A23C'},
  {value: stats.pendingGrades, label: '待登记成绩', icon: List, color: '#F56C6C'}
])

// --- Methods ---
const fetchUserInfo = async () => {
  try {
    // It's better to use getCurrentUser if available and consistent
    const res = await getUserInfo() // Or use getCurrentUser()
    userInfo.value = res.data || {}
  } catch (error) {
    console.error('获取教师信息失败:', error)
    // ElMessage.error('获取教师信息失败')
  }
}

const fetchDashboardData = async () => {
  loadingStats.value = true
  loadingCourses.value = true
  loadingNotices.value = true
  loadingTasks.value = true

  try {
    // Fetch teacher specific stats (assuming an API exists)
    // const statsRes = await getTeacherStats();
    // stats.totalCourses = statsRes.data.totalCourses || 0;
    // stats.totalStudents = statsRes.data.totalStudents || 0;
    // stats.teachingHours = statsRes.data.teachingHours || 0;
    // stats.pendingGrades = statsRes.data.pendingGrades || 0;
    // Mock data for now:
    stats.totalCourses = 5
    stats.totalStudents = 120
    stats.teachingHours = 16
    stats.pendingGrades = 2

    // Fetch today's schedule
    const scheduleRes = await getTeacherSchedule({ /* params */})
    const allCourses = scheduleRes.data?.courses || []
    const today = new Date().getDay()
    todayCourses.value = allCourses.filter(course => course.dayOfWeek === today)
        .sort((a, b) => a.startTime.localeCompare(b.startTime))

    // Fetch recent notices
    const noticeRes = await getNoticeList({page: 1, size: 5})
    notices.value = noticeRes.data?.list || []

    // Fetch upcoming tasks (assuming API exists)
    // const taskRes = await getUpcomingTasks();
    // upcomingTasks.value = taskRes.data || [];
    // Mock data for now:
    upcomingTasks.value = [
      {id: 1, courseName: '高等数学', taskName: '期中测验成绩录入', deadline: '2025-04-25'},
      {id: 2, courseName: '大学物理', taskName: '实验报告批阅', deadline: '2025-04-28'},
    ]

  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error('加载仪表盘数据失败')
  } finally {
    loadingStats.value = false
    loadingCourses.value = false
    loadingNotices.value = false
    loadingTasks.value = false
  }
}

const goToSchedule = () => {
  router.push('/teacher/schedule')
}

const goToNotices = () => {
  router.push('/teacher/notices')
}

const goToCourses = () => {
  router.push('/teacher/courses')
}

const viewCourseDetail = (course) => {
  // Navigate to course detail or management page
  router.push(`/teacher/courses/${course.courseId || course.id}`) // Adjust based on your routing
}

const viewNoticeDetail = (notice) => {
  currentNoticeId.value = notice.id
  noticeDialogVisible.value = true
}

// Format Date
const formatDate = (dateString) => {
  if (!dateString) return ''
  const options = {year: 'numeric', month: 'short', day: 'numeric'};
  return new Date(dateString).toLocaleDateString('zh-CN', options);
}

// Get course status (example logic)
const getCourseStatus = (course) => {
  const now = new Date();
  const start = new Date(now.toDateString() + ' ' + course.startTime);
  const end = new Date(now.toDateString() + ' ' + course.endTime);
  if (now < start) return {text: '未开始', class: 'status-info'};
  if (now >= start && now <= end) return {text: '进行中', class: 'status-success'};
  return {text: '已结束', class: 'status-warning'};
}

// Get Notice Name and Tag Type (reuse from student dashboard or centralize)
const noticeTypeMap = {
  1: {name: '系统通知', tag: 'danger'},
  2: {name: '院系通知', tag: 'warning'},
  3: {name: '课程通知', tag: 'primary'},
  default: {name: '其他', tag: 'info'}
};
const getNoticeName = (type) => (noticeTypeMap[type] || noticeTypeMap.default).name;
const getNoticeTagType = (type) => (noticeTypeMap[type] || noticeTypeMap.default).tag;

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchUserInfo()
  fetchDashboardData()
})

</script>

<style scoped>
.teacher-dashboard {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 5px 0;
  font-size: 24px;
  font-weight: 500;
}

.welcome-text {
  color: #606266;
  font-size: 14px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.content-row {
  /* Styles for rows containing main content cards */
}

.dashboard-card {
  margin-bottom: 20px;
  height: calc(100% - 20px); /* Adjust height considering margin */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.loading-placeholder {
  padding: 20px;
}

/* Today Courses */
.course-list {
  max-height: 350px; /* Adjust as needed */
  overflow-y: auto;
}

.course-item {
  display: flex;
  align-items: flex-start;
  padding: 15px 0;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.3s;
}
.course-item:hover {
  background-color: #f5f7fa;
}

.course-item:last-child {
  border-bottom: none;
}

.course-time {
  width: 100px;
  text-align: center;
  margin-right: 15px;
  flex-shrink: 0;
}

.time-range {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 5px;
}

.time-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  color: #fff;
}

.status-info {
  background-color: #909399;
}

.status-success {
  background-color: #67C23A;
}

.status-warning {
  background-color: #E6A23C;
}

.course-info {
  flex-grow: 1;
}

.course-name {
  font-weight: 500;
  margin-bottom: 6px;
}

.course-location,
.course-class {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.course-location .el-icon,
.course-class .el-icon {
  margin-right: 5px;
  color: #909399;
}

/* Latest Notices */
.notice-list {
  max-height: 150px; /* Adjust as needed */
  overflow-y: auto;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.3s;
}
.notice-item:hover {
  background-color: #f5f7fa;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 10px;
  font-size: 14px;
}

.notice-meta {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.notice-meta .el-tag {
  margin-right: 8px;
}

.notice-time {
  font-size: 13px;
  color: #909399;
}

/* Upcoming Tasks */
.task-list {
  max-height: 150px; /* Adjust as needed */
  overflow-y: auto;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
}

.task-item:last-child {
  border-bottom: none;
}

.task-info {
  flex-grow: 1;
  margin-right: 15px;
  overflow: hidden;
}

.task-course {
  font-size: 12px;
  color: #909399;
  display: block;
  margin-bottom: 4px;
}

.task-name {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-deadline {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #E6A23C;
  flex-shrink: 0;
}

.task-deadline .el-icon {
  margin-right: 5px;
}
</style> 