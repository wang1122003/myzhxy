<template>
  <NoticeView
      :canManage="false"
      :showFilter="true"
      role="teacher"
  />
</template>

<script setup>
import NoticeView from '@/components/common/NoticeView.vue';
</script> 