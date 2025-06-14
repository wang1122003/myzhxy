<template>
  <PageContainer title="选课管理">
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

    <!-- 手动添加/编辑课表项 对话框 -->
    <DialogWrapper
        v-model:visible="dialogVisible"
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
                  style="width: 100%"
              >
                <el-option
                    v-for="item in termList"
                    :key="item.id"
                    :label="item.termName || item.name"
                    :value="item.id || item.code"
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
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
                label="星期"
                prop="dayOfWeek"
            >
              <el-select
                  v-model="scheduleForm.dayOfWeek"
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
                    :label="`${room.building} - ${room.name || room.roomNumber || 'Unknown'}`"
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
    </DialogWrapper>
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {InfoFilled, Plus} from '@element-plus/icons-vue';
import {addSchedule, deleteSchedule, getSchedulesPage, updateSchedule} from '@/api/schedule';
import {getAllTerms} from '@/api/term';
import {getAllCoursesForSelect} from '@/api/course';
import {getTeacherList} from '@/api/user';
import {getAllClassroomsList} from '@/api/classroom';
import DialogWrapper from '@/views/ui/DialogWrapper.vue';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import FilterForm from '@/views/ui/AdvancedFilterForm.vue';
import TableView from '@/views/ui/TableView.vue';


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
  dayOfWeek: null,
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
  dayOfWeek: null,
  startTime: '',
  endTime: '',
  classroomId: null,
  startWeek: 1,
  endWeek: 16,
  notes: '',
  capacity: 0,
});
const submitting = ref(false);
const loadingForm = ref(false); // 用于加载表单内下拉框数据
const loadingCourses = ref(false);
const loadingTeachers = ref(false);
const loadingClassrooms = ref(false);
const courseOptions = ref([]);
const teacherOptions = ref([]);
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
    options: termList.value.map(term => ({label: term.termName || term.name, value: term.id})),
    props: {clearable: true, filterable: true, style: {width: '200px'}}
  },
  {
    type: 'select',
    label: '星期',
    prop: 'dayOfWeek',
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
  {
    prop: 'dayOfWeek',
    label: '星期',
    width: 100,
    formatter: (row) => formatWeekday(row.dayOfWeek)
  },
  {
    prop: 'startTime',
    label: '开始时间',
    width: 100,
    formatter: (row) => formatTime(row.startTime)
  },
  {
    prop: 'endTime',
    label: '结束时间',
    width: 100,
    formatter: (row) => formatTime(row.endTime)
  },
  {prop: 'classroomName', label: '教室', minWidth: 120},
  {prop: 'weeks', label: '周次', width: 100, formatter: (row) => `${row.startWeek}-${row.endWeek}周`},
  {prop: 'capacity', label: '容量', width: 80},
  {prop: 'selectedCount', label: '已选', width: 80},
  {
    label: '余量',
    width: 80,
    formatter: (row) => {
      const capacity = parseInt(row.capacity, 10) || 0;
      const selected = parseInt(row.selectedCount, 10) || 0;
      return capacity - selected;
    }
  },
  {prop: 'notes', label: '备注', minWidth: 100, showOverflowTooltip: true},
  {
    label: '操作',
    width: 220,
    type: 'action',
    actions: [
      {type: 'primary', label: '编辑', event: 'edit'},
      {type: 'danger', label: '删除', event: 'delete'},
      {type: 'success', label: '查看选课', event: 'viewEnrollments', icon: 'View'}
    ]
  }
];

// --- 方法 ---

// 辅助函数：通过ID从选项列表获取名称
const getNameFromOptions = (id, optionsArray, idKey = 'id', nameKey = 'name') => {
  if (id === null || id === undefined || !optionsArray || !optionsArray.value) return `ID: ${id}`;
  const found = optionsArray.value.find(opt => opt[idKey] === id);
  // 尝试多个可能的名称字段
  if (found) {
    return found[nameKey] || found.courseName || found.realName || found.username || `存在但无名 (ID: ${id})`;
  }
  return `未知 (ID: ${id})`;
};

