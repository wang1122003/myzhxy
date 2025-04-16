<template>
  <div class="forum-container">
    <div class="page-header">
      <h2>校园论坛</h2>
      <el-button type="primary" @click="handleCreatePost">发帖</el-button>
    </div>

    <el-row :gutter="20">
      <!-- 左侧分类导航 -->
      <el-col :span="4">
        <el-card class="category-card">
          <el-menu
              :default-active="activeCategory"
              class="category-menu"
              @select="handleCategorySelect"
          >
            <el-menu-item index="all">
              <el-icon>
                <List/>
              </el-icon>
              <span>全部帖子</span>
            </el-menu-item>
            <el-menu-item index="campus">
              <el-icon>
                <House/>
              </el-icon>
              <span>校园生活</span>
            </el-menu-item>
            <el-menu-item index="study">
              <el-icon>
                <Reading/>
              </el-icon>
              <span>学习交流</span>
            </el-menu-item>
            <el-menu-item index="club">
              <el-icon>
                <Star/>
              </el-icon>
              <span>社团活动</span>
            </el-menu-item>
            <el-menu-item index="career">
              <el-icon>
                <Briefcase/>
              </el-icon>
              <span>求职就业</span>
            </el-menu-item>
            <el-menu-item index="trade">
              <el-icon>
                <Goods/>
              </el-icon>
              <span>二手交易</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧帖子列表 -->
      <el-col :span="20">
        <el-card class="post-list-card">
          <el-table :data="posts" style="width: 100%">
            <el-table-column label="标题" prop="title">
              <template #default="scope">
                <el-link type="primary" @click="viewPost(scope.row)">{{ scope.row.title }}</el-link>
              </template>
            </el-table-column>
            <el-table-column label="作者" prop="author" width="120"/>
            <el-table-column label="分类" prop="category" width="100">
              <template #default="scope">
                <el-tag :type="getCategoryTag(scope.row.category)">
                  {{ getCategoryName(scope.row.category) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="发布时间" prop="createTime" width="180"/>
            <el-table-column label="浏览" prop="viewCount" width="80"/>
            <el-table-column label="评论" prop="commentCount" width="80"/>
          </el-table>

          <div class="pagination-container">
            <el-pagination
                :current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                background
                layout="prev, pager, next"
                @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发帖对话框 -->
    <el-dialog
        v-model="postDialogVisible"
        :title="'发帖'"
        width="700px"
    >
      <el-form ref="postFormRef" :model="postForm" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title"/>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="postForm.category" style="width: 100%">
            <el-option label="校园生活" value="campus"/>
            <el-option label="学习交流" value="study"/>
            <el-option label="社团活动" value="club"/>
            <el-option label="求职就业" value="career"/>
            <el-option label="二手交易" value="trade"/>
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
              v-model="postForm.content"
              :rows="10"
              placeholder="请输入帖子内容"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
              :file-list="postForm.attachments"
              :on-remove="handleUploadRemove"
              :on-success="handleUploadSuccess"
              action="/api/upload"
              class="upload-demo"
              multiple
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持任意格式文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="postDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">发布</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 帖子详情对话框 -->
    <el-dialog
        v-model="postDetailVisible"
        :title="currentPost.title"
        width="70%"
    >
      <div class="post-detail">
        <div class="post-info">
          <span>作者：{{ currentPost.author }}</span>
          <span>发布时间：{{ currentPost.createTime }}</span>
          <span>浏览：{{ currentPost.viewCount }}</span>
          <span>评论：{{ currentPost.commentCount }}</span>
        </div>
        <div class="post-content">{{ currentPost.content }}</div>
        <div v-if="currentPost.attachments && currentPost.attachments.length" class="post-attachments">
          <h4>附件：</h4>
          <ul>
            <li v-for="file in currentPost.attachments" :key="file.id">
              <el-link :href="file.url" target="_blank">{{ file.name }}</el-link>
            </li>
          </ul>
        </div>
        <div class="comment-section">
          <h4>评论（{{ currentPost.commentCount }}）</h4>
          <div class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <span class="comment-author">{{ comment.author }}</span>
                <span class="comment-time">{{ comment.createTime }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
            </div>
          </div>
          <div class="comment-form">
            <el-input
                v-model="commentContent"
                :rows="3"
                placeholder="请输入评论内容"
                type="textarea"
            />
            <el-button style="margin-top: 10px" type="primary" @click="submitComment">发表评论</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Briefcase, Goods, House, List, Reading, Star} from '@element-plus/icons-vue'
import {addComment, addPost, getAllPosts, getPostById, getPostComments, incrementPostViews} from '@/api/forum'

export default {
  name: 'Forum',
  components: {
    List,
    House,
    Reading,
    Star,
    Briefcase,
    Goods
  },
  setup() {
    const posts = ref([])
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const activeCategory = ref('all')
    const postDialogVisible = ref(false)
    const postDetailVisible = ref(false)
    const postFormRef = ref(null)
    const postForm = reactive({
      title: '',
      category: '',
      content: '',
      attachments: []
    })
    const currentPost = reactive({
      id: '',
      title: '',
      author: '',
      createTime: '',
      content: '',
      attachments: [],
      viewCount: 0,
      commentCount: 0
    })
    const comments = ref([])
    const commentContent = ref('')

    const rules = {
      title: [{required: true, message: '请输入标题', trigger: 'blur'}],
      category: [{required: true, message: '请选择分类', trigger: 'change'}],
      content: [{required: true, message: '请输入内容', trigger: 'blur'}]
    }

    onMounted(() => {
      fetchPosts()
    })

    const fetchPosts = () => {
      getAllPosts({
        page: currentPage.value,
        pageSize: pageSize.value,
        category: activeCategory.value === 'all' ? '' : activeCategory.value
      }).then(response => {
        posts.value = response.data.content
        total.value = response.data.totalElements
      }).catch((error) => {
        console.error('获取帖子列表失败', error)
        ElMessage.error('获取帖子列表失败')
      })
    }

    const getCategoryTag = (category) => {
      const tags = {
        campus: 'success',
        study: 'primary',
        club: 'warning',
        career: 'danger',
        trade: 'info'
      }
      return tags[category] || 'info'
    }

    const getCategoryName = (category) => {
      const names = {
        campus: '校园生活',
        study: '学习交流',
        club: '社团活动',
        career: '求职就业',
        trade: '二手交易'
      }
      return names[category] || category
    }

    const handleCategorySelect = (category) => {
      activeCategory.value = category
      currentPage.value = 1
      fetchPosts()
    }

    const handleCreatePost = () => {
      postDialogVisible.value = true
      Object.keys(postForm).forEach(key => {
        postForm[key] = ''
      })
      postForm.attachments = []
    }

    const handleSubmit = () => {
      postFormRef.value.validate((valid) => {
        if (valid) {
          addPost(postForm).then(() => {
            ElMessage.success('发布成功')
            postDialogVisible.value = false
            fetchPosts()
          }).catch(() => {
            ElMessage.error('发布失败')
          })
        }
      })
    }

    const viewPost = (post) => {
      incrementPostViews(post.id).then(() => {
        getPostById(post.id).then(response => {
          Object.assign(currentPost, response.data)
          fetchComments(post.id)
          postDetailVisible.value = true
        }).catch((error) => {
          console.error('获取帖子详情失败', error)
          ElMessage.error('获取帖子详情失败')
        })
      }).catch((error) => {
        console.error('增加浏览量失败', error)
      })
    }

    const fetchComments = (postId) => {
      getPostComments(postId).then(response => {
        comments.value = response.data
      }).catch((error) => {
        console.error('获取评论列表失败', error)
        ElMessage.error('获取评论列表失败')
      })
    }

    const submitComment = () => {
      if (!commentContent.value.trim()) {
        ElMessage.warning('请输入评论内容')
        return
      }
      addComment({
        postId: currentPost.id,
        content: commentContent.value
      }).then(() => {
        ElMessage.success('评论成功')
        commentContent.value = ''
        viewPost(currentPost)
      }).catch(() => {
        ElMessage.error('评论失败')
      })
    }

    const handleUploadSuccess = (response, file) => {
      postForm.attachments.push({
        name: file.name,
        url: response.data.url,
        fileId: response.data.fileId
      })
    }

    const handleUploadRemove = (file, fileList) => {
      postForm.attachments = fileList
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchPosts()
    }

    return {
      posts,
      total,
      pageSize,
      currentPage,
      activeCategory,
      postDialogVisible,
      postDetailVisible,
      postFormRef,
      postForm,
      currentPost,
      comments,
      commentContent,
      rules,
      getCategoryTag,
      getCategoryName,
      handleCategorySelect,
      handleCreatePost,
      handleSubmit,
      viewPost,
      submitComment,
      handleUploadSuccess,
      handleUploadRemove,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.forum-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.category-card {
  margin-bottom: 20px;
}

.category-menu {
  border-right: none;
}

.post-list-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.post-detail {
  padding: 20px;
}

.post-info {
  margin-bottom: 20px;
  color: #909399;
}

.post-info span {
  margin-right: 20px;
}

.post-content {
  line-height: 1.6;
  margin-bottom: 20px;
}

.post-attachments {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
  margin-bottom: 20px;
}

.post-attachments h4 {
  margin-bottom: 10px;
}

.post-attachments ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.post-attachments li {
  margin-bottom: 5px;
}

.comment-section {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.comment-section h4 {
  margin-bottom: 20px;
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-header {
  margin-bottom: 10px;
}

.comment-author {
  font-weight: bold;
  margin-right: 10px;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  line-height: 1.6;
}

.comment-form {
  margin-top: 20px;
}
</style> 