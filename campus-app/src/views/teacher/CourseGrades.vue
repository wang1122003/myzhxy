<template>
  <div class="course-grades-container">
    <el-page-header :title="`课程成绩管理 - ${courseName}`" @back="goBack">
    </el-page-header>

    <el-card v-loading="loading" class="grades-card">
      <template #header>
        <div class="clearfix">
          <span>{{ courseName }} - 成绩录入</span>
          <el-button link style="float: right; padding: 3px 0" type="primary" @click="saveAllChanges">保存所有修改
          </el-button>
        </div>
      </template>

      <el-table
          v-loading="loading"
          :data="students"
          empty-text="暂无学生或成绩记录"
          style="width: 100%"
      >
        <el-table-column label="学号" prop="student.studentId" width="120"/>
        <el-table-column label="姓名" prop="student.realName" width="100"/>
        <el-table-column label="班级" prop="student.className" width="150"/>
        <el-table-column label="成绩" width="150">
          <template #default="{ row }">
            <el-input-number
                v-model="row.score" 
                :min="0"
                :max="100"
                :precision="1"
                :step="1"
                size="small"
                style="width: 100px;"
                @change="markChanged(row)" 
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.changed" size="small" type="warning">已修改</el-tag>
            <el-tag v-else size="small" type="info">未修改</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上次更新时间" min-width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
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
import {exportGrades, getCourseScores, importGrades, recordStudentScore} from '@/api/grade';
import {formatDateTime} from '@/utils/formatters';

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
    const res = await getCourseScores(courseId.value, {
      page: currentPage.value,
      size: pageSize.value
    });

    // 为每个学生添加changed标志，用于跟踪是否有修改
    students.value = (res.data?.list || []).map(scoreRecord => ({
      ...scoreRecord,
      changed: false
    }));

    total.value = res.data?.total || 0;
  } catch (error) {
    console.error('获取学生成绩失败', error);
    ElMessage.error('获取学生成绩失败');
    students.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理分数修改
const markChanged = (row) => {
  row.changed = true;
};

// 处理批量保存
const saveAllChanges = async () => {
  const changedStudents = students.value.filter(s => s.changed);
  if (changedStudents.length === 0) {
    ElMessage.info('没有需要保存的修改');
    return;
  }

  loading.value = true;
  let successCount = 0;
  let failCount = 0;

  for (const student of changedStudents) {
    try {
      const payload = {
        id: student.id,
        studentId: student.studentId,
        courseId: student.courseId,
        score: student.score
      };
      await recordStudentScore(payload);
      student.changed = false;
      successCount++;
    } catch (error) {
      console.error(`保存学生 ${student.student?.realName || student.studentId} 成绩失败`, error);
      failCount++;
    }
  }

  loading.value = false;
  if (failCount > 0) {
    ElMessage.error(`${successCount}条成绩保存成功，${failCount}条保存失败`);
  } else {
    ElMessage.success(`${successCount}条成绩保存成功`);
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