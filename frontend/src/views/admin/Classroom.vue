<template>
  <div class="classroom-management">
    <el-card class="page-container">
      <template #header>
        <div class="header">
          <span>教室管理</span>
          <el-button :icon="Plus" type="primary" @click="handleAdd">添加教室</el-button>
        </div>
      </template>

      <!-- 筛选区域 -->
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="教室名称">
          <el-input v-model="filters.name" clearable placeholder="请输入教室名称"/>
        </el-form-item>
        <el-form-item label="教学楼">
          <el-input v-model="filters.building" clearable placeholder="请输入教学楼名称"/>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.type" clearable placeholder="请选择教室类型">
            <el-option label="全部" value=""/>
            <el-option label="普通教室" value="REGULAR"/>
            <el-option label="机房" value="COMPUTER_LAB"/>
            <el-option label="实验室" value="LABORATORY"/>
            <el-option label="会议室" value="MEETING_ROOM"/>
            <el-option label="其他" value="OTHER"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="请选择状态">
            <el-option label="全部" value=""/>
            <el-option label="可用" value="AVAILABLE"/>
            <el-option label="维修中" value="UNDER_MAINTENANCE"/>
            <el-option label="停用" value="DECOMMISSIONED"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="fetchClassrooms">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 教室列表 -->
      <el-table
          :data="classroomList"
          v-loading="loading"
          style="width: 100%"
      >
        <el-table-column label="教室名称" min-width="150" prop="name"/>
        <el-table-column label="教学楼" prop="building" width="120"/>
        <el-table-column label="类型" prop="type" width="120">
          <template #default="scope">{{ formatClassroomType(scope.row.type) }}</template>
        </el-table-column>
        <el-table-column label="容量" prop="capacity" width="80"/>
        <el-table-column label="设施" min-width="180" prop="facilities" show-overflow-tooltip/>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getClassroomStatusType(scope.row.status)">
              {{ formatClassroomStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button
                :icon="Edit"
                circle
                size="small"
                type="primary"
                @click="handleEdit(scope.row)"
            />
            <el-button
                :icon="Delete"
                circle
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
            />
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无教室数据"/>
        </template>
      </el-table>

      <!-- 分页 -->
      <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          class="pagination-container"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 添加/编辑 对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="isEditMode ? '编辑教室' : '添加教室'"
        width="600px"
        @close="handleDialogClose"
    >
      <el-form ref="classroomFormRef" :model="currentClassroom" :rules="formRules" label-width="100px">
        <el-form-item label="教室名称" prop="name">
          <el-input v-model="currentClassroom.name" placeholder="请输入教室名称"/>
        </el-form-item>
        <el-form-item label="教学楼" prop="building">
          <el-input v-model="currentClassroom.building" placeholder="请输入教学楼名称"/>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="currentClassroom.type" placeholder="请选择教室类型" style="width: 100%;">
            <el-option label="普通教室" value="REGULAR"/>
            <el-option label="机房" value="COMPUTER_LAB"/>
            <el-option label="实验室" value="LABORATORY"/>
            <el-option label="会议室" value="MEETING_ROOM"/>
            <el-option label="其他" value="OTHER"/>
          </el-select>
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="currentClassroom.capacity" :min="1" placeholder="请输入容量" style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="设施" prop="facilities">
          <el-input v-model="currentClassroom.facilities" :rows="3" placeholder="请输入教室设施，如：投影仪、空调"
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="currentClassroom.status">
            <el-radio label="AVAILABLE">可用</el-radio>
            <el-radio label="UNDER_MAINTENANCE">维修中</el-radio>
            <el-radio label="DECOMMISSIONED">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button :loading="submitting" type="primary" @click="handleSubmit">
            {{ isEditMode ? '保存' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {
  ElButton,
  ElCard,
  ElDialog,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElRadio,
  ElRadioGroup,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag
} from 'element-plus';
import {Delete, Edit, Plus, Search} from '@element-plus/icons-vue';
import {addClassroom, deleteClassroom, getClassroomsPage, updateClassroom} from '@/api/classroom';

// Loading states
const loading = ref(false);
const submitting = ref(false);

// Table data
const classroomList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// Filters
const filters = reactive({
  name: '',
  building: '',
  type: '',
  status: ''
});

// Dialog state
const dialogVisible = ref(false);
const isEditMode = ref(false);
const currentClassroom = ref({});
const classroomFormRef = ref(null);

// Form rules
const formRules = reactive({
  name: [{required: true, message: '请输入教室名称', trigger: 'blur'}],
  building: [{required: true, message: '请输入教学楼名称', trigger: 'blur'}],
  type: [{required: true, message: '请选择教室类型', trigger: 'change'}],
  capacity: [
    {required: true, message: '请输入容量', trigger: 'blur'},
    {type: 'number', message: '容量必须为数字值'}
  ],
  status: [{required: true, message: '请选择状态', trigger: 'change'}]
});

// Fetch classrooms
const fetchClassrooms = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...filters
    };
    const res = await getClassroomsPage(params);
    classroomList.value = res.records || [];
    total.value = res.total || 0;
  } catch (error) {
    console.error("获取教室列表失败:", error);
  } finally {
    loading.value = false;
  }
};

// Formatters (Move to utils later)
const formatClassroomType = (type) => {
  const map = {
    REGULAR: '普通教室',
    COMPUTER_LAB: '机房',
    LABORATORY: '实验室',
    MEETING_ROOM: '会议室',
    OTHER: '其他'
  };
  return map[type] || type;
};
const formatClassroomStatus = (status) => {
  const map = {
    AVAILABLE: '可用',
    UNDER_MAINTENANCE: '维修中',
    DECOMMISSIONED: '停用'
  };
  return map[status] || status;
};
const getClassroomStatusType = (status) => {
  const map = {
    AVAILABLE: 'success',
    UNDER_MAINTENANCE: 'warning',
    DECOMMISSIONED: 'danger'
  };
  return map[status] || 'info';
};

// Pagination handlers
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1; // Reset to first page
  fetchClassrooms();
};
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchClassrooms();
};

