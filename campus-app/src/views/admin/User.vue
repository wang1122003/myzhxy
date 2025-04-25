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
            <el-option label="管理员" value="admin"/>
            <el-option label="教师" value="teacher"/>
            <el-option label="学生" value="student"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" clearable placeholder="选择状态" style="width: 120px;">
            <el-option :value="1" label="正常"/>
            <el-option :value="0" label="禁用"/>
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
                active-value="Active"
                inactive-value="Inactive"
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
            <el-option label="管理员" value="Admin"/>
            <el-option label="教师" value="Teacher"/>
            <el-option label="学生" value="Student"/>
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
              active-value="Active"
              inactive-value="Inactive"
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
  status: 'Active'
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
  email: [formRules.required, formRules.email],
  phone: [formRules.required, formRules.phone]
});

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || null,
      role: searchParams.role || null,
      status: searchParams.status
    };
    const res = await getUserList(params);
    userList.value = res.data.rows || [];
    total.value = res.data.total || 0;
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
    status: 'Active'
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
  // Shallow copy might be enough, or deep copy if needed
  userForm.value = {...row, password: ''}; // Don't populate password for edit
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
  const newStatus = row.status;
  const statusText = newStatus === 'Active' ? '启用' : '禁用';
  try {
    await updateUserStatus(row.id, newStatus);
    ElMessage.success(`${statusText}成功`);
    // No need to refetch, v-model already updated the local state
  } catch (error) {
    console.error(`更新用户状态失败 (ID: ${row.id})`, error);
    ElMessage.error(`${statusText}失败`);
    // Revert switch state on error
    row.status = newStatus === 'Active' ? 'Inactive' : 'Active';
  }
};

// 提交用户表单 (添加/编辑)
const submitUserForm = () => {
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEditMode.value) {
          // 编辑模式 - 调用 updateUser API
          // 确保只发送需要的字段，特别是移除 password
          const {password, id, ...updateData} = userForm.value;
          await updateUser(id, updateData);
          ElMessage.success('用户信息更新成功');
        } else {
          // 添加模式 - 调用 addUser API
          await addUser(userForm.value);
          ElMessage.success('用户添加成功');
        }
        dialogVisible.value = false; // 关闭对话框
        await fetchUsers(); // 刷新用户列表
      } catch (error) {
        console.error("提交用户表单失败", error);
        // 错误消息由 API 请求拦截器处理，这里可以不再重复提示
        // ElMessage.error(isEditMode.value ? '更新用户失败' : '添加用户失败');
      }
    } else {
      console.log('表单验证失败');
      return false;
    }
  });
};

// 格式化角色显示
const formatRole = (userType) => {
  if (userType === 'Admin') return '管理员';
  if (userType === 'Teacher') return '教师';
  if (userType === 'Student') return '学生';
  return userType || '未知';
};

// 获取角色标签类型
const getRoleTagType = (userType) => {
  if (userType === 'Admin') return 'danger';
  if (userType === 'Teacher') return 'success';
  if (userType === 'Student') return 'info';
  return 'info';
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