<template>
  <div class="forum-container">
    <h1 class="forum-title">校园论坛</h1>

    <div class="forum-header">
      <div class="filter-bar">
        <el-select v-model="selectedForumType" clearable placeholder="选择分类" @change="handleForumTypeChange">
          <el-option v-for="forum in forums"
                     :key="forum.type"
                     :label="forum.name"
                     :value="forum.type"/>
        </el-select>

        <el-select v-model="sortBy" placeholder="排序方式" @change="fetchPosts">
          <el-option label="最新发布" value="createTime"/>
          <el-option label="最多浏览" value="viewCount"/>
          <el-option label="最多点赞" value="likeCount"/>
        </el-select>
      </div>

      <div class="search-bar">
        <el-input
            v-model="searchKeyword"
            :prefix-icon="Search"
            clearable
            placeholder="搜索帖子"
            @keyup.enter="handleSearch"/>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <div class="forum-content">
      <div class="main-content">
        <div class="forum-header">
          <h2>{{ currentCategory ? currentCategory.name : '所有帖子' }}</h2>
          <el-button type="primary" @click="navigateToCreate">
            <el-icon>
              <Plus/>
            </el-icon>
            发布新帖
          </el-button>
        </div>
        <PostList :category-id="selectedCategoryId" @post-selected="handlePostSelect"/>
      </div>

      <div class="side-content">
        <el-card class="hot-post-card">
          <template #header>
            <div class="side-card-header">
              <el-icon>
                <Hot/>
              </el-icon>
              <span>热门帖子</span>
            </div>
          </template>
          <el-skeleton v-if="loadingHotPosts" :rows="5" animated/>
          <div v-else-if="hotPosts.length === 0" class="no-data">暂无热门帖子</div>
          <ul v-else class="hot-post-list">
            <li v-for="post in hotPosts" :key="post.id" @click="goToPostDetail(post.id)">
              <span class="hot-post-title">{{ post.title }}</span>
              <span class="hot-post-count">{{ post.viewCount }} 浏览</span>
            </li>
          </ul>
        </el-card>

        <el-card class="tag-cloud-card">
          <template #header>
            <div class="side-card-header">
              <el-icon>
                <Collection/>
              </el-icon>
              <span>热门标签</span>
            </div>
          </template>
          <el-skeleton v-if="loadingTags" :rows="3" animated/>
          <div v-else-if="popularTags.length === 0" class="no-data">暂无热门标签</div>
          <div v-else class="tag-cloud">
            <el-tag
                v-for="tag in popularTags"
                :key="tag.id"
                :effect="selectedTag === tag.id ? 'dark' : 'plain'"
                class="tag-item"
                @click="handleTagClick(tag)">
              {{ tag.name }} ({{ tag.count }})
            </el-tag>
          </div>
        </el-card>
      </div>
    </div>

    <!-- Post Detail Dialog (if needed) -->
    <!-- ... -->
  </div>
</template>

<script>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {ChatDotRound, Collection, EditPen, Hot, InfoFilled, Search, Star, View, Plus} from '@element-plus/icons-vue'
import {getAllPosts, getHotPosts, incrementViewCount} from '@/api/post'
import request from '@/utils/request'
import {formatDistanceToNow} from 'date-fns'
import {zhCN} from 'date-fns/locale'
import PostList from './components/PostList.vue'

