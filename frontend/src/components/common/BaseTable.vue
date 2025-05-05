<template>
  <!-- 基础表格容器 -->
  <div class="base-table-container">
    <!-- Element Plus 表格组件 -->
    <el-table
        :ref="tableId"
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
      <!-- 默认插槽，用于插入表格列 (el-table-column) -->
      <slot></slot>
      <!-- 空数据状态插槽 -->
      <template #empty>
        <!-- 允许父组件通过 "empty" 插槽自定义空状态内容 -->
        <slot name="empty">
          <!-- 默认空状态提示 -->
          <div class="empty-data">没有数据</div>
        </slot>
      </template>
    </el-table>
    <!-- 分页器容器，当 isPagination 为 true 时显示 -->
    <div v-if="isPagination" class="pagination-container">
      <!-- Element Plus 分页组件 -->
      <el-pagination
          :current-page="currentPage"
          :layout="paginationLayout"
          :page-size="pageSize"
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

// 定义组件的 props
const props = defineProps({
  // 表格数据
  tableData: {
    type: Array,
    default: () => []
  },
  // 是否显示纵向边框
  border: {
    type: Boolean,
    default: true
  },
  // 是否为斑马纹 table
  stripe: {
    type: Boolean,
    default: true
  },
  // 是否显示加载中动画
  loading: {
    type: Boolean,
    default: false
  },
  // 表格的尺寸
  size: {
    type: String,
    default: 'default',
    validator: (value) => {
      // 校验器，确保 size 值是有效的
      return ['large', 'default', 'small', ''].includes(value); // 允许空字符串 (等同于 default)
    }
  },
  // Table 的高度，单位 px，设置后将固定表头
  height: {
    type: [String, Number],
    default: null
  },
  // Table 的最大高度，单位 px
  maxHeight: {
    type: [String, Number],
    default: null
  },
  // 是否显示表头
  showHeader: {
    type: Boolean,
    default: true
  },
  // 行数据的 Key
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  // 渲染树形数据时的配置项
  treeProps: {
    type: Object,
    default: () => ({
      children: 'children',
      hasChildren: 'hasChildren'
    })
  },
  // 是否要高亮当前行
  highlightCurrentRow: {
    type: Boolean,
    default: false
  },
  // 是否显示分页
  isPagination: {
    type: Boolean,
    default: false
  },
  // 当前页数 (用于分页)
  currentPage: {
    type: Number,
    default: 1
  },
  // 每页显示条目个数 (用于分页)
  pageSize: {
    type: Number,
    default: 10
  },
  // 每页显示个数选择器的选项设置 (用于分页)
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  // 分页组件布局，子组件名用逗号分隔
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  // 总条目数 (用于分页)
  total: {
    type: Number,
    default: 0
  }
});

// 定义组件的 emits
const emit = defineEmits([
  'row-click',          // 行点击事件
  'selection-change',   // 多选选中项改变事件
  'sort-change',        // 排序改变事件
  'size-change',        // 分页大小改变事件
  'current-change',     // 分页页码改变事件
  'update:current-page', // 用于 v-model:current-page
  'update:page-size'     // 用于 v-model:page-size
]);

// 表格实例引用
const tableId = ref(`el-table-${Date.now()}-${Math.random().toString(36).substring(2, 8)}`);

// --- 表格事件处理函数 --- 

/**
 * 处理行点击事件
 * @param {object} row 当前行数据
 * @param {object} column 当前列配置
 * @param {Event} event DOM 事件对象
 */
const onRowClick = (row, column, event) => {
  emit('row-click', row, column, event);
};

/**
 * 处理多选框选中状态改变事件
 * @param {Array} selection 当前选中的行数据数组
 */
const onSelectionChange = (selection) => {
  emit('selection-change', selection);
};

/**
 * 处理排序条件改变事件
 * @param {object} column 包含 prop, order, column 的对象
 */
const onSortChange = (column) => {
  emit('sort-change', column);
};

// --- 分页事件处理函数 --- 

/**
 * 处理每页显示条数改变事件
 * @param {number} val 新的 pageSize
 */
const onSizeChange = (val) => {
  emit('size-change', val);
};

/**
 * 处理当前页码改变事件
 * @param {number} val 新的 currentPage
 */
const onCurrentChange = (val) => {
  emit('current-change', val);
};

// --- 生命周期钩子 ---
onMounted(() => {
  // 可以在组件挂载后执行一些初始化逻辑
});

</script>

<style scoped>
/* 基础表格容器样式 */
.base-table-container {
  width: 100%; /* 宽度占满父容器 */
}

/* 分页容器样式 */
.pagination-container {
  margin-top: 16px; /* 与表格的间距 */
  display: flex; /* 使用 flex 布局 */
  justify-content: flex-end; /* 内容右对齐 */
}

/* 空数据状态默认样式 */
.empty-data {
  text-align: center; /* 文本居中 */
  padding: 20px 0; /* 上下内边距 */
  color: #909399; /* 字体颜色 */
}
</style> 