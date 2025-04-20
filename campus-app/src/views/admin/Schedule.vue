<template>
  <div class="schedule-management-container">
    <div class="page-header">
      <h2>课表管理</h2>
      <div>
        <!-- 可以添加自动排课等按钮 -->
        <el-button
            type="warning"
            @click="handleAutoSchedule"
        >
          自动排课
        </el-button>
        <el-button
            type="primary"
            @click="handleAddScheduleItem"
        >
          手动添加
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form
          :inline="true"
          :model="searchParams"
          @submit.prevent="fetchSchedule"
      >
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
          <el-input
              v-model="searchParams.teacherName"
              clearable
              placeholder="教师姓名"
          />
        </el-form-item>
        <el-form-item label="课程">
          <el-input
              v-model="searchParams.courseName"
              clearable
              placeholder="课程名称/代码"
          />
        </el-form-item>
        <el-form-item label="班级">
          <el-input
              v-model="searchParams.className"
              clearable
              placeholder="班级名称"
          />
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              @click="fetchSchedule"
          >
            查询课表
          </el-button>
        </el-form-item>
        <el-form-item label="视图">
          <el-radio-group v-model="viewType">
            <el-radio-button value="week">
              周视图
            </el-radio-button>
            <el-radio-button value="list">
              列表视图
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-card>

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
              :class="{ 'has-course': findCoursesForCell(day.value, time).length > 0 }"
              class="schedule-cell"
          >
            <!-- 管理员视图可能一个格子有多门课 -->
            <div
                v-for="course in findCoursesForCell(day.value, time)"
                :key="course.id"
                :style="{ backgroundColor: getCourseColor(course.courseId) }"
                class="course-item admin-course-item"
                @click="showCourseDetail(course)"
            >
              <div class="course-name">
                {{ course.courseName }}
              </div>
              <div class="course-detail">
                {{ course.teacherName }} / {{ course.classroomName }}
              </div>
              <div class="course-detail">
                {{ course.className }}
              </div>
            </div>
            <!-- 可以添加按钮用于在该时间段添加课程 -->
            <el-button
                v-if="findCoursesForCell(day.value, time).length === 0"
                :icon="Plus"
                circle
                class="add-in-cell-btn"
                size="small"
                type="primary"
                @click="handleAddScheduleItemInCell(day.value, time)"
            />
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
          <el-table-column
              label="授课教师"
              prop="teacherName"
          />
          <el-table-column
              label="班级"
              prop="className"
          />
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
              prop="classroomName"
          />
          <el-table-column
              label="操作"
              width="150"
          >
            <template #default="scope">
              <el-button
                  size="small"
                  type="primary"
                  @click="handleEditScheduleItem(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  @click="handleDeleteScheduleItem(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <!-- 分页 (列表视图可能需要) -->
      <div
          v-if="viewType === 'list' && total > 0"
          class="pagination-container"
      >
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

    <!-- 手动添加/编辑课表项 对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="700px"
        @close="resetScheduleForm"
    >
      <el-form
          ref="scheduleFormRef"
          v-loading="loadingForm"
          :model="scheduleForm"
          :rules="scheduleFormRules"
          label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
                label="学期"
                prop="termId"
            >
              <el-select
                  v-model="scheduleForm.termId"
                  :disabled="isEditMode"
                  filterable
                  placeholder="选择学期"
              >
                <el-option
                    v-for="item in semestersRef"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                label="课程"
                prop="courseId"
            >
              <el-select
                  v-model="scheduleForm.courseId"
                  :loading="loadingCourses"
                  filterable
                  placeholder="选择课程"
              >
                <el-option
                    v-for="course in courseOptions"
                    :key="course.id"
                    :label="`${course.courseNo} - ${course.courseName}`"
                    :value="course.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
                label="教师"
                prop="teacherId"
            >
              <el-select
                  v-model="scheduleForm.teacherId"
                  :loading="loadingTeachers"
                  filterable
                  placeholder="选择教师"
              >
                <el-option
                    v-for="teacher in teacherOptions"
                    :key="teacher.id"
                    :label="teacher.realName || teacher.username"
                    :value="teacher.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                label="班级"
                prop="classId"
            >
              <el-select
                  v-model="scheduleForm.classId"
                  :loading="loadingClasses"
                  filterable
                  placeholder="选择班级"
              >
                <el-option
                    v-for="cls in classOptions"
                    :key="cls.id"
                    :label="cls.name"
                    :value="cls.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
                label="教室"
                prop="classroomId"
            >
              <el-select
                  v-model="scheduleForm.classroomId"
                  :loading="loadingClassrooms"
                  filterable
                  placeholder="选择教室"
              >
                <el-option
                    v-for="room in classroomOptions"
                    :key="room.id"
                    :label="`${room.building} - ${room.name}`"
                    :value="room.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                label="星期"
                prop="weekDay"
            >
              <el-select
                  v-model="scheduleForm.weekDay"
                  placeholder="选择星期"
              >
                <el-option
                    v-for="day in weekdaysRef"
                    :key="day.value"
                    :label="day.label"
                    :value="day.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
                label="开始时间段"
                prop="startTime"
            >
              <el-select
                  v-model="scheduleForm.startTime"
                  placeholder="选择开始时间"
              >
                <el-option
                    v-for="time in timeSlotsRef"
                    :key="time.slot"
                    :label="`${time.label} (${time.startTime})`"
                    :value="time.startTime"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                label="结束时间段"
                prop="endTime"
            >
              <el-select
                  v-model="scheduleForm.endTime"
                  placeholder="选择结束时间"
              >
                <el-option
                    v-for="time in timeSlotsRef"
                    :key="time.slot"
                    :label="`${time.label} (${time.endTime})`"
                    :value="time.endTime"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
                label="开始周"
                prop="startWeek"
            >
              <el-input-number
                  v-model="scheduleForm.startWeek"
                  :max="25"
                  :min="1"
                  placeholder="起始周"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                label="结束周"
                prop="endWeek"
            >
              <el-input-number
                  v-model="scheduleForm.endWeek"
                  :max="25"
                  :min="1"
                  placeholder="结束周"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
              :loading="submitting"
              type="primary"
              @click="submitScheduleForm"
          >确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 自动排课对话框 -->
    <el-dialog
        v-model="autoScheduleDialogVisible"
        title="自动排课"
        width="700px"
    >
      <el-form
          ref="autoScheduleFormRef"
          v-loading="autoScheduleLoading"
          :model="autoScheduleForm"
          :rules="autoScheduleFormRules"
          label-width="120px"
      >
        <el-form-item label="学期" prop="termId">
          <el-select
              v-model="autoScheduleForm.termId"
              filterable
              placeholder="选择学期"
          >
            <el-option
                v-for="item in semestersRef"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="课程" prop="courses">
          <el-select
              v-model="autoScheduleForm.courses"
              :loading="loadingCourses"
              filterable
              multiple
              placeholder="选择需要排课的课程"
          >
            <el-option
                v-for="course in courseOptions"
                :key="course.id"
                :label="`${course.courseNo} - ${course.courseName}`"
                :value="course.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="班级" prop="classes">
          <el-select
              v-model="autoScheduleForm.classes"
              :loading="loadingClasses"
              filterable
              multiple
              placeholder="选择需要排课的班级"
          >
            <el-option
                v-for="cls in classOptions"
                :key="cls.id"
                :label="cls.name"
                :value="cls.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="排课规则">
          <el-checkbox v-model="autoScheduleForm.avoidTimeConflicts">避免时间冲突</el-checkbox>
          <el-tooltip content="避免同一教师、班级、教室在同一时间段被多次安排">
            <el-icon>
              <InfoFilled/>
            </el-icon>
          </el-tooltip>
        </el-form-item>

        <el-form-item label="教师负荷均衡">
          <el-checkbox v-model="autoScheduleForm.balanceTeacherLoad">平衡教师课时分配</el-checkbox>
          <el-tooltip content="尽量平均分配教师的课时，避免某些教师课时过多">
            <el-icon>
              <InfoFilled/>
            </el-icon>
          </el-tooltip>
        </el-form-item>

        <el-form-item label="教师喜好">
          <el-checkbox v-model="autoScheduleForm.respectPreferences">尊重教师时间喜好</el-checkbox>
          <el-tooltip content="尽量按照教师的时间偏好安排课程">
            <el-icon>
              <InfoFilled/>
            </el-icon>
          </el-tooltip>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="autoScheduleDialogVisible = false">取消</el-button>
          <el-button
              :loading="autoScheduleLoading"
              type="primary"
              @click="submitAutoScheduleForm"
          >开始排课</el-button>
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
  ElCol,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElRadioButton,
  ElRadioGroup,
  ElRow,
  ElSelect,
  ElTable,
  ElTableColumn
} from 'element-plus';
import {InfoFilled, Plus} from '@element-plus/icons-vue';
import {
  addSchedule,
  deleteSchedule,
  generateSchedule,
  getScheduleById,
  getScheduleList,
  updateSchedule
} from '@/api/schedule';
import {getClasses, getTerms, getTimeSlots, getWeekdays} from '@/api/common';
import {getAllCoursesForSelect} from '@/api/course';
import {getTeacherSelectList} from '@/api/user';
import {getAvailableClassrooms} from '@/api/classroom';

