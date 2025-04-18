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
      <el-table v-loading="loading" :data="userList" style="width: 100%">
        <el-table-column label="用户名" min-width="120" prop="username"/>
        <el-table-column label="姓名" prop="name" width="120"/>
        <el-table-column label="角色" prop="role" width="100">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">{{ formatRole(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="邮箱" min-width="180" prop="email"/>
        <el-table-column label="手机号" prop="phone" width="150"/>
        <el-table-column label="状态" prop="status" width="80">
          <template #default="scope">
            <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="最后登录时间" prop="lastLoginTime" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.lastLoginTime) }}
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
      </el-table>

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
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名"/>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin"/>
            <el-option label="教师" value="teacher"/>
            <el-option label="学生" value="student"/>
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
              :active-value="1"
              :inactive-value="0"
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
  ElTable,
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
  username: '',
  email: '',
  role: '',
  status: 1
});
const userFormRules = reactive({ // 表单验证规则
  username: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
    {min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入初始密码', trigger: 'blur'},
    {min: 6, message: '密码长度至少为 6 位', trigger: 'blur'}
  ],
  name: [{required: true, message: '请输入姓名', trigger: 'blur'}],
  role: [{required: true, message: '请选择角色', trigger: 'change'}],
  email: [
    {type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change']}
  ],
  phone: [
    // 简单校验，可根据需要加强
    {pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur'}
  ]
});

// 计算属性判断是添加还是编辑模式
const isEditMode = computed(() => !!userForm.value.id);

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
    userList.value = res.data.list || [];
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
    userFormRef.value.resetFields(); // 重置表单项为初始值
  }
  // 手动重置不在 resetFields 范围内的字段或需要特定初始值的字段
  userForm.value = {
    id: null, // 确保 id 被清空
    username: '',
    password: '', // 清空密码字段
    name: '',
    role: '',
    email: '',
    phone: '',
    status: 1 // 默认状态为 1 (正常)
  };
};

// 添加用户（打开对话框）
const handleAddUser = () => {
  resetForm(); // 先重置表单
  dialogTitle.value = '添加用户';
  dialogVisible.value = true;
};

// 编辑用户（打开对话框）
const handleEditUser = (row) => {
  resetForm(); // 先重置，防止数据污染
  dialogTitle.value = '编辑用户';
  // 使用 Object.assign 或扩展运算符复制数据，避免直接引用
  userForm.value = {...row};
  // 编辑时通常不直接修改密码，password 字段留空或在 userForm 中移除
  delete userForm.value.password; // 从表单数据中移除密码字段
  dialogVisible.value = true;
};

// 删除用户
const handleDeleteUser = (row) => {
  if (row.username === 'admin') {
    ElMessage.warning('不能删除超级管理员账号');
    return;
  }
  ElMessageBox.confirm(`确定要删除用户 ${row.name} (${row.username}) 吗? 此操作不可恢复。`, '警告', {
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
  ElMessageBox.confirm(`确定要重置用户 ${row.name} (${row.username}) 的密码吗? 新密码将是默认密码（例如 '123456'）。`, '提示', {
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
  const originalStatus = row.status === 1 ? 0 : 1; // 记录原始状态的反状态，用于失败时回滚
  const actionText = row.status === 1 ? '启用' : '禁用';
  try {
    await updateUserStatus(row.id, row.status); // 假设 API 是 updateUserStatus(userId, status)
    ElMessage.success(`用户 ${row.username} 已${actionText}`);
  } catch (error) {
    console.error(`更新用户状态失败 for ${row.username}`, error);
    ElMessage.error(`更新用户 ${row.username} 状态失败`);
    // 状态改回去
    row.status = originalStatus;
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
const formatRole = (role) => {
  const roleMap = {
    admin: '管理员',
    teacher: '教师',
    student: '学生'
  };
  return roleMap[role] || role;
};

// 获取角色标签类型
const getRoleTagType = (role) => {
  const typeMap = {
    admin: 'danger',
    teacher: 'success',
    student: 'primary'
  };
  return typeMap[role] || 'info';
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