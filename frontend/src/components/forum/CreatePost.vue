<template>
  <div class="create-post-container">
    <el-form
        ref="postFormRef"
        :model="postForm"
        :rules="rules"
        label-width="80px"
    >
      <el-form-item label="标题" prop="title">
        <el-input
            v-model="postForm.title"
            maxlength="100"
            placeholder="请输入帖子标题（2-100个字符）"
            show-word-limit
        />
      </el-form-item>

      <el-form-item label="内容" prop="content">
        <div style="border: 1px solid #dcdfe6; border-radius: 4px;">
          <Toolbar
              :defaultConfig="toolbarConfig"
              :editor="editorRef"
              mode="default"
              style="border-bottom: 1px solid #dcdfe6"
          />
          <Editor
              v-model="postForm.content"
              :defaultConfig="editorConfig"
              mode="default"
              style="height: 350px; overflow-y: hidden;"
              @onCreated="handleEditorCreated"
          />
        </div>
      </el-form-item>

      <el-form-item>
        <el-button :loading="submitting" type="primary" @click="submitForm">发布帖子</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {onBeforeUnmount, onMounted, reactive, ref, shallowRef} from 'vue'
import {ElMessage} from 'element-plus'
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import {createPost} from '@/api/post'

const formRules = {
  required: {required: true, message: '此项为必填项', trigger: 'blur'},
};

export default {
  name: 'CreatePost',
  components: {
    Editor,
    Toolbar
  },
  emits: ['post-created'],
  setup(props, {emit}) {
    const postFormRef = ref(null)
    const postForm = reactive({
      title: '',
      content: '',
      tags: []
    })

    const rules = {
      title: [formRules.required, {min: 2, max: 100, message: '标题长度在2到100个字符之间', trigger: 'blur'}],
      content: [formRules.required, {min: 10, message: '内容不能少于10个字符', trigger: 'blur'}]
    }

    const submitting = ref(false)
    const editorRef = shallowRef()

    const toolbarConfig = {
      excludeKeys: []
    }

    const editorConfig = {
      placeholder: '请输入帖子内容...',
      MENU_CONF: {
        uploadImage: {
          server: '/api/posts/upload/image',
          fieldName: 'file',
          headers: {
            Authorization: `Bearer ${sessionStorage.getItem('token') || ''}`
          },
          maxFileSize: 5 * 1024 * 1024,
          maxNumberOfFiles: 10,
          allowedFileTypes: ['image/*'],
          customInsert(res, insertFn) {
            if (res && res.data && res.data.url) {
              let url = res.data.url;
              insertFn(url, res.data.alt || '', url);
            } else {
              ElMessage.error(res.message || '图片上传失败');
            }
          },
          onError(file, err, res) {
            console.error('图片上传失败:', err, res);
            ElMessage.error(`图片 ${file.name} 上传失败: ${err.message || '服务器错误'}`);
          }
        }
      }
    }

    const handleEditorCreated = (editor) => {
      editorRef.value = editor
    }

    const submitForm = async () => {
      if (!postFormRef.value) return

      await postFormRef.value.validate(async (valid) => {
        if (!valid) return

        if (!editorRef.value) {
          ElMessage.error('编辑器未初始化');
          return;
        }

        const htmlContent = editorRef.value.getHtml() || '';
        const textContent = editorRef.value.getText() || '';

        if (htmlContent === '<p><br></p>' || textContent.length < 10) {
          ElMessage.error('内容不能少于10个字符');
          return;
        }

        submitting.value = true

        try {
          const postData = {
            title: postForm.title,
            content: htmlContent,
            summary: textContent.substring(0, 150),
            tags: []
          }

          const res = await createPost(postData)

          ElMessage.success('发布成功')
          resetForm()
          emit('post-created', res.data)
        } catch (error) {
          console.error('发布失败:', error)
          const errorMsg = error?.response?.data?.message || error?.message || '发布失败，请稍后再试';
          ElMessage.error(errorMsg);
        } finally {
          submitting.value = false
        }
      })
    }

    const resetForm = () => {
      if (postFormRef.value) {
        postFormRef.value.resetFields()
      }
      postForm.title = ''
      postForm.content = ''
      postForm.tags = []

      if (editorRef.value) {
        editorRef.value.setHtml('<p><br></p>')
      }
    }

    onMounted(() => {
    })

    onBeforeUnmount(() => {
      const editor = editorRef.value
      if (editor) {
        editor.destroy()
      }
      editorRef.value = null
    })

    return {
      postFormRef,
      postForm,
      rules,
      submitting,
      editorRef,
      toolbarConfig,
      editorConfig,
      handleEditorCreated,
      submitForm,
      resetForm
    }
  }
}
</script>

<style scoped>
.create-post-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-form-item {
  margin-bottom: 22px;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.w-e-toolbar {
  border-bottom: 1px solid #dcdfe6 !important;
  padding: 5px 0;
}

.w-e-text-container {
  height: 350px !important;
  padding: 10px;
}

.el-select--multiple {
  line-height: normal;
}
</style>