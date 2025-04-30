<template>
  <div class="profile-container">
    <div class="page-header">
      <h2>个人信息</h2>
      <el-button type="primary" @click="handleEdit">
        编辑信息
      </el-button>
    </div>

    <el-card class="profile-card">
      <div class="profile-info">
        <div class="avatar-container">
          <el-avatar :size="100" :src="userAvatar">
            {{ userInfo.realName?.substring(0, 1) }}
          </el-avatar>
          <el-upload
              :action="uploadAvatarUrl"
              :before-upload="beforeAvatarUpload"
              :disabled="uploadingAvatar"
              :headers="headers"
              :on-error="handleAvatarError"
              :on-success="handleAvatarSuccess"
              :show-file-list="false"
              class="avatar-uploader"
          >
            <el-button :loading="uploadingAvatar" size="small" type="primary">
              <el-icon>
                <Upload/>
              </el-icon>
              更换头像
            </el-button>
          </el-upload>
        </div>

        <div class="info-container">
          <el-descriptions :column="2" border title="基本信息">
            <!-- 通用信息 -->
            <el-descriptions-item label="用户名/学号/工号">
              {{ userInfo.username }}
            </el-descriptions-item>
            <el-descriptions-item label="姓名">
              {{ userInfo.realName }}
            </el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ formatGender(userInfo.gender) }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ userInfo.email }}
            </el-descriptions-item>
            <el-descriptions-item label="手机">
              {{ userInfo.phone }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              {{ formatRole(userInfo.role) }}
            </el-descriptions-item>
            <!-- 学生特定信息 -->
            <template v-if="userRole === 'student'">
              <el-descriptions-item label="院系">
                {{ userInfo.department }}
              </el-descriptions-item>
              <el-descriptions-item label="专业">
                {{ userInfo.major }}
              </el-descriptions-item>
              <el-descriptions-item label="班级">
                {{ userInfo.className }}
              </el-descriptions-item>
            </template>
            <!-- 教师特定信息 (如果需要) -->
            <template v-if="userRole === 'teacher'">
              <el-descriptions-item label="院系">
                {{ userInfo.department }}
              </el-descriptions-item>
              <el-descriptions-item label="职称">
                {{ userInfo.title || '暂无' }}
              </el-descriptions-item>
            </template>
            <!-- 管理员特定信息 (如果需要) -->
            <template v-if="userRole === 'admin'">
              <!-- 可添加管理员特定字段 -->
            </template>
          </el-descriptions>
        </div>
      </div>
    </el-card>

    <!-- 编辑个人信息对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑个人信息" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="true"/>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" :disabled="true"/>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 根据角色动态显示/禁用字段 -->
        <template v-if="userRole === 'student' || userRole === 'teacher'">
          <el-form-item label="院系" prop="department">
            <el-input v-model="form.department" :disabled="true"/>
          </el-form-item>
        </template>
        <template v-if="userRole === 'student'">
          <el-form-item label="专业" prop="major">
            <el-input v-model="form.major" :disabled="true"/>
          </el-form-item>
          <el-form-item label="班级" prop="className">
            <el-input v-model="form.className" :disabled="true"/>
          </el-form-item>
        </template>
        <template v-if="userRole === 'teacher'">
          <el-form-item label="职称" prop="title">
            <el-input v-model="form.title"/> <!-- 假设职称可编辑 -->
          </el-form-item>
        </template>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email"/>
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" show-password type="password"/>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" show-password type="password"/>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" show-password type="password"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Upload} from '@element-plus/icons-vue';
import {changePassword as changePasswordApi, getCurrentUser, updateUserProfile} from '@/api/user'; // 引入修改密码API
import {useUserStore} from '@/stores/userStore'; // Correct import for userStore
import {useRouter} from 'vue-router';

const router = useRouter();
const dialogVisible = ref(false);
const passwordDialogVisible = ref(false); // 控制修改密码对话框
const formRef = ref(null);
const passwordFormRef = ref(null); // 密码表单引用
const uploadingAvatar = ref(false);
const loading = ref(false);

// 使用 reactive 创建表单数据
const form = reactive({
  username: '',
  realName: '',
  gender: null,
  department: '',
  major: '',
  className: '',
  title: '', // 添加教师职称
  email: '',
  phone: '',
});

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

// Get user store instance
const userStore = useUserStore();
// Access state/getters via userStore
const token = computed(() => userStore.token);
const userInfo = computed(() => userStore.userInfo);
const userRole = computed(() => userStore.userRole());
const userAvatar = computed(() => userStore.userAvatar());

// 将 userInfo 的值同步到 form
const syncFormWithUserInfo = () => {
  if (userInfo.value) {
    form.username = userInfo.value.username || '';
    form.realName = userInfo.value.realName || '';
    form.gender = userInfo.value.gender;
    form.department = userInfo.value.department || '';
    form.major = userInfo.value.major || '';
    form.className = userInfo.value.className || '';
    form.title = userInfo.value.title || ''; // 同步职称
    form.email = userInfo.value.email || '';
    form.phone = userInfo.value.phone || '';
  }
};

const uploadAvatarUrl = computed(() => {
  // 根据实际 API 调整，可能需要 userId
  return `/api/file/upload/avatar/${userInfo.value?.id}`; // 假设有 ID
});

const headers = computed(() => ({
  Authorization: token.value // Use the computed token here
}));

const rules = {
  email: [
    {required: true, message: '请输入邮箱', trigger: 'blur'},
    {type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change']}
  ],
  phone: [
    {required: true, message: '请输入手机号', trigger: 'blur'},
    {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: ['blur', 'change']}
  ]
};

const passwordRules = {
  oldPassword: [{required: true, message: '请输入旧密码', trigger: 'blur'}],
  newPassword: [
    {required: true, message: '请输入新密码', trigger: 'blur'},
    {min: 6, message: '密码长度不能少于6位', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请确认新密码', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致!'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

const fetchProfile = async () => {
  loading.value = true;
  try {
    const response = await getCurrentUser();
    if (response.code === 200 && response.data) {
      userStore.setUserInfo(response.data); // 使用Pinia store更新用户信息
      // 更新全局头像
      if (response.data.avatar) {
        userStore.setAvatar(response.data.avatar);
      }
      syncFormWithUserInfo(); // 将获取到的信息同步到表单
    } else {
      ElMessage.error(response.message || '获取用户信息失败');
      if (response.code === 401) {
        await handleLogout();
      }
    }
  } catch (error) {
    console.error('获取用户信息异常:', error);
    ElMessage.error('获取用户信息异常');
    if (error?.response?.status === 401) {
      await handleLogout();
    }
  } finally {
    loading.value = false;
  }
};

onMounted(fetchProfile);

const handleEdit = () => {
  syncFormWithUserInfo(); // 打开编辑框时，确保表单数据是当前最新的
  dialogVisible.value = true;
};

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      // 只提交允许修改的字段
      const dataToUpdate = {
        gender: form.gender,
        email: form.email,
        phone: form.phone,
        // 如果教师职称可编辑，则包含 title
        ...(userRole.value === 'teacher' && {title: form.title})
      };

      loading.value = true;
      try {
        const response = await updateUserProfile(dataToUpdate);
        if (response.code === 0 || response.code === 200) {
          ElMessage.success('更新成功');
          dialogVisible.value = false;
          // 更新成功后重新获取用户信息，以确保显示的是最新数据
          await fetchProfile();
        } else {
          ElMessage.error(response.message || '更新失败');
        }
      } catch (error) {
        console.error("更新用户信息失败:", error);
        ElMessage.error("更新用户信息失败，请稍后再试");
      } finally {
        loading.value = false;
      }
    }
  });
};

const beforeAvatarUpload = (rawFile) => {
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.error('请上传图片格式文件!');
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 5) { // 限制 5MB
    ElMessage.error('图片大小不能超过 5MB!');
    return false;
  }
  uploadingAvatar.value = true;
  return true;
};

