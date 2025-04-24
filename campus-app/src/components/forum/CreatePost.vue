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
            maxlength="50"
            placeholder="请输入帖子标题（2-50个字符）"
            show-word-limit
        />
      </el-form-item>

      <el-form-item label="分类" prop="forumId">
        <el-select
            v-model="postForm.forumId"
            placeholder="请选择分类"
            style="width: 100%"
        >
          <el-option
              v-for="item in forums"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="标签">
        <el-select
            v-model="selectedTags"
            :max="5"
            :max-collapse-tags="3"
            allow-create
            default-first-option
            filterable
            multiple
            placeholder="请选择或输入标签，最多5个"
            style="width: 100%;"
        >
          <el-option
              v-for="tag in availableTags"
              :key="tag.value"
              :label="tag.label"
              :value="tag.value"
          />
        </el-select>
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
import {createPost, getAvailableForums} from '@/api/post'
import request from '@/utils/request'

export default {
  name: 'CreatePost',
  components: {
    Editor,
    Toolbar
  },
  emits: ['post-created'],
  setup(props, {emit}) {
    // 表单数据
    const postFormRef = ref(null)
    const postForm = reactive({
      title: '',
      forumId: '',
      content: '',
      tags: []
    })

    // 表单校验规则
    const rules = {
      title: [
        {required: true, message: '请输入帖子标题', trigger: 'blur'},
        {min: 2, max: 100, message: '标题长度在2到100个字符之间', trigger: 'blur'}
      ],
      forumId: [
        {required: true, message: '请选择帖子分类', trigger: 'change'}
      ],
      content: [
        {required: true, message: '请输入帖子内容', trigger: 'blur'},
        {min: 10, message: '内容不能少于10个字符', trigger: 'blur'}
      ]
    }

    // 论坛板块数据
    const forums = ref([])

    // 标签数据
    const availableTags = ref([])
    const selectedTags = ref([])

    // 提交状态
    const submitting = ref(false)

    // 富文本编辑器配置
    const editorRef = shallowRef()

    const toolbarConfig = {
      excludeKeys: []
    }

    const editorConfig = {
      placeholder: '请输入帖子内容...',
      MENU_CONF: {
        uploadImage: {
          server: '/api/forum/upload/image',
          fieldName: 'file',
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          },
          maxFileSize: 5 * 1024 * 1024, // 最大5MB
          maxNumberOfFiles: 10, // 最大可上传10张图片
          allowedFileTypes: ['image/*'],
          customInsert(res, insertFn) {
            if (res.code === 0 && res.data) {
              let url = res.data
              // 确保URL是完整的
              if (url && !url.startsWith('http')) {
                url = `${window.location.origin}/${url.startsWith('/') ? url.substring(1) : url}`
              }
              insertFn(url, '', url)
            } else {
              ElMessage.error(res.message || '图片上传失败')
            }
          },
          onError(file, err, res) {
            console.error('图片上传失败:', err, res)
            ElMessage.error(`图片 ${file.name} 上传失败: ${err.message}`)
          }
        }
      }
    }

    // 编辑器创建完成的回调
    const handleEditorCreated = (editor) => {
      editorRef.value = editor
    }

    // 获取论坛板块
    const fetchForums = async () => {
      try {
        const res = await getAvailableForums()
        forums.value = res.data || []
      } catch (error) {
        console.error('获取板块失败:', error)
        ElMessage.error('获取板块失败')
      }
    }

    // 获取热门标签
    const fetchPopularTags = async () => {
      try {
        const res = await request.get('/forum/tags/popular')
        if (res && res.data) {
          availableTags.value = res.data.map(tag => ({
            value: tag.id || tag.name,
            label: tag.name
          }))
        }
      } catch (error) {
        console.error('获取热门标签失败:', error)
      }
    }

    // 提交表单
    const submitForm = async () => {
      if (!postFormRef.value) return

      await postFormRef.value.validate(async (valid) => {
        if (!valid) return

        submitting.value = true

        try {
          // 获取富文本内容
          const htmlContent = editorRef.value.getHtml()
          const textContent = editorRef.value.getText()

          // 构造发布数据
          const postData = {
            title: postForm.title,
            forumId: postForm.forumId,
            content: htmlContent,
            summary: textContent.substring(0, 150), // 摘要最多150字
            tags: selectedTags.value.map(tag => ({name: tag}))
          }

          // 发布帖子
          const res = await createPost(postData)

          ElMessage.success('发布成功')
          resetForm()
          emit('post-created', res.data)
        } catch (error) {
          console.error('发布失败:', error)
          ElMessage.error('发布失败: ' + (error.message || '未知错误'))
        } finally {
          submitting.value = false
        }
      })
    }

    // 重置表单
    const resetForm = () => {
      if (postFormRef.value) {
        postFormRef.value.resetFields()
      }

      postForm.title = ''
      postForm.forumId = ''
      postForm.content = ''
      selectedTags.value = []

      if (editorRef.value) {
        editorRef.value.setHtml('')
      }
    }

    // 组件卸载前销毁编辑器实例
    onBeforeUnmount(() => {
      const editor = editorRef.value
      if (editor) {
        editor.destroy()
      }
    })

    // 组件挂载时获取数据
    onMounted(() => {
      fetchForums()
      fetchPopularTags()
    })

    return {
      postFormRef,
      postForm,
      rules,
      forums,
      availableTags,
      selectedTags,
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
}

:deep(.w-e-text-container) {
  height: 350px;
}
</style> 