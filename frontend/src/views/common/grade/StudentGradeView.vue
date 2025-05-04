<template>
  <div class="page-container">
    <el-card class="main-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2>成绩查询</h2>
          <div class="header-actions">
            <el-select v-model="filterTerm" clearable placeholder="选择学期" @change="handleTermChange">
              <el-option v-for="term in termOptions" :key="term.value" :label="term.label" :value="term.value"/>
            </el-select>
            <el-select v-model="sortOption" placeholder="排序方式" @change="handleSortChange">
              <el-option v-for="option in sortOptions" :key="option.value" :label="option.label" :value="option.value"/>
            </el-select>
            <el-button type="primary" @click="refreshData">
              <el-icon>
                <Search/>
              </el-icon>
              查询
            </el-button>
          </div>
        </div>
      </template>

      <!-- 成绩统计卡片 -->
      <div class="stats-container">
        <el-row :gutter="20">
          <el-col :md="6" :sm="12" :xs="24">
            <el-card class="stats-card" shadow="hover">
              <template #header>
                <div class="stats-header">课程数量</div>
              </template>
              <div class="stats-value">{{ courseCount }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 切换视图按钮 -->
      <div class="view-toggle">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="table">表格视图</el-radio-button>
          <el-radio-button label="card">卡片视图</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 成绩展示区域 -->
      <div v-loading="loading" class="grades-container">
        <!-- 表格视图 -->
        <div v-if="viewMode === 'table' && !isMobileView">
          <el-table
              :data="tableData"
              :empty-text="tableData.length === 0 ? '暂无成绩数据' : '加载中...'"
              :row-class-name="getRowClassName"
              border
              stripe
              style="width: 100%"
          >
            <el-table-column label="课程名称" min-width="180" prop="courseName" sortable></el-table-column>
            <el-table-column label="总分" min-width="80" prop="totalScore" sortable></el-table-column>
            <el-table-column label="等级" min-width="80" prop="grade" sortable>
              <template #default="scope">
                <span :class="getGradeClass(scope.row.grade)">{{ scope.row.grade }}</span>
              </template>
            </el-table-column>
            <el-table-column label="绩点" min-width="80" prop="gradePoint" sortable></el-table-column>
            <el-table-column label="学期" min-width="120" prop="term" sortable></el-table-column>
          </el-table>
        </div>

        <!-- 卡片视图 -->
        <div v-else class="grades-card-view">
          <div v-if="tableData.length === 0" class="no-data">
            <el-empty description="暂无成绩数据"></el-empty>
          </div>

          <div v-else class="grades-grid">
            <el-card
                v-for="grade in tableData"
                :key="grade.id || Math.random()"
                :body-style="{ padding: '0' }"
                class="grade-card"
                shadow="hover"
            >
              <div :class="getCardColorClass(grade.grade)" class="grade-card-content">
                <div class="grade-card-header">
                  <div class="grade-title">{{ grade.courseName }}</div>
                  <div class="grade-value">{{ grade.grade }}</div>
                </div>

                <div class="grade-card-body">
                  <div class="grade-info-row">
                    <div class="grade-info-item">
                      <div class="info-label">总分</div>
                      <div class="info-value">{{ grade.totalScore }}</div>
                    </div>
                    <div class="grade-info-item">
                      <div class="info-label">绩点</div>
                      <div class="info-value">{{ grade.gradePoint }}</div>
                    </div>
                  </div>

                  <div class="grade-info-row">
                    <div class="grade-info-item">
                      <div class="info-label">学期</div>
                      <div class="info-value">{{ grade.term }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 分页控件 -->
        <div v-if="totalItems > 0" class="pagination-container">
          <el-pagination
              v-if="!isMobileView"
              :current-page="currentPage"
              :page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="totalItems"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          ></el-pagination>

          <el-pagination
              v-else
              :current-page="currentPage"
              :page-size="pageSize"
              :total="totalItems"
              layout="prev, pager, next"
              @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue';
import {Search} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {getMyScores} from '@/api/grade.js';

// 状态数据
const loading = ref(false);
const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);
const isMobileView = ref(window.innerWidth < 768);
const viewMode = ref(window.innerWidth < 768 ? 'card' : 'table');
const filterTerm = ref('');
const sortOption = ref('term-desc');
const termOptions = ref([]);
const originalData = ref([]);

// 排序选项
const sortOptions = [
  {label: '学期 (降序)', value: 'term-desc'},
  {label: '学期 (升序)', value: 'term-asc'},
  {label: '成绩 (降序)', value: 'grade-desc'},
  {label: '成绩 (升序)', value: 'grade-asc'},
  {label: '绩点 (降序)', value: 'gpa-desc'},
  {label: '绩点 (升序)', value: 'gpa-asc'}
];

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobileView.value = window.innerWidth < 768;
  if (window.innerWidth < 768 && viewMode.value === 'table') {
    viewMode.value = 'card';
  }
});

