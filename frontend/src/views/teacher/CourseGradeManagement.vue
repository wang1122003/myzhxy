<template>
  <PageContainer :title="`成绩录入 - ${courseInfo?.courseName || courseId}`" @back="goBack">
    <template #header-actions>
      <el-button :disabled="!hasUnsavedChanges" :loading="saveLoading" type="primary" @click="saveAllChanges">
        <el-icon>
          <Finished/>
        </el-icon>
        保存修改
      </el-button>
      <el-button @click="handleExport">
        <el-icon>
          <Download/>
        </el-icon>
        导出成绩数据
      </el-button>
    </template>

    <FilterForm
        :items="filterItems"
        :model="filterParams"
        :show-add-button="false"
        @reset="handleReset"
        @search="handleSearch"
    />

    <TableView
        ref="tableViewRef"
        :action-column-config="actionColumnConfig"
        :columns="tableColumns"
        :data="studentList"
        :loading="loading"
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
        :total="totalStudents"
        @refresh="fetchStudentGrades"
    />

  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref, h} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {
  ElAlert,
  ElButton,
  ElIcon,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElTag,
  ElUpload
} from 'element-plus';
import {Download, Finished, Search, Upload} from '@element-plus/icons-vue';
import {getCourseById} from '@/api/course';
import {getCourseScores, recordScore, exportGrades as exportGradesApi} from '@/api/grade';
import {formatDateTime} from '@/utils/formatters';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import TableView from '@/views/ui/TableView.vue';
import FilterForm from '@/views/ui/AdvancedFilterForm.vue';
import DialogWrapper from '@/views/ui/DialogWrapper.vue';

const route = useRoute();
const router = useRouter();
const courseId = ref(route.params.courseId);
const termId = ref(route.query.termId);

// --- State ---
const loading = ref(false);
const saveLoading = ref(false);
const courseInfo = ref(null);
const studentList = ref([]);
const totalStudents = ref(0);
const currentPage = ref(1);
const pageSize = ref(20);
const tableViewRef = ref(null);

// Filter parameters
const filterParams = reactive({
  keyword: '' // Search student name or ID
});

// Changed scores tracking
const changedScoresMap = ref(new Map());

// --- Computed Properties ---

const hasUnsavedChanges = computed(() => changedScoresMap.value.size > 0);

// FilterForm items
const filterItems = computed(() => [
  {
    type: 'input',
    label: '搜索学生',
    prop: 'keyword',
    placeholder: '学号/姓名',
    props: {clearable: true, prefixIcon: Search, style: {width: '250px'}}
  }
]);

// TableView columns
const tableColumns = computed(() => [
  {prop: 'student.studentId', label: '学号', width: 150},
  {prop: 'student.realName', label: '姓名', width: 120},
  {prop: 'student.className', label: '班级', minWidth: 180},
  {
    prop: 'score',
    label: '成绩 (0-100)',
    width: 180,
    slots: {
      default: (scope) => h(ElInputNumber, {
        modelValue: scope.row.score,
        min: 0,
        max: 100,
        precision: 1,
        step: 1,
        size: 'small',
        controlsPosition: 'right',
        style: 'width: 120px',
        onChange: (value) => handleScoreChange(scope.row, value),
        onBlur: (event) => validateScoreInput(event, scope.row),
      })
    }
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    slots: {
      default: (scope) => {
        if (changedScoresMap.value.has(scope.row.student.studentId)) {
          return h(ElTag, {type: 'warning', size: 'small'}, () => '待保存');
        } else if (scope.row.id) {
          return h(ElTag, {type: 'success', size: 'small'}, () => '已录入');
        }
        return h(ElTag, {type: 'info', size: 'small'}, () => '未录入');
      }
    }
  },
  {
    prop: 'updateTime',
    label: '上次更新',
    minWidth: 160,
    formatter: (row) => row.updateTime ? formatDateTime(row.updateTime) : '-'
  }
]);

// TableView action column
const actionColumnConfig = computed(() => ({show: false}));

// --- Methods ---

const goBack = () => {
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm('有未保存的成绩修改，确定要离开吗？', '提示', {
      confirmButtonText: '确定离开',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/teacher/grades');
    }).catch(() => {
    });
  } else {
    router.push('/teacher/grades');
  }
};

