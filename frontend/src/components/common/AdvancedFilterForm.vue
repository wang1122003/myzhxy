<template>
  <div :class="{ 'collapsed': !expanded && collapsible }" class="advanced-filter-form">
    <el-form :inline="true" :model="model" @submit.prevent="handleSearch">
      <div class="visible-filters">
        <slot></slot>
        <div class="form-buttons">
          <el-button type="primary" @click="handleSearch">
            <el-icon>
              <Search/>
            </el-icon>
            {{ searchButtonText }}
          </el-button>
          <el-button v-if="showReset" @click="handleReset">
            <el-icon>
              <Refresh/>
            </el-icon>
            {{ resetButtonText }}
          </el-button>
          <el-button v-if="collapsible" type="text" @click="toggleExpand">
            {{ expanded ? '收起' : '展开' }}
            <el-icon>
              <component :is="expanded ? 'ArrowUp' : 'ArrowDown'"/>
            </el-icon>
          </el-button>
        </div>
      </div>

      <div v-if="collapsible" v-show="expanded" class="advanced-filters">
        <slot name="advanced"></slot>
      </div>
    </el-form>

    <div v-if="showTags && filterTags.length > 0" class="filter-tags">
      <el-tag
          v-for="(tag, index) in filterTags"
          :key="index"
          class="filter-tag"
          closable
          @close="handleRemoveTag(tag)"
      >
        <span class="tag-label">{{ tag.label }}:</span> {{ tag.value }}
      </el-tag>
      <el-button v-if="filterTags.length > 0" size="small" type="text" @click="handleClearAllTags">
        清除全部
      </el-button>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, watch} from 'vue';
import {Search, Refresh, ArrowUp, ArrowDown} from '@element-plus/icons-vue';

const props = defineProps({
  model: {
    type: Object,
    required: true
  },
  searchButtonText: {
    type: String,
    default: '查询'
  },
  resetButtonText: {
    type: String,
    default: '重置'
  },
  showReset: {
    type: Boolean,
    default: true
  },
  collapsible: {
    type: Boolean,
    default: false
  },
  defaultExpanded: {
    type: Boolean,
    default: false
  },
  showTags: {
    type: Boolean,
    default: false
  },
  // 字段标签映射（用于显示过滤标签）
  fieldLabels: {
    type: Object,
    default: () => ({})
  },
  // 字段格式化函数（用于格式化标签中的值）
  fieldFormatters: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['search', 'reset', 'filter-change', 'tag-removed', 'clear-filters']);

const expanded = ref(props.defaultExpanded);

// 计算活跃的过滤条件标签
const filterTags = computed(() => {
  if (!props.showTags) return [];

  return Object.entries(props.model)
      .filter(([key, value]) => {
        // 过滤掉空值或未定义的值
        if (value === '' || value === null || value === undefined) return false;
        // 如果是数组，检查是否有值
        if (Array.isArray(value) && value.length === 0) return false;
        return true;
      })
      .map(([key, value]) => {
        const label = props.fieldLabels[key] || key;
        let displayValue = value;

        // 应用格式化器（如果有）
        if (props.fieldFormatters[key]) {
          displayValue = props.fieldFormatters[key](value);
        } else if (Array.isArray(value)) {
          displayValue = value.join(', ');
        } else if (typeof value === 'boolean') {
          displayValue = value ? '是' : '否';
        }

        return {
          key,
          label,
          value: displayValue,
          originalValue: value
        };
      });
});

const toggleExpand = () => {
  expanded.value = !expanded.value;
};

const handleSearch = () => {
  emit('search', props.model);
};

const handleReset = () => {
  // 重置表单
  Object.keys(props.model).forEach(key => {
    const value = props.model[key];
    if (Array.isArray(value)) {
      props.model[key] = [];
    } else if (typeof value === 'boolean') {
      props.model[key] = false;
    } else if (typeof value === 'number') {
      props.model[key] = 0;
    } else {
      props.model[key] = '';
    }
  });
  emit('reset');
  emit('filter-change', props.model);
};

const handleRemoveTag = (tag) => {
  const {key} = tag;
  const value = props.model[key];

  if (Array.isArray(value)) {
    props.model[key] = [];
  } else if (typeof value === 'boolean') {
    props.model[key] = false;
  } else if (typeof value === 'number') {
    props.model[key] = 0;
  } else {
    props.model[key] = '';
  }

  emit('tag-removed', key);
  emit('filter-change', props.model);
};

const handleClearAllTags = () => {
  handleReset();
  emit('clear-filters');
};

// 监听过滤器变化
watch(() => props.model, () => {
  emit('filter-change', props.model);
}, {deep: true});
</script>

<style scoped>
.advanced-filter-form {
  width: 100%;
}

.visible-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.form-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-left: auto;
}

.advanced-filters {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.filter-tags {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.filter-tag {
  display: flex;
  align-items: center;
}

.tag-label {
  font-weight: bold;
  margin-right: 4px;
}

/* 响应式处理 */
@media (max-width: 768px) {
  .visible-filters {
    flex-direction: column;
    align-items: stretch;
  }

  .form-buttons {
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
  }
}
</style> 