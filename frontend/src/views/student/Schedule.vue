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
              :key="getTermId(item)"
              :label="getTermName(item)"
              :value="getTermId(item)"
          />
        </el-select>
      <el-radio-group v-model="viewType">
        <el-radio-button label="week">周视图</el-radio-button>
        <el-radio-button label="list">列表视图</el-radio-button>
        </el-radio-group>
      <el-button :icon="Refresh" :loading="loadingSchedule" circle style="margin-left: 10px;" title="刷新课表"
                 @click="fetchSchedule"></el-button>
    </template>

    <el-card v-loading="loadingSchedule || loadingSemesters" class="schedule-card">
      <!-- 周视图 -->
      <div v-if="viewType === 'week'" class="week-view-wrapper">
        <div v-if="!scheduleList || scheduleList.length === 0">
          <el-empty description="当前学期暂无课表数据"/>
        </div>
        <div v-else class="week-view">
          <!-- 时间列 -->
          <div class="time-column">
            <div class="header-cell">时间</div>
            <div v-for="time in timeSlots" :key="time.slot" class="time-cell">
              <div class="time-slot-label">第{{ time.slot }}节</div>
              <div class="time-range">{{ time.startTime }} - {{ time.endTime }}</div>
            </div>
          </div>
          <!-- 星期列 -->
          <div v-for="day in weekdays" :key="day.value" class="day-column">
            <div class="header-cell">{{ day.label }}</div>
            <div
                v-for="time in timeSlots" :key="`${day.value}-${time.slot}`"
                class="schedule-cell"
                :style="getGridCellStyle(day.value, time.slot)"
                @click="cellClickHandler(day.value, time.slot)"
            >
              <div v-if="findCourseForCell(day.value, time.slot)" class="course-item">
                <div class="course-name">
                  {{ getCourseInfo(findCourseForCell(day.value, time.slot), 'courseName', '未命名课程') }}
                </div>
                <div class="course-location">@
                  {{ getCourseInfo(findCourseForCell(day.value, time.slot), 'classroomName', '地点未知') }}
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
            {{ getCourseInfo(currentCourse, 'startTime', '').substring(0, 5) }} -
            {{ getCourseInfo(currentCourse, 'endTime', '').substring(0, 5) }}
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
      return `${start.substring(0, 5) || '?'} - ${end.substring(0, 5) || '?'}`;
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
  // Need to adjust this based on how start/end times map to slots
  // This is a simplified version assuming exact startTime match
  // A more robust way would check if timeSlot falls within course start/end section
  const targetStartTime = timeSlots.find(t => t.slot === timeSlot)?.startTime;
  if (!targetStartTime) return null;

  return scheduleList.value.find(course => {
    // 兼容不同的属性名称
    const courseDay = course.weekday || course.dayOfWeek || course.day_of_week || course.day;
    const courseStartTime = course.startTime || course.start_time;

    // 匹配星期几和时间段
    return courseDay == day && // 使用==而不是===，允许数字和字符串比较
        courseStartTime?.startsWith(targetStartTime.substring(0, 5)); // Match HH:mm
  });
  // TODO: Implement proper section mapping if backend provides startSection/endSection
};

// Get style for grid cell (row span and background color)
const getGridCellStyle = (day, timeSlot) => {
  const course = findCourseForCell(day, timeSlot);
  if (!course) return {};

  // Calculate row span based on duration (needs section info)
  // Placeholder: assume each course takes 1 slot for now
  const rowSpan = 1;
  // Example if using sections: const rowSpan = course.endSection - course.startSection + 1;

  // Only apply style to the starting cell of the course
  const startSlot = timeSlots.find(t => course.startTime?.startsWith(t.startTime.substring(0, 5)))?.slot;
  if (startSlot !== timeSlot) {
    // Hide this cell if it's covered by a multi-slot course starting earlier
    const courseAbove = findCourseForCell(day, timeSlot - 1);
    const courseAboveEndSlot = timeSlots.find(t => courseAbove?.endTime?.startsWith(t.endTime.substring(0, 5)))?.slot;
    if (courseAbove && courseAboveEndSlot >= timeSlot) {
      return {display: 'none'}; // Hide cells covered by the span
    }
    return {}; // Not the start cell, no special style
  }

  return {
    // gridRowStart: `span ${rowSpan}`, // Use gridRowStart for spanning
    backgroundColor: getCourseColor(course.courseId),
    color: '#fff',
    cursor: 'pointer'
  };
};

