<template>
  <PageContainer title="课表管理">
    <template #actions>
        <el-button
            type="primary"
            :icon="Plus"
            @click="handleAddScheduleItem"
        >
          手动添加
        </el-button>
    </template>

    <FilterForm
        :items="filterItems"
        :loading="loadingSemesters"
        :model="searchParams"
        @reset="handleReset"
        @search="handleSearch"
    />

    <TableView
        :columns="tableColumns"
            :data="scheduleList"
        :loading="loadingSchedule"
        :total="total"
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
        @delete="handleDeleteScheduleItem"
        @edit="handleEditScheduleItem"
        @refresh="fetchSchedule"
        />

    <!-- 手动添加/编辑课表项 对话框 (保持不变) -->
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
                    v-for="item in termList"
                    :key="item.id"
                    :label="item.termName"
                    :value="item.id"
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
                  style="width: 100%;"
                  @change="handleCourseChange"
              >
                <el-option
                    v-for="course in courseOptions"
                    :key="course.id"
                    :label="`${course.courseCode || course.courseNo || ''} - ${course.courseName}`"
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
                  style="width: 100%;"
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
                  style="width: 100%;"
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
                label="星期"
                prop="weekday"
            >
              <el-select
                  v-model="scheduleForm.weekday"
                  placeholder="选择星期"
                  style="width: 100%;"
              >
                <el-option
                    v-for="day in WEEKDAY_OPTIONS"
                    :key="day.value"
                    :label="day.label"
                    :value="day.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
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
                  style="width: 100%;"
              >
                <el-option
                    v-for="room in classroomOptions"
                    :key="room.id"
                    :label="`${room.building} - ${room.roomNumber}`"
                    :value="room.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
                label="开始时间"
                prop="startTime"
            >
              <el-time-select
                  v-model="scheduleForm.startTime"
                  end="21:30"
                  start="08:00"
                  step="00:30"
                  placeholder="选择开始时间"
                  style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                label="结束时间"
                prop="endTime"
            >
              <el-time-select
                  v-model="scheduleForm.endTime"
                  :min-time="scheduleForm.startTime"
                  end="21:30"
                  start="08:00"
                  placeholder="选择结束时间"
                  step="00:30"
                  style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="周次范围" prop="weekNumbers">
              <el-input-number v-model="scheduleForm.startWeek" :max="scheduleForm.endWeek || 20" :min="1"
                               label="开始周"></el-input-number>
              <span style="margin: 0 10px;">至</span>
              <el-input-number v-model="scheduleForm.endWeek" :max="20" :min="scheduleForm.startWeek || 1"
                               label="结束周"></el-input-number>
              <el-tooltip content="指定该排课适用的周次范围，例如第1周到第16周" placement="top">
                <el-icon style="margin-left: 8px; cursor: pointer;">
                  <InfoFilled/>
                </el-icon>
              </el-tooltip>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="notes">
              <el-input
                  v-model="scheduleForm.notes"
                  :rows="2"
                  placeholder="可选，输入备注信息"
                  type="textarea"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              :loading="submitting"
              @click="handleSubmitSchedule"
          >
            {{ submitButtonText }}
          </el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import {ref, reactive, onMounted, computed, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Search, Edit, Delete, InfoFilled} from '@element-plus/icons-vue';
import {getSchedulesPage, addSchedule, updateSchedule, deleteSchedule} from '@/api/schedule'; // Corrected: Use schedule.js
import {getAllTerms} from '@/api/term'; // Corrected function name
import {getAllCoursesForSelect} from '@/api/course'; // Corrected function name for select
import {getTeacherSelectList} from '@/api/user'; // Corrected function name for select
import {getClasses} from '@/api/common'; // Corrected function name for select
import {getAllClassroomsList} from '@/api/classroom'; // Corrected function name for select
import {listClasses} from '@/api/common'; // Corrected: Classes list likely in common.js
import {listTerms} from '@/api/term'; // Assuming term API exists
import {
  adminGetTeachers, adminListAllCourses, adminListAllClassrooms, adminListTerms
} from '@/api/user'; // Corrected: APIs likely in user.js


