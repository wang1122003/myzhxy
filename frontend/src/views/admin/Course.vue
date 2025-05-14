<template>
  <PageContainer title="课程管理">
    <template #header-actions>
      <el-button type="primary" @click="handleAddCourse">
        <el-icon>
          <Plus/>
        </el-icon>
        添加课程
      </el-button>
      </template>

    <!-- 搜索和筛选 -->
    <template #filters>
      <FilterForm :model="searchParams" @reset="resetSearch" @search="handleSearch">
        <el-form-item label="课程名称/编号">
          <el-input v-model="searchParams.keyword" clearable placeholder="请输入课程名称或编号" style="width: 250px;"/>
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="searchParams.courseType" clearable placeholder="请选择课程类型" style="width: 150px;">
            <el-option label="全部" value=""/>
            <el-option label="必修课" value="1"/>
            <el-option label="选修课" value="2"/>
            <el-option label="通识课" value="3"/>
          </el-select>
        </el-form-item>
      </FilterForm>
    </template>

      <!-- 课程列表 -->
    <TableView
          :data="courseList"
          v-model="pagination"
          :loading="loading"
          :total="total"
          border
          stripe
          @page-change="handlePageChange"
      >
        <el-table-column
            label="课程名称"
            prop="courseName"
            min-width="180"
        />
        <el-table-column
            label="课程编号"
            prop="courseCode"
            width="120"
        />
        <el-table-column
            label="学分"
            prop="credit"
            width="80"
        />
      <el-table-column
          label="课时"
          prop="hours"
            width="80"
        />
        <el-table-column
            label="课程类型"
            prop="courseType"
            width="100"
        >
          <template #default="scope">{{ formatCourseType(scope.row.courseType) }}</template>
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            width="180"
        >
          <template #default="scope">
            <el-button
                size="small"
                type="primary"
                @click="handleEditCourse(scope.row)"
            >编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDeleteCourse(scope.row)"
            >删除
            </el-button>
          </template>
        </el-table-column>
    </TableView>

    <!-- 添加/编辑 对话框 -->
    <DialogWrapper
        v-model:visible="dialogVisible"
        :title="isEditMode ? '编辑课程' : '添加课程'"
        width="600px"
        @close="handleDialogClose"
    >
      <FormView
          ref="courseFormRef"
          v-loading="formLoading"
          :model="currentCourse"
          :rules="courseFormRules"
          :submitting="submitting"
          @cancel="dialogVisible = false"
          @submit="submitCourseForm"
      >
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="currentCourse.courseName" placeholder="请输入课程名称"/>
        </el-form-item>
        <el-form-item label="课程编号" prop="courseCode">
          <el-input v-model="currentCourse.courseCode" :disabled="isEditMode" placeholder="请输入课程编号"/>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="currentCourse.credit" :min="0" :precision="1" :step="0.5" placeholder="请输入学分"
                           style="width: 180px;"/>
        </el-form-item>
        <el-form-item label="课时" prop="hours">
          <el-input-number v-model="currentCourse.hours" :min="0" :precision="0" :step="1" placeholder="请输入课时"
                           style="width: 180px;"/>
        </el-form-item>
        <el-form-item label="课程类型" prop="courseType">
          <el-select v-model="currentCourse.courseType" placeholder="请选择课程类型" style="width: 100%;"
                     @change="(val) => console.log('课程类型变更:', val)">
            <el-option
                v-for="item in courseTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程描述" prop="introduction">
          <el-input
              v-model="currentCourse.introduction"
              :rows="3"
              placeholder="请输入课程描述"
              type="textarea"
          />
        </el-form-item>
      </FormView>
    </DialogWrapper>
  </PageContainer>
</template>

<script setup>
import {computed, nextTick, onMounted, reactive, ref} from 'vue';
import {
  ElButton,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElSelect,
  ElTableColumn
} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import {addCourse, deleteCourse, getCourseList, updateCourse} from '@/api/course';
import {getTeacherList} from '@/api/user';
import {getAllTerms} from '@/api/term';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import FilterForm from '@/views/ui/AdvancedFilterForm.vue';
import TableView from '@/views/ui/TableView.vue';
import FormView from '@/views/ui/SmartForm.vue';
import DialogWrapper from '@/views/ui/DialogWrapper.vue';

