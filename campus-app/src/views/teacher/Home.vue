<template>
  <div class="home-container">
    <div class="home-header">
      <h2>教师工作台</h2>
      <p class="welcome-text">欢迎回来，{{ userInfo.name || '老师' }}！今天是 {{ currentDate }}</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalCourses }}</div>
            <div class="stat-label">授课课程数</div>
          </div>
          <div class="stat-icon">
            <el-icon>
              <Reading/>
            </el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalStudents }}</div>
            <div class="stat-label">学生总数</div>
          </div>
          <div class="stat-icon">
            <el-icon>
              <User/>
            </el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.teachingHours }}</div>
            <div class="stat-label">本周课时</div>
          </div>
          <div class="stat-icon">
            <el-icon>
              <Clock/>
            </el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingGrades }}</div>
            <div class="stat-label">待登记成绩</div>
          </div>
          <div class="stat-icon">
            <el-icon>
              <List/>
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-row">
      <!-- 今日课程 -->
      <el-col :md="12" :xs="24">
        <el-card class="today-courses">
          <template #header>
            <div class="card-header">
              <span>今日课程</span>
              <el-button text type="primary" @click="goToSchedule">查看完整课表</el-button>
            </div>
          </template>
          <el-empty v-if="todayCourses.length === 0" description="今日没有课程安排"/>
          <div v-else class="course-list">
            <div
                v-for="course in todayCourses"
                :key="course.id"
                class="course-item"
                @click="viewCourseDetail(course)"
            >
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
                <div class="course-class">
                  <el-icon>
                    <User/>
                  </el-icon>
                  <span>{{ course.className || '未知班级' }} ({{ course.studentCount || 0 }}人)</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 最新通知 -->
      <el-col :md="12" :xs="24">
        <el-card class="latest-notices">
          <template #header>
            <div class="card-header">
              <span>最新通知</span>
              <el-button text type="primary" @click="goToNotices">查看全部</el-button>
            </div>
          </template>
          <el-empty v-if="notices.length === 0" description="暂无通知"/>
          <div v-else class="notice-list">
            <div
                v-for="notice in notices"
                :key="notice.id"
                class="notice-item"
                @click="viewNoticeDetail(notice)"
            >
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-meta">
                <el-tag :type="getNoticeTagType(notice.type)" size="small">
                  {{ getNoticeName(notice.type) }}
                </el-tag>
                <span class="notice-time">{{ notice.publishTime }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-row">
      <!-- 近期截止任务 -->
      <el-col :md="12" :xs="24">
        <el-card class="upcoming-tasks">
          <template #header>
            <div class="card-header">
              <span>近期截止任务</span>
              <el-button text type="primary" @click="goToCourses">管理课程</el-button>
            </div>
          </template>
          <el-empty v-if="upcomingTasks.length === 0" description="暂无截止任务"/>
          <div v-else class="task-list">
            <div
                v-for="task in upcomingTasks"
                :key="task.id"
                class="task-item"
            >
              <div class="task-info">
                <div class="task-title">{{ task.title }}</div>
                <div class="task-course">{{ task.courseName }}</div>
              </div>
              <div class="task-deadline">
                <div class="deadline-label">截止日期</div>
                <div class="deadline-value">{{ task.deadline }}</div>
                <div :class="getTaskStatus(task).class" class="task-status">
                  {{ getTaskStatus(task).text }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 待批改作业 -->
      <el-col :md="12" :xs="24">
        <el-card class="pending-assignments">
          <template #header>
            <div class="card-header">
              <span>待批改作业</span>
              <el-button text type="primary" @click="goToCourses">查看全部</el-button>
            </div>
          </template>
          <el-empty v-if="pendingAssignments.length === 0" description="暂无待批改作业"/>
          <div v-else class="assignment-list">
            <div
                v-for="assignment in pendingAssignments"
                :key="assignment.id"
                class="assignment-item"
                @click="goToGradeAssignment(assignment)"
            >
              <div class="assignment-info">
                <div class="assignment-title">{{ assignment.title }}</div>
                <div class="assignment-course">{{ assignment.courseName }}</div>
              </div>
              <div class="assignment-stats">
                <el-progress
                    :format="() => `${assignment.gradedCount}/${assignment.totalCount}`"
                    :percentage="getPercentage(assignment.gradedCount, assignment.totalCount)"
                    :status="getPercentage(assignment.gradedCount, assignment.totalCount) === 100 ? 'success' : ''"
                ></el-progress>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 通知详情对话框 -->
    <el-dialog
        v-model="noticeDialogVisible"
        :title="currentNotice?.title || '通知详情'"
        width="500px"
    >
      <template v-if="currentNotice">
        <div class="notice-dialog-meta">
          <span>发布人: {{ currentNotice.publisher }}</span>
          <span>发布时间: {{ currentNotice.publishTime }}</span>
        </div>
        <div class="notice-dialog-content" v-html="currentNotice.content"></div>
        <div v-if="currentNotice.attachments && currentNotice.attachments.length > 0" class="notice-dialog-attachments">
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
      </template>
    </el-dialog>

    <!-- 课程详情对话框 -->
    <el-dialog
        v-model="courseDialogVisible"
        :title="currentCourse?.courseName || '课程详情'"
        width="500px"
    >
      <template v-if="currentCourse">
        <el-descriptions :column="1" border title="">
          <el-descriptions-item label="课程名称">{{ currentCourse.courseName }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">{{ currentCourse.startTime }} - {{
              currentCourse.endTime
            }}
          </el-descriptions-item>
          <el-descriptions-item label="上课地点">{{ currentCourse.classroom }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentCourse.className }}</el-descriptions-item>
          <el-descriptions-item label="学生人数">{{ currentCourse.studentCount || 0 }}人</el-descriptions-item>
          <el-descriptions-item v-if="currentCourse.description" label="备注">{{
              currentCourse.description
            }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="courseDialogVisible = false">关闭</el-button>
          <el-button
              type="primary"
              @click="goToCourseManage(currentCourse)"
          >管理课程</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getUserInfo} from '@/api/user'
