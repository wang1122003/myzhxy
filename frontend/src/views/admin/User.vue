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
    <DialogWrapper
        v-model:visible="dialogVisible"
        :title="dialogTitle"
        width="600px"
        @close="resetForm"
    >
      <SmartForm
          ref="userFormRef"
          :model="userForm"
          :rules="userFormRules.value"
          :submitting="submitting"
          @cancel="handleCancel"
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
      </SmartForm>
    </DialogWrapper>
  </PageContainer>
</template>

<script setup>
import {computed, nextTick, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import {addUser, deleteUser, getUserList, resetPassword, updateUser} from '@/api/user';
import SmartForm from '@/views/ui/SmartForm.vue';
import TableView from '@/views/ui/TableView.vue';
import DialogWrapper from '@/views/ui/DialogWrapper.vue';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import FilterForm from '@/views/ui/AdvancedFilterForm.vue';

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
});

// 计算属性判断是添加还是编辑模式
const isEditMode = computed(() => !!userForm.value.id);

const userFormRules = computed(() => ({ // 表单验证规则
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
}));

// 角色选项
const userRoleOptions = [
  {value: 'ADMIN', label: '管理员'},
  {value: 'TEACHER', label: '教师'},
  {value: 'STUDENT', label: '学生'}
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
  // 先重置表单
  resetForm();
  // 设置弹窗标题
  dialogTitle.value = '添加用户';
  // 使用nextTick确保DOM更新完成后再显示弹窗
  nextTick(() => {
    dialogVisible.value = true;
  });
};

// 处理编辑用户
const handleEditUser = (row) => {
  // 先重置表单
  resetForm();
  // 设置弹窗标题
  dialogTitle.value = '编辑用户';
  // 等待DOM更新后再设置表单值并打开弹窗
  nextTick(() => {
    userForm.value = {...row};
    // 再次等待表单值更新后打开弹窗
    nextTick(() => {
      dialogVisible.value = true;
    });
  });
};

// 处理重置密码
const handleResetPassword = (row) => {
  ElMessageBox.confirm(`确定要重置 ${row.realName}(${row.username}) 的密码吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 生成随机密码 (6位数字+字母组合)
      const newPassword = Math.random().toString(36).substring(2, 8);

      // 发送重置密码请求，包含新密码
      await resetPassword(row.id, {password: newPassword});

      // 显示新密码给管理员
      ElMessageBox.alert(`密码已重置为: ${newPassword}`, '重置成功', {
        confirmButtonText: '确定',
        type: 'success',
        callback: () => {
          ElMessage.success('密码重置成功');
        }
      });
    } catch (error) {
      console.error('重置密码失败', error);
      let errorMsg = '重置密码失败';
      if (error.message) {
        errorMsg += ': ' + error.message;
      }
      ElMessage.error(errorMsg);
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

// 提交用户表单
const submitUserForm = async () => {
  submitting.value = true;
  try {
    console.log('原始表单数据:', userForm.value);
    
    if (isEditMode.value) {
      // 更新用户
      const updateData = {
        id: userForm.value.id,
        realName: userForm.value.realName?.trim(),
        userType: userForm.value.userType,
        email: userForm.value.email?.trim() || null,
        phone: userForm.value.phone?.trim() || null,
        // 如果有密码则传递，否则不传递（后端会保留原密码）
        ...(userForm.value.password ? {password: userForm.value.password} : {})
      };

      console.log('发送更新请求数据:', updateData);
      await updateUser(updateData.id, updateData);
      ElMessage.success('更新成功');
    } else {
      // 新增用户 - 按照后端要求格式化数据
      // 1. 验证表单
      await userFormRef.value.validate();

      // 2. 创建符合后端实体类的数据对象
      const userData = {
        username: userForm.value.username.trim(),
        password: userForm.value.password.trim(),
        realName: userForm.value.realName.trim(),
        userType: userForm.value.userType,
      };

      // 3. 添加可选字段（如有值）
      if (userForm.value.email?.trim()) {
        userData.email = userForm.value.email.trim();
      }

      if (userForm.value.phone?.trim()) {
        userData.phone = userForm.value.phone.trim();
      }

      console.log('发送注册请求数据:', userData);
      const response = await addUser(userData);
      console.log('注册响应:', response);
      ElMessage.success('添加成功');
    }

    dialogVisible.value = false;
    fetchUserList();
  } catch (error) {
    console.error('提交表单失败:', error);

    if (error.response) {
      console.log('错误状态码:', error.response.status);
      console.log('错误头信息:', error.response.headers);
      console.log('错误数据:', error.response.data);
    }

    // 提取错误信息
    let errorMsg = '操作失败';

    // 处理500错误中的特定信息
    if (error.response?.status === 500) {
      errorMsg = '服务器内部错误';
      if (error.response.data && typeof error.response.data === 'string') {
        // 从HTML错误响应中提取更有用的信息
        try {
          const messageMatch = error.response.data.match(/<b>消息<\/b>\s*(.*?)<\/p>/);
          if (messageMatch && messageMatch[1]) {
            // 如果包含"countByUsername"，这是一个已知的后端问题，提供更友好的错误信息
            if (messageMatch[1].includes('countByUsername')) {
              errorMsg = '用户注册失败，后端验证用户名唯一性的功能暂时不可用，请联系管理员';
            } else {
              errorMsg += ': ' + messageMatch[1].trim();
            }
          }
        } catch (e) {
          console.error('从错误响应中提取信息失败:', e);
        }
      }
    } else if (error.response?.data?.message) {
      errorMsg = error.response.data.message;
    } else if (typeof error.response?.data === 'string') {
      console.log('错误响应HTML:', error.response.data);
      // 从HTML错误页面中提取信息
      errorMsg = '服务器处理请求时发生错误，请检查输入数据格式';

      // 尝试从HTML错误页面提取更具体的错误信息
      try {
        const errorMatch = error.response.data.match(/<b>描述<\/b>(.*?)<\/p>/);
        if (errorMatch && errorMatch[1]) {
          errorMsg += ': ' + errorMatch[1].trim();
        }
      } catch (e) {
        console.error('从错误响应中提取信息失败:', e);
      }
    }

    ElMessage.error(errorMsg);
  } finally {
    submitting.value = false;
  }
};

// 重置表单
const resetForm = () => {
  // 确保先重置表单对象
  userForm.value = {
    id: null,
    username: '',
    password: '',
    realName: '',
    userType: '',
    email: '',
    phone: '',
  };

  // 然后重置验证状态
  nextTick(() => {
    if (userFormRef.value) {
      userFormRef.value.resetFields();
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

// 页面加载时获取用户列表
onMounted(() => {
  fetchUserList();
});

// 处理弹窗打开事件
const handleDialogOpen = () => {
  // 确保表单验证状态重置
  if (userFormRef.value) {
    userFormRef.value.resetFields();
  }
  console.log('弹窗已打开，表单数据:', userForm.value);
};

// 处理取消按钮点击事件
const handleCancel = () => {
  console.log('取消添加/编辑用户');
  // 直接关闭弹窗，不需要setTimeout
  dialogVisible.value = false;
};
</script>

<style scoped>
.table-action-buttons {
  display: flex;
  gap: 8px;
}
</style>