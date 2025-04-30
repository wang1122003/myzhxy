<template>
  <div :class="{ 'is-card': isCard, 'is-inline': inline }" class="smart-form">
    <div v-if="isCard && (title || $slots.header)" class="form-header">
      <div class="form-title">{{ title }}</div>
      <slot name="header"></slot>
    </div>

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
      <slot></slot>
    </el-form>

    <div v-if="$slots.footer" class="form-footer">
      <slot name="footer"></slot>
    </div>
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
import {ref, computed} from 'vue';

const props = defineProps({
  model: {
    type: Object,
    required: true
  },
  rules: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  },
  submitting: {
    type: Boolean,
    default: false
  },
  labelWidth: {
    type: String,
    default: '100px'
  },
  labelPosition: {
    type: String,
    default: 'right'
  },
  inline: {
    type: Boolean,
    default: false
  },
  validateOnRuleChange: {
    type: Boolean,
    default: true
  },
  size: {
    type: String,
    default: 'default'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  scrollToError: {
    type: Boolean,
    default: true
  },
  // UI控制
  isCard: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  // 按钮控制
  showButtons: {
    type: Boolean,
    default: true
  },
  showCancel: {
    type: Boolean,
    default: true
  },
  showReset: {
    type: Boolean,
    default: true
  },
  submitText: {
    type: String,
    default: '提交'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  resetText: {
    type: String,
    default: '重置'
  }
});

const emit = defineEmits(['submit', 'cancel', 'reset', 'validate']);

const formRef = ref(null);

// 表单验证
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

// 验证特定字段
const validateField = (prop) => {
  return formRef.value?.validateField(prop);
};

// 提交表单
const handleSubmit = async () => {
  try {
    await validate();
    emit('submit', props.model);
  } catch (error) {
    emit('validate', error);
  }
};

// 取消表单
const handleCancel = () => {
  emit('cancel');
};

// 重置表单
const handleReset = () => {
  resetFields();
  emit('reset');
};

// 重置表单字段
const resetFields = () => {
  formRef.value?.resetFields();
};

// 清除验证
const clearValidate = (props) => {
  formRef.value?.clearValidate(props);
};

// 滚动到指定字段
const scrollToField = (prop) => {
  formRef.value?.scrollToField(prop);
};

// 暴露方法
defineExpose({
  validate,
  validateField,
  resetFields,
  clearValidate,
  scrollToField,
  formRef
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
  justify-content: flex-end;
  margin-top: 20px;
  gap: 10px;
}

/* 表单项间距 */
:deep(.el-form-item) {
  margin-bottom: var(--el-form-item-margin-bottom, 18px);
}

/* 内联表单特殊处理 */
.smart-form.is-inline :deep(.el-form-item) {
  margin-right: 16px;
}
</style> 