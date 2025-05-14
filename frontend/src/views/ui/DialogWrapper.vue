<template>
  <el-dialog
      v-model="dialogVisible"
      :append-to-body="true"
      :close-on-click-modal="false"
      :lock-scroll="true"
      :modal-append-to-body="false"
      :show-close="true"
      destroy-on-close
      v-bind="$attrs"
      @close="handleClose"
  >
    <div class="dialog-content-wrapper">
      <slot></slot>
    </div>
    <template v-if="$slots.footer" #footer>
      <slot name="footer"></slot>
    </template>
  </el-dialog>
</template>

<script setup>
import {ref, watch} from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:visible', 'close']);

const dialogVisible = ref(props.visible);

// 监听外部visible变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal;
});

// 监听内部dialogVisible变化
watch(dialogVisible, (newVal) => {
  if (newVal !== props.visible) {
    emit('update:visible', newVal);
  }
});

// 处理关闭事件
const handleClose = () => {
  emit('close');
};
</script>

<style scoped>
.dialog-content-wrapper {
  position: relative;
  z-index: 10;
  max-height: 70vh;
  overflow-y: auto;
}

:deep(.el-dialog) {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

:deep(.el-dialog__header) {
  padding-bottom: 15px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  margin-bottom: 15px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 15px;
  margin-top: 15px;
}

:deep(.el-dialog__body) {
  padding: 20px 25px;
}

/* 防止弹窗闪烁 */
:deep(.el-overlay) {
  position: fixed;
  z-index: 2000;
}

:deep(.el-dialog) {
  position: relative;
  z-index: 2001;
}
</style> 