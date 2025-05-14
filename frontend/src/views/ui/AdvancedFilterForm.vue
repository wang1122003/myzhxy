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
    default: '查询' // 按钮文字：查询
  },
  resetButtonText: {
    type: String,
    default: '重置' // 按钮文字：重置
  },
  showReset: {
    type: Boolean,
    default: true // 是否显示重置按钮
  },
  collapsible: {
    type: Boolean,
    default: false // 是否可折叠
  },
  defaultExpanded: {
    type: Boolean,
    default: false // 默认是否展开（如果可折叠）
  },
  showTags: {
    type: Boolean,
    default: false // 是否显示已选过滤条件的标签
  },
  // 字段标签映射（用于显示过滤标签的名称）
  fieldLabels: {
    type: Object,
    default: () => ({})
  },
  // 字段格式化函数（用于格式化标签中显示的值）
  fieldFormatters: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['search', 'reset', 'filter-change', 'tag-removed', 'clear-filters']);

const expanded = ref(props.defaultExpanded); // 控制展开/收起的状态

// 计算活跃的过滤条件标签
const filterTags = computed(() => {
  if (!props.showTags) return [];

  return Object.entries(props.model)
      .filter(([key, value]) => {
        // 过滤掉空值或未定义的值
        if (value === '' || value === null || value === undefined) return false;
        // 如果是数组，检查是否为空数组
        if (Array.isArray(value) && value.length === 0) return false;
        return true;
      })
      .map(([key, value]) => {
        const label = props.fieldLabels[key] || key; // 获取字段的显示标签，默认为字段名
        let displayValue = value;

        // 应用格式化函数（如果提供了）
        if (props.fieldFormatters[key]) {
          displayValue = props.fieldFormatters[key](value);
        } else if (Array.isArray(value)) {
          displayValue = value.join(', '); // 数组值用逗号连接
        } else if (typeof value === 'boolean') {
          displayValue = value ? '是' : '否'; // 布尔值显示为 是/否
        }

        return {
          key,
          label,
          value: displayValue,
          originalValue: value
        };
      });
});

// 切换展开/收起状态
const toggleExpand = () => {
  expanded.value = !expanded.value;
};

// 处理搜索事件
const handleSearch = () => {
  emit('search', props.model);
};

// 处理重置事件
const handleReset = () => {
  // 重置表单模型中的每个字段
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
  emit('filter-change', props.model); // 触发过滤器变更事件
};

// 处理移除单个过滤标签
const handleRemoveTag = (tag) => {
  const {key} = tag;
  const value = props.model[key];

  // 重置对应字段的值
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
  emit('filter-change', props.model); // 触发过滤器变更事件
};

// 处理清除所有过滤标签
const handleClearAllTags = () => {
  handleReset(); // 调用重置方法
  emit('clear-filters'); // 触发清除所有过滤器事件
};

// 监听过滤器模型变化，以便在外部更改时也能同步标签显示
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
  gap: 10px; /* 可视过滤器之间的间隙 */
}

.form-buttons {
  display: flex;
  gap: 8px; /* 按钮之间的间隙 */
  align-items: center;
  margin-left: auto; /* 按钮组推向右侧 */
}

.advanced-filters {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--el-border-color-lighter); /* 高级过滤器区域的上边框 */
}

.filter-tags {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px; /* 过滤标签之间的间隙 */
  align-items: center;
}

.filter-tag {
  display: flex;
  align-items: center;
}

.tag-label {
  font-weight: bold;
  margin-right: 4px; /* 标签文本与值之间的间隙 */
}

/* 响应式处理：小屏幕下垂直排列 */
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