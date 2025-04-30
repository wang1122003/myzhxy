<template>
  <div class="forum-management-container">
    <div class="page-header">
      <h2>论坛管理</h2>
      <div class="action-buttons">
        <!-- 移除分类管理按钮 -->
        <!-- <el-button type="success" @click="activeTab = 'categories'">
          <el-icon>
            <Menu/>
          </el-icon>
          板块管理
        </el-button> -->
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
            <!-- 移除分类筛选 -->
            <!-- <el-form-item label="分类">
              <el-select v-model="postFilters.categoryId" clearable filterable placeholder="选择分类">
                <el-option label="全部" value=""/>
                <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                />
              </el-select>
            </el-form-item> -->
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
            <!-- 移除分类列 -->
            <!-- <el-table-column label="分类" prop="categoryName" width="100"/> -->
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

      <!-- 移除分类管理 Tab -->
      <!-- <el-tab-pane label="分类管理" name="category">
        <CategoryManagement/>
      </el-tab-pane> -->
    </el-tabs>

    <!-- 编辑帖子对话框 -->
    <el-dialog v-model="editPostDialogVisible" :close-on-click-modal="false" title="编辑帖子" width="70%">
      <el-form ref="editPostFormRef" :model="currentPost" :rules="editPostRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="currentPost.title" placeholder="请输入标题"/>
        </el-form-item>
        <!-- 移除分类选择 -->
        <!-- <el-form-item label="分类" prop="categoryId">
          <el-select v-model="currentPost.categoryId" placeholder="选择分类">
            <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
            />
          </el-select>
        </el-form-item> -->
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
import {onBeforeUnmount, onMounted, reactive, ref} from 'vue'
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
import {ArrowDown, ChatDotRound, Plus, Refresh, Search} from '@element-plus/icons-vue'
// Assume API functions are correctly defined
import {deletePost as deletePostAdmin, getPosts as getPostsAdmin, updatePost as updatePostAdmin} from '@/api/post'
// Import child components if they exist and are used
// 移除 CategoryManagement 导入
// import CategoryManagement from '@/components/forum/CategoryManagement.vue' // Corrected path
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
  // 移除 categoryId
  // categoryId: '',
  status: ''
});
// 移除 categories
// const categories = ref([]); // For category dropdown in filter

// --- Dialog and Form State ---
const editPostDialogVisible = ref(false); // Define dialog visibility state
const viewPostDialogVisible = ref(false); // Define dialog visibility state
const editSubmitting = ref(false);
const currentPost = reactive({ // Form model for editing/creating
  id: null,
  title: '',
  // 移除 categoryId
  // categoryId: null,
  content: '',
  // ... 其他字段 ...
});
const editPostFormRef = ref(null); // Ref for edit post form
const currentPostId = ref(null); // For viewing post detail

const editPostRules = { // Rules for editing post
  title: [{required: true, message: '标题不能为空', trigger: 'blur'}],
  content: [{required: true, message: '内容不能为空', trigger: 'blur'}]
  // 移除 categoryId 验证规则
  // categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
};

// Editor instance for RichTextEditor
let editorInstance = null;
const handleEditorReady = (editor) => {
  editorInstance = editor;
};
onBeforeUnmount(() => {
  if (editorInstance) {
    editorInstance.destroy();
  }
});

// --- Methods ---

// Fetch Posts
const fetchPosts = async () => {
  postLoading.value = true;
  try {
    // 动态构建 params，仅包含非空值的过滤条件
    const params = {
      page: postCurrentPage.value,
      size: postPageSize.value,
    };
    if (postFilters.keyword) {
      params.keyword = postFilters.keyword;
    }
    // 移除 categoryId 的处理
    // if (postFilters.categoryId) {
    //   params.categoryId = postFilters.categoryId;
    // }
    if (postFilters.status) { // 只有当 status 非空时才添加到 params
      params.status = postFilters.status;
    }

    const res = await getPostsAdmin(params); // 使用 admin API endpoint
    if (res && res.data) {
      postList.value = res.data.rows || [];
      postTotal.value = res.data.total || 0;
    } else {
      postList.value = [];
      postTotal.value = 0;
    }
  } catch (error) {
    console.error("获取帖子列表失败:", error);
    ElMessage.error("获取帖子列表失败");
  } finally {
    postLoading.value = false;
  }
};

// Fetch Categories (移除)
// const fetchCategories = async () => {
//   try {
//     const res = await getCategoriesAdmin();
//     categories.value = res.data || [];
//   } catch (error) {
//     console.error("获取分类列表失败:", error);
//     ElMessage.error("获取分类列表失败");
//   }
// };

// Handle Tab Change
const handleTabChange = (tab) => {
  activeTab.value = tab.paneName;
  if (activeTab.value === 'post') {
    fetchPosts();
  }
  // 如果有 CommentManagement 或其他 Tab，也可能需要刷新
};

