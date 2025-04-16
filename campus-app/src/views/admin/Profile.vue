<template>
  <div class="profile-container">
    <div class="page-header">
      <h2>个人信息</h2>
      <el-button type="primary" @click="handleEdit">编辑信息</el-button>
    </div>

    <el-card class="profile-card">
      <div class="profile-info">
        <div class="avatar-container">
          <el-avatar :size="100" :src="userAvatar">{{ userInfo.name?.substring(0, 1) }}</el-avatar>
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
            <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ userInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="角色">管理员</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
            <el-descriptions-item label="手机">{{ userInfo.phone }}</el-descriptions-item>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name"/>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email"/>
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone"/>
        </el-form-item>
        <!-- 管理员可编辑字段较少 -->
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {computed, onMounted, reactive, ref} from 'vue';
import {
  ElAvatar,
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElMessage,
  ElUpload
} from 'element-plus'; // 移除 Radio 相关
import {Upload} from '@element-plus/icons-vue';
import {getAdminProfile, updateAdminProfile} from '@/api/user'; // 使用管理员 API

export default {
  name: 'AdminProfile', // 修改组件名
  components: {
    Upload,
    ElDescriptions,
    ElDescriptionsItem,
    ElCard,
    ElAvatar,
    ElUpload,
    ElButton,
    ElIcon,
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput
  },
  setup() {
    const userInfo = ref({});
    const userAvatar = ref('');
    const dialogVisible = ref(false);
    const formRef = ref(null);
    const uploadingAvatar = ref(false);

    const uploadAvatarUrl = `/api/user/avatar/upload`; // 通用上传接口
    const headers = computed(() => {
      return {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    });

    // 表单数据，根据管理员信息调整
    const form = reactive({
      id: '',
      name: '',
      email: '',
      phone: '',
      avatar: ''
      // 移除 gender, department, title 等字段
    });

    const rules = {
      name: [
        {required: true, message: '请输入姓名', trigger: 'blur'}
      ],
      email: [
        {required: true, message: '请输入邮箱', trigger: 'blur'},
        {type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur'}
      ],
      phone: [
        {required: true, message: '请输入手机号', trigger: 'blur'},
        {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur'}
      ]
    };

    const fetchUserInfo = async () => {
      try {
        const response = await getAdminProfile();
        userInfo.value = response.data;
        userAvatar.value = response.data.avatar || '';
        // 填充表单
        Object.keys(form).forEach(key => {
          if (Object.prototype.hasOwnProperty.call(response.data, key)) {
            form[key] = response.data[key] || '';
          }
        });
      } catch (error) {
        console.error('获取管理员信息失败', error);
        ElMessage.error('获取管理员信息失败');
      }
    };

    const handleEdit = () => {
      dialogVisible.value = true;
      // 重新从最新的 userInfo 填充表单
      Object.keys(form).forEach(key => {
        if (Object.prototype.hasOwnProperty.call(userInfo.value, key)) {
          form[key] = userInfo.value[key] || '';
        }
      });
    };

    const handleSubmit = () => {
      formRef.value.validate((valid) => {
        if (valid) {
          updateAdminProfile(form).then(() => { // 调用管理员 API
            ElMessage.success('更新成功');
            dialogVisible.value = false;
            fetchUserInfo(); // 重新获取信息
          }).catch(error => {
            console.error('更新管理员信息失败', error);
            ElMessage.error('更新管理员信息失败');
          });
        }
      });
    };

    // 头像上传逻辑与之前相同
    const handleAvatarSuccess = (response) => {
      uploadingAvatar.value = false;
      if (response.code === 200 && response.data) {
        userAvatar.value = response.data;
        userInfo.value.avatar = response.data;
        form.avatar = response.data;
        ElMessage.success('头像更新成功');
      } else {
        ElMessage.error(response.message || '头像上传失败');
      }
    };

    const handleAvatarError = (error) => {
      uploadingAvatar.value = false;
      console.error("头像上传失败", error);
      ElMessage.error('头像上传失败，请稍后重试');
    };

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
      uploadingAvatar.value = true;
      return true;
    };

    onMounted(() => {
      fetchUserInfo();
    });

    return {
      userInfo,
      userAvatar,
      dialogVisible,
      formRef,
      form,
      rules,
      headers,
      uploadAvatarUrl,
      uploadingAvatar,
      handleEdit,
      handleSubmit,
      handleAvatarSuccess,
      handleAvatarError,
      beforeAvatarUpload
    };
  }
}
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