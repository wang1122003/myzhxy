<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">智慧校园</div>
      <el-menu
          :default-active="$route.path"
          active-text-color="#409EFF"
          background-color="#304156"
          class="el-menu-vertical"
          router
          text-color="#bfcbd9"
      >
        <el-menu-item index="/admin">
          <el-icon>
            <HomeFilled/>
          </el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon>
            <User/>
          </el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/course">
          <el-icon>
            <Document/>
          </el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/classroom">
          <el-icon>
            <School/>
          </el-icon>
          <span>教室管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/schedule">
          <el-icon>
            <Calendar/>
          </el-icon>
          <span>课表管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/activity">
          <el-icon>
            <Bell/>
          </el-icon>
          <span>活动管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/notice">
          <el-icon>
            <Message/>
          </el-icon>
          <span>公告管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/forum">
          <el-icon>
            <ChatDotRound/>
          </el-icon>
          <span>论坛管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/file">
          <el-icon>
            <Files/>
          </el-icon>
          <span>文件管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="avatar-wrapper">
              <el-avatar :size="30" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"/>
              <span class="username">{{ userInfo.name || '管理员' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">个人资料</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Bell, Calendar, ChatDotRound, Document, Files, HomeFilled, Message, School, User} from '@element-plus/icons-vue'
import {getCurrentUser} from '@/api/user'

export default {
  name: 'AdminIndex',
  components: {
    HomeFilled,
    User,
    Document,
    School,
    Calendar,
    Bell,
    Message,
    ChatDotRound,
    Files
  },
  setup() {
    const router = useRouter()
    const userInfo = ref({})

    onMounted(() => {
      fetchUserInfo()
    })

    const fetchUserInfo = () => {
      getCurrentUser().then(response => {
        userInfo.value = response.data
      }).catch(() => {
        logout()
      })
    }

    const goToProfile = () => {
      router.push('/admin/profile')
    }

    const logout = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      router.push('/login')
      ElMessage({
        message: '退出登录成功',
        type: 'success'
      })
    }

    return {
      userInfo,
      goToProfile,
      logout
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  color: #fff;
}

.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
  font-size: 14px;
  color: #606266;
}

.el-menu-vertical {
  border-right: none;
}
</style> 