<template>
  <el-container class="app-container" direction="vertical">
    <app-header/>
    <el-main class="app-main-global">
      <router-view v-slot="{ Component, route }">
        <transition mode="out-in" name="fade">
          <component :is="Component" :key="route.fullPath"/>
        </transition>
      </router-view>
    </el-main>
  </el-container>
</template>

<script>
import {computed, onMounted} from 'vue'
import {useUserStore} from '@/stores/userStore'
import AppHeader from '@/views/layouts/AppHeader.vue'

export default {
  name: 'App',
  components: {
    AppHeader,
  },
  setup() {
    const userStore = useUserStore()
    const currentYear = computed(() => new Date().getFullYear())

    const tryRestoreUserSession = async () => {
      console.log("[App.vue] 尝试恢复用户会话...");
      // 检查localStorage中是否有token
      const token = localStorage.getItem('token');
      if (token) {
        console.log("[App.vue] 发现token，设置到store中...");
        userStore.setToken(token); // 更新Pinia store中的token

        // 即使有token，也需要验证会话有效性并获取最新用户信息
        // 如果 userStore.isLoggedIn.value 已经是 true (因为 setToken 可能触发其更新)
        // 并且 userInfo 还没有被填充，则尝试获取
        if (userStore.isLoggedIn.value) { // 使用 .value
          console.log("[App.vue] 用户已登录，尝试获取/验证用户信息...");
          try {
            // fetchAndSetUserInfo 会验证token并在成功时设置userInfo
            await userStore.fetchAndSetUserInfo();
            console.log('[App.vue] 用户信息获取完成。');
          } catch (fetchError) {
            // 会话可能已经失效，但我们不立即登出用户
            // 只是记录错误信息，允许用户继续操作，等到需要授权的操作时再处理
            console.error('[App.vue] 用户信息获取失败，但继续保持当前状态:', fetchError.message);

            // 更新存储中的错误状态，但不强制登出
            userStore.setSessionError(true);
          }
        } else {
          console.log('[App.vue] 用户未在本地登录，跳过用户信息获取。');
        }
      }
    }

    onMounted(async () => {
      document.body.classList.add('app-loaded')
      try {
        await tryRestoreUserSession();
      } catch (error) {
        console.error('App挂载：初始用户信息获取过程中出错:', error);
      }
    })

    return {
      currentYear
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #ebeef5;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #909399;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #606266;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.app-container {
  min-height: 100vh;
  display: flex;
}

.app-main-global {
  flex: 1;
  padding: 0;
  overflow-y: auto;
}

/* 页面过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 卡片悬浮效果 */
.el-card {
  transition: transform 0.3s, box-shadow 0.3s;
  margin-bottom: 16px;
}

.el-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

/* 表格增强样式 */
.el-table {
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 6px 0 rgba(0, 0, 0, 0.05);
}

.el-table th.el-table__cell {
  background-color: rgba(58, 123, 213, 0.05);
  font-weight: bold;
}

.el-table .el-table__row:hover > td.el-table__cell {
  background-color: rgba(58, 123, 213, 0.05);
}

/* 按钮悬停增强 */
.el-button:not(.is-disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

/* 响应式断点调整 */
@media (max-width: 768px) {
  .el-table {
    width: 100%;
    overflow-x: auto;
  }

  .el-dialog {
    width: 90% !important;
    margin-top: 10vh !important;
  }

  .app-main-global {
    padding: 12px;
  }

  html, body {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .app-main-global {
    padding: 8px;
  }

  html, body {
    font-size: 12px;
  }
}
</style>
