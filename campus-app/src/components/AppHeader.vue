<template>
  <el-header class="app-header">
    <div class="header-logo">
      <h1>智慧校园管理系统</h1>
    </div>
    <div class="header-user">
      <template v-if="isLoggedIn">
        <span class="welcome-message">{{ userRoleName }} ({{ userName }}), 欢迎您!</span>
        <el-dropdown @command="handleCommand">
          <span class="user-info-dropdown-trigger">
            <el-icon><Setting/></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="dashboard">工作台</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else>
        <el-button type="primary" @click="goToHome">登录</el-button>
      </template>
    </div>
  </el-header>
</template>

<script>
import {computed} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Setting} from '@element-plus/icons-vue'

export default {
  name: 'AppHeader',
  components: {
    Setting
  },
  setup() {
    const router = useRouter()

    const isLoggedIn = computed(() => {
      return !!localStorage.getItem('token')
    })

    const getUserInfoFromStorage = () => {
      try {
        const userString = localStorage.getItem('user')
        return userString ? JSON.parse(userString) : null
      } catch (e) {
        return null
      }
    }

    const userName = computed(() => {
      const user = getUserInfoFromStorage()
      return user?.username || '用户'
    })

    const userRole = computed(() => {
      const user = getUserInfoFromStorage()
      return user?.role || ''
    })

    const userRoleName = computed(() => {
      const role = userRole.value
      const names = {
        student: '学生',
        teacher: '教师',
        admin: '管理员'
      }
      return names[role] || role
    })

    const goToHome = () => {
      router.push('/')
    }

    const handleCommand = (command) => {
      if (command === 'logout') {
        ElMessageBox.confirm('确定要退出登录吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          localStorage.removeItem('role')
          ElMessage.success('已退出登录')
          router.push('/')
        }).catch(() => {
        })
      } else if (command === 'profile') {
        if (userRole.value === 'student') {
          router.push('/student/profile')
        } else if (userRole.value === 'teacher') {
          router.push('/teacher/profile')
        } else if (userRole.value === 'admin') {
          router.push('/admin/profile')
        } else {
          router.push('/')
        }
      } else if (command === 'dashboard') {
        if (userRole.value === 'student') {
          router.push('/student')
        } else if (userRole.value === 'teacher') {
          router.push('/teacher')
        } else if (userRole.value === 'admin') {
          router.push('/admin')
        } else {
          router.push('/')
        }
      }
    }

    return {
      isLoggedIn,
      userName,
      userRoleName,
      goToHome,
      handleCommand
    }
  }
}
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, .08);
  padding: 0 20px;
  height: 60px;
}

.header-logo h1 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.header-user {
  display: flex;
  align-items: center;
}

.welcome-message {
  margin-right: 15px;
  color: #606266;
  font-size: 14px;
}

.user-info-dropdown-trigger {
  display: flex;
  align-items: center;
  cursor: pointer;
  outline: none;
}

.user-info-dropdown-trigger .el-icon {
  font-size: 18px;
  color: #606266;
}
</style> 