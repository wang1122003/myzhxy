<template>
  <div :class="{ 'is-card': isCard, 'is-inline': inline }" class="smart-form">
    <!-- 卡片头部 (如果 isCard 为 true 且 title 或 header 插槽存在) -->
    <div v-if="isCard && (title || $slots.header)" class="form-header">
      <div class="form-title">{{ title }}</div>
      <slot name="header"></slot>
    </div>

    <!-- Element Plus 表单 -->
    <el-form
        ref="formRef"
        v-loading="loading"
        :disabled="disabled"
        :inline="inline"
        :label-position="labelPosition"
        :label-width="labelWidth"
        :model="model"
        :rules="rules"
        :scroll-to-error="scrollToError"
        :size="size"
        :validate-on-rule-change="validateOnRuleChange"
        v-bind="$attrs" 
    >
      <!-- 默认插槽，用于放置表单项 -->
      <slot></slot>
    </el-form>

    <!-- 表单底部 (优先使用 footer 插槽) -->
    <div v-if="$slots.footer" class="form-footer">
      <slot name="footer"></slot>
    </div>
    <!-- 默认底部按钮 (如果未提供 footer 插槽且 showButtons 为 true) -->
    <div v-else-if="showButtons" class="form-footer">
      <el-button v-if="showCancel" @click="handleCancel">{{ cancelText }}</el-button>
      <el-button v-if="showReset" @click="handleReset">{{ resetText }}</el-button>
      <el-button :loading="submitting" type="primary" @click="handleSubmit">
        {{ submitText }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue';

const props = defineProps({
  // 表单数据模型
  model: {
    type: Object,
    required: true
  },
  // 表单验证规则
  rules: {
    type: Object,
    default: () => ({})
  },
  // 是否显示加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 是否显示提交中状态 (通常用于按钮)
  submitting: {
    type: Boolean,
    default: false
  },
  // 表单项标签宽度
  labelWidth: {
    type: String,
    default: '100px'
  },
  // 表单项标签位置
  labelPosition: {
    type: String,
    default: 'right'
  },
  // 是否为行内表单
  inline: {
    type: Boolean,
    default: false
  },
  // 是否在 rules 属性改变后立即触发一次验证
  validateOnRuleChange: {
    type: Boolean,
    default: true
  },
  // 表单尺寸
  size: {
    type: String,
    default: 'default' // 可选值: 'large', 'default', 'small'
  },
  // 是否禁用该表单内的所有组件
  disabled: {
    type: Boolean,
    default: false
  },
  // 当校验失败时，滚动到第一个错误表单项
  scrollToError: {
    type: Boolean,
    default: true
  },
  // UI 控制
  // 是否以卡片形式展示
  isCard: {
    type: Boolean,
    default: false
  },
  // 卡片标题 (仅在 isCard 为 true 时有效)
  title: {
    type: String,
    default: ''
  },
  // 按钮控制
  // 是否显示默认的底部按钮区域
  showButtons: {
    type: Boolean,
    default: true
  },
  // 是否显示取消按钮
  showCancel: {
    type: Boolean,
    default: true
  },
  // 是否显示重置按钮
  showReset: {
    type: Boolean,
    default: true
  },
  // 提交按钮文本
  submitText: {
    type: String,
    default: '提交'
  },
  // 取消按钮文本
  cancelText: {
    type: String,
    default: '取消'
  },
  // 重置按钮文本
  resetText: {
    type: String,
    default: '重置'
  }
});

// 定义组件触发的事件
const emit = defineEmits(['submit', 'cancel', 'reset', 'validate']);

// 表单实例的引用
const formRef = ref(null);

// 触发表单验证
const validate = () => {
  if (!formRef.value) return Promise.reject('表单引用不存在');

  return new Promise((resolve, reject) => {
    formRef.value.validate((valid, invalidFields) => {
      if (valid) {
        resolve(true);
      } else {
        reject(invalidFields);
      }
    });
  });
};

// 验证表单的单个字段
const validateField = (prop) => {
  return formRef.value?.validateField(prop);
};

// 处理表单提交
const handleSubmit = async () => {
  try {
    await validate(); // 先进行验证
    emit('submit', props.model); // 验证通过，触发 submit 事件
  } catch (error) {
    emit('validate', error); // 验证失败，触发 validate 事件，传递错误信息
  }
};

// 处理取消操作
const handleCancel = () => {
  emit('cancel');
};

// 处理重置操作
const handleReset = () => {
  resetFields(); // 调用内部重置方法
  emit('reset'); // 触发 reset 事件
};

// 重置该表单项，将其值重置为初始值，并移除校验结果
const resetFields = () => {
  formRef.value?.resetFields();
};

// 清理某个字段或整个表单的验证信息
const clearValidate = (props) => {
  formRef.value?.clearValidate(props);
};

// 滚动到指定的表单字段
const scrollToField = (prop) => {
  formRef.value?.scrollToField(prop);
};

// 使用 defineExpose 暴露组件的方法，允许父组件通过 ref 调用
defineExpose({
  validate,
  validateField,
  resetFields,
  clearValidate,
  scrollToField,
  formRef // 也可暴露 form 实例本身
});
</script>

<style scoped>
.smart-form {
  width: 100%;
}

.smart-form.is-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  padding: 20px;
  box-shadow: var(--el-box-shadow-light);
  background-color: var(--el-bg-color);
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.form-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.form-footer {
  display: flex;
  justify-content: flex-end; /* 按钮默认右对齐 */
  margin-top: 20px;
  gap: 10px; /* 按钮间距 */
}

/* 使用 :deep() 调整 Element Plus 组件内部样式 */
:deep(.el-form-item) {
  margin-bottom: 18px;
}

/* 行内表单特殊处理 */
.smart-form.is-inline :deep(.el-form-item) {
  margin-right: 16px;
}
</style> 