// 获取成绩数据
const fetchData = async () => {
  loading.value = true;
  try {
    const response = await getMyScores({
      page: currentPage.value,
      size: pageSize.value
    });

    let processedData = [];
    if (response && response.data && Array.isArray(response.data)) {
      processedData = response.data.map(formatScoreItem);
      totalItems.value = response.data.length;
    } else if (Array.isArray(response)) {
      processedData = response.map(formatScoreItem);
      totalItems.value = response.length;
    } else {
      console.error('未识别的响应格式:', response);
      ElMessage.warning('无法识别返回的成绩数据格式');
    }

    // 更新原始数据
    originalData.value = [...processedData];

    // 提取学期选项
    extractTermOptions(processedData);

    // 应用筛选和排序
    applyFiltersAndSort(processedData);

    if (processedData.length === 0) {
      ElMessage.info('暂无成绩数据');
    }
  } catch (error) {
    console.error('获取成绩列表失败:', error);
    ElMessage.error('获取成绩列表失败');
    tableData.value = [];
    originalData.value = [];
    totalItems.value = 0;
  } finally {
    loading.value = false;
  }
};

// 提取学期选项
const extractTermOptions = (data) => {
  const terms = new Set(data.map(item => item.term).filter(Boolean));
  termOptions.value = Array.from(terms).map(term => ({
    label: term,
    value: term
  })).sort((a, b) => b.value.localeCompare(a.value)); // 按时间降序排列
};

// 应用筛选和排序
const applyFiltersAndSort = (data = null) => {
  const dataToProcess = data || [...originalData.value];

  // 筛选学期
  let filtered = filterTerm.value
      ? dataToProcess.filter(item => item.term === filterTerm.value)
      : dataToProcess

  // 排序
  filtered = sortData(filtered, sortOption.value);

  // 更新表格数据
  tableData.value = filtered;
};

// 排序数据
const sortData = (data, sortOption) => {
  const [field, direction] = sortOption.split('-');
  const multiplier = direction === 'desc' ? -1 : 1;

  return [...data].sort((a, b) => {
    let valueA, valueB;

    switch (field) {
      case 'term':
        return multiplier * String(a.term || '').localeCompare(String(b.term || ''));
      case 'grade':
        valueA = parseFloat(a.grade) || 0;
        valueB = parseFloat(b.grade) || 0;
        return multiplier * (valueA - valueB);
      case 'gpa':
        valueA = parseFloat(a.gradePoint) || 0;
        valueB = parseFloat(b.gradePoint) || 0;
        return multiplier * (valueA - valueB);
      default:
        return 0;
    }
  });
};

// 格式化成绩项
const formatScoreItem = (item) => {
  // 创建一个包含所有可能字段的课程对象
  const courseInfo = {
    // 尝试直接从item获取课程相关字段
    courseName: item.courseName || item.course_name || '',
    ...(item.course || {}),
    ...(item.courseInfo || {})
  };

  // 处理GPA/成绩
  let gradeValue = item.grade || item.totalScore || item.score || '--';
  let gradePoint = item.gradePoint || item.gpa || convertGradeToPoint(gradeValue) || '--';

  return {
    id: item.id,
    courseName: courseInfo.courseName || '未知课程',
    grade: gradeValue,
    totalScore: item.totalScore ?? '--',
    term: item.termInfo || '--',
    gradePoint: gradePoint,
    // 保存原始数据以便调试
    rawData: item
  };
};

// 将字母成绩或百分制成绩转换为绩点
const convertGradeToPoint = (grade) => {
  if (!grade || grade === '--') return null;

  // 如果已经是绩点格式，直接返回
  if (!isNaN(grade) && parseFloat(grade) <= 4.0) return parseFloat(grade);

  // 处理字母成绩
  const letterMap = {
    'A+': 4.0, 'A': 4.0, 'A-': 3.7,
    'B+': 3.3, 'B': 3.0, 'B-': 2.7,
    'C+': 2.3, 'C': 2.0, 'C-': 1.7,
    'D+': 1.3, 'D': 1.0,
    'F': 0
  };

  if (letterMap[grade]) return letterMap[grade];

  // 处理百分制成绩
  const numGrade = parseFloat(grade);
  if (isNaN(numGrade)) return null;

  if (numGrade >= 90) return 4.0;
  if (numGrade >= 85) return 3.7;
  if (numGrade >= 82) return 3.3;
  if (numGrade >= 78) return 3.0;
  if (numGrade >= 75) return 2.7;
  if (numGrade >= 72) return 2.3;
  if (numGrade >= 68) return 2.0;
  if (numGrade >= 66) return 1.7;
  if (numGrade >= 64) return 1.3;
  if (numGrade >= 60) return 1.0;
  return 0;
};

// 计算统计数据
const courseCount = computed(() => tableData.value.length);

