<template>
  <div class="classroom-management-container">
    <div class="page-header">
      <h2>教室管理</h2>
      <el-button
          type="primary"
          @click="handleAddClassroom"
      >
        <el-icon>
          <Plus/>
        </el-icon>
        添加教室
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form
          :inline="true"
          :model="searchParams"
          @submit.prevent="handleSearch"
      >
        <el-form-item label="关键词">
          <el-input
              v-model="searchParams.keyword"
              clearable
              placeholder="教室编号/教学楼"
              style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="教学楼">
          <el-input
              v-model="searchParams.building"
              clearable
              placeholder="输入教学楼"
              style="width: 150px;"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
              v-model="searchParams.status"
              clearable
              placeholder="选择状态"
              style="width: 120px;"
          >
            <el-option
                :value="1"
                label="可用"
            />
            <el-option
                :value="0"
                label="维修中"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              @click="handleSearch"
          >
            查询
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 教室列表 -->
    <el-card class="classroom-list-card">
      <el-table
          v-loading="loading"
          :data="classroomList"
          style="width: 100%"
      >
        <el-table-column
            label="教室编号"
            prop="roomNumber"
            width="150"
        />
        <el-table-column
            label="教室名称"
            min-width="180"
            prop="roomName"
        />
        <el-table-column
            label="教学楼"
            prop="building"
            width="120"
        />
        <el-table-column
            label="容量"
            prop="capacity"
            width="100"
        />
        <el-table-column
            label="状态"
            prop="status"
            width="100"
        >
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
            label="创建时间"
            prop="createTime"
            width="180"
        >
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            width="150"
        >
          <template #default="scope">
            <el-button
                size="small"
                type="primary"
                @click="handleEditClassroom(scope.row)"
            >
              编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDeleteClassroom(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div
          v-if="total > 0"
          class="pagination-container"
      >
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
    </el-card>

    <!-- 添加/编辑教室对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        @close="resetForm"
    >
      <el-form
          ref="classroomFormRef"
          :model="classroomForm"
          :rules="classroomFormRules"
          label-width="100px"
      >
        <el-form-item
            label="教室编号"
            prop="roomNumber"
        >
          <el-input
              v-model="classroomForm.roomNumber"
              :disabled="isEditMode"
              placeholder="请输入教室编号"
          />
        </el-form-item>
        <el-form-item
            label="教室名称"
            prop="roomName"
        >
          <el-input
              v-model="classroomForm.roomName"
              placeholder="请输入教室名称"
          />
        </el-form-item>
        <el-form-item
            label="容量"
            prop="capacity"
        >
          <el-input-number
              v-model="classroomForm.capacity"
              :min="0"
              placeholder="请输入教室容量"
          />
        </el-form-item>
        <el-form-item
            label="类型"
            prop="type"
        >
          <el-select
              v-model="classroomForm.type"
              placeholder="请选择教室类型"
          >
            <el-option
                label="普通教室"
                value="normal"
            />
            <el-option
                label="多媒体教室"
                value="multimedia"
            />
            <el-option
                label="实验室"
                value="lab"
            />
            <el-option
                label="会议室"
                value="meeting"
            />
            <el-option
                label="其他"
                value="other"
            />
          </el-select>
        </el-form-item>
        <el-form-item
            label="状态"
            prop="status"
        >
          <el-select
              v-model="classroomForm.status"
              placeholder="请选择教室状态"
          >
            <el-option
                :value="1"
                label="可用"
            />
            <el-option
                :value="0"
                label="维修中"
            />
          </el-select>
        </el-form-item>
        <el-form-item
            label="位置"
            prop="location"
        >
          <el-input
              v-model="classroomForm.location"
              placeholder="请输入教室位置(可选)"
          />
        </el-form-item>
        <el-form-item
            label="设备描述"
            prop="equipment"
        >
          <el-input
              v-model="classroomForm.equipment"
              :rows="3"
              placeholder="请输入设备描述(可选)"
              type="textarea"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              @click="submitClassroomForm"
          >确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {
  ElButton,
  ElCard,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag
} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import {addClassroom, deleteClassroom, getClassroomList, updateClassroom} from '@/api/classroom';

// 使用defineOptions定义组件选项 
defineOptions({
  name: 'ClassroomManagement' // 保持多词命名
});

const loading = ref(false);
const classroomList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  keyword: '',
  building: '',
  status: null
});

