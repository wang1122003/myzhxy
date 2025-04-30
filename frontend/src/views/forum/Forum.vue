<template>
  <div>
    <div class="forum-container">
      <h1 class="forum-title">校园论坛</h1>

      <div class="forum-header">
        <div class="filter-bar">
          <el-select v-model="sortBy" placeholder="排序方式" value-key="value" @change="fetchPosts">
            <el-option label="最新发布" value="createTime"/>
            <el-option label="最新更新" value="updateTime"/>
            <el-option label="最多浏览" value="viewCount"/>
            <el-option label="最多点赞" value="likeCount"/>
            <el-option label="最多评论" value="commentCount"/>
          </el-select>
        </div>

        <div class="search-bar">
          <el-input
              v-model="searchKeyword"
              :prefix-icon="Search"
              clearable
              placeholder="搜索帖子标题或内容"
              @keyup.enter="handleSearch"/>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
      </div>

      <div class="forum-content">
        <div class="main-content">
          <div class="post-list-container">
            <el-card v-if="!loading && isLoggedIn" class="create-post-card">
              <div class="create-post-btn" @click="showPostModal = true">
                <el-icon>
                  <EditPen/>
                </el-icon>
                <span>发布新帖子</span>
              </div>
            </el-card>

            <el-card v-else-if="!loading && !isLoggedIn" class="login-hint-card">
              <div class="login-hint">
                <el-icon>
                  <InfoFilled/>
                </el-icon>
                <span>登录后可发布新帖子和参与评论</span>
                <el-button size="small" type="primary" @click="goToLogin">去登录</el-button>
              </div>
            </el-card>

            <el-skeleton :loading="loading" :rows="10" animated/>

            <el-empty v-if="!loading && posts.length === 0" description="暂无相关帖子"/>

            <template v-if="!loading && posts.length > 0">
              <el-card v-for="post in posts" :key="post.id" class="post-card" @click="goToPostDetail(post.id)">
                <div class="post-header">
                  <div class="post-author">
                    <el-avatar :size="40" :src="post.author?.avatar || defaultAvatar"></el-avatar>
                    <div class="author-info">
                      <div class="author-name">{{ post.author?.username || '匿名用户' }}</div>
                      <div class="post-time">{{ formatTime(post.updateTime || post.createTime) }}</div>
                    </div>
                  </div>
                </div>

                <div class="post-title">{{ post.title }}</div>
                <div class="post-summary">
                  {{ post.summary || (post.content ? post.content.substring(0, 150) + '...' : '暂无摘要') }}
                </div>

                <div class="post-footer">
                  <div class="post-stats">
                    <div class="stat-item">
                      <el-icon>
                        <View/>
                      </el-icon>
                      <span>{{ post.viewCount || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <el-icon>
                        <ChatDotRound/>
                      </el-icon>
                      <span>{{ post.commentCount || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <el-icon>
                        <Star/>
                      </el-icon>
                      <span>{{ post.likeCount || 0 }}</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </template>
          </div>

          <div v-if="!loading && total > pageSize" class="pagination-container">
            <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="total"
                background
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>

      <el-dialog
          v-model="showPostModal"
          :close-on-click-modal="false"
          append-to-body
          destroy-on-close
          title="发布新帖子"
          width="70%">
        <CreatePost @post-created="handlePostCreated"/>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {ChatDotRound, EditPen, InfoFilled, Opportunity, Search, Star, View} from '@element-plus/icons-vue'
import {getAllPosts, incrementViewCount} from '@/api/post'
import {formatDistanceToNow} from 'date-fns'
import {zhCN} from 'date-fns/locale'
import CreatePost from '@/components/forum/CreatePost.vue'
import {useUserStore} from '@/stores/userStore'

export default {
  name: 'ForumPage',
  components: {
    CreatePost,
    EditPen,
    InfoFilled,
    View,
    ChatDotRound,
    Star,
    Opportunity,
    Search
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const userStore = useUserStore()

    const isLoggedIn = computed(() => userStore.isLoggedIn)
    const defaultAvatar = computed(() => userStore.avatar || '/src/assets/default-avatar.png')

    const posts = ref([])
    const loading = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const searchKeyword = ref(route.query.q || '')
    const sortBy = ref('createTime')

    const showPostModal = ref(false)

    const fetchPosts = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value,
          size: pageSize.value,
          sortBy: sortBy.value,
          keyword: searchKeyword.value || undefined
        }
        const responseData = await getAllPosts(params)
        console.log('拦截器处理后的响应数据:', responseData)
        if (responseData && typeof responseData.total === 'number' && Array.isArray(responseData.rows)) {
          console.log('成功解析帖子数据:', responseData)
          posts.value = responseData.rows || []
          total.value = responseData.total || 0
        } else {
          console.error('获取帖子列表失败或返回数据结构不正确:', responseData)
          const errorMsg = responseData && responseData.message ? `: ${responseData.message}` : ''
          ElMessage.error('加载帖子列表失败' + errorMsg)
          posts.value = []
          total.value = 0
        }
      } catch (error) {
        console.error('获取帖子列表失败:', error)
        ElMessage.error('加载帖子列表失败: ' + (error.message || '未知错误'))
        posts.value = []
        total.value = 0
      } finally {
        loading.value = false
      }
    }

    const formatTime = (time) => {
      // console.log('Formatting time:', time, 'Type:', typeof time);
      if (!time) return ''
      try {
        let dateObj;
        // Check if time is in the array format [year, month, day, hour, minute, second]
        if (Array.isArray(time) && time.length >= 6) {
          // Month is 0-indexed in JS Date constructor, adjust month by -1
          dateObj = new Date(time[0], time[1] - 1, time[2], time[3], time[4], time[5]);
        } else {
          // Try creating Date object directly (for standard strings or numbers)
          dateObj = new Date(time);
        }

        // Check if the created Date object is valid
        if (isNaN(dateObj.getTime())) {
          console.error('Invalid time value passed to formatTime or failed to parse:', time);
          return '日期无效'; // Return an indicator for invalid dates
        }
        return formatDistanceToNow(dateObj, {addSuffix: true, locale: zhCN})
      } catch (error) {
        console.error('Error formatting time:', time, error);
        return '日期格式错误'; // Return an error indicator
      }
    }

    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchPosts()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchPosts()
    }

    const handleSearch = () => {
      currentPage.value = 1
      fetchPosts()
    }

    const goToPostDetail = async (postId) => {
      try {
        await incrementViewCount(postId);
      } catch (error) {
        console.warn(`Failed to increment view count for post ${postId}:`, error);
      }
      router.push({name: 'PostDetail', params: {id: postId}});
    }

    const goToLogin = () => {
      router.push({path: '/', query: {redirect: router.currentRoute.value.fullPath}});
    }

    const handlePostCreated = () => {
      showPostModal.value = false;
      fetchPosts();
      ElMessage.success('发布成功！');
    }

    onMounted(() => {
      fetchPosts();
    });

    return {
      posts,
      loading,
      currentPage,
      pageSize,
      total,
      searchKeyword,
      sortBy,
      showPostModal,
      isLoggedIn,
      defaultAvatar,
      fetchPosts,
      formatTime,
      handleSizeChange,
      handleCurrentChange,
      handleSearch,
      goToPostDetail,
      goToLogin,
      handlePostCreated,
      Search,
      EditPen,
      InfoFilled,
      View,
      ChatDotRound,
      Star,
      Opportunity
    }
  }
}
</script>

