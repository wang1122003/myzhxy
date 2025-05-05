<template>
  <div class="post-detail-container">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon>
          <ArrowLeft/>
        </el-icon>
        返回论坛
      </el-button>
    </div>

    <div class="post-detail-content">
      <!-- 加载动画 -->
      <el-skeleton v-if="loading" :rows="15" animated/>

      <!-- 帖子详情 -->
      <template v-else-if="post.id">
        <el-card class="post-card">
          <div class="post-header">
            <div class="post-title">
              <h1>{{ post.title }}</h1>
              <div v-if="post.category" class="post-category">
                <el-tag size="small">{{ post.category }}</el-tag>
              </div>
            </div>

            <div class="post-meta">
              <div class="post-author">
                <el-avatar :size="40" :src="post?.author?.avatar || defaultAvatar"></el-avatar>
                <div class="author-info">
                  <div class="author-name">
                    {{ post?.author?.realName ?? post?.author?.username ?? post?.authorName ?? '匿名用户' }}
                  </div>
                  <div class="post-time">{{ formatTime(post.createTime) }}</div>
                </div>
              </div>

              <div class="post-actions">
                <el-button
                    :class="{'active-like': post.liked}"
                    :loading="likeLoading"
                    link
                    @click="handleLike"
                >
                  <el-icon>
                    <Star/>
                  </el-icon>
                  <span>{{ post.likeCount || 0 }}</span>
                </el-button>

                <el-button link>
                  <el-icon>
                    <View/>
                  </el-icon>
                  <span>{{ post.viewCount || 0 }}</span>
                </el-button>

                <el-button link @click="scrollToComments">
                  <el-icon>
                    <ChatDotRound/>
                  </el-icon>
                  <span>{{ post.commentCount || 0 }}</span>
                </el-button>
              </div>
            </div>
          </div>

          <div class="post-divider"></div>

          <!-- 帖子正文 -->
          <div class="post-content" v-html="post.content"></div>
        </el-card>

        <!-- 评论区 -->
        <el-card id="comments-section" class="comments-card">
          <template #header>
            <div class="card-header">
              <h3>评论 ({{ comments.length }})</h3>
            </div>
          </template>

          <!-- 评论列表 -->
          <div v-if="comments.length === 0" class="no-comments">
            暂无评论，快来发表第一条评论吧
          </div>

          <div v-else class="comment-list">
            <div
                v-for="comment in comments"
                :key="comment.id"
                class="comment-item"
            >
              <div class="comment-header">
                <div class="comment-author">
                  <el-avatar :size="32" :src="comment.avatar || defaultAvatar"></el-avatar>
                  <div class="author-info">
                    <div class="author-name">
                      {{ comment.author ? (comment.author.realName || comment.author.username) : comment.authorName }}
                    </div>
                    <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
                  </div>
                </div>

                <div v-if="canEditComment(comment)" class="comment-actions">
                  <el-dropdown>
                    <el-icon>
                      <MoreFilled/>
                    </el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click="handleDeleteComment(comment)">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>

              <div class="comment-content">{{ comment.content }}</div>

              <div class="comment-footer">
                <el-button
                    :class="{'active-like': comment.liked}"
                    :loading="comment.likeLoading"
                    link
                    size="small"
                    @click="handleLikeComment(comment)"
                >
                  <el-icon>
                    <Star/>
                  </el-icon>
                  <span>{{ comment.likeCount || 0 }}</span>
                </el-button>

                <el-button
                    link
                    size="small"
                    @click="handleReply(comment)"
                >
                  <el-icon>
                    <ChatLineRound/>
                  </el-icon>
                  <span>回复</span>
                </el-button>
              </div>

              <!-- 子评论 -->
              <div v-if="comment.replies && comment.replies.length > 0" class="sub-comments">
                <div
                    v-for="subComment in comment.replies"
                    :key="subComment.id"
                    class="sub-comment-item"
                >
                  <div class="comment-header">
                    <div class="comment-author">
                      <el-avatar :size="28" :src="subComment.avatar || defaultAvatar"></el-avatar>
                      <div class="author-info">
                        <div class="author-name">
                          {{ subComment.author ? (subComment.author.realName || subComment.author.username) : '' }}
                          <span v-if="subComment.replyToAuthor" class="reply-to">
                            回复 {{ subComment.replyToAuthor.realName || subComment.replyToAuthor.username }}
                          </span>
                        </div>
                        <div class="comment-time">{{ formatTime(subComment.createTime) }}</div>
                      </div>
                    </div>

                    <div v-if="canEditComment(subComment)" class="comment-actions">
                      <el-dropdown>
                        <el-icon>
                          <MoreFilled/>
                        </el-icon>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item @click="handleDeleteComment(subComment)">删除</el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>

                  <div class="comment-content">{{ subComment.content }}</div>

                  <div class="comment-footer">
                    <el-button
                        :class="{'active-like': subComment.liked}"
                        :loading="subComment.likeLoading"
                        link
                        size="small"
                        @click="handleLikeComment(subComment)"
                    >
                      <el-icon>
                        <Star/>
                      </el-icon>
                      <span>{{ subComment.likeCount || 0 }}</span>
                    </el-button>

                    <el-button
                        link
                        size="small"
                        @click="handleReply(comment, subComment)"
                    >
                      <el-icon>
                        <ChatLineRound/>
                      </el-icon>
                      <span>回复</span>
                    </el-button>
                  </div>
                </div>
              </div>

              <!-- 回复输入框 -->
              <div v-if="replyingTo === comment.id" class="reply-box">
                <el-input
                    v-model="replyContent"
                    :placeholder="replyingToName ? `回复 ${replyingToName}` : '回复评论'"
                    :rows="2"
                    maxlength="500"
                    show-word-limit
                    type="textarea"
                />
                <div class="reply-actions">
                  <el-button size="small" @click="cancelReply">取消</el-button>
                  <el-button
                      :loading="submittingReply"
                      size="small"
                      type="primary"
                      @click="submitReply"
                  >
                    回复
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 发表评论 -->
          <div v-if="isLoggedIn" class="comment-form">
            <h4 class="comment-form-title">发表评论</h4>
            <el-input
                v-model="commentContent"
                :rows="3"
                maxlength="500"
                placeholder="请输入评论内容..."
                show-word-limit
                type="textarea"
            />
            <div class="comment-submit">
              <el-button
                  :disabled="!commentContent.trim()"
                  :loading="submittingComment"
                  type="primary"
                  @click="submitComment"
              >
                发表评论
              </el-button>
            </div>
          </div>

          <!-- 未登录提示 -->
          <div v-else class="login-hint">
            <el-alert
                :closable="false"
                show-icon
                title="请登录后参与评论"
                type="info"
            >
              <template #default>
                <div class="login-action">
                  <el-button size="small" type="primary" @click="goToLogin">去登录</el-button>
                </div>
              </template>
            </el-alert>
          </div>
        </el-card>
      </template>

      <!-- 帖子不存在 -->
      <el-empty v-else description="帖子不存在或已被删除">
        <el-button type="primary" @click="goBack">返回论坛</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {ArrowLeft, ChatDotRound, ChatLineRound, MoreFilled, Star, View} from '@element-plus/icons-vue'
