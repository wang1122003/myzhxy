<template>
  <div class="teacher-course-list">
    <el-card class="main-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2>成绩管理 - 课程选择</h2>
        </div>
      </template>

      <!-- 过滤表单 -->
      <div class="filter-container">
        <el-form :model="filterParams" inline>
          <el-form-item label="学期">
            <el-select
                v-model="filterParams.termId"
                :loading="termsLoading"
                clearable
                placeholder="选择学期"
                style="width: 220px"
            >
              <el-option
                  label="全部学期"
                  value=""
              />
              <el-option
                  v-for="term in termList"
                  :key="term.id"
                  :label="term.termName"
                  :value="term.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="课程">
            <el-input
                v-model="filterParams.keyword"
                clearable
                placeholder="搜索课程名称/代码"
                style="width: 250px"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon>
                <Search/>
              </el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 课程列表 -->
      <div v-loading="loading">
        <el-table
            :data="courseList"
            border
            stripe
            style="width: 100%"
            @row-click="handleRowClick"
        >
          <el-table-column label="课程代码" prop="courseCode" width="150"></el-table-column>
          <el-table-column label="课程名称" min-width="200" prop="courseName"></el-table-column>
          <el-table-column label="授课班级" min-width="180" prop="className"></el-table-column>
          <el-table-column label="所属学期" prop="termName" width="180"></el-table-column>
          <el-table-column align="center" label="学生人数" prop="studentCount" width="100"></el-table-column>
          <el-table-column align="center" label="录入状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.gradeStatus)" size="small">
                {{ formatGradeStatus(scope.row.gradeStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="120">
            <template #default="scope">
              <el-button
                  link
                  type="primary"
                  @click.stop="handleManageGrades(scope.row)"
              >
                成绩录入
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页控件 -->
        <div class="pagination-container">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="totalCourses"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {Search} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {getTeacherCourses} from '@/api/course';
import {getAllTerms} from '@/api/term';
import {useUserStore} from '@/stores/userStore';

const userStore = useUserStore();
const emit = defineEmits(['select-course']);

// 状态数据
const loading = ref(false);
const termsLoading = ref(false);
const courseList = ref([]);
const termList = ref([]);
const totalCourses = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 过滤参数
const filterParams = reactive({
  termId: '',
  keyword: ''
});

// 获取学期列表
const fetchTerms = async () => {
  termsLoading.value = true;
  try {
    const res = await getAllTerms();
    termList.value = res.data || [];

    // 默认选择当前学期（如果有）
    const currentTerm = termList.value.find(t => t.isCurrent);
    if (currentTerm) {
      // 不主动设置，让用户选择
      // filterParams.termId = currentTerm.id;
    }
  } catch (error) {
    console.error('获取学期列表失败:', error);
  } finally {
    termsLoading.value = false;
  }
};

// 获取教师课程列表
const fetchCourses = async () => {
  loading.value = true;
  try {
    // 直接从userStore中获取用户ID
    // userStore.userId可能是函数，需要正确处理
    let teacherId = null;

    if (userStore.userInfo && userStore.userInfo.userId) {
      teacherId = userStore.userInfo.userId;
    } else if (typeof userStore.userId === 'function') {
      teacherId = userStore.userId();
    } else {
      teacherId = userStore.userId;
    }

    const params = {
      page: currentPage.value,
      size: pageSize.value,
      termId: filterParams.termId || undefined,
      keyword: filterParams.keyword || undefined
    };

    // 确保teacherId是有效值
    if (teacherId !== null && teacherId !== undefined) {
      params.teacherId = teacherId;
    }

    console.log('获取课程列表，参数:', params, '用户ID:', teacherId);

    // 发送请求获取课程列表
    const res = await getTeacherCourses(params);

    // 更灵活地处理后端返回的数据格式
    if (res && res.data && res.data.records) {
      // 标准格式: res.data.records 和 res.data.total
      courseList.value = res.data.records;
      totalCourses.value = res.data.total || 0;
    } else if (res && res.records) {
      // 数据直接在res根级别: res.records 和 res.total
      courseList.value = res.records;
      totalCourses.value = res.total || 0;
    } else if (Array.isArray(res)) {
      // 直接返回数组
      courseList.value = res;
      totalCourses.value = res.length;
    } else if (res && res.data && Array.isArray(res.data)) {
      // 数组包装在data字段中
      courseList.value = res.data;
      totalCourses.value = res.data.length;
    } else {
      courseList.value = [];
      totalCourses.value = 0;
      console.warn('无法解析的数据格式:', res);
    }

    console.log('成功加载课程列表，数量:', courseList.value.length);
  } catch (error) {
    console.error('获取课程列表失败:', error);
    ElMessage.error('获取课程列表失败，请检查网络连接或联系管理员');
    courseList.value = [];
    totalCourses.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchCourses();
};

// 处理重置
const handleReset = () => {
  filterParams.termId = '';
  filterParams.keyword = '';
  currentPage.value = 1;
  fetchCourses();
};

// 处理页面大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchCourses();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchCourses();
};

// 处理行点击
const handleRowClick = (row) => {
  handleManageGrades(row);
};

// 处理成绩管理
const handleManageGrades = (row) => {
  emit('select-course', row);
};

// 格式化成绩录入状态
const formatGradeStatus = (status) => {
  const statusMap = {
    'NOT_STARTED': '未开始',
    'IN_PROGRESS': '录入中',
    'COMPLETED': '已完成'
  };
  return statusMap[status] || '未知';
};

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success'
  };
  return typeMap[status] || 'info';
};

onMounted(() => {
  fetchTerms();
  fetchCourses();
});
</script>

<style scoped>
.teacher-course-list {
  width: 100%;
}

.main-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .el-form-item {
    margin-bottom: 10px;
  }
}
</style> 