import {getTeacherSchedule} from '@/api/schedule'
import {getNoticeById, getNoticeList} from '@/api/notice'
import {getTeacherCourses} from '@/api/course'
import {downloadFile} from '@/api/file'
import {Clock, Download, List, Location, Reading, User} from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref({})
const stats = ref({
  totalCourses: 0,
  totalStudents: 0,
  teachingHours: 0,
  pendingGrades: 0
})
const todayCourses = ref([])
const notices = ref([])
const upcomingTasks = ref([])
const pendingAssignments = ref([])
const noticeDialogVisible = ref(false)
const courseDialogVisible = ref(false)
const currentNotice = ref(null)
const currentCourse = ref(null)

// 当前日期格式化
const currentDate = computed(() => {
  const now = new Date()
  const dayNames = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${dayNames[now.getDay()]}`
})

// 今天是星期几（1-7，周一到周日）
const today = computed(() => {
  const day = new Date().getDay()
  return day === 0 ? 7 : day
})

// 当前时间
const currentTime = computed(() => {
  const now = new Date()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
})

// 获取课程状态
const getCourseStatus = (course) => {
  if (!course.startTime || !course.endTime) {
    return {text: '未知', class: 'status-unknown'}
  }

  const courseStartTime = course.startTime
  const courseEndTime = course.endTime
  const now = currentTime.value

  if (now < courseStartTime) {
    return {text: '未开始', class: 'status-upcoming'}
  } else if (now >= courseStartTime && now <= courseEndTime) {
    return {text: '进行中', class: 'status-ongoing'}
  } else {
    return {text: '已结束', class: 'status-finished'}
  }
}

// 获取任务状态
const getTaskStatus = (task) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const deadlineDate = new Date(task.deadline)
  deadlineDate.setHours(0, 0, 0, 0)

  const diffDays = Math.ceil((deadlineDate - today) / (1000 * 60 * 60 * 24))

  if (diffDays < 0) {
    return {text: '已截止', class: 'status-expired'}
  } else if (diffDays === 0) {
    return {text: '今日截止', class: 'status-today'}
  } else if (diffDays === 1) {
    return {text: '明日截止', class: 'status-tomorrow'}
  } else if (diffDays <= 3) {
    return {text: `${diffDays}天后截止`, class: 'status-upcoming'}
  } else {
    return {text: `${diffDays}天后截止`, class: 'status-future'}
  }
}

// 获取完成百分比
const getPercentage = (graded, total) => {
  if (!total || total === 0) return 0
  return Math.round((graded / total) * 100)
}

// 获取通知标签类型
const getNoticeTagType = (type) => {
  const typeMap = {
    1: 'success', // 普通通知
    2: 'warning', // 重要通知
    3: 'danger',  // 紧急通知
    4: 'info',    // 活动通知
    5: ''         // 其他通知
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
  return nameMap[type] || '通知'
}

// 初始化数据
const initData = async () => {
  try {
    await fetchUserInfo()
    await fetchStats()
    await fetchTodayCourses()
    await fetchNotices()
    await fetchTasks()
    await fetchPendingAssignments()
  } catch (error) {
    console.error('初始化数据失败', error)
  }
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = await getUserInfo()
    userInfo.value = response.data || {}
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    // 在实际应用中，可能需要调用专门的统计接口
    // 这里简化为从课程数据中计算统计信息
    const response = await getTeacherCourses()
    const courses = response.data || []

    let totalStudents = 0
    let pendingGrades = 0

    courses.forEach(course => {
      totalStudents += course.studentCount || 0
      pendingGrades += course.pendingGradeCount || 0
    })

    stats.value = {
      totalCourses: courses.length,
      totalStudents,
      teachingHours: calculateWeeklyTeachingHours(courses),
      pendingGrades
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 计算每周教学时长
const calculateWeeklyTeachingHours = (courses) => {
  // 简化计算，假设每节课为45分钟
  return courses.reduce((total, course) => {
    const courseHours = course.weeklyHours || 0
    return total + courseHours
  }, 0)
}

// 获取今日课程
const fetchTodayCourses = async () => {
  try {
    const response = await getTeacherSchedule({weekday: today.value})
    todayCourses.value = response.data || []

    // 按时间排序
    todayCourses.value.sort((a, b) => {
      return a.startTime.localeCompare(b.startTime)
    })
  } catch (error) {
    console.error('获取今日课程失败', error)
  }
}

// 获取最新通知
const fetchNotices = async () => {
  try {
    // 只获取最近5条通知
    const response = await getNoticeList({pageSize: 5, pageNum: 1})
    notices.value = response.data?.list || []
  } catch (error) {
    console.error('获取通知失败', error)
  }
}

// 获取近期任务
const fetchTasks = async () => {
  try {
    // 在实际应用中，需要调用相关API获取近期任务
    // 这里用模拟数据替代
    upcomingTasks.value = [
      {
        id: 1,
        title: '期中考试',
        courseName: '高等数学',
        deadline: '2023-11-15'
      },
      {
        id: 2,
        title: '课程论文提交',
        courseName: '数据结构',
        deadline: '2023-11-20'
      },
      {
        id: 3,
        title: '实验报告',
        courseName: '计算机网络',
        deadline: '2023-11-25'
      }
    ]
  } catch (error) {
    console.error('获取近期任务失败', error)
  }
}

// 获取待批改作业
const fetchPendingAssignments = async () => {
  try {
    // 在实际应用中，需要调用相关API获取待批改作业
    // 这里用模拟数据替代
    pendingAssignments.value = [
      {
        id: 1,
        title: '第三次作业',
        courseName: '高等数学',
        gradedCount: 25,
        totalCount: 40
      },
      {
        id: 2,
        title: '实验一',
        courseName: '数据结构',
        gradedCount: 18,
        totalCount: 35
      },
      {
        id: 3,
        title: '课程设计',
        courseName: '计算机网络',
        gradedCount: 5,
        totalCount: 30
      }
    ]
  } catch (error) {
    console.error('获取待批改作业失败', error)
  }
}

// 查看通知详情
const viewNoticeDetail = async (notice) => {
  try {
    if (notice.id) {
      const response = await getNoticeById(notice.id)
      currentNotice.value = response.data
      noticeDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取通知详情失败', error)
    ElMessage.error('获取通知详情失败')
  }
}

// 查看课程详情
const viewCourseDetail = (course) => {
  currentCourse.value = course
  courseDialogVisible.value = true
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

// 导航到课表页面
const goToSchedule = () => {
  router.push('/teacher/schedule')
}

// 导航到通知页面
const goToNotices = () => {
  router.push('/teacher/notices')
}

// 导航到课程页面
const goToCourses = () => {
  router.push('/teacher/courses')
}

// 导航到课程管理页面
const goToCourseManage = (course) => {
  if (course && course.courseId) {
    courseDialogVisible.value = false
    router.push({
      path: '/teacher/courses',
      query: {courseId: course.courseId}
    })
  }
}

// 导航到作业批改页面
const goToGradeAssignment = (assignment) => {
  if (assignment && assignment.id) {
    router.push({
      path: '/teacher/grade',
      query: {assignmentId: assignment.id}
    })
  }
}

// 页面挂载时加载数据
onMounted(() => {
  initData()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.home-header {
  margin-bottom: 20px;
}

.welcome-text {
  color: #606266;
  font-size: 14px;
  margin-top: 5px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
  margin-bottom: 20px;
}

.stat-content {
  flex: 1;
  z-index: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.stat-label {
  color: #909399;
}

.stat-icon {
  position: absolute;
  right: 20px;
  font-size: 48px;
  color: rgba(64, 158, 255, 0.1);
}

.content-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.today-courses, .latest-notices, .upcoming-tasks, .pending-assignments {
  height: 350px;
  margin-bottom: 20px;
}

.course-list, .notice-list, .task-list, .assignment-list {
  height: 280px;
  overflow-y: auto;
}

.course-item {
  display: flex;
  padding: 12px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.course-item:hover {
  background-color: #F5F7FA;
}

.course-time {
  width: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding-right: 15px;
  border-right: 1px solid #EBEEF5;
}

.time-range {
  font-weight: bold;
  margin-bottom: 5px;
}

.time-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.status-upcoming {
  background-color: #E6A23C;
  color: white;
}

.status-ongoing {
  background-color: #67C23A;
  color: white;
}

.status-finished {
  background-color: #909399;
  color: white;
}

.status-unknown {
  background-color: #F56C6C;
  color: white;
}

.course-info {
  flex: 1;
  padding-left: 15px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.course-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.course-location, .course-class {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  margin-bottom: 3px;
}

.course-location i, .course-class i {
  margin-right: 5px;
}

.notice-item {
  padding: 12px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notice-item:hover {
  background-color: #F5F7FA;
}

.notice-title {
  font-weight: bold;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.task-item {
  display: flex;
  padding: 12px;
  border-bottom: 1px solid #EBEEF5;
  transition: background-color 0.3s;
}

.task-info {
  flex: 1;
}

.task-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.task-course {
  font-size: 13px;
  color: #606266;
}

.task-deadline {
  text-align: right;
  min-width: 100px;
}

.deadline-label {
  font-size: 12px;
  color: #909399;
}

.deadline-value {
  font-weight: bold;
  margin: 3px 0;
}

.task-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-block;
}

.status-expired {
  background-color: #F56C6C;
  color: white;
}

.status-today {
  background-color: #E6A23C;
  color: white;
}

.status-tomorrow {
  background-color: #E6A23C;
  color: white;
}

.status-upcoming {
  background-color: #67C23A;
  color: white;
}

.status-future {
  background-color: #909399;
  color: white;
}

.assignment-item {
  padding: 12px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.assignment-item:hover {
  background-color: #F5F7FA;
}

.assignment-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.assignment-course {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.notice-dialog-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 14px;
  color: #606266;
}

.notice-dialog-content {
  margin-bottom: 20px;
  line-height: 1.6;
}

.attachments-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.attachment-item {
  margin-bottom: 5px;
}
</style> 