// 获取学期列表
const fetchTermList = async () => {
  loadingSemesters.value = true;
  try {
    const res = await getAllTerms();
    console.log('学期列表响应:', res);

    // 处理不同的响应格式
    if (res && res.data && Array.isArray(res.data)) {
      termList.value = res.data;
    } else if (Array.isArray(res)) {
      termList.value = res;
    } else if (res && res.code === 200 && Array.isArray(res.data)) {
      termList.value = res.data;
    } else {
      console.warn('未能识别的学期数据格式:', res);
      termList.value = [];
    }

    // 设置默认学期为当前激活的学期
    if (termList.value.length > 0) {
      const activeTerm = termList.value.find(t => t.isCurrent === true || t.current === 1);
      if (activeTerm) {
        searchParams.termId = activeTerm.id;
        scheduleForm.termId = activeTerm.id;
      } else {
        // 没有标记为当前的学期，使用第一个
        searchParams.termId = termList.value[0].id;
        scheduleForm.termId = termList.value[0].id;
      }
      console.log('已选择学期:', searchParams.termId);
    } else {
      ElMessage.warning('未找到任何学期数据');
    }
  } catch (error) {
    console.error("获取学期列表失败:", error);
    ElMessage.error('获取学期列表失败: ' + (error.message || '未知错误'));
    termList.value = [];
  } finally {
    loadingSemesters.value = false;
  }
};

