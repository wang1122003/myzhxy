<template>
  <div class="schedule-container">
    <div class="page-header">
      <h2>我的课表</h2>
      <div class="filter-container">
        <el-select
            v-model="semester"
            placeholder="选择学期"
            style="margin-right: 10px;"
            @change="fetchSchedule"
        >
          <el-option
              v-for="item in semesters"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
        <el-radio-group v-model="viewType" style="margin-left: 20px;">
          <el-radio-button label="week">周视图</el-radio-button>
          <el-radio-button label="list">列表视图</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <el-card class="schedule-card">
      <!-- 周视图 -->
      <div v-if="viewType === 'week'" class="week-view">
        <div class="time-column">
          <div class="header-cell">时间</div>
          <div v-for="time in timeSlots" :key="time.slot" class="time-cell">
            {{ time.label }}
            <div class="time-range">{{ time.range }}</div>
          </div>
        </div>
        <div v-for="day in weekdays" :key="day.value" class="day-column">
          <div class="header-cell">{{ day.label }}</div>
          <div
              v-for="time in timeSlots"
              :key="`${day.value}-${time.slot}`"
              :class="{ 'has-course': hasCourse(day.value, time.slot) }"
              class="schedule-cell"
          >
            <div
                v-if="getCourse(day.value, time.slot)"
                :style="{ backgroundColor: getCourseColor(getCourse(day.value, time.slot).courseId) }"
                class="course-item"
                @click="showCourseDetail(getCourse(day.value, time.slot))"
            >
              <div class="course-name">{{ getCourse(day.value, time.slot).courseName }}</div>
              <div class="course-location">{{ getCourse(day.value, time.slot).classroom }}</div>
              <div class="course-class">{{ getCourse(day.value, time.slot).className || '' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="list-view">
        <el-table :data="scheduleList" border style="width: 100%">
          <el-table-column label="课程名称" min-width="150" prop="courseName"/>
          <el-table-column label="班级" prop="className" width="120"/>
          <el-table-column label="星期" prop="weekday" width="100">
            <template #default="scope">
              {{ getWeekdayName(scope.row.weekday) }}
            </template>
          </el-table-column>
          <el-table-column label="开始时间" prop="startTime" width="100"/>
          <el-table-column label="结束时间" prop="endTime" width="100"/>
          <el-table-column label="教室" prop="classroom" width="120"/>
          <el-table-column fixed="right" label="操作" width="120">
            <template #default="scope">
              <el-button
                  size="small"
                  type="primary"
                  @click="showCourseDetail(scope.row)"
              >详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 课程详情对话框 -->
    <el-dialog
        v-model="dialogVisible"
        title="课程详情"
        width="500px"
    >
      <template v-if="currentCourse">
        <el-descriptions :column="1" border title="">
          <el-descriptions-item label="课程名称">{{ currentCourse.courseName }}</el-descriptions-item>
          <el-descriptions-item label="课程代码">{{ currentCourse.courseCode }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentCourse.className }}</el-descriptions-item>
          <el-descriptions-item label="学生人数">{{ currentCourse.studentCount || 0 }}人</el-descriptions-item>
          <el-descriptions-item label="上课时间">
            {{ getWeekdayName(currentCourse.weekday) }} {{ currentCourse.startTime }} - {{ currentCourse.endTime }}
          </el-descriptions-item>
          <el-descriptions-item label="上课地点">{{ currentCourse.classroom }}</el-descriptions-item>
          <el-descriptions-item label="学分">{{ currentCourse.credit }}</el-descriptions-item>
          <el-descriptions-item label="学期">{{ currentCourse.semester }}</el-descriptions-item>
          <el-descriptions-item v-if="currentCourse.description" label="课程描述">
            {{ currentCourse.description }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button
              type="primary"
              @click="goToCourseManage(currentCourse)"
          >管理课程</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getTeacherSchedule} from '@/api/schedule'

export default {
  name: 'TeacherScheduleView',
  setup() {
    const router = useRouter()
    const scheduleList = ref([])
    const viewType = ref('week')
    const semester = ref('2023-2024-1')
    const dialogVisible = ref(false)
    const currentCourse = ref(null)

    const semesters = [
      {label: '2023-2024学年第一学期', value: '2023-2024-1'},
      {label: '2023-2024学年第二学期', value: '2023-2024-2'},
      {label: '2024-2025学年第一学期', value: '2024-2025-1'}
    ]

    const weekdays = [
      {label: '星期一', value: 1},
      {label: '星期二', value: 2},
      {label: '星期三', value: 3},
      {label: '星期四', value: 4},
      {label: '星期五', value: 5},
      {label: '星期六', value: 6},
      {label: '星期日', value: 7}
    ]

    const timeSlots = [
      {slot: 1, label: '第一节', range: '08:00-08:45'},
      {slot: 2, label: '第二节', range: '08:55-09:40'},
      {slot: 3, label: '第三节', range: '10:00-10:45'},
      {slot: 4, label: '第四节', range: '10:55-11:40'},
      {slot: 5, label: '第五节', range: '13:30-14:15'},
      {slot: 6, label: '第六节', range: '14:25-15:10'},
      {slot: 7, label: '第七节', range: '15:30-16:15'},
      {slot: 8, label: '第八节', range: '16:25-17:10'},
      {slot: 9, label: '第九节', range: '18:30-19:15'},
      {slot: 10, label: '第十节', range: '19:25-20:10'}
    ]

    // 课程颜色映射
    const courseColors = {}
    const colorPalette = [
      '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
      '#3ECBBC', '#EA7C69', '#AA68CA', '#68C0CA', '#CA6897'
    ]

    // 获取教师课表
    const fetchSchedule = async () => {
      try {
        const response = await getTeacherSchedule({semester: semester.value})
        scheduleList.value = response.data || []

        // 为课程分配颜色
        scheduleList.value.forEach(schedule => {
          if (!courseColors[schedule.courseId]) {
            const colorIndex = Object.keys(courseColors).length % colorPalette.length
            courseColors[schedule.courseId] = colorPalette[colorIndex]
          }
        })
      } catch (error) {
        console.error('获取课表失败', error)
        ElMessage.error('获取课表失败')
      }
    }

    // 判断指定时间和日期是否有课程
    const hasCourse = (day, timeSlot) => {
      return scheduleList.value.some(schedule => {
        return schedule.weekday === day && getTimeSlot(schedule.startTime) === timeSlot
      })
    }

    // 获取指定时间和日期的课程
    const getCourse = (day, timeSlot) => {
      return scheduleList.value.find(schedule => {
        return schedule.weekday === day && getTimeSlot(schedule.startTime) === timeSlot
      })
    }

    // 根据时间获取对应的时间槽
    const getTimeSlot = (time) => {
      // 根据时间（如 "08:00"）获取对应的时间槽
      const timeSlotMap = {
        '08:00': 1,
        '08:55': 2,
        '10:00': 3,
        '10:55': 4,
        '13:30': 5,
        '14:25': 6,
        '15:30': 7,
        '16:25': 8,
        '18:30': 9,
        '19:25': 10
      }
      return timeSlotMap[time] || 0
    }

    // 获取课程颜色
    const getCourseColor = (courseId) => {
      return courseColors[courseId] || '#409EFF'
    }

    // 获取星期名称
    const getWeekdayName = (weekday) => {
      const weekdayMap = {
        1: '星期一',
        2: '星期二',
        3: '星期三',
        4: '星期四',
        5: '星期五',
        6: '星期六',
        7: '星期日'
      }
      return weekdayMap[weekday] || '未知'
    }

    // 显示课程详情
    const showCourseDetail = (course) => {
      currentCourse.value = course
      dialogVisible.value = true
    }

    // 跳转到课程管理页面
    const goToCourseManage = (course) => {
      if (course && course.courseId) {
        dialogVisible.value = false
        router.push({
          path: '/teacher/courses',
          query: {courseId: course.courseId}
        })
      }
    }

    // 页面初始化时加载数据
    onMounted(() => {
      fetchSchedule()
    })

    return {
      scheduleList,
      viewType,
      semester,
      dialogVisible,
      currentCourse,
      semesters,
      weekdays,
      timeSlots,
      courseColors,
      colorPalette,
      fetchSchedule,
      hasCourse,
      getCourse,
      getTimeSlot,
      getCourseColor,
      getWeekdayName,
      showCourseDetail,
      goToCourseManage
    }
  }
}
</script>

<style scoped>
.schedule-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  align-items: center;
}

.schedule-card {
  margin-bottom: 20px;
}

.week-view {
  display: flex;
  min-height: 700px;
}

.time-column, .day-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #EBEEF5;
}

.header-cell {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  background-color: #F5F7FA;
  border-bottom: 1px solid #EBEEF5;
}

.time-cell {
  height: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #EBEEF5;
  font-size: 0.9em;
}

.time-range {
  font-size: 0.8em;
  color: #909399;
  margin-top: 5px;
}

.schedule-cell {
  height: 100px;
  border-bottom: 1px solid #EBEEF5;
  position: relative;
}

.course-item {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 5px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.course-item:hover {
  transform: scale(1.05);
  z-index: 10;
}

.course-name {
  font-weight: bold;
  font-size: 0.9em;
  margin-bottom: 5px;
  text-align: center;
}

.course-location, .course-class {
  font-size: 0.8em;
  margin-bottom: 2px;
}

.list-view {
  width: 100%;
}
</style> 