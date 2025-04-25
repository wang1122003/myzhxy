<template>
  <div class="data-display">
    <!-- 加载状态 -->
    <el-skeleton
        v-if="loading && skeletonRows > 0"
        :rows="skeletonRows"
        :throttle="500"
        animated
    />

    <!-- 加载图标 -->
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

    <!-- 数据内容 -->
    <div v-else class="data-content">
      <slot></slot>
    </div>

    <!-- 底部插槽（例如：分页） -->
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
    Loading,
    Warning
  },
  props: {
    // 数据状态
    loading: {
      type: Boolean,
      default: false
    },
    data: {
      type: [Array, Object],
      default: () => []
    },
    error: {
      type: [String, Object],
      default: null
    },

    // 加载状态配置
    loadingText: {
      type: String,
      default: '正在加载数据...'
    },
    skeletonRows: {
      type: Number,
      default: 0 // 0 表示使用加载图标，>0 表示使用骨架屏
    },

    // 空数据状态配置
    emptyText: {
      type: String,
      default: '暂无数据'
    },
    emptyImage: {
      type: String,
      default: ''
    },
    emptyImageSize: {
      type: Number,
      default: 100
    },
    showEmptyButton: {
      type: Boolean,
      default: false
    },
    emptyButtonText: {
      type: String,
      default: '添加数据'
    },

    // 错误状态配置
    errorTitle: {
      type: String,
      default: '加载失败'
    },
    showRetryButton: {
      type: Boolean,
      default: true
    },
    retryButtonText: {
      type: String,
      default: '重试'
    }
  },
  emits: ['empty-action', 'retry'],
  setup(props) {
    // 判断数据是否为空
    const isEmpty = computed(() => {
      if (Array.isArray(props.data)) {
        return props.data.length === 0
      } else if (props.data && typeof props.data === 'object') {
        return Object.keys(props.data).length === 0
      }
      return !props.data
    })

    return {
      isEmpty
    }
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
  animation: rotate 1.5s linear infinite;
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