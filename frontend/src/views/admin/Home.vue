<template>
  <div class="admin-dashboard">
    <h1>管理员工作台</h1>
    <p>这里是管理员功能概览和快捷入口。</p>
    <!-- TODO: 在这里添加具体的仪表盘内容，如图表、统计数据、常用操作链接等 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>系统概览</span>
        </div>
      </template>
      <div>用户总数：XXX</div>
      <div>课程总数：XXX</div>
      <div>今日活动：XXX</div>
    </el-card>
  </div>
</template>

<script setup>
// import { ref, onMounted } from 'vue';
// import { ElCard } from 'element-plus'; // 假设使用 Element Plus

// onMounted(() => {
//   // 获取仪表盘数据
// });
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.box-card {
  margin-top: 20px;
}
.card-header {
  font-weight: bold;
}
</style> 