// Dialog actions
const handleAdd = () => {
  isEditMode.value = false;
  currentClassroom.value = {status: 'AVAILABLE'}; // Default values
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  isEditMode.value = true;
  currentClassroom.value = {...row};
  dialogVisible.value = true;
};

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除教室 "${row.name}" 吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
      .then(async () => {
        try {
          await deleteClassroom(row.id);
          ElMessage.success('删除成功');
          fetchClassrooms(); // Refresh list
        } catch (error) {
          console.error("删除教室失败:", error);
          ElMessage.error(error?.response?.data?.message || '删除失败');
        }
      })
      .catch(() => {
      }); // User cancelled
};

const handleDialogClose = () => {
  classroomFormRef.value?.resetFields();
  currentClassroom.value = {};
};

// Form submission
const handleSubmit = async () => {
  try {
    const isValid = await classroomFormRef.value?.validate();
    if (!isValid) return;

    submitting.value = true;
    if (isEditMode.value) {
      await updateClassroom(currentClassroom.value.id, currentClassroom.value);
      ElMessage.success('更新成功');
    } else {
      await addClassroom(currentClassroom.value);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    fetchClassrooms(); // Refresh list
  } catch (error) {
    console.error("提交教室失败:", error);
    ElMessage.error(error?.response?.data?.message || (isEditMode.value ? '更新失败' : '添加失败'));
  } finally {
    submitting.value = false;
  }
};

// Initial load
onMounted(() => {
  fetchClassrooms();
});
</script>

<script>
// Non-setup script block for component name
export default {
  name: 'ClassroomManagement' // Changed name to multi-word
}
</script>

<style scoped>
.classroom-management {
  padding: 20px;
}

.page-container {
  min-height: 600px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  text-align: right;
}
</style>