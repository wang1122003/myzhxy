<template>
  <div class="schedule-container">
    <div class="page-header">
      <h2>我的课表</h2>
      <div class="filter-container">
        <el-select
            v-model="semester"
            placeholder="选择学期"
            style="margin-right: 10px;"
            :loading="loadingSemesters"
            filterable
            @change="fetchSchedule"
        >
          <el-option
              v-for="item in semestersRef"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
        <el-radio-group
            v-model="viewType"
            style="margin-left: 20px;"
        >
          <el-radio-button label="week">
            周视图
          </el-radio-button>
          <el-radio-button label="list">
            列表视图
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <el-card
        v-loading="loadingSchedule || loadingTimeSlots || loadingWeekdays"
        class="schedule-card"
    >
      <!-- 周视图 -->
      <div
          v-if="viewType === 'week'"
          class="week-view"
      >
        <div class="time-column">
          <div class="header-cell">
            时间
          </div>
          <div
              v-for="time in timeSlotsRef"
              :key="time.slot"
              class="time-cell"
          >
            {{ time.label }}
            <div class="time-range">
              {{ time.startTime }} - {{ time.endTime }}
            </div>
          </div>
        </div>
        <div
            v-for="day in weekdaysRef"
            :key="day.value"
            class="day-column"
        >
          <div class="header-cell">
            {{ day.label }}
          </div>
          <div
              v-for="time in timeSlotsRef"
              :key="`${day.value}-${time.slot}`"
              :class="{ 'has-course': findCourseForCell(day.value, time) }"
              class="schedule-cell"
          >
            <div
                v-if="findCourseForCell(day.value, time)"
                :style="{ backgroundColor: getCourseColor(findCourseForCell(day.value, time).courseId) }"
                class="course-item"
                @click="showCourseDetail(findCourseForCell(day.value, time))"
            >
              <div class="course-name">
                {{ findCourseForCell(day.value, time).courseName }}
              </div>
              <div class="course-location">
                {{ findCourseForCell(day.value, time).classroom }}
              </div>
              <!-- 可以考虑显示班级 -->
              <!-- <div class="course-class">{{ findCourseForCell(day.value, time).className }}</div> -->
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div
          v-else
          class="list-view"
      >
        <el-table
            :data="scheduleList"
            border
            style="width: 100%"
        >
          <el-table-column
              label="课程名称"
              prop="courseName"
          />
          <!-- <el-table-column label="授课教师" prop="teacherName"/> --> <!-- 教师课表可能不需要显示自己名字 -->
          <el-table-column
              label="星期"
              prop="weekday"
          >
            <template #default="scope">
              {{ formatWeekday(scope.row.weekday) }}
            </template>
          </el-table-column>
          <el-table-column
              label="开始时间"
              prop="startTime"
          />
          <el-table-column
              label="结束时间"
              prop="endTime"
          />
          <el-table-column
              label="教室"
              prop="classroom"
          />
          <el-table-column
              label="班级"
              prop="className"
          /> <!-- 教师课表显示班级 -->
          <el-table-column
              label="操作"
              width="100"
          >
            <template #default="scope">
              <el-button
                  size="small"
                  type="primary"
                  @click="showCourseDetail(scope.row)"
              >
                详情
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
        <el-descriptions
            :column="1"
            border
            title=""
        >
          <el-descriptions-item label="课程名称">
            {{ currentCourse.courseName }}
          </el-descriptions-item>
          <!-- <el-descriptions-item label="授课教师">{{ currentCourse.teacherName }}</el-descriptions-item> -->
          <el-descriptions-item label="上课时间">
            {{ formatWeekday(currentCourse.weekday) }} {{ currentCourse.startTime }} - {{ currentCourse.endTime }}
          </el-descriptions-item>
          <el-descriptions-item label="上课地点">
            {{ currentCourse.classroom }}
          </el-descriptions-item>
          <el-descriptions-item label="学分">
            {{ currentCourse.credit }}
          </el-descriptions-item>
          <el-descriptions-item label="授课班级">
            {{ currentCourse.className }}
          </el-descriptions-item> <!-- 显示班级 -->
          <!-- 未来可添加学生列表等 -->
        </el-descriptions>
      </template>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {computed, onMounted, ref} from 'vue';
import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElDialog,
  ElMessage,
  ElOption,
  ElRadioButton,
  ElRadioGroup,
  ElSelect,
  ElTable,
  ElTableColumn
} from 'element-plus';
import {getTeacherSchedule} from '@/api/schedule'; // 引入教师课表 API
import {getTerms, getTimeSlots, getWeekdays} from '@/api/common';

