<template>
  <PageContainer title="用户管理">
    <template #header-actions>
      <el-button type="primary" @click="handleAddUser">
        <el-icon>
          <Plus/>
        </el-icon>
        添加用户
      </el-button>
    </template>

    <!-- 搜索和筛选 -->
    <template #filters>
      <FilterForm :model="searchParams" @reset="resetSearch" @search="handleSearch">
        <el-form-item label="关键词">
          <el-input v-model="searchParams.keyword" clearable placeholder="用户名/姓名/邮箱/手机号"
                    style="width: 250px;"/>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchParams.role" clearable placeholder="选择角色" style="width: 150px;">
            <el-option
                v-for="item in userRoleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" clearable placeholder="选择状态" style="width: 120px;">
            <el-option label="正常" value="ACTIVE"/>
            <el-option label="禁用" value="INACTIVE"/>
          </el-select>
        </el-form-item>
      </FilterForm>
    </template>

    <!-- 用户列表 -->
    <TableView
        v-model="pagination"
        :data="userList"
        :loading="loading"
        :total="total"
        border
        stripe
        @page-change="handlePageChange"
    >
      <el-table-column label="用户名" min-width="120" prop="username"/>
      <el-table-column label="姓名" prop="realName" width="120"/>
      <el-table-column label="角色" prop="userType" width="100">
        <template #default="scope">
          <el-tag :type="getRoleTagType(scope.row.userType)">{{ formatRole(scope.row.userType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="邮箱" min-width="180" prop="email"/>
      <el-table-column label="手机号" prop="phone" width="150"/>
      <el-table-column label="状态" prop="status" width="100">
        <template #default="scope">
          <el-switch
              v-model="scope.row.status"
              :active-value="'ACTIVE'"
              :inactive-value="'INACTIVE'"
              @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="200">
        <template #default="scope">
          <div class="table-action-buttons">
            <el-button size="small" type="primary" @click="handleEditUser(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button :disabled="scope.row.username === 'admin'" size="small" type="danger"
                       @click="handleDeleteUser(scope.row)">删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </TableView>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :close-on-click-modal="false" :title="dialogTitle" width="600px"
               @close="resetForm">
      <FormView
          ref="userFormRef"
          :model="userForm"
          :rules="userFormRules"
          :submitting="submitting"
          @cancel="dialogVisible = false"
          @submit="submitUserForm"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEditMode" placeholder="请输入用户名"/>
        </el-form-item>
        <el-form-item v-if="!isEditMode" label="密码" prop="password">
          <el-input v-model="userForm.password" placeholder="请输入初始密码" show-password type="password"/>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名"/>
        </el-form-item>
        <el-form-item label="角色" prop="userType">
          <el-select v-model="userForm.userType" placeholder="请选择角色" style="width: 100%;">
            <el-option
                v-for="item in userRoleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱"/>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="userForm.status"
              :active-value="'ACTIVE'"
              :inactive-value="'INACTIVE'"
              active-text="正常"
              inactive-text="禁用"
          />
        </el-form-item>
      </FormView>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {
  ElMessage,
  ElMessageBox
} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import {addUser, deleteUser, getUserList, resetPassword, updateUser, updateUserStatus} from '@/api/user';

const loading = ref(false);
const submitting = ref(false);
const userList = ref([]);
const total = ref(0);
const pagination = ref({
  pageNum: 1,
  pageSize: 10
});
const searchParams = reactive({
  keyword: '',
  role: '',
  status: ''
});

const dialogVisible = ref(false);
const dialogTitle = ref('添加用户');
const userFormRef = ref(null);
const userForm = ref({
  id: null,
  username: '',
  password: '',
  realName: '',
  userType: '',
  email: '',
  phone: '',
  status: 'ACTIVE'
});

// 计算属性判断是添加还是编辑模式
const isEditMode = computed(() => !!userForm.value.id);

const userFormRules = reactive({ // 表单验证规则
  username: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
    {min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur'}
  ],
  password: [
    {required: !isEditMode.value, message: '请输入初始密码', trigger: 'blur'},
    {min: 6, message: '密码长度至少为 6 位', trigger: 'blur'}
  ],
  realName: [{required: true, message: '请输入姓名', trigger: 'blur'}],
  userType: [{required: true, message: '请选择角色', trigger: 'change'}],
  email: [
    {required: true, message: '请输入邮箱地址', trigger: 'blur'},
    {
      type: 'email',
      message: '请输入正确的邮箱地址',
      trigger: ['blur', 'change']
    }
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: ['blur', 'change']
    }
  ]
});

// 角色选项
const userRoleOptions = [
  {value: 'Admin', label: '管理员'},
  {value: 'Teacher', label: '教师'},
  {value: 'Student', label: '学生'}
];

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      keyword: searchParams.keyword || undefined,
      userType: searchParams.role || undefined,
      status: searchParams.status || undefined
    };

    const res = await getUserList(params);
    if (res && res.records) {
      userList.value = res.records;
      total.value = res.total;
    } else {
      userList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取用户列表失败', error);
    ElMessage.error('获取用户列表失败');
    userList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = () => {
  pagination.value.pageNum = 1;
  fetchUserList();
};

// 重置搜索
const resetSearch = () => {
  pagination.value.pageNum = 1;
  fetchUserList();
};

// 处理分页变化
const handlePageChange = () => {
  fetchUserList();
};

// 处理添加用户
const handleAddUser = () => {
  resetForm();
  dialogTitle.value = '添加用户';
  dialogVisible.value = true;
};

// 处理编辑用户
const handleEditUser = (row) => {
  resetForm();
  dialogTitle.value = '编辑用户';
  userForm.value = {...row};
  dialogVisible.value = true;
};

// 处理重置密码
const handleResetPassword = (row) => {
  ElMessageBox.confirm(`确定要重置 ${row.realName}(${row.username}) 的密码吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await resetPassword(row.id);
      ElMessage.success('密码重置成功');
    } catch (error) {
      console.error('重置密码失败', error);
      ElMessage.error('重置密码失败');
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 处理删除用户
const handleDeleteUser = (row) => {
  if (row.username === 'admin') {
    ElMessage.error('不能删除超级管理员账号');
    return;
  }

  ElMessageBox.confirm(`确定要删除用户 ${row.realName}(${row.username}) 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id);
      ElMessage.success('删除成功');
      fetchUserList();
    } catch (error) {
      console.error('删除用户失败', error);
      ElMessage.error('删除用户失败');
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 处理状态变更
const handleStatusChange = async (row) => {
  if (row.username === 'admin' && row.status === 'INACTIVE') {
    ElMessage.error('不能禁用超级管理员账号');
    row.status = 'ACTIVE';
    return;
  }

  try {
    await updateUserStatus(row.id, row.status);
    ElMessage.success(`状态已${row.status === 'ACTIVE' ? '启用' : '禁用'}`);
  } catch (error) {
    console.error('更新状态失败', error);
    ElMessage.error('更新状态失败');
    // 恢复状态
    row.status = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  }
};

// 提交用户表单
const submitUserForm = async () => {
  submitting.value = true;
  try {
    if (isEditMode.value) {
      await updateUser(userForm.value);
      ElMessage.success('更新成功');
    } else {
      await addUser(userForm.value);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    fetchUserList();
  } catch (error) {
    console.error('提交表单失败', error);
    ElMessage.error(error.response?.data?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
};

// 重置表单
const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields();
  }
  userForm.value = {
    id: null,
    username: '',
    password: '',
    realName: '',
    userType: '',
    email: '',
    phone: '',
    status: 'ACTIVE'
  };
};

// 格式化角色显示
const formatRole = (role) => {
  const roleMap = {
    'Admin': '管理员',
    'Teacher': '教师',
    'Student': '学生'
  };
  return roleMap[role] || role;
};

// 获取角色标签类型
const getRoleTagType = (role) => {
  const typeMap = {
    'Admin': 'danger',
    'Teacher': 'warning',
    'Student': 'success'
  };
  return typeMap[role] || 'info';
};

// 页面加载时获取用户列表
onMounted(() => {
  fetchUserList();
});
</script>

<style scoped>
.table-action-buttons {
  display: flex;
  gap: 8px;
}
</style>