<template>
  <PageContainer title="我的课表">
    <template #actions>
        <el-select
            v-model="semester"
            placeholder="选择学期"
            style="margin-right: 10px; width: 200px;"
            :loading="loadingSemesters"
            filterable
            @change="fetchSchedule"
        >
          <el-option
              v-for="item in semestersRef"
              :key="item.id"
              :label="item.termName"
              :value="item.id" 
          />
        </el-select>
      <el-radio-group v-model="viewType" style="margin-left: 10px;">
        <el-radio-button label="week">周视图</el-radio-button>
        <el-radio-button label="list">列表视图</el-radio-button>
        </el-radio-group>
      <el-button :icon="Refresh" :loading="loadingSchedule" circle style="margin-left: 10px;" title="刷新课表"
                 @click="fetchSchedule"></el-button>
    </template>

    <el-card v-loading="loadingSchedule || loadingTimeSlots || loadingSemesters" class="schedule-card" shadow="never">
      <!-- 周视图 -->
      <div v-if="viewType === 'week'" class="week-view-wrapper">
        <div v-if="!scheduleList || scheduleList.length === 0">
          <el-empty description="当前学期暂无课表数据"/>
        </div>
        <div v-else class="week-view">
          <!-- 时间列 -->
          <div class="time-column">
            <div class="header-cell">时间</div>
            <div v-for="time in timeSlotsRef" :key="time.slot" class="time-cell">
              <div class="time-slot-label">第{{ time.slot }}节</div>
              <div class="time-range">{{ time.startTime }} - {{ time.endTime }}</div>
            </div>
          </div>
          <!-- 星期列 -->
          <div v-for="day in weekdaysRef" :key="day.value" class="day-column">
            <div class="header-cell">{{ day.label }}</div>
            <div
                v-for="time in timeSlotsRef" :key="`${day.value}-${time.slot}`"
              class="schedule-cell"
                :style="getGridCellStyle(day.value, time.slot)"
                @click="cellClickHandler(day.value, time.slot)"
            >
              <div v-if="findCourseForCell(day.value, time.slot)" class="course-item">
                <div class="course-name">{{ findCourseForCell(day.value, time.slot).courseName }}</div>
                <div class="course-location">@ {{ findCourseForCell(day.value, time.slot).classroomName }}</div>
                <div class="course-class">班级: {{ findCourseForCell(day.value, time.slot).className || '-' }}</div>
                <div class="course-weeks">周次: {{
                    findCourseForCell(day.value, time.slot).startWeek
                  }}-{{ findCourseForCell(day.value, time.slot).endWeek }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="list-view">
        <TableView
            :action-column-config="actionColumnConfig"
            :data="scheduleList"
            :columns="tableColumns"
            :loading="loadingSchedule"
            :show-pagination="false"
            @refresh="fetchSchedule"
            @view-detail="showCourseDetail"
        />
      </div>
    </el-card>

    <!-- 课程详情对话框 -->
    <el-dialog v-model="dialogVisible" title="课程详情" width="500px">
      <template v-if="currentCourse">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="课程名称">{{ currentCourse.courseName }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">{{ formatWeekday(currentCourse.weekday) }}
            {{ currentCourse.startTime?.substring(0, 5) }} - {{ currentCourse.endTime?.substring(0, 5) }}
          </el-descriptions-item>
          <el-descriptions-item label="上课周次">第 {{ currentCourse.startWeek }} - {{ currentCourse.endWeek }} 周
          </el-descriptions-item>
          <el-descriptions-item label="上课地点">{{ currentCourse.classroomName }}</el-descriptions-item>
          <el-descriptions-item label="授课班级">{{ currentCourse.className || '-' }}</el-descriptions-item>
          <!-- <el-descriptions-item label="学分">{{ currentCourse.credit }}</el-descriptions-item> -->
        </el-descriptions>
      </template>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {
  ElButton, ElCard, ElDescriptions, ElDescriptionsItem, ElDialog, ElMessage,
  ElOption, ElRadioButton, ElRadioGroup, ElSelect, ElIcon, ElEmpty
} from 'element-plus';
import {Refresh} from '@element-plus/icons-vue';
import {getTeacherWeeklySchedule} from '@/api/schedule'; // Corrected: Use schedule.js for weekly schedule
import {getTimeSlots, getWeekdays} from '@/api/common'; // Corrected: Use common.js
import {getAllTerms} from '@/api/term'; // Corrected function name

// --- State ---
const loadingSchedule = ref(false);
const loadingSemesters = ref(false);
const loadingTimeSlots = ref(false);
// const loadingWeekdays = ref(false); // Weekdays are usually static

const scheduleList = ref([]);
const viewType = ref('week');
const semester = ref(null); // Selected semester ID
const dialogVisible = ref(false);
const currentCourse = ref(null);

const semestersRef = ref([]); // Semester options
const timeSlotsRef = ref([]); // Time slot definitions
const weekdaysRef = reactive([ // Static weekdays
  {value: 1, label: '星期一'}, {value: 2, label: '星期二'}, {value: 3, label: '星期三'},
  {value: 4, label: '星期四'}, {value: 5, label: '星期五'}, {value: 6, label: '星期六'},
  {value: 7, label: '星期日'},
]);

// --- Computed Properties for TableView ---
const tableColumns = computed(() => [
  {prop: 'courseName', label: '课程名称', minWidth: 180},
  {prop: 'className', label: '授课班级', minWidth: 150},
  {
    prop: 'weekday',
    label: '星期',
    width: 100,
    formatter: (row) => formatWeekday(row.weekday)
  },
  {
    prop: 'timeRange',
    label: '时间',
    width: 150,
    formatter: (row) => `${row.startTime?.substring(0, 5) || '?'} - ${row.endTime?.substring(0, 5) || '?'}`
  },
  {
    prop: 'weeksRange',
    label: '周次',
    width: 120,
    formatter: (row) => `第 ${row.startWeek}-${row.endWeek} 周`
  },
  {prop: 'classroomName', label: '教室', minWidth: 120}
]);

const actionColumnConfig = computed(() => ({
  width: 100,
  fixed: 'right',
  buttons: [
    {label: '详情', type: 'primary', size: 'small', event: 'view-detail'}
  ]
}));


// --- Helper Functions ---

const formatWeekday = (day) => {
  const found = weekdaysRef.find(d => d.value === day);
  return found ? found.label : `星期${day}`;
};

// Color generation for week view
const courseColors = {};
const colorPalette = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#fab1a0', '#ffeaa7', '#55efc4', '#74b9ff', '#a29bfe', '#fd79a8', '#00b894'];
let colorIndex = 0;

const getCourseColor = (courseId) => {
  if (!courseColors[courseId]) {
    courseColors[courseId] = colorPalette[colorIndex % colorPalette.length];
    colorIndex++;
  }
  return courseColors[courseId];
};

// Find course for a cell in the week view (simplified based on startTime match)
const findCourseForCell = (day, timeSlot) => {
  // Needs adjustment based on actual time slot definition / start/end sections
  const targetStartTime = timeSlotsRef.value.find(t => t.slot === timeSlot)?.startTime;
  if (!targetStartTime) return null;

  return scheduleList.value.find(course =>
      course.weekday === day &&
      course.startTime?.startsWith(targetStartTime.substring(0, 5)) // Match HH:mm
  );
  // TODO: Implement proper section mapping if backend provides startSection/endSection
};

// Get style for grid cell (row span and color)
const getGridCellStyle = (day, timeSlot) => {
  const course = findCourseForCell(day, timeSlot);
  if (!course) return {};

  // Placeholder: assume 1 slot per course for now
  const rowSpan = 1;
  // Example: const rowSpan = course.endSection - course.startSection + 1;

  // Apply style only to the starting cell
  const startSlot = timeSlotsRef.value.find(t => course.startTime?.startsWith(t.startTime.substring(0, 5)))?.slot;
  if (startSlot !== timeSlot) {
    // Hide cells covered by a multi-slot course starting earlier
    const courseAbove = findCourseForCell(day, timeSlot - 1);
    // Determine end slot based on duration/endTime (requires more robust logic)
    const courseAboveEndSlot = timeSlotsRef.value.find(t => courseAbove?.endTime?.startsWith(t.endTime.substring(0, 5)))?.slot;
    if (courseAbove && courseAboveEndSlot >= timeSlot) {
      return {display: 'none'}; // Hide cells covered by the span
    }
    return {}; // Not the start cell
  }

  return {
    // gridRowStart: `span ${rowSpan}`, // Use grid-row-start for spanning
    backgroundColor: getCourseColor(course.id), // Use course ID for color mapping
    color: '#fff',
    cursor: 'pointer'
  };
};

// Handle cell click in week view
const cellClickHandler = (day, timeSlot) => {
  const course = findCourseForCell(day, timeSlot);
  if (course) {
    showCourseDetail(course);
  }
};


// --- Data Fetching Methods ---

// Fetch available semesters
const fetchSemesters = async () => {
  loadingSemesters.value = true;
  try {
    const res = await getAllTerms(); // Corrected function name
    semestersRef.value = res.data || [];
    if (semestersRef.value.length > 0 && !semester.value) {
      const current = semestersRef.value.find(s => s.isCurrent);
      semester.value = current ? current.id : semestersRef.value[0].id;
        }
  } catch (error) {
    console.error("获取学期列表失败:", error);
    ElMessage.error('获取学期列表失败');
  } finally {
        loadingSemesters.value = false;
  }
};

// Fetch time slots (should ideally be static or fetched once)
const fetchTimeSlots = async () => {
  loadingTimeSlots.value = true;
  try {
    // Assume backend provides time slots like [{ slot: 1, startTime: '08:00', endTime: '08:45' }, ...]
    const res = await getTimeSlots(); // Corrected
    timeSlotsRef.value = res.data || [];
  } catch (error) {
    console.error("获取时间段失败:", error);
    // Fallback to default if API fails?
    // timeSlotsRef.value = [ { slot: 1, startTime: '08:00', endTime: '08:45' }, ... ];
  } finally {
        loadingTimeSlots.value = false;
  }
};

// Fetch teacher schedule for the selected semester
const fetchSchedule = async () => {
  if (!semester.value) return;
  loadingSchedule.value = true;
  colorIndex = 0; // Reset color index
  Object.keys(courseColors).forEach(key => delete courseColors[key]); // Clear color map

  try {
    const params = {
      termId: semester.value,
      teacherId: userStore.userId // Pass current teacher's ID
      // weekStartDate: 'YYYY-MM-DD' // 如果是周课表，可能还需要周开始日期
    };
    const res = await getTeacherWeeklySchedule(params); // Corrected
    scheduleList.value = res.data || [];
  } catch (error) {
    console.error("获取课表失败:", error);
    // Error handled by interceptor
        scheduleList.value = [];
  } finally {
        loadingSchedule.value = false;
  }
};

// Show course detail dialog
const showCourseDetail = (course) => {
  currentCourse.value = course;
  dialogVisible.value = true;
};

// --- Lifecycle Hooks ---
onMounted(async () => {
  loadingSchedule.value = true; // Show loading initially
  try {
    // Fetch semesters first
    await fetchSemesters();
    // Fetch schedule if semester is set
    if (semester.value) {
      await fetchSchedule();
    }
    // Fetch time slots and weekdays
    const [slotsRes, daysRes] = await Promise.all([
      // teacherGetTimeSlots(), -> Replaced below
      getTimeSlots(), // Corrected
      // teacherGetWeekdays() -> Replaced below
      getWeekdays() // Corrected
    ]);
    timeSlotsRef.value = slotsRes.data || defaultTimeSlots; // Use default if API fails
    weekdaysRef.value = daysRes.value || defaultWeekdays; // Use default if API fails

  } catch (error) {
    console.error("Error loading schedule page data:", error);
    // Use defaults on error
    timeSlotsRef.value = defaultTimeSlots;
    weekdaysRef.value = defaultWeekdays;
  } finally {
    loadingSchedule.value = false;
  }
});

</script>

<style scoped>
.schedule-card {
  margin-top: 20px;
}

.week-view-wrapper {
  overflow-x: auto;
}
.week-view {
  display: grid;
  /* Adjust columns based on timeSlotsRef and weekdaysRef count */
  grid-template-columns: 80px repeat(7, 1fr);
  border: 1px solid #ebeef5;
  min-width: 800px;
}

.header-cell, .time-cell, .schedule-cell {
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  padding: 8px;
  text-align: center;
  min-height: 70px; /* Increased height */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 12px;
}

.header-cell {
  background-color: #f5f7fa;
  font-weight: bold;
}

.time-column .header-cell,
.time-column .time-cell {
  border-right: 1px solid #ebeef5;
}

.day-column:last-child .header-cell,
.day-column:last-child .schedule-cell {
  border-right: none;
}

.time-column > div:last-child,
.day-column > div:last-child {
  border-bottom: none;
}

.time-cell .time-slot-label {
  font-weight: bold;
  margin-bottom: 4px;
}

.time-cell .time-range {
  color: #909399;
}

.schedule-cell {
  position: relative;
  transition: background-color 0.2s ease;
}

.course-item {
  width: 100%;
  height: 100%;
  padding: 4px;
  border-radius: 4px;
  color: #fff;
  font-size: 11px; /* Slightly smaller */
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  cursor: pointer;
  box-sizing: border-box;
  line-height: 1.3;
}

.course-name {
  font-weight: bold;
  margin-bottom: 2px;
  white-space: normal; /* Allow wrapping */
  word-break: break-all;
}

.course-location, .course-class, .course-weeks {
  font-size: 10px;
  opacity: 0.9;
  margin-top: 2px;
}

.list-view {
  /* Add styles if needed */
}

.dialog-footer {
  text-align: right;
}
</style> 