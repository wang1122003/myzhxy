<template>
  <div class="data-display">
    <!-- 加载状态骨架屏 -->
    <el-skeleton
        v-if="loading && skeletonRows > 0"
        :rows="skeletonRows"
        :throttle="500"
        animated
    />

    <!-- 加载图标状态 -->
    <div v-else-if="loading" class="loading-container">
      <el-icon :size="32" class="loading-icon">
        <Loading/>
      </el-icon>
      <p class="loading-text">{{ loadingText }}</p>
    </div>

    <!-- 空数据状态 -->
    <el-empty
        v-else-if="isEmpty"
        :description="emptyText"
        :image="emptyImage"
        :image-size="emptyImageSize"
    >
      <slot name="empty-action">
        <el-button v-if="showEmptyButton" @click="$emit('empty-action')">
          {{ emptyButtonText }}
        </el-button>
      </slot>
    </el-empty>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <el-icon :size="32" class="error-icon">
        <Warning/>
      </el-icon>
      <h3 class="error-title">{{ errorTitle }}</h3>
      <p class="error-text">{{ error }}</p>
      <el-button v-if="showRetryButton" @click="$emit('retry')">
        {{ retryButtonText }}
      </el-button>
    </div>

    <!-- 数据内容区域 -->
    <div v-else class="data-content">
      <slot></slot>
    </div>

    <!-- 底部插槽 (例如：用于分页) -->
    <div v-if="!loading && !isEmpty && !error && $slots.footer" class="data-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script>
import {computed} from 'vue'
import {Loading, Warning} from '@element-plus/icons-vue'

export default {
  name: 'DataDisplay',
  components: {
    Loading, // 加载图标组件
    Warning  // 警告图标组件
  },
  props: {
    // 数据加载状态
    loading: {
      type: Boolean,
      default: false
    },
    // 传入的数据 (数组或对象)
    data: {
      type: [Array, Object],
      default: () => []
    },
    // 错误信息 (字符串或对象)
    error: {
      type: [String, Object],
      default: null
    },

    // 加载状态相关配置
    loadingText: {
      type: String,
      default: '正在加载数据...' // 加载时显示的文本
    },
    skeletonRows: {
      type: Number,
      default: 0 // 骨架屏行数，0 表示使用加载图标，大于0表示使用骨架屏
    },

    // 空数据状态相关配置
    emptyText: {
      type: String,
      default: '暂无数据' // 空状态时显示的描述文字
    },
    emptyImage: {
      type: String,
      default: '' // 空状态时显示的图片 URL
    },
    emptyImageSize: {
      type: Number,
      default: 100 // 空状态图片尺寸
    },
    showEmptyButton: {
      type: Boolean,
      default: false // 是否显示空状态下的操作按钮
    },
    emptyButtonText: {
      type: String,
      default: '添加数据' // 空状态按钮文本
    },

    // 错误状态相关配置
    errorTitle: {
      type: String,
      default: '加载失败' // 错误状态标题
    },
    showRetryButton: {
      type: Boolean,
      default: true // 是否显示错误状态下的重试按钮
    },
    retryButtonText: {
      type: String,
      default: '重试' // 重试按钮文本
    }
  },
  emits: ['empty-action', 'retry'], // 定义组件可能触发的事件
  setup(props) {
    // 计算数据是否为空
    const isEmpty = computed(() => {
      if (Array.isArray(props.data)) {
        return props.data.length === 0;
      } else if (props.data && typeof props.data === 'object') {
        return Object.keys(props.data).length === 0;
      }
      return !props.data;
    });

    return {
      isEmpty
    };
  }
}
</script>

<style scoped>
.data-display {
  position: relative;
  width: 100%;
}

.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.loading-icon {
  color: #409EFF;
  animation: rotate 1.5s linear infinite; /* 旋转动画 */
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: 16px;
  color: #909399;
  font-size: 14px;
}

.error-icon {
  color: #F56C6C;
  margin-bottom: 16px;
}

.error-title {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

.error-text {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
  text-align: center;
}

.data-footer {
  margin-top: 20px;
}
</style> 