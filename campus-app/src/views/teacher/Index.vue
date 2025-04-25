<template>
  <el-container class="layout-container">
    <el-aside :class="{'is-collapse': isCollapsed}" :width="isCollapsed ? '64px' : '200px'" class="sidebar-container">
      <div class="sidebar-header">
        <el-icon class="collapse-icon" @click="toggleCollapse">
          <Expand v-if="isCollapsed"/>
          <Fold v-else/>
        </el-icon>
      </div>
      <el-scrollbar wrap-class="scrollbar-wrapper">
        <el-menu
            :collapse="isCollapsed"
            :collapse-transition="false"
            :default-active="$route.path"
            active-text-color="#409EFF"
            background-color="#304156"
            class="el-menu-vertical"
            router
            text-color="#bfcbd9"
            @select="handleMenuSelect"
        >
          <template v-for="item in menuItems" :key="item.index">
            <el-menu-item :index="item.index">
              <el-icon>
                <component :is="item.icon"/>
              </el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>
    <el-container>
      <el-main>
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref, shallowRef} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Calendar, Document, Expand, Fold, HomeFilled, User} from '@element-plus/icons-vue'
import {getUserInfo} from '@/api/user'

const router = useRouter()
const route = useRoute()
const userInfo = ref({})
const isCollapsed = ref(false)
const isMobile = ref(false)

// 定义菜单项
const menuItems = shallowRef([
  {index: '/teacher', icon: HomeFilled, title: '首页'},
  // {index: '/teacher/notices', icon: ChatLineRound, title: '通知公告'}, // 移除通知公告
  {index: '/teacher/profile', icon: User, title: '个人资料'},
  {index: '/teacher/schedule', icon: Calendar, title: '我的课表'},
  {index: '/teacher/course-management', icon: 'Notebook', title: '课程管理'},
  {index: '/teacher/grades', icon: 'DataAnalysis', title: '成绩管理'},
]);

// 新增：计算当前页面标题
const currentPageTitle = computed(() => {
  const currentPath = route.path;
  const menuItem = menuItems.value.find(item => {
    // 完全匹配或作为子路由前缀匹配
    return item.index === currentPath || currentPath.startsWith(item.index + '/');
  });
  return menuItem ? menuItem.title : '教师中心'; // 如果找不到匹配项，显示默认标题
});

onMounted(() => {
  fetchUserInfo()
  checkScreenWidth();
  window.addEventListener('resize', checkScreenWidth);
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkScreenWidth);
});

const checkScreenWidth = () => {
  isMobile.value = window.innerWidth < 768;
  isCollapsed.value = isMobile.value;
}

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
}

const handleMenuSelect = () => {
  if (isMobile.value) {
    isCollapsed.value = true;
  }
}

const fetchUserInfo = () => {
  getUserInfo().then(response => {
    userInfo.value = response.data
  }).catch(() => {
    logout()
  })
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
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar-container {
  background-color: #304156;
  color: #fff;
  transition: width 0.28s;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  color: #bfcbd9;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
}

.el-menu--collapse {
  width: 64px;
}

.el-menu-vertical {
  border-right: none;
}
</style> 