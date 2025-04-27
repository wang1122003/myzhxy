<template>
  <div class="teacher-dashboard">
    <h1>教师工作台</h1>
    <p>管理您的课程、学生和教学任务。</p>
    <!-- TODO: 添加具体内容，如待批改作业、近期课程、学生列表快捷方式等 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>我的课程</span>
            </div>
          </template>
          <div>[课程1 名称]</div>
          <div>[课程2 名称]</div>
          <router-link to="/teacher/course-management">管理我的课程</router-link>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>教学日历</span>
            </div>
          </template>
          <div>今日安排：XXX</div>
          <div>明日安排：XXX</div>
          <router-link to="/teacher/schedule">查看完整日历</router-link>
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
.teacher-dashboard {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}
.card-header {
  font-weight: bold;
}
</style> 