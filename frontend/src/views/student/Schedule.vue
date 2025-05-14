<template>
  <PageContainer :title="pageTitle">
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
      <div v-if="viewType === 'week'" class="week-view-wrapper">
        <div v-if="!scheduleList || scheduleList.length === 0">
          <el-empty description="当前学期暂无课表数据"/>
        </div>
        <div v-else class="week-view">
          <div class="time-column">
            <div class="header-cell">时间</div>
            <div v-for="time in timeSlotsRef" :key="time.slot" class="time-cell">
              <div class="time-slot-label">第{{ time.slot }}节</div>
              <div class="time-range">{{ time.startTime }} - {{ time.endTime }}</div>
            </div>
          </div>
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
                <div v-if="findCourseForCell(day.value, time.slot).teacherName && showTeacherInfoFlag"
                     class="course-teacher">
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

    <DialogWrapper
        v-model:visible="dialogVisible"
        title="课程详情"
        width="500px"
    >
      <template v-if="currentCourse">
        <div class="course-details">
          <p><strong>课程名称:</strong> {{ currentCourse.courseName }}</p>
          <p v-if="currentCourse.teacherName"><strong>教师:</strong> {{ currentCourse.teacherName }}</p>
          <p v-if="currentCourse.classroom"><strong>教室:</strong> {{ currentCourse.classroom }}
            {{ currentCourse.building }}</p>
          <p><strong>时间:</strong>
            第{{ currentCourse.startWeek }}周-第{{ currentCourse.endWeek }}周
            {{ dayMap[currentCourse.dayOfWeek] }}
            {{ currentCourse.startTime }}-{{ currentCourse.endTime }}
          </p>
          <p v-if="currentCourse.credit"><strong>学分:</strong> {{ currentCourse.credit }}</p>
          <p v-if="currentCourse.courseType"><strong>类型:</strong> {{ courseTypeMap[currentCourse.courseType] }}</p>
        </div>
      </template>
    </DialogWrapper>
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue';
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
import {useUserStore} from '@/stores/userStore';
import {getStudentSchedule, getTeacherSchedule} from '@/api/schedule'; // getTeacherSchedule might not be needed here
import {getAllTerms as getAllSemesters, getCurrentTerm as getCurrentSemester} from '@/api/term';
import {getTimeSlots} from '@/api/common';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import TableView from '@/views/ui/TableView.vue';
import DialogWrapper from '@/views/ui/DialogWrapper.vue';

// Configuration for this StudentScheduleView
const currentRole = 'student';
const pageTitle = '我的课表';
const showTeacherInfoFlag = true;

const userStore = useUserStore();
const viewType = ref('week');
const semester = ref(null);
const semestersRef = ref([]);
const scheduleList = ref([]);
const timeSlotsRef = ref([]);
const weekdaysRef = ref([
  {label: '星期一', value: 1},
  {label: '星期二', value: 2},
  {label: '星期三', value: 3},
  {label: '星期四', value: 4},
  {label: '星期五', value: 5},
  {label: '星期六', value: 6},
  {label: '星期日', value: 7}
]);

const loadingSchedule = ref(false);
const loadingSemesters = ref(false);
const loadingTimeSlots = ref(false);

const dialogVisible = ref(false);
const currentCourse = ref(null);

const getDayName = (dayValue) => weekdaysRef.value.find(d => d.value === dayValue)?.label || '';

