<template>
  <div class="grades-container">
    <div class="page-header">
      <h2>我的成绩</h2>
      <div class="filter-container">
        <el-select
            v-model="semesterFilter"
            :loading="loadingSemesters"
            clearable
            filterable
            placeholder="选择学期"
            style="width: 200px;"
            @change="fetchGrades"
        >
          <el-option
              v-for="item in semesters"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </div>
    </div>

    <el-card
        v-loading="loadingGrades"
        class="grades-card"
    >
      <el-table
          :data="grades"
          border
          style="width: 100%"
      >
        <el-table-column
            label="课程代码"
            prop="courseCode"
            width="150"
        />
        <el-table-column
            label="课程名称"
            min-width="200"
            prop="courseName"
        />
        <el-table-column
            label="学分"
            prop="credit"
            width="80"
        />
        <el-table-column
            label="成绩类型"
            prop="gradeType"
            width="120"
        >
          <template #default="scope">
            {{ scope.row.gradeType === 'score' ? '百分制' : '等级制' }}
          </template>
        </el-table-column>
        <el-table-column
            label="成绩"
            prop="gradeValue"
            width="100"
        >
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.gradeValue, scope.row.gradeType)">
              {{ formatGrade(scope.row.gradeValue, scope.row.gradeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
            label="绩点"
            prop="gpa"
            width="80"
        />
        <el-table-column
            label="所属学期"
            prop="semester"
            width="180"
        />
      </el-table>
      <el-empty
          v-if="grades.length === 0 && !loadingGrades"
          description="暂无成绩记录"
      />

      <!-- 可以在这里添加总学分、平均绩点等统计信息 -->
      <div
          v-if="grades.length > 0"
          class="summary-info"
      >
        <!-- 待添加 -->
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {ElCard, ElEmpty, ElMessage, ElOption, ElSelect, ElTable, ElTableColumn, ElTag} from 'element-plus';
import {getStudentGrades} from '@/api/grade'; // 假设 API 在这里
import {getTerms} from '@/api/common'; // 复用获取学期的 API

const loadingGrades = ref(false);
const loadingSemesters = ref(false);
const grades = ref([]);
const semesters = ref([]);
const semesterFilter = ref(null);

// 获取学期列表
const fetchSemesters = async () => {
  loadingSemesters.value = true;
  try {
    const res = await getTerms();
    semesters.value = res.data || [];
    // 默认选择最新的学期 (如果列表有序)
    // if (semesters.value.length > 0) {
    //   semesterFilter.value = semesters.value[0].value;
    // }
  } catch (error) {
    console.error("获取学期列表失败", error);
    // 不阻塞主流程，允许用户手动选择
  } finally {
    loadingSemesters.value = false;
  }
};

// 获取成绩
const fetchGrades = async () => {
  loadingGrades.value = true;
  try {
    const params = {
      semester: semesterFilter.value // 根据选择的学期筛选
    };
    const res = await getStudentGrades(params);
    grades.value = res.data || [];
  } catch (error) {
    console.error("获取成绩失败", error);
    ElMessage.error("获取成绩失败");
    grades.value = []; // 清空列表
  } finally {
    loadingGrades.value = false;
  }
};

// 格式化成绩显示
const formatGrade = (value, type) => {
  if (value === null || value === undefined) return '-';
  if (type === 'level') return value; // 等级制直接显示
  return value.toFixed(1); // 百分制保留一位小数
}

// 根据成绩获取 Tag 类型
const getTagType = (value, type) => {
  if (value === null || value === undefined) return 'info';
  if (type === 'level') {
    const level = value.toUpperCase();
    if (['A', 'A+', 'A-'].includes(level)) return 'success';
    if (['B', 'B+', 'B-'].includes(level)) return 'primary';
    if (['C', 'C+', 'C-'].includes(level)) return 'warning';
    return 'danger'; // D, F 等
  } else {
    if (value >= 90) return 'success';
    if (value >= 80) return 'primary';
    if (value >= 70) return 'warning';
    if (value >= 60) return 'info';
    return 'danger';
  }
}

// 组件挂载后加载数据
onMounted(async () => {
  await fetchSemesters();
  // 初始加载所有成绩或最新学期成绩（取决于产品设计）
  await fetchGrades();
});
</script>

<script>
// 添加标准的 export default
export default {
  name: 'StudentGrades' // 使用更明确的多词名称
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
  margin-bottom: 20px;
}

.grades-card {
  min-height: 300px; /* 防止加载时内容塌陷 */
}

.summary-info {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  /* 添加更多样式展示总学分、绩点等 */
}
</style> 