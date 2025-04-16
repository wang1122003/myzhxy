<template>
  <div class="schedule-management-container">
    <div class="page-header">
      <h2>课表管理</h2>
      <div>
        <!-- 可以添加自动排课等按钮 -->
        <el-button type="warning" @click="handleAutoSchedule">自动排课</el-button>
        <el-button type="primary" @click="handleAddScheduleItem">手动添加</el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchParams" @submit.prevent="fetchSchedule">
        <el-form-item label="学期">
          <el-select
              v-model="searchParams.semester"
              :loading="loadingSemesters"
              filterable
              placeholder="选择学期"
              required
              style="width: 200px;"
              @change="fetchSchedule"
          >
            <el-option
                v-for="item in semestersRef"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教师">
          <el-input v-model="searchParams.teacherName" clearable placeholder="教师姓名"/>
        </el-form-item>
        <el-form-item label="课程">
          <el-input v-model="searchParams.courseName" clearable placeholder="课程名称/代码"/>
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="searchParams.className" clearable placeholder="班级名称"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchSchedule">查询课表</el-button>
        </el-form-item>
        <el-form-item label="视图">
          <el-radio-group v-model="viewType">
            <el-radio-button label="week">周视图</el-radio-button>
            <el-radio-button label="list">列表视图</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-loading="loadingSchedule || loadingTimeSlots || loadingWeekdays" class="schedule-card">
      <!-- 周视图 -->
      <div v-if="viewType === 'week'" class="week-view">
        <div class="time-column">
          <div class="header-cell">时间</div>
          <div v-for="time in timeSlotsRef" :key="time.slot" class="time-cell">
            {{ time.label }}
            <div class="time-range">{{ time.startTime }} - {{ time.endTime }}</div>
          </div>
        </div>
        <div v-for="day in weekdaysRef" :key="day.value" class="day-column">
          <div class="header-cell">{{ day.label }}</div>
          <div
              v-for="time in timeSlotsRef"
              :key="`${day.value}-${time.slot}`"
              :class="{ 'has-course': findCoursesForCell(day.value, time).length > 0 }"
              class="schedule-cell"
          >
            <!-- 管理员视图可能一个格子有多门课 -->
            <div v-for="course in findCoursesForCell(day.value, time)" :key="course.id"
                 :style="{ backgroundColor: getCourseColor(course.courseId) }"
                 class="course-item admin-course-item"
                 @click="showCourseDetail(course)"
            >
              <div class="course-name">{{ course.courseName }}</div>
              <div class="course-detail">{{ course.teacherName }} / {{ course.classroom }}</div>
              <div class="course-detail">{{ course.className }}</div>
            </div>
            <!-- 可以添加按钮用于在该时间段添加课程 -->
            <el-button v-if="findCoursesForCell(day.value, time).length === 0"
                       :icon="Plus"
                       circle
                       class="add-in-cell-btn"
                       size="small"
                       type="primary"
                       @click="handleAddScheduleItemInCell(day.value, time)"/>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="list-view">
        <el-table :data="scheduleList" border style="width: 100%">
          <el-table-column label="课程名称" prop="courseName"/>
          <el-table-column label="授课教师" prop="teacherName"/>
          <el-table-column label="班级" prop="className"/>
          <el-table-column label="星期" prop="weekday">
            <template #default="scope">
              {{ formatWeekday(scope.row.weekday) }}
            </template>
          </el-table-column>
          <el-table-column label="开始时间" prop="startTime"/>
          <el-table-column label="结束时间" prop="endTime"/>
          <el-table-column label="教室" prop="classroom"/>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" type="primary" @click="handleEditScheduleItem(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDeleteScheduleItem(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <!-- 分页 (列表视图可能需要) -->
      <div v-if="viewType === 'list' && total > 0" class="pagination-container">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 课程详情/编辑/添加 对话框 (占位符) -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
    >
      <p>手动添加/编辑课表项表单待实现...</p>
      <!-- 表单需要选择课程、教师、班级、时间、教室等 -->
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitScheduleForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {
  ElButton,
  ElCard,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElRadioButton,
  ElRadioGroup,
  ElSelect,
  ElTable,
  ElTableColumn
} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import {deleteSchedule, getScheduleList} from '@/api/schedule'; // 移除 addSchedule, updateSchedule
import {getTerms, getTimeSlots, getWeekdays} from '@/api/common';

// 修改组件名称为多词组合
defineOptions({
  name: 'ScheduleManagement'
})

const loadingSchedule = ref(false);
const loadingSemesters = ref(false);
const loadingTimeSlots = ref(false);
const loadingWeekdays = ref(false);

const scheduleList = ref([]); // 列表视图数据或周视图的原始数据
const viewType = ref('week');
const semestersRef = ref([]);
const timeSlotsRef = ref([]);
const weekdaysRef = ref([]);

const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(50); // 列表视图默认每页数量

const searchParams = reactive({
  semester: null,
  teacherName: '',
  courseName: '',
  className: ''
});

const dialogVisible = ref(false);
const dialogTitle = ref('添加课表项');

// 获取公共数据：学期、时间段、星期
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
    semestersRef.value = termsRes.data || [];
    timeSlotsRef.value = timeSlotsRes.data || [];
    weekdaysRef.value = weekdaysRes.data || [];

    if (semestersRef.value.length > 0 && !searchParams.semester) {
      searchParams.semester = semestersRef.value[0].value; // 默认选中第一个学期
      await fetchSchedule(); // 获取默认学期的课表
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

// 获取课表数据
const fetchSchedule = async () => {
  if (!searchParams.semester) {
    ElMessage.warning('请先选择学期');
    return;
  }
  loadingSchedule.value = true;
  try {
    const params = {
      semester: searchParams.semester,
      teacherName: searchParams.teacherName || null,
      courseName: searchParams.courseName || null,
      className: searchParams.className || null,
      // 如果是列表视图，传递分页参数
      page: viewType.value === 'list' ? currentPage.value : null,
      size: viewType.value === 'list' ? pageSize.value : null,
    };
    // 移除 null 参数
    Object.keys(params).forEach(key => params[key] == null && delete params[key]);

    const response = await getScheduleList(params); // 使用 getScheduleList
    if (viewType.value === 'list') {
      scheduleList.value = response.data.list || [];
      total.value = response.data.total || 0;
    } else {
      // 周视图需要全部数据，假设API能返回全部或需要调整API
      scheduleList.value = response.data.list || response.data || [];
      total.value = 0; // 周视图不分页
    }

    // 分配颜色 (周视图用)
    if (viewType.value === 'week') {
      Object.keys(courseColors).forEach(key => delete courseColors[key]);
      scheduleList.value.forEach(schedule => {
        if (schedule.courseId && !courseColors[schedule.courseId]) {
          const colorIndex = Object.keys(courseColors).length % colorPalette.length;
          courseColors[schedule.courseId] = colorPalette[colorIndex];
        }
      });
    }

  } catch (error) {
    console.error('获取课表失败', error);
    ElMessage.error('获取课表失败');
    scheduleList.value = [];
    total.value = 0;
  } finally {
    loadingSchedule.value = false;
  }
};

// --- 周视图相关方法 ---
const weekdayMap = computed(() => {
  const map = {};
  weekdaysRef.value.forEach(day => {
    map[day.value] = day.label;
  });
  return map;
});
const formatWeekday = (weekdayValue) => weekdayMap.value[weekdayValue] || '';

const timeToMinutes = (timeStr) => {
  if (!timeStr || !timeStr.includes(':')) return 0;
  const [hours, minutes] = timeStr.split(':').map(Number);
  return hours * 60 + minutes;
};

// 查找周视图单元格对应的所有课程
const findCoursesForCell = (dayValue, timeSlot) => {
  if (!timeSlot || !timeSlot.startTime) return [];
  const slotStartMinutes = timeToMinutes(timeSlot.startTime);
  return scheduleList.value.filter(schedule => {
    return schedule.weekday === dayValue && timeToMinutes(schedule.startTime) === slotStartMinutes;
  });
};

const getCourseColor = (courseId) => courseColors[courseId] || '#409EFF';

// --- 列表视图分页 ---
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchSchedule();
};
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchSchedule();
};

// --- 操作按钮 --- 
const handleAutoSchedule = () => {
  ElMessage.info('自动排课功能待实现');
  // 可能需要弹出对话框设置参数，然后调用自动排课API
};

const handleAddScheduleItem = () => {
  dialogTitle.value = '手动添加课表项';
  // 清空表单
  dialogVisible.value = true;
  ElMessage.info('手动添加课表项功能待实现');
};

const handleAddScheduleItemInCell = (day, time) => {
  dialogTitle.value = `添加 ${formatWeekday(day)} ${time.label} 课表项`;
  // 预填表单的星期和时间
  dialogVisible.value = true;
  ElMessage.info('在单元格添加课表项功能待实现');
}

const handleEditScheduleItem = (row) => {
  dialogTitle.value = '编辑课表项';
  // 填充表单
  dialogVisible.value = true;
  ElMessage.info(`编辑课表项 ${row.courseName} 功能待实现`);
};

const handleDeleteScheduleItem = (row) => {
  ElMessageBox.confirm(`确定要删除 ${row.courseName} 这条课表安排吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
        type: 'warning'
  }).then(async () => {
    try {
      await deleteSchedule(row.id); // 调用删除API
      ElMessage.success('删除成功');
      fetchSchedule(); // 刷新列表
    } catch (error) {
      console.error("删除课表项失败", error);
      ElMessage.error("删除课表项失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const showCourseDetail = (course) => {
  // 管理员视图可能不需要详情弹窗，或者弹窗内容不同
  ElMessage.info(`查看课程 ${course.courseName} 详情（管理员视图）`);
  // 可以复用之前的对话框，但传递不同的数据或调整显示
  // currentCourse.value = course;
  // dialogVisible.value = true;
};

const submitScheduleForm = () => {
  // 表单验证和提交逻辑 (addSchedule / updateSchedule)
  dialogVisible.value = false;
  ElMessage.info('提交课表项表单功能待实现');
}

// 组件挂载
onMounted(() => {
  fetchInitialData();
});

</script>

<style scoped>
.schedule-management-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}
.schedule-card {
  min-height: 400px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  text-align: right;
}

/* 周视图样式 (复用并微调) */
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
  height: 100px; /* 管理员视图格子可能需要更高 */
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
  height: 100px; /* 对应 time-cell 高度 */
  box-sizing: border-box;
  position: relative; /* 为了添加按钮定位 */
}

.course-item {
  border-radius: 4px;
  padding: 3px 5px;
  font-size: 11px; /* 字号减小 */
  color: #fff;
  cursor: pointer;
  overflow: hidden;
  margin-bottom: 2px; /* 多个课程项之间的间距 */
  text-align: left; /* 左对齐 */
}

.admin-course-item { /* 管理员视图特定样式 */
  position: relative; /* 改为相对定位，使其在单元格内堆叠 */
}

.course-name {
  font-weight: bold;
}

.course-detail {
  font-size: 10px;
  line-height: 1.2;
}

.add-in-cell-btn {
  position: absolute;
  bottom: 5px;
  right: 5px;
  opacity: 0.5; /* 默认半透明 */
  transition: opacity 0.2s;
}

.schedule-cell:hover .add-in-cell-btn {
  opacity: 1; /* 悬停时完全显示 */
}

/* 列表视图 */
.list-view {
  width: 100%;
}

</style> 