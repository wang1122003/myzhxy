<template>
  <div class="forum-management-container">
    <div class="page-header">
      <h2>论坛管理</h2>
      <div class="action-buttons">
        <el-button type="success" @click="activeTab = 'categories'">
          <el-icon>
            <Menu/>
          </el-icon>
          板块管理
        </el-button>
        <el-button type="warning" @click="activeTab = 'comments'">
          <el-icon>
            <ChatDotRound/>
          </el-icon>
          评论管理
        </el-button>
        <el-button
            type="primary"
            @click="handleAddPost"
        >
          <el-icon>
            <Plus/>
          </el-icon>
          发布帖子
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabChange">
      <el-tab-pane label="帖子管理" name="post">
        <el-card class="tab-content-card">
          <template #header>
            <div class="card-header">
              <span>帖子列表</span>
              <!-- 可以添加刷新按钮等 -->
              <el-button :icon="Refresh" circle @click="fetchPosts"/>
            </div>
          </template>
          <!-- 帖子筛选 -->
          <el-form :inline="true" :model="postFilters" class="filter-form">
            <el-form-item label="关键词">
              <el-input v-model="postFilters.keyword" clearable placeholder="标题/内容/作者"/>
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="postFilters.categoryId" clearable filterable placeholder="选择分类">
                <el-option label="全部" value=""/>
                <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="postFilters.status" clearable placeholder="选择状态">
                <el-option label="全部" value=""/>
                <el-option label="正常" value="PUBLISHED"/>
                <el-option label="待审核" value="PENDING"/>
                <el-option label="已删除" value="DELETED"/>
                <el-option label="私密" value="PRIVATE"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button :icon="Search" type="primary" @click="fetchPosts">查询</el-button>
            </el-form-item>
          </el-form>
          <!-- 帖子表格 -->
          <el-table v-loading="postLoading" :data="postList" style="width: 100%">
            <el-table-column label="标题" min-width="200" prop="title" show-overflow-tooltip/>
            <el-table-column label="分类" prop="categoryName" width="100"/>
            <el-table-column label="作者" prop="authorName" width="120"/>
            <el-table-column label="浏览/评论/点赞" width="150">
              <template #default="scope">
                {{ scope.row.viewCount || 0 }} / {{ scope.row.commentCount || 0 }} / {{ scope.row.likeCount || 0 }}
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="status" width="100">
              <template #default="scope">
                <el-tag :type="getPostStatusType(scope.row.status)">
                  {{ formatPostStatus(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="置顶/精华" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.isTop" size="small" style="margin-right: 5px;">顶</el-tag>
                <el-tag v-if="scope.row.isEssence" size="small" type="warning">精</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="发布时间" prop="createTime" width="160">
              <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="280">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleViewPost(scope.row)">查看</el-button>
                <el-button size="small" @click="handleEditPost(scope.row)">编辑</el-button>
                <el-dropdown style="margin-left: 10px;">
                  <el-button size="small">更多
                    <el-icon>
                      <arrow-down/>
                    </el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="handleToggleTop(scope.row)">{{
                          scope.row.isTop ? '取消置顶' : '置顶'
                        }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="handleToggleEssence(scope.row)">
                        {{ scope.row.isEssence ? '取消加精' : '加精' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="handleDeletePost(scope.row, 'soft')">删除</el-dropdown-item>
                      <el-dropdown-item divided @click="handleDeletePost(scope.row, 'hard')">彻底删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无帖子数据"/>
            </template>
          </el-table>
          <!-- 帖子分页 -->
          <el-pagination
              v-if="postTotal > 0"
              v-model:current-page="postCurrentPage"
              v-model:page-size="postPageSize"
              :page-sizes="[10, 20, 50]"
              :total="postTotal"
              class="pagination-container"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handlePostSizeChange"
              @current-change="handlePostCurrentChange"
          />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="评论管理" name="comment">
        <CommentManagement/>
      </el-tab-pane>

      <el-tab-pane label="分类管理" name="category">
        <CategoryManagement/>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑帖子对话框 -->
    <el-dialog v-model="editPostDialogVisible" :close-on-click-modal="false" title="编辑帖子" width="70%">
      <el-form ref="editPostFormRef" :model="currentPost" :rules="editPostRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="currentPost.title" placeholder="请输入标题"/>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="currentPost.categoryId" placeholder="选择分类">
            <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <!-- Consider using a simple textarea for admin editing or a full rich text editor -->
          <RichTextEditor v-model="currentPost.content" @onCreated="handleEditorReady"/>
          <!-- <el-input v-model="currentPost.content" type="textarea" :rows="10" placeholder="请输入内容"/> -->
        </el-form-item>
        <!-- Add more fields if needed, e.g., tags -->
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editPostDialogVisible = false">取消</el-button>
          <el-button :loading="editSubmitting" type="primary" @click="submitEditPost">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看帖子对话框 -->
    <NoticeDetailDialog v-model="viewPostDialogVisible" :notice-id="currentPostId"/>

  </div>
</template>

<script setup>
import {nextTick, onBeforeUnmount, onMounted, reactive, ref, shallowRef} from 'vue'
import {
  ElButton,
  ElCard,
  ElDialog,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTabPane,
  ElTabs,
  ElTag
} from 'element-plus'
// Import necessary icons
import {ArrowDown, ChatDotRound, Menu, Plus, Refresh, Search} from '@element-plus/icons-vue'
// Assume API functions are correctly defined
import {
  deletePost as deletePostAdmin,
  getForumCategories as getCategoriesAdmin,
  getPosts as getPostsAdmin,
  updatePost as updatePostAdmin
} from '@/api/post'
// Import child components if they exist and are used
import CategoryManagement from '@/components/forum/CategoryManagement.vue' // Corrected path
import CommentManagement from '@/components/forum/CommentManagement.vue' // Corrected path
import RichTextEditor from '@/components/common/RichTextEditor.vue' // Adjust path if needed
import NoticeDetailDialog from '@/components/common/NoticeDetailDialog.vue' // Assuming this is for viewing posts

// Tab state
const activeTab = ref('post');

// --- Post Management State ---
const postLoading = ref(false);
const postList = ref([]);
const postTotal = ref(0);
const postCurrentPage = ref(1);
const postPageSize = ref(10);
const postFilters = reactive({ // Initialize postFilters
  keyword: '',
  categoryId: '',
  status: ''
});
const categories = ref([]); // For category dropdown in filter

// --- Dialog and Form State ---
const editPostDialogVisible = ref(false); // Define dialog visibility state
const viewPostDialogVisible = ref(false); // Define dialog visibility state
const editSubmitting = ref(false);
const currentPost = reactive({ // Form model for editing/creating
  id: null,
  title: '',
  categoryId: null,
  content: '',
  // Add other post fields as needed (e.g., tags, status)
});
const currentPostId = ref(null); // For viewing post details
const editPostFormRef = ref(null);
const editPostRules = reactive({ // Rules for the edit/create form
  title: [{required: true, message: '请输入标题', trigger: 'blur'}],
  categoryId: [{required: true, message: '请选择分类', trigger: 'change'}],
  content: [{required: true, message: '请输入内容', trigger: 'blur'}]
});

// Rich Text Editor Ref
const editorRef = shallowRef();

// --- Methods for Post Management ---

// Fetch categories for filter dropdown
const fetchCategories = async () => {
  try {
    const res = await getCategoriesAdmin(); // Use renamed admin API function
    if (res.code === 200) {
      categories.value = res.data || [];
    } else {
      ElMessage.error(res.message || '获取分类失败');
    }
  } catch (error) {
    console.error("获取分类失败:", error);
    // ElMessage.error('获取分类失败');
  }
};

// Fetch posts based on filters and pagination
const fetchPosts = async () => {
  postLoading.value = true;
  try {
    const params = {
      page: postCurrentPage.value,
      size: postPageSize.value,
      keyword: postFilters.keyword || undefined,
      categoryId: postFilters.categoryId || undefined,
      status: postFilters.status || undefined
    };
    const res = await getPostsAdmin(params); // Use renamed admin API function
    if (res.code === 200 && res.data) {
      postList.value = res.data.list || [];
      postTotal.value = res.data.total || 0;
    } else {
      ElMessage.error(res.message || '获取帖子列表失败');
      postList.value = [];
      postTotal.value = 0;
    }
  } catch (error) {
    console.error("获取帖子列表失败:", error);
    postList.value = [];
    postTotal.value = 0;
    // ElMessage.error('获取帖子列表失败');
  } finally {
    postLoading.value = false;
  }
};

// Handle adding a new post (show dialog)
const handleAddPost = () => {
  Object.assign(currentPost, { // Reset form
    id: null,
    title: '',
    categoryId: null,
    content: ''
  });
  editPostDialogVisible.value = true;
  // Reset editor content if necessary
  editorRef.value?.setHtml('');
};

// Handle editing a post (show dialog with data)
const handleEditPost = (row) => {
  Object.assign(currentPost, {...row}); // Copy row data to form
  editPostDialogVisible.value = true;
  // Set editor content (wait for dialog to render)
  nextTick(() => {
    editorRef.value?.setHtml(row.content || '');
  });
};

// Handle viewing a post (show detail dialog)
const handleViewPost = (row) => {
  currentPostId.value = row.id;
  viewPostDialogVisible.value = true;
};

// Submit post edit/creation
const submitEditPost = async () => {
  if (!editPostFormRef.value) return;
  await editPostFormRef.value.validate(async (valid) => {
    if (valid) {
      editSubmitting.value = true;
      try {
        // Get content from editor
        currentPost.content = editorRef.value?.getHtml() || '';
        let res;
        if (currentPost.id) { // Edit existing
          res = await updatePostAdmin(currentPost.id, currentPost);
        } else { // Create new (API needs defining)
          // res = await createPostAdmin(currentPost);
          ElMessage.warning('创建帖子 API 未实现'); // Placeholder
          throw new Error('Create API not implemented');
        }

        if (res.code === 200) {
          ElMessage.success(currentPost.id ? '更新成功' : '创建成功');
          editPostDialogVisible.value = false;
          fetchPosts(); // Refresh list
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch (error) {
        console.error("提交帖子失败:", error);
        // ElMessage.error('操作失败');
      } finally {
        editSubmitting.value = false;
      }
    }
  });
};

// Handle toggling top status
const handleToggleTop = async (row) => {
  const action = row.isTop ? '取消置顶' : '置顶';
  try {
    await updatePostAdmin(row.id, {isTop: !row.isTop});
    ElMessage.success(`${action}成功`);
    fetchPosts(); // Refresh
  } catch (error) {
    ElMessage.error(`${action}失败`);
    }
};

// Handle toggling essence status
const handleToggleEssence = async (row) => {
  const action = row.isEssence ? '取消加精' : '加精';
  try {
    await updatePostAdmin(row.id, {isEssence: !row.isEssence});
    ElMessage.success(`${action}成功`);
    fetchPosts(); // Refresh
  } catch (error) {
    ElMessage.error(`${action}失败`);
    }
};

// Handle deleting a post
const handleDeletePost = (row, type = 'soft') => {
  const actionText = type === 'hard' ? '彻底删除' : '删除';
  ElMessageBox.confirm(`确定要${actionText}帖子 "${row.title}" 吗? ${type === 'hard' ? '此操作不可恢复!' : ''}`, '警告', {
    confirmButtonText: `确定${actionText}`,
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePostAdmin(row.id); // Pass only id
      ElMessage.success(`${actionText}成功`);
      fetchPosts(); // Refresh list
    } catch (error) {
      console.error(`${actionText}帖子失败:`, error);
      // ElMessage.error(`${actionText}失败`);
    }
  }).catch(() => {
    ElMessage.info(`已取消${actionText}`);
  });
};

// Pagination handlers for posts
const handlePostSizeChange = (val) => {
  postPageSize.value = val;
  postCurrentPage.value = 1;
  fetchPosts();
};
const handlePostCurrentChange = (val) => {
  postCurrentPage.value = val;
  fetchPosts();
};

// Helper functions for formatting
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '-';
  try {
    return new Date(dateTimeString).toLocaleString();
  } catch (e) {
    return dateTimeString;
  }
};
const formatPostStatus = (status) => {
  const map = {'PUBLISHED': '正常', 'PENDING': '待审核', 'DELETED': '已删除', 'PRIVATE': '私密'};
  return map[status] || status || '未知';
};
const getPostStatusType = (status) => {
  const map = {'PUBLISHED': 'success', 'PENDING': 'warning', 'DELETED': 'danger', 'PRIVATE': 'info'};
  return map[status] || 'info';
};

// Handle tab change if necessary (e.g., load data for other tabs)
const handleTabChange = (tab) => {
  // console.log('Tab changed to:', tab.props.name);
  if (tab.props.name === 'post') {
    fetchPosts(); // Reload posts if switching back
  }
  // Add logic for other tabs if they don't load data automatically
};

// Rich text editor setup
const handleEditorReady = (editor) => {
  editorRef.value = editor;
};
onBeforeUnmount(() => {
  editorRef.value?.destroy();
});

// Initial data fetch on mount
onMounted(() => {
  fetchCategories();
  fetchPosts();
});

</script>
<!-- Add export name for component dev tools -->
<script>
export default {
  name: 'ForumManagement'
}
</script>

<style scoped>
.forum-management-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.tab-content-card {
  /* Add styles if needed */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 15px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.dialog-footer {
  text-align: right;
}
</style>