// 按成绩分数获取CSS类名
const getGradeClass = (grade) => {
  if (!grade || grade === '--') return '';

  const numGrade = parseFloat(grade);
  if (isNaN(numGrade)) {
    // 处理字母成绩
    if (grade === 'A' || grade === 'A+' || grade === 'A-') return 'grade-excellent';
    if (grade === 'B' || grade === 'B+' || grade === 'B-') return 'grade-good';
    if (grade === 'C' || grade === 'C+' || grade === 'C-') return 'grade-average';
    if (grade === 'D' || grade === 'D+') return 'grade-pass';
    if (grade === 'F') return 'grade-fail';
    return '';
  }

  // 处理数字成绩
  if (numGrade >= 90) return 'grade-excellent';
  if (numGrade >= 80) return 'grade-good';
  if (numGrade >= 70) return 'grade-average';
  if (numGrade >= 60) return 'grade-pass';
  return 'grade-fail';
};

// 获取卡片背景颜色类
const getCardColorClass = (grade) => {
  if (!grade || grade === '--') return 'card-default';

  const numGrade = parseFloat(grade);
  if (isNaN(numGrade)) {
    // 处理字母成绩
    if (grade === 'A' || grade === 'A+' || grade === 'A-') return 'card-excellent';
    if (grade === 'B' || grade === 'B+' || grade === 'B-') return 'card-good';
    if (grade === 'C' || grade === 'C+' || grade === 'C-') return 'card-average';
    if (grade === 'D' || grade === 'D+') return 'card-pass';
    if (grade === 'F') return 'card-fail';
    return 'card-default';
  }

  // 处理数字成绩
  if (numGrade >= 90) return 'card-excellent';
  if (numGrade >= 80) return 'card-good';
  if (numGrade >= 70) return 'card-average';
  if (numGrade >= 60) return 'card-pass';
  return 'card-fail';
};

// 获取表格行的类名
const getRowClassName = ({row}) => {
  return getGradeClass(row.grade);
};

// 处理学期变化
const handleTermChange = () => {
  applyFiltersAndSort();
};

// 处理排序变化
const handleSortChange = () => {
  applyFiltersAndSort();
};

// 刷新数据
const refreshData = () => {
  currentPage.value = 1;
  fetchData();
};

// 处理页面大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchData();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchData();
};

// 监听筛选和排序变化
watch([filterTerm, sortOption], () => {
  applyFiltersAndSort();
});

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.main-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.stats-container {
  margin-bottom: 20px;
}

.stats-card {
  margin-bottom: 15px;
  transition: transform 0.3s;
}

.stats-card:hover {
  transform: translateY(-5px);
}

.stats-header {
  font-size: 14px;
  color: #606266;
  text-align: center;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  padding: 10px 0;
}

.view-toggle {
  margin-bottom: 15px;
  display: flex;
  justify-content: flex-end;
}

.grades-container {
  min-height: 300px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 卡片视图样式 */
.grades-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.grade-card {
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
}

.grade-card:hover {
  transform: translateY(-5px);
}

.grade-card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-top: 3px solid #409EFF;
}

.grade-card-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.grade-title {
  font-weight: bold;
  font-size: 16px;
  width: 70%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.grade-value {
  font-size: 20px;
  font-weight: bold;
}

.grade-card-body {
  padding: 15px;
  flex-grow: 1;
}

.grade-info-row {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.grade-info-item {
  flex: 1 1 33%;
  min-width: 80px;
  margin-bottom: 5px;
}

.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 3px;
}

.info-value {
  font-size: 14px;
}

/* 成绩颜色 */
.grade-excellent {
  color: #67C23A !important;
}

.grade-good {
  color: #409EFF !important;
}

.grade-average {
  color: #E6A23C !important;
}

.grade-pass {
  color: #F56C6C !important;
}

.grade-fail {
  color: #F56C6C !important;
  font-weight: bold;
}

/* 卡片颜色 */
.card-excellent {
  border-top-color: #67C23A;
}

.card-good {
  border-top-color: #409EFF;
}

.card-average {
  border-top-color: #E6A23C;
}

.card-pass {
  border-top-color: #E6A23C;
}

.card-fail {
  border-top-color: #F56C6C;
}

.card-default {
  border-top-color: #909399;
}

/* 表格行样式 */
:deep(.el-table .grade-excellent) {
  background-color: rgba(103, 194, 58, 0.1);
}

:deep(.el-table .grade-good) {
  background-color: rgba(64, 158, 255, 0.1);
}

:deep(.el-table .grade-average) {
  background-color: rgba(230, 162, 60, 0.1);
}

:deep(.el-table .grade-pass) {
  background-color: rgba(230, 162, 60, 0.05);
}

:deep(.el-table .grade-fail) {
  background-color: rgba(245, 108, 108, 0.1);
}

/* 移动响应式 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .stats-container {
    margin-top: 15px;
  }

  .grades-grid {
    grid-template-columns: 1fr;
  }
}
</style> 