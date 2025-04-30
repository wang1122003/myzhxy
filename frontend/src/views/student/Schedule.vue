<template>
  <PageContainer title="我的课表">
    <template #actions>
        <el-select
            v-model="semester"
            placeholder="选择学期"
            style="margin-right: 10px; width: 150px;"
            :loading="loadingSemesters"
            filterable
            @change="fetchSchedule"
        >
          <el-option
              v-for="item in semestersRef"
              :key="getTermId(item)"
              :label="getTermName(item)"
              :value="getTermId(item)"
          />
        </el-select>
      <el-radio-group v-model="viewType" size="small">
        <el-radio-button label="week">周视图</el-radio-button>
        <el-radio-button label="list">列表视图</el-radio-button>
        </el-radio-group>
      <el-button :icon="Refresh" :loading="loadingSchedule" circle size="small" style="margin-left: 10px;"
                 title="刷新课表"
                 @click="fetchSchedule"></el-button>
    </template>

    <el-card v-loading="loadingSchedule || loadingSemesters" class="schedule-card">
      <!-- 周视图 -->
      <div v-if="viewType === 'week'" class="week-view-wrapper">
        <div v-if="!scheduleList || scheduleList.length === 0">
          <el-empty description="当前学期暂无课表数据"/>
        </div>
        <div v-else class="week-view-container">
          <div class="week-view">
            <!-- 时间列 -->
          <div class="time-column">
            <div class="header-cell">时间</div>
            <div v-for="time in timeSlots" :key="time.slot" class="time-cell">
              <div class="time-slot-label">第{{ time.slot }}节</div>
              <div class="time-range">{{ time.startTime }}-{{ time.endTime }}</div>
            </div>
          </div>
            <!-- 星期列 -->
            <div v-for="day in weekdays" :key="day.value" class="day-column">
              <div class="header-cell">{{ day.label }}</div>
              <div
                  v-for="time in timeSlots" :key="`${day.value}-${time.slot}`"
                  :style="getGridCellStyle(day.value, time.slot)"
                  class="schedule-cell"
                  @click="cellClickHandler(day.value, time.slot)"
              >
                <div v-if="findCourseForCell(day.value, time.slot)" class="course-item">
                  <div class="course-info">
                <div class="course-name">
                  {{ getCourseInfo(findCourseForCell(day.value, time.slot), 'courseName', '未命名课程') }}
                </div>
                    <div class="course-location">
                      @{{ getCourseInfo(findCourseForCell(day.value, time.slot), 'classroomName', '地点未知') }}
                    </div>
                    <div class="course-teacher">
                      {{ getCourseInfo(findCourseForCell(day.value, time.slot), 'teacherName', '教师未知') }}
                    </div>
                    <div class="course-weeks">{{
                        getCourseInfo(findCourseForCell(day.value, time.slot), 'startWeek', '?')
                      }}-{{ getCourseInfo(findCourseForCell(day.value, time.slot), 'endWeek', '?') }}周
                    </div>
                </div>
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
          <el-descriptions-item label="课程名称">{{
              getCourseInfo(currentCourse, 'courseName', '未命名课程')
            }}
          </el-descriptions-item>
          <el-descriptions-item label="授课教师">{{
              getCourseInfo(currentCourse, 'teacherName', '教师未知')
            }}
          </el-descriptions-item>
          <el-descriptions-item label="上课时间">
            {{ formatWeekday(currentCourse.weekday || currentCourse.dayOfWeek || currentCourse.day_of_week || 0) }}
            {{ formatTimeString(getCourseInfo(currentCourse, 'startTime', '')) }} -
            {{ formatTimeString(getCourseInfo(currentCourse, 'endTime', '')) }}
          </el-descriptions-item>
          <el-descriptions-item label="上课周次">第 {{ getCourseInfo(currentCourse, 'startWeek', '?') }} -
            {{ getCourseInfo(currentCourse, 'endWeek', '?') }} 周
          </el-descriptions-item>
          <el-descriptions-item label="上课地点">{{
              getCourseInfo(currentCourse, 'classroomName', '地点未知')
            }}
          </el-descriptions-item>
          <!-- 如果有其他信息可以添加 -->
          <!-- <el-descriptions-item label="学分">{{ getCourseInfo(currentCourse, 'credit', '--') }}</el-descriptions-item> -->
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
import {computed, onMounted, ref, reactive} from 'vue';
import {
  ElMessage,
  ElRadioGroup,
  ElRadioButton,
  ElSelect,
  ElOption,
  ElCard,
  ElTable,
  ElTableColumn,
  ElDialog,
  ElDescriptions,
  ElDescriptionsItem,
  ElButton,
  ElIcon,
  ElEmpty
} from 'element-plus';
import {Refresh} from '@element-plus/icons-vue';
import {useRoute} from 'vue-router';
import {getAllTerms} from '@/api/term';
import {getSchedulesByStudent} from '@/api/schedule';
import {getCourseById} from '@/api/course';
import {getUserById} from '@/api/user';
import {getClassroomById} from '@/api/classroom';
import PageContainer from '@/components/common/EnhancedPageContainer.vue';

