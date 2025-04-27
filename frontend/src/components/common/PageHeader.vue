<template>
  <div class="page-header">
    <div class="page-header-left">
      <div v-if="showBack" class="back-button">
        <el-button text @click="handleBack">
          <el-icon>
            <Back/>
          </el-icon>
          返回
        </el-button>
      </div>
      <div class="title-container">
        <h2 class="page-title">{{ title }}</h2>
        <el-breadcrumb v-if="breadcrumbs && breadcrumbs.length" separator="/">
          <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index" :to="item.to">
            {{ item.text }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>
    <div v-if="$slots.actions" class="page-header-actions">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script>
import {Back} from '@element-plus/icons-vue'
import {useRouter} from 'vue-router'

export default {
  name: 'PageHeader',
  components: {
    Back
  },
  props: {
    title: {
      type: String,
      required: true
    },
    breadcrumbs: {
      type: Array,
      default: () => []
    },
    showBack: {
      type: Boolean,
      default: false
    },
    backTo: {
      type: [String, Object],
      default: null
    }
  },
  setup(props) {
    const router = useRouter()

    const handleBack = () => {
      if (props.backTo) {
        router.push(props.backTo)
      } else {
        router.back()
      }
    }

    return {
      handleBack
    }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.page-header-left {
  display: flex;
  align-items: center;
}

.back-button {
  margin-right: 16px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.title-container {
  display: flex;
  flex-direction: column;
}

.page-header-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .page-header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style> 