<style scoped>
.forum-container {
  padding: 20px;
  max-width: 1200px;
  margin: 20px auto;
  background-color: #f9fafb;
}

.forum-title {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
  font-weight: 600;
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.filter-bar,
.search-bar {
  display: flex;
  gap: 15px;
}

.forum-content {
  display: flex;
  gap: 20px;
}

.main-content {
  flex: 3;
}

.post-list-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.create-post-card,
.login-hint-card {
  margin-bottom: 15px;
  transition: background-color 0.3s;
}

.create-post-card:hover {
  background-color: #f0f9eb;
}

.create-post-btn,
.login-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 15px;
  color: #67c23a;
  font-size: 16px;
}

.login-hint {
  color: #409eff;
  justify-content: space-between;
}

.login-hint .el-button {
  margin-left: 10px;
}

.create-post-btn .el-icon {
  margin-right: 8px;
}

.post-card {
  cursor: pointer;
  transition: box-shadow 0.3s, background-color 0.3s;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 18px;
}

.post-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  background-color: #fcfdfd;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.author-name {
  font-weight: 600;
  color: #333;
  font-size: 1.05em;
}

.post-time {
  font-size: 0.8em;
  color: #a8abb2;
}

.post-title {
  font-size: 1.25em;
  font-weight: 600;
  margin-bottom: 10px;
  color: #303133;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.post-summary {
  font-size: 0.95em;
  color: #555;
  margin-bottom: 18px;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f2f3f5;
  padding-top: 12px;
  margin-top: 5px;
}

.post-stats {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 0.9em;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.stat-item .el-icon {
  font-size: 1.1em;
}

.pagination-container {
  margin-top: 25px;
  display: flex;
  justify-content: center;
}

.side-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.hot-post-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.hot-post-list li {
  padding: 8px 0;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hot-post-list li:last-child {
  border-bottom: none;
}

.hot-post-list li:hover {
  background-color: #f5f7fa;
}

.hot-post-title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 10px;
  font-size: 0.95em;
}

.hot-post-count {
  font-size: 0.85em;
  color: #909399;
  white-space: nowrap;
}

.no-data {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}

/* Responsive adjustments */
@media (max-width: 992px) {
  .forum-content {
    flex-direction: column-reverse; /* Stack side content below main on smaller screens */
  }
  .side-content {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 20px; /* Add gap for wrapped items */
  }

  .hot-post-card, .tag-cloud-card {
    flex: 1 1 calc(50% - 10px);
    min-width: 250px;
  }
}

/* Restore the 768px block */
@media (max-width: 768px) {
  .forum-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .filter-bar, .search-bar {
    flex-direction: column;
    width: 100%;
  }

  .side-content {
    flex-direction: column;
    gap: 20px;
  }

  .hot-post-card, .tag-cloud-card {
    flex: 1 1 100%;
  }

  .post-title {
    font-size: 1.1em;
  }

  .post-summary {
    font-size: 0.9em;
  }

  .pagination-container .el-pagination {
    justify-content: center;
  }

  .pagination-container .el-pagination__jump {
    margin-left: 5px !important;
  }

  .pagination-container .el-pagination__sizes {
    margin-right: 5px !important;
  }
}
</style> 