<template>
  <!-- FormPanel: 一个封装了编辑/查看状态切换和保存/取消逻辑的表单容器 -->
  <el-card
      :body-style="{ padding: padding } as CSSProperties" <!-- 卡片主体样式，设置内边距 -->
  :class="{ 'is-editing': isEditing }"               <!-- 根据编辑状态添加 CSS 类 -->
  class="form-panel"                                <!-- 基础 CSS 类 -->
  shadow="hover"                                    <!-- 鼠标悬浮时显示阴影 -->
  >
  <!-- 卡片头部插槽 (当 title prop 或 header 插槽提供时渲染) -->
    <template v-if="title || $slots.header" #header>
      <!-- 头部内容容器 -->
      <div class="form-header">
        <!-- 标题区域 -->
        <div class="form-title">
          <!-- 优先显示 title prop -->
          <span v-if="title">{{ title }}</span>
          <!-- 否则使用 header 插槽 -->
          <slot v-else name="header"></slot>
        </div>
        <!-- 操作按钮区域 (当 showActions 为 true 时显示) -->
        <div v-if="showActions" class="form-actions">
          <!-- 头部操作按钮插槽，提供默认实现 -->
          <slot name="headerActions">
            <!-- 编辑按钮 (非编辑状态且非总是编辑模式时显示) -->
            <el-button
                v-if="!isEditing && !alwaysEditing"
                type="primary"
                @click="handleEdit" <!-- 点击触发编辑状态切换 -->
            >
            <el-icon>
              <Edit/>
            </el-icon> <!-- 编辑图标 -->
              编辑
            </el-button>
            <!-- 取消和保存按钮 (编辑状态或总是编辑模式时显示) -->
            <template v-if="isEditing || alwaysEditing">
              <!-- 取消按钮 -->
              <el-button @click="handleCancel">取消</el-button>
              <!-- 保存按钮 -->
              <el-button
                  :loading="submitLoading" <!-- 提交加载状态 -->
                  type="primary"
              @click="handleSubmit" <!-- 点击触发表单提交 -->
              >
                保存
              </el-button>
            </template>
          </slot>
        </div>
      </div>
    </template>

  <!-- 表单主体内容区域 -->
    <div :style="{ maxWidth: maxWidth }" class="form-body">
      <!-- Element Plus 表单 -->
      <el-form
          ref="formRef"                                     <!-- 表单实例引用 -->
      :disabled="!isEditing && !alwaysEditing"           <!-- 非编辑状态且非总是编辑模式时禁用表单 -->
      :hide-required-asterisk="hideRequiredAsterisk"   <!-- 是否隐藏必填字段的标签旁边的红色星号 -->
      :label-position="labelPosition || 'right'"       <!-- 表单域标签的位置 -->
      :label-width="labelWidth || '120px'"             <!-- 标签的长度 -->
      :model="localModel"                              <!-- 表单数据对象 (内部维护的响应式对象) -->
      :require-asterisk-position="requireAsteriskPosition || 'left'" <!-- 星号的位置 -->
      :rules="rules"                                   <!-- 表单验证规则 -->
      :status-icon="statusIcon"                        <!-- 是否在输入框中显示校验结果反馈图标 -->
      @submit.prevent="handleSubmit"                  <!-- 阻止默认提交行为，调用内部 handleSubmit -->
      >
      <!-- 默认插槽，用于放置 el-form-item -->
        <slot></slot>

      <!-- 表单底部的操作按钮区域 (当 showFooterActions 且处于编辑状态或总是编辑模式时显示) -->
        <el-form-item v-if="showFooterActions && (isEditing || alwaysEditing)">
          <!-- 底部操作按钮插槽，提供默认实现 -->
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
import {Edit} from '@element-plus/icons-vue' // 引入 Element Plus 编辑图标
import type {CSSProperties} from 'vue'; // 引入 CSS 属性类型

