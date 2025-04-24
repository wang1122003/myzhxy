<template>
  <div class="schedule-management-container">
    <div class="page-header">
      <h2>课表管理</h2>
      <div>
        <!-- 移除 "自动排课" 按钮 -->
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
              v-model="searchParams.termId"
              placeholder="选择学期"
              clearable
              filterable
              @change="handleFilterChange"
              style="width: 200px"
          >
            <el-option
                v-for="term in termList"
                :key="term.id"
                :label="term.termName"
                :value="term.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="星期">
          <el-select
              v-model="searchParams.weekDay"
              placeholder="选择星期"
              clearable
              @change="handleFilterChange"
              style="width: 150px"
          >
            <el-option
                v-for="day in weekDayOptions"
                :key="day.value"
                :label="day.label"
                :value="day.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
              v-model="searchParams.keyword"
              placeholder="搜索课程/教师/教室"
              clearable
              style="width: 240px"
              @keyup.enter="handleFilterChange"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilterChange" :icon="Search">查询</el-button>
          <el-button :icon="Plus" @click="handleAddScheduleItem">添加排课</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card
        v-loading="loadingSchedule || loadingWeekdays"
        class="schedule-card"
    >
      <!-- 列表视图 -->
      <div class="list-view">
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
      <!-- 分页 -->
      <div
          v-if="total > 0"
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
              <el-time-select
                  v-model="scheduleForm.startTime"
                  end="22:00"
                  format="HH:mm"
                  start="07:00"
                  placeholder="选择开始时间"
                  step="00:15"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                label="结束时间段"
                prop="endTime"
            >
              <el-time-select
                  v-model="scheduleForm.endTime"
                  end="22:00"
                  format="HH:mm"
                  start="07:00"
                  placeholder="选择结束时间"
                  step="00:15"
              />
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
  ElRow,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTimeSelect,
} from 'element-plus';
import {addSchedule, deleteSchedule, getScheduleById, getScheduleList, updateSchedule} from '@/api/schedule';
import {getClasses, getTerms, getWeekdays} from '@/api/common';
import {getAllCoursesForSelect} from '@/api/course';
import {getTeacherSelectList} from '@/api/user';
import {getAvailableClassrooms} from '@/api/classroom';
import {getTermList} from '@/api/term';
import {Search, Plus} from '@element-plus/icons-vue';

const loadingSchedule = ref(false);
const loadingSemesters = ref(false);
const loadingWeekdays = ref(false);

const scheduleList = ref([]);
const semestersRef = ref([]);
const weekdaysRef = ref([]);

const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const searchParams = reactive({
  termId: null,
  teacherName: '',
  courseName: '',
  className: '',
  weekDay: null,
  keyword: '',
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
  endTime: [
    {required: true, message: '请选择结束时间', trigger: 'change'},
    {
      validator: (rule, value, callback) => {
        if (scheduleForm.value.startTime && value <= scheduleForm.value.startTime) {
          callback(new Error('结束时间必须晚于开始时间'));
        } else {
          callback();
        }
      }, trigger: 'change'
    }
  ],
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

const termList = ref([]);

const weekDayOptions = ref([]);

const fetchInitialData = async () => {
  loadingSemesters.value = true;
  loadingWeekdays.value = true;
  try {
    const [termsRes, weekdaysRes] = await Promise.all([
      getTerms(),
      getWeekdays()
    ]);
    semestersRef.value = termsRes.data || [];
    weekdaysRef.value = weekdaysRes.data || [];

    if (semestersRef.value.length > 0 && !searchParams.termId) {
      searchParams.termId = semestersRef.value[0].value;
      await fetchSchedule();
    }
  } catch (error) {
    console.error('获取初始数据失败', error);
    ElMessage.error('获取学期/星期数据失败');
  } finally {
    loadingSemesters.value = false;
    loadingWeekdays.value = false;
  }
};

const fetchSchedule = async () => {
  if (!searchParams.termId) {
    ElMessage.warning('请先选择学期');
    return;
  }
  loadingSchedule.value = true;
  try {
    const params = {
      termId: searchParams.termId,
      teacherName: searchParams.teacherName || null,
      courseName: searchParams.courseName || null,
      className: searchParams.className || null,
      page: currentPage.value,
      size: pageSize.value,
    };
    Object.keys(params).forEach(key => params[key] == null && delete params[key]);

    const response = await getScheduleList(params);
    // 修正：后端返回 IPage 对象，包含 records 和 total
    if (response.data && response.data.records) {
      scheduleList.value = response.data.records;
      total.value = response.data.total || 0;
    } else {
      // 处理异常情况或旧格式 (如果后端可能不返回 IPage)
      scheduleList.value = [];
      total.value = 0;
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

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchSchedule();
};
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchSchedule();
};

const handleAddScheduleItem = () => {
  resetScheduleForm();
  dialogTitle.value = '手动添加课表项';
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
    const loadedData = {...res.data};
    if (loadedData.startTime && loadedData.startTime.includes(':')) {
      loadedData.startTime = loadedData.startTime.substring(0, 5);
    }
    if (loadedData.endTime && loadedData.endTime.includes(':')) {
      loadedData.endTime = loadedData.endTime.substring(0, 5);
    }
    scheduleForm.value = loadedData;
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
    termId: searchParams.termId,
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

const fetchTerms = async () => {
  try {
    const res = await getTermList();
    if (res.code === 200 && res.data) {
      termList.value = res.data;
      // 设置默认选中当前学期
      const currentTerm = res.data.find(t => t.current === 1);
      if (currentTerm) {
        searchParams.termId = currentTerm.id;
        fetchSchedule(); // 加载默认学期的课表
      }
    } else {
      ElMessage.error(res.message || '获取学期列表失败');
    }
  } catch (error) {
    console.error("获取学期列表失败", error);
    ElMessage.error('获取学期列表时发生错误');
  }
};

const handleFilterChange = () => {
  currentPage.value = 1;
  fetchSchedule();
};

const fetchWeekdays = async () => {
  loadingWeekdays.value = true;
  try {
    const res = await getWeekdays();
    if (res.success && Array.isArray(res.data)) {
      weekDayOptions.value = res.data.map(day => ({
        label: day.label,
        value: day.value
      }));
    } else {
      ElMessage.error(res.message || '获取星期列表失败');
      weekDayOptions.value = [];
    }
  } catch (error) {
    console.error('获取星期列表失败:', error);
    ElMessage.error('获取星期列表失败');
    weekDayOptions.value = [];
  } finally {
    loadingWeekdays.value = false;
  }
};

onMounted(() => {
  fetchInitialData();
  fetchTerms();
  fetchWeekdays();
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

/* 列表视图 */
.list-view {
  width: 100%;
}

</style> 