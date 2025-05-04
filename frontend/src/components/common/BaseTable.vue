<template>
  <!-- 基础表格容器 -->
  <div class="base-table-container">
    <!-- Element Plus 表格组件 -->
    <el-table
        v-loading="loading"       <!-- 加载状态 -->
    :border="border"           <!-- 是否显示纵向边框 -->
    :data="tableData"         <!-- 表格数据源 -->
    :height="height"           <!-- 表格固定高度 -->
    :highlight-current-row="highlightCurrentRow" <!-- 是否高亮当前行 -->
    :max-height="maxHeight"     <!-- 表格最大高度 -->
    :row-key="rowKey"           <!-- 行数据的 Key，用于优化渲染或特定功能 (如树形表格、展开行) -->
    :show-header="showHeader"   <!-- 是否显示表头 -->
    :size="size"             <!-- 表格尺寸 -->
    :stripe="stripe"           <!-- 是否显示斑马纹 -->
    :tree-props="treeProps"     <!-- 树形表格配置 -->
    @row-click="onRowClick"     <!-- 行点击事件 -->
    @selection-change="onSelectionChange" <!-- 多选框选中状态改变事件 -->
    @sort-change="onSortChange"     <!-- 排序条件改变事件 -->
    :ref="tableId"              <!-- 绑定表格实例引用 (可选，但修复了潜在的 tableId 错误) -->
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
          :current-page="currentPage"  <!-- 当前页码 (通过 prop 接收) -->
      :page-size="pageSize"       <!-- 每页显示条目个数 (通过 prop 接收) -->
      :layout="paginationLayout" <!-- 分页组件布局 -->
      :page-sizes="pageSizes"     <!-- 可选的每页显示条目数 -->
      :total="total"             <!-- 总条目数 -->
      @size-change="onSizeChange"    <!-- pageSize 改变时触发 -->
      @current-change="onCurrentChange" <!-- currentPage 改变时触发 -->
      @update:current-page="val => emit('update:current-page', val)" <!-- 支持 currentPage 的 v-model 双向绑定 -->
      @update:page-size="val => emit('update:page-size', val)"       <!-- 支持 pageSize 的 v-model 双向绑定 -->
      />
    </div>
  </div>
</template>

<script setup>
import {defineEmits, defineProps, onMounted, ref} from 'vue';
import type {ElTable} from 'element-plus'; // 引入 ElTable 类型，用于 tableId 的类型提示 (可选)

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
  // 行数据的 Key，用来优化 Table 的渲染；在使用 reserve-selection 功能与显示树形数据时，该属性是必填的。类型为 String 时，支持多层访问：user.info.id，但不支持 user.info[0].id，此种情况请使用 Function。
  rowKey: {
    type: [String, Function],
    default: 'id' // 默认为 'id'
  },
  // 渲染树形数据时的配置项
  treeProps: {
    type: Object as()
=>
{
  children ? : string;
  hasChildren ? : string
}
, // 类型断言，指定对象结构
    default: () => ({
  children: 'children',       // 指定子节点列表的字段名
  hasChildren: 'hasChildren'  // 指定当前节点是否包含子节点的字段名
    })
  },
// 是否要高亮当前行
{
  Boolean,
    default: false
  },
// 是否显示分页
{
  Boolean,
    default: false
  },
// 当前页数 (用于分页)
{
  Number,
    default: 1
  },
// 每页显示条目个数 (用于分页)
{
  Number,
    default: 10
  },
// 每页显示个数选择器的选项设置 (用于分页)
{
  Array,
    default: () => [10, 20, 50, 100]
  },
// 分页组件布局，子组件名用逗号分隔
{
  String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
// 总条目数 (用于分页)
{
  Number,
    default: 0
  }
})

// 定义组件的 emits
const emit = defineEmits([
  'row-click',          // 行点击事件
  'selection-change', // 多选选中项改变事件
  'sort-change',        // 排序改变事件
  'size-change',        // 分页大小改变事件
  'current-change',     // 分页页码改变事件
  'update:current-page', // 用于 v-model:current-page
  'update:page-size'      // 用于 v-model:page-size
]);

// 表格实例引用 (可选，但修复了潜在的 tableId 读取错误)
// 也可以使用 const tableRef = ref<InstanceType<typeof ElTable> | null>(null);
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
  // 注意: 如果使用了 v-model:page-size，这里不需要手动 emit 'update:page-size'
};

/**
 * 处理当前页码改变事件
 * @param {number} val 新的 currentPage
 */
const onCurrentChange = (val) => {
  emit('current-change', val);
  // 注意: 如果使用了 v-model:current-page，这里不需要手动 emit 'update:current-page'
};

// --- 生命周期钩子 ---
onMounted(() => {
  // 可以在组件挂载后执行一些初始化逻辑，例如获取数据等
  // 但通常数据获取的逻辑会在父组件中处理
});

// 可以暴露一些方法给父组件调用 (如果需要)
// defineExpose({ ... });

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