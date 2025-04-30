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

        <div class="side-content">
          <el-card class="hot-post-card">
            <template #header>
              <div class="side-card-header">
                <el-icon>
                  <Opportunity/>
                </el-icon>
                <span>热门帖子</span>
              </div>
            </template>
            <el-skeleton v-if="loadingHotPosts" :rows="5" animated/>
            <div v-else-if="!hotPosts || hotPosts.length === 0" class="no-data">暂无热门帖子</div>
            <ul v-else class="hot-post-list">
              <li v-for="post in hotPosts" :key="post.id" @click="goToPostDetail(post.id)">
                <span class="hot-post-title">{{ post.title }}</span>
                <span class="hot-post-count">{{ post.viewCount || 0 }} 浏览</span>
              </li>
            </ul>
          </el-card>
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
import {getAllPosts, getHotPosts, incrementViewCount} from '@/api/post'
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

    const hotPosts = ref([])
    const loadingHotPosts = ref(false)

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
        const res = await getAllPosts(params)
        if (res.data) {
          posts.value = res.data.records || []
          total.value = res.data.total || 0
        }
      } catch (error) {
        console.error('获取帖子列表失败:', error)
        ElMessage.error('加载帖子列表失败')
        posts.value = []
        total.value = 0
      } finally {
        loading.value = false
      }
    }

    const fetchHotPosts = async () => {
      loadingHotPosts.value = true
      try {
        const res = await getHotPosts({limit: 10})
        hotPosts.value = res.data || []
      } catch (error) {
        console.error('获取热门帖子失败:', error)
      } finally {
        loadingHotPosts.value = false
      }
    }

    const formatTime = (time) => {
      if (!time) return ''
      return formatDistanceToNow(new Date(time), {addSuffix: true, locale: zhCN})
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
      router.push({name: 'PostDetail', params: {id: postId}})
    }

    const goToLogin = () => {
      router.push('/login')
    }

    const handlePostCreated = (newPost) => {
      showPostModal.value = false
      fetchPosts()
    }

    watch(() => route.query, (newQuery) => {
      searchKeyword.value = newQuery.q || '';
      fetchPosts();
    }, {deep: true});

    onMounted(() => {
      fetchPosts()
      fetchHotPosts()
    })

    return {
      isLoggedIn,
      defaultAvatar,
      posts,
      loading,
      currentPage,
      pageSize,
      total,
      searchKeyword,
      sortBy,
      hotPosts,
      loadingHotPosts,
      showPostModal,
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
      Opportunity,
      fetchPosts
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

.side-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-list-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
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
  transition: box-shadow 0.3s;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
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
  font-weight: 600;
  color: #333;
}

.post-time {
  font-size: 0.85em;
  color: #909399;
}

.post-title {
  font-size: 1.2em;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-summary {
  font-size: 0.95em;
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.6;
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
}

.post-stats {
  display: flex;
  gap: 15px;
  color: #909399;
  font-size: 0.9em;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
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