const loadingSchedule = ref(false);
const loadingSemesters = ref(false);
const loadingTimeSlots = ref(false);
const loadingWeekdays = ref(false);

const scheduleList = ref([]);
const viewType = ref('week');
const semestersRef = ref([]);
const timeSlotsRef = ref([]);
const weekdaysRef = ref([]);

const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(50);

const searchParams = reactive({
  semester: null,
  teacherName: '',
  courseName: '',
  className: ''
});

const dialogVisible = ref(false);
const dialogTitle = ref('添加课表项');

const loadingForm = ref(false);
const submitting = ref(false);
const isEditMode = ref(false);
const scheduleFormRef = ref(null);

const scheduleForm = ref({
  id: null,
  termId: null,
  courseId: null,
  teacherId: null,
  classId: null,
  classroomId: null,
  weekDay: null,
  startTime: null,
  endTime: null,
  startWeek: 1,
  endWeek: 16,
});

const scheduleFormRules = reactive({
  termId: [{required: true, message: '请选择学期', trigger: 'change'}],
  courseId: [{required: true, message: '请选择课程', trigger: 'change'}],
  teacherId: [{required: true, message: '请选择教师', trigger: 'change'}],
  classId: [{required: true, message: '请选择班级', trigger: 'change'}],
  classroomId: [{required: true, message: '请选择教室', trigger: 'change'}],
  weekDay: [{required: true, message: '请选择星期', trigger: 'change'}],
  startTime: [{required: true, message: '请选择开始时间', trigger: 'change'}],
  endTime: [{required: true, message: '请选择结束时间', trigger: 'change'}],
  startWeek: [{required: true, message: '请输入开始周', trigger: 'blur'}],
  endWeek: [
    {required: true, message: '请输入结束周', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (scheduleForm.value.startWeek && value < scheduleForm.value.startWeek) {
          callback(new Error('结束周不能小于开始周'));
        } else {
          callback();
        }
      }, trigger: 'blur'
    }
  ],
});

