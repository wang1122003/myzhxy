<template>
  <PageContainer title="成绩管理 - 课程选择">
    <FilterForm
        :items="filterItems"
        :model="filterParams"
        :show-add-button="false"
        @reset="handleReset"
        @search="handleSearch"
    />

    <TableView
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :action-column-config="actionColumnConfig"
        :columns="tableColumns"
        :data="courseList"
        :loading="loading"
        :total="totalCourses"
        @refresh="fetchCourses"
        @manage-grades="handleManageGrades"
    />
  </PageContainer>
</template>

<script setup>
import {computed, h, onMounted, reactive, ref} from 'vue';
import {useRouter} from 'vue-router';
import {getTeacherCourses} from '@/api/course'; // Corrected: Use course.js, assume it returns grade entry status
import {getAllTerms} from '@/api/term'; // Corrected function name

const router = useRouter();

// --- State ---
const loading = ref(false);
const termsLoading = ref(false);
const courseList = ref([]); // List of courses taught by the teacher
const termList = ref([]);   // List of available terms/semesters
const totalCourses = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// Filter parameters
const filterParams = reactive({
  termId: '', // Selected term ID
  keyword: '' // Course name/code keyword
});

// --- Computed Properties ---

// FilterForm items configuration
const filterItems = computed(() => [
  {
    type: 'select',
    label: '学期',
    prop: 'termId',
    placeholder: '选择学期',
    options: [{value: '', label: '全部学期'}, ...termList.value.map(t => ({value: t.id, label: t.termName}))],
    props: {clearable: true, filterable: true, loading: termsLoading.value, style: {width: '220px'}}
  },
  {
    type: 'input',
    label: '课程',
    prop: 'keyword',
    placeholder: '搜索课程名称/代码',
    props: {clearable: true, style: {width: '250px'}}
  }
]);

// TableView columns configuration
const tableColumns = computed(() => [
  {prop: 'courseCode', label: '课程代码', width: 150},
  {prop: 'courseName', label: '课程名称', minWidth: 200},
  {prop: 'className', label: '授课班级', minWidth: 180}, // Assuming API returns class name
  {prop: 'termName', label: '所属学期', width: 200}, // Assuming API returns term name
  {prop: 'studentCount', label: '学生人数', width: 100, align: 'center'}, // Assuming API returns student count
  {
    prop: 'gradeStatus', label: '录入状态', width: 120, align: 'center',
    slots: {default: (scope) => h('span', formatGradeStatus(scope.row.gradeStatus))} // Or use ElTag
  }
]);

// TableView action column configuration
const actionColumnConfig = computed(() => ({
  width: 120,
  fixed: 'right',
  buttons: [
    {label: '成绩录入', type: 'primary', event: 'manage-grades'}
  ]
}));

// --- Methods ---

// Fetch available terms/semesters
const fetchTerms = async () => {
  termsLoading.value = true;
  try {
    const res = await getAllTerms(); // Corrected
    termList.value = res.data || [];
    // Default to current term if available
    const currentTerm = termList.value.find(t => t.isCurrent);
    if (currentTerm && !filterParams.termId) {
      // filterParams.termId = currentTerm.id; // Don't auto-filter initially
    }
  } catch (error) {
    console.error('获取学期列表失败:', error);
    // Error handled by interceptor
  } finally {
    termsLoading.value = false;
  }
};

// Fetch courses needing grade entry
const fetchCourses = async () => {
  loading.value = true;
  try {
    const params = {
      termId: filterParams.termId || undefined,
      needsGradeEntry: true, // Assuming backend supports this filter
      // teacherId: userStore.userId // Pass teacher ID if needed
    };
    const res = await getTeacherCourses(params); // Corrected, assume this works or adjust filter
    courseList.value = res.data?.records || [];
    totalCourses.value = res.data?.total || 0;
  } catch (error) {
    console.error('获取待录入成绩课程列表失败:', error);
    // Error handled by interceptor
    courseList.value = [];
    totalCourses.value = 0;
  } finally {
    loading.value = false;
  }
};

// Handle search triggered by FilterForm
const handleSearch = () => {
  currentPage.value = 1; // Reset page when searching
  fetchCourses();
};

// Handle reset triggered by FilterForm
const handleReset = () => {
  filterParams.termId = '';
  filterParams.keyword = '';
  currentPage.value = 1;
  fetchCourses();
};

// Format grade status (example)
const formatGradeStatus = (status) => {
  const statusMap = {NOT_STARTED: '未开始', IN_PROGRESS: '录入中', COMPLETED: '已完成'};
  return statusMap[status] || '未知';
};

// Navigate to the specific grade management page for the selected course
const handleManageGrades = (row) => {
  // Navigate to the detailed grade management view, passing courseId and maybe termId
  router.push(`/teacher/courses/${row.courseId}/grades?termId=${row.termId}`); // Adjust route as needed
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchTerms();
  fetchCourses(); // Fetch initial list (maybe without term filter initially)
});

</script>

<style scoped>
/* Add styles if needed */
</style> 