<template>
  <div class="rich-text-editor">
    <!-- 当前为 Element Plus 文本域的简单封装 -->
    <!-- TODO: 后续可以替换为真正的富文本编辑器实现，如 WangEditor, Tiptap 等 -->
    <el-input
        v-model="content"
        :placeholder="placeholder"
        :rows="minHeight / 20" 
        type="textarea"
    />
  </div>
</template>

<script setup>
import {ref, watch} from 'vue';

const props = defineProps({
  // 双向绑定的内容
  modelValue: {
    type: String,
    default: ''
  },
  // 占位提示符
  placeholder: {
    type: String,
    default: '请输入内容...'
  },
  // 编辑器最小高度 (px)
  minHeight: {
    type: Number,
    default: 200
  }
});

const emit = defineEmits(['update:modelValue']);

const content = ref(props.modelValue);

// 监听外部 props.modelValue 的变化，以更新内部 content
watch(() => props.modelValue, (newVal) => {
  if (newVal !== content.value) {
    content.value = newVal;
  }
});

// 监听内部 content 的变化，以通过 emit 更新外部绑定的 modelValue
watch(content, (newVal) => {
  emit('update:modelValue', newVal);
});
</script>

<style scoped>
.rich-text-editor {
  width: 100%;
}

/* 通过 :deep() 修改 Element Plus 内部组件样式 */
.rich-text-editor :deep(.el-textarea__inner) {
  /* 使用 v-bind 将 props 中的 minHeight 绑定到 CSS */
  min-height: v-bind('`${props.minHeight}px`');
  resize: vertical; /* 允许垂直方向调整大小 */
}

/* 如果未来使用真正的富文本编辑器，可能需要以下包装器样式 */
.editor-wrapper {
  border: 1px solid #ccc;
  z-index: 100; /* 确保编辑器在其他元素之上 */
}
</style> 