<template>
  <div class="table-view-container">
    <!-- 工具栏插槽 -->
    <div v-if="$slots.toolbar" class="table-toolbar">
      <slot name="toolbar"></slot>
    </div>

    <!-- Element Plus 表格 -->
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

      <!-- 渲染方式 1: 通过 columns 属性配置 -->
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
          <!-- 自定义列渲染 (通过 VNode 或函数) -->
          <template v-if="column.slots && column.slots.default" #default="scope">
            <component :is="renderColumn(column, scope)"/>
          </template>
          <!-- 使用 formatter 格式化 -->
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
              <!-- 如果 buttons 是一个返回按钮数组的函数 (动态按钮) -->
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
              <!-- 如果 buttons 是一个固定的按钮数组 -->
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

      <!-- 渲染方式 2: 通过父组件传递插槽 -->
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
  // 表格数据源
  data: {
    type: Array,
    required: true
  },
  // 表格加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 列配置数组 (可选，如果不提供则使用默认插槽方式)
  columns: {
    type: Array,
    default: null
  },
  // 操作列配置对象
  actionColumnConfig: {
    type: Object,
    default: null
  },
  // 分页：总条目数
  total: {
    type: Number,
    default: 0
  },
  // 分页：当前页码 (支持 v-model)
  currentPage: {
    type: Number,
    default: 1
  },
  // 分页：每页显示条目数 (支持 v-model)
  pageSize: {
    type: Number,
    default: 10
  },
  // 分页：每页显示个数选择器的选项设置
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  // 分页：分页组件布局
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  // 是否显示分页组件
  showPagination: {
    type: Boolean,
    default: true
  },
  // 表格为空时显示的文本
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  // 表格属性：是否强制显示分页 (即使 total 为 0)
  isPagination: {
    type: Boolean,
    default: false
  },
  // 表格属性：是否带有纵向边框
  border: {
    type: Boolean,
    default: true
  },
  // 表格属性：是否为斑马纹 table
  stripe: {
    type: Boolean,
    default: true
  },
  // 表格属性：Table 的高度，用于固定表头
  height: {
    type: [String, Number],
    default: null
  },
  // 表格属性：Table 的最大高度
  maxHeight: {
    type: [String, Number],
    default: null
  },
  // 表格属性：行数据的 Key，用来优化 Table 的渲染
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  // 表格属性：行的 className 的回调方法
  rowClassName: {
    type: [String, Function],
    default: null
  },
  // 表格属性：是否要高亮当前行
  highlightCurrentRow: {
    type: Boolean,
    default: false
  },
  // 扩展功能：是否显示多选列
  selectable: {
    type: Boolean,
    default: false
  },
  // 扩展功能：是否显示序号列
  showIndex: {
    type: Boolean,
    default: false
  }
});

// 定义组件可以触发的事件
const emit = defineEmits([
  'update:current-page', // 用于 v-model:current-page
  'update:page-size',    // 用于 v-model:page-size
  'refresh',             // 请求刷新数据（通常由分页或工具栏触发）
  'selection-change',    // 多选框选中项改变时触发
  'view-detail',         // 查看详情按钮点击事件 (约定)
  'edit',                // 编辑按钮点击事件 (约定)
  'delete',              // 删除按钮点击事件 (约定)
  'row-click',           // 行点击事件
  'sort-change',         // 排序条件改变时触发
  'select-course',       // 选课按钮点击事件 (特定场景)
  'drop-course',         // 退课按钮点击事件 (特定场景)
  'page-change',         // 分页页码或大小改变时触发 { currentPage, pageSize }
  // ... 可以添加更多自定义事件，并在 handleButtonClick 中处理
]);

/**
 * 渲染列内容的辅助函数 (处理 VNode 或函数类型的 slot 定义)
 * @param {object} column 当前列配置
 * @param {object} scope Element Plus 表格作用域插槽提供的数据 { row, column, $index }
 * @returns VNode | string | null
 */
const renderColumn = (column, scope) => {
  if (typeof column.slots.default === 'function') {
    // 如果 slot 是函数，直接调用它并传入 scope
    return column.slots.default(scope);
  } else if (typeof column.slots.default === 'string') {
    // 如果是字符串，可以简单地将其包装在 span 中返回
    // 注意：更复杂的 VNode 字符串解析需要更专业的库
    return h('span', {}, column.slots.default);
  } else {
    // 默认返回原始值
    return scope.row[column.prop];
  }
};

/**
 * 处理分页大小 (pageSize) 改变事件
 * @param {number} size 新的每页条数
 */
const handleSizeChange = (size) => {
  emit('update:page-size', size);
  emit('update:current-page', 1); // 切换分页大小通常回到第一页
  emit('page-change', {currentPage: 1, pageSize: size});
  emit('refresh'); // 触发刷新事件
};

/**
 * 处理当前页码 (currentPage) 改变事件
 * @param {number} page 新的当前页码
 */
const handleCurrentChange = (page) => {
  emit('update:current-page', page);
  emit('page-change', {currentPage: page, pageSize: props.pageSize});
  emit('refresh'); // 触发刷新事件
};

/**
 * 处理行点击事件
 * @param {object} row 被点击的行数据
 * @param {object} column 被点击的列配置
 * @param {Event} event DOM 事件对象
 */
const handleRowClick = (row, column, event) => {
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
 * @param {object} sortInfo 包含 { column, prop, order } 的对象
 */
const onSortChange = ({column, prop, order}) => {
  emit('sort-change', {column, prop, order});
};

/**
 * 处理操作列按钮点击事件
 * @param {string} eventName 按钮配置中定义的事件名称
 * @param {object} rowData 当前行的数据
 */
const handleButtonClick = (eventName, rowData) => {
  // console.log(`操作按钮点击: 事件='${eventName}', 行数据ID='${rowData[props.rowKey]}'`);
  emit(eventName, rowData); // 触发按钮配置中定义的事件，并传递行数据
};

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
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px; /* 按钮之间的间距 */
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: #c0c4cc;
}
</style> 