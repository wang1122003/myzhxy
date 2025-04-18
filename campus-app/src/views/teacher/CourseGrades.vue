<template>
  <div class="course-grades-container">
    <el-page-header :title="`课程成绩管理 - ${courseName}`" @back="goBack">
    </el-page-header>

    <el-card v-loading="loading" class="grades-card">
      <div class="card-header">
        <h3>学生成绩列表</h3>
        <div class="header-actions">
          <el-button :disabled="!hasUnsavedChanges" type="success" @click="handleBatchSave">
            <el-icon>
              <Check/>
            </el-icon>
            保存成绩
          </el-button>
          <el-button type="warning" @click="handleExport">
            <el-icon>
              <Download/>
            </el-icon>
            导出成绩
          </el-button>
          <el-button type="primary" @click="handleImport">
            <el-icon>
              <Upload/>
            </el-icon>
            导入成绩
          </el-button>
        </div>
      </div>

      <el-table :data="students" border style="width: 100%">
        <el-table-column type="index" width="50"/>
        <el-table-column label="学号" prop="studentNo" width="120"/>
        <el-table-column label="姓名" prop="studentName" width="100"/>
        <el-table-column label="班级" prop="className" width="150"/>
        <el-table-column label="考勤得分 (20%)" width="150">
          <template #default="scope">
            <el-input-number
                v-model="scope.row.attendanceScore"
                :max="100"
                :min="0"
                size="small"
                @change="handleScoreChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="作业得分 (30%)" width="150">
          <template #default="scope">
            <el-input-number
                v-model="scope.row.homeworkScore"
                :max="100"
                :min="0"
                size="small"
                @change="handleScoreChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="考试得分 (50%)" width="150">
          <template #default="scope">
            <el-input-number
                v-model="scope.row.examScore"
                :max="100"
                :min="0"
                size="small"
                @change="handleScoreChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="总评得分" width="100">
          <template #default="scope">
            <span :class="getTotalScoreClass(scope.row.totalScore)">
              {{ calculateTotalScore(scope.row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="最终评级" width="100">
          <template #default="scope">
            <el-tag :type="getGradeType(scope.row.totalScore)">
              {{ calculateGrade(scope.row.totalScore) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="200">
          <template #default="scope">
            <el-input
                v-model="scope.row.remark"
                placeholder="输入备注"
                @change="handleScoreChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.changed" type="warning">未保存</el-tag>
            <el-tag v-else-if="scope.row.totalScore" type="success">已录入</el-tag>
            <el-tag v-else type="info">未录入</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pagination-container">
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

      <el-empty v-if="students.length === 0 && !loading" description="暂无学生数据"/>
    </el-card>

    <!-- 导入成绩对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入成绩" width="500px">
      <el-upload
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
          accept=".xlsx,.xls,.csv"
          action="#"
          class="upload-demo"
      >
        <template #trigger>
          <el-button type="primary">选择文件</el-button>
        </template>
        <template #tip>
          <div class="el-upload__tip">
            请上传Excel或CSV格式的成绩单文件。文件格式要求：第一列为学号，之后依次为考勤分、作业分、考试分。
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button :disabled="!importFile" type="primary" @click="confirmImport">
            确认导入
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {
  ElButton,
  ElCard,
  ElDialog,
  ElEmpty,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElPageHeader,
  ElPagination,
  ElTable,
  ElTableColumn,
  ElTag,
  ElUpload
} from 'element-plus';
import {Check, Download, Upload} from '@element-plus/icons-vue';
import {exportGrades, getCourseStudents, importGrades, saveStudentGrades} from '@/api/grade';

const route = useRoute();
const router = useRouter();
const courseId = computed(() => route.params.courseId);
const courseName = computed(() => route.query.courseName || '未命名课程');

const loading = ref(false);
const students = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(20);
const hasUnsavedChanges = computed(() => students.value.some(student => student.changed));

// 导入相关
const importDialogVisible = ref(false);
const importFile = ref(null);

// 获取学生名单和成绩
const fetchStudentGrades = async () => {
  loading.value = true;
  try {
    const res = await getCourseStudents({
      courseId: courseId.value,
      page: currentPage.value,
      size: pageSize.value
    });

    // 为每个学生添加changed标志，用于跟踪是否有修改
    students.value = (res.data?.list || []).map(student => ({
      ...student,
      attendanceScore: student.attendanceScore || 0,
      homeworkScore: student.homeworkScore || 0,
      examScore: student.examScore || 0,
      totalScore: student.totalScore || 0,
      changed: false
    }));

    total.value = res.data?.total || 0;
  } catch (error) {
    console.error('获取学生名单和成绩失败', error);
    ElMessage.error('获取学生名单和成绩失败');
  } finally {
    loading.value = false;
  }
};

// 计算总分
const calculateTotalScore = (student) => {
  if (!student) return 0;
  const attendance = student.attendanceScore || 0;
  const homework = student.homeworkScore || 0;
  const exam = student.examScore || 0;

  // 根据权重计算总分
  const totalScore = (attendance * 0.2) + (homework * 0.3) + (exam * 0.5);
  return Math.round(totalScore * 10) / 10; // 保留一位小数
};

// 计算评级
const calculateGrade = (score) => {
  if (score >= 90) return 'A';
  if (score >= 80) return 'B';
  if (score >= 70) return 'C';
  if (score >= 60) return 'D';
  return 'F';
};

// 获取总分颜色类
const getTotalScoreClass = (score) => {
  if (score >= 90) return 'score-a';
  if (score >= 80) return 'score-b';
  if (score >= 70) return 'score-c';
  if (score >= 60) return 'score-d';
  return 'score-f';
};

// 获取评级标签类型
const getGradeType = (score) => {
  if (score >= 90) return 'success';
  if (score >= 80) return 'primary';
  if (score >= 70) return 'warning';
  if (score >= 60) return 'info';
  return 'danger';
};

// 处理分数修改
const handleScoreChange = (student) => {
  student.changed = true;
  student.totalScore = calculateTotalScore(student);
};

// 处理批量保存
const handleBatchSave = async () => {
  const changedStudents = students.value.filter(student => student.changed);
  if (changedStudents.length === 0) {
    ElMessage.info('没有需要保存的成绩修改');
    return;
  }

  try {
    loading.value = true;
    await saveStudentGrades({
      courseId: courseId.value,
      students: changedStudents.map(student => ({
        studentId: student.studentId,
        attendanceScore: student.attendanceScore,
        homeworkScore: student.homeworkScore,
        examScore: student.examScore,
        totalScore: student.totalScore,
        remark: student.remark
      }))
    });

    ElMessage.success('成绩保存成功');

    // 清除修改标记
    students.value.forEach(student => {
      student.changed = false;
    });

  } catch (error) {
    console.error('保存成绩失败', error);
    ElMessage.error('保存成绩失败');
  } finally {
    loading.value = false;
  }
};

// 导出成绩
const handleExport = async () => {
  try {
    loading.value = true;
    await exportGrades(courseId.value);
    ElMessage.success('成绩导出成功');
  } catch (error) {
    console.error('成绩导出失败', error);
    ElMessage.error('成绩导出失败');
  } finally {
    loading.value = false;
  }
};

// 处理导入按钮点击
const handleImport = () => {
  importDialogVisible.value = true;
  importFile.value = null;
};

// 处理文件选择变化
const handleFileChange = (file) => {
  importFile.value = file;
};

// 确认导入
const confirmImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件');
    return;
  }

  try {
    loading.value = true;
    // 构造FormData
    const formData = new FormData();
    formData.append('file', importFile.value.raw);
    formData.append('courseId', courseId.value);

    await importGrades(formData);

    ElMessage.success('成绩导入成功');
    importDialogVisible.value = false;
    // 重新加载数据
    await fetchStudentGrades();
  } catch (error) {
    console.error('成绩导入失败', error);
    ElMessage.error('成绩导入失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm(
        '有未保存的成绩修改，确定要离开吗？',
        '提示',
        {
          confirmButtonText: '确定离开',
          cancelButtonText: '取消',
          type: 'warning'
        }
    ).then(() => {
      router.push({name: 'TeacherCourses'});
    }).catch(() => {
    });
  } else {
    router.push({name: 'TeacherCourses'});
  }
};

// 处理分页
const handleSizeChange = (size) => {
  // 如果有未保存的更改，提示用户
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm(
        '有未保存的成绩修改，更改页面大小将丢失这些修改，是否继续？',
        '提示',
        {
          confirmButtonText: '继续',
          cancelButtonText: '取消',
          type: 'warning'
        }
    ).then(() => {
      pageSize.value = size;
      currentPage.value = 1;
      fetchStudentGrades();
    }).catch(() => {
    });
  } else {
    pageSize.value = size;
    currentPage.value = 1;
    fetchStudentGrades();
  }
};

const handleCurrentChange = (page) => {
  // 如果有未保存的更改，提示用户
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm(
        '有未保存的成绩修改，更改页码将丢失这些修改，是否继续？',
        '提示',
        {
          confirmButtonText: '继续',
          cancelButtonText: '取消',
          type: 'warning'
        }
    ).then(() => {
      currentPage.value = page;
      fetchStudentGrades();
    }).catch(() => {
    });
  } else {
    currentPage.value = page;
    fetchStudentGrades();
  }
};

onMounted(() => {
  fetchStudentGrades();
});
</script>

<script>
export default {
  name: 'CourseGrades'
}
</script>

<style scoped>
.course-grades-container {
  padding: 20px;
}

.grades-card {
  margin-top: 20px;
  min-height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.score-a {
  color: #67c23a;
  font-weight: bold;
}

.score-b {
  color: #409eff;
  font-weight: bold;
}

.score-c {
  color: #e6a23c;
  font-weight: bold;
}

.score-d {
  color: #909399;
}

.score-f {
  color: #f56c6c;
  font-weight: bold;
}
</style> 