<template>
  <div class="grade-container">
    <div class="page-header">
      <h2>成绩管理</h2>
      <div class="course-selector">
        <el-select
            v-model="selectedTermId" 
            placeholder="选择学期"
            clearable
            filterable
            :loading="termsLoading"
            style="width: 200px; margin-right: 10px;" 
            @change="handleTermChange"
        >
          <el-option
              v-for="term in termList"
              :key="term.id"
              :label="term.termName"
              :value="term.id"
          />
        </el-select>
        <el-select
            v-model="selectedCourseId"
            clearable
            placeholder="选择课程"
            style="width: 220px;"
            @change="handleCourseChange"
        >
          <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
          />
        </el-select>
      </div>
    </div>

    <!-- 课程未选择提示 -->
    <el-empty
        v-if="!selectedCourseId"
        description="请选择一个课程进行成绩管理"
    />

    <!-- 成绩管理表格 -->
    <template v-else>
      <el-card class="grade-card">
        <div class="filter-container">
          <el-input
              v-model="searchQuery"
              :prefix-icon="Search"
              clearable
              placeholder="搜索学号或姓名"
              style="width: 250px; margin-right: 10px;"
          />
          <el-button link type="primary" @click="saveAllChanges">保存所有修改</el-button>
          <!-- Removed export/import buttons, add back if needed and supported -->
        </div>

        <el-table
            v-loading="studentsLoading"
            :data="filteredStudents"
            empty-text="暂无学生或成绩记录"
            style="width: 100%"
        >
          <el-table-column label="学号" prop="student.studentId" width="150"/>
          <el-table-column label="姓名" prop="student.realName" width="120"/>
          <el-table-column label="班级" prop="student.className" width="180"/>
          <el-table-column label="成绩" width="150">
            <template #default="{ row }">
              <el-input-number
                  v-model="row.score"
                  :max="100"
                  :min="0"
                    :precision="1"
                    :step="1"
                  size="small"
                  style="width: 100px;"
                  @change="markChanged(row)"
                />
              </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.changed" size="small" type="warning">已修改</el-tag>
              <el-tag v-else-if="row.id" size="small" type="success">已录入</el-tag> <!-- Check if record exists -->
              <el-tag v-else size="small" type="info">未录入</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上次更新" min-width="150">
            <template #default="{ row }">
              {{ formatDateTime(row.updateTime) }}
            </template>
          </el-table-column>
          <!-- Removed actions column or simplify if needed -->
        </el-table>

        <!-- Optional Pagination -->
        <el-pagination
            v-if="totalStudents > pageSize"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="totalStudents"
            background
            layout="prev, pager, next, total"
            style="margin-top: 20px; text-align: right;"
            @current-change="handlePageChange"
        />

      </el-card>
      <!-- Removed Tabs and Grade Item Management -->
      <!-- Removed Grade Item Dialog -->
      <!-- Removed Student Grading Dialog (simplified to inline table editing) -->
    </template>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {
  ElButton,
  ElCard,
  ElEmpty,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag
} from 'element-plus'
import {Search} from '@element-plus/icons-vue'
import {getTeacherCourses} from '@/api/course'
import {getCourseScores, recordScore} from '@/api/grade' // Updated API
import {formatDateTime} from '@/utils/formatters'; // Assuming formatter utility
import {getAllTerms} from '@/api/term' // Corrected import: use getAllTerms instead of getTermList

const route = useRoute()
const router = useRouter()

// Data loading status
const studentsLoading = ref(false)
const termsLoading = ref(false) // 加载学期状态

// Course related
const courses = ref([])
const selectedCourseId = ref('')

// Term related
const termList = ref([]) // 学期列表
const selectedTermId = ref('') // 选中的学期ID

// Student Scores related
const students = ref([]) // Stores score records { id?, studentId, courseId, score, updateTime, student: {...} }
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(15) // Adjust page size as needed
const totalStudents = ref(0)

const filteredStudents = computed(() => {
  const searchLower = searchQuery.value.toLowerCase();
  if (!searchLower) {
    return students.value;
  }
  return students.value.filter(student =>
      (student.student?.studentId && student.student.studentId.includes(searchLower)) ||
      (student.student?.realName && student.student.realName.toLowerCase().includes(searchLower))
  );
});

// Fetch teacher's courses
const fetchCourses = async () => {
  try {
    const res = await getTeacherCourses()
    courses.value = res.data || []
  } catch (error) {
    console.error('获取教师课程列表失败', error)
    ElMessage.error('获取教师课程列表失败')
  }
}

// Fetch terms
const fetchTerms = async () => {
  termsLoading.value = true;
  try {
    const res = await getAllTerms({sortByCreateDesc: true}); // Example params if needed
    if (res.code === 200 && Array.isArray(res.data)) {
      termList.value = res.data;
      if (termList.value.length > 0) {
        selectedTermId.value = termList.value[0].id; // Default to the first/latest term
        fetchCourses(); // Fetch courses for the default term
      }
    } else {
      ElMessage.error(res.message || '获取学期列表失败');
    }
  } catch (error) {
    console.error('获取学期列表失败:', error);
    ElMessage.error('获取学期列表失败');
  } finally {
    termsLoading.value = false;
  }
};

