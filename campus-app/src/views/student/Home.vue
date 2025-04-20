<template>
  <div class="student-dashboard">
    <h2 class="dashboard-title">学生工作台</h2>

    <el-row :gutter="20">
      <!-- 左侧区域 -->
      <el-col :md="8" :sm="24" :xs="24">
        <!-- 学生个人信息 -->
        <el-card class="dashboard-card user-card" shadow="hover">
          <div class="user-info">
            <el-avatar :size="80" :src="userAvatar" class="user-avatar">
              {{ userInfo.name ? userInfo.name.substring(0, 1) : 'U' }}
            </el-avatar>
            <div class="user-details">
              <h3>{{ userInfo.name || '同学' }}</h3>
              <p>{{ userInfo.username || '学号未知' }}</p>
              <p>{{ userInfo.department || '院系未知' }} {{ userInfo.major || '' }}</p>
              <el-button size="small" type="primary" @click="goToProfile">个人资料</el-button>
            </div>
          </div>
        </el-card>

        <!-- 学习数据统计 -->
        <el-card class="dashboard-card stats-card" shadow="hover">
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

      <!-- 右侧区域 -->
      <el-col :md="16" :sm="24" :xs="24">
        <!-- 今日课程 -->
        <el-card class="dashboard-card today-courses-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日课程</span>
              <el-button text type="primary" @click="goToSchedule">查看课表</el-button>
            </div>
          </template>
          <div v-if="loadingCourses" class="loading-placeholder">
            <el-skeleton :rows="3" animated/>
          </div>
          <div v-else-if="todayCourses.length > 0" class="course-list">
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
                <el-tag :type="getCourseStatus(course).type" size="small">{{ getCourseStatus(course).label }}</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="今天没有课程"/>
        </el-card>

        <!-- 最新通知公告 -->
        <el-card class="dashboard-card notice-card" shadow="hover">
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
              <div class="notice-title">
                <el-tag v-if="notice.type" :type="getNoticeTypeTag(notice.type)" class="notice-tag" size="small">
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
        <el-card class="dashboard-card activity-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>校园活动</span>
              <el-button text type="primary" @click="goToActivities">更多活动</el-button>
            </div>
          </template>
          <div v-if="loadingActivities" class="loading-placeholder">
            <el-skeleton :rows="2" animated/>
          </div>
          <div v-else-if="activities.length > 0" class="activity-list">
            <div v-for="activity in activities" :key="activity.id" class="activity-item"
                 @click="viewActivityDetail(activity)">
              <img :src="activity.coverImage || 'https://via.placeholder.com/80x80?text=Activity'"
                   class="activity-image">
              <div class="activity-info">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-meta">
                  <el-icon>
                    <Calendar/>
                  </el-icon>
                  {{ formatDate(activity.startTime) }}
                </div>
                <div class="activity-meta">
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

    <!-- 公告详情对话框 -->
    <NoticeDetailDialog
        v-model="noticeDialogVisible"
        :notice-id="currentNoticeId"
    />

    <!-- 活动详情对话框 (如果需要的话) -->
    <!-- <ActivityDetailDialog v-model="activityDialogVisible" :activity-id="currentActivityId" /> -->

  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Calendar, Collection, Location, Reading, Star} from '@element-plus/icons-vue'
import {getStudentSchedule} from '@/api/schedule' // 假设有获取学生课表API
import {getNoticeList} from '@/api/notice' // 修正导入
import {getActivityList} from '@/api/activity' // 假设获取活动列表API
import NoticeDetailDialog from '@/components/NoticeDetailDialog.vue' // 引入通知详情组件
import {userAvatar, userId, userRealName} from '@/utils/auth'

const router = useRouter()

// --- State ---
// 直接从 auth.js 获取 userInfo 和 avatar
const userInfo = computed(() => ({
  name: userRealName.value,
  username: userId.value,
  department: '',
  major: ''
}))

const stats = reactive({
  courseCount: 0,
  totalCredit: 0,
  gpa: '0.00'
})
const todayCourses = ref([])
const notices = ref([])
const activities = ref([])
const loadingCourses = ref(false)
const loadingNotices = ref(false)
const loadingActivities = ref(false)

const noticeDialogVisible = ref(false)
const currentNoticeId = ref(null)
// const activityDialogVisible = ref(false) // 如果需要活动详情弹窗
// const currentActivityId = ref(null)

// --- Computed ---
// （如果需要的话）

// --- Methods ---
const fetchDashboardData = async () => {
  loadingCourses.value = true
  loadingNotices.value = true
  loadingActivities.value = true

  try {
    // 并发获取数据
    const [scheduleRes, noticeRes, activityRes] = await Promise.all([
      getStudentSchedule({ /* params if needed, e.g., current week */}),
      getNoticeList({page: 1, size: 5}), // 获取最新的5条通知
      getActivityList({page: 1, size: 3, status: 1}) // 获取最新的3条报名中活动
    ])

    // 处理课表数据
    // 这里需要根据 getStudentSchedule 返回的数据结构来过滤出今天的课程
    // 以下是示例逻辑，需要根据实际API调整
    const allCourses = scheduleRes.data?.courses || []
    const today = new Date().getDay() // 0 for Sunday, 1 for Monday, etc.
    todayCourses.value = allCourses.filter(course => course.dayOfWeek === today)
        .sort((a, b) => a.startTime.localeCompare(b.startTime))

    // 处理通知数据
    notices.value = noticeRes.data?.list || []

    // 处理活动数据
    activities.value = activityRes.data?.list || []

    // Mock 学习统计数据 (后续需要从后端获取)
    stats.courseCount = 15
    stats.totalCredit = 45
    stats.gpa = '3.85'

  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error('加载仪表盘数据失败')
  } finally {
    loadingCourses.value = false
    loadingNotices.value = false
    loadingActivities.value = false
  }
}

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

