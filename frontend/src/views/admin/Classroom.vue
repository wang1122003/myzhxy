<template>
  <PageContainer title="教室管理">
    <template #actions>
      <el-button :icon="Plus" type="primary" @click="handleAdd">添加教室</el-button>
    </template>

    <!-- 筛选区域 -->
    <FilterForm
        :items="filterItems"
        :model="filters"
        @reset="handleReset"
        @search="handleSearch"
    />

    <!-- 教室列表 -->
    <TableView
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :action-column-config="actionColumnConfig"
        :columns="tableColumns"
        :data="classroomList"
        :loading="loading"
        :total="total"
        @delete="handleDelete"
        @edit="handleEdit"
        @refresh="fetchClassrooms"
    />

    <!-- 添加/编辑 对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="isEditMode ? '编辑教室' : '添加教室'"
        width="600px"
        @close="handleDialogClose"
        destroy-on-close
    >
      <el-form ref="classroomFormRef" :model="currentClassroom" :rules="formRules" label-width="100px">
        <el-form-item label="教室名称" prop="name">
          <el-input v-model="currentClassroom.name" placeholder="例如: 逸夫楼 101"/>
        </el-form-item>
        <el-form-item label="教学楼" prop="building">
          <el-input v-model="currentClassroom.building" placeholder="例如: 逸夫楼"/>
        </el-form-item>
        <el-form-item label="教室编号" prop="roomNumber">
          <el-input v-model="currentClassroom.roomNumber" placeholder="例如: 101"/>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="currentClassroom.type" placeholder="请选择教室类型" style="width: 100%;">
            <el-option v-for="item in CLASSROOM_TYPE_OPTIONS.filter(o => o.value)" :key="item.value" :label="item.label"
                       :value="item.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="currentClassroom.capacity" :min="1" placeholder="请输入容量" style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="设施" prop="facilities">
          <el-input
              v-model="currentClassroom.facilities"
              :rows="3"
              placeholder="请输入教室设施，用逗号分隔，如：投影仪, 空调, Wi-Fi"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="currentClassroom.status">
            <el-radio v-for="item in CLASSROOM_STATUS_OPTIONS.filter(o => o.value)" :key="item.value"
                      :label="item.value">{{ item.label }}
            </el-radio>
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
  </PageContainer>
</template>

<script setup>
import {onMounted, reactive, ref, computed, watch, h, resolveComponent} from 'vue';
import {ElMessage, ElMessageBox, ElTag} from 'element-plus';
import {Delete, Edit, Plus, Search} from '@element-plus/icons-vue';
import {getClassroomsPage, addClassroom, updateClassroom, deleteClassroom} from '@/api/classroom'; // Corrected: Use classroom.js

// --- Constants & Options ---
const CLASSROOM_TYPE_OPTIONS = [
  {value: '', label: '全部'},
  {value: 'REGULAR', label: '普通教室'},
  {value: 'COMPUTER_LAB', label: '机房'},
  {value: 'LABORATORY', label: '实验室'},
  {value: 'MEETING_ROOM', label: '会议室'},
  {value: 'OTHER', label: '其他'}
];

const CLASSROOM_STATUS_OPTIONS = [
  {value: '', label: '全部'},
  {value: 'AVAILABLE', label: '可用', type: 'success'},
  {value: 'UNDER_MAINTENANCE', label: '维修中', type: 'warning'},
  {value: 'DECOMMISSIONED', label: '停用', type: 'danger'}
];

// --- Reactive State ---
const loading = ref(false);
const submitting = ref(false);
const classroomList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const filters = reactive({
  name: '', // Combined name/roomNumber search?
  building: '',
  type: '',
  status: ''
});
const dialogVisible = ref(false);
const isEditMode = ref(false);
const currentClassroom = reactive({
  id: null,
  name: '',
  building: '',
  roomNumber: '',
  type: 'REGULAR', // Default type
  capacity: 30, // Default capacity
  facilities: '',
  status: 'AVAILABLE' // Default status
});
const classroomFormRef = ref(null);

// --- Computed Properties ---

// FilterForm Configuration
const filterItems = computed(() => [
  {
    type: 'input',
    label: '教室名称/编号',
    prop: 'name', // Backend might need a combined search field 'keyword'
    placeholder: '名称或编号',
    props: {clearable: true}
  },
  {
    type: 'input',
    label: '教学楼',
    prop: 'building',
    placeholder: '输入教学楼名称',
    props: {clearable: true}
  },
  {
    type: 'select',
    label: '类型',
    prop: 'type',
    placeholder: '选择教室类型',
    options: CLASSROOM_TYPE_OPTIONS,
    props: {clearable: true}
  },
  {
    type: 'select',
    label: '状态',
    prop: 'status',
    placeholder: '选择状态',
    options: CLASSROOM_STATUS_OPTIONS,
    props: {clearable: true}
  }
]);

