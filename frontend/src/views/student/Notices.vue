<template>
  <NoticeView
      :canManage="false"
      :showFilter="true"
      role="student"
  />
</template>

<script setup>
import NoticeView from '@/components/common/NoticeView.vue';
</script>