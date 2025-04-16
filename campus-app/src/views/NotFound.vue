<template>
  <div class="not-found-container">
    <h1>404 - 页面未找到</h1>
    <p>抱歉，您访问的页面不存在。</p>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup>
import {useRouter} from 'vue-router';

const router = useRouter();

const goHome = () => {
  router.push('/');
};
</script>

<style scoped>
.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 100px); /* Adjust height as needed */
  text-align: center;
}

h1 {
  font-size: 3em;
  margin-bottom: 10px;
}

p {
  font-size: 1.2em;
  margin-bottom: 20px;
}
</style> 