const loading = ref(false);
const courseList = ref([]);
const total = ref(0);
const pagination = ref({
  pageNum: 1,
  pageSize: 10
});
const searchParams = reactive({
  keyword: '',
  courseType: '',
  termId: null,
});

const dialogVisible = ref(false);
const courseFormRef = ref(null);
const currentCourse = ref({
  id: null,
  courseCode: '',
  courseName: '',
  credit: 0.0,
  hours: 0,
  courseType: null,
  introduction: '',
});

const courseFormRules = reactive({
  courseCode: [
    {required: true, message: '请输入课程代码', trigger: 'blur'}
  ],
  courseName: [
    {required: true, message: '请输入课程名称', trigger: 'blur'}
  ],
  credit: [
    {required: true, message: '请输入学分', trigger: 'blur'},
    {type: 'number', message: '学分必须为数字值'},
    {
      validator: (rule, value, callback) => {
        if (value < 0) {
          callback(new Error('学分不能为负数'));
        } else {
          callback();
        }
      }, trigger: 'blur'
    }
  ],
  courseType: [
    {required: true, message: '请选择课程类型', trigger: 'change'}
  ]
});

const isEditMode = computed(() => !!currentCourse.value.id);

const teacherList = ref([]);
const termList = ref([]);
const formLoading = ref(false);
const submitting = ref(false);

// 课程类型选项
const courseTypeOptions = [
  {label: '必修课', value: 1},
  {label: '选修课', value: 2},
  {label: '通识课', value: 3}
];

