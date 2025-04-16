<template>
  <div class="error-container">
    <div class="error-content">
      <el-icon class="error-icon">
        <WarningFilled/>
      </el-icon>
      <h1>访问错误</h1>
      <p>抱歉，您没有权限访问该页面或者页面不存在</p>
      <div class="error-actions">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
        <el-button @click="$router.go(-1)">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import {WarningFilled} from '@element-plus/icons-vue'

export default {
  name: 'ErrorPage',
  components: {
    WarningFilled
  }
}
</script>

<style scoped>
.error-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.error-content {
  text-align: center;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-width: 500px;
}

.error-icon {
  font-size: 80px;
  color: #F56C6C;
  margin-bottom: 20px;
}

h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 20px;
}

p {
  color: #606266;
  margin-bottom: 30px;
  font-size: 16px;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style> 