// 查看通知详情
const viewNoticeDetail = (notice) => {
  currentNoticeId.value = notice.id
  noticeDialogVisible.value = true
}

// 查看活动详情
const viewActivityDetail = (activity) => {
  // currentActivityId.value = activity.id;
  // activityDialogVisible.value = true;
  // 暂时跳转到活动列表页
  router.push('/student/activities')
  console.log('查看活动详情:', activity.id)
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const options = {year: 'numeric', month: 'short', day: 'numeric'};
  return new Date(dateString).toLocaleDateString('zh-CN', options);
}

// 获取课程状态
const getCourseStatus = (course) => {
  // 实现判断课程状态（未开始、进行中、已结束）的逻辑
  // ... (需要根据 startTime, endTime 和当前时间判断)
  // 示例:
  const now = new Date();
  const start = new Date(now.toDateString() + ' ' + course.startTime); // 假设startTime是 HH:mm 格式
  const end = new Date(now.toDateString() + ' ' + course.endTime);
  if (now < start) return {label: '未开始', type: 'info'};
  if (now >= start && now <= end) return {label: '进行中', type: 'success'};
  return {label: '已结束', type: 'warning'};
}

// 获取通知类型名称和标签类型 (需要根据实际API调整)
const noticeTypeMap = {
  1: {name: '系统通知', tag: 'danger'},
  2: {name: '院系通知', tag: 'warning'},
  3: {name: '课程通知', tag: 'primary'},
  4: {name: '活动通知', tag: 'success'},
  default: {name: '其他', tag: 'info'}
};
const getNoticeTypeName = (type) => (noticeTypeMap[type] || noticeTypeMap.default).name;
const getNoticeTypeTag = (type) => (noticeTypeMap[type] || noticeTypeMap.default).tag;


// --- Lifecycle Hooks ---
onMounted(() => {
  fetchDashboardData() // 获取看板数据
  // 添加检查，如果 auth.js 中没有用户信息，则强制登出
  if (!userId.value) {
    console.warn("学生ID不存在，强制登出");
    // 可能需要调用登出逻辑
    // 可以考虑提示用户或执行登出
  }
})

</script>

<style scoped>
.student-dashboard {
  padding: 20px;
}

.dashboard-title {
  margin-bottom: 24px;
  color: #303133;
  font-weight: 500;
}

.dashboard-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

/* 用户信息卡片 */
.user-card .user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  margin-right: 20px;
  flex-shrink: 0;
}

.user-details h3 {
  margin-top: 0;
  margin-bottom: 8px;
}

.user-details p {
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}

.user-details .el-button {
  margin-top: 10px;
}

/* 学习统计卡片 */
.stats-list {
  display: flex;
  flex-direction: column;
}
.stats-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.stats-item:last-child {
  margin-bottom: 0;
}
.stats-icon {
  width: 45px;
  height: 45px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 22px;
  color: #fff;
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

/* 今日课程 */
.course-list {
  max-height: 250px; /* 限制高度并允许滚动 */
  overflow-y: auto;
}
.course-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
}
.course-item:last-child {
  border-bottom: none;
}
.course-time {
  width: 120px;
  text-align: center;
  font-size: 14px;
  color: #606266;
  margin-right: 15px;
  flex-shrink: 0;
}
.course-info {
  flex-grow: 1;
}
.course-name {
  font-weight: 500;
  margin-bottom: 5px;
}
.course-location {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
}
.course-location .el-icon {
  margin-right: 4px;
}
.course-status {
  margin-left: 15px;
  flex-shrink: 0;
}

/* 最新通知 */
.notice-list {
  max-height: 200px; /* 限制高度并允许滚动 */
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
.notice-tag {
  margin-right: 8px;
  flex-shrink: 0;
}
.notice-date {
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

/* 近期活动 */
.activity-list {
  max-height: 280px;
  overflow-y: auto;
}
.activity-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.3s;
}
.activity-item:hover {
  background-color: #f5f7fa;
}

.activity-item:last-child {
  border-bottom: none;
}
.activity-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
  flex-shrink: 0;
}
.activity-info {
  flex-grow: 1;
  overflow: hidden;
}
.activity-title {
  font-weight: 500;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-meta {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  margin-bottom: 3px;
}

.activity-meta .el-icon {
  margin-right: 5px;
}
.activity-status {
  margin-left: 15px;
  flex-shrink: 0;
}

.loading-placeholder {
  padding: 20px;
}
</style> 