// Fetch Course Info
const fetchCourseInfo = async () => {
  if (!courseId.value) return;
  try {
    const res = await getCourseById(courseId.value);
    courseInfo.value = res.data || {};
  } catch (error) {
    console.error('获取课程信息失败:', error);
  }
};

// Fetch Student Scores
const fetchStudentGrades = async () => {
  if (!courseId.value || !termId.value) {
    ElMessage.warning('缺少课程或学期信息，无法加载学生列表');
    return;
  }
  loading.value = true;
  changedScoresMap.value.clear();
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      courseId: courseId.value,
      termId: termId.value,
      keyword: filterParams.keyword || undefined
    };
    const res = await getCourseScores(courseId.value, params);
    studentList.value = (res.data?.records || []).map(s => ({...s}));
    totalStudents.value = res.data?.total || 0;
  } catch (error) {
    console.error('获取学生成绩列表失败:', error);
    studentList.value = [];
    totalStudents.value = 0;
  } finally {
    loading.value = false;
  }
};

// Handle score input change
const handleScoreChange = (row, value) => {
  let scoreValue = (value === null || value === undefined) ? null : Math.max(0, Math.min(100, parseFloat(value)));
  if (isNaN(scoreValue)) scoreValue = null;
  row.score = scoreValue;
  changedScoresMap.value.set(row.student.studentId, scoreValue);
};

// Validate score input on blur
const validateScoreInput = (event, row) => {
  const value = event.target.value;
  if (value !== '' && (isNaN(value) || parseFloat(value) < 0 || parseFloat(value) > 100)) {
    ElMessage.warning(`学生 ${row.student.realName} 的成绩输入无效，请输入0-100之间的数字。`);
  }
};

// Save all changed scores
const saveAllChanges = async () => {
  if (!hasUnsavedChanges.value) {
    ElMessage.info('没有需要保存的修改');
    return;
  }

  saveLoading.value = true;
  const updates = [];
  changedScoresMap.value.forEach((score, studentId) => {
    const originalRecord = studentList.value.find(s => s.student.studentId === studentId);
    updates.push({
      id: originalRecord?.id,
      studentId: studentId,
      courseId: courseId.value,
      termId: termId.value,
      score: score
    });
  });

    try {
      await Promise.all(updates.map(update => recordScore(update)));
      ElMessage.success(`已成功保存 ${updates.length} 条成绩修改`);
      changedScoresMap.value.clear();
      await fetchStudentGrades();
    } catch (error) {
      console.error('批量保存成绩失败:', error);
    } finally {
      saveLoading.value = false;
    }
};

// Handle search/filter
const handleSearch = () => {
  currentPage.value = 1;
  fetchStudentGrades();
};

const handleReset = () => {
  filterParams.keyword = '';
  currentPage.value = 1;
  fetchStudentGrades();
};

// --- Export Logic ---
const handleExport = async () => {
  if (!courseId.value || !termId.value) {
    ElMessage.error('课程或学期信息丢失，无法导出');
    return;
  }

  ElMessageBox.confirm('确定要导出当前课程的成绩数据吗？', '导出成绩', {
    confirmButtonText: '确定导出',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
      try {
        await exportGradesApi({courseId: courseId.value, termId: termId.value});
        // 浏览器会自动处理下载，这里可以给个提示
        ElMessage.success('已开始导出成绩数据，请稍候...');
      } catch (error) {
        console.error('导出成绩失败:', error);
        ElMessage.error(error.message || '导出成绩失败');
      }
  }).catch(() => {
    ElMessage.info('已取消导出');
  });
};

// --- Lifecycle Hooks ---
onMounted(() => {
  if (!termId.value) {
    ElMessage.error('缺少学期信息，请返回上一页重新选择');
  }
  fetchCourseInfo();
  fetchStudentGrades();
});

</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.filter-area {
  display: flex;
  align-items: center;
}

.action-buttons .el-button + .el-button {
  margin-left: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  text-align: right;
}

/* Quill styles for potential rich text display in details */
.notice-content :deep(.ql-editor) {
  padding: 0;
  line-height: 1.8;
  min-height: 100px;
}

.attachments-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.attachments-section li {
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.file-size {
  margin-left: 10px;
  font-size: 0.9em;
  color: #909399;
}
</style> 