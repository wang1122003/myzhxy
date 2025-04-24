<template>
  <el-table
      :data="tableData"
      :row-key="rowKey"
      v-bind="$attrs"
      @selection-change="handleSelectionChange"
  >
    <slot/>
    <template #empty>
      <div class="flex justify-center py-4 text-gray-400">
        暂无数据
      </div>
    </template>
  </el-table>
</template>

<script setup>
const props = defineProps({
  tableData: {
    type: Array,
    default: () => []
  },
  rowKey: {
    type: String,
    default: 'id'
  }
});

const emit = defineEmits(['selection-change']);

const handleSelectionChange = (val) => {
  emit('selection-change', val);
};
</script>