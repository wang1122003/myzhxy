<template>
  <div class="data-table">
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
        @row-click="onRowClick"
        @selection-change="onSelectionChange"
    >
      <el-table-column v-if="selectable" align="center" type="selection" width="55"/>
      <el-table-column v-if="showIndex" align="center" label="序号" type="index" width="70"/>
      <slot></slot>
      <template #empty>
        <el-empty :description="emptyText"/>
      </template>
    </el-table>

    <div v-if="total > 0" class="pagination-container">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :layout="layout"
          :page-sizes="pageSizes"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import {ref, watch, computed} from 'vue';

const props = defineProps({
  data: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  total: {
    type: Number,
    default: 0
  },
  modelValue: {
    type: Object,
    default: () => ({
      pageNum: 1,
      pageSize: 10
    })
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  // 表格属性
  border: {
    type: Boolean,
    default: false
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
    default: null
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
  'update:modelValue',
  'page-change',
  'row-click',
  'selection-change'
]);

const currentPage = ref(props.modelValue.pageNum || 1);
const pageSize = ref(props.modelValue.pageSize || 10);

watch(() => props.modelValue, (newVal) => {
  if (newVal.pageNum !== undefined) {
    currentPage.value = newVal.pageNum;
  }
  if (newVal.pageSize !== undefined) {
    pageSize.value = newVal.pageSize;
  }
}, {deep: true});

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  emitPageChange();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  emitPageChange();
};

const emitPageChange = () => {
  const pagination = {
    pageNum: currentPage.value,
    pageSize: pageSize.value
  };
  emit('update:modelValue', pagination);
  emit('page-change', pagination);
};

const onRowClick = (row, column, event) => {
  emit('row-click', row, column, event);
};

const onSelectionChange = (selection) => {
  emit('selection-change', selection);
};

// 暴露方法
defineExpose({
  refresh: emitPageChange
});
</script>

<style scoped>
.data-table {
  width: 100%;
}

.table-toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 支持暗色模式 */
:deep(.el-table) {
  --el-table-border-color: var(--el-border-color-lighter);
}
</style>
 