// --- 常量 ---
const WEEKDAY_OPTIONS = [
  {value: 1, label: '星期一'},
  {value: 2, label: '星期二'},
  {value: 3, label: '星期三'},
  {value: 4, label: '星期四'},
  {value: 5, label: '星期五'},
  {value: 6, label: '星期六'},
  {value: 7, label: '星期日'}
];

// --- 响应式状态 ---
const loadingSchedule = ref(false);
const loadingSemesters = ref(false);
const scheduleList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  termId: null,
  weekDay: null,
  keyword: '',
});
const termList = ref([]);

const dialogVisible = ref(false);
const dialogTitle = ref('添加排课');
const isEditMode = ref(false);
const scheduleFormRef = ref(null);
const scheduleForm = reactive({
  id: null,
  termId: null,
  courseId: null,
  teacherId: null,
  classId: null,
  weekday: null,
  startTime: '',
  endTime: '',
  classroomId: null,
  startWeek: 1,
  endWeek: 16,
  notes: ''
});
const submitting = ref(false);
const loadingForm = ref(false); // 用于加载表单内下拉框数据
const loadingCourses = ref(false);
const loadingTeachers = ref(false);
const loadingClasses = ref(false);
const loadingClassrooms = ref(false);
const courseOptions = ref([]);
const teacherOptions = ref([]);
const classOptions = ref([]);
const classroomOptions = ref([]);

// --- 计算属性 ---
const submitButtonText = computed(() => (isEditMode.value ? '更新' : '创建'));

// --- FilterForm 配置 ---
const filterItems = computed(() => [
  {
    type: 'select',
    label: '学期',
    prop: 'termId',
    placeholder: '选择学期',
    options: termList.value.map(term => ({label: term.termName, value: term.id})),
    props: {clearable: true, filterable: true, style: {width: '200px'}}
  },
  {
    type: 'select',
    label: '星期',
    prop: 'weekDay',
    placeholder: '选择星期',
    options: WEEKDAY_OPTIONS,
    props: {clearable: true, style: {width: '150px'}}
  },
  {
    type: 'input',
    label: '关键词',
    prop: 'keyword',
    placeholder: '搜索课程/教师/教室',
    props: {clearable: true, style: {width: '240px'}}
  }
]);

// --- TableView 配置 ---
const tableColumns = [
  {prop: 'courseName', label: '课程名称', minWidth: 150},
  {prop: 'teacherName', label: '授课教师', minWidth: 100},
  {prop: 'className', label: '班级', minWidth: 120},
  {
    prop: 'weekday',
    label: '星期',
    width: 100,
    formatter: (row) => formatWeekday(row.weekday)
  },
  {
    prop: 'startTime',
    label: '开始时间',
    width: 100,
    formatter: (row) => row.startTime ? String(row.startTime).substring(0, 5) : '-'
  },
  {
    prop: 'endTime',
    label: '结束时间',
    width: 100,
    formatter: (row) => row.endTime ? String(row.endTime).substring(0, 5) : '-'
  },
  {prop: 'classroomName', label: '教室', minWidth: 120},
  {prop: 'weeks', label: '周次', width: 100, formatter: (row) => `${row.startWeek}-${row.endWeek}周`},
  {prop: 'notes', label: '备注', minWidth: 150, showOverflowTooltip: true},
  {
    label: '操作',
    width: 150,
    type: 'action', // 标记为操作列
    actions: [ // 定义操作按钮
      {type: 'primary', label: '编辑', event: 'edit'},
      {type: 'danger', label: '删除', event: 'delete'},
    ]
  }
];

// --- 方法 ---

// 获取学期列表
const fetchTermList = async () => {
  loadingSemesters.value = true;
  try {
    const res = await getAllTerms(); // Corrected function name
    termList.value = res.data || [];
    // 设置默认学期为当前激活的学期（如果只有一个）
    if (termList.value.length > 0) {
      const activeTerm = termList.value.find(t => t.isCurrent); // 假设有 isCurrent 字段
      searchParams.termId = activeTerm ? activeTerm.id : termList.value[0].id;
      scheduleForm.termId = activeTerm ? activeTerm.id : termList.value[0].id; // 表单也设置默认值
    }
  } catch (error) {
    console.error("获取学期列表失败:", error);
    // ElMessage.error('获取学期列表失败'); // FilterForm 会显示加载状态
  } finally {
    loadingSemesters.value = false;
  }
};

