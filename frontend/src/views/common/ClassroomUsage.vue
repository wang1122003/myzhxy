<template>
  <PageContainer title="教室使用情况查询">
    <!-- 查询表单 -->
    <div class="search-form card-shadow">
      <el-form
          :model="searchForm"
          inline
          label-width="85px"
          @submit.prevent="handleSearch"
      >
        <el-form-item label="查询类型">
          <el-radio-group v-model="searchForm.searchType">
            <el-radio :value="1">查询教室使用情况</el-radio>
            <el-radio :value="2">查询空闲教室</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider/>

        <!-- 共同的查询条件 -->
        <el-form-item label="学期">
          <el-select
              v-model="searchForm.termInfo"
              clearable
              placeholder="请选择学期"
              style="width: 180px"
          >
            <el-option
                v-for="item in termOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="日期">
          <el-date-picker
              v-model="searchForm.date"
              :disabled-date="disabledDate"
              format="YYYY-MM-DD"
              placeholder="选择日期"
              style="width: 180px"
              type="date"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <!-- 查询教室使用情况的专属条件 -->
        <template v-if="searchForm.searchType === 1">
          <el-form-item label="教学楼">
            <el-select
                v-model="searchForm.building"
                clearable
                placeholder="请选择教学楼"
                style="width: 180px"
                @change="handleBuildingChange"
            >
              <el-option
                  v-for="item in buildingOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="教室">
            <el-select
                v-model="searchForm.classroomId"
                :loading="loadingClassrooms"
                clearable
                filterable
                placeholder="请选择教室"
                style="width: 220px"
            >
              <el-option
                  v-for="room in classroomOptions"
                  :key="room.id"
                  :label="`${room.building || ''} ${room.name || ''}`"
                  :value="room.id"
              />
            </el-select>
          </el-form-item>
        </template>

        <!-- 查询空闲教室的专属条件 -->
        <template v-if="searchForm.searchType === 2">
          <el-form-item label="时间段">
            <el-time-select
                v-model="searchForm.startTime"
                end="21:30"
                placeholder="开始时间"
                start="08:00"
                step="00:30"
                style="width: 135px"
            />
            <span class="mx-2">至</span>
            <el-time-select
                v-model="searchForm.endTime"
                :min-time="searchForm.startTime"
                end="21:30"
                placeholder="结束时间"
                start="08:00"
                step="00:30"
                style="width: 135px"
            />
          </el-form-item>

          <el-form-item label="教学楼">
            <el-select
                v-model="searchForm.building"
                clearable
                placeholder="请选择教学楼"
                style="width: 180px"
            >
              <el-option
                  v-for="item in buildingOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="教室类型">
            <el-select
                v-model="searchForm.roomType"
                clearable
                placeholder="请选择教室类型"
                style="width: 180px"
            >
              <el-option
                  v-for="item in roomTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="最小容量">
            <el-input-number
                v-model="searchForm.minCapacity"
                :max="500"
                :min="0"
                placeholder="最小容量"
                style="width: 180px"
            />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 教室使用情况结果 -->
    <div v-if="searchForm.searchType === 1 && !loading">
      <div v-if="usageResults.classroom" class="classroom-info card-shadow">
        <h2>{{ usageResults.classroom.building }} {{ usageResults.classroom.name }}</h2>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="教室类型">
            {{ formatRoomType(usageResults.classroom.roomType) }}
          </el-descriptions-item>
          <el-descriptions-item label="容量">
            {{ usageResults.classroom.capacity }}人
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(usageResults.classroom.status)">
              {{ formatStatus(usageResults.classroom.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 周课表视图 -->
      <div v-if="hasUsageData" class="timetable-wrapper card-shadow">
        <h3>课表安排</h3>
        <div class="timetable-container">
          <table class="timetable">
            <thead>
            <tr>
              <th class="time-column">时间段</th>
              <th v-for="day in weekDays" :key="day.value">{{ day.label }}</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(slot, slotIndex) in timeSlots" :key="slotIndex">
              <td class="time-column">{{ slot.label }}</td>
              <td
                  v-for="day in weekDays"
                  :key="`${day.value}-${slotIndex}`"
                  :class="{'occupied': isSlotOccupied(day.value, slot)}"
              >
                <div v-if="getSlotInfo(day.value, slot)" class="class-info">
                  <p class="course-name">{{ getSlotInfo(day.value, slot).courseName }}</p>
                  <p class="teacher-name">{{ getSlotInfo(day.value, slot).teacherName }}</p>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

      <el-empty v-else description="暂无课表数据"/>

      <!-- 统计数据 -->
      <div v-if="usageResults.statistics" class="statistics-wrapper card-shadow">
        <h3>使用统计</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-title">总时间段</div>
              <div class="stat-value">{{ usageResults.statistics.totalSlots }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-title">已占用</div>
              <div class="stat-value">{{ usageResults.statistics.occupiedSlots }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-title">使用率</div>
              <div class="stat-value">{{ (usageResults.statistics.usageRate * 100).toFixed(2) }}%</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 空闲教室查询结果 -->
    <div v-if="searchForm.searchType === 2 && !loading" class="available-rooms card-shadow">
      <h3>空闲教室列表</h3>
      <el-table
          v-if="availableRooms.length > 0"
          :data="availableRooms"
          border
          stripe
          style="width: 100%"
      >
        <el-table-column label="教室名称" min-width="120" prop="name"/>
        <el-table-column label="教学楼" prop="building" width="120"/>
        <el-table-column
            :formatter="(row) => formatRoomType(row.roomType)"
            label="类型"
            prop="roomType"
            width="100"
        />
        <el-table-column label="容量" prop="capacity" width="80"/>
        <el-table-column
            label="状态"
            prop="status"
            width="80"
        >
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            width="120"
        >
          <template #default="scope">
            <el-button
                link
                type="primary"
                @click="viewRoomUsage(scope.row)"
            >
              查看使用情况
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="没有找到符合条件的空闲教室"/>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated/>
    </div>
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {getAllClassroomsList, getAvailableRooms, getClassroomsByBuilding, getClassroomUsage} from '@/api/classroom';

// 学期选项
const termOptions = [
  {label: '2023-2024学年第一学期', value: '202301'},
  {label: '2023-2024学年第二学期', value: '202302'},
  {label: '2024-2025学年第一学期', value: '202401'},
];

// 教室类型选项
const roomTypeOptions = [
  {label: '普通教室', value: '1'},
  {label: '多媒体教室', value: '2'},
  {label: '实验室', value: '3'},
];

// 星期
const weekDays = [
  {label: '周一', value: 1},
  {label: '周二', value: 2},
  {label: '周三', value: 3},
  {label: '周四', value: 4},
  {label: '周五', value: 5},
  {label: '周六', value: 6},
  {label: '周日', value: 7},
];

// 时间段
const timeSlots = [
  {label: '08:00-08:50', start: '08:00', end: '08:50'},
  {label: '09:00-09:50', start: '09:00', end: '09:50'},
  {label: '10:00-10:50', start: '10:00', end: '10:50'},
  {label: '11:00-11:50', start: '11:00', end: '11:50'},
  {label: '13:30-14:20', start: '13:30', end: '14:20'},
  {label: '14:30-15:20', start: '14:30', end: '15:20'},
  {label: '15:30-16:20', start: '15:30', end: '16:20'},
  {label: '16:30-17:20', start: '16:30', end: '17:20'},
  {label: '18:30-19:20', start: '18:30', end: '19:20'},
  {label: '19:30-20:20', start: '19:30', end: '20:20'},
  {label: '20:30-21:20', start: '20:30', end: '21:20'},
];

// 教学楼选项 (从教室数据中提取)
const buildingOptions = ref([]);

// 状态
const loading = ref(false);
const loadingClassrooms = ref(false);
const classroomOptions = ref([]);
const usageResults = ref({});
const availableRooms = ref([]);

// 搜索表单
const searchForm = reactive({
  searchType: 1, // 1: 查询教室使用情况, 2: 查询空闲教室
  classroomId: null,
  termInfo: '',
  date: '',
  startTime: '',
  endTime: '',
  building: '',
  roomType: '',
  minCapacity: null
});

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7; // 今天之前的日期禁用
};

// 计算属性
const hasUsageData = computed(() => {
  return usageResults.value.weekdayUsage &&
      Object.keys(usageResults.value.weekdayUsage).length > 0;
});

// 教室类型格式化
const formatRoomType = (type) => {
  const typeMap = {
    1: '普通教室',
    2: '多媒体教室',
    3: '实验室'
  };
  return typeMap[type] || '未知';
};

// 教室状态格式化
const formatStatus = (status) => {
  const statusMap = {
    0: '禁用',
    1: '正常',
    2: '维护中'
  };
  return statusMap[status] || '未知';
};

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  };
  return typeMap[status] || 'info';
};

// 判断时间段是否被占用
const isSlotOccupied = (day, slot) => {
  return getSlotInfo(day, slot) !== null;
};

// 获取时间段的课程信息
const getSlotInfo = (day, slot) => {
  if (!usageResults.value.weekdayUsage || !usageResults.value.weekdayUsage[day]) {
    return null;
  }

  // 获取当天的所有课程
  const daySchedules = usageResults.value.weekdayUsage[day];

  // 检查是否有课程在这个时间段
  for (const schedule of daySchedules) {
    if (isTimeOverlap(slot.start, slot.end, schedule.startTime, schedule.endTime)) {
      return schedule;
    }
  }

  return null;
};

// 判断两个时间段是否重叠
const isTimeOverlap = (start1, end1, start2, end2) => {
  return !(end1 <= start2 || start1 >= end2);
};

// 查询操作
const handleSearch = async () => {
  if (searchForm.searchType === 1 && !searchForm.classroomId) {
    ElMessage.warning('请选择要查询的教室');
    return;
  }

  if (searchForm.searchType === 2 && (!searchForm.startTime || !searchForm.endTime)) {
    ElMessage.warning('请选择时间段');
    return;
  }

  loading.value = true;

  try {
    if (searchForm.searchType === 1) {
      // 查询教室使用情况
      const res = await getClassroomUsage({
        classroomId: searchForm.classroomId,
        termInfo: searchForm.termInfo,
        date: searchForm.date
      });

      // 增加错误处理
      if (res && res.data) {
        usageResults.value = res.data;
      } else if (typeof res === 'object' && res !== null) {
        usageResults.value = res;
      } else {
        console.error('获取教室使用情况返回格式异常:', res);
        ElMessage.error('数据格式异常，请联系管理员');
        usageResults.value = {};
      }
    } else {
      // 查询空闲教室
      const timeSlot = searchForm.startTime && searchForm.endTime
          ? `${searchForm.startTime}-${searchForm.endTime}`
          : '';

      const res = await getAvailableRooms({
        date: searchForm.date,
        timeSlot: timeSlot,
        termInfo: searchForm.termInfo,
        building: searchForm.building,
        roomType: searchForm.roomType,
        minCapacity: searchForm.minCapacity
      });

      // 增加错误处理
      if (res && res.data) {
        availableRooms.value = res.data;
      } else if (Array.isArray(res)) {
        availableRooms.value = res;
      } else {
        console.error('获取空闲教室返回格式异常:', res);
        ElMessage.error('数据格式异常，请联系管理员');
        availableRooms.value = [];
      }
    }
  } catch (error) {
    console.error('查询失败:', error);
    ElMessage.error('查询失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 重置表单
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    if (key !== 'searchType') {
      searchForm[key] = key === 'minCapacity' ? null : '';
    }
  });

  // 清空结果
  usageResults.value = {};
  availableRooms.value = [];

  // 清空教室选项
  classroomOptions.value = [];
};

// 查看教室使用情况
const viewRoomUsage = (room) => {
  searchForm.searchType = 1;
  searchForm.building = room.building;
  searchForm.classroomId = room.id;
  handleSearch();
};

// 处理教学楼变化，加载对应教室列表
const handleBuildingChange = async (building) => {
  searchForm.classroomId = null;

  if (!building) {
    // 如果清空教学楼选择，加载所有教室
    await fetchAllClassrooms();
    return;
  }

  await fetchClassroomsByBuilding(building);
};

// 获取所有教室列表
const fetchAllClassrooms = async () => {
  loadingClassrooms.value = true;
  classroomOptions.value = [];

  try {
    const res = await getAllClassroomsList();

    if (res && res.data) {
      classroomOptions.value = res.data;
    } else if (Array.isArray(res)) {
      classroomOptions.value = res;
    } else {
      console.warn('获取所有教室列表返回异常格式:', res);
      ElMessage.warning('获取教室列表格式异常');
    }
  } catch (error) {
    console.error('获取所有教室列表失败:', error);
    ElMessage.error('获取教室列表失败');
  } finally {
    loadingClassrooms.value = false;
  }
};

// 按教学楼获取教室列表
const fetchClassroomsByBuilding = async (building) => {
  if (!building) return;

  loadingClassrooms.value = true;
  classroomOptions.value = [];

  try {
    // 使用现有API加上building参数
    const res = await getClassroomsByBuilding(building, {status: 1});

    if (res && res.data && res.data.records) {
      classroomOptions.value = res.data.records;
    } else if (res && Array.isArray(res.records)) {
      classroomOptions.value = res.records;
    } else if (Array.isArray(res)) {
      classroomOptions.value = res;
    } else {
      console.warn('按教学楼获取教室列表返回异常格式:', res);
      ElMessage.warning('获取教室列表格式异常');
    }
  } catch (error) {
    console.error('获取教室列表失败:', error);
    ElMessage.error('获取教室列表失败');
  } finally {
    loadingClassrooms.value = false;
  }
};

// 获取所有教学楼选项
const fetchBuildingOptions = async () => {
  try {
    // 获取所有教室以提取教学楼信息
    const res = await getAllClassroomsList();

    let classrooms = [];
    if (res && res.data) {
      classrooms = res.data;
      // 顺便更新教室选项
      classroomOptions.value = res.data;
    } else if (Array.isArray(res)) {
      classrooms = res;
      // 顺便更新教室选项
      classroomOptions.value = res;
    } else {
      console.warn('获取教室列表返回格式异常:', res);
      ElMessage.warning('获取教学楼列表格式异常');
      return;
    }

    // 提取教学楼选项
    const buildings = new Set();
    classrooms.forEach(room => {
      if (room.building) {
        buildings.add(room.building);
      }
    });

    buildingOptions.value = Array.from(buildings).map(building => ({
      label: building,
      value: building
    }));
  } catch (error) {
    console.error('获取教学楼列表失败:', error);
    ElMessage.error('获取教学楼列表失败');
  }
};

// 初始化 - 加载教学楼选项和所有教室
onMounted(() => {
  fetchBuildingOptions(); // 这个函数现在会同时更新classroomOptions
});
</script>

<style scoped>
.card-shadow {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  background-color: #fff;
  padding: 20px;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.classroom-info {
  margin-bottom: 20px;
}

.classroom-info h2 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.5rem;
  color: #303133;
}

.timetable-wrapper {
  overflow-x: auto;
}

.timetable-wrapper h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.2rem;
  color: #303133;
}

.timetable-container {
  overflow-x: auto;
}

.timetable {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.timetable th, .timetable td {
  border: 1px solid #ebeef5;
  text-align: center;
  padding: 12px 8px;
  vertical-align: middle;
}

.timetable th {
  background-color: #f5f7fa;
  font-weight: bold;
}

.time-column {
  width: 100px;
  background-color: #f5f7fa;
}

.occupied {
  background-color: #ffecec;
}

.class-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.course-name {
  font-weight: bold;
  margin: 0;
}

.teacher-name {
  font-size: 0.85em;
  color: #606266;
  margin: 0;
}

.statistics-wrapper h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.2rem;
  color: #303133;
}

.stat-card {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 16px;
  text-align: center;
}

.stat-title {
  font-size: 1rem;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #409eff;
}

.available-rooms h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.2rem;
  color: #303133;
}

.loading-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .timetable th, .timetable td {
    padding: 8px 4px;
    font-size: 0.85rem;
  }

  .time-column {
    width: 80px;
  }

  .course-name, .teacher-name {
    font-size: 0.8rem;
  }
}
</style> 