// --- State ---
const loadingSchedule = ref(false);
const loadingSemesters = ref(false);
const scheduleList = ref([]); // Store the raw schedule list from API
const viewType = ref('week'); // 'week' or 'list'
const semester = ref(null); // Selected semester ID
const semestersRef = ref([]); // Semester options for select
const dialogVisible = ref(false);
const currentCourse = ref(null); // Course detail for dialog

// 缓存查询到的课程、教师和教室信息
const courseDetailsCache = reactive({});
const teacherDetailsCache = reactive({});
const classroomDetailsCache = reactive({});

// --- Constants for Week View ---
// TODO: These should ideally come from backend or config
const timeSlots = reactive([
  {slot: 1, label: '第1节', startTime: '08:00', endTime: '08:45'},
  {slot: 2, label: '第2节', startTime: '08:55', endTime: '09:40'},
  {slot: 3, label: '第3节', startTime: '10:00', endTime: '10:45'},
  {slot: 4, label: '第4节', startTime: '10:55', endTime: '11:40'},
  {slot: 5, label: '第5节', startTime: '14:00', endTime: '14:45'},
  {slot: 6, label: '第6节', startTime: '14:55', endTime: '15:40'},
  {slot: 7, label: '第7节', startTime: '16:00', endTime: '16:45'},
  {slot: 8, label: '第8节', startTime: '16:55', endTime: '17:40'},
  {slot: 9, label: '第9节', startTime: '19:00', endTime: '19:45'},
  {slot: 10, label: '第10节', startTime: '19:55', endTime: '20:40'},
  {slot: 11, label: '第11节', startTime: '20:50', endTime: '21:35'},
]);
const weekdays = reactive([
  {value: 1, label: '星期一'},
  {value: 2, label: '星期二'},
  {value: 3, label: '星期三'},
  {value: 4, label: '星期四'},
  {value: 5, label: '星期五'},
  {value: 6, label: '星期六'},
  {value: 7, label: '星期日'},
]);

// --- Computed Properties for TableView ---
const tableColumns = computed(() => [
  {
    prop: 'courseName',
    label: '课程名称',
    minWidth: 150,
    formatter: (row) => getCourseInfo(row, 'courseName', '未命名课程')
  },
  {
    prop: 'teacherName',
    label: '授课教师',
    minWidth: 100,
    formatter: (row) => getCourseInfo(row, 'teacherName', '教师未知')
  },
  {
    prop: 'weekday',
    label: '星期',
    width: 100,
    formatter: (row) => formatWeekday(row.weekday || row.dayOfWeek || row.day_of_week || 0)
  },
  {
    prop: 'timeRange',
    label: '时间',
    width: 150,
    formatter: (row) => {
      const start = getCourseInfo(row, 'startTime', '?');
      const end = getCourseInfo(row, 'endTime', '?');
      return `${formatTimeString(start)} - ${formatTimeString(end)}`;
    }
  },
  {
    prop: 'weeksRange',
    label: '周次',
    width: 120,
    formatter: (row) => `第 ${getCourseInfo(row, 'startWeek', '?')}-${getCourseInfo(row, 'endWeek', '?')} 周`
  },
  {
    prop: 'classroomName',
    label: '教室',
    minWidth: 120,
    formatter: (row) => getCourseInfo(row, 'classroomName', '地点未知')
  }
]);

const actionColumnConfig = computed(() => ({
  width: 100,
  fixed: 'right',
  buttons: [
    {label: '详情', type: 'primary', size: 'small', event: 'view-detail'}
  ]
}));

// --- Helper Functions ---

