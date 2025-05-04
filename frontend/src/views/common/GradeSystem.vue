<template>
  <div class="grade-system-container">
    <!-- 根据用户角色显示不同组件 -->
    <template v-if="userRole === 'student'">
      <student-grade-view/>
    </template>

    <template v-else-if="userRole === 'teacher'">
      <!-- 教师端课程选择和成绩管理 -->
      <div v-if="!selectedCourse">
        <teacher-course-list @select-course="handleSelectCourse"/>
      </div>
      <div v-else>
        <div class="back-button-container">
          <el-button icon="ArrowLeft" @click="backToCourseList">返回课程列表</el-button>
        </div>
        <teacher-grade-management :course="selectedCourse"/>
      </div>
    </template>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {useUserStore} from '@/stores/userStore';
import StudentGradeView from './grade/StudentGradeView.vue';
import TeacherCourseList from './grade/TeacherCourseList.vue';
import TeacherGradeManagement from './grade/TeacherGradeManagement.vue';

const userStore = useUserStore();
const selectedCourse = ref(null);

// 使用计算属性获取用户角色，而不是ref
const userRole = computed(() => {
  return userStore.userRole();
});

// 选择要管理成绩的课程
const handleSelectCourse = (course) => {
  selectedCourse.value = course;
};

// 返回课程列表
const backToCourseList = () => {
  selectedCourse.value = null;
};

// 确保我们在组件挂载前已经有正确的角色信息
// 如果需要，可以在这里添加角色验证逻辑
onMounted(() => {
  console.log('GradeSystem 组件挂载，当前用户角色:', userRole.value);
});
</script>

<style scoped>
.grade-system-container {
  padding: 20px;
}

.back-button-container {
  margin-bottom: 20px;
}
</style> 