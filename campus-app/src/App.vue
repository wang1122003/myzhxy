<template>
  <el-container class="app-container">
    <app-header/>
    <el-main class="app-main">
      <router-view v-slot="{ Component }">
        <transition mode="out-in" name="fade">
          <keep-alive>
            <component :is="Component"/>
          </keep-alive>
        </transition>
      </router-view>
    </el-main>
    <el-footer class="app-footer">
      <div class="footer-content">
        <p>智慧校园管理系统 © {{ currentYear }} 版权所有</p>
      </div>
    </el-footer>
  </el-container>
</template>

<script>
import AppHeader from './components/AppHeader.vue'
import {computed, onMounted} from 'vue'
import {fetchUserInfo} from '@/utils/auth'

export default {
  name: 'App',
  components: {
    AppHeader
  },
  setup() {
    const currentYear = computed(() => new Date().getFullYear())

    onMounted(async () => {
      // 添加页面加载完成后的类，用于页面过渡动画
      document.body.classList.add('app-loaded')
      // 尝试从 localStorage 恢复用户信息到响应式变量中
      // auth.js 文件本身在加载时就会执行这个初始化
      // 但如果需要确保在 App 组件挂载时 userInfo 是最新的，可以调用 fetchUserInfo
      // 注意：这里的 fetchUserInfo 不会发 API 请求，只是检查本地状态
      try {
        await fetchUserInfo()
      } catch (error) {
        console.error('App mounted: Error during initial user info fetch/check:', error)
        // 如果检查出错，可能需要强制登出
        // logout()
        // router.push('/login')
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

/* 渐变色彩类 (移除，因为未使用) */
/*
.gradient-text {
  background: linear-gradient(to right, #3a7bd5, #00d2ff);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
*/

#app {
  height: 100%;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  padding: 16px;
  background-color: #f5f7fa;
}

.app-footer {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-top: 1px solid #dcdfe6;
  @media (max-width: 768px) {
    height: 50px;
    padding: 0 16px;
  }
}

.footer-content {
  font-size: 14px;
  color: #606266;
  @media (max-width: 768px) {
    font-size: 12px;
  }
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

/* 加载动画 */
.app-loaded .el-loading-mask {
  transition: opacity 0.5s;
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

  .app-main {
    padding: 12px;
  }

  html, body {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .app-main {
    padding: 8px;
  }

  html, body {
    font-size: 12px;
  }

  .app-footer {
    height: 40px;
    padding: 0 12px;
  }

  .footer-content {
    font-size: 11px;
  }
}
</style>
