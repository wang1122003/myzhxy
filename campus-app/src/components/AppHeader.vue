<template>
  <el-header class="app-header">
    <div class="header-logo">
      <img alt="校园系统" class="logo-image" src="@/assets/logo.png"/>
      <h1>智慧校园管理系统</h1>
    </div>
    <div class="header-nav">
      <el-button class="nav-button" link @click="goToForum">
        <el-icon>
          <ChatDotRound/>
        </el-icon>
        校园论坛
      </el-button>
    </div>
    <div class="header-user">
      <template v-if="isLoggedIn">
        <div class="user-greeting">
          <span class="welcome-message">{{ userRoleName }} ({{ userName }}), 欢迎您!</span>
        </div>
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-avatar">
            <el-avatar :size="36" :src="userAvatar">{{ userName.substring(0, 1).toUpperCase() }}</el-avatar>
            <el-icon class="dropdown-icon">
              <ArrowDown/>
            </el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon>
                  <User/>
                </el-icon>
                <span>个人中心</span>
              </el-dropdown-item>
              <el-dropdown-item command="dashboard">
                <el-icon>
                  <HomeFilled/>
                </el-icon>
                <span>工作台</span>
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon>
                  <SwitchButton/>
                </el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else>
        <el-button class="nav-button-mobile" link @click="goToForum">
          <el-icon>
            <ChatDotRound/>
          </el-icon>
          论坛
        </el-button>
        <el-button type="primary" @click="goToHome">
          <el-icon>
            <Right/>
          </el-icon>
          登录
        </el-button>
      </template>
    </div>
  </el-header>
</template>

<script>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {ArrowDown, ChatDotRound, HomeFilled, Right, SwitchButton, User} from '@element-plus/icons-vue'

export default {
  name: 'AppHeader',
  components: {
    User,
    HomeFilled,
    SwitchButton,
    ArrowDown,
    Right,
    ChatDotRound
  },
  setup() {
    const router = useRouter()
    const userAvatar = ref('')

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

    const goToForum = () => {
      router.push('/forum')
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
      userAvatar,
      goToHome,
      goToForum,
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
  box-shadow: 0 2px 6px 0 rgba(0, 0, 0, 0.05);
  padding: 0 24px;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-logo {
  display: flex;
  align-items: center;
}

.logo-image {
  height: 32px;
  margin-right: 8px;
}

.header-logo h1 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  font-weight: bold;
  background: linear-gradient(to right, #3a7bd5, #00d2ff);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-nav {
  display: flex;
  align-items: center;
  margin-left: auto;
  margin-right: auto;
}

.nav-button {
  font-size: 16px;
  font-weight: 500;
  color: #409EFF;
}

.nav-button:hover {
  color: #66b1ff;
}

.header-user {
  display: flex;
  align-items: center;
}

.user-greeting {
  margin-right: 16px;
}

.welcome-message {
  margin-right: 16px;
  color: #606266;
  font-size: 14px;
}

.user-avatar {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 4px;
  border-radius: 4px;
}

.user-avatar:hover {
  background-color: rgba(58, 123, 213, 0.1);
}

.dropdown-icon {
  font-size: 13px;
  color: #909399;
  margin-left: 4px;
}

.nav-button-mobile {
  display: none;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 16px;
  }

  .header-logo h1 {
    font-size: 16px;
  }

  .welcome-message {
    display: none;
  }

  .header-nav {
    display: none;
  }

  .nav-button-mobile {
    display: inline-flex;
    margin-right: 10px;
  }
}

@media (max-width: 576px) {
  .app-header {
    padding: 0 12px;
  }

  .header-logo h1 {
    font-size: 14px;
  }
}
</style> 