// Helper to convert time to minutes since midnight for easier comparison
const timeToMinutes = (timeValue) => {
  let timeStr = '';
  if (typeof timeValue === 'string') {
    timeStr = timeValue.substring(0, 5);
  } else if (timeValue instanceof Date) {
    timeStr = `${timeValue.getHours().toString().padStart(2, '0')}:${timeValue.getMinutes().toString().padStart(2, '0')}`;
  } else if (Array.isArray(timeValue) && timeValue.length >= 2) {
    timeStr = `${timeValue[0].toString().padStart(2, '0')}:${timeValue[1].toString().padStart(2, '0')}`;
  } else if (typeof timeValue === 'number') {
    // 处理毫秒时间戳格式
    const date = new Date(timeValue);
    timeStr = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
  if (timeStr.match(/^\d{2}:\d{2}$/)) {
    const [hours, minutes] = timeStr.split(':').map(Number);
    return hours * 60 + minutes;
  }
  return -1; // Invalid format
};

// 属性映射函数，用于从不同格式的学期数据中提取ID
const getTermId = (term) => {
  if (!term) return null;
  // 优先使用code字段作为学期ID
  return term.code || term.id || term.termId || term.term_id || term.value;
}

// 属性映射函数，用于从不同格式的学期数据中提取名称
const getTermName = (term) => {
  if (!term) return '';
  // 优先使用displayName字段作为学期名称
  return term.displayName || term.termName || term.name || term.term_name || term.label || `${term.year || ''}${term.semester || ''}`;
}

// 检查学期是否为当前学期
const isCurrentTerm = (term) => {
  if (!term) return false;
  return term.isCurrent || term.is_current || term.current === true || term.status === 'current';
}

const formatWeekday = (day) => {
  const found = weekdays.find(d => d.value === day);
  return found ? found.label : `星期${day}`;
};

// Generate distinct colors for courses in week view
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

// Find course for a specific cell in the week view
const findCourseForCell = (day, timeSlot) => {
  const targetStartTime = timeSlots.find(t => t.slot === timeSlot)?.startTime; // "HH:MM" 格式
  if (!targetStartTime) return null;

  return scheduleList.value.find(course => {
    const courseDay = course.weekday || course.dayOfWeek || course.day_of_week || course.day;
    const rawStartTime = course.startTime || course.start_time;

    // 将各种可能的 startTime 格式转换为 "HH:MM" 字符串
    let courseStartTimeHHMM = null;
    if (typeof rawStartTime === 'string') {
      // 假设是 "HH:MM:SS" 或 "HH:MM"
      courseStartTimeHHMM = rawStartTime.substring(0, 5);
    } else if (rawStartTime instanceof Date) {
      // 如果是 Date 对象
      const hours = rawStartTime.getHours().toString().padStart(2, '0');
      const minutes = rawStartTime.getMinutes().toString().padStart(2, '0');
      courseStartTimeHHMM = `${hours}:${minutes}`;
    } else if (Array.isArray(rawStartTime) && rawStartTime.length >= 2) {
      // 假设是 [H, M, ...] 数组
      const hours = rawStartTime[0].toString().padStart(2, '0');
      const minutes = rawStartTime[1].toString().padStart(2, '0');
      courseStartTimeHHMM = `${hours}:${minutes}`;
    } else if (typeof rawStartTime === 'number') {
      // 处理毫秒时间戳格式
      const date = new Date(rawStartTime);
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      courseStartTimeHHMM = `${hours}:${minutes}`;
    }

    // 只有成功转换后才进行比较
    return courseDay == day && courseStartTimeHHMM === targetStartTime;
  });
};

// Get style for grid cell (row span and background color)
const getGridCellStyle = (day, timeSlot) => {
  const course = findCourseForCell(day, timeSlot);
  if (!course) return {};

  // --- 以下部分需要根据 startTime/endTime 确定 rowSpan ---
  const courseStartMinutes = timeToMinutes(course.startTime || course.start_time);
  const courseEndMinutes = timeToMinutes(course.endTime || course.end_time);
  const slotStartMinutes = timeToMinutes(timeSlots.find(t => t.slot === timeSlot)?.startTime);
  const slotEndMinutes = timeToMinutes(timeSlots.find(t => t.slot === timeSlot)?.endTime);

  if (courseStartMinutes === -1 || courseEndMinutes === -1 || slotStartMinutes === -1) {
    console.warn("getGridCellStyle: Invalid time format for course or slot", course, timeSlots.find(t => t.slot === timeSlot));
    return {}; // Skip styling if time format is bad
  }

  // Check if this cell is the starting cell of the course
  if (courseStartMinutes !== slotStartMinutes) {
    return {}; // Not the start cell, apply default style
  }

  // Calculate row span based on duration
  let rowSpan = 0;
  for (let i = timeSlot; i < timeSlots.length; i++) {
    const currentSlotEndMinutes = timeToMinutes(timeSlots[i].endTime);
    if (currentSlotEndMinutes <= courseEndMinutes) {
      rowSpan++;
    } else {
      break;
    }
    // Safety break if end time is somehow before start time
    if (currentSlotEndMinutes === -1 || currentSlotEndMinutes < slotStartMinutes) break;
  }
  rowSpan = Math.max(1, rowSpan); // Minimum span is 1
  // --- rowSpan 计算结束 ---

  return {
    gridRowEnd: `span ${rowSpan}`, // 使用 gridRowEnd 替代 gridRowStart: span
    backgroundColor: getCourseColor(course.courseId),
    color: '#fff',
    cursor: 'pointer',
    zIndex: 1, // Ensure spanned items are on top
    position: 'relative' // Needed for zIndex
  };
};

// Hide cells that are covered by a spanning course item
const isCellCovered = (day, timeSlot) => {
  for (let prevSlot = timeSlot - 1; prevSlot >= 1; prevSlot--) {
    const courseAbove = findCourseForCell(day, prevSlot);
    if (courseAbove) {
      const startMinutes = timeToMinutes(courseAbove.startTime || courseAbove.start_time);
      const endMinutes = timeToMinutes(courseAbove.endTime || courseAbove.end_time);
      const currentSlotStartMinutes = timeToMinutes(timeSlots.find(t => t.slot === timeSlot)?.startTime);

      if (startMinutes !== -1 && endMinutes !== -1 && currentSlotStartMinutes !== -1 &&
          timeToMinutes(timeSlots.find(t => t.slot === prevSlot)?.startTime) === startMinutes && // Is it the start cell?
          currentSlotStartMinutes < endMinutes) { // Is the current cell covered by the duration?
        return true; // Covered by a course starting above
      }
      // If we found a course in a cell above but it doesn't cover the current one, we can stop checking upwards
      break;
    }
  }
  return false;
};

// 安全获取课程属性，处理字段缺失情况
const getCourseInfo = (course, property, defaultValue = '') => {
  // 不同的字段名映射
  const propertyMap = {
    'courseName': ['courseName', 'course_name', 'name', 'title', 'course', 'courseTitle', 'course_title'],
    'teacherName': ['teacherName', 'teacher_name', 'teacher', 'teacherInfo', 'teacher_info', 'real_name', 'realName'],
    'classroomName': ['classroomName', 'classroom_name', 'classroom', 'location', 'room', 'roomName', 'room_name', 'place'],
    'startWeek': ['startWeek', 'start_week', 'weekStart', 'beginWeek', 'begin_week'],
    'endWeek': ['endWeek', 'end_week', 'weekEnd', 'lastWeek', 'last_week'],
    'startTime': ['startTime', 'start_time', 'beginTime', 'begin_time', 'timeStart', 'time_start'],
    'endTime': ['endTime', 'end_time', 'overTime', 'over_time', 'timeEnd', 'time_end']
  };

  if (!course) return defaultValue;

  // 尝试所有可能的属性名
  const possibleProperties = propertyMap[property] || [property];
  for (const prop of possibleProperties) {
    if (course[prop] !== undefined && course[prop] !== null) {
      return course[prop];
    }
  }

  // 如果找不到直接属性，从缓存中查找相关实体信息
  if (property === 'courseName' && course.courseId && courseDetailsCache[course.courseId]) {
    const courseDetail = courseDetailsCache[course.courseId];
    return courseDetail.name || courseDetail.courseName || courseDetail.course_name || defaultValue;
  }

  if (property === 'teacherName' && course.teacherId && teacherDetailsCache[course.teacherId]) {
    const teacherDetail = teacherDetailsCache[course.teacherId];
    return teacherDetail.realName || teacherDetail.name || teacherDetail.teacherName || teacherDetail.real_name || defaultValue;
  }

  if (property === 'classroomName' && course.classroomId && classroomDetailsCache[course.classroomId]) {
    const classroomDetail = classroomDetailsCache[course.classroomId];
    return classroomDetail.name || classroomDetail.roomName || classroomDetail.classroomName || defaultValue;
  }

  // 检查是否有嵌套对象包含这个属性 (例如 course.course.name 或 course.teacher.name)
  if (course.course) {
    // 尝试从course对象获取
    for (const prop of ['name', 'title', 'courseName']) {
      if (course.course[prop] !== undefined && course.course[prop] !== null) {
        if (property === 'courseName') return course.course[prop];
      }
    }
  }

  if (course.teacher) {
    // 尝试从teacher对象获取
    for (const prop of ['name', 'teacherName', 'realName', 'real_name']) {
      if (course.teacher[prop] !== undefined && course.teacher[prop] !== null) {
        if (property === 'teacherName') return course.teacher[prop];
      }
    }
  }

  if (course.classroom) {
    // 尝试从classroom对象获取
    for (const prop of ['name', 'classroomName', 'roomName', 'room_name', 'location']) {
      if (course.classroom[prop] !== undefined && course.classroom[prop] !== null) {
        if (property === 'classroomName') return course.classroom[prop];
      }
    }
  }

  return defaultValue;
};

// 安全格式化时间字符串，支持多种格式
const formatTimeString = (timeValue) => {
  if (!timeValue) return '--:--';

  if (typeof timeValue === 'string') {
    return timeValue.substring(0, 5); // 取 HH:MM 部分
  } else if (timeValue instanceof Date) {
    return `${timeValue.getHours().toString().padStart(2, '0')}:${timeValue.getMinutes().toString().padStart(2, '0')}`;
  } else if (Array.isArray(timeValue) && timeValue.length >= 2) {
    return `${timeValue[0].toString().padStart(2, '0')}:${timeValue[1].toString().padStart(2, '0')}`;
  } else if (typeof timeValue === 'number') {
    // 处理毫秒时间戳
    const date = new Date(timeValue);
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }

  return '--:--'; // 无法识别的格式
};

// Handle click on a grid cell
const cellClickHandler = (day, timeSlot) => {
  const course = findCourseForCell(day, timeSlot);
  if (course) {
    showCourseDetail(course);
  }
};

// Show course detail dialog
const showCourseDetail = (course) => {
  currentCourse.value = course;
  dialogVisible.value = true;
};

// --- Lifecycle Hooks ---
onMounted(async () => {
  await fetchSemesters();
  // Fetch schedule only after a semester is selected (handled by default value setting or @change)
  if (semester.value) {
    await fetchSchedule();
  }
});

// --- Methods ---

// Fetch available semesters
const fetchSemesters = async () => {
  loadingSemesters.value = true;
  try {
    console.log('[Schedule] 开始获取学期列表');
    const res = await getAllTerms();
    console.log('[Schedule] 学期API返回数据:', res);

    if (res && res.code === 200 && res.data) {
      semestersRef.value = Array.isArray(res.data) ? res.data : [];
    } else if (Array.isArray(res)) {
      semestersRef.value = res;
    } else {
      semestersRef.value = [];
      ElMessage.warning('获取学期列表失败');
    }

    console.log('[Schedule] 学期列表:', semestersRef.value);

    // 详细输出第一个学期的完整数据结构
        if (semestersRef.value.length > 0) {
          console.log('[Schedule] 第一个学期的完整数据结构:', JSON.stringify(semestersRef.value[0]));
          console.log('[Schedule] 第一个学期的所有属性名:', Object.keys(semestersRef.value[0]));
        }

    // 调试输出每个学期的ID和名称
    semestersRef.value.forEach((term, index) => {
      console.log(`[Schedule] 学期${index + 1}: id=${getTermId(term)}, name=${getTermName(term)}, isCurrent=${isCurrentTerm(term)}`);
    });

    // 设置默认学期
    if (semestersRef.value.length > 0 && !semester.value) {
      // 先尝试找当前学期，如果没有则选第一个
      const current = semestersRef.value.find(term => isCurrentTerm(term));
      if (current) {
        console.log('[Schedule] 默认选择当前学期:', getTermName(current), getTermId(current));
        semester.value = getTermId(current);
      } else {
        console.log('[Schedule] 默认选择第一个学期:', getTermName(semestersRef.value[0]), getTermId(semestersRef.value[0]));
        semester.value = getTermId(semestersRef.value[0]);
      }
      // 自动加载选中学期的课表
      fetchSchedule();
    } else if (semestersRef.value.length === 0) {
      ElMessage.warning('没有可用的学期数据');
        }
      } catch (error) {
    console.error("[Schedule] 获取学期列表失败:", error);
    ElMessage.error('获取学期列表失败');
        semestersRef.value = [];
      } finally {
    loadingSemesters.value = false;
  }
};

// Fetch student schedule for the selected semester
const fetchSchedule = async () => {
  if (!semester.value) {
    ElMessage.warning('请先选择学期');
    return; // Don't fetch if no semester selected
  }
  loadingSchedule.value = true;
  // Reset color mapping for new schedule
  colorIndex = 0;
  Object.keys(courseColors).forEach(key => delete courseColors[key]);
  try {
    console.log('[Schedule] 开始获取课表数据，学期ID:', semester.value);
    const params = {termId: semester.value};
    const res = await getSchedulesByStudent(params);
    console.log('[Schedule] 课表API返回数据:', res);

    // 更详细地输出API返回的数据结构
    console.log('[Schedule] 课表API返回类型:', typeof res);
    if (res) {
      console.log('[Schedule] 课表API返回顶层属性:', Object.keys(res));
      // 详细检查第一条记录的所有字段
      if (Array.isArray(res) && res.length > 0) {
        console.log('[Schedule] 第一条课表记录的所有字段:', Object.keys(res[0]));
        console.log('[Schedule] 第一条课表记录的完整内容:', JSON.stringify(res[0]));
      }
      if (res.data) {
        console.log('[Schedule] res.data类型:', typeof res.data);
        if (Array.isArray(res.data)) {
          console.log('[Schedule] res.data是数组，长度:', res.data.length);
          if (res.data.length > 0) {
            console.log('[Schedule] 第一条数据样例:', JSON.stringify(res.data[0]));
          }
        } else {
          console.log('[Schedule] res.data不是数组，详情:', JSON.stringify(res.data));
        }
      }
    }

    // 直接处理数组响应
    if (Array.isArray(res)) {
      scheduleList.value = res;
      console.log('[Schedule] 成功解析课表数据(直接数组):', scheduleList.value.length, '条记录');
      // 详细输出首条数据的所有属性
      if (scheduleList.value.length > 0) {
        console.log('[Schedule] 首条课表记录详情:');
        const firstRecord = scheduleList.value[0];
        console.log('- 原始对象:', firstRecord);
        console.log('- 课程ID:', firstRecord.courseId);
        console.log('- 课程名称 (courseName):', firstRecord.courseName);
        console.log('- 教师ID:', firstRecord.teacherId);
        console.log('- 教师名称 (teacherName):', firstRecord.teacherName);
        console.log('- 教室ID:', firstRecord.classroomId);
        console.log('- 教室名称 (classroomName):', firstRecord.classroomName);
        console.log('- 星期几 (dayOfWeek):', firstRecord.dayOfWeek);
        console.log('- 开始时间 (startTime):', firstRecord.startTime, typeof firstRecord.startTime);
        console.log('- 结束时间 (endTime):', firstRecord.endTime, typeof firstRecord.endTime);
        console.log('- 起始周 (startWeek):', firstRecord.startWeek);
        console.log('- 结束周 (endWeek):', firstRecord.endWeek);
      }

      // 获取关联实体的详细信息
      await fetchRelatedEntities(scheduleList.value);
    }
    // 保留其他格式处理逻辑以兼容性
    else if (res && res.code === 200) {
      if (Array.isArray(res.data)) {
        scheduleList.value = res.data;
        console.log('[Schedule] 成功解析课表数据(标准数组格式):', scheduleList.value.length, '条记录');
        // 获取关联实体的详细信息
        await fetchRelatedEntities(scheduleList.value);
      } else if (res.data && res.data.records && Array.isArray(res.data.records)) {
        scheduleList.value = res.data.records;
        console.log('[Schedule] 成功解析课表数据(分页格式):', scheduleList.value.length, '条记录');
        // 获取关联实体的详细信息
        await fetchRelatedEntities(scheduleList.value);
      } else if (res.data && typeof res.data === 'object') {
        // 如果res.data是单个对象，尝试作为课表项处理
        scheduleList.value = [res.data];
        console.log('[Schedule] 成功解析课表数据(单对象格式)');
        // 获取关联实体的详细信息
        await fetchRelatedEntities(scheduleList.value);
      } else if (res.data === null || res.data === undefined) {
        // API返回成功但无数据
        scheduleList.value = [];
        console.log('[Schedule] API返回空数据');
      } else {
        console.warn('[Schedule] 未识别的res.data格式:', res.data);
        scheduleList.value = [];
      }
    } else if (res && res.list && Array.isArray(res.list)) {
      // 有些API返回 {list: [...]} 格式
      scheduleList.value = res.list;
      console.log('[Schedule] 成功解析课表数据(list数组):', scheduleList.value.length, '条记录');
      // 获取关联实体的详细信息
      await fetchRelatedEntities(scheduleList.value);
    } else if (res && res.items && Array.isArray(res.items)) {
      // 有些API返回 {items: [...]} 格式
      scheduleList.value = res.items;
      console.log('[Schedule] 成功解析课表数据(items数组):', scheduleList.value.length, '条记录');
      // 获取关联实体的详细信息
      await fetchRelatedEntities(scheduleList.value);
    } else if (res && res.rows && Array.isArray(res.rows)) {
      // 有些API返回 {rows: [...]} 格式
      scheduleList.value = res.rows;
      console.log('[Schedule] 成功解析课表数据(rows数组):', scheduleList.value.length, '条记录');
      // 获取关联实体的详细信息
      await fetchRelatedEntities(scheduleList.value);
    } else if (res && res.success === true && res.result) {
      // 处理 {success: true, result: ...} 格式
      if (Array.isArray(res.result)) {
        scheduleList.value = res.result;
        // 获取关联实体的详细信息
        await fetchRelatedEntities(scheduleList.value);
      } else if (res.result && res.result.list && Array.isArray(res.result.list)) {
        scheduleList.value = res.result.list;
        // 获取关联实体的详细信息
        await fetchRelatedEntities(scheduleList.value);
      } else if (typeof res.result === 'object') {
        scheduleList.value = [res.result];
        // 获取关联实体的详细信息
        await fetchRelatedEntities(scheduleList.value);
      } else {
        scheduleList.value = [];
      }
      console.log('[Schedule] 成功解析课表数据(success/result格式):', scheduleList.value.length, '条记录');
    } else if (res && typeof res === 'object') {
      // 最后尝试：将响应对象本身当作单个课表项处理
      try {
        // 检查对象是否有可能是课表项（包含一些关键属性）
        const hasScheduleProps =
            res.courseName || res.course_name || res.name || res.title ||
            res.weekday || res.dayOfWeek || res.day_of_week || res.day;

        if (hasScheduleProps) {
          scheduleList.value = [res];
          console.log('[Schedule] 将响应对象作为单个课表项处理');
          // 获取关联实体的详细信息
          await fetchRelatedEntities(scheduleList.value);
        } else {
          console.warn('[Schedule] 响应对象不符合课表项特征:', res);
          scheduleList.value = [];
        }
      } catch (e) {
        console.error('[Schedule] 解析对象为课表项时出错:', e);
        scheduleList.value = [];
      }
    } else {
      console.warn('[Schedule] 课表API返回格式异常:', res);
      scheduleList.value = [];
    }

    // 没有数据时显示提示
    if (scheduleList.value.length === 0) {
      ElMessage.info('当前学期暂无课表数据');
    }
  } catch (error) {
    console.error("[Schedule] 获取课表失败:", error);
    scheduleList.value = [];
    ElMessage.error("获取课表失败，请稍后重试");
  } finally {
    loadingSchedule.value = false;
  }
};

// 获取课表关联实体的详细信息
const fetchRelatedEntities = async (schedules) => {
  if (!schedules || schedules.length === 0) return;

  // 收集所有需要查询的ID
  const courseIds = new Set();
  const teacherIds = new Set();
  const classroomIds = new Set();

  schedules.forEach(schedule => {
    if (schedule.courseId && !courseDetailsCache[schedule.courseId]) {
      courseIds.add(schedule.courseId);
    }
    if (schedule.teacherId && !teacherDetailsCache[schedule.teacherId]) {
      teacherIds.add(schedule.teacherId);
    }
    if (schedule.classroomId && !classroomDetailsCache[schedule.classroomId]) {
      classroomIds.add(schedule.classroomId);
    }
  });

  // 并行获取所有实体详细信息
  const promises = [];

  // 查询课程信息
  for (const courseId of courseIds) {
    promises.push(fetchCourseDetails(courseId));
  }

  // 查询教师信息
  for (const teacherId of teacherIds) {
    promises.push(fetchTeacherDetails(teacherId));
  }

  // 查询教室信息
  for (const classroomId of classroomIds) {
    promises.push(fetchClassroomDetails(classroomId));
  }

  // 等待所有查询完成
  await Promise.allSettled(promises);

  console.log('[Schedule] 已加载关联实体信息:');
  console.log('- 课程信息:', Object.keys(courseDetailsCache).length, '个');
  console.log('- 教师信息:', Object.keys(teacherDetailsCache).length, '个');
  console.log('- 教室信息:', Object.keys(classroomDetailsCache).length, '个');
};

// 获取课程、教师和教室详细信息，用于补充课表显示
const fetchCourseDetails = async (courseId) => {
  if (!courseId) return null;
  if (courseDetailsCache[courseId]) return courseDetailsCache[courseId];

  try {
    const res = await getCourseById(courseId);
    if (res && res.code === 200 && res.data) {
      courseDetailsCache[courseId] = res.data;
      return res.data;
    } else if (res && !res.code) {
      // 直接返回数据的情况
      courseDetailsCache[courseId] = res;
      return res;
    }
  } catch (error) {
    console.error('[Schedule] 获取课程详情失败:', error);
  }
  return null;
};

const fetchTeacherDetails = async (teacherId) => {
  if (!teacherId) return null;
  if (teacherDetailsCache[teacherId]) return teacherDetailsCache[teacherId];

  try {
    const res = await getUserById(teacherId);
    if (res && res.code === 200 && res.data) {
      teacherDetailsCache[teacherId] = res.data;
      return res.data;
    } else if (res && !res.code) {
      // 直接返回数据的情况
      teacherDetailsCache[teacherId] = res;
      return res;
    }
  } catch (error) {
    console.error('[Schedule] 获取教师详情失败:', error);
  }
  return null;
};

const fetchClassroomDetails = async (classroomId) => {
  if (!classroomId) return null;
  if (classroomDetailsCache[classroomId]) return classroomDetailsCache[classroomId];

  try {
    const res = await getClassroomById(classroomId);
    if (res && res.code === 200 && res.data) {
      classroomDetailsCache[classroomId] = res.data;
      return res.data;
    } else if (res && !res.code) {
      // 直接返回数据的情况
      classroomDetailsCache[classroomId] = res;
      return res;
    }
  } catch (error) {
    console.error('[Schedule] 获取教室详情失败:', error);
  }
  return null;
};
</script>

<style scoped>
.schedule-card {
  margin-top: 16px;
}

/* 周视图样式 */
.week-view-wrapper {
  width: 100%;
}

.week-view-container {
  width: 100%;
  overflow: auto;
  max-height: 600px; /* 限制最大高度，避免占据太多空间 */
  position: relative;
}

.week-view {
  display: grid;
  grid-template-columns: 50px repeat(7, 1fr); /* 减小时间列宽度 */
  grid-auto-rows: minmax(50px, auto); /* 减小行高 */
  min-width: 800px; /* 确保在小屏幕上可以水平滚动 */
  position: relative; /* 为固定元素提供参考点 */
}

.time-column {
  position: sticky;
  left: 0;
  z-index: 3;
  background-color: #fff;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.05);
}

.header-cell {
  position: sticky;
  top: 0;
  z-index: 2;
  background-color: #f5f7fa;
  font-weight: bold;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 35px; /* 减小表头高度 */
  font-size: 12px; /* 减小表头字体 */
  padding: 0;
}

/* 时间列的表头单元格需要更高的z-index */
.time-column .header-cell {
  z-index: 4;
  background-color: #f5f7fa;
  border-right: 1px solid #ebeef5;
}

.time-column,
.day-column {
  display: flex;
  flex-direction: column;
}

.time-cell {
  height: 50px; /* 减小时间单元格高度 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding: 2px;
  background-color: #fff;
  border-right: 1px solid #ebeef5;
}

.time-slot-label {
  font-weight: bold;
  font-size: 11px; /* 减小时间标签字体 */
  margin: 0;
}

.time-range {
  font-size: 9px; /* 减小时间范围字体 */
  color: #606266;
  margin: 0;
}

.schedule-cell {
  min-height: 50px; /* 减小单元格最小高度 */
  border-bottom: 1px solid #ebeef5;
  border-right: 1px solid #ebeef5;
  position: relative;
  background-color: #fff;
}

.course-item {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2px; /* 进一步减小内边距 */
  overflow: hidden;
  color: white;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.course-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.course-name {
  font-weight: bold;
  font-size: 11px; /* 减小课程名称字体 */
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 1px;
  line-height: 1.1;
  text-align: center;
  max-width: 100%;
}

.course-location,
.course-teacher,
.course-weeks {
  font-size: 9px; /* 减小其他信息字体 */
  word-break: break-all;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  line-height: 1.1;
  margin: 0;
  text-align: center;
}

/* 列表视图 */
.list-view {
  width: 100%;
}

@media screen and (max-width: 768px) {
  .week-view {
    min-width: 700px;
    grid-template-columns: 40px repeat(7, 1fr); /* 更窄的时间列 */
  }

  .time-column {
    width: 40px; /* 更窄的时间列 */
  }

  .time-cell {
    padding: 1px;
  }

  .time-slot-label {
    font-size: 10px;
  }

  .time-range {
    font-size: 8px;
  }

  .header-cell {
    font-size: 11px;
    padding: 2px 0;
    height: 30px;
  }

  .course-name {
    font-size: 10px;
    -webkit-line-clamp: 2;
  }

  .course-location, .course-teacher, .course-weeks {
    font-size: 8px;
  }

  /* 对于非常小的屏幕，自动切换到列表视图 */
  @media screen and (max-width: 480px) {
    :deep(.el-radio-button__inner) {
      padding: 8px 12px;
      font-size: 12px;
    }

    :deep(.el-select) {
      width: 130px !important;
    }
  }
}

:deep(.el-table .cell) {
  padding-left: 5px;
  padding-right: 5px;
}

:deep(.el-table th) {
  padding: 8px 0;
}

:deep(.el-table td) {
  padding: 5px 0;
}
</style> 