const courseOptions = ref([]);
const teacherOptions = ref([]);
const classOptions = ref([]);
const classroomOptions = ref([]);
const loadingCourses = ref(false);
const loadingTeachers = ref(false);
const loadingClasses = ref(false);
const loadingClassrooms = ref(false);

const autoScheduleDialogVisible = ref(false);
const autoScheduleLoading = ref(false);
const autoScheduleForm = ref({
  termId: null,
  avoidTimeConflicts: true,
  balanceTeacherLoad: true,
  respectPreferences: true,
  courses: [],
  classes: []
});

const autoScheduleFormRules = reactive({
  termId: [{required: true, message: '请选择学期', trigger: 'change'}],
  courses: [{required: true, message: '请至少选择一门课程', trigger: 'change'}],
  classes: [{required: true, message: '请至少选择一个班级', trigger: 'change'}]
});

const autoScheduleFormRef = ref(null);
const submitAutoScheduleForm = () => {
  autoScheduleFormRef.value.validate(async (valid) => {
    if (valid) {
      autoScheduleLoading.value = true;
      try {
        // 准备请求数据
        const data = {
          ...autoScheduleForm.value
        };

        // 调用自动排课 API
        await generateSchedule(data);
        ElMessage.success('自动排课成功，正在刷新数据');
        autoScheduleDialogVisible.value = false;

        // 重新获取课表数据
        fetchSchedule();
      } catch (error) {
        console.error("自动排课失败", error);
        ElMessage.error(error?.response?.data?.message || '自动排课失败，请检查参数设置');
      } finally {
        autoScheduleLoading.value = false;
      }
    }
  });
};

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
      searchParams.semester = semestersRef.value[0].value;
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
      page: viewType.value === 'list' ? currentPage.value : null,
      size: viewType.value === 'list' ? pageSize.value : null,
    };
    Object.keys(params).forEach(key => params[key] == null && delete params[key]);

    const response = await getScheduleList(params);
    if (viewType.value === 'list') {
      scheduleList.value = response.data.list || [];
      total.value = response.data.total || 0;
    } else {
      scheduleList.value = response.data.list || response.data || [];
      total.value = 0;
    }

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

