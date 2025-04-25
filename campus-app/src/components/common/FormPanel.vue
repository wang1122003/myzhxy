<template>
  <el-card
      :body-style="{ padding: padding }"
      :class="{ 'is-editing': isEditing }"
      class="form-panel"
      shadow="hover"
  >
    <template v-if="title || $slots.header" #header>
      <div class="form-header">
        <div class="form-title">
          <span v-if="title">{{ title }}</span>
          <slot v-else name="header"></slot>
        </div>
        <div v-if="showActions" class="form-actions">
          <slot name="headerActions">
            <el-button
                v-if="!isEditing && !alwaysEditing"
                type="primary"
                @click="handleEdit"
            >
              <el-icon>
                <Edit/>
              </el-icon>
              编辑
            </el-button>
            <template v-if="isEditing || alwaysEditing">
              <el-button @click="handleCancel">取消</el-button>
              <el-button
                  :loading="submitLoading"
                  type="primary"
                  @click="handleSubmit"
              >
                保存
              </el-button>
            </template>
          </slot>
        </div>
      </div>
    </template>

    <div :style="{ maxWidth: maxWidth }" class="form-body">
      <el-form
          ref="formRef"
          :disabled="!isEditing && !alwaysEditing"
          :hide-required-asterisk="hideRequiredAsterisk"
          :label-position="labelPosition || 'right'"
          :label-width="labelWidth || '120px'"
          :model="localModel"
          :require-asterisk-position="requireAsteriskPosition || 'left'"
          :rules="rules"
          :status-icon="statusIcon"
          @submit.prevent="handleSubmit"
      >
        <slot></slot>

        <el-form-item v-if="showFooterActions && (isEditing || alwaysEditing)">
          <slot name="footerActions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button
                :loading="submitLoading"
                type="primary"
                @click="handleSubmit"
            >
              保存
            </el-button>
          </slot>
        </el-form-item>
      </el-form>
    </div>
  </el-card>
</template>

<script>
import {computed, reactive, ref, watchEffect} from 'vue'
import {Edit} from '@element-plus/icons-vue'

export default {
  name: 'FormPanel',
  components: {
    Edit
  },
  props: {
    title: {
      type: String,
      default: ''
    },
    model: {
      type: Object,
      required: true
    },
    rules: {
      type: Object,
      default: () => ({})
    },
    padding: {
      type: String,
      default: '20px'
    },
    maxWidth: {
      type: String,
      default: '800px'
    },
    showActions: {
      type: Boolean,
      default: true
    },
    showFooterActions: {
      type: Boolean,
      default: false
    },
    alwaysEditing: {
      type: Boolean,
      default: false
    },
    defaultEditing: {
      type: Boolean,
      default: false
    },
    submitLoading: {
      type: Boolean,
      default: false
    },
    labelWidth: String,
    labelPosition: String,
    statusIcon: {
      type: Boolean,
      default: true
    },
    requireAsteriskPosition: String,
    hideRequiredAsterisk: {
      type: Boolean,
      default: false
    }
  },
  emits: ['submit', 'cancel', 'edit', 'validate', 'update:model'],
  setup(props, {emit, expose}) {
    const formRef = ref(null)
    const isEditing = ref(props.defaultEditing || props.alwaysEditing)
    const initialValues = ref({})
    const localModel = reactive({})

    watchEffect(() => {
      const newModel = JSON.parse(JSON.stringify(props.model))
      Object.assign(localModel, newModel)
      initialValues.value = newModel
    })

    const isFormChanged = computed(() => {
      const current = JSON.stringify(localModel)
      const initial = JSON.stringify(initialValues.value)
      return current !== initial
    })

    const handleEdit = () => {
      isEditing.value = true
      emit('edit')
    }

    const handleCancel = () => {
      if (isFormChanged.value) {
        Object.keys(localModel).forEach(key => {
          if (initialValues.value[key] !== undefined) {
            localModel[key] = initialValues.value[key]
          } else {
            delete localModel[key]
          }
        })
        Object.keys(initialValues.value).forEach(key => {
          if (localModel[key] === undefined) {
            localModel[key] = initialValues.value[key]
          }
        })
      }

      if (formRef.value) {
        formRef.value.clearValidate()
      }

      if (!props.alwaysEditing) {
        isEditing.value = false
      }

      emit('cancel')
    }

    const handleSubmit = () => {
      if (formRef.value) {
        formRef.value.validate((valid, fields) => {
          if (valid) {
            initialValues.value = JSON.parse(JSON.stringify(localModel))

            if (!props.alwaysEditing) {
              isEditing.value = false
            }

            emit('update:model', {...localModel})
            emit('submit', {...localModel})
          } else {
            emit('validate', fields)
          }
        })
      }
    }

    expose({
      submitForm: handleSubmit,
      cancelForm: handleCancel,
      editForm: handleEdit,
      resetForm: () => {
        if (formRef.value) {
          Object.assign(localModel, initialValues.value)
          formRef.value.clearValidate()
        }
      },
      validateForm: () => {
        if (formRef.value) {
          return formRef.value.validate()
        }
        return Promise.reject('表单未初始化')
      },
      getFormRef: () => formRef.value
    })

    return {
      formRef,
      isEditing,
      localModel,
      handleEdit,
      handleCancel,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.form-panel {
  transition: all 0.3s;
}

.form-panel.is-editing {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-actions, .form-footer-actions {
  display: flex;
  gap: 8px;
}

.form-body {
  margin: 0 auto;
}

@media (max-width: 768px) {
  .form-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .form-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style> 