<template>
  <el-header class="app-header">
    <div class="header-logo">
      <h1>校园管理系统</h1>
    </div>
    <div class="header-menu">
      <el-menu :default-active="activeIndex" :router="true" mode="horizontal">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/notice">通知公告</el-menu-item>
        <el-menu-item index="/forum">校园论坛</el-menu-item>
        <el-menu-item index="/activities">校园活动</el-menu-item>
      </el-menu>
    </div>
    <div class="header-user">
      <template v-if="isLoggedIn">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            {{ userName }}
            <el-icon><ArrowDown/></el-icon>
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
        <el-button type="primary" @click="goToLogin">登录</el-button>
      </template>
    </div>
  </el-header>
</template>

<script>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {ArrowDown} from '@element-plus/icons-vue'

export default {
  name: 'AppHeader',
  components: {
    ArrowDown
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const activeIndex = ref('/')

    // 计算当前用户是否已登录
    const isLoggedIn = computed(() => {
      return !!localStorage.getItem('token')
    })

    // 获取用户名
    const userName = computed(() => {
      try {
        const userString = localStorage.getItem('user')
        if (userString) {
          const user = JSON.parse(userString)
          return user.username || '用户'
        }
        return '用户'
      } catch (e) {
        return '用户'
      }
    })

    // 获取用户角色
    const userRole = computed(() => {
      try {
        const userString = localStorage.getItem('user')
        if (userString) {
          const user = JSON.parse(userString)
          return user.role || ''
        }
        return ''
      } catch (e) {
        return ''
      }
    })

    // 监听路由变化，更新活动菜单项
    onMounted(() => {
      activeIndex.value = route.path
    })

    // 跳转到登录页
    const goToLogin = () => {
      router.push('/login')
    }

    // 处理下拉菜单命令
    const handleCommand = (command) => {
      if (command === 'logout') {
        ElMessageBox.confirm('确定要退出登录吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 清除本地存储的token和用户信息
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          localStorage.removeItem('role')
          ElMessage.success('已退出登录')
          router.push('/login')
        }).catch(() => {
        })
      } else if (command === 'profile') {
        // 根据用户角色跳转到不同的个人中心页面
        if (userRole.value === 'student') {
          router.push('/student/profile')
        } else if (userRole.value === 'teacher') {
          router.push('/teacher/profile')
        } else if (userRole.value === 'admin') {
          router.push('/admin/user')
        }
      } else if (command === 'dashboard') {
        // 根据用户角色跳转到不同的工作台
        if (userRole.value === 'student') {
          router.push('/student')
        } else if (userRole.value === 'teacher') {
          router.push('/teacher')
        } else if (userRole.value === 'admin') {
          router.push('/admin')
        }
      }
    }

    return {
      activeIndex,
      isLoggedIn,
      userName,
      goToLogin,
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
}

.header-logo h1 {
  margin: 0;
  font-size: 18px;
  color: #409EFF;
}

.header-user {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}

.user-info .el-icon {
  margin-left: 5px;
}
</style> 