const findCoursesForCell = (dayValue, timeSlot) => {
  if (!timeSlot || !timeSlot.startTime) return [];
  const slotStartMinutes = timeToMinutes(timeSlot.startTime);

  return scheduleList.value.filter(schedule => {
    if (!schedule.startTime) return false; // 跳过 startTime 为空的记录
    let scheduleStartTimeStr = '';
    try {
      // 将 Date 对象格式化为 HH:mm
      const date = new Date(schedule.startTime);
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      scheduleStartTimeStr = `${hours}:${minutes}`;
    } catch (e) {
      console.error("格式化课表开始时间失败:", schedule.startTime, e);
      return false; // 格式化失败则不匹配
    }
    // 用格式化后的字符串计算分钟数
    const scheduleStartMinutes = timeToMinutes(scheduleStartTimeStr);
    return schedule.weekday === dayValue && scheduleStartMinutes === slotStartMinutes;
  });
};

const getCourseColor = (courseId) => courseColors[courseId] || '#409EFF';

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchSchedule();
};
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchSchedule();
};

const handleAutoSchedule = () => {
  autoScheduleDialogVisible.value = true;
  loadingCourses.value = true;
  loadingClasses.value = true;

  // 初始化表单
  autoScheduleForm.value = {
    termId: searchParams.semester,
    avoidTimeConflicts: true,
    balanceTeacherLoad: true,
    respectPreferences: true,
    courses: [],
    classes: []
  };

  // 获取课程和班级数据
  Promise.all([
    getAllCoursesForSelect(),
    getClasses()
  ]).then(([courseRes, classRes]) => {
    courseOptions.value = courseRes.data || [];
    classOptions.value = classRes.data || [];
  }).catch(error => {
    console.error("获取课程和班级数据失败", error);
    ElMessage.error("获取课程和班级数据失败");
  }).finally(() => {
    loadingCourses.value = false;
    loadingClasses.value = false;
  });
};

const handleAddScheduleItem = () => {
  resetScheduleForm();
  dialogTitle.value = '手动添加课表项';
  dialogVisible.value = true;
  fetchSelectOptions();
};

