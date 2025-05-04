<template>
  <ScheduleView
      :showTeacherInfo="false"
      role="teacher"
      title="我的课表"
  />
</template>

<script setup>
import ScheduleView from '@/components/common/ScheduleView.vue';
</script>