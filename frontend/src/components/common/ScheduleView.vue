<template>
  <PageContainer :title="title">
    <template #header-actions>
      <el-select
          v-model="semester"
          :loading="loadingSemesters"
          filterable
          placeholder="选择学期"
          style="margin-right: 10px; width: 200px;"
          @change="fetchSchedule"
      >
        <el-option
            v-for="(item, index) in semestersRef"
            :key="item.code || item.id || item.termId || index"
            :label="item.displayName || item.termName || item.name || `学期${index+1}`"
            :value="item.code || item.id || item.termId || index+1"
        />
      </el-select>
      <el-radio-group v-model="viewType" style="margin-left: 10px;">
        <el-radio-button :value="'week'">周视图</el-radio-button>
        <el-radio-button :value="'list'">列表视图</el-radio-button>
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
                v-for="time in timeSlotsRef" v-show="!isCellCovered(day.value, time.slot)"
                :key="`${day.value}-${time.slot}`"
                :style="getGridCellStyle(day.value, time.slot)"
                class="schedule-cell"
                @click="cellClickHandler(day.value, time.slot)"
            >
              <div v-if="findCourseForCell(day.value, time.slot)" class="course-item">
                <div class="course-name">{{ findCourseForCell(day.value, time.slot).courseName }}</div>
                <div class="course-location">@{{ findCourseForCell(day.value, time.slot).classroomName }}</div>
                <div class="course-weeks">周次: {{ findCourseForCell(day.value, time.slot).weeksRange }}</div>
                <div v-if="findCourseForCell(day.value, time.slot).teacherName" class="course-teacher">
                  教师: {{ findCourseForCell(day.value, time.slot).teacherName }}
                </div>
                <div v-if="findCourseForCell(day.value, time.slot).className" class="course-class">
                  班级: {{ findCourseForCell(day.value, time.slot).className }}
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
            :columns="tableColumns"
            :data="scheduleList"
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
          <el-descriptions-item label="上课时间">{{ currentCourse.weekdayName }} {{ currentCourse.timeRange }}
          </el-descriptions-item>
          <el-descriptions-item label="上课周次">{{ currentCourse.weeksRange }}</el-descriptions-item>
          <el-descriptions-item label="上课地点">{{ currentCourse.classroomName }}</el-descriptions-item>
          <el-descriptions-item v-if="showTeacherInfo && currentCourse.teacherName" label="授课教师">
            {{ currentCourse.teacherName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentCourse.className" label="班级">
            {{ currentCourse.className }}
          </el-descriptions-item>
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

<style scoped>
.schedule-card {
  margin-top: 20px;
}

.week-view-wrapper {
  overflow-x: auto;
}

.week-view {
  display: flex;
  min-width: 1000px;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.time-column, .day-column {
  display: flex;
  flex-direction: column;
  border-right: 1px solid #ebeef5;
}

.time-column {
  width: 100px;
  flex-shrink: 0;
  background-color: #f8f9fa;
}

.day-column {
  flex: 1;
  min-width: 140px;
}

.header-cell {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  font-weight: bold;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
}

.time-cell {
  height: 90px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5px;
  border-bottom: 1px solid #ebeef5;
}

.time-slot-label {
  font-weight: bold;
  margin-bottom: 5px;
}

.time-range {
  font-size: 12px;
  color: #606266;
}

.schedule-cell {
  height: 90px;
  border-bottom: 1px solid #ebeef5;
  padding: 5px;
  cursor: pointer;
  transition: all 0.3s;
}

.schedule-cell:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.course-item {
  height: 100%;
  padding: 8px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #333;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.course-item:hover {
  transform: scale(1.02);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.course-name {
  font-weight: bold;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.course-location, .course-weeks, .course-teacher, .course-class {
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 2px;
  opacity: 0.9;
}

.course-item:hover .course-name,
.course-item:hover .course-location,
.course-item:hover .course-weeks,
.course-item:hover .course-teacher,
.course-item:hover .course-class {
  white-space: normal;
  overflow: visible;
}

.list-view {
  padding: 10px 0;
}
</style>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElDialog,
  ElEmpty,
  ElMessage,
  ElOption,
  ElRadioButton,
  ElRadioGroup,
  ElSelect
} from 'element-plus';
import {Refresh} from '@element-plus/icons-vue';
import {getStudentWeeklySchedule, getTeacherWeeklySchedule} from '@/api/schedule';
import {getTimeSlots, getWeekdays} from '@/api/common'; // Keep fetching these for UI structure
import {getAllTerms} from '@/api/term';
import {useUserStore} from '@/stores/userStore';
import TableView from '@/components/common/TableView.vue';
import PageContainer from '@/components/common/EnhancedPageContainer.vue';

// 接收角色类型和相关配置
const props = defineProps({
  role: {
    type: String,
    required: true,
    validator: (value) => ['student', 'teacher', 'admin'].includes(value)
  },
  title: {
    type: String,
    default: '课程表'
  },
  showTeacherInfo: {
    type: Boolean,
    default: false
  }
});

// --- State ---
const loadingSchedule = ref(false);
const loadingSemesters = ref(false);
const loadingTimeSlots = ref(false); // Still needed for UI grid

// 用户信息
const userStore = useUserStore();

// Default definitions might still be useful as fallbacks or initial state
const defaultTimeSlots = [
  {slot: 1, startTime: '08:00', endTime: '08:45'},
  {slot: 2, startTime: '08:55', endTime: '09:40'},
  {slot: 3, startTime: '10:00', endTime: '10:45'},
  {slot: 4, startTime: '10:55', endTime: '11:40'},
  {slot: 5, startTime: '14:00', endTime: '14:45'},
  {slot: 6, startTime: '14:55', endTime: '15:40'},
  {slot: 7, startTime: '16:00', endTime: '16:45'},
  {slot: 8, startTime: '16:55', endTime: '17:40'},
  {slot: 9, startTime: '19:00', endTime: '19:45'},
  {slot: 10, startTime: '19:55', endTime: '20:40'}
];
const defaultWeekdays = [
  {value: 1, label: '星期一'},
  {value: 2, label: '星期二'},
  {value: 3, label: '星期三'},
  {value: 4, label: '星期四'},
  {value: 5, label: '星期五'},
  {value: 6, label: '星期六'},
  {value: 7, label: '星期日'}
];

const scheduleList = ref([]); // Will store the processed list from backend
const viewType = ref('week');
const semester = ref(null);
const dialogVisible = ref(false);
const currentCourse = ref(null);

const semestersRef = ref([]);
const timeSlotsRef = ref(defaultTimeSlots); // Keep for week view grid rendering
const weekdaysRef = ref(defaultWeekdays); // Keep for week view grid rendering

// --- Computed Properties for TableView (Simplified) ---
const tableColumns = computed(() => {
  const columns = [
    {prop: 'courseName', label: '课程名称', minWidth: 180},
    // Use backend provided formatted fields, remove formatters
    {prop: 'weekdayName', label: '星期', width: 100},
    {prop: 'timeRange', label: '时间', width: 150},
    {prop: 'weeksRange', label: '周次', width: 120},
    {prop: 'classroomName', label: '教室', width: 120},
  ];

  // Add role-specific columns (using backend provided fields)
  if (props.role === 'student') {
    columns.splice(1, 0, {prop: 'teacherName', label: '教师', width: 120});
  } else if (props.role === 'teacher') {
    columns.splice(1, 0, {prop: 'className', label: '班级', width: 120});
  }

  return columns;
});

const actionColumnConfig = computed(() => ({
  width: 100,
  fixed: 'right',
  buttons: [
    {label: '详情', type: 'primary', link: true, event: 'view-detail'}
  ]
}));

// --- Methods ---

// Simplified fetchSchedule
const fetchSchedule = async () => {
  if (!semester.value) {
    ElMessage.warning('请选择学期');
    return;
  }

  loadingSchedule.value = true;
  scheduleList.value = []; // Clear previous data

  try {
    const params = {
      termCode: semester.value // Use termCode as expected by backend
    };

    // 获取用户ID时确保调用userId()函数
    let userId;
    try {
      userId = typeof userStore.userId === 'function' ? userStore.userId() : userStore.userId;
      console.log("获取课表: 角色=", props.role, ", 用户ID=", userId, ", 学期=", semester.value);
    } catch (error) {
      console.error("获取用户ID失败:", error);
      ElMessage.warning('无法获取用户ID，请重新登录或联系管理员');
      loadingSchedule.value = false;
      return;
    }

    if (!userId) {
      ElMessage.warning('无法获取用户ID，请重新登录或联系管理员');
      loadingSchedule.value = false;
      return;
    }

    let res;
    try {
      if (props.role === 'student') {
        params.studentId = userId;
        console.log("请求学生课表参数:", params);
        res = await getStudentWeeklySchedule(params);
      } else if (props.role === 'teacher') {
        params.teacherId = userId;
        console.log("请求教师课表参数:", params);
        res = await getTeacherWeeklySchedule(params);
      } else {
        ElMessage.warning('当前角色不支持查看课表');
        loadingSchedule.value = false;
        return;
      }
    } catch (error) {
      console.error("请求课表接口失败:", error);
      ElMessage.error('获取课表数据失败，请稍后再试');
      loadingSchedule.value = false;
      return;
    }

    console.log("课表API响应:", res);

    // 检查并处理各种可能的响应格式
    if (res && res.schedules && Array.isArray(res.schedules)) {
      // 标准格式，直接使用
      scheduleList.value = res.schedules;
      console.log("成功加载处理后的课表数据，共", scheduleList.value.length, "条记录");
    } else if (res && Array.isArray(res)) {
      // 返回了数组，直接使用
      scheduleList.value = res;
      console.log("API返回了数组格式的课表数据，已直接使用");
    } else if (res && res.error) {
      // 服务器返回了错误信息
      console.warn("服务器返回错误:", res.error);
      ElMessage.warning(`服务器返回错误: ${res.error}`);
      scheduleList.value = [];
    } else if (res && typeof res === 'object') {
      // 尝试从对象中提取课表数据
      console.warn("课表响应格式不标准，尝试从对象中提取数据");

      // 查找可能包含课表数据的字段
      const possibleArrays = Object.entries(res)
          .filter(([key, value]) => Array.isArray(value))
          .sort(([, a], [, b]) => b.length - a.length); // 按数组长度降序排序

      if (possibleArrays.length > 0) {
        const [key, value] = possibleArrays[0];
        console.log(`找到可能的课表数组字段: ${key}，包含 ${value.length} 条记录`);
        scheduleList.value = value;
      } else {
        console.warn("无法从响应中找到课表数组数据");
        scheduleList.value = [];
        ElMessage.warning('未能识别课表数据格式');
      }
    } else {
      console.warn("课表响应格式不符合预期或无数据:", res);
      ElMessage.warning('未能加载课表数据，可能当前学期无课表安排');
      scheduleList.value = [];
    }

    // 如果成功获取到数据，但数组为空
    if (scheduleList.value.length === 0) {
      console.log("获取到的课表数据为空");
      ElMessage.info('本学期没有课表安排');
    } else {
      console.log("处理后的课表数据(前3项示例):",
          scheduleList.value.slice(0, 3).map(item => ({
            id: item.id,
            courseName: item.courseName,
            weekday: item.weekday,
            startSlot: item.startSlot,
            endSlot: item.endSlot
          }))
      );
    }
  } catch (error) {
    console.error("获取课表失败:", error);
    ElMessage.error('获取课表数据失败，请稍后再试');
    scheduleList.value = [];
  } finally {
    loadingSchedule.value = false;
  }
};


// fetchTerms remains largely the same, ensure it uses the correct term identifier (termCode)
const fetchTerms = async () => {
  loadingSemesters.value = true;
  try {
    const res = await getAllTerms(); // Assumes API endpoint is /api/common/terms
    console.log("学期列表响应:", res);

    // Check multiple possible structures for term data
    let termsData = [];
    if (res && res.data && Array.isArray(res.data)) {
      termsData = res.data;
    } else if (Array.isArray(res)) {
      termsData = res;
    } else {
      // Fallback/Demo data if API fails
      console.warn("未能从API获取学期数据，使用默认值");
      termsData = [
        {code: "2023-2024-1", displayName: "2023-2024学年第一学期"},
        {code: "2023-2024-2", displayName: "2023-2024学年第二学期"}
      ];
    }

    semestersRef.value = termsData.map(term => ({
      // Standardize the keys used by the <el-option>
      code: term.code || term.termCode || term.id, // Prefer 'code' as used in backend query
      displayName: term.displayName || term.name || term.termName || `学期 ${term.id || term.code}`
    }));


    if (semestersRef.value.length > 0) {
      console.log("第一个学期数据(处理后):", JSON.stringify(semestersRef.value[0]));
      // Set default semester using the 'code' field if not already set
      if (!semester.value && semestersRef.value[0].code) {
        semester.value = semestersRef.value[0].code;
        console.log("自动选择学期:", semester.value);
        fetchSchedule(); // Fetch schedule for the default semester
      } else if (semester.value) {
        // If semester was already selected (e.g. from route params), fetch schedule
        fetchSchedule();
      }
    } else {
      console.warn("未找到有效的学期数据");
      ElMessage.warning('未找到学期信息');
    }
  } catch (error) {
    console.error("获取学期列表失败:", error);
    ElMessage.error('获取学期列表失败');
    // Fallback/Demo data on error
    semestersRef.value = [
      {code: "2023-2024-1", displayName: "2023-2024学年第一学期"},
      {code: "2023-2024-2", displayName: "2023-2024学年第二学期"}
    ];
    if (!semester.value && semestersRef.value.length > 0) {
      semester.value = semestersRef.value[0].code;
      fetchSchedule();
    }
  } finally {
    loadingSemesters.value = false;
  }
};


// fetchTimeSlots - still needed for rendering the time column in week view
const fetchTimeSlots = async () => {
  loadingTimeSlots.value = true;
  try {
    const res = await getTimeSlots(); // Assumes API endpoint is /api/common/time-slots
    console.log("时间段响应:", res);

    let slotsData = [];
    if (res && res.data && Array.isArray(res.data)) {
      slotsData = res.data;
    } else if (Array.isArray(res)) {
      slotsData = res;
    }

    if (slotsData.length > 0) {
      // Map backend slots (potentially 5) to the 10-slot structure needed for UI grid
      // This assumes the backend getTimeSlots returns start/end times that can be mapped,
      // OR we just use the default 10 slots for the grid display regardless of backend API.
      // Let's stick to using defaultTimeSlots for grid display for simplicity,
      // as the backend calculation now handles the actual 1-10 slot mapping for courses.
      timeSlotsRef.value = defaultTimeSlots; // Use the fixed 10-slot definition for UI grid
      console.log("使用预定义的10个时间槽进行UI渲染");

    } else {
      console.warn("未能获取时间段数据，使用默认值渲染UI");
      timeSlotsRef.value = defaultTimeSlots;
    }
  } catch (error) {
    console.error("获取时间段失败:", error);
    timeSlotsRef.value = defaultTimeSlots; // Use default on error
    ElMessage.error('获取时间段信息失败');
  } finally {
    loadingTimeSlots.value = false;
  }
};

// fetchWeekdays - still needed for rendering the day columns in week view
const fetchWeekdays = async () => {
  try {
    const res = await getWeekdays(); // Assumes API endpoint is /api/common/weekdays
    console.log("星期定义响应:", res);

    let weekdaysData = [];
    if (res && res.data && Array.isArray(res.data)) {
      weekdaysData = res.data;
    } else if (Array.isArray(res)) {
      weekdaysData = res;
    }

    if (weekdaysData.length > 0) {
      // Map backend data to the {value, label} format needed by the template
      weekdaysRef.value = weekdaysData.map(day => ({
        value: day.id || day.value, // Backend might use 'id' or 'value'
        label: day.name || day.label // Backend might use 'name' or 'label'
      })).sort((a, b) => a.value - b.value); // Ensure sorted 1-7

    } else {
      console.warn("未能获取星期定义，使用默认值");
      weekdaysRef.value = defaultWeekdays;
    }
  } catch (error) {
    console.error("获取星期定义失败:", error);
    weekdaysRef.value = defaultWeekdays; // Use default on error
    ElMessage.error('获取星期信息失败');
  }
};


// Simplified findCourseForCell - uses backend provided fields
const findCourseForCell = (weekday, timeSlot) => {
  if (!scheduleList.value || scheduleList.value.length === 0) {
    return null;
  }
  // Find the first course matching the day and whose slot range includes the target slot
  return scheduleList.value.find(course =>
      course.weekday === weekday &&
      course.startSlot <= timeSlot &&
      course.endSlot >= timeSlot
  );
};

// Simplified isCellCovered - uses backend provided fields
const isCellCovered = (weekday, timeSlot) => {
  if (timeSlot === 1) return false; // First slot is never covered
  if (!scheduleList.value) return false;

  // Check if any course starts *before* this slot and ends *at or after* this slot on the same day
  return scheduleList.value.some(course =>
      course.weekday === weekday &&
      course.startSlot < timeSlot && // Starts before the current slot
      course.endSlot >= timeSlot    // Ends at or after the current slot
  );
};

// Simplified getGridCellStyle - uses backend provided fields
const getGridCellStyle = (weekday, timeSlot) => {
  const course = findCourseForCell(weekday, timeSlot);
  if (!course) return {};

  // Apply span only if the current cell is the *start* of the course
  if (course.startSlot === timeSlot) {
    const slotSpan = Math.max(1, course.endSlot - course.startSlot + 1);
    // console.log(`课程 ${course.courseName} 跨度 ${slotSpan} 从 ${course.startSlot} 到 ${course.endSlot}`);
    const courseColor = getCourseColor(course.courseName || course.courseId?.toString() || course.id?.toString());
    return {
      'grid-row': `span ${slotSpan}`,
      'background-color': courseColor + '33', // 添加透明度
      'border-left': `4px solid ${courseColor}`
    };
  }
  // Otherwise, return empty style (cell will be hidden by v-show="!isCellCovered")
  return {};
};

// cellClickHandler remains the same, relies on findCourseForCell
const cellClickHandler = (weekday, timeSlot) => {
  const course = findCourseForCell(weekday, timeSlot);
  if (course) {
    console.log("课程点击:", course);
    showCourseDetail(course);
  }
};

// Simplified showCourseDetail - directly uses the course object from backend
const showCourseDetail = (course) => {
  // The course object from scheduleList should already have all necessary formatted fields
  currentCourse.value = course;
  console.log("显示课程详情 (使用后端数据):", currentCourse.value);
  dialogVisible.value = true;
};


// getCourseColor remains the same
const getCourseColor = (courseIdentifier) => {
  const colors = [
    '#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1',
    '#eb2f96', '#2f54eb', '#13c2c2', '#a0d911', '#fa8c16'
  ];
  let hash = 0;
  const identifierString = String(courseIdentifier || ''); // Ensure it's a string

  if (!identifierString) return colors[0];

  for (let i = 0; i < identifierString.length; i++) {
    hash = identifierString.charCodeAt(i) + ((hash << 5) - hash);
  }

  return colors[Math.abs(hash) % colors.length];
};

// --- Lifecycle Hooks ---
onMounted(() => {
  // Fetch basic UI structure data first
  fetchTimeSlots();
  fetchWeekdays();
  // Then fetch terms, which triggers fetching the schedule
  fetchTerms();
});
</script>