// 安全获取课程属性，处理字段缺失情况
const getCourseInfo = (course, property, defaultValue = '') => {
  // 不同的字段名映射
  const propertyMap = {
    'courseName': ['courseName', 'course_name', 'name', 'title'],
    'teacherName': ['teacherName', 'teacher_name', 'teacher'],
    'classroomName': ['classroomName', 'classroom_name', 'classroom', 'location', 'room'],
    'startWeek': ['startWeek', 'start_week', 'weekStart'],
    'endWeek': ['endWeek', 'end_week', 'weekEnd']
  };

  if (!course) return defaultValue;

  // 尝试所有可能的属性名
  const possibleProperties = propertyMap[property] || [property];
  for (const prop of possibleProperties) {
    if (course[prop] !== undefined && course[prop] !== null) {
      return course[prop];
    }
  }

  return defaultValue;
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

    // 增强数据解析逻辑，支持更多可能的响应格式
    if (res && res.code === 200) {
      if (Array.isArray(res.data)) {
        scheduleList.value = res.data;
        console.log('[Schedule] 成功解析课表数据(标准数组格式):', scheduleList.value.length, '条记录');
      } else if (res.data && res.data.records && Array.isArray(res.data.records)) {
        scheduleList.value = res.data.records;
        console.log('[Schedule] 成功解析课表数据(分页格式):', scheduleList.value.length, '条记录');
      } else if (res.data && typeof res.data === 'object') {
        // 如果res.data是单个对象，尝试作为课表项处理
        scheduleList.value = [res.data];
        console.log('[Schedule] 成功解析课表数据(单对象格式)');
      } else if (res.data === null || res.data === undefined) {
        // API返回成功但无数据
        scheduleList.value = [];
        console.log('[Schedule] API返回空数据');
      } else {
        console.warn('[Schedule] 未识别的res.data格式:', res.data);
        scheduleList.value = [];
      }
    } else if (Array.isArray(res)) {
      // 直接是数组的情况
      scheduleList.value = res;
      console.log('[Schedule] 成功解析课表数据(直接数组):', scheduleList.value.length, '条记录');
    } else if (res && res.list && Array.isArray(res.list)) {
      // 有些API返回 {list: [...]} 格式
      scheduleList.value = res.list;
      console.log('[Schedule] 成功解析课表数据(list数组):', scheduleList.value.length, '条记录');
    } else if (res && res.items && Array.isArray(res.items)) {
      // 有些API返回 {items: [...]} 格式
      scheduleList.value = res.items;
      console.log('[Schedule] 成功解析课表数据(items数组):', scheduleList.value.length, '条记录');
    } else if (res && res.rows && Array.isArray(res.rows)) {
      // 有些API返回 {rows: [...]} 格式
      scheduleList.value = res.rows;
      console.log('[Schedule] 成功解析课表数据(rows数组):', scheduleList.value.length, '条记录');
    } else if (res && res.success === true && res.result) {
      // 处理 {success: true, result: ...} 格式
      if (Array.isArray(res.result)) {
        scheduleList.value = res.result;
      } else if (res.result && res.result.list && Array.isArray(res.result.list)) {
        scheduleList.value = res.result.list;
      } else if (typeof res.result === 'object') {
        scheduleList.value = [res.result];
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
</script>

<style scoped>
.schedule-card {
  margin-top: 20px;
}

/* Week View Styles */
.week-view-wrapper {
  overflow-x: auto; /* Allow horizontal scrolling on smaller screens */
}
.week-view {
  display: grid;
  grid-template-columns: 80px repeat(7, 1fr); /* Time column + 7 day columns */
  border: 1px solid #ebeef5;
  min-width: 800px; /* Ensure minimum width */
}

.header-cell, .time-cell, .schedule-cell {
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  padding: 8px;
  text-align: center;
  min-height: 60px; /* Adjust height as needed */
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
  position: relative; /* Needed for absolute positioned course items if spanning */
  transition: background-color 0.2s ease;
}

.schedule-cell.has-course {
  /* background-color: #ecf5ff; */ /* Optional: subtle background for cells with courses */
}

.course-item {
  width: 100%;
  height: 100%;
  padding: 4px;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  cursor: pointer;
  box-sizing: border-box;
}

.course-name {
  font-weight: bold;
  margin-bottom: 2px;
  white-space: normal; /* Allow wrapping */
  word-break: break-all;
}

.course-location, .course-teacher, .course-weeks {
  font-size: 10px;
  opacity: 0.9;
  margin-top: 2px;
}

/* List View Styles */
.list-view {
  /* Add styles if needed */
}

.dialog-footer {
  text-align: right;
}
</style> 