// 获取排课列表
const fetchSchedule = async () => {
  loadingSchedule.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      termId: searchParams.termId,
      weekday: searchParams.weekDay, // 注意属性名匹配
      keyword: searchParams.keyword,
    };
    const res = await getSchedulesPage(params); // Corrected
    scheduleList.value = res.data.records || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取排课列表失败:", error);
    // ElMessage.error('获取排课列表失败'); // TableView 有自己的错误提示
  } finally {
    loadingSchedule.value = false;
  }
};

// 处理筛选表单搜索
const handleSearch = () => {
  currentPage.value = 1; // 重置到第一页
  fetchSchedule();
};

// 处理筛选表单重置 (FilterForm 内部实现)
const handleReset = () => {
  searchParams.termId = termList.value.find(t => t.isCurrent)?.id || (termList.value.length > 0 ? termList.value[0].id : null);
  searchParams.weekDay = null;
  searchParams.keyword = '';
  currentPage.value = 1;
  fetchSchedule();
};


// 格式化星期几
const formatWeekday = (day) => {
  const found = WEEKDAY_OPTIONS.find(d => d.value === day);
  return found ? found.label : day;
};

// --- 添加/编辑 弹窗逻辑 ---

// 获取表单所需的下拉选项数据
const fetchFormSupportData = async () => {
  loadingForm.value = true;
  loadingCourses.value = true;
  loadingTeachers.value = true;
  loadingClasses.value = true;
  loadingClassrooms.value = true;
  try {
    // 并行获取所有数据
    const [courseRes, teacherRes, classRes, classroomRes] = await Promise.all([
      getAllCoursesForSelect({size: 1000}), // Corrected
      getTeacherSelectList({size: 1000}), // Corrected
      getClasses({size: 1000}), // Corrected
      getAllClassroomsList({size: 1000}) // Corrected
    ]);
    courseOptions.value = courseRes.data?.records || courseRes.data || []; // Adapt based on API response structure (page vs list)
    teacherOptions.value = teacherRes.data?.records || teacherRes.data || [];
    classOptions.value = classRes.data?.records || classRes.data || [];
    classroomOptions.value = classroomRes.data?.records || classroomRes.data || [];

  } catch (error) {
    console.error("获取表单下拉选项失败:", error);
    ElMessage.error('加载表单选项失败，请稍后重试');
    dialogVisible.value = false; // 获取失败则关闭弹窗
  } finally {
    loadingForm.value = false;
    loadingCourses.value = false;
    loadingTeachers.value = false;
    loadingClasses.value = false;
    loadingClassrooms.value = false;
  }
};

// 当选择课程时，尝试自动填充教师（如果课程有关联的默认教师）
// 注意：这需要后端课程数据包含默认教师信息，或者提供一个根据课程查教师的接口
const handleCourseChange = (courseId) => {
  const selectedCourse = courseOptions.value.find(c => c.id === courseId);
  if (selectedCourse && selectedCourse.defaultTeacherId && teacherOptions.value.some(t => t.id === selectedCourse.defaultTeacherId)) {
    scheduleForm.teacherId = selectedCourse.defaultTeacherId;
  }
};

// 重置表单
const resetScheduleForm = () => {
  if (scheduleFormRef.value) {
    scheduleFormRef.value.resetFields();
  }
  scheduleForm.id = null;
  // 保留学期选择或设为默认
  scheduleForm.termId = searchParams.termId;
  scheduleForm.courseId = null;
  scheduleForm.teacherId = null;
  scheduleForm.classId = null;
  scheduleForm.weekday = null;
  scheduleForm.startTime = '';
  scheduleForm.endTime = '';
  scheduleForm.classroomId = null;
  scheduleForm.startWeek = 1;
  scheduleForm.endWeek = 16;
  scheduleForm.notes = '';
  };

// 打开添加弹窗
const handleAddScheduleItem = () => {
  isEditMode.value = false;
  dialogTitle.value = '添加排课';
  resetScheduleForm();
  dialogVisible.value = true;
  fetchFormSupportData(); // 获取下拉数据
};