const fetchCourseList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      keyword: searchParams.keyword || undefined,
      courseType: searchParams.courseType || undefined,
      termId: searchParams.termId || undefined
    };
    console.log('课程查询请求参数:', params);
    
    const res = await getCourseList(params);
    console.log('课程API返回数据:', res);
    
    if (res && res.records !== undefined && res.total !== undefined) {
      console.log('获取课程成功，分页格式1:', res.records);
      if (res.records.length > 0) {
        console.log('第一条课程数据:', JSON.stringify(res.records[0]));
      }
      courseList.value = res.records;
      total.value = res.total;
    } else if (res && res.list !== undefined && res.total !== undefined) {
      console.log('获取课程成功，分页格式2:', res.list);
      courseList.value = res.list;
      total.value = res.total;
    } else if (res && res.data && res.data.records) {
      courseList.value = res.data.records;
      total.value = res.data.total;
    } else if (Array.isArray(res)) {
      console.warn('后端 /courses 返回了纯数组，分页信息可能丢失');
      courseList.value = res;
      total.value = res.length;
    } else if (res && Array.isArray(res.data)) {
      console.warn('后端 /courses 返回了包装在data中的数组');
      courseList.value = res.data;
      total.value = res.data.length;
    } else {
      console.warn('从 /courses 获取的数据格式不符合预期:', res);
      courseList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取课程列表失败", error);
    ElMessage.error(error.message || '加载课程数据时遇到问题，请稍后重试。');
    courseList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const fetchTeachers = async () => {
  try {
    const res = await getTeacherList({userType: 'Teacher'});
    teacherList.value = res.data || [];
  } catch (error) {
    console.error("获取教师列表失败", error);
  }
};

const fetchTerms = async () => {
  try {
    const res = await getAllTerms();
    console.log('获取学期列表返回数据:', res);

    if (Array.isArray(res)) {
      termList.value = res;
      const currentTerm = res.find(t => t.isCurrent === true);
      if (currentTerm && !searchParams.termId) {
        searchParams.termId = currentTerm.code;
        fetchCourseList();
      }
    } else if (res && res.code === 200 && res.data) {
      termList.value = res.data;
      const currentTerm = res.data.find(t => t.current === 1 || t.isCurrent === true);
      if (currentTerm && !searchParams.termId) {
        searchParams.termId = currentTerm.id || currentTerm.code;
        fetchCourseList();
      }
    } else {
      console.error('获取学期列表失败, 响应格式不符合预期:', res);
      ElMessage.error('获取学期列表失败');
    }
  } catch (error) {
    console.error("获取学期列表失败", error);
    ElMessage.error('获取学期列表时发生错误');
  }
};

const handleSearch = () => {
  pagination.value.pageNum = 1;
  console.log('执行搜索，参数:', searchParams);
  fetchCourseList();
};

const handlePageChange = () => {
  fetchCourseList();
};

const resetSearch = () => {
  searchParams.keyword = '';
  searchParams.courseType = '';
  pagination.value.pageNum = 1;
  fetchCourseList();
};

const resetForm = () => {
  if (courseFormRef.value) {
    courseFormRef.value.resetFields();
  }
  currentCourse.value = {
    id: null,
    courseCode: '',
    courseName: '',
    credit: 0.0,
    hours: 0,
    courseType: 1,
    introduction: '',
    status: '1'
  };
};

const handleAddCourse = () => {
  resetForm();
  dialogVisible.value = true;
};

const handleEditCourse = (row) => {
  resetForm();
  const formattedRow = {...row};

  // 如果courseType是字符串类型，转换为数字
  if (typeof formattedRow.courseType === 'string') {
    switch (formattedRow.courseType) {
      case 'COMPULSORY':
        formattedRow.courseType = 1;
        break;
      case 'ELECTIVE':
        formattedRow.courseType = 2;
        break;
      case 'GENERAL':
        formattedRow.courseType = 3;
        break;
      default: {
        // 尝试将字符串转换为数字
        const typeNum = parseInt(formattedRow.courseType);
        if (!isNaN(typeNum)) {
          formattedRow.courseType = typeNum;
        } else {
          // 如果无法识别，使用默认值
          formattedRow.courseType = 1;
        }
      }
    }
  }

  // 确保courseType是数字类型
  if (formattedRow.courseType === null || formattedRow.courseType === undefined) {
    formattedRow.courseType = 1;
  }

  currentCourse.value = formattedRow;

  nextTick(() => {
  dialogVisible.value = true;
  });
};

const handleDeleteCourse = (row) => {
  ElMessageBox.confirm(`确定要删除课程 ${row.courseName} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCourse(row.id);
      ElMessage.success('删除成功');
      fetchCourseList();
    } catch (error) {
      console.error("删除课程失败", error);
      ElMessage.error(error.message || "删除课程失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const submitCourseForm = async () => {
  submitting.value = true;
      try {
        // 创建数据副本，避免直接修改原始数据
        const courseData = {...currentCourse.value};

        // 确保课程类型数据正确 - 将字符串转换为数字
        if (typeof courseData.courseType === 'string') {
          switch (courseData.courseType) {
            case 'COMPULSORY':
              courseData.courseType = 1;
              break;
            case 'ELECTIVE':
              courseData.courseType = 2;
              break;
            case 'GENERAL':
              courseData.courseType = 3;
              break;
              // 尝试从字符串转换为数字
            default:
              const typeNum = parseInt(courseData.courseType);
              if (!isNaN(typeNum)) {
                courseData.courseType = typeNum;
              }
          }
        }

        // 确保其他必要字段存在
        if (courseData.status === undefined) {
          courseData.status = '1'; // 默认启用状态
        }

        console.log('提交课程数据:', courseData);
    
        if (isEditMode.value) {
          await updateCourse(courseData.id, courseData);
          ElMessage.success('课程更新成功');
        } else {
          await addCourse(courseData);
          ElMessage.success('课程添加成功');
        }
        dialogVisible.value = false;
        fetchCourseList();
      } catch (error) {
        console.error('提交表单失败', error);
        if (error.response) {
          console.log('错误状态码:', error.response.status);
          console.log('错误响应数据:', error.response.data);
        }
        ElMessage.error(error.response?.data?.message || '操作失败');
      } finally {
        submitting.value = false;
      }
};

const handleDialogClose = () => {
  resetForm();
};

const formatCourseType = (type) => {
  const types = {
    1: '必修课',
    2: '选修课',
    3: '通识课',
    // 兼容字符串值
    '1': '必修课',
    '2': '选修课',
    '3': '通识课',
    // 兼容旧的字符串枚举值
    'COMPULSORY': '必修课',
    'ELECTIVE': '选修课',
    'GENERAL': '通识课'
  };
  return types[type] || (type ? `未知类型(${type})` : '');
};

onMounted(() => {
  fetchCourseList();
  fetchTeachers();
  fetchTerms();
});

</script>

<script>
export default {
  name: 'CourseManagement'
}
</script>

<style scoped>
.course-management-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.course-list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>