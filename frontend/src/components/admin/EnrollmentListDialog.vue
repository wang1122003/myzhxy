<template>
  <el-dialog
      :before-close="handleClose"
      :model-value="modelValue"
      destroy-on-close
      title="活动报名列表"
      top="5vh"
      width="70%"
  >
    <div v-loading="loading">
      <div class="toolbar" style="margin-bottom: 15px; display: flex; justify-content: space-between;">
        <span>共 {{ totalEnrollments }} 人报名</span>
        <!-- Comment out export button -->
        <!--
        <el-button type="primary" :icon="Download" @click="exportEnrollments" :loading="exportLoading">导出列表</el-button>
        -->
      </div>
      <TableView
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :columns="tableColumns"
          :data="enrollmentList"
          :loading="loading"
          :show-action-column="false"
          :total="totalEnrollments"
          @refresh="fetchEnrollments"
      />
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import {ref, watch, computed} from 'vue';
import {ElMessage, ElButton} from 'element-plus';
import {Download} from '@element-plus/icons-vue';
import {getActivityEnrollments} from '@/api/activity'; // Corrected: Use activity.js
// import { adminGetActivityEnrollments, adminExportActivityEnrollments } from '@/api/user'; // Export likely not available or needs specific implementation
import {formatDateTime} from '@/utils/formatters';

const props = defineProps({
  modelValue: Boolean,
  activityId: {
    type: [String, Number],
    required: true
  }
});

const emit = defineEmits(['update:modelValue']);

const loading = ref(false);
const exportLoading = ref(false);
const enrollmentList = ref([]);
const totalEnrollments = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// Table columns configuration
const tableColumns = computed(() => [
  {prop: 'student.studentId', label: '学号', width: 150},
  {prop: 'student.realName', label: '姓名', width: 120},
  {prop: 'student.className', label: '班级', minWidth: 180},
  {
    prop: 'enrollTime',
    label: '报名时间',
    width: 180,
    formatter: (row) => formatDateTime(row.enrollTime)
  },
  // Add other relevant info if available (e.g., contact info)
]);

// Fetch enrollments when dialog opens and activityId changes
watch(() => [props.modelValue, props.activityId], ([newModelValue, newActivityId]) => {
  if (newModelValue && newActivityId) {
    currentPage.value = 1;
    fetchEnrollments();
  } else {
    enrollmentList.value = [];
    totalEnrollments.value = 0;
  }
}, {immediate: true});

// Watch for pagination changes
watch([currentPage, pageSize], () => {
  if (props.modelValue && props.activityId) {
    fetchEnrollments();
  }
});

// Fetch enrollment list data
const fetchEnrollments = async () => {
  if (!props.activityId) return;
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      // Add other filters if needed
    };
    // const res = await adminGetActivityEnrollments(props.activityId, params);
    const res = await getActivityEnrollments(props.activityId, params); // Corrected
    enrollmentList.value = res.data?.records || [];
    totalEnrollments.value = res.data?.total || 0;
  } catch (error) {
    console.error('获取报名列表失败:', error);
    ElMessage.error('获取报名列表失败');
    enrollmentList.value = [];
    totalEnrollments.value = 0;
  } finally {
    loading.value = false;
  }
};

// Export enrollment list
// Comment out export function
/*
const exportEnrollments = async () => {
    exportLoading.value = true;
    try {
        await adminExportActivityEnrollments(props.activityId); // Assume API handles download
        ElMessage.success('开始导出报名列表');
    } catch (error) {
        console.error("导出失败:", error);
    } finally {
        exportLoading.value = false;
    }
};
*/

// Close the dialog
const handleClose = () => {
  emit('update:modelValue', false);
};
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style> 