// 打开编辑弹窗
const handleEditScheduleItem = (row) => {
  isEditMode.value = true;
  dialogTitle.value = '编辑排课';
  resetScheduleForm(); // 先重置
  // 填充表单数据
  Object.assign(scheduleForm, {
    id: row.id,
    termId: row.termId,
    courseId: row.courseId,
    teacherId: row.teacherId,
    classId: row.classId,
    weekday: row.weekday,
    startTime: row.startTime ? String(row.startTime).substring(0, 5) : '', // 确保是 HH:mm 格式
    endTime: row.endTime ? String(row.endTime).substring(0, 5) : '', // 确保是 HH:mm 格式
    classroomId: row.classroomId,
    startWeek: row.startWeek,
    endWeek: row.endWeek,
    notes: row.notes
  });
  dialogVisible.value = true;
  fetchFormSupportData(); // 获取下拉数据
};

// 提交表单
const handleSubmitSchedule = async () => {
  if (!scheduleFormRef.value) return;
  await scheduleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 准备提交的数据，确保时间格式正确
        const dataToSubmit = {...scheduleForm};
        // 后端需要 HH:mm:ss 格式，如果 TimeSelect 返回的是 HH:mm
        // dataToSubmit.startTime = dataToSubmit.startTime ? `${dataToSubmit.startTime}:00` : null;
        // dataToSubmit.endTime = dataToSubmit.endTime ? `${dataToSubmit.endTime}:00` : null;

        if (isEditMode.value) {
          await updateSchedule(dataToSubmit.id, dataToSubmit); // Corrected
          ElMessage.success('更新成功');
        } else {
          await addSchedule(dataToSubmit); // Corrected
          ElMessage.success('添加成功');
        }
        dialogVisible.value = false;
        fetchSchedule(); // 刷新列表
      } catch (error) {
        console.error("提交排课信息失败:", error);
        // 错误消息由 request 拦截器处理
      } finally {
        submitting.value = false;
      }
    } else {
      ElMessage.warning('请检查表单必填项');
      return false;
    }
  });
};

// 删除排课项
const handleDeleteScheduleItem = (row) => {
  ElMessageBox.confirm(
      `确定要删除课程【${row.courseName}】在【${formatWeekday(row.weekday)} ${row.startTime?.substring(0, 5)}-${row.endTime?.substring(0, 5)}】的排课吗？`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    try {
      await deleteSchedule(row.id); // Corrected
      ElMessage.success('删除成功');
      fetchSchedule(); // 刷新列表
    } catch (error) {
      console.error("删除排课失败:", error);
      // 错误消息由 request 拦截器处理
    }
  }).catch(() => {
    // 取消删除
    ElMessage.info('已取消删除');
  });
};

// 表单验证规则
const scheduleFormRules = reactive({
  termId: [{required: true, message: '请选择学期', trigger: 'change'}],
  courseId: [{required: true, message: '请选择课程', trigger: 'change'}],
  teacherId: [{required: true, message: '请选择教师', trigger: 'change'}],
  classId: [{required: true, message: '请选择班级', trigger: 'change'}],
  weekday: [{required: true, message: '请选择星期', trigger: 'change'}],
  startTime: [{required: true, message: '请选择开始时间', trigger: 'change'}],
  endTime: [{required: true, message: '请选择结束时间', trigger: 'change'}],
  classroomId: [{required: true, message: '请选择教室', trigger: 'change'}],
  startWeek: [{required: true, type: 'number', message: '请输入开始周', trigger: 'blur'}],
  endWeek: [
    {required: true, type: 'number', message: '请输入结束周', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (value < scheduleForm.startWeek) {
          callback(new Error('结束周不能小于开始周'));
        } else {
          callback();
        }
      }, trigger: 'blur'
    }
  ],
});

// --- 生命周期钩子 ---
onMounted(() => {
  fetchTermList().then(() => {
    fetchSchedule(); // 获取学期后再获取排课
  });
});

// 监听分页变化 (TableView 内部处理 v-model 更新)
watch([currentPage, pageSize], () => {
  fetchSchedule();
}, {immediate: false}); // 不需要立即执行，因为 onMounted 会调用

</script>

<style scoped>
/* 可以保留特定于此页面的样式，或移至 common.css */
</style>