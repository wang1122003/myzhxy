<template>
  <div class="grades-container">
    <el-card class="grades-card">
      <template #header>
        <div class="page-header">
          <span>我的成绩</span>
          <!-- Removed semester filter as termId is likely handled differently or implicitly -->
        </div>
      </template>

      <el-table
          v-loading="loadingGrades"
          :data="grades"
          style="width: 100%"
          empty-text="暂无成绩记录"
      >
        <!-- 课程编号 -->
        <el-table-column
            label="课程编号"
            prop="course.courseCode" 
            width="150"
        />
        <!-- 课程名称 -->
        <el-table-column
            label="课程名称"
            prop="course.courseName"
            min-width="200"
        />
        <!-- 学分 -->
        <el-table-column
            label="学分"
            align="center"
            width="80"
            prop="course.credit"
        />
        <!-- 成绩 -->
        <el-table-column
            label="成绩"
            align="center"
            width="100"
            prop="totalScore"
        >
          <template #default="{ row }">
            <el-tag :type="getTagType(row.totalScore)">
              {{ formatScore(row.totalScore) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 录入时间 -->
        <el-table-column
            align="center"
            label="录入时间"
            width="180"
            prop="updateTime"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <!-- Removed obsolete columns like 'grade', 'totalScore', 'semester' -->
      </el-table>

      <!-- Optional: Summary Info -->
      <div
          v-if="grades.length > 0"
          class="summary-info"
      >
        <!-- GPA Calculation -->
        <span><strong>平均绩点 (GPA):</strong> {{ calculatedGPA }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {ElCard, ElMessage, ElTable, ElTableColumn, ElTag} from 'element-plus';
import {getMyGrades} from '@/api/grade'; // Updated API function
// Removed getTerms API import if semester filter is gone
import {formatDateTime} from '@/utils/formatters'; // Assuming a utility for date formatting

const loadingGrades = ref(false);
const grades = ref([]);
// Removed semesters and semesterFilter refs

// Fetch grades for the logged-in student
const fetchGrades = async () => {
  loadingGrades.value = true;
  try {
    // Removed semester parameter if no longer needed
    const params = {};
    const res = await getMyGrades(params);
    // Assuming the backend now returns course details nested within each score object
    // Adjust if the structure is different (e.g., need separate call to get course details)
    grades.value = res.data || [];
  } catch (error) {
    console.error("获取成绩失败", error);
    ElMessage.error("获取成绩失败");
    grades.value = []; // Clear list on error
  } finally {
    loadingGrades.value = false;
  }
};

// Convert numeric score to grade point (4.0 scale)
const scoreToGP = (score) => {
  if (score === null || score === undefined) return 0;
  if (score >= 90) return 4.0;
  if (score >= 80) return 3.0;
  if (score >= 70) return 2.0;
  if (score >= 60) return 1.0;
  return 0.0;
}

// Calculate GPA using computed property
const calculatedGPA = computed(() => {
  let totalCredits = 0;
  let totalWeightedGP = 0;

  grades.value.forEach(grade => {
    const credit = grade.course?.credit; // Use optional chaining
    const score = grade.totalScore;

    if (credit !== null && credit !== undefined && credit > 0 && score !== null && score !== undefined) {
      const gradePoint = scoreToGP(score);
      totalCredits += credit;
      totalWeightedGP += gradePoint * credit;
    }
  });

  if (totalCredits === 0) {
    return 'N/A'; // Avoid division by zero
  }

  const gpa = totalWeightedGP / totalCredits;
  return gpa.toFixed(2); // Format to 2 decimal places
});

// Format score display
const formatScore = (value) => {
  if (value === null || value === undefined) return '-';
  // Ensure value is treated as a number for toFixed
  const numericValue = Number(value);
  if (isNaN(numericValue)) return '-';
  return numericValue.toFixed(1); // Keep one decimal place for score
}

// Get tag type based on score value
const getTagType = (value) => {
  if (value === null || value === undefined) return 'info';
  const numericValue = Number(value); // Ensure comparison is done with number
  if (isNaN(numericValue)) return 'info';
  if (numericValue >= 90) return 'success';
  if (numericValue >= 80) return 'primary'; // Changed from 'default' or assumed type to 'primary'
  if (numericValue >= 70) return 'warning';
  if (numericValue >= 60) return 'info'; // Changed from 'gray' or assumed type to 'info'
  return 'danger'; // Below 60
}

// Component mounted: fetch initial data
onMounted(async () => {
  // Removed fetchSemesters call
  await fetchGrades();
});

</script>

<script>
// Keep standard export default
export default {
  name: 'StudentGrades'
}
</script>

<style scoped>
.grades-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.grades-card {
  min-height: 300px;
}

.summary-info {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  text-align: right; /* Align GPA to the right */
  font-size: 14px;
}
</style> 