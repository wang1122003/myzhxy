<template>
  <div class="forum-container">
    <div class="page-header">
      <h2>论坛管理</h2>
      <el-button type="primary" @click="handleAdd">添加帖子</el-button>
    </div>

    <el-card class="forum-card">
      <el-table :data="posts" border style="width: 100%">
        <el-table-column label="帖子标题" prop="title"/>
        <el-table-column label="作者" prop="author"/>
        <el-table-column label="分类" prop="category">
          <template #default="scope">
            <el-tag :type="getCategoryTag(scope.row.category)">
              {{ getCategoryName(scope.row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" prop="createTime"/>
        <el-table-column label="浏览数" prop="viewCount"/>
        <el-table-column label="评论数" prop="commentCount"/>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
                :type="scope.row.status === 1 ? 'warning' : 'success'"
                size="small"
                @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '删除' : '恢复' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">彻底删除</el-button>
          </template>
        </el-table-column>
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

    <!-- 添加/编辑帖子对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="700px"
    >
      <el-form ref="postFormRef" :model="postForm" :rules="rules" label-width="100px">
        <el-form-item label="帖子标题" prop="title">
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
        <el-form-item label="帖子内容" prop="content">
          <el-input
              v-model="postForm.content"
              :rows="10"
              placeholder="请输入帖子内容"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="附件" prop="attachments">
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
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {addPost, deletePost, getPostList, updatePost} from '@/api/forum'

export default {
  name: 'AdminForum',
  setup() {
    const posts = ref([])
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const postFormRef = ref(null)
    const postForm = reactive({
      id: '',
      title: '',
      category: '',
      content: '',
      attachments: [],
      status: 1
    })

    const rules = {
      title: [{required: true, message: '请输入帖子标题', trigger: 'blur'}],
      category: [{required: true, message: '请选择分类', trigger: 'change'}],
      content: [{required: true, message: '请输入帖子内容', trigger: 'blur'}]
    }

    const fetchPosts = () => {
      getPostList({
        page: currentPage.value - 1,
        size: pageSize.value
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

    const handleAdd = () => {
      dialogTitle.value = '添加帖子'
      dialogVisible.value = true
      Object.keys(postForm).forEach(key => {
        postForm[key] = ''
      })
      postForm.attachments = []
      postForm.status = 1
    }

    const handleEdit = (row) => {
      dialogTitle.value = '编辑帖子'
      dialogVisible.value = true
      Object.keys(postForm).forEach(key => {
        postForm[key] = row[key]
      })
    }

    const handleToggleStatus = (row) => {
      const newStatus = row.status === 1 ? 0 : 1
      updatePost(row.id, {...row, status: newStatus}).then(() => {
        ElMessage.success(newStatus === 1 ? '恢复成功' : '删除成功')
        fetchPosts()
      }).catch((error) => {
        console.error('更新帖子状态失败', error)
        ElMessage.error('操作失败')
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要彻底删除该帖子吗？此操作不可恢复', '提示', {
        type: 'warning'
      }).then(() => {
        deletePost(row.id).then(() => {
          ElMessage.success('删除成功')
          fetchPosts()
        }).catch((error) => {
          console.error('删除帖子失败', error)
          ElMessage.error('删除失败')
        })
      })
    }

    const handleUploadSuccess = (response, file, fileList) => {
      postForm.attachments = fileList.map(file => ({
        name: file.name,
        url: file.response?.data || file.url
      }))
    }

    const handleUploadRemove = (file, fileList) => {
      postForm.attachments = fileList.map(file => ({
        name: file.name,
        url: file.response?.data || file.url
      }))
    }

    const handleSubmit = () => {
      postFormRef.value.validate((valid) => {
        if (valid) {
          if (postForm.id) {
            updatePost(postForm.id, postForm).then(() => {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              fetchPosts()
            }).catch((error) => {
              console.error('更新帖子失败', error)
              ElMessage.error('更新失败')
            })
          } else {
            addPost(postForm).then(() => {
              ElMessage.success('添加成功')
              dialogVisible.value = false
              fetchPosts()
            }).catch((error) => {
              console.error('添加帖子失败', error)
              ElMessage.error('添加失败')
            })
          }
        }
      })
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchPosts()
    }

    onMounted(() => {
      fetchPosts()
    })

    return {
      posts,
      total,
      pageSize,
      currentPage,
      dialogVisible,
      dialogTitle,
      postFormRef,
      postForm,
      rules,
      getCategoryTag,
      getCategoryName,
      handleAdd,
      handleEdit,
      handleToggleStatus,
      handleDelete,
      handleUploadSuccess,
      handleUploadRemove,
      handleSubmit,
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

.forum-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 