// Fetch student scores for the selected course and term
const fetchStudents = async () => {
  if (!selectedCourseId.value || !selectedTermId.value) {
    students.value = [];
    totalStudents.value = 0;
    return;
  }

  studentsLoading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      termId: selectedTermId.value // 添加 termId 参数
    };
    const response = await getCourseScores(selectedCourseId.value, params)
    // Assuming response structure includes student details nested or needs joining
    students.value = (response.data?.list || []).map(s => ({...s, changed: false}));
    totalStudents.value = response.data?.total || 0;
  } catch (error) {
    console.error('获取学生成绩列表失败', error)
    ElMessage.error('获取学生成绩列表失败')
    students.value = [];
    totalStudents.value = 0;
  } finally {
    studentsLoading.value = false
  }
}

// Mark changed when score is edited
const markChanged = (row) => {
  row.changed = true;
}

// Handle course selection change
const handleCourseChange = async () => {
  currentPage.value = 1; // Reset page when course changes
  // Update URL query param for course
  router.push({ query: { ...route.query, courseId: selectedCourseId.value } });
  await fetchStudents(); // Fetch students for new course and current term
}

// Handle term selection change
const handleTermChange = async () => {
  currentPage.value = 1; // Reset page when term changes
  // Update URL query param for term (optional)
  // router.push({ query: { ...route.query, termId: selectedTermId.value } });
  await fetchStudents(); // Fetch students for current course and new term
};

// Handle pagination change
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchStudents();
};

// Save all modified scores
const saveAllChanges = async () => {
  const changedScores = students.value.filter(s => s.changed);
  if (changedScores.length === 0) {
    ElMessage.info('没有需要保存的修改');
    return;
  }

  studentsLoading.value = true;
  let successCount = 0;
  let failCount = 0;

  for (const scoreRecord of changedScores) {
    try {
      const payload = {
        id: scoreRecord.id, // Will be null/undefined for new records
        studentId: scoreRecord.studentId,
        courseId: selectedCourseId.value,
        score: scoreRecord.score
      };
      // Corrected function call: use recordScore
      const response = await recordScore(payload);
      // Update the local record with the ID and reset changed flag if successful
      if (response.code === 200 && response.data) {
        // If it was a new record, backend might return the full object including the new ID
        // Update local state minimally
        scoreRecord.id = response.data.id || scoreRecord.id;
        scoreRecord.updateTime = new Date(); // Or use time from response if available
        scoreRecord.changed = false;
        successCount++;
      } else {
        failCount++;
        ElMessage.error(`保存 ${scoreRecord.student?.realName || scoreRecord.studentId} 成绩失败: ${response.message || '未知错误'}`);
      }
    } catch (error) {
      console.error(`保存学生 ${scoreRecord.student?.realName || scoreRecord.studentId} 成绩失败`, error);
      failCount++;
      ElMessage.error(`保存 ${scoreRecord.student?.realName || scoreRecord.studentId} 成绩失败`);
    }
  }

  studentsLoading.value = false;
  if (failCount > 0) {
    ElMessage.warning(`${successCount}条成绩保存成功，${failCount}条保存失败，请检查`);
  } else {
    ElMessage.success(`${successCount}条成绩保存成功`);
  }
  // Optionally re-fetch to ensure consistency, especially if new records were added
  // await fetchStudents(); 
};

// Initialize data on mount
const initData = async () => {
  await fetchTerms(); // 先获取学期
  await fetchCourses(); // 再获取课程
  // Check URL query for initial selections
  let courseFromQuery = route.query.courseId;
  let termFromQuery = route.query.termId; // Check for term in query too

  let needsFetch = false;

  if (termFromQuery && termList.value.some(t => t.id === termFromQuery)) {
    selectedTermId.value = termFromQuery;
    needsFetch = true;
  } else if (selectedTermId.value) { // If term wasn't in query, but we have a default
     needsFetch = true;
  }

  if (courseFromQuery && courses.value.some(c => c.id === courseFromQuery)) {
    selectedCourseId.value = courseFromQuery;
    needsFetch = true;
  }

  if (needsFetch && selectedCourseId.value && selectedTermId.value) {
    await fetchStudents();
  }
}

onMounted(() => {
  initData()
})

// Watch for external changes to courseId query param (e.g., browser back/forward)
watch(() => route.query.courseId, (newCourseId) => {
  if (newCourseId && newCourseId !== selectedCourseId.value) {
    if (courses.value.some(c => c.id === newCourseId)) {
      selectedCourseId.value = newCourseId;
      handleCourseChange();
    }
  }
});

// Removed all logic related to grade items, tabs, and complex dialogs

</script>

<style scoped>
.grade-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.grade-card {
  margin-bottom: 20px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.el-tag {
  margin-right: 5px;
}
</style> 