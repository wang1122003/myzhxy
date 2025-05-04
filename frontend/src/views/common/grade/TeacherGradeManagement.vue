<template>
  <div class="teacher-grade-management">
    <el-card class="main-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2>{{ course.courseName }} - 成绩管理</h2>
          <div class="course-info">
            <el-tag size="small">{{ course.termName }}</el-tag>
            <el-tag size="small" type="info">{{ course.className }}</el-tag>
            <el-tag size="small" type="success">学生: {{ studentList.length }}人</el-tag>
          </div>
        </div>
      </template>

      <!-- 操作工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
              v-model="searchKeyword"
              clearable
              placeholder="搜索学生姓名/学号"
              style="width: 250px;"
              @input="handleSearch"
          >
            <template #prefix>
              <el-icon>
                <Search/>
              </el-icon>
            </template>
          </el-input>
        </div>
        <div class="toolbar-right">
          <el-button :disabled="!hasChanges" type="primary" @click="handleBatchSubmit">
            提交成绩修改
          </el-button>
          <el-button type="success" @click="exportGrades">
            导出成绩表
          </el-button>
          <el-button @click="resetScores">
            重置修改
          </el-button>
        </div>
      </div>

      <!-- 成绩表格 -->
      <div v-loading="loading" class="grade-table-container">
        <el-table
            ref="gradeTable"
            :cell-class-name="getCellClass"
            :data="filteredStudents"
            :empty-text="loading ? '加载中...' : '暂无学生数据'"
            border
            stripe
            style="width: 100%"
        >
          <el-table-column label="学号" prop="studentId" sortable width="120"></el-table-column>
          <el-table-column label="姓名" prop="studentName" sortable width="120"></el-table-column>
          <el-table-column label="班级" prop="className" width="150"></el-table-column>

          <!-- 平时成绩 -->
          <el-table-column label="平时成绩" width="120">
            <template #default="scope">
              <div class="editable-cell">
                <el-input
                    v-if="scope.row.editing"
                    v-model="scope.row.regularScore"
                    :disabled="scope.row.locked"
                    max="100"
                    min="0"
                    type="number"
                    @change="handleScoreChange(scope.row, 'regularScore')"
                ></el-input>
                <span v-else>{{ formatScore(scope.row.regularScore) }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- 期中成绩 -->
          <el-table-column label="期中成绩" width="120">
            <template #default="scope">
              <div class="editable-cell">
                <el-input
                    v-if="scope.row.editing"
                    v-model="scope.row.midtermScore"
                    :disabled="scope.row.locked"
                    max="100"
                    min="0"
                    type="number"
                    @change="handleScoreChange(scope.row, 'midtermScore')"
                ></el-input>
                <span v-else>{{ formatScore(scope.row.midtermScore) }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- 期末成绩 -->
          <el-table-column label="期末成绩" width="120">
            <template #default="scope">
              <div class="editable-cell">
                <el-input
                    v-if="scope.row.editing"
                    v-model="scope.row.finalScore"
                    :disabled="scope.row.locked"
                    max="100"
                    min="0"
                    type="number"
                    @change="handleScoreChange(scope.row, 'finalScore')"
                ></el-input>
                <span v-else>{{ formatScore(scope.row.finalScore) }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- 总评成绩 -->
          <el-table-column label="总评成绩" sortable width="120">
            <template #default="scope">
              <div :class="getScoreClass(scope.row.totalScore)" class="editable-cell">
                <el-input
                    v-if="scope.row.editing && scope.row.manualTotal"
                    v-model="scope.row.totalScore"
                    :disabled="scope.row.locked"
                    max="100"
                    min="0"
                    type="number"
                    @change="handleScoreChange(scope.row, 'totalScore')"
                ></el-input>
                <span v-else>{{ formatScore(scope.row.totalScore) }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- 等级 -->
          <el-table-column label="等级" width="120">
            <template #default="scope">
              <div :class="getGradeClass(scope.row.grade)" class="grade-cell">
                {{ scope.row.grade || formatGradeFromScore(scope.row.totalScore) }}
              </div>
            </template>
          </el-table-column>

          <!-- 备注 -->
          <el-table-column label="备注" min-width="150">
            <template #default="scope">
              <div class="editable-cell">
                <el-input
                    v-if="scope.row.editing"
                    v-model="scope.row.comment"
                    :disabled="scope.row.locked"
                    placeholder="输入备注"
                ></el-input>
                <span v-else>{{ scope.row.comment || '-' }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- 操作 -->
          <el-table-column fixed="right" label="操作" width="160">
            <template #default="scope">
              <div class="operate-cell">
                <el-button
                    v-if="!scope.row.editing"
                    :disabled="scope.row.locked"
                    link
                    type="primary"
                    @click="enableEditing(scope.row)"
                >
                  编辑
                </el-button>
                <template v-else>
                  <el-button
                      :disabled="scope.row.locked"
                      link
                      type="success"
                      @click="saveScore(scope.row)"
                  >
                    保存
                  </el-button>
                  <el-button
                      :disabled="scope.row.locked"
                      link
                      type="danger"
                      @click="cancelEditing(scope.row)"
                  >
                    取消
                  </el-button>
                </template>
                <el-button
                    link
                    type="info"
                    @click="toggleLock(scope.row)"
                >
                  {{ scope.row.locked ? '解锁' : '锁定' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 批量提交对话框 -->
      <el-dialog
          v-model="batchSubmitDialogVisible"
          title="提交成绩修改"
          width="500px"
      >
        <div class="batch-submit-content">
          <p>您即将提交 <strong>{{ changedStudents.length }}</strong> 名学生的成绩修改，确认提交吗？</p>
          <p class="warning">提交后将无法撤销，请确认成绩无误。</p>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="batchSubmitDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitGradeChanges">确认提交</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue';
import {Search} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {getCourseStudents, saveStudentGrades} from '@/api/grade';

const props = defineProps({
  course: {
    type: Object,
    required: true
  }
});

// 状态数据
const loading = ref(false);
const studentList = ref([]);
const originalStudentData = ref([]);
const searchKeyword = ref('');
const batchSubmitDialogVisible = ref(false);

// 计算属性：过滤后的学生列表
const filteredStudents = computed(() => {
  if (!searchKeyword.value) return studentList.value;

  const keyword = searchKeyword.value.toLowerCase();
  return studentList.value.filter(student => {
    return student.studentName.toLowerCase().includes(keyword) ||
        student.studentId.toLowerCase().includes(keyword);
  });
});

// 计算属性：有修改的学生
const changedStudents = computed(() => {
  return studentList.value.filter(student => student.changed);
});

// 计算属性：是否有修改
const hasChanges = computed(() => {
  return changedStudents.value.length > 0;
});

// 获取课程学生列表和成绩
const fetchStudents = async () => {
  loading.value = true;
  try {
    console.log('正在获取课程学生成绩数据，课程信息:', props.course);

    // 确保课程ID存在
    if (!props.course || (!props.course.courseId && !props.course.id)) {
      ElMessage.error('无效的课程信息，无法获取学生列表');
      loading.value = false;
      return;
    }

    // 使用课程对象中的id字段作为courseId参数
    const courseId = props.course.courseId || props.course.id;

    const res = await getCourseStudents({
      courseId: courseId,
      termId: props.course.termId
    });

    // 打印原始响应数据以便调试
    console.log('课程学生成绩原始响应数据:', res);

    // 更灵活地处理响应数据结构
    let students = [];

    // 检查不同的数据结构可能性
    if (res && Array.isArray(res)) {
      // 直接是数组
      students = res;
    } else if (res && res.data && Array.isArray(res.data)) {
      // 有data字段包含数组
      students = res.data;
    } else if (res && res.records && Array.isArray(res.records)) {
      // 有records字段包含数组
      students = res.records;
    } else if (res && typeof res === 'object') {
      // 如果是其他对象结构，尝试找到可能的数组字段
      const possibleArrayFields = Object.values(res).filter(v => Array.isArray(v));
      if (possibleArrayFields.length > 0) {
        students = possibleArrayFields[0];
      }
    }

    console.log('解析后的学生数据数组:', students);

    // 处理学生数据，添加编辑状态
    const processedStudents = students.map(student => ({
      ...student,
      editing: false,
      locked: false,
      changed: false,
      manualTotal: false, // 是否手动设置总分
      // 格式化数字字段
      regularScore: student.regularScore !== null ? parseFloat(student.regularScore) : null,
      midtermScore: student.midtermScore !== null ? parseFloat(student.midtermScore) : null,
      finalScore: student.finalScore !== null ? parseFloat(student.finalScore) : null,
      totalScore: student.totalScore !== null ? parseFloat(student.totalScore) : null
    }));

    studentList.value = processedStudents;

    // 保存原始数据，用于撤销编辑
    originalStudentData.value = JSON.parse(JSON.stringify(processedStudents));

    console.log('成功获取学生成绩数据，数量:', processedStudents.length);
  } catch (error) {
    console.error('获取学生列表失败:', error);
    ElMessage.error('获取学生列表失败');
    studentList.value = [];
  } finally {
    loading.value = false;
  }
};

// 启用编辑模式
const enableEditing = (student) => {
  student.editing = true;
  // 备份数据用于取消编辑
  student._backup = JSON.parse(JSON.stringify(student));
};

// 取消编辑
const cancelEditing = (student) => {
  // 恢复备份数据
  if (student._backup) {
    Object.keys(student._backup).forEach(key => {
      if (key !== '_backup' && key !== 'editing') {
        student[key] = student._backup[key];
      }
    });
  }
  student.editing = false;

  // 如果是新增的成绩，可能需要重新计算总分
  if (!student.manualTotal) {
    calculateTotalScore(student);
  }
};

// 保存单个学生成绩
const saveScore = (student) => {
  student.editing = false;
  student.changed = true;

  ElMessage.success('成绩已修改，请点击"提交成绩修改"按钮保存到服务器');
};

// 切换锁定状态
const toggleLock = (student) => {
  student.locked = !student.locked;

  if (student.locked) {
    student.editing = false;
    ElMessage.info('已锁定该学生成绩，无法编辑');
  } else {
    ElMessage.info('已解锁该学生成绩，可以编辑');
  }
};

// 处理分数变化
const handleScoreChange = (student, field) => {
  // 验证分数范围
  if (student[field] !== null) {
    let score = parseFloat(student[field]);
    if (isNaN(score)) {
      student[field] = null;
    } else {
      // 限制在0-100范围内
      score = Math.max(0, Math.min(100, score));
      student[field] = score;
    }
  }

  // 如果修改的不是总分，则重新计算总分
  if (field !== 'totalScore' && !student.manualTotal) {
    calculateTotalScore(student);
  } else if (field === 'totalScore') {
    // 标记为手动设置总分
    student.manualTotal = true;
  }
};

// 计算总分
const calculateTotalScore = (student) => {
  // 假设评分规则：平时成绩30%，期中30%，期末40%
  const regularWeight = 0.3;
  const midtermWeight = 0.3;
  const finalWeight = 0.4;

  let totalScore = null;

  const regularScore = student.regularScore !== null ? student.regularScore : 0;
  const midtermScore = student.midtermScore !== null ? student.midtermScore : 0;
  const finalScore = student.finalScore !== null ? student.finalScore : 0;

  // 如果有任意一个成绩，就计算总分
  if (student.regularScore !== null || student.midtermScore !== null || student.finalScore !== null) {
    totalScore = (
        regularScore * regularWeight +
        midtermScore * midtermWeight +
        finalScore * finalWeight
    );
    totalScore = parseFloat(totalScore.toFixed(1)); // 保留一位小数
  }

  student.totalScore = totalScore;
};

// 处理批量提交
const handleBatchSubmit = () => {
  if (changedStudents.value.length === 0) {
    ElMessage.warning('没有需要提交的成绩修改');
    return;
  }

  batchSubmitDialogVisible.value = true;
};

// 提交成绩修改到服务器
const submitGradeChanges = async () => {
  loading.value = true;
  try {
    // 获取正确的课程ID
    const courseId = props.course.courseId || props.course.id;

    // 准备提交的数据
    const gradesToSubmit = changedStudents.value.map(student => ({
      id: student.id, // 成绩记录ID
      studentId: student.studentId,
      courseId: courseId,
      regularScore: student.regularScore,
      midtermScore: student.midtermScore,
      finalScore: student.finalScore,
      totalScore: student.totalScore,
      grade: formatGradeFromScore(student.totalScore),
      comment: student.comment
    }));

    await saveStudentGrades(gradesToSubmit);

    ElMessage.success('成绩提交成功');

    // 更新原始数据
    originalStudentData.value = JSON.parse(JSON.stringify(studentList.value));

    // 重置变更标记
    studentList.value.forEach(student => {
      student.changed = false;
    });

    batchSubmitDialogVisible.value = false;
  } catch (error) {
    console.error('提交成绩失败:', error);
    ElMessage.error('提交成绩失败');
  } finally {
    loading.value = false;
  }
};

// 导出成绩表
const exportGrades = () => {
  ElMessage.info('导出成绩表功能尚未实现');
};

// 重置所有修改
const resetScores = () => {
  if (!hasChanges.value) {
    ElMessage.info('没有需要重置的修改');
    return;
  }

  ElMessageBox.confirm('确定要重置所有未提交的成绩修改吗？', '重置确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 恢复原始数据
    studentList.value = JSON.parse(JSON.stringify(originalStudentData.value));
    ElMessage.success('已重置所有修改');
  }).catch(() => {
    // 用户取消操作
  });
};

// 处理搜索
const handleSearch = () => {
  // 使用计算属性自动过滤
};

// 格式化分数显示
const formatScore = (score) => {
  if (score === null || score === undefined) return '-';
  return parseFloat(score).toFixed(1);
};

// 根据分数计算等级
const formatGradeFromScore = (score) => {
  if (score === null || score === undefined) return '-';

  score = parseFloat(score);
  if (isNaN(score)) return '-';

  if (score >= 90) return 'A';
  if (score >= 85) return 'A-';
  if (score >= 82) return 'B+';
  if (score >= 78) return 'B';
  if (score >= 75) return 'B-';
  if (score >= 72) return 'C+';
  if (score >= 68) return 'C';
  if (score >= 65) return 'C-';
  if (score >= 62) return 'D+';
  if (score >= 60) return 'D';
  return 'F';
};

// 获取单元格样式
const getCellClass = ({row, column}) => {
  if (column.property === 'totalScore') {
    return getScoreClass(row.totalScore);
  }

  // 如果已修改但未提交
  if (row.changed) {
    return 'changed-cell';
  }

  return '';
};

// 获取分数样式
const getScoreClass = (score) => {
  if (score === null || score === undefined) return '';

  score = parseFloat(score);
  if (isNaN(score)) return '';

  if (score >= 90) return 'score-excellent';
  if (score >= 80) return 'score-good';
  if (score >= 70) return 'score-average';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
};

// 获取等级样式
const getGradeClass = (grade) => {
  if (!grade || grade === '-') return '';

  if (grade.startsWith('A')) return 'grade-excellent';
  if (grade.startsWith('B')) return 'grade-good';
  if (grade.startsWith('C')) return 'grade-average';
  if (grade.startsWith('D')) return 'grade-pass';
  if (grade === 'F') return 'grade-fail';
  return '';
};

// 监听课程变化
watch(() => props.course, () => {
  fetchStudents();
}, {immediate: true});

onMounted(() => {
  fetchStudents();
});
</script>

<style scoped>
.teacher-grade-management {
  width: 100%;
}

.main-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.course-info {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.toolbar-left, .toolbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.grade-table-container {
  overflow-x: auto;
}

.editable-cell {
  padding: 5px 0;
}

.grade-cell {
  padding: 5px 8px;
  border-radius: 4px;
  font-weight: bold;
  display: inline-block;
}

.operate-cell {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
}

.batch-submit-content {
  padding: 10px 0;
}

.warning {
  color: #F56C6C;
  font-weight: bold;
}

/* 成绩颜色样式 */
.score-excellent, .grade-excellent {
  color: #67C23A !important;
}

.score-good, .grade-good {
  color: #409EFF !important;
}

.score-average, .grade-average {
  color: #E6A23C !important;
}

.score-pass, .grade-pass {
  color: #E6A23C !important;
}

.score-fail, .grade-fail {
  color: #F56C6C !important;
  font-weight: bold;
}

/* 修改单元格样式 */
.changed-cell {
  background-color: rgba(103, 194, 58, 0.1);
}

/* 响应式样式 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
  }

  .toolbar-right {
    justify-content: flex-start;
  }
}
</style> 