const handleAvatarSuccess = (response, uploadFile) => {
  uploadingAvatar.value = false;
  if (response.code === 0 || response.code === 200) {
    ElMessage.success('头像上传成功');
    const newAvatarUrl = response.data.url; // 假设返回的数据结构包含 url
    userStore.setAvatar(newAvatarUrl); // 更新全局头像
    // 可以选择性地更新 userInfo 中的 avatar 字段，如果后端在 updateUserProfile 时不返回完整信息
    if (userInfo.value) {
      userInfo.value.avatar = newAvatarUrl;
    }
  } else {
    ElMessage.error(response.message || '头像上传失败');
  }
};

const handleAvatarError = (error) => {
  uploadingAvatar.value = false;
  console.error("头像上传失败:", error);
  try {
    const errorData = JSON.parse(error.message || '{}');
    ElMessage.error(errorData.message || '头像上传失败，请检查网络或联系管理员');
  } catch (e) {
    ElMessage.error('头像上传失败，请检查网络或联系管理员');
  }
};

const handleLogout = async () => {
  await userStore.logout();
  router.push('/');
  ElMessage.info('会话已过期，请重新登录');
};

const formatGender = (gender) => {
  if (gender === 1) return '男';
  if (gender === 0) return '女';
  return '未知';
};

const formatRole = (role) => {
  if (role === 'student') return '学生';
  if (role === 'teacher') return '教师';
  if (role === 'admin') return '管理员';
  return '未知角色';
};

// --- 修改密码逻辑 ---
const openChangePasswordDialog = () => {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields(); // 清除校验状态
  }
  passwordDialogVisible.value = true;
};

const handlePasswordSubmit = () => {
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const response = await changePasswordApi({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        });
        if (response.code === 0 || response.code === 200) {
          ElMessage.success('密码修改成功，请重新登录');
          passwordDialogVisible.value = false;
          await handleLogout(); // 修改成功后强制登出
        } else {
          ElMessage.error(response.message || '密码修改失败');
        }
      } catch (error) {
        console.error("修改密码失败:", error);
        ElMessage.error(error.message || "修改密码失败，请稍后再试");
      } finally {
        loading.value = false;
      }
    }
  });
};

</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.profile-info {
  display: flex;
  align-items: flex-start; /* 让头像和信息上下对齐 */
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 40px; /* 增加头像和信息的间距 */
  flex-shrink: 0; /* 防止头像容器被压缩 */
}

.avatar-uploader {
  margin-top: 10px;
}

.info-container {
  flex-grow: 1; /* 让信息容器占据剩余空间 */
}

/* 可以添加更多样式 */
</style> 