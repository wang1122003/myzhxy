<template>
  <div class="profile-container">
    <div class="page-header">
      <h2>个人信息</h2>
      <el-button
          type="primary"
          @click="handleEdit"
      >
        编辑信息
      </el-button>
    </div>

    <el-card class="profile-card">
      <div class="profile-info">
        <div class="avatar-container">
          <el-avatar
              :size="100"
              :src="userAvatar"
          >
            {{ userInfo.name?.substring(0, 1) }}
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
            <el-button
                :loading="uploadingAvatar"
                size="small"
                type="primary"
            >
              <el-icon>
                <Upload/>
              </el-icon>
              更换头像
            </el-button>
          </el-upload>
        </div>

        <div class="info-container">
          <el-descriptions
              :column="2"
              border
              title="基本信息"
          >
            <el-descriptions-item label="用户名">
              {{ userInfo.username }}
            </el-descriptions-item>
            <el-descriptions-item label="姓名">
              {{ userInfo.name }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              管理员
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ userInfo.email }}
            </el-descriptions-item>
            <el-descriptions-item label="手机">
              {{ userInfo.phone }}
            </el-descriptions-item>
            <!-- 可以根据需要添加其他管理员特有字段 -->
          </el-descriptions>
        </div>
      </div>
    </el-card>

    <!-- 编辑个人信息对话框 -->
    <el-dialog
        v-model="dialogVisible"
        title="编辑个人信息"
        width="500px"
    >
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item
            label="姓名"
            prop="name"
        >
          <el-input v-model="form.name"/>
        </el-form-item>
        <el-form-item
            label="邮箱"
            prop="email"
        >
          <el-input v-model="form.email"/>
        </el-form-item>
        <el-form-item
            label="手机"
            prop="phone"
        >
          <el-input v-model="form.phone"/>
        </el-form-item>
        <!-- 管理员可编辑字段较少 -->
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              @click="handleSubmit"
          >保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {onMounted, reactive, ref} from 'vue';
import {getUserProfile, updateUserProfile} from '@/api/user'; // 假设API路径
import {ElMessage} from 'element-plus';
// 从 auth.js 导入需要的状态和方法
import {logout, setUserInfo, userId, userInfo} from '@/utils/auth'; // 导入 logout 函数
import {useRouter} from 'vue-router';
import {Upload} from '@element-plus/icons-vue';

// 不再需要 useUserStore
// import { useUserStore } from '@/store/user'; // 导入 user store

const router = useRouter();
// const userStore = useUserStore(); // 不再需要 store 实例

const profile = ref({}); // 本地状态用于表单绑定
const loading = ref(false);
const isEditing = ref(false);

// 头像上传相关
const uploadUrl = ref('/api/upload/avatar'); // 替换为你的上传 API 地址
const headers = ref({
  Authorization: localStorage.getItem('Authorization-Token') || '' // 从 localStorage 获取 token
});

const fetchProfile = async () => {
  loading.value = true;
  try {
    // 使用响应式的 userId ref
    if (!userId.value) {
      ElMessage.error('无法获取用户ID，请重新登录');
      await handleLogout();
      return;
    }
    const response = await getUserProfile(userId.value);
    if (response.code === 200 && response.data) {
      profile.value = {...response.data};
    } else {
      ElMessage.error(response.message || '获取用户信息失败');
    }
  } catch (error) {
    console.error('获取用户信息异常:', error);
    ElMessage.error('获取用户信息异常');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchProfile);

const handleEdit = () => {
  isEditing.value = true;
  // 使用 auth.js 中的 userInfo 初始化编辑表单
  if (userInfo.value) {
    profile.value = {...userInfo.value};
  }
};

const handleSave = async () => {
  loading.value = true;
  try {
    // 确保 profile.value 包含 id
    if (!profile.value.id) {
      profile.value.id = userId.value;
    }
    const response = await updateUserProfile(profile.value);
    if (response.code === 200) {
      ElMessage.success('个人信息更新成功');
      setUserInfo(response.data); // 更新 auth.js 中的用户信息
      isEditing.value = false;
      // fetchProfile(); // 可选：重新获取
    } else {
      ElMessage.error(response.message || '更新失败');
    }
  } catch (error) {
    console.error('更新用户信息异常:', error);
    ElMessage.error('更新用户信息异常');
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
  isEditing.value = false;
  // 从 auth.js 中的 userInfo 恢复数据
  if (userInfo.value) {
    profile.value = {...userInfo.value};
  }
  // fetchProfile(); // 或者重新获取
};

// 头像上传成功处理
const handleAvatarSuccess = (response, uploadFile) => {
  if (response.code === 200 && response.data) {
    profile.value.avatarUrl = response.data.url; // 更新本地表单
    // 同时更新 auth.js 中的用户信息
    const currentUserInfo = {...userInfo.value, avatarUrl: response.data.url};
    setUserInfo(currentUserInfo);
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error(response.message || '头像上传失败');
  }
};

// 上传前校验
const beforeAvatarUpload = (rawFile) => {
  const isJPG = rawFile.type === 'image/jpeg';
  const isPNG = rawFile.type === 'image/png';
  const isLt2M = rawFile.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    ElMessage.error('头像图片只能是 JPG 或 PNG 格式!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

// 统一的登出处理
const handleLogout = async () => {
  try {
    await logout(); // 调用 auth.js 中的 logout
    router.push('/login');
  } catch (error) {
    console.error("登出时出错:", error);
    logout(); // 确保前端状态被清理
    router.push('/login');
  }
};

// 辅助函数：将性别数字转为文字
const formatGender = (gender) => {
  if (gender === 1) return '男';
  if (gender === 0) return '女';
  return '未知';
};

export default {
  name: 'AdminProfile',
  components: {
    Upload,
    ElMessage
  },
  setup() {
    const dialogVisible = ref(false);
    const formRef = ref(null);
    const uploadingAvatar = ref(false);

    const form = reactive({
      id: '',
      name: '',
      email: '',
      phone: '',
      avatar: ''
    });

    const rules = {
      name: [
        {required: true, message: '请输入姓名', trigger: 'blur'}
      ],
      email: [
        {required: true, message: '请输入邮箱', trigger: 'blur'},
        {type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change']}
      ],
      phone: [
        {required: true, message: '请输入手机号', trigger: 'blur'},
        {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: ['blur', 'change']}
      ]
    };

    const handleSubmit = () => {
      formRef.value.validate((valid) => {
        if (valid) {
          const dataToUpdate = {...form};
          handleSave().then(() => {
            dialogVisible.value = false;
          });
        }
      });
    };

    const handleAvatarError = (error) => {
      uploadingAvatar.value = false;
      let message = '头像上传失败';
      try {
        const responseData = JSON.parse(error.message || '{}');
        message = responseData.message || message;
      } catch (e) {
      }
      ElMessage.error(message);
      console.error('头像上传错误:', error);
    };

    return {
      dialogVisible,
      formRef,
      form,
      rules,
      handleEdit,
      handleSubmit,
      handleAvatarError,
      uploadingAvatar
    };
  }
};
</script>

<style scoped>
/* 样式与 teacher/Profile.vue 相同 */
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
  align-items: flex-start;
}

.avatar-container {
  margin-right: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader {
  margin-top: 10px;
}

.info-container {
  flex: 1;
}

.dialog-footer {
  text-align: right;
}
</style> 