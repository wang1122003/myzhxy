<template>
  <el-container class="app-container">
    <app-header/>
    <el-main class="app-main">
      <router-view/>
    </el-main>
    <el-footer class="app-footer">
      <div class="footer-content">
        <p>智慧校园管理系统</p>
      </div>
    </el-footer>
  </el-container>
</template>

<script>
import AppHeader from './components/AppHeader.vue'

export default {
  name: 'App',
  components: {
    AppHeader
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
  font-family: Arial, Helvetica, sans-serif;
}

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
  padding: 0;
  background-color: #f5f7fa;
}

.app-footer {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-top: 1px solid #dcdfe6;
}

.footer-content {
  font-size: 14px;
  color: #606266;
}
</style>
