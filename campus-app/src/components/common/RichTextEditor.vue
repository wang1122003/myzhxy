<template>
  <div class="rich-text-editor">
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
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  },
  minHeight: {
    type: Number,
    default: 200
  }
});

const emit = defineEmits(['update:modelValue']);

const content = ref(props.modelValue);

// 监听 modelValue 的变化
watch(() => props.modelValue, (newVal) => {
  if (newVal !== content.value) {
    content.value = newVal;
  }
});

// 监听内容变化
watch(content, (newVal) => {
  emit('update:modelValue', newVal);
});
</script>

<style scoped>
.rich-text-editor {
  width: 100%;
}

.rich-text-editor :deep(.el-textarea__inner) {
  min-height: v-bind('`${props.minHeight}px`');
  resize: vertical;
}
</style> 