// TableView Columns Configuration
const tableColumns = computed(() => [
  {prop: 'name', label: '教室名称', minWidth: 150},
  {prop: 'building', label: '教学楼', width: 120},
  {prop: 'roomNumber', label: '编号', width: 80},
  {
    prop: 'type',
    label: '类型',
    width: 120,
    formatter: (row) => formatClassroomType(row.type)
  },
  {prop: 'capacity', label: '容量', width: 80},
  {prop: 'facilities', label: '设施', minWidth: 180, showOverflowTooltip: true},
  {
    prop: 'status',
    label: '状态',
    width: 100,
    slots: {
      default: (scope) =>
          h(resolveComponent('ElTag'), {
            type: getClassroomStatusType(scope.row.status)
          }, () => formatClassroomStatus(scope.row.status))
    }
  }
]);

// TableView Action Column Configuration
const actionColumnConfig = computed(() => ({
  width: 150,
  fixed: 'right',
  buttons: [
    {label: '编辑', type: 'primary', size: 'small', event: 'edit', icon: Edit},
    {label: '删除', type: 'danger', size: 'small', event: 'delete', icon: Delete}
  ]
}));

// --- Helper Functions ---
const formatClassroomType = (type) => {
  const found = CLASSROOM_TYPE_OPTIONS.find(opt => opt.value === type);
  return found ? found.label : type;
};

const formatClassroomStatus = (status) => {
  const found = CLASSROOM_STATUS_OPTIONS.find(opt => opt.value === status);
  return found ? found.label : status;
};

const getClassroomStatusType = (status) => {
  const found = CLASSROOM_STATUS_OPTIONS.find(opt => opt.value === status);
  return found ? found.type : ''; // Default empty type
};

// --- Methods ---

// Fetch classrooms
const fetchClassrooms = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: filters.name, // Assuming backend handles 'name' as keyword for name/roomNumber
      building: filters.building,
      type: filters.type,
      status: filters.status
    };
    // Filter out null/empty params before sending
    const validParams = Object.entries(params).reduce((acc, [key, value]) => {
      if (value !== null && value !== '') {
        acc[key] = value;
      }
      return acc;
    }, {});

    const res = await getClassroomsPage(validParams);
    classroomList.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch (error) {
    console.error("获取教室列表失败:", error);
    // Error handled by interceptor
  } finally {
    loading.value = false;
  }
};

// Handle FilterForm search
const handleSearch = () => {
  currentPage.value = 1;
  fetchClassrooms();
};

// Handle FilterForm reset
const handleReset = () => {
  filters.name = '';
  filters.building = '';
  filters.type = '';
  filters.status = '';
  currentPage.value = 1;
  fetchClassrooms();
};

// Reset dialog form
const resetForm = () => {
  Object.assign(currentClassroom, {
    id: null,
    name: '',
    building: '',
    roomNumber: '',
    type: 'REGULAR',
    capacity: 30,
    facilities: '',
    status: 'AVAILABLE'
  });
  if (classroomFormRef.value) {
    classroomFormRef.value.resetFields();
  }
};

// Handle Add button click
const handleAdd = () => {
  resetForm();
  isEditMode.value = false;
  dialogVisible.value = true;
};

// Handle Edit button click (from TableView event)
const handleEdit = (row) => {
  resetForm(); // Reset first
  // Assign row data to reactive form object
  Object.assign(currentClassroom, row);
  isEditMode.value = true;
  dialogVisible.value = true;
};

// Handle Dialog Close
const handleDialogClose = () => {
  resetForm();
  dialogVisible.value = false;
};

// Handle Submit (Create/Update)
const handleSubmit = async () => {
  if (!classroomFormRef.value) return;
  await classroomFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const dataToSend = {...currentClassroom};
        if (isEditMode.value) {
          await updateClassroom(dataToSend.id, dataToSend);
          ElMessage.success('更新成功');
        } else {
          await addClassroom(dataToSend);
          ElMessage.success('添加成功');
        }
        dialogVisible.value = false;
        fetchClassrooms(); // Refresh list
      } catch (error) {
        console.error("提交教室信息失败:", error);
        // Error handled by interceptor
      } finally {
        submitting.value = false;
      }
    } else {
      ElMessage.warning('请检查表单必填项');
      return false;
    }
  });
};

// Handle Delete (from TableView event)
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除教室【${row.name} (${row.building} ${row.roomNumber})】吗？`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteClassroom(row.id);
      ElMessage.success('删除成功');
      fetchClassrooms(); // Refresh list
    } catch (error) {
      console.error("删除教室失败:", error);
      // Error handled by interceptor
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// Form validation rules
const formRules = reactive({
  name: [{required: true, message: '请输入教室名称', trigger: 'blur'}],
  building: [{required: true, message: '请输入教学楼名称', trigger: 'blur'}],
  roomNumber: [{required: true, message: '请输入教室编号', trigger: 'blur'}],
  type: [{required: true, message: '请选择教室类型', trigger: 'change'}],
  capacity: [
    {required: true, message: '请输入容量', trigger: 'blur'},
    {type: 'number', message: '容量必须为数字值', trigger: 'blur'}
  ],
  status: [{required: true, message: '请选择状态', trigger: 'change'}]
});

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchClassrooms();
});

// Watch for page changes (handled by TableView v-model)
watch([currentPage, pageSize], fetchClassrooms, {immediate: false});

</script>

<style scoped>
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.dialog-footer {
  text-align: right;
}
</style>