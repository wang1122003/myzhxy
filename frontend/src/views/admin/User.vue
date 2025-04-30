<template>
  <div class="user-management-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleAddUser">
        <el-icon>
          <Plus/>
        </el-icon>
        添加用户
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchParams" @submit.prevent="handleSearch">
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="user-list-card">
      <BaseTable v-loading="loading" :table-data="userList" style="width: 100%">
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
            <el-button size="small" type="primary" @click="handleEditUser(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button :disabled="scope.row.username === 'admin'" size="small" type="danger"
                       @click="handleDeleteUser(scope.row)">删除
            </el-button> <!-- 禁止删除 admin -->
          </template>
        </el-table-column>
      </BaseTable>

      <!-- 分页 -->
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
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :close-on-click-modal="false" :title="dialogTitle" width="600px"
               @close="resetForm">
      <el-form ref="userFormRef" :model="userForm" :rules="userFormRules" label-width="100px">
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
          <el-select v-model="userForm.userType" placeholder="请选择角色">
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
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUserForm">确定</el-button>
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
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElSwitch,
  ElTableColumn,
  ElTag
} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import {addUser, deleteUser, getUserList, resetPassword, updateUser, updateUserStatus} from '@/api/user'; // 引入 API

const loading = ref(false);
const userList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  keyword: '',
  role: '',
  status: null
});

const dialogVisible = ref(false);
const dialogTitle = ref('添加用户');
const userFormRef = ref(null); // 表单引用
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
    {type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change']}
  ],
  phone: [
    {required: true, message: '请输入手机号码', trigger: 'blur'},
    // 可选：添加手机号格式验证
    {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: ['blur', 'change']}
  ]
});

// 用户状态枚举
const userStatusOptions = [
  {label: '正常', value: 'ACTIVE'},
  {label: '禁用', value: 'INACTIVE'}
];

// 用户角色枚举
const userRoleOptions = [
  {label: '管理员', value: 'ADMIN'},
  {label: '教师', value: 'TEACHER'},
  {label: '学生', value: 'STUDENT'}
];

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || undefined,
      userType: searchParams.role ? searchParams.role.toUpperCase() : undefined,
      status: searchParams.status || undefined
    };
    const res = await getUserList(params);
    userList.value = res.records || [];
    total.value = res.total || 0;
  } catch (error) {
    console.error("获取用户列表失败", error);
    ElMessage.error("获取用户列表失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1; // 搜索时回到第一页
  fetchUsers();
};

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchUsers();
};

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchUsers();
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

// 添加用户（打开对话框）
const handleAddUser = () => {
  dialogTitle.value = '添加用户';
  resetForm(); // Use resetForm to initialize
  dialogVisible.value = true;
};

// 编辑用户（打开对话框）
const handleEditUser = (row) => {
  dialogTitle.value = '编辑用户';
  // Backend sends uppercase enums, form select expects 'Admin', 'Teacher', 'Student'
  // Backend status is 'ACTIVE'/'INACTIVE', form switch expects the same.
  const userTypeForForm = row.userType === 'ADMIN' ? 'Admin'
      : row.userType === 'TEACHER' ? 'Teacher'
          : row.userType === 'STUDENT' ? 'Student'
              : ''; // Handle unknown types
  userForm.value = {
    ...row,
    userType: userTypeForForm, // Map to form value
    status: row.status, // Directly use backend value ('ACTIVE'/'INACTIVE')
    password: '' // Don't populate password for edit
  };
  dialogVisible.value = true;
};

// 删除用户
const handleDeleteUser = (row) => {
  if (row.username === 'admin') {
    ElMessage.warning('不能删除超级管理员账号');
    return;
  }
  ElMessageBox.confirm(`确定要删除用户 ${row.realName} (${row.username}) 吗? 此操作不可恢复。`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id);
      ElMessage.success('用户删除成功');
      await fetchUsers(); // 刷新列表
    } catch (error) {
      console.error("删除用户失败", error);
      // ElMessage.error("删除用户失败"); // 错误消息由拦截器处理
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 重置密码
const handleResetPassword = (row) => {
  ElMessageBox.confirm(`确定要重置用户 ${row.realName} (${row.username}) 的密码吗? 新密码将是默认密码（例如 '123456'）。`, '提示', {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning',
      },
  ).then(async () => {
    try {
      // 调用重置密码 API，传入用户 ID
      await resetPassword(row.id);
      ElMessage.success(`用户 ${row.username} 的密码已重置`);
      // 通常不需要刷新列表，因为密码是后台操作
    } catch (error) {
      console.error(`重置用户 ${row.username} 密码失败`, error);
      // ElMessage.error(`重置密码失败`); // 错误消息由拦截器处理
    }
  }).catch(() => {
    ElMessage.info('已取消重置密码');
  });
};

// 更改用户状态
const handleStatusChange = async (row) => {
  // v-model now directly binds to 'ACTIVE' or 'INACTIVE' from the backend/list
  const currentStatusValue = row.status;
  const optimisticNewStatus = currentStatusValue === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  const statusText = optimisticNewStatus === 'ACTIVE' ? '启用' : '禁用';

  // Optimistically update the UI first for better UX
  row.status = optimisticNewStatus;

  try {
    // API expects 'ACTIVE' or 'INACTIVE'
    await updateUserStatus(row.id, optimisticNewStatus);
    ElMessage.success(`${statusText}成功`);
    // No need to refetch, status is updated locally.
  } catch (error) {
    console.error(`更新用户状态失败 (ID: ${row.id})`, error);
    ElMessage.error(`${statusText}失败，请重试`);
    // Revert switch state on error
    row.status = currentStatusValue; // Revert to original value
  }
};

// 提交用户表单 (添加/编辑)
const submitUserForm = () => {
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // Prepare data for API: Convert userType, status is already correct ('ACTIVE'/'INACTIVE')
        const dataToSend = {
          ...userForm.value,
          // Convert form's 'Admin'/'Teacher'/'Student' to backend's 'ADMIN'/'TEACHER'/'STUDENT'
          userType: userForm.value.userType ? userForm.value.userType.toUpperCase() : null,
          // userForm.status is already 'ACTIVE'/'INACTIVE' due to switch binding
          status: userForm.value.status
        };

        if (isEditMode.value) {
          // Edit mode - Call updateUser API
          // Remove fields backend doesn't update
          const {password, id, username, createTime, ...updateData} = dataToSend;
          await updateUser(id, updateData);
          ElMessage.success('用户信息更新成功');
        } else {
          // Add mode - Call addUser API (which maps to /register)
          // Ensure password is included for add
          await addUser(dataToSend);
          ElMessage.success('用户添加成功');
        }
        dialogVisible.value = false;
        await fetchUsers();
      } catch (error) {
        console.error("提交用户表单失败", error);
      }
    } else {
      console.log('表单验证失败');
      return false;
    }
  });
};

// 格式化角色显示
const formatRole = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'TEACHER': '教师',
    'STUDENT': '学生'
  };
  return roleMap[role] || role;
};

// 获取角色标签类型
const getRoleTagType = (role) => {
  const typeMap = {
    'ADMIN': 'danger',
    'TEACHER': 'warning',
    'STUDENT': 'success'
  };
  return typeMap[role] || 'info';
};

// 组件挂载后加载数据
onMounted(() => {
  fetchUsers();
});

</script>

<script>
export default {
  name: 'UserManagement'
}
</script>

<style scoped>
.user-management-container {
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

.user-list-card {
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