export default {
  name: 'FormPanel', // 组件名称
  components: {
    Edit // 注册 Edit 图标组件
  },
  props: {
    // 面板标题
    title: {
      type: String,
      default: ''
    },
    // 表单数据模型对象 (从父组件传入)
    model: {
      type: Object,
      required: true // 必填
    },
    // 表单验证规则
    rules: {
      type: Object,
      default: () => ({}) // 默认为空对象
    },
    // 卡片主体的内边距
    padding: {
      type: String,
      default: '20px'
    },
    // 表单主体的最大宽度
    maxWidth: {
      type: String,
      default: '800px'
    },
    // 是否显示卡片头部的操作按钮 (编辑/保存/取消)
    showActions: {
      type: Boolean,
      default: true
    },
    // 是否显示表单底部的操作按钮 (保存/取消)
    showFooterActions: {
      type: Boolean,
      default: false
    },
    // 是否始终处于编辑状态 (隐藏编辑按钮，一直显示保存/取消)
    alwaysEditing: {
      type: Boolean,
      default: false
    },
    // 是否默认进入编辑状态
    defaultEditing: {
      type: Boolean,
      default: false
    },
    // 控制保存按钮的加载状态
    submitLoading: {
      type: Boolean,
      default: false
    },
    // 表单域标签的宽度，例如 '50px'。 作为 Form 直接子元素的 form-item 会继承该值。
    labelWidth: String,
    // 表单域标签的位置，如果值为 left 或者 right 时，则需要设置 label-width
    labelPosition: {
      type: String,
      validator: (value) => [
        'left', 'right', 'top', '' // 允许的值
      ].includes(value)
    },
    // 是否在输入框中显示校验结果反馈图标
    statusIcon: {
      type: Boolean,
      default: true
    },
    // 必填字段的标签旁边的红色星号的位置
    requireAsteriskPosition: {
      type: String,
      validator: (value) => [
        'left', 'right', '' // 允许的值
      ].includes(value)
    },
    // 是否隐藏必填字段的标签旁边的红色星号
    hideRequiredAsterisk: {
      type: Boolean,
      default: false
    }
  },
  // 定义组件触发的事件
  emits: ['submit', 'cancel', 'edit', 'validate', 'update:model'],
  setup(props, {emit, expose}) {
    // el-form 实例引用
    const formRef = ref(null)
    // 内部维护的编辑状态，受 defaultEditing 和 alwaysEditing props 初始化影响
    const isEditing = ref(props.defaultEditing || props.alwaysEditing)
    // 存储表单初始值，用于取消编辑时恢复
    const initialValues = ref({})
    // 内部维护的表单数据模型，使用 reactive 包裹以实现双向绑定
    const localModel = reactive({})

    // 使用 watchEffect 监听 props.model 的变化，并更新内部 localModel 和 initialValues
    watchEffect(() => {
      // 深拷贝 props.model，避免直接修改父组件数据
      const newModel = JSON.parse(JSON.stringify(props.model))
      // 将新模型的值合并到 localModel 中
      Object.assign(localModel, newModel)
      // 更新初始值记录
      initialValues.value = newModel
    })

    // 计算属性：判断表单值是否发生改变
    const isFormChanged = computed(() => {
      const current = JSON.stringify(localModel) // 当前表单值的 JSON 字符串
      const initial = JSON.stringify(initialValues.value) // 初始表单值的 JSON 字符串
      return current !== initial // 比较字符串判断是否改变
    })

    // 处理“编辑”按钮点击事件
    const handleEdit = () => {
      isEditing.value = true // 进入编辑状态
      emit('edit') // 触发 edit 事件通知父组件
    }

    // 处理“取消”按钮点击事件
    const handleCancel = () => {
      // 如果表单值已改变
      if (isFormChanged.value) {
        // 恢复 localModel 到 initialValues
        // 遍历当前 localModel 的 key
        Object.keys(localModel).forEach(key => {
          if (initialValues.value[key] !== undefined) {
            // 如果初始值存在，则恢复
            localModel[key] = initialValues.value[key]
          } else {
            // 如果初始值不存在 (说明是编辑时新增的字段)，则删除
            delete localModel[key]
          }
        })
        // 遍历初始值的 key，确保初始值中有的字段都被恢复到 localModel
        Object.keys(initialValues.value).forEach(key => {
          if (localModel[key] === undefined) {
            localModel[key] = initialValues.value[key]
          }
        })
      }

      // 清除表单验证状态
      if (formRef.value) {
        formRef.value.clearValidate()
      }

      // 如果不是 alwaysEditing 模式，退出编辑状态
      if (!props.alwaysEditing) {
        isEditing.value = false
      }

      emit('cancel') // 触发 cancel 事件通知父组件
    }

    // 处理“保存”按钮点击事件 (或表单提交事件)
    const handleSubmit = () => {
      if (formRef.value) {
        // 调用 el-form 的 validate 方法进行验证
        formRef.value.validate((valid, fields) => {
          if (valid) { // 验证通过
            // 更新 initialValues 为当前保存的值，以便下次取消时恢复到这个状态
            initialValues.value = JSON.parse(JSON.stringify(localModel))

            // 如果不是 alwaysEditing 模式，退出编辑状态
            if (!props.alwaysEditing) {
              isEditing.value = false
            }

            // 触发 update:model 事件，用于 v-model:model 的双向绑定
            emit('update:model', {...localModel})
            // 触发 submit 事件，并将当前表单数据传递给父组件
            emit('submit', {...localModel})
          } else { // 验证失败
            // 触发 validate 事件，并将失败的字段信息传递给父组件
            emit('validate', fields)
            // 可以选择在这里给出统一的验证失败提示
            // ElMessage.error('表单校验失败，请检查输入项');
          }
        })
      }
    }

    // 使用 expose 暴露方法给父组件
    expose({
      submitForm: handleSubmit, // 暴露提交方法
      cancelForm: handleCancel, // 暴露取消方法
      editForm: handleEdit,     // 暴露进入编辑状态方法
      // 暴露重置表单方法 (恢复到 initialValues 并清除验证)
      resetForm: () => {
        if (formRef.value) {
          Object.assign(localModel, initialValues.value)
          formRef.value.clearValidate()
        }
      },
      // 暴露触发表单验证的方法
      validateForm: () => {
        if (formRef.value) {
          return formRef.value.validate() // 返回 Promise
        }
        return Promise.reject('表单未初始化')
      },
      // 暴露获取 el-form 实例的方法
      getFormRef: () => formRef.value
    })

    // 返回 setup 函数中需要在模板中使用的数据和方法
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
/* 表单面板基础样式 */
.form-panel {
  transition: all 0.3s; /* 添加过渡效果 */
}

/* 编辑状态下的样式 */
.form-panel.is-editing {
  /* 增加阴影 */
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

/* 表单头部样式 */
.form-header {
  display: flex;
  justify-content: space-between; /* 两端对齐 */
  align-items: center; /* 垂直居中 */
}

/* 标题样式 */
.form-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 操作按钮区域样式 */
.form-actions {
  display: flex;
  gap: 8px; /* 按钮间距 */
}

/* 表单主体样式 */
.form-body {
  margin: 0 auto; /* 水平居中 (结合 maxWidth 生效) */
}

/* 响应式布局：小屏幕下调整头部布局 */
@media (max-width: 768px) {
  .form-header {
    flex-direction: column; /* 垂直排列 */
    align-items: flex-start; /* 左对齐 */
    gap: 12px; /* 元素间距 */
  }

  .form-actions {
    width: 100%; /* 宽度占满 */
    justify-content: flex-end; /* 按钮右对齐 */
  }
}
</style>