import {
  cancelLikeComment,
  cancelLikePost,
  createComment,
  deleteComment,
  getPostById,
  getRootComments,
  likeComment,
  likePost
} from '@/api/post'
import {formatDistance} from 'date-fns'
import {zhCN} from 'date-fns/locale'
import {useUserStore} from '@/stores/userStore'

export default {
  name: 'PostDetail',
  components: {
    ArrowLeft,
    Star,
    View,
    ChatDotRound,
    ChatLineRound,
    MoreFilled
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()
    const userInfo = computed(() => userStore.userInfo || {})
    const postId = computed(() => route.params.id)

    // 用户登录状态
    const isLoggedIn = computed(() => {
      return !!localStorage.getItem('token')
    })

    // 帖子数据
    const post = ref({})
    const loading = ref(true)
    const likeLoading = ref(false)
    const defaultAvatar = '/src/assets/default-avatar.png'

    // 评论数据
    const comments = ref([])
    const commentContent = ref('')
    const submittingComment = ref(false)
    const commentLoading = ref(false)
    const commentTotal = ref(0)

    // 回复数据
    const replyingTo = ref(null)
    const replyingToComment = ref(null)
    const replyContent = ref('')
    const replyingToName = ref('')
    const submittingReply = ref(false)

    // 获取帖子详情
    const fetchPostDetail = async () => {
      loading.value = true
      try {
        const res = await getPostById(postId.value)
        post.value = res
      } catch (error) {
        console.error('获取帖子详情失败:', error)
        ElMessage.error('获取帖子详情失败')
        post.value = {}
      } finally {
        loading.value = false
      }
    }

    // 获取评论列表 (适配后端临时接口，不分页)
    const fetchComments = async () => {
      commentLoading.value = true;
      try {
        // 不再传递分页参数
        const responseData = await getRootComments(postId.value);
        console.log('评论接口返回数据:', responseData);

        // 后端直接返回评论数组 (Result包装已被拦截器处理掉)
        if (Array.isArray(responseData)) {
          comments.value = responseData;
          commentTotal.value = comments.value.length; // 直接使用数组长度作为总数
        } else {
          console.error('评论接口返回数据格式不正确，期望是数组:', responseData);
          comments.value = [];
          commentTotal.value = 0;
        }

        // 添加点赞加载状态
        comments.value.forEach(comment => {
          comment.likeLoading = false
          // 考虑是否嵌套回复或需要单独获取
          if (comment.replies) {
            comment.replies.forEach(subComment => {
              subComment.likeLoading = false
            })
          }
        })
      } catch (error) {
        console.error('获取评论失败:', error)
        // Don't show generic error message if it was a 404, 
        // maybe the post just has no comments (though API should return empty array ideally)
        if (!error.message || !error.message.includes('404')) {
          ElMessage.error('获取评论失败');
        }
        comments.value = []
        commentTotal.value = 0;
      } finally {
        commentLoading.value = false;
      }
    }

    // 前往登录页
    const goToLogin = () => {
      router.push('/login')
    }

    // 点赞帖子
    const handleLike = async () => {
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录')
        return
      }

      likeLoading.value = true
      try {
        if (post.value.liked) {
          await cancelLikePost(postId.value)
          post.value.liked = false
          post.value.likeCount--
        } else {
          await likePost(postId.value)
          post.value.liked = true
          post.value.likeCount++
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        likeLoading.value = false
      }
    }

    // 点赞评论
    const handleLikeComment = async (comment) => {
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录')
        return
      }

      comment.likeLoading = true
      try {
        if (comment.liked) {
          await cancelLikeComment(comment.id)
          comment.liked = false
          comment.likeCount--
        } else {
          await likeComment(comment.id)
          comment.liked = true
          comment.likeCount++
        }
      } catch (error) {
        console.error('点赞评论操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        comment.likeLoading = false
      }
    }

    // 删除评论
    const handleDeleteComment = async (comment) => {
      if (!canEditComment(comment)) {
        ElMessage.warning('您没有权限删除此评论')
        return
      }

      try {
        await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await deleteComment(comment.id)
        ElMessage.success('评论已删除')

        // 重新获取评论列表
        fetchComments()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除评论失败:', error)
          ElMessage.error('删除评论失败')
        }
      }
    }

    // 发表评论
    const submitComment = async () => {
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录')
        return
      }

      if (!commentContent.value.trim()) {
        ElMessage.warning('评论内容不能为空')
        return
      }

      submittingComment.value = true
      try {
        await createComment({
          postId: postId.value,
          content: commentContent.value.trim()
        })

        ElMessage.success('评论发表成功')
        commentContent.value = ''

        // 重新获取评论列表和帖子详情
        fetchComments()
        fetchPostDetail()
      } catch (error) {
        console.error('发表评论失败:', error)
        ElMessage.error('发表评论失败')
      } finally {
        submittingComment.value = false
      }
    }

    // 回复评论
    const handleReply = (comment, subComment = null) => {
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录')
        return
      }

      replyingTo.value = comment.id
      replyingToComment.value = subComment ? subComment : comment
      replyingToName.value = subComment
          ? (subComment.author ? (subComment.author.realName || subComment.author.username) : subComment.authorName || '')
          : (comment.author ? (comment.author.realName || comment.author.username) : comment.authorName || '')
      replyContent.value = ''
    }

    // 取消回复
    const cancelReply = () => {
      replyingTo.value = null
      replyingToComment.value = null
      replyingToName.value = ''
      replyContent.value = ''
    }

    // 提交回复
    const submitReply = async () => {
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录')
        return
      }

      if (!replyContent.value.trim()) {
        ElMessage.warning('回复内容不能为空')
        return
      }

      submittingReply.value = true
      try {
        await createComment({
          postId: postId.value,
          parentId: replyingTo.value,
          replyToId: replyingToComment.value.id,
          content: replyContent.value.trim()
        })

        ElMessage.success('回复发表成功')
        cancelReply()

        // 重新获取评论列表和帖子详情
        fetchComments()
        fetchPostDetail()
      } catch (error) {
        console.error('发表回复失败:', error)
        ElMessage.error('发表回复失败')
      } finally {
        submittingReply.value = false
      }
    }

    // 判断是否可以编辑评论 (当前用户是评论作者或管理员)
    const canEditComment = (comment) => {
      return isLoggedIn.value &&
          (comment.authorId === userInfo.value.id || userInfo.value.role === 'admin')
    }

    // 格式化时间
    const formatTime = (dateString) => {
      if (!dateString) return '-'
      try {
        const date = new Date(dateString)
        return formatDistance(date, new Date(), {
          addSuffix: true,
          locale: zhCN
        })
      } catch (e) {
        return dateString
      }
    }

    // 返回上一页
    const goBack = () => {
      router.push('/forum')
    }

    // 滚动到评论区
    const scrollToComments = () => {
      const commentsSection = document.getElementById('comments-section')
      if (commentsSection) {
        commentsSection.scrollIntoView({behavior: 'smooth', block: 'start'})
      }
    }

    onMounted(() => {
      fetchPostDetail()
      fetchComments()
    })

    return {
      post,
      loading,
      likeLoading,
      comments,
      commentContent,
      submittingComment,
      replyingTo,
      replyContent,
      replyingToName,
      submittingReply,
      userInfo,
      isLoggedIn,
      defaultAvatar,
      commentLoading,
      commentTotal,

      goBack,
      scrollToComments,
      handleLike,
      submitComment,
      canEditComment,
      handleDeleteComment,
      handleLikeComment,
      handleReply,
      cancelReply,
      submitReply,
      formatTime,
      goToLogin
    }
  }
}
</script>