// Handle Pagination
const handlePostSizeChange = (val) => {
  postPageSize.value = val;
  fetchPosts();
};
const handlePostCurrentChange = (val) => {
  postCurrentPage.value = val;
  fetchPosts();
};

// Handle Post Actions
const handleAddPost = () => {
  // Navigate to CreatePost page or open a dialog
  // For simplicity, assume navigation
  window.open('/forum/create', '_blank'); // Open in new tab
};

const handleViewPost = (row) => {
  currentPostId.value = row.id;
  viewPostDialogVisible.value = true;
};

const handleEditPost = (row) => {
  // Reset form before populating
  if (editPostFormRef.value) {
    editPostFormRef.value.resetFields();
  }
  // Shallow copy or deep copy depending on need
  Object.assign(currentPost, row);
  // Ensure content is loaded correctly for RichTextEditor
  if (editorInstance) {
    editorInstance.setHtml(row.content || '');
  }
  editPostDialogVisible.value = true;
};

const submitEditPost = async () => {
  if (!editPostFormRef.value) return;
  await editPostFormRef.value.validate(async (valid) => {
    if (valid) {
      editSubmitting.value = true;
      try {
        // Ensure content is up-to-date from editor
        currentPost.content = editorInstance ? editorInstance.getHtml() : currentPost.content;

        await updatePostAdmin(currentPost.id, {
          title: currentPost.title,
          content: currentPost.content,
          // 移除 categoryId
          // categoryId: currentPost.categoryId
          // Include other editable fields if necessary
        });
        ElMessage.success("更新成功");
          editPostDialogVisible.value = false;
          fetchPosts(); // Refresh list
      } catch (error) {
        console.error("更新帖子失败:", error);
        ElMessage.error("更新帖子失败");
      } finally {
        editSubmitting.value = false;
      }
    }
  });
};

const handleDeletePost = async (row, type = 'soft') => {
  const actionText = type === 'hard' ? '彻底删除' : '删除';
  await ElMessageBox.confirm(`确定要${actionText}帖子 "${row.title}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // TODO: Differentiate between soft and hard delete if backend supports it
      // For now, assume both use the same delete endpoint
      await deletePostAdmin(row.id);
      ElMessage.success(`${actionText}成功`);
      fetchPosts(); // Refresh list
    } catch (error) {
      console.error(`${actionText}帖子失败:`, error);
      ElMessage.error(`${actionText}失败`);
    }
  }).catch(() => {
    ElMessage.info(`已取消${actionText}`);
  });
};

const handleToggleTop = async (row) => {
  const action = row.isTop ? '取消置顶' : '置顶';
  try {
    // Assuming setPostTopAdmin exists and works
    // await setPostTopAdmin(row.id, !row.isTop);
    ElMessage.warning(`"${action}" 功能暂未实现`); // Placeholder
    // fetchPosts(); // Refresh list after successful action
  } catch (error) {
    console.error(`${action}失败:`, error);
    ElMessage.error(`${action}失败`);
  }
};

const handleToggleEssence = async (row) => {
  const action = row.isEssence ? '取消加精' : '加精';
  try {
    // Assuming setPostEssenceAdmin exists and works
    // await setPostEssenceAdmin(row.id, !row.isEssence);
    ElMessage.warning(`"${action}" 功能暂未实现`); // Placeholder
    // fetchPosts(); // Refresh list after successful action
  } catch (error) {
    console.error(`${action}失败:`, error);
    ElMessage.error(`${action}失败`);
  }
};

// Utility Functions
const formatDateTime = (timeStr) => {
  if (!timeStr) return '-';
  try {
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {hour12: false});
  } catch (e) {
    return timeStr;
  }
};

const formatPostStatus = (status) => {
  const statusMap = {
    PUBLISHED: '正常',
    PENDING: '待审核',
    DELETED: '已删除',
    PRIVATE: '私密',
    DRAFT: '草稿'
  };
  return statusMap[status] || status;
};

const getPostStatusType = (status) => {
  switch (status) {
    case 'PUBLISHED':
      return 'success';
    case 'PENDING':
      return 'warning';
    case 'DELETED':
      return 'danger';
    case 'PRIVATE':
      return 'info';
    case 'DRAFT':
      return 'info';
    default:
      return 'info';
  }
};

// Lifecycle Hook
onMounted(() => {
  fetchPosts();
  // 移除 fetchCategories 调用
  // fetchCategories(); 
});

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

.page-header h2 {
  margin: 0;
}

.action-buttons > .el-button {
  margin-left: 10px;
}

.tab-content-card {
  margin-top: 15px;
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
  justify-content: flex-end;
}
</style>