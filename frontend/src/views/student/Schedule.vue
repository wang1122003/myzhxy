<template>
  <ScheduleView
      :showTeacherInfo="true"
      role="student"
      title="我的课表"
  />
</template>

<script setup>
import ScheduleView from '@/components/common/ScheduleView.vue';
</script>