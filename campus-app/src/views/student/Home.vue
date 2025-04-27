<template>
  <div class="student-dashboard">
    <h1>学生主页</h1>
    <p>欢迎回来！这里是你的课程、成绩和活动概览。</p>
    <!-- TODO: 添加具体内容，如近期课程、待办事项、成绩提醒等 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>我的课表</span>
            </div>
          </template>
          <div>今日课程：XXX</div>
          <div>明日课程：XXX</div>
          <router-link to="/student/schedule">查看完整课表</router-link>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>最新通知</span>
            </div>
          </template>
          <div>[通知标题1]</div>
          <div>[通知标题2]</div>
          <router-link to="/student/notices">查看所有通知</router-link>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
// import { ref, onMounted } from 'vue';
// import { ElCard, ElRow, ElCol } from 'element-plus';
// import { RouterLink } from 'vue-router';

// onMounted(() => {
//   // 获取数据
// });
</script>

<style scoped>
.student-dashboard {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}
.card-header {
  font-weight: bold;
}
</style> 