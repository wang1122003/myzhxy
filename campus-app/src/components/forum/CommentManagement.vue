<template>
  <div class="comment-management-container">
    <div class="header">
      <h3>评论管理</h3>
      <div class="filter-actions">
        <el-select v-model="filterStatus" clearable placeholder="评论状态" @change="handleFilterChange">
          <el-option label="全部" value=""/>
          <el-option :value="1" label="正常"/>
          <el-option :value="0" label="待审核"/>
          <el-option :value="-1" label="已删除"/>
        </el-select>
        <el-input
            v-model="searchKeyword"
            clearable
            placeholder="搜索评论内容"
            style="width: 200px; margin-left: 10px;"
            @keyup.enter="handleSearch"
        >
          <template #suffix>
            <el-icon class="el-input__icon" @click="handleSearch">
              <Search/>
            </el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <el-table
        v-loading="loading"
        :data="comments"
        border
        style="width: 100%"
    >
      <el-table-column type="expand">
        <template #default="props">
          <div class="comment-detail">
            <p><strong>评论内容：</strong></p>
            <p>{{ props.row.content }}</p>
            <p v-if="props.row.postTitle"><strong>帖子标题：</strong> {{ props.row.postTitle }}</p>
            <p v-if="props.row.parentId"><strong>回复对象：</strong> {{ props.row.replyToName }}</p>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="评论内容" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          {{ truncateContent(scope.row.content) }}
        </template>
      </el-table-column>
      <el-table-column label="发布者" prop="authorName" width="120"/>
      <el-table-column label="帖子标题" min-width="150" prop="postTitle" show-overflow-tooltip/>
      <el-table-column label="点赞数" prop="likeCount" sortable width="80"/>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.status === -1" type="danger">已删除</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="200">
        <template #default="scope">
          <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="success"
              @click="handleApproveComment(scope.row)"
          >
            审核通过
          </el-button>
          <el-button
              v-if="scope.row.status !== -1"
              size="small"
              type="danger"
              @click="handleDeleteComment(scope.row)"
          >
            删除
          </el-button>
          <el-button
              v-if="scope.row.status === -1"
              size="small"
              type="primary"
              @click="handleRestoreComment(scope.row)"
          >
            恢复
          </el-button>
          <el-button
              size="small"
              @click="viewPost(scope.row)"
          >
            查看帖子
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页控件 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Search} from '@element-plus/icons-vue'
import {useRouter} from 'vue-router'
import {getAllComments, updateCommentStatus} from '@/api/forum'
import {formatDistanceToNow} from 'date-fns'
import {zhCN} from 'date-fns/locale'

export default {
  name: 'CommentManagement',
  components: {
    Search
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const comments = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const filterStatus = ref('')
    const searchKeyword = ref('')

    // 获取评论列表
    const fetchComments = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value,
          size: pageSize.value,
          status: filterStatus.value !== '' ? filterStatus.value : null,
          keyword: searchKeyword.value || null
        }

        const res = await getAllComments(params)
        comments.value = res.data.list || []
        total.value = res.data.total || 0
      } catch (error) {
        console.error('获取评论列表失败:', error)
        ElMessage.error('获取评论列表失败')
      } finally {
        loading.value = false
      }
    }

    // 截断内容
    const truncateContent = (content) => {
      if (!content) return ''
      return content.length > 50 ? content.substring(0, 50) + '...' : content
    }

    // 审核通过评论
    const handleApproveComment = async (comment) => {
      try {
        await updateCommentStatus(comment.id, 1)
        ElMessage.success('评论已通过审核')
        comment.status = 1
      } catch (error) {
        console.error('审核评论失败:', error)
        ElMessage.error('操作失败')
      }
    }

    // 删除评论
    const handleDeleteComment = (comment) => {
      ElMessageBox.confirm(`确定要删除此评论吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await updateCommentStatus(comment.id, -1)
          ElMessage.success('评论已删除')
          comment.status = -1
        } catch (error) {
          console.error('删除评论失败:', error)
          ElMessage.error('删除失败')
        }
      }).catch(() => {
        ElMessage.info('已取消删除')
      })
    }

    // 恢复评论
    const handleRestoreComment = async (comment) => {
      try {
        await updateCommentStatus(comment.id, 1)
        ElMessage.success('评论已恢复')
        comment.status = 1
      } catch (error) {
        console.error('恢复评论失败:', error)
        ElMessage.error('操作失败')
      }
    }

    // 查看帖子
    const viewPost = (comment) => {
      if (comment.postId) {
        router.push(`/forum/post/${comment.postId}`)
      } else {
        ElMessage.warning('无法获取帖子信息')
      }
    }

    // 过滤状态变化
    const handleFilterChange = () => {
      currentPage.value = 1
      fetchComments()
    }

    // 搜索
    const handleSearch = () => {
      currentPage.value = 1
      fetchComments()
    }

    // 分页大小变化
    const handleSizeChange = (val) => {
      pageSize.value = val
      currentPage.value = 1
      fetchComments()
    }

    // 页码变化
    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchComments()
    }

    // 格式化时间
    const formatTime = (timeStr) => {
      if (!timeStr) return '-'
      try {
        const date = new Date(timeStr)
        return formatDistanceToNow(date, {locale: zhCN})
      } catch (e) {
        return timeStr
      }
    }

    onMounted(() => {
      fetchComments()
    })

    return {
      loading,
      comments,
      total,
      currentPage,
      pageSize,
      filterStatus,
      searchKeyword,
      truncateContent,
      handleApproveComment,
      handleDeleteComment,
      handleRestoreComment,
      viewPost,
      handleFilterChange,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      formatTime
    }
  }
}
</script>

<style scoped>
.comment-management-container {
  margin-bottom: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.header h3 {
  margin: 0;
}

.filter-actions {
  display: flex;
  align-items: center;
}

.comment-detail {
  padding: 10px 20px;
  background-color: #f9f9f9;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 