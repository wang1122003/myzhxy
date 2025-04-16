<template>
  <div class="forum-container">
    <h1>校园论坛</h1>
    <p>此页面允许公开访问查看帖子。</p>
    <p>发帖、回帖等操作需要登录。</p>
    <!-- 后续实现帖子列表、详情、发帖等功能 -->
    <el-empty description="论坛功能待实现"/>
  </div>
</template>

<script>
import {onMounted, ref} from 'vue'
// import { getPostList } from '@/api/forum' // 假设的 API
import {ElEmpty} from 'element-plus'

export default {
  name: 'ForumPage',
  components: {
    ElEmpty
  },
  setup() {
    const posts = ref([])
    const loading = ref(false)

    onMounted(() => {
      // fetchPosts()
    })

    return {
      posts,
      loading
    }
  }
}
</script>

<style scoped>
.forum-container {
  padding: 20px;
}
</style> 