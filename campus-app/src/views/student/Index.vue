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
      <el-header class="header-container">
        <div class="header-title">学生端</div>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref, shallowRef} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessageBox} from 'element-plus'
import {Bell, Calendar, Document, Expand, Files, Fold, HomeFilled, User} from '@element-plus/icons-vue'
import {logout, userAvatar, userRealName} from '@/utils/auth'

const router = useRouter()
const route = useRoute()

const isCollapsed = ref(false)
const isMobile = ref(false)

const menuItems = shallowRef([
  {index: '/student', icon: HomeFilled, title: '首页'},
  // {index: '/student/notices', icon: ChatLineRound, title: '通知公告'},
  {index: '/student/profile', icon: User, title: '个人资料'},
  {index: '/student/schedule', icon: Calendar, title: '课程表'},
  {index: '/student/grades', icon: Document, title: '成绩查询'},
  {index: '/student/activities', icon: Bell, title: '活动列表'},
  {index: '/student/files', icon: Files, title: '文件管理'},
]);

const currentPageTitle = computed(() => {
  const currentPath = route.path;
  const menuItem = menuItems.value.find(item => {
    return item.index === currentPath || currentPath.startsWith(item.index + '/');
  });
  return menuItem ? menuItem.title : '学生中心';
});

onMounted(() => {
  checkScreenWidth();
  window.addEventListener('resize', checkScreenWidth);

  if (!userRealName || !userAvatar) {
    console.warn("Store 中无用户信息，强制登出");
    logout();
  }
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

const handleLogout = async () => {
  await ElMessageBox.confirm(
      '您确定要退出登录吗?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      await logout();
      router.push('/login');
    } catch (error) {
      console.error("登出时出错:", error);
      logout();
      router.push('/login');
    }
  }).catch(() => {
    // 用户取消，无需操作
  });
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