const fetchSemesters = async () => {
  loadingSemesters.value = true;
  try {
    const allSemestersRes = await getAllSemesters(); // 假设返回数组
    // 检查 allSemestersRes 是否为数组
    if (Array.isArray(allSemestersRes)) {
      semestersRef.value = allSemestersRes.map(s => ({
        ...s,
        displayName: s.termName || s.name || `学期ID: ${s.id || s.code || '未知'}`
      }));
    } else {
      console.error("Invalid response structure from getAllSemesters (expected array):", allSemestersRes);
      semestersRef.value = [];
      ElMessage.error('获取学期列表格式错误');
    }

    const currentSemRes = await getCurrentSemester(); // 假设返回对象
    // 检查 currentSemRes 是否为有效对象 且 semestersRef 已填充
    if (currentSemRes && typeof currentSemRes === 'object' && !Array.isArray(currentSemRes) && semestersRef.value.length > 0) {
      const currentCode = currentSemRes.code || currentSemRes.id || currentSemRes.termId;
      if (currentCode && semestersRef.value.some(s => (s.code || s.id || s.termId) === currentCode)) {
        semester.value = currentCode;
      } else {
        console.warn("当前学期数据:", currentSemRes);
        console.warn("学期列表:", semestersRef.value);
        console.warn("当前学期未在获取到的学期列表中找到，请手动选择。");
        // 可选：如果需要，可以在此处设置默认值，例如列表的第一个学期
        // if (!semester.value && semestersRef.value.length > 0) {
        //   semester.value = semestersRef.value[0].code || semestersRef.value[0].id || semestersRef.value[0].termId;
        // }
      }
    } else {
      // 如果 currentSemRes 无效或 semestersRef 为空，则打印警告
      console.warn("未能获取到有效的当前学期信息或学期列表为空。", currentSemRes);
      // 同样可以考虑设置默认值
      // if (!semester.value && semestersRef.value.length > 0) {
      //    semester.value = semestersRef.value[0].code || semestersRef.value[0].id || semestersRef.value[0].termId;
      // }
    }
  } catch (error) {
    console.error("Error fetching semesters:", error);
    ElMessage.error('获取学期列表或当前学期失败');
    semestersRef.value = [];
  } finally {
    loadingSemesters.value = false;
  }
};

const fetchTimeSlots = async () => {
  loadingTimeSlots.value = true;
  try {
    const res = await getTimeSlots(); // 假设 res 是数组
    // 直接检查 res 是否为数组
    if (Array.isArray(res)) {
      // 添加对 slot 属性存在的检查和默认值，使排序更健壮
      timeSlotsRef.value = [...res].sort((a, b) => (a.slot ?? Infinity) - (b.slot ?? Infinity));
    } else {
      console.error("Invalid response structure from getTimeSlots (expected array):", res);
      timeSlotsRef.value = [];
      ElMessage.error('获取时间段信息格式错误');
    }
  } catch (error) {
    console.error("Error fetching time slots:", error);
    ElMessage.error('获取时间段信息失败');
    timeSlotsRef.value = [];
  } finally {
    loadingTimeSlots.value = false;
  }
};

const fetchSchedule = async () => {
  if (!semester.value) {
    ElMessage.warning('请先选择一个学期');
    return;
  }
  loadingSchedule.value = true;
  try {
    let apiResponse; // 用于存储 API 的原始响应
    const params = {semesterId: semester.value};
    let scheduleDataArray = []; // 用于存储最终的课表数组

    // 根据角色调用不同的 API
    const userId = userStore.userInfo?.id;
    if (!userId) {
      ElMessage.error('无法获取用户信息，无法加载课表。');
      scheduleList.value = [];
      loadingSchedule.value = false;
      return;
    }

    if (currentRole === 'student') {
      apiResponse = await getStudentSchedule(userId, params);
    } else if (currentRole === 'teacher') {
      // 虽然这是 student 文件，但保持逻辑完整性
      apiResponse = await getTeacherSchedule(userId, params);
    } else {
      ElMessage.error('无效的用户角色');
      scheduleList.value = [];
      loadingSchedule.value = false;
      return;
    }

    // --- 重点：处理不同的响应结构 ---
    if (apiResponse) {
      // 情况 1: 响应本身就是数组 (经过拦截器处理?)
      if (Array.isArray(apiResponse)) {
        scheduleDataArray = apiResponse;
      }
      // 情况 2: 响应是对象，数据在 data 属性中
      else if (apiResponse.data) {
        // 子情况 2.1: data 是数组
        if (Array.isArray(apiResponse.data)) {
          scheduleDataArray = apiResponse.data;
        }
        // 子情况 2.2: data 是对象，实际数组在 data.records 或 data.list
        else if (typeof apiResponse.data === 'object') {
          if (Array.isArray(apiResponse.data.records)) {
            scheduleDataArray = apiResponse.data.records;
          } else if (Array.isArray(apiResponse.data.list)) {
            scheduleDataArray = apiResponse.data.list;
          }
        }
      }
      // 其他情况或无法识别的结构
      if (scheduleDataArray.length === 0 && apiResponse) {
        console.warn("无法从 API 响应中提取课表数组:", apiResponse);
      }
    } else {
      console.warn("获取课表 API 返回无效响应:", apiResponse);
    }
    // --- 结束：处理不同的响应结构 ---

    // 使用提取到的数组进行 map 操作
    scheduleList.value = scheduleDataArray.map(item => ({
      ...item,
      weekdayName: getDayName(item.weekday),
      // 确保 timeSlotsRef.value 是数组再find
      timeRange: `${(Array.isArray(timeSlotsRef.value) ? timeSlotsRef.value.find(ts => ts.slot === item.startSlot)?.startTime : '') || ''} - ${(Array.isArray(timeSlotsRef.value) ? timeSlotsRef.value.find(ts => ts.slot === item.endSlot)?.endTime : '') || ''}`,
      weeksRange: Array.isArray(item.weeks) ? item.weeks.join(', ') : (item.weeks || '') // 确保 weeks 是数组
    }));

  } catch (error) {
    console.error(`Error fetching ${currentRole} schedule:`, error);
    ElMessage.error('获取课表失败');
    scheduleList.value = [];
  } finally {
    loadingSchedule.value = false;
  }
};

