<template>
  <div class="create-post-container">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon>
          <ArrowLeft/>
        </el-icon>
        返回论坛
      </el-button>
      <h2>发布新帖子</h2>
    </div>

    <div class="form-container">
      <el-form
          ref="formRef"
          :model="postForm"
          :rules="rules"
          label-position="top"
          @submit.prevent="submitForm"
      >
        <el-form-item label="标题" prop="title">
          <el-input
              v-model="postForm.title"
              maxlength="100"
              placeholder="请输入帖子标题"
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
                v-for="item in categories"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <rich-text-editor
              v-model="postForm.content"
              :min-height="300"
              placeholder="请输入帖子内容..."
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-tag
              v-for="tag in postForm.tags"
              :key="tag"
              class="tag-item"
              closable
              @close="removeTag(tag)"
          >
            {{ tag }}
          </el-tag>

          <el-input
              v-if="inputTagVisible"
              ref="tagInputRef"
              v-model="inputTagValue"
              class="tag-input"
              size="small"
              @blur="addTag"
              @keyup.enter="addTag"
          />

          <el-button
              v-else
              class="button-new-tag"
              size="small"
              @click="showTagInput"
          >
            <el-icon>
              <Plus/>
            </el-icon>
            添加标签
          </el-button>

          <div class="tag-tip">添加标签，最多5个，每个标签不超过10个字符</div>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="goBack">取消</el-button>
            <el-button
                :loading="submitting"
                type="primary"
                @click="submitForm"
            >
              发布帖子
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import {nextTick, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {ArrowLeft, Plus} from '@element-plus/icons-vue'
import RichTextEditor from '@/views/ui/RichTextEditor.vue'
import {createPost, getForumCategories} from '@/api/post'

export default {
  name: 'CreatePost',
  components: {
    RichTextEditor,
    ArrowLeft,
    Plus
  },
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const tagInputRef = ref(null)

    // 表单数据
    const postForm = reactive({
      title: '',
      forumId: '',
      content: '',
      tags: []
    })

    // 表单验证规则
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

    // 分类数据
    const categories = ref([])
    const submitting = ref(false)

    // 标签相关
    const inputTagVisible = ref(false)
    const inputTagValue = ref('')

    // 获取分类
    const fetchCategories = async () => {
      try {
        const res = await getForumCategories()
        categories.value = res.data || []
      } catch (error) {
        console.error('获取分类失败:', error)
        ElMessage.error('获取分类失败')
      }
    }

    // 提交表单
    const submitForm = async () => {
      if (!formRef.value) return

      await formRef.value.validate(async (valid) => {
        if (!valid) return

        submitting.value = true
        try {
          const tagObjects = postForm.tags.map(tag => ({name: tag}))

          const formData = {
            ...postForm,
            tags: tagObjects
          }

          await createPost(formData)
          ElMessage.success('帖子发布成功')
          router.push('/forum')
        } catch (error) {
          console.error('发布帖子失败:', error)
          ElMessage.error('发布帖子失败')
        } finally {
          submitting.value = false
        }
      })
    }

    // 显示标签输入框
    const showTagInput = () => {
      inputTagVisible.value = true
      nextTick(() => {
        tagInputRef.value.focus()
      })
    }

    // 添加标签
    const addTag = () => {
      if (inputTagValue.value) {
        if (postForm.tags.length >= 5) {
          ElMessage.warning('最多添加5个标签')
          inputTagVisible.value = false
          inputTagValue.value = ''
          return
        }

        if (inputTagValue.value.length > 10) {
          ElMessage.warning('标签长度不能超过10个字符')
          return
        }

        if (!postForm.tags.includes(inputTagValue.value)) {
          postForm.tags.push(inputTagValue.value)
        }
      }

      inputTagVisible.value = false
      inputTagValue.value = ''
    }

    // 移除标签
    const removeTag = (tag) => {
      const index = postForm.tags.indexOf(tag)
      if (index !== -1) {
        postForm.tags.splice(index, 1)
      }
    }

    // 返回论坛
    const goBack = () => {
      router.push('/forum')
    }

    onMounted(() => {
      fetchCategories()
    })

    return {
      formRef,
      postForm,
      rules,
      categories,
      submitting,
      inputTagVisible,
      inputTagValue,
      tagInputRef,

      submitForm,
      goBack,
      showTagInput,
      addTag,
      removeTag
    }
  }
}
</script>

<style scoped>
.create-post-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 0 15px;
}

.form-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 20px;
}

.tag-item {
  margin-right: 10px;
  margin-bottom: 10px;
}

.tag-input {
  width: 100px;
  margin-right: 10px;
  vertical-align: bottom;
}

.button-new-tag {
  margin-bottom: 10px;
}

.tag-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .create-post-container {
    padding: 15px;
  }

  .form-container {
    padding: 20px;
  }
}
</style> 