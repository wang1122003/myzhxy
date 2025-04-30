<template>
  <el-container class="layout-container">
    <!-- 桌面端侧边栏 -->
    <el-aside v-if="!isMobile" :class="{'is-collapse': isCollapsed}" :width="isCollapsed ? '64px' : '200px'"
              class="sidebar-container">
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
          <template v-for="item in sidebarMenuItems" :key="item.index">
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

    <el-container class="main-container">
      <!-- 移动端顶部导航 -->
      <el-header v-if="isMobile" class="mobile-header">
        <div class="mobile-header-title">学生工作台</div>
        <el-dropdown trigger="click" @command="navigateTo">
          <el-button circle icon="Menu" type="primary"></el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="item in sidebarMenuItems" :key="item.index" :command="item.index">
                <el-icon>
                  <component :is="item.icon"/>
                </el-icon>
                <span>{{ item.title }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="app-main">
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {Expand, Fold, QuestionFilled} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isCollapsed = ref(false)
const isMobile = ref(false)

// 动态生成侧边栏菜单项
const sidebarMenuItems = computed(() => {
  const studentRoute = router.options.routes.find(r => r.name === 'StudentLayout');
  if (!studentRoute || !studentRoute.children) {
    return [];
  }
  return studentRoute.children
      .filter(child => child.meta && child.meta.showInSidebar)
      .map(child => {
        const parentPath = studentRoute.path.endsWith('/') ? studentRoute.path : studentRoute.path + '/';
        const fullPath = parentPath + child.path;
        return {
          index: fullPath.replace(/\/$/, '') || studentRoute.path,
          title: child.meta.title || 'Untitled',
          icon: child.meta.icon || QuestionFilled
        }
      });
});

onMounted(() => {
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

const navigateTo = (path) => {
  router.push(path);
}
</script>

<script>
// Non-setup script block for component name
export default {
  name: 'StudentLayout'
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
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  color: #bfcbd9;
  padding: 10px;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
}

.el-menu-vertical {
  border-right: none;
  flex-grow: 1;
}

.main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.app-main {
  padding: 20px;
  background-color: #f0f2f5;
  flex-grow: 1;
  overflow-y: auto;
}

.el-scrollbar {
  height: calc(100% - 60px);
}

.scrollbar-wrapper {
  overflow-x: hidden !important;
}

/* 移动端样式 */
.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  background-color: #304156;
  color: white;
  height: 50px;
}

.mobile-header-title {
  font-size: 18px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .app-main {
    padding: 10px;
  }
}
</style> 