<template>
  <CourseManagementView
      :showAddButton="false"
      :showFilter="true"
      role="teacher"
      title="我的课程管理"
  />
</template>

<script setup>
import CourseManagementView from '@/components/common/CourseManagementView.vue';
</script> 