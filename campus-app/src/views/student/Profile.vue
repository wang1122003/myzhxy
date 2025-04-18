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
              :disabled="uploadingAvatar"
              :before-upload="beforeAvatarUpload"
              :on-success="handleAvatarSuccess"
              :headers="headers"
              :show-file-list="false"
              class="avatar-uploader"
              :on-error="handleAvatarError"
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
            <el-descriptions-item label="学号">
              {{ userInfo.username }}
            </el-descriptions-item>
            <el-descriptions-item label="姓名">
              {{ userInfo.name }}
            </el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ userInfo.gender === 'male' ? '男' : '女' }}
            </el-descriptions-item>
            <el-descriptions-item label="院系">
              {{ userInfo.department }}
            </el-descriptions-item>
            <el-descriptions-item label="专业">
              {{ userInfo.major }}
            </el-descriptions-item>
            <el-descriptions-item label="班级">
              {{ userInfo.className }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ userInfo.email }}
            </el-descriptions-item>
            <el-descriptions-item label="手机">
              {{ userInfo.phone }}
            </el-descriptions-item>
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
          <el-input
              v-model="form.name"
              :disabled="true"
          />
        </el-form-item>
        <el-form-item
            label="性别"
            prop="gender"
        >
          <el-radio-group v-model="form.gender">
            <el-radio label="male">
              男
            </el-radio>
            <el-radio label="female">
              女
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
            label="院系"
            prop="department"
        >
          <el-input
              v-model="form.department"
              :disabled="true"
          />
        </el-form-item>
        <el-form-item
            label="专业"
            prop="major"
        >
          <el-input
              v-model="form.major"
              :disabled="true"
          />
        </el-form-item>
        <el-form-item
            label="班级"
            prop="className"
        >
          <el-input
              v-model="form.className"
              :disabled="true"
          />
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
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Upload} from '@element-plus/icons-vue';
import {getStudentProfile, updateStudentProfile} from '@/api/user';

export default {
  name: 'StudentProfile',
  components: {
    Upload
  },
  setup() {
    const userInfo = ref({})
    const userAvatar = ref('')
    const dialogVisible = ref(false)
    const formRef = ref(null)
    const uploadingAvatar = ref(false)

    const uploadAvatarUrl = `/api/user/avatar/upload`
    const headers = computed(() => {
      return {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })

    const form = reactive({
      id: '',
      name: '',
      gender: 'male',
      department: '',
      major: '',
      className: '',
      email: '',
      phone: '',
      avatar: ''
    })

    const rules = {
      email: [
        {required: true, message: '请输入邮箱', trigger: 'blur'},
        {type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur'}
      ],
      phone: [
        {required: true, message: '请输入手机号', trigger: 'blur'},
        {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur'}
      ]
    }

    const fetchUserInfo = async () => {
      try {
        const response = await getStudentProfile()
        userInfo.value = response.data
        userAvatar.value = response.data.avatar || ''
      } catch (error) {
        console.error('获取用户信息失败', error)
        ElMessage.error('获取用户信息失败')
      }
    }

    const handleEdit = () => {
      dialogVisible.value = true
      Object.keys(form).forEach(key => {
        form[key] = userInfo.value[key] || ''
      })
    }

    const handleSubmit = () => {
      formRef.value.validate((valid) => {
        if (valid) {
          updateStudentProfile(form).then(() => {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            fetchUserInfo()
          }).catch(error => {
            console.error('更新用户信息失败', error)
            ElMessage.error('更新用户信息失败')
          })
        }
      })
    }

    const handleAvatarSuccess = (response) => {
      uploadingAvatar.value = false
      if (response.code === 200 && response.data) {
        userAvatar.value = response.data
        userInfo.value.avatar = response.data
        form.avatar = response.data
        ElMessage.success('头像更新成功')
      } else {
        ElMessage.error(response.message || '头像上传失败')
      }
    }

    const handleAvatarError = (error) => {
      uploadingAvatar.value = false
      console.error("头像上传失败", error)
      ElMessage.error('头像上传失败，请稍后重试')
    }

    const beforeAvatarUpload = (rawFile) => {
      const isJPG = rawFile.type === 'image/jpeg'
      const isPNG = rawFile.type === 'image/png'
      const isLt2M = rawFile.size / 1024 / 1024 < 2

      if (!isJPG && !isPNG) {
        ElMessage.error('头像图片只能是 JPG 或 PNG 格式!')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('头像图片大小不能超过 2MB!')
        return false
      }
      uploadingAvatar.value = true
      return true
    }

    onMounted(() => {
      fetchUserInfo()
    })

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
    }
  }
}
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

.info-item {
  margin-bottom: 15px;
}

.info-label {
  font-weight: bold;
  margin-right: 10px;
  min-width: 80px;
  display: inline-block;
}
</style> 