const handleAddScheduleItemInCell = (day, timeSlot) => {
  resetScheduleForm();
  scheduleForm.value.weekDay = day;
  scheduleForm.value.startTime = timeSlot.startTime;
  scheduleForm.value.endTime = timeSlot.endTime;
  dialogTitle.value = `添加课表项 (星期${day} ${timeSlot.label})`;
  dialogVisible.value = true;
  fetchSelectOptions();
};

const handleEditScheduleItem = async (row) => {
  resetScheduleForm();
  dialogTitle.value = '编辑课表项';
  isEditMode.value = true;
  loadingForm.value = true;
  dialogVisible.value = true;
  fetchSelectOptions();
  try {
    const res = await getScheduleById(row.id);
    scheduleForm.value = {...res.data};
  } catch (error) {
    console.error("获取课表详情失败", error);
    ElMessage.error("获取课表详情失败");
    dialogVisible.value = false;
  } finally {
    loadingForm.value = false;
  }
};

const handleDeleteScheduleItem = (row) => {
  ElMessageBox.confirm(
      `确定要删除 ${row.courseName} (星期${formatWeekday(row.weekday)} ${row.startTime}-${row.endTime}) 这条课表记录吗?`,
      '提示',
      {confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'}
  ).then(async () => {
    try {
      await deleteSchedule(row.id);
      ElMessage.success('删除成功');
      fetchSchedule();
    } catch (error) {
      console.error("删除课表项失败", error);
      ElMessage.error("删除课表项失败");
    }
  }).catch(() => {
  });
};

const showCourseDetail = (course) => {
  ElMessage.info(`查看课程 ${course.courseName} 详情（管理员视图）`);
};

const fetchSelectOptions = async () => {
  loadingCourses.value = true;
  loadingTeachers.value = true;
  loadingClasses.value = true;
  loadingClassrooms.value = true;
  try {
    const [courseRes, teacherRes, classRes, classroomRes] = await Promise.all([
      getAllCoursesForSelect(),
      getTeacherSelectList(),
      getClasses(),
      getAvailableClassrooms()
    ]);
    courseOptions.value = courseRes.data || [];
    teacherOptions.value = teacherRes.data?.list || teacherRes.data || [];
    classOptions.value = classRes.data || [];
    classroomOptions.value = classroomRes.data || [];
  } catch (error) {
    console.error("获取下拉选项失败", error);
    ElMessage.error("获取课程/教师/班级/教室列表失败");
  } finally {
    loadingCourses.value = false;
    loadingTeachers.value = false;
    loadingClasses.value = false;
    loadingClassrooms.value = false;
  }
};

const resetScheduleForm = () => {
  if (scheduleFormRef.value) {
    scheduleFormRef.value.resetFields();
  }
  scheduleForm.value = {
    id: null,
    termId: searchParams.semester,
    courseId: null,
    teacherId: null,
    classId: null,
    classroomId: null,
    weekDay: null,
    startTime: null,
    endTime: null,
    startWeek: 1,
    endWeek: 16,
  };
  isEditMode.value = false;
};

const submitScheduleForm = () => {
  scheduleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const dataToSend = {...scheduleForm.value};

        if (isEditMode.value) {
          await updateSchedule(dataToSend.id, dataToSend);
          ElMessage.success('更新成功');
        } else {
          await addSchedule(dataToSend);
          ElMessage.success('添加成功');
        }
        dialogVisible.value = false;
        fetchSchedule();
      } catch (error) {
        console.error("提交课表项失败", error);
        ElMessage.error(error?.response?.data?.message || '提交失败，可能存在时间冲突');
      } finally {
        submitting.value = false;
      }
    } else {
      console.log('课表表单验证失败');
      return false;
    }
  });
};

onMounted(() => {
  fetchInitialData();
});

</script>

<script>
export default {
  name: 'ScheduleManagement'
}
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