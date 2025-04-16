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
              v-if="isEditing"
              :before-upload="beforeAvatarUpload"
              :on-success="handleAvatarSuccess"
              :show-file-list="false"
              action="/api/upload"
              class="avatar-uploader"
          >
            <el-button size="small">修改头像</el-button>
          </el-upload>
        </div>

        <div class="info-container">
          <el-descriptions :column="2" border title="基本信息">
            <el-descriptions-item label="工号">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ userInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ userInfo.gender === 'male' ? '男' : '女' }}</el-descriptions-item>
            <el-descriptions-item label="院系">{{ userInfo.department }}</el-descriptions-item>
            <el-descriptions-item label="职称">{{ userInfo.title }}</el-descriptions-item>
            <el-descriptions-item label="研究方向">{{ userInfo.researchField }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
            <el-descriptions-item label="手机">{{ userInfo.phone }}</el-descriptions-item>
            <el-descriptions-item label="办公室">{{ userInfo.office }}</el-descriptions-item>
          </el-descriptions>

          <el-divider/>

          <div class="profile-section">
            <h3>个人简介</h3>
            <p>{{ userInfo.bio || '暂无个人简介' }}</p>
          </div>
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
          <el-input v-model="form.name" :disabled="true"/>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="male">男</el-radio>
            <el-radio label="female">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="院系" prop="department">
          <el-input v-model="form.department" :disabled="true"/>
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-select v-model="form.title" placeholder="请选择职称">
            <el-option label="教授" value="professor"/>
            <el-option label="副教授" value="associateProfessor"/>
            <el-option label="讲师" value="lecturer"/>
            <el-option label="助教" value="assistant"/>
          </el-select>
        </el-form-item>
        <el-form-item label="研究方向" prop="researchField">
          <el-input v-model="form.researchField"/>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email"/>
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone"/>
        </el-form-item>
        <el-form-item label="办公室" prop="office">
          <el-input v-model="form.office"/>
        </el-form-item>
        <el-form-item label="个人简介" prop="bio">
          <el-input
              v-model="form.bio"
              :rows="4"
              placeholder="请输入个人简介"
              type="textarea"
          />
        </el-form-item>
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
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {getUserInfo, updateUserInfo} from '@/api/user'

export default {
  name: 'TeacherProfile',
  setup() {
    const userInfo = ref({})
    const userAvatar = ref('')
    const isEditing = ref(false)
    const dialogVisible = ref(false)
    const formRef = ref(null)
    const form = reactive({
      id: '',
      name: '',
      gender: 'male',
      department: '',
      title: '',
      researchField: '',
      email: '',
      phone: '',
      office: '',
      bio: '',
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
      ],
      title: [
        {required: true, message: '请选择职称', trigger: 'change'}
      ]
    }

    const fetchUserInfo = () => {
      getUserInfo().then(response => {
        userInfo.value = response.data
        userAvatar.value = response.data.avatar || ''
      }).catch(error => {
        console.error('获取用户信息失败', error)
        ElMessage.error('获取用户信息失败')
      })
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
          updateUserInfo(form).then(() => {
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
      userAvatar.value = response.data
      form.avatar = response.data
      updateUserInfo({id: userInfo.value.id, avatar: response.data}).then(() => {
        ElMessage.success('头像更新成功')
        fetchUserInfo()
      }).catch(error => {
        console.error('更新头像失败', error)
        ElMessage.error('更新头像失败')
      })
    }

    const beforeAvatarUpload = (file) => {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        ElMessage.error('头像图片只能是JPG或PNG格式!')
      }
      if (!isLt2M) {
        ElMessage.error('头像图片大小不能超过2MB!')
      }
      return isJPG && isLt2M
    }

    onMounted(() => {
      fetchUserInfo()
    })

    return {
      userInfo,
      userAvatar,
      isEditing,
      dialogVisible,
      formRef,
      form,
      rules,
      handleEdit,
      handleSubmit,
      handleAvatarSuccess,
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

.profile-section {
  margin-top: 20px;
}

.profile-section h3 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #606266;
}

.profile-section p {
  margin: 0;
  line-height: 1.6;
  color: #303133;
}
</style> 