// 获取排课列表并转换名称
const fetchSchedule = async () => {
  loadingSchedule.value = true;
  try {
    // 确保辅助数据已加载,如果未加载，应有提示或机制去加载它们
    // 为简化，这里假设它们已在 onMounted 中被 fetchFormSupportData 填充

    const params = {
      page: currentPage.value,
      size: pageSize.value,
      termId: searchParams.termId,
      dayOfWeek: searchParams.dayOfWeek, // 使用更新后的属性名
      keyword: searchParams.keyword,
    };
    const res = await getSchedulesPage(params); // res 结构: {success, data: {records, total}}

    if (res && res.success && res.data && res.data.records) {
      const rawSchedules = res.data.records;
      total.value = res.data.total || 0;

      scheduleList.value = rawSchedules.map(item => ({
        ...item,
        courseName: getNameFromOptions(item.courseId, courseOptions, 'id', 'courseName'),
        teacherName: getNameFromOptions(item.teacherId, teacherOptions, 'id', 'realName'), // 假设教师的主要显示名是 realName
        classroomName: classroomOptions.value.find(opt => opt.id === item.classroomId)
            ? `${classroomOptions.value.find(opt => opt.id === item.classroomId).building} - ${classroomOptions.value.find(opt => opt.id === item.classroomId).name || classroomOptions.value.find(opt => opt.id === item.classroomId).roomNumber}`
            : `未知教室 (ID: ${item.classroomId})`,
        notes: item.notes || '-', // 如果后端可能不返回notes，给个默认值
        capacity: item.capacity || 0,
        selectedCount: item.selectedCount || 0
      }));
    } else if (res && res.records) { // 兼容没有 success 和 data 外包装，直接是 {records, total} 的情况
      const rawSchedules = res.records;
      total.value = res.total || 0;
      scheduleList.value = rawSchedules.map(item => ({
        ...item,
        courseName: getNameFromOptions(item.courseId, courseOptions, 'id', 'courseName'),
        teacherName: getNameFromOptions(item.teacherId, teacherOptions, 'id', 'realName'),
        classroomName: classroomOptions.value.find(opt => opt.id === item.classroomId)
            ? `${classroomOptions.value.find(opt => opt.id === item.classroomId).building} - ${classroomOptions.value.find(opt => opt.id === item.classroomId).name || classroomOptions.value.find(opt => opt.id === item.classroomId).roomNumber}`
            : `未知教室 (ID: ${item.classroomId})`,
        notes: item.notes || '-',
        capacity: item.capacity || 0,
        selectedCount: item.selectedCount || 0
      }));
    } else {
      console.warn('未能识别的排课数据格式或请求失败:', res);
      scheduleList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取排课列表失败:", error);
    ElMessage.error('获取排课列表失败: ' + (error.message || '未知错误'));
    scheduleList.value = [];
    total.value = 0;
  } finally {
    loadingSchedule.value = false;
  }
};

// 格式化星期几
const formatWeekday = (day) => {
  const found = WEEKDAY_OPTIONS.find(d => d.value === day);
  return found ? found.label : day;
};

// 格式化时间显示
const formatTime = (timeStr) => {
  if (!timeStr || typeof timeStr !== 'string') return '-';
  // 期望格式 YYYY-MM-DD HH:mm:ss
  if (timeStr.length === 19 && timeStr.charAt(10) === ' ' && timeStr.charAt(13) === ':') {
    return timeStr.substring(11, 16); // 提取 HH:mm
  }
  // 如果已经是 HH:mm 格式
  if (timeStr.length === 5 && timeStr.charAt(2) === ':') {
    return timeStr;
  }
  // 尝试通过Date对象解析，作为备选，但要注意时区问题和性能
  try {
    const date = new Date(timeStr);
    if (!isNaN(date.getTime())) {
      return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }
  } catch (e) {
    // 静默处理，返回原始值
  }
  return timeStr; // 返回原始或部分解析的值作为最后的手段
};

// 获取表单所需的下拉选项数据
const fetchFormSupportData = async () => {
  loadingForm.value = true;
  loadingCourses.value = true;
  loadingTeachers.value = true;
  loadingClassrooms.value = true;
  try {
    // 并行获取所有数据
    const [courseRes, teacherRes, classroomRes] = await Promise.all([
      getAllCoursesForSelect({size: 1000}),
      getTeacherList({size: 1000}),
      getAllClassroomsList({size: 1000})
    ]);

    console.log('下拉数据响应:', {
      课程: courseRes,
      教师: teacherRes,
      教室: classroomRes
    });

    // 处理课程数据
    if (courseRes && courseRes.data && courseRes.data.records) {
      courseOptions.value = courseRes.data.records;
    } else if (courseRes && courseRes.records) {
      courseOptions.value = courseRes.records;
    } else if (Array.isArray(courseRes)) {
      courseOptions.value = courseRes;
    } else if (courseRes && Array.isArray(courseRes.data)) {
      courseOptions.value = courseRes.data;
    } else {
      courseOptions.value = [];
      console.warn('未能识别的课程数据格式');
    }

    // 处理教师数据
    if (teacherRes && teacherRes.data && teacherRes.data.records) {
      teacherOptions.value = teacherRes.data.records;
    } else if (teacherRes && teacherRes.records) {
      teacherOptions.value = teacherRes.records;
    } else if (Array.isArray(teacherRes)) {
      teacherOptions.value = teacherRes;
    } else if (teacherRes && Array.isArray(teacherRes.data)) {
      teacherOptions.value = teacherRes.data;
    } else {
      teacherOptions.value = [];
      console.warn('未能识别的教师数据格式');
    }

    // 处理教室数据
    if (classroomRes && classroomRes.data && classroomRes.data.records) {
      classroomOptions.value = classroomRes.data.records;
    } else if (classroomRes && classroomRes.records) {
      classroomOptions.value = classroomRes.records;
    } else if (Array.isArray(classroomRes)) {
      classroomOptions.value = classroomRes;
    } else if (classroomRes && Array.isArray(classroomRes.data)) {
      classroomOptions.value = classroomRes.data;
    } else {
      classroomOptions.value = [];
      console.warn('未能识别的教室数据格式');
    }

  } catch (error) {
    console.error("获取表单下拉选项失败:", error);
    ElMessage.error('加载表单选项失败: ' + (error.message || '未知错误'));
    dialogVisible.value = false; // 获取失败则关闭弹窗
  } finally {
    loadingForm.value = false;
    loadingCourses.value = false;
    loadingTeachers.value = false;
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
  scheduleForm.dayOfWeek = null;
  scheduleForm.startTime = '';
  scheduleForm.endTime = '';
  scheduleForm.classroomId = null;
  scheduleForm.startWeek = 1;
  scheduleForm.endWeek = 16;
  scheduleForm.notes = '';
  scheduleForm.capacity = 0;
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

  // 处理时间字段，确保正确格式
  let startTimeFormatted = '';
  let endTimeFormatted = '';

  if (row.startTime) {
    startTimeFormatted = formatTime(row.startTime);
  }

  if (row.endTime) {
    endTimeFormatted = formatTime(row.endTime);
  }
  
  // 填充表单数据
  Object.assign(scheduleForm, {
    id: row.id,
    termId: row.termId,
    courseId: row.courseId,
    teacherId: row.teacherId,
    dayOfWeek: row.dayOfWeek,
    startTime: startTimeFormatted, // 使用格式化后的时间
    endTime: endTimeFormatted, // 使用格式化后的时间
    classroomId: row.classroomId,
    startWeek: row.startWeek,
    endWeek: row.endWeek,
    notes: row.notes,
    capacity: row.capacity || 0
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
          await updateSchedule(dataToSubmit.id, dataToSubmit);
          ElMessage.success('更新成功');
        } else {
          await addSchedule(dataToSubmit);
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
  // 格式化时间显示
  const startTimeFormatted = formatTime(row.startTime);
  const endTimeFormatted = formatTime(row.endTime);
  
  ElMessageBox.confirm(
      `确定要删除课程【${row.courseName}】在【${formatWeekday(row.dayOfWeek)} ${startTimeFormatted}-${endTimeFormatted}】的排课吗？`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    try {
      await deleteSchedule(row.id);
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
  dayOfWeek: [{required: true, message: '请选择星期', trigger: 'change'}],
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
  capacity: [{required: true, type: 'number', message: '请输入课程容量', trigger: 'blur', min: 0}]
});

// --- 生命周期钩子 ---
onMounted(async () => {
  await fetchTermList(); // 获取学期列表，并设置默认学期 (searchParams.termId)
  await fetchFormSupportData(); // *确保先加载辅助数据*

  if (searchParams.termId) {
    fetchSchedule(); // 使用已更新的 fetchSchedule
  } else {
    ElMessage.info('请先选择一个学期以查看排课');
    loadingSchedule.value = false;
  }
});

watch(() => searchParams.termId, (newTermId) => {
  if (newTermId) {
    currentPage.value = 1;
    fetchSchedule(); // 使用已更新的 fetchSchedule
  } else {
    scheduleList.value = [];
    total.value = 0;
  }
});

// 确保 searchParams 中的 weekday 也改为 dayOfWeek
watch(() => searchParams.dayOfWeek, () => {
  if (searchParams.termId) { // 只有当学期也选定时才触发搜索
    currentPage.value = 1;
    fetchSchedule();
  }
});

// 处理筛选表单搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchSchedule(); // 使用已更新的 fetchSchedule
};

// 处理筛选表单重置
const handleReset = () => {
  if (termList.value.length > 0) {
    const activeTerm = termList.value.find(t => t.isCurrent === true || t.current === 1);
    searchParams.termId = activeTerm ? activeTerm.id : termList.value[0].id;
  } else {
    searchParams.termId = null;
  }
  searchParams.dayOfWeek = null; // 重置 dayOfWeek
  searchParams.keyword = '';
  currentPage.value = 1;
  fetchSchedule(); // 使用已更新的 fetchSchedule
};

</script>

<style scoped>
/* 可以保留特定于此页面的样式，或移至 common.css */
</style>