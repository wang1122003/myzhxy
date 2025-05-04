<template>
  <div class="table-view-container">
    <!-- 工具栏插槽 -->
    <div v-if="$slots.toolbar" class="table-toolbar">
      <slot name="toolbar"></slot>
    </div>

    <el-table
        v-loading="loading"
        :border="border"
        :data="data"
        :height="height"
        :highlight-current-row="highlightCurrentRow"
        :max-height="maxHeight"
        :row-class-name="rowClassName"
        :row-key="rowKey"
        :stripe="stripe"
        style="width: 100%"
        v-bind="$attrs"
        @selection-change="onSelectionChange"
        @row-click="handleRowClick"
        @sort-change="onSortChange"
    >
      <!-- 选择列 -->
      <el-table-column v-if="selectable" align="center" type="selection" width="55"/>

      <!-- 序号列 -->
      <el-table-column v-if="showIndex" align="center" label="序号" type="index" width="70"/>

      <!-- 方式1: 通过columns属性渲染列 -->
      <template v-if="columns">
        <el-table-column
            v-for="column in columns"
            :key="column.prop || column.name || column.label"
            :align="column.align || 'left'"
            :fixed="column.fixed"
            :label="column.label"
            :min-width="column.minWidth || column.min_width"
            :prop="column.prop"
            :class-name="column.className"
            :sortable="column.sortable"
            :width="column.width"
            :show-overflow-tooltip="column.showOverflowTooltip !== false"
        >
          <template v-if="column.slots && column.slots.default" #default="scope">
            <component :is="renderColumn(column, scope)"/>
          </template>
          <template v-else-if="column.formatter" #default="scope">
            {{ column.formatter(scope.row) }}
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column
            v-if="actionColumnConfig"
            :fixed="actionColumnConfig.fixed || 'right'"
            :width="actionColumnConfig.width || 150"
            :align="actionColumnConfig.align || 'center'"
            label="操作"
        >
          <template #default="scope">
            <div class="action-buttons">
              <template v-if="typeof actionColumnConfig.buttons === 'function'">
                <el-button
                    v-for="(button, index) in actionColumnConfig.buttons(scope.row)"
                    :key="index"
                    :icon="button.icon"
                    :link="button.link"
                    :loading="button.loading"
                    :size="button.size || 'small'"
                    :type="button.type || 'primary'"
                    @click.stop="handleButtonClick(button.event, scope.row)"
                >
                  {{ button.label }}
                </el-button>
              </template>
              <template v-else>
                <el-button
                    v-for="(button, index) in actionColumnConfig.buttons"
                    :key="index"
                    :icon="button.icon"
                    :link="button.link"
                    :size="button.size || 'small'"
                    :type="button.type || 'primary'"
                    @click.stop="handleButtonClick(button.event, scope.row)"
                >
                  {{ button.label }}
                </el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </template>

      <!-- 方式2: 通过插槽渲染列 -->
      <slot v-if="!columns"></slot>

      <!-- 空数据插槽 -->
      <template #empty>
        <slot name="empty">
          <div class="empty-state">
            <el-empty :description="emptyText || '暂无数据'" :image-size="120">
              <template #image>
                <slot name="empty-image">
                  <el-icon class="empty-icon">
                    <Collection/>
                  </el-icon>
                </slot>
              </template>
              <template v-if="$slots['empty-action']" #default>
                <slot name="empty-action"></slot>
              </template>
            </el-empty>
          </div>
        </slot>
      </template>
    </el-table>

    <!-- 分页器 -->
    <div v-if="(total > 0 || isPagination) && showPagination" class="pagination-container">
      <el-pagination
          :current-page="currentPage"
          :layout="layout"
          :page-size="pageSize"
          :page-sizes="pageSizes"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          @update:current-page="val => $emit('update:current-page', val)"
          @update:page-size="val => $emit('update:page-size', val)"
      />
    </div>
  </div>
</template>

<script setup>
import {h} from 'vue';
import {Collection} from '@element-plus/icons-vue';

const props = defineProps({
  // 数据源
  data: {
    type: Array,
    required: true
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 列配置（可选，如果不提供则使用插槽方式）
  columns: {
    type: Array,
    default: null
  },
  // 操作列配置
  actionColumnConfig: {
    type: Object,
    default: null
  },
  // 分页总数
  total: {
    type: Number,
    default: 0
  },
  // 当前页码
  currentPage: {
    type: Number,
    default: 1
  },
  // 每页条数
  pageSize: {
    type: Number,
    default: 10
  },
  // 每页条数选项
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  // 分页器布局
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  // 是否显示分页
  showPagination: {
    type: Boolean,
    default: true
  },
  // 空数据文本
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  // 表格属性
  isPagination: {
    type: Boolean,
    default: false
  },
  border: {
    type: Boolean,
    default: true
  },
  stripe: {
    type: Boolean,
    default: true
  },
  height: {
    type: [String, Number],
    default: null
  },
  maxHeight: {
    type: [String, Number],
    default: null
  },
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  rowClassName: {
    type: [String, Function],
    default: null
  },
  highlightCurrentRow: {
    type: Boolean,
    default: false
  },
  // 扩展功能
  selectable: {
    type: Boolean,
    default: false
  },
  showIndex: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits([
  'update:current-page',
  'update:page-size',
  'refresh',
  'selection-change',
  'view-detail',
  'edit',
  'delete',
  'row-click',
  'sort-change',
  'select-course',
  'drop-course',
  'join-activity',
  'cancel-join'
]);

// 渲染自定义列
const renderColumn = (column, scope) => {
  if (column.slots && column.slots.default) {
    try {
      // 使用渲染函数创建列内容
      return column.slots.default(scope);
    } catch (e) {
      console.error('Error rendering column slot:', e);
      return h('span', {}, 'Error');
    }
  }
  return h('span', {}, scope.row[column.prop] || '');
};

// 按钮点击处理
const handleButtonClick = (event, row) => {
  emit(event, row);
};

// 行点击处理
const handleRowClick = (row, column, event) => {
  emit('row-click', row, column, event);
};

// 选择项变更
const onSelectionChange = (selection) => {
  emit('selection-change', selection);
};

// 排序变更
const onSortChange = (column) => {
  emit('sort-change', column);
};

// 分页处理
const handleSizeChange = (size) => {
  emit('update:page-size', size);
  // 不要自动触发刷新，由父组件处理
};

const handleCurrentChange = (current) => {
  emit('update:current-page', current);
  // 不要自动触发刷新，由父组件处理
};

// 暴露方法
defineExpose({
  refresh: () => emit('refresh')
});
</script>

<style scoped>
.table-view-container {
  width: 100%;
}

.table-toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.empty-state {
  padding: 30px 0;
}

.empty-icon {
  font-size: 60px;
  color: #c0c4cc;
}

:deep(.el-table) {
  border-radius: 4px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: bold;
  color: #303133;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafafa;
}

:deep(.el-table__row:hover td) {
  background-color: #f0f7ff !important;
}
</style> 