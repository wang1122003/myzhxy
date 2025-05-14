<template>
  <!-- 增强型页面容器组件 -->
  <div class="enhanced-page-container">
    <!-- 页面头部区域 -->
    <div class="enhanced-page-header">
      <!-- 头部左侧 (标题和副标题) -->
      <div class="header-left">
        <!-- 主标题 (优先使用 prop) -->
        <h2 v-if="title">{{ title }}</h2>
        <!-- 标题插槽 (如果提供了插槽，则优先显示插槽内容) -->
        <slot name="title"></slot>
        <!-- 副标题区域 (当提供了 subtitle prop 或 subtitle 插槽时显示) -->
        <div v-if="subtitle || $slots.subtitle" class="header-subtitle">
          <!-- 副标题文本 (优先使用 prop) -->
          <span v-if="subtitle">{{ subtitle }}</span>
          <!-- 副标题插槽 -->
          <slot name="subtitle"></slot>
        </div>
      </div>
      <!-- 头部右侧 (通常用于放置操作按钮) -->
      <div class="header-right">
        <!-- 头部操作区插槽 -->
        <slot name="header-actions"></slot>
      </div>
    </div>

    <!-- 面包屑导航区域 (当提供了 breadcrumb prop 或 breadcrumb 插槽时显示) -->
    <div v-if="breadcrumb || $slots.breadcrumb" class="breadcrumb-container">
      <!-- 面包屑插槽 -->
      <slot name="breadcrumb">
        <!-- 默认面包屑实现 (使用 Element Plus breadcrumb) -->
        <el-breadcrumb v-if="breadcrumb && breadcrumb.length">
          <!-- 遍历面包屑数据 -->
          <el-breadcrumb-item v-for="(item, index) in breadcrumb" :key="index" :to="item.to">
            {{ item.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </slot>
    </div>

    <!-- 筛选区域卡片 (当提供了 filters 插槽时显示) -->
    <el-card v-if="$slots.filters" class="filter-card">
      <!-- 筛选内容插槽 -->
      <slot name="filters"></slot>
    </el-card>

    <!-- 主要内容区域卡片 -->
    <el-card :body-style="contentStyle" class="content-card">
      <!-- 内容卡片头部 (当提供了 contentTitle prop 或 content-title 插槽时显示) -->
      <template v-if="contentTitle || $slots.content-title" #header>
        <div class="content-card-header">
          <!-- 内容标题 (优先使用 prop) -->
          <span v-if="contentTitle">{{ contentTitle }}</span>
          <!-- 内容标题插槽 -->
          <slot name="content-title"></slot>
          <!-- 内容卡片头部右侧的操作区 -->
          <div class="content-actions">
            <!-- 内容操作区插槽 -->
            <slot name="content-actions"></slot>
          </div>
        </div>
      </template>
      <!-- 主要内容插槽 (默认插槽) -->
      <slot></slot>
    </el-card>

    <!-- 页面底部区域 (当提供了 footer 插槽时显示) -->
    <div v-if="$slots.footer" class="page-footer">
      <!-- 底部内容插槽 -->
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue';

// 定义组件 props
const props = defineProps({
  // 页面主标题
  title: {
    type: String,
    default: '' // 默认为空字符串
  },
  // 页面副标题
  subtitle: {
    type: String,
    default: ''
  },
  // 内容卡片的标题
  contentTitle: {
    type: String,
    default: ''
  },
  // 面包屑导航数据
  // 数组格式应为 [{ title: string, to?: object | string }, ...]
  breadcrumb: {
    type: Array,
    default: () => [] // 默认为空数组
  },
  // 内容卡片的内边距 (CSS padding 值)
  contentPadding: {
    type: String,
    default: '20px' // 默认为 20px
  }
});

// 计算内容卡片的 body-style
const contentStyle = computed(() => {
  // 返回一个包含 padding 属性的对象
  return {
    padding: props.contentPadding
  };
});
</script>

<style scoped>
/* 页面容器整体样式 */
.enhanced-page-container {
  padding: 20px; /* 内边距 */
  height: 100%; /* 高度占满父容器 (如果父容器有确定高度) */
  box-sizing: border-box; /* 内边距和边框包含在宽高内 */
}

/* 页面头部样式 */
.enhanced-page-header {
  display: flex; /* 使用 flex 布局 */
  justify-content: space-between; /* 两端对齐 */
  align-items: flex-start; /* 垂直方向顶部对齐 */
  margin-bottom: 20px; /* 底部外边距 */
}

/* 头部左侧样式 */
.header-left {
  display: flex;
  flex-direction: column; /* 垂直排列 */
}

/* 主标题样式 */
.header-left h2 {
  margin: 0; /* 移除默认外边距 */
  font-size: 22px; /* 字号 */
  font-weight: 600; /* 字重 */
  color: var(--el-text-color-primary); /* 使用 Element Plus 的主文本颜色变量 */
  line-height: 1.4; /* 行高 */
}

/* 副标题样式 */
.header-subtitle {
  margin-top: 8px; /* 顶部外边距 */
  font-size: 14px; /* 字号 */
  color: var(--el-text-color-secondary); /* 使用 Element Plus 的次要文本颜色变量 */
}

/* 头部右侧样式 */
.header-right {
  display: flex;
  align-items: center; /* 垂直居中 */
  gap: 12px; /* 子元素间距 */
}

/* 面包屑容器样式 */
.breadcrumb-container {
  margin-bottom: 20px;
}

/* 筛选卡片样式 */
.filter-card {
  margin-bottom: 20px;
}

/* 内容卡片样式 */
.content-card {
  margin-bottom: 20px;
}

/* 内容卡片头部样式 */
.content-card-header {
  display: flex;
  justify-content: space-between; /* 两端对齐 */
  align-items: center; /* 垂直居中 */
  font-size: 16px; /* 字号 */
  font-weight: 500; /* 字重 */
}

/* 内容卡片头部操作区样式 */
.content-actions {
  display: flex;
  gap: 8px; /* 子元素间距 */
}

/* 页面底部样式 */
.page-footer {
  margin-top: 20px;
}
</style> 