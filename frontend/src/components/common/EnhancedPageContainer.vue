<template>
  <div class="enhanced-page-container">
    <div class="enhanced-page-header">
      <div class="header-left">
        <h2 v-if="title">{{ title }}</h2>
        <slot name="title"></slot>
        <div v-if="subtitle || $slots.subtitle" class="header-subtitle">
          <span v-if="subtitle">{{ subtitle }}</span>
          <slot name="subtitle"></slot>
        </div>
      </div>
      <div class="header-right">
        <slot name="header-actions"></slot>
      </div>
    </div>

    <div v-if="breadcrumb || $slots.breadcrumb" class="breadcrumb-container">
      <slot name="breadcrumb">
        <el-breadcrumb v-if="breadcrumb && breadcrumb.length">
          <el-breadcrumb-item v-for="(item, index) in breadcrumb" :key="index" :to="item.to">
            {{ item.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </slot>
    </div>

    <el-card v-if="$slots.filters" class="filter-card">
      <slot name="filters"></slot>
    </el-card>

    <el-card :body-style="contentStyle" class="content-card">
      <template v-if="contentTitle || $slots.content-title" #header>
        <div class="content-card-header">
          <span v-if="contentTitle">{{ contentTitle }}</span>
          <slot name="content-title"></slot>
          <div class="content-actions">
            <slot name="content-actions"></slot>
          </div>
        </div>
      </template>
      <slot></slot>
    </el-card>

    <div v-if="$slots.footer" class="page-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue';

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  },
  contentTitle: {
    type: String,
    default: ''
  },
  breadcrumb: {
    type: Array,
    default: () => []
  },
  contentPadding: {
    type: String,
    default: '20px'
  }
});

const contentStyle = computed(() => {
  return {
    padding: props.contentPadding
  };
});
</script>

<style scoped>
.enhanced-page-container {
  padding: 20px;
  height: 100%;
}

.enhanced-page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-left h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
}

.header-subtitle {
  margin-top: 8px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb-container {
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.content-card {
  margin-bottom: 20px;
}

.content-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}

.content-actions {
  display: flex;
  gap: 8px;
}

.page-footer {
  margin-top: 20px;
}
</style> 