export default {
  name: 'TeacherScheduleView', // 修改组件名
  components: {
    // 注册 Element Plus 组件
    ElCard,
    ElTable,
    ElTableColumn,
    ElSelect,
    ElOption,
    ElRadioGroup,
    ElRadioButton,
    ElButton,
    ElDialog,
    ElDescriptions,
    ElDescriptionsItem
  },
  setup() {
    const loadingSchedule = ref(false);
    const loadingSemesters = ref(false);
    const loadingTimeSlots = ref(false);
    const loadingWeekdays = ref(false);

    const scheduleList = ref([]);
    const viewType = ref('week');
    const semester = ref(null);
    const dialogVisible = ref(false);
    const currentCourse = ref(null);

    const semestersRef = ref([]);
    const timeSlotsRef = ref([]);
    const weekdaysRef = ref([]);

    const fetchInitialData = async () => {
      loadingSemesters.value = true;
      loadingTimeSlots.value = true;
      loadingWeekdays.value = true;
      try {
        const [termsRes, timeSlotsRes, weekdaysRes] = await Promise.all([
          getTerms(),
          getTimeSlots(),
          getWeekdays()
        ]);

        semestersRef.value = termsRes.data;
        timeSlotsRef.value = timeSlotsRes.data;
        weekdaysRef.value = weekdaysRes.data;

        if (semestersRef.value.length > 0) {
          semester.value = semestersRef.value[0].value;
          await fetchSchedule();
        }
      } catch (error) {
        console.error('获取初始数据失败', error);
        ElMessage.error('获取学期/时间段/星期数据失败');
      } finally {
        loadingSemesters.value = false;
        loadingTimeSlots.value = false;
        loadingWeekdays.value = false;
      }
    };

    const courseColors = {};
    const colorPalette = [
      '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
      '#3ECBBC', '#EA7C69', '#AA68CA', '#68C0CA', '#CA6897'
    ];

    const fetchSchedule = async () => {
      if (!semester.value) return;
      loadingSchedule.value = true;
      try {
        const response = await getTeacherSchedule({semester: semester.value}); // 调用教师课表 API
        scheduleList.value = response.data || [];

        // 重置并分配颜色
        Object.keys(courseColors).forEach(key => delete courseColors[key]);
        scheduleList.value.forEach(schedule => {
          if (schedule.courseId && !courseColors[schedule.courseId]) {
            const colorIndex = Object.keys(courseColors).length % colorPalette.length;
            courseColors[schedule.courseId] = colorPalette[colorIndex];
          }
        });
      } catch (error) {
        console.error('获取课表失败', error);
        ElMessage.error('获取课表失败');
        scheduleList.value = [];
      } finally {
        loadingSchedule.value = false;
      }
    };

    const weekdayMap = computed(() => {
      const map = {};
      weekdaysRef.value.forEach(day => {
        map[day.value] = day.label;
      });
      return map;
    });
    const formatWeekday = (weekdayValue) => {
      return weekdayMap.value[weekdayValue] || '';
    };

    const timeToMinutes = (timeStr) => {
      if (!timeStr || !timeStr.includes(':')) return 0;
      const [hours, minutes] = timeStr.split(':').map(Number);
      return hours * 60 + minutes;
    };

    const findCourseForCell = (dayValue, timeSlot) => {
      if (!timeSlot || !timeSlot.startTime || !timeSlot.endTime) return null;

      const slotStartMinutes = timeToMinutes(timeSlot.startTime);

      return scheduleList.value.find(schedule => {
        if (schedule.weekday !== dayValue) {
          return false;
        }
        const courseStartMinutes = timeToMinutes(schedule.startTime);
        // 教师课表可能一个时间段有多个班级，这里简化为只找第一个匹配的
        // 如果需要显示合并单元格，逻辑会更复杂
        return courseStartMinutes === slotStartMinutes;
      });
    };

    const getCourseColor = (courseId) => {
      return courseColors[courseId] || '#409EFF';
    };

    const showCourseDetail = (course) => {
      currentCourse.value = course;
      dialogVisible.value = true;
    };

    onMounted(() => {
      fetchInitialData();
    });

    return {
      loadingSchedule,
      loadingSemesters,
      loadingTimeSlots,
      loadingWeekdays,
      scheduleList,
      viewType,
      semester,
      semestersRef,
      timeSlotsRef,
      weekdaysRef,
      dialogVisible,
      currentCourse,
      fetchSchedule,
      formatWeekday,
      findCourseForCell,
      getCourseColor,
      showCourseDetail
    };
  }
};
</script>

<style scoped>
/* 样式与 student/Schedule.vue 基本相同 */
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
  min-height: 400px;
}
.week-view {
  display: flex;
  border-left: 1px solid #ebeef5;
  border-top: 1px solid #ebeef5;
}

.time-column,
.day-column {
  display: flex;
  flex-direction: column;
}

.time-column {
  flex: 0 0 100px;
}

.day-column {
  flex: 1;
}

.header-cell,
.time-cell,
.schedule-cell {
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  text-align: center;
  position: relative;
}
.header-cell {
  background-color: #fafafa;
  font-weight: bold;
  padding: 10px 5px;
  height: 50px;
  box-sizing: border-box;
}
.time-cell {
  height: 80px;
  padding: 5px;
  font-size: 12px;
  color: #909399;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
}
.time-range {
  font-size: 10px;
  margin-top: 4px;
}
.schedule-cell {
  height: 80px;
  box-sizing: border-box;
}
.course-item {
  position: absolute;
  top: 2px;
  left: 2px;
  right: 2px;
  bottom: 2px;
  border-radius: 4px;
  padding: 5px;
  font-size: 12px;
  color: #fff;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}
.course-item:hover {
  transform: scale(1.05);
  z-index: 10;
}
.course-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.course-location {
  font-size: 10px;
}
.list-view {
  width: 100%;
}

.dialog-footer {
  text-align: right;
}
</style> 