const dialogVisible = ref(false);
const dialogTitle = ref('添加教室');
const classroomFormRef = ref(null);
const classroomForm = ref({
  id: null,
  building: '',
  roomNumber: '',
  floor: null,
  capacity: 0,
  type: 'normal',
  equipment: '',
  status: 1,
});

const classroomFormRules = reactive({
  building: [
    {required: true, message: '请输入教学楼', trigger: 'blur'}
  ],
  roomNumber: [
    {required: true, message: '请输入教室编号', trigger: 'blur'}
  ],
  capacity: [
    {required: true, message: '请输入教室容量', trigger: 'blur'},
    {type: 'number', message: '容量必须为数字值'}
  ],
  type: [
    {required: true, message: '请选择教室类型', trigger: 'change'}
  ],
  status: [
    {required: true, message: '请选择教室状态', trigger: 'change'}
  ]
});

const isEditMode = computed(() => !!classroomForm.value.id);

const fetchClassrooms = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || null,
      building: searchParams.building || null,
      status: searchParams.status
    };
    const res = await getClassroomList(params);
    classroomList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取教室列表失败", error);
    ElMessage.error("获取教室列表失败");
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchClassrooms();
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchClassrooms();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchClassrooms();
};

const resetForm = () => {
  if (classroomFormRef.value) {
    classroomFormRef.value.resetFields();
  }
  classroomForm.value = {
    id: null,
    building: '',
    roomNumber: '',
    floor: null,
    capacity: 0,
    type: 'normal',
    equipment: '',
    status: 1,
  };
};

const handleAddClassroom = () => {
  resetForm();
  dialogTitle.value = '添加教室';
  dialogVisible.value = true;
};

const handleEditClassroom = (row) => {
  resetForm();
  dialogTitle.value = '编辑教室';
  classroomForm.value = {...row};
  dialogVisible.value = true;
};

const handleDeleteClassroom = (row) => {
  ElMessageBox.confirm(`确定要删除教室 ${row.building}-${row.roomNumber} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteClassroom(row.id);
      ElMessage.success('删除成功');
      fetchClassrooms();
    } catch (error) {
      console.error("删除教室失败", error);
      ElMessage.error("删除教室失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const submitClassroomForm = () => {
  classroomFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const dataToSend = {...classroomForm.value};
        if (isEditMode.value) {
          await updateClassroom(dataToSend.id, dataToSend);
          ElMessage.success('教室信息更新成功');
        } else {
          await addClassroom(dataToSend);
          ElMessage.success('教室添加成功');
        }
        dialogVisible.value = false;
        await fetchClassrooms();
      } catch (error) {
        console.error("提交教室表单失败", error);
        ElMessage.error(error?.response?.data?.message || '提交教室表单失败');
      }
    } else {
      console.log('教室表单验证失败');
      return false;
    }
  });
};

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    1: '可用',
    0: '维修中',
  };
  return statusMap[status] !== undefined ? statusMap[status] : '未知';
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    1: 'success',
    0: 'danger',
  };
  return typeMap[status] || 'info';
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-';
  try {
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {hour12: false});
  } catch (e) {
    return timeStr;
  }
};

// 组件挂载后加载数据
onMounted(() => {
  fetchClassrooms();
});

</script>

<style lang="less" scoped>
.classroom-management-container {
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

.classroom-list-card {
  /* 样式 */
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  text-align: right;
}
</style> 