<style scoped>
.post-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.post-card {
  margin-bottom: 30px;
}

.post-header {
  margin-bottom: 20px;
}

.post-title {
  margin-bottom: 15px;
}

.post-title h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: bold;
}

.post-time, .comment-time {
  font-size: 12px;
  color: #909399;
}

.post-actions {
  display: flex;
  gap: 15px;
}

.post-divider {
  height: 1px;
  background-color: #EBEEF5;
  margin: 20px 0;
}

.post-content {
  margin-bottom: 30px;
  line-height: 1.6;
}

.comments-card {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
}

.no-comments {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

.comment-list {
  margin-bottom: 20px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #EBEEF5;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-actions {
  color: #909399;
  cursor: pointer;
}

.comment-content {
  margin-bottom: 10px;
  line-height: 1.5;
  color: #303133;
  word-break: break-word;
}

.comment-footer {
  display: flex;
  gap: 15px;
}

.sub-comments {
  margin-top: 15px;
  margin-left: 42px;
  background-color: #f9f9f9;
  border-radius: 4px;
  padding: 10px;
}

.sub-comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
}

.sub-comment-item:last-child {
  border-bottom: none;
}

.reply-to {
  font-size: 12px;
  color: #909399;
  margin-left: 5px;
  font-weight: normal;
}

.reply-box {
  margin-top: 15px;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.comment-form {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
}

.comment-form-title {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
}

.comment-submit {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}

.active-like {
  color: #e6a23c;
}

:deep(.el-button--text) {
  display: flex;
  align-items: center;
  gap: 5px;
}

@media (max-width: 768px) {
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .post-actions {
    width: 100%;
    justify-content: space-around;
  }
}

.login-hint {
  padding: 20px 0;
}

.login-action {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style> 