export default {
  name: 'ForumIndex',
  components: {
    PostList,
  },
  setup() {
    const router = useRouter()

    // 用户登录状态
    const isLoggedIn = computed(() => {
      return !!localStorage.getItem('token')
    })

    // 文章列表状态
    const posts = ref([])
    const loading = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const selectedForumType = ref('')
    const searchKeyword = ref('')
    const sortBy = ref('createTime')
    const forums = ref([])
    const selectedTag = ref(null)

    // 侧边栏状态
    const hotPosts = ref([])
    const loadingHotPosts = ref(false)
    const popularTags = ref([])
    const loadingTags = ref(false)

    // 发帖模态框
    const showPostModal = ref(false)

    // 其他常量
    const defaultAvatar = '/src/assets/default-avatar.png'

    // 获取分类列表
    const fetchForums = async () => {
      try {
        const res = await getAvailableForums()
        forums.value = res.data || []
      } catch (error) {
        console.error('获取论坛板块失败:', error)
        ElMessage.error('获取论坛板块失败')
      }
    }

    // 获取帖子列表
    const fetchPosts = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value,
          size: pageSize.value,
          sortBy: sortBy.value,
          keyword: searchKeyword.value || undefined,
          forumType: selectedForumType.value || undefined,
          tag: selectedTag.value || undefined
        }

        const res = await getAllPosts(params)
        posts.value = res.data.list
        total.value = res.data.total
      } catch (error) {
        console.error('获取帖子列表失败:', error)
        ElMessage.error('获取帖子列表失败')
      } finally {
        loading.value = false
      }
    }

    // 获取热门帖子
    const fetchHotPosts = async () => {
      loadingHotPosts.value = true
      try {
        const res = await getHotPosts()
        hotPosts.value = res.data.slice(0, 10) // 只显示前10个热门帖子
      } catch (error) {
        console.error('获取热门帖子失败:', error)
      } finally {
        loadingHotPosts.value = false
      }
    }

    // 获取热门标签
    const fetchPopularTags = async () => {
      loadingTags.value = true
      try {
        // 假设API已实现
        const res = await request.get('/forum/tags/popular')
        popularTags.value = res.data
      } catch (error) {
        console.error('获取热门标签失败:', error)
      } finally {
        loadingTags.value = false
      }
    }

    // 分页、筛选和搜索处理函数
    const handleSizeChange = (size) => {
      pageSize.value = size
      fetchPosts()
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchPosts()
    }

    const handleForumTypeChange = () => {
      currentPage.value = 1
      fetchPosts()
    }

    const handleSearch = () => {
      currentPage.value = 1
      fetchPosts()
    }

    const handleTagClick = (tag) => {
      if (selectedTag.value === tag.id) {
        selectedTag.value = null
      } else {
        selectedTag.value = tag.id
      }
      currentPage.value = 1
      fetchPosts()
    }

    // 跳转到帖子详情
    const goToPostDetail = async (postId) => {
      try {
        await incrementViewCount(postId)
        router.push(`/forum/post/${postId}`)
      } catch (error) {
        console.error('增加浏览量失败:', error)
        // 仍然跳转到详情页
        router.push(`/forum/post/${postId}`)
      }
    }

    // 获取分类标签名称
    const getForumName = (type) => {
      const forum = forums.value.find(f => f.type === type)
      return forum ? forum.name : type
    }

    // 格式化时间
    const formatTime = (dateString) => {
      try {
        return formatDistanceToNow(new Date(dateString), {
          addSuffix: true,
          locale: zhCN
        })
      } catch (e) {
        return dateString
      }
    }

    // 处理发帖成功
    const handlePostCreated = () => {
      showPostModal.value = false
      fetchPosts()
      ElMessage.success('发布成功')
    }

    // 前往登录页
    const goToLogin = () => {
      router.push('/login')
    }

    const navigateToCreate = () => {
      router.push('/forum/create')
    }

    onMounted(() => {
      fetchForums()
      fetchPosts()
      fetchHotPosts()
      fetchPopularTags()
    })

    return {
      // 数据
      posts,
      loading,
      currentPage,
      pageSize,
      total,
      selectedForumType,
      searchKeyword,
      sortBy,
      forums,
      hotPosts,
      loadingHotPosts,
      popularTags,
      loadingTags,
      selectedTag,
      showPostModal,
      isLoggedIn,
      defaultAvatar,

      // 方法
      fetchPosts,
      handleSizeChange,
      handleCurrentChange,
      handleForumTypeChange,
      handleSearch,
      handleTagClick,
      goToPostDetail,
      getForumName,
      formatTime,
      handlePostCreated,
      goToLogin,
      navigateToCreate,

      // 图标
      Search,
      View,
      ChatDotRound,
      Star,
      Hot,
      Collection,
      EditPen,
      InfoFilled,
      Plus
    }
  }
}
</script>

<style scoped>
.forum-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.forum-title {
  margin-bottom: 20px;
  text-align: center;
  color: #409EFF;
}

.forum-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-bar {
  display: flex;
  gap: 15px;
}

.search-bar {
  display: flex;
  gap: 10px;
}

.forum-content {
  display: flex;
  gap: 20px;
}

.main-content {
  flex: 1;
}

.side-content {
  width: 300px;
}

.post-list-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.create-post-card {
  margin-bottom: 15px;
}

.create-post-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.create-post-btn:hover {
  background-color: #f0f9ff;
}

.post-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
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

.post-time {
  font-size: 12px;
  color: #909399;
}

.post-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.post-summary {
  color: #606266;
  margin-bottom: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
}

.post-tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.side-card-header {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: bold;
}

.hot-post-card, .tag-cloud-card {
  margin-bottom: 20px;
}

.hot-post-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.hot-post-list li {
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
  display: flex;
  justify-content: space-between;
  cursor: pointer;
}

.hot-post-list li:last-child {
  border-bottom: none;
}

.hot-post-list li:hover .hot-post-title {
  color: #409EFF;
}

.hot-post-title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hot-post-count {
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  cursor: pointer;
}

.no-data {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}

.login-hint-card {
  margin-bottom: 15px;
}

.login-hint {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px;
}

@media (max-width: 992px) {
  .forum-content {
    flex-direction: column;
  }

  .side-content {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .forum-header {
    flex-direction: column;
  }

  .search-bar {
    width: 100%;
  }

  .filter-bar {
    width: 100%;
  }
}
</style> 