<template>
  <el-card :body-style="{ padding: 0 }" class="stats-card" shadow="hover">
    <div :style="{ background: gradient ? `linear-gradient(to right, ${color}, ${secondaryColor})` : 'none' }"
         class="stats-card-content">
      <div :class="{ 'light-text': gradient }" class="stats-info">
        <div class="stats-title">{{ title }}</div>
        <div class="stats-value">
          <count-to
              v-if="animation"
              :decimal-places="2"
              :duration="2000"
              :end-val="value"
              :start-val="0"
              separator=",">
          </count-to>
          <template v-else>{{ formatValue(value) }}</template>
          <span v-if="suffix" class="stats-suffix">{{ suffix }}</span>
        </div>
        <div v-if="subTitle" class="stats-subtitle">{{ subTitle }}</div>
      </div>
      <div :style="{ color: gradient ? '#ffffff' : color }" class="stats-icon">
        <el-icon :size="iconSize || 32">
          <component :is="icon"/>
        </el-icon>
      </div>
    </div>
    <div v-if="showFooter" class="stats-footer">
      <div :class="{ 'up': trend > 0, 'down': trend < 0, 'light-text': gradient }"
           class="stats-trend">
        <el-icon>
          <component :is="trend > 0 ? 'ArrowUp' : (trend < 0 ? 'ArrowDown' : 'ArrowRight')"></component>
        </el-icon>
        <span>{{ Math.abs(trend) }}% {{ trend > 0 ? '增长' : (trend < 0 ? '下降' : '持平') }}</span>
      </div>
      <div class="stats-compare">{{ compareText }}</div>
    </div>
  </el-card>
</template>

<script>
import {defineComponent} from 'vue'
import {ArrowDown, ArrowRight, ArrowUp} from '@element-plus/icons-vue'

// 创建一个简易的数字动画组件
const CountTo = {
  name: 'CountTo',
  props: {
    startVal: {
      type: Number,
      default: 0
    },
    endVal: {
      type: Number,
      required: true
    },
    duration: {
      type: Number,
      default: 2000
    },
    decimalPlaces: {
      type: Number,
      default: 0
    },
    separator: {
      type: String,
      default: ','
    }
  },
  data() {
    return {
      displayValue: this.startVal,
      animationFrameId: null
    }
  },
  mounted() {
    this.start()
  },
  beforeUnmount() {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId)
    }
  },
  methods: {
    start() {
      const startTime = performance.now()
      const startVal = this.startVal
      const endVal = this.endVal
      const duration = this.duration

      const animateCount = (timestamp) => {
        const progress = Math.min((timestamp - startTime) / duration, 1)
        this.displayValue = startVal + (endVal - startVal) * this.easeOutQuad(progress)

        if (progress < 1) {
          this.animationFrameId = requestAnimationFrame(animateCount)
        } else {
          this.displayValue = endVal
        }
      }

      this.animationFrameId = requestAnimationFrame(animateCount)
    },
    easeOutQuad(t) {
      return t * (2 - t)
    },
    format(value) {
      value = Number(value).toFixed(this.decimalPlaces)
      if (this.separator) {
        const parts = value.toString().split('.')
        parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, this.separator)
        return parts.join('.')
      }
      return value
    }
  },
  render() {
    return this.format(this.displayValue)
  }
}

export default defineComponent({
  name: 'StatsCard',
  components: {
    CountTo,
    ArrowUp,
    ArrowDown,
    ArrowRight
  },
  props: {
    title: {
      type: String,
      required: true
    },
    value: {
      type: [Number, String],
      required: true
    },
    suffix: {
      type: String,
      default: ''
    },
    subTitle: {
      type: String,
      default: ''
    },
    icon: {
      type: String,
      required: true
    },
    iconSize: {
      type: Number,
      default: 32
    },
    color: {
      type: String,
      default: '#3a7bd5'
    },
    secondaryColor: {
      type: String,
      default: '#00d2ff'
    },
    gradient: {
      type: Boolean,
      default: false
    },
    animation: {
      type: Boolean,
      default: true
    },
    trend: {
      type: Number,
      default: 0
    },
    compareText: {
      type: String,
      default: '较上期'
    },
    showFooter: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    formatValue(val) {
      // 如果是数字，格式化显示
      if (!isNaN(val)) {
        return new Intl.NumberFormat('zh-CN').format(val)
      }
      return val
    }
  }
})
</script>

<style scoped>
.stats-card {
  transition: transform 0.3s, box-shadow 0.3s;
  border-radius: 8px;
  overflow: hidden;
}

.stats-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stats-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  transition: all 0.3s;
}

.stats-info {
  flex: 1;
}

.stats-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
  display: flex;
  align-items: baseline;
}

.stats-suffix {
  font-size: 14px;
  margin-left: 4px;
  font-weight: normal;
}

.stats-subtitle {
  font-size: 12px;
  color: #909399;
}

.stats-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  width: 48px;
  height: 48px;
  margin-left: 16px;
}

.stats-footer {
  display: flex;
  justify-content: space-between;
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-top: 1px solid #ebeef5;
}

.stats-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.stats-trend.up {
  color: #67c23a;
}

.stats-trend.down {
  color: #f56c6c;
}

.stats-compare {
  font-size: 12px;
  color: #909399;
}

.light-text {
  color: #ffffff !important;
}

.light-text .stats-title,
.light-text .stats-value,
.light-text .stats-subtitle,
.light-text .stats-suffix {
  color: #ffffff !important;
}

/* 响应式调整 */
@media (max-width: 576px) {
  .stats-card-content {
    padding: 15px;
  }

  .stats-value {
    font-size: 20px;
  }

  .stats-icon {
    font-size: 24px;
    width: 36px;
    height: 36px;
  }
}
</style>