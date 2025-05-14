<template>
  <!-- 统计卡片组件 -->
  <el-card :body-style="{ padding: '0px' }" class="stats-card" shadow="hover">
    <!-- 图标区域 -->
    <div :style="{ backgroundColor: cardColor }" class="card-icon-section">
      <el-icon :size="40">
        <component :is="icon"/>
      </el-icon>
    </div>
    <!-- 内容区域 -->
    <div class="card-content">
      <!-- 标题 -->
      <div class="card-title">{{ title }}</div>
      <!-- 数值 -->
      <div :style="{ color: valueColor }" class="card-value">
        <span v-if="prefix" class="value-prefix">{{ prefix }}</span>
        <span class="value-main">{{ animatedValue.toFixed(valuePrecision) }}</span>
        <span v-if="suffix" class="value-suffix">{{ suffix }}</span>
      </div>
      <!-- 描述 -->
      <div v-if="description || $slots.description" class="card-description">
        <slot name="description">
          {{ description }}
        </slot>
      </div>
    </div>
    <!-- 操作区域 -->
    <div v-if="$slots.actions || actions" class="card-actions">
      <slot name="actions">
        <el-button v-for="(action, index) in actions" :key="index" text v-bind="action.props"
                   @click="() => handleActionClick(action.event)">
          {{ action.label }}
        </el-button>
      </slot>
    </div>
  </el-card>
</template>

<script setup>
import {ref, computed, watch, onMounted, onUnmounted} from 'vue';
import {gsap} from 'gsap'; // 引入 GreenSock 动画库

const props = defineProps({
  // 主要内容配置
  icon: {
    type: [String, Object], // 可以是图标组件的名称或组件对象本身
    required: true
  },
  title: {
    type: String,
    required: true // 标题是必需的
  },
  value: {
    type: Number,
    required: true // 数值是必需的
  },
  valuePrecision: {
    type: Number,
    default: 0 // 数值精度 (小数点后位数)
  },
  prefix: {
    type: String,
    default: '' // 数值前缀 (例如: ￥)
  },
  suffix: {
    type: String,
    default: '' // 数值后缀 (例如: %)
  },
  description: {
    type: String,
    default: '' // 描述文本
  },
  // 样式配置
  cardColor: {
    type: String,
    default: 'var(--el-color-primary)' // 图标区域背景色，默认为 Element Plus 主题色
  },
  valueColor: {
    type: String,
    default: 'var(--el-color-primary)' // 数值文本颜色，默认为 Element Plus 主题色
  },
  // 动画配置
  animateValue: {
    type: Boolean,
    default: true // 是否启用数值动画
  },
  animationDuration: {
    type: Number,
    default: 1.5 // 动画持续时间 (秒)
  },
  // 操作按钮配置
  actions: {
    type: Array,
    default: () => [] // 动作按钮数组，格式: [{ label: '按钮文字', event: '事件名', props: { ...按钮属性 } }]
  }
});

// 定义组件触发的事件
const emit = defineEmits(['action']);

// 动画相关状态
const animatedValue = ref(props.animateValue ? 0 : props.value); // 用于动画的目标 ref
let tween = null; // GSAP 动画实例

// 启动数值动画
const startAnimation = () => {
  if (!props.animateValue) {
    animatedValue.value = props.value; // 如果禁用动画，直接设置为目标值
    return;
  }

  if (tween) {
    tween.kill(); // 如果已有动画在运行，先停止它
  }

  // 使用 GSAP 创建从当前值到目标值的动画
  tween = gsap.to(animatedValue, { // GSAP 可以直接对 ref 对象进行动画
    value: props.value,
    duration: props.animationDuration,
    ease: 'power2.out', // 动画缓动效果
    onUpdate: () => {
      // Vue 3 ref 的值通过 .value 访问
      // GSAP 直接修改 ref 的 value 属性，Vue 会自动侦测到变化
    }
  });
};

// 监听 props.value 的变化，当外部传入的值改变时，重新启动动画
watch(() => props.value, (newValue, oldValue) => {
  if (newValue !== oldValue) {
    startAnimation();
  }
}, {immediate: false}); // immediate: false 表示初始挂载时不立即执行

// 组件挂载后执行初始动画
onMounted(() => {
  startAnimation();
});

// 组件卸载前清理动画，防止内存泄漏
onUnmounted(() => {
  if (tween) {
    tween.kill();
  }
});

// 处理操作按钮点击事件
const handleActionClick = (event) => {
  emit('action', event); // 触发 action 事件，并将事件名传递给父组件
};

</script>

<style scoped>
.stats-card {
  display: flex;
  flex-direction: column; /* 垂直布局 */
  border-radius: 8px; /* 圆角 */
  overflow: hidden; /* 隐藏溢出内容 */
  transition: box-shadow 0.3s ease-in-out, transform 0.3s ease-in-out; /* 过渡效果 */
}

.stats-card:hover {
  transform: translateY(-4px); /* 悬停时向上移动 */
  box-shadow: var(--el-box-shadow-dark); /* 悬停时添加阴影 */
}

.card-icon-section {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff; /* 图标颜色 */
  transition: background-color 0.3s; /* 背景色过渡 */
}

.card-content {
  padding: 16px;
  text-align: center; /* 内容居中 */
  flex-grow: 1; /* 占据剩余空间 */
}

.card-title {
  font-size: 15px;
  color: var(--el-text-color-secondary); /* 标题颜色 */
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: bold; /* 加粗 */
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
  align-items: baseline; /* 基线对齐 */
  line-height: 1.2;
  transition: color 0.3s; /* 颜色过渡 */
}

.value-prefix,
.value-suffix {
  font-size: 16px;
  font-weight: normal;
  margin: 0 4px; /* 前后缀与数值的间距 */
}

.card-description {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  min-height: 1.5em; /* 保证至少有一行的高度，避免空描述时布局跳动 */
}

.card-actions {
  padding: 8px 16px;
  border-top: 1px solid var(--el-border-color-lighter); /* 顶部边框 */
  display: flex;
  justify-content: space-around; /* 按钮均匀分布 */
  align-items: center;
}

.card-actions .el-button {
  padding: 4px 8px;
  font-size: 13px;
}

/* 响应式调整：小屏幕下减小间距和字体大小 */
@media (max-width: 768px) {
  .card-icon-section {
    padding: 15px;
  }

  .card-content {
    padding: 12px;
  }

  .card-value {
    font-size: 24px;
  }

  .value-prefix,
  .value-suffix {
    font-size: 14px;
  }

  .card-title,
  .card-description {
    font-size: 13px;
  }
}
</style> 