const findCourseForCell = (day, slot) => {
  return scheduleList.value.find(course =>
      course.weekday === day &&
      course.startSlot <= slot &&
      course.endSlot >= slot
  );
};

const isCellCovered = (day, slot) => {
  return scheduleList.value.some(course =>
      course.weekday === day &&
      course.startSlot < slot &&
      course.endSlot >= slot
  );
};

const getGridCellStyle = (day, slot) => {
  const course = findCourseForCell(day, slot);
  if (course && course.startSlot === slot) {
    const duration = course.endSlot - course.startSlot + 1;
    return {
      gridRowStart: `span ${duration}`,
      backgroundColor: getCourseColor(course.courseId),
      zIndex: 5,
      alignSelf: 'stretch'
    };
  }
  return {};
};

const courseColors = {};
const getCourseColor = (courseId) => {
  if (!courseColors[courseId]) {
    let hash = 0;
    for (let i = 0; i < (courseId?.toString().length || 0); i++) {
      hash = courseId.toString().charCodeAt(i) + ((hash << 5) - hash);
    }
    const colors = ['#E6F7FF', '#FFFBE6', '#F6FFED', '#FCF0FF', '#FFF0F0', '#E6FFF0'];
    courseColors[courseId] = colors[Math.abs(hash) % colors.length];
  }
  return courseColors[courseId];
};

const cellClickHandler = (day, slot) => {
  const course = findCourseForCell(day, slot);
  if (course) {
    showCourseDetail(course);
  }
};

const tableColumns = computed(() => [
  {prop: 'courseName', label: '课程名称', minWidth: 150},
  {prop: 'weekdayName', label: '星期', width: 100},
  {prop: 'timeRange', label: '时间', width: 150},
  {prop: 'classroomName', label: '地点', width: 120},
  {prop: 'weeksRange', label: '周次', width: 150},
  ...(showTeacherInfoFlag ? [{prop: 'teacherName', label: '教师', width: 100}] : []),
]);

const actionColumnConfig = computed(() => ({
  label: '操作',
  width: 80,
  fixed: 'right',
  buttons: [
    {label: '详情', type: 'primary', link: true, event: 'view-detail'},
  ]
}));

const showCourseDetail = (course) => {
  currentCourse.value = course;
  dialogVisible.value = true;
};

watch(semester, (newSemester, oldSemester) => {
  if (newSemester && newSemester !== oldSemester) {
    fetchSchedule();
  }
});

onMounted(async () => {
  await fetchTimeSlots();
  await fetchSemesters();
  // 如果 semester.value 在 fetchSemesters 之后被成功设置，watch 会触发 fetchSchedule
  // 添加一个保险措施：如果 watch 由于某些原因没有触发，但 semester.value 确实有值了，且课表为空，则手动调用
  if (semester.value && scheduleList.value.length === 0) {
    console.log("[onMounted] Fallback: Manually calling fetchSchedule as semester is set and schedule is empty.");
    fetchSchedule();
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

.day-column:last-child {
  border-right: none;
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

.time-column .time-cell:last-child, .day-column .schedule-cell:last-child {
  border-bottom: none;
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
  transition: all 0.2s ease-in-out;
  position: relative;
}

.schedule-cell:hover:not(:has(.course-item)) {
  background-color: #f0f9eb;
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
  transition: all 0.2s ease-in-out;
  box-sizing: border-box;
}

.course-item:hover {
  transform: scale(1.03);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.course-name {
  font-weight: bold;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.course-location, .course-weeks, .course-teacher, .course-class {
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 2px;
  opacity: 0.9;
}


.list-view {
  padding: 10px 0;
}

.dialog-footer {
  text-align: right;
}
</style>