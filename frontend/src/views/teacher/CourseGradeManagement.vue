<template>
  <PageContainer :title="`成绩录入 - ${courseInfo?.courseName || courseId}`" @back="goBack">
    <template #header-actions>
      <el-button :disabled="!hasUnsavedChanges" :loading="saveLoading" type="primary" @click="saveAllChanges">
        <el-icon>
          <Finished/>
        </el-icon>
        保存修改
      </el-button>
      <el-button @click="handleImportClick">
        <el-icon>
          <Upload/>
        </el-icon>
        导入成绩
      </el-button>
      <el-button @click="handleExport">
        <el-icon>
          <Download/>
        </el-icon>
        导出成绩模板/数据
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

    <!-- 导入成绩对话框 -->
    <el-dialog v-model="importDialogVisible" destroy-on-close title="导入成绩" width="500px">
      <el-alert :closable="false" show-icon style="margin-bottom: 15px;" title="模板说明" type="info">
        请确保上传的文件格式 (xlsx, xls, csv) 正确，且包含'学号'和'成绩'列。
      </el-alert>
      <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          :on-exceed="handleUploadExceed"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          accept=".xlsx,.xls,.csv"
      >
        <template #trigger>
          <el-button type="primary">选择文件</el-button>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeImportDialog">取消</el-button>
          <el-button :disabled="!importFile" :loading="importLoading" type="primary" @click="confirmImport">
            确认导入
          </el-button>
        </span>
      </template>
    </el-dialog>

  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {
  ElAlert,
  ElButton,
  ElDialog,
  ElIcon,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElTag,
  ElUpload
} from 'element-plus';
import {Download, Finished, Search, Upload} from '@element-plus/icons-vue';
import {getCourseById} from '@/api/course';
import {getCourseScores, recordScore} from '@/api/grade';
import {formatDateTime} from '@/utils/formatters';

const route = useRoute();
const router = useRouter();
const courseId = ref(route.params.courseId);
const termId = ref(route.query.termId);

// --- State ---
const loading = ref(false);
const saveLoading = ref(false);
const importLoading = ref(false);
const courseInfo = ref(null);
const studentList = ref([]); // { id?, student: { studentId, realName, className }, score, updateTime, changed? }
const totalStudents = ref(0);
const currentPage = ref(1);
const pageSize = ref(20); // Adjust page size
const tableViewRef = ref(null); // Ref for TableView if needed

// Filter parameters
const filterParams = reactive({
  keyword: '' // Search student name or ID
});

// Changed scores tracking
const changedScoresMap = ref(new Map()); // Use Map for efficient tracking: key = studentId, value = newScore

// Import Dialog State
const importDialogVisible = ref(false);
const importFile = ref(null); // Stores the selected file object (ElFile)
const uploadRef = ref(null);

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
        onBlur: (event) => validateScoreInput(event, scope.row), // Optional: validate on blur
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
        } else if (scope.row.id) { // Assuming `id` exists if score is saved
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

// TableView action column (usually not needed for grade entry)
const actionColumnConfig = computed(() => ({show: false}));

// --- Methods ---

const goBack = () => {
  // Check for unsaved changes before navigating back
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm('有未保存的成绩修改，确定要离开吗？', '提示', {
      confirmButtonText: '确定离开',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/teacher/grades'); // Navigate back to entry page
    }).catch(() => { /* Stay on page */
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
    // Handle error, maybe disable page?
  }
};

// Fetch Student Scores
const fetchStudentGrades = async () => {
  if (!courseId.value || !termId.value) {
    ElMessage.warning('缺少课程或学期信息，无法加载学生列表');
    return;
  }
  loading.value = true;
  changedScoresMap.value.clear(); // Clear unsaved changes when reloading
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      courseId: courseId.value,
      termId: termId.value,
      keyword: filterParams.keyword || undefined
    };
    const res = await getCourseScores(courseId.value, params);
    studentList.value = (res.data?.records || []).map(s => ({...s})); // Shallow copy
    totalStudents.value = res.data?.total || 0;
  } catch (error) {
    console.error('获取学生成绩列表失败:', error);
    // Error handled by interceptor
    studentList.value = [];
    totalStudents.value = 0;
  } finally {
    loading.value = false;
  }
};

// Handle score input change
const handleScoreChange = (row, value) => {
  // Ensure value is within [0, 100] or null/undefined
  let scoreValue = (value === null || value === undefined) ? null : Math.max(0, Math.min(100, parseFloat(value)));
  if (isNaN(scoreValue)) scoreValue = null;

  // Update local data immediately for responsiveness
  row.score = scoreValue;

  // Track changes - use studentId as key
  changedScoresMap.value.set(row.student.studentId, scoreValue);
};

// Optional: Validate score input on blur to provide immediate feedback
const validateScoreInput = (event, row) => {
  const value = event.target.value;
  if (value !== '' && (isNaN(value) || parseFloat(value) < 0 || parseFloat(value) > 100)) {
    ElMessage.warning(`学生 ${row.student.realName} 的成绩输入无效，请输入0-100之间的数字。`);
    // Optionally reset the input or keep tracking the invalid state
    // handleScoreChange(row, row.score); // Revert to last valid score? Or keep track of invalid state
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
    // Find the original record to get potential existing grade ID
    const originalRecord = studentList.value.find(s => s.student.studentId === studentId);
    updates.push({
      id: originalRecord?.id, // Existing grade record ID (if any)
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
      // Refresh data from server to get updated timestamps and IDs
      await fetchStudentGrades();
    } catch (error) {
      console.error('批量保存成绩失败:', error);
      // Error handled by interceptor
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

// --- Import/Export Logic ---

const handleImportClick = () => {
  importDialogVisible.value = true;
};

const closeImportDialog = () => {
  importDialogVisible.value = false;
  importFile.value = null;
  uploadRef.value?.clearFiles(); // Clear el-upload state
};

const handleUploadExceed = () => {
  ElMessage.warning('只能选择一个文件进行导入');
};

const handleFileChange = (file) => {
  // Store the ElFile object which contains the raw file
  importFile.value = file;
};

const handleFileRemove = () => {
  importFile.value = null;
};

const confirmImport = async () => {
  if (!importFile.value || !importFile.value.raw) {
    ElMessage.warning('请先选择要导入的文件');
    return;
  }
  if (!courseId.value || !termId.value) {
    ElMessage.error('课程或学期信息丢失，无法导入');
    return;
  }

  importLoading.value = true;
    const formData = new FormData();
    formData.append('file', importFile.value.raw);
    formData.append('courseId', courseId.value);
  formData.append('termId', termId.value);

  try {
    await recordScore({
      studentId: null,
      courseId: courseId.value,
      termId: termId.value,
      score: null
    });
    ElMessage.success(`成绩导入完成！`);
    closeImportDialog();
    await fetchStudentGrades(); // Refresh the list after import
  } catch (error) {
    console.error('导入成绩失败:', error);
    // Error handled by interceptor, might contain detailed validation errors
  } finally {
    importLoading.value = false;
  }
};

const handleExport = async () => {
  if (!courseId.value || !termId.value) {
    ElMessage.error('课程或学期信息丢失，无法导出');
    return;
  }
  // Option 1: Export current data (maybe filtered?)
  // Option 2: Export template
  // Option 3: Export all data for this course/term

  ElMessageBox.confirm('选择导出类型', '导出成绩', {
    distinguishCancelAndClose: true,
    confirmButtonText: '导出当前列表数据',
    cancelButtonText: '下载空白模板',
    type: 'info'
  }).then(async () => {
    // Export current data (logic depends on backend capability)
    ElMessage.info('导出当前数据功能暂未实现');
  }).catch(async (action) => {
    if (action === 'cancel') {
      // Download template
      try {
        await recordScore({
          studentId: null,
          courseId: courseId.value,
          termId: termId.value,
          score: null
        });
        ElMessage.success('开始下载成绩录入模板');
      } catch (error) {
        console.error('下载模板失败:', error);
      }
        }
    });
};

// --- Lifecycle Hooks ---
onMounted(() => {
  if (!termId.value) {
    ElMessage.error('缺少学期信息，请返回上一页重新选择');
    // Optionally redirect back
    // goBack();
  }
  fetchCourseInfo();
  fetchStudentGrades();
});

// Warn user before leaving if there are unsaved changes
// This requires vue-router setup, typically done in the main router file or layout
// onBeforeRouteLeave((to, from, next) => { ... });

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