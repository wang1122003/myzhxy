<template>
  <div class="base-table-container">
    <el-table
        v-loading="loading"
        :border="border"
        :data="tableData"
        :height="height"
        :highlight-current-row="highlightCurrentRow"
        :max-height="maxHeight"
        :row-key="rowKey"
        :show-header="showHeader"
        :size="size"
        :stripe="stripe"
        :tree-props="treeProps"
        @row-click="onRowClick"
        @selection-change="onSelectionChange"
        @sort-change="onSortChange"
    >
      <slot></slot>
      <template #empty>
        <slot name="empty">
          <div class="empty-data">没有数据</div>
        </slot>
      </template>
    </el-table>
    <div v-if="isPagination" class="pagination-container">
      <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :layout="paginationLayout"
          :page-sizes="pageSizes"
          :total="total"
          @size-change="onSizeChange"
          @current-change="onCurrentChange"
          @update:current-page="val => emit('update:current-page', val)"
          @update:page-size="val => emit('update:page-size', val)"
      />
    </div>
  </div>
</template>

<script setup>
import {defineEmits, defineProps, onMounted, ref} from 'vue';

const props = defineProps({
  tableData: {
    type: Array,
    default: () => []
  },
  border: {
    type: Boolean,
    default: true
  },
  stripe: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default'
  },
  height: {
    type: [String, Number],
    default: null
  },
  maxHeight: {
    type: [String, Number],
    default: null
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  treeProps: {
    type: Object,
    default: () => ({
      children: 'children',
      hasChildren: 'hasChildren'
    })
  },
  highlightCurrentRow: {
    type: Boolean,
    default: false
  },
  isPagination: {
    type: Boolean,
    default: false
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  total: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits([
  'row-click',
  'selection-change',
  'sort-change',
  'size-change',
  'current-change',
  'update:current-page',
  'update:page-size'
]);

// 表格ID，解决Cannot read properties of null (reading 'tableId')错误
const tableId = ref(`table-${Date.now()}`);

// 表格事件处理
const onRowClick = (row, column, event) => {
  emit('row-click', row, column, event);
};

const onSelectionChange = (selection) => {
  emit('selection-change', selection);
};

const onSortChange = (column) => {
  emit('sort-change', column);
};

// 分页事件处理
const onSizeChange = (val) => {
  emit('size-change', val);
};

const onCurrentChange = (val) => {
  emit('current-change', val);
};

onMounted(() => {
  // 可以在此处添加初始化逻辑
});
</script>

<style scoped>
.base-table-container {
  width: 100%;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.empty-data {
  text-align: center;
  padding: 20px 0;
  color: #909399;
}
</style> 