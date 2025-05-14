<template>
  <PageContainer :show-footer="false" title="论坛管理">
    <template #actions>
      <el-button :icon="Plus" type="primary" @click="handleAddPost">
          发布帖子
        </el-button>
      <!-- 可以在这里添加其他全局操作 -->
    </template>

    <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
      <el-tab-pane label="帖子管理" name="post">
          <!-- 帖子筛选 -->
        <FilterForm
            :items="postFilterItems"
            :model="postFilters"
            :show-add-button="false"
            @reset="handlePostReset"
            @search="handlePostSearch"
        />

        <!-- 帖子表格 -->
        <TableView
            :action-column-config="postActionColumnConfig"
            :columns="postTableColumns"
            :data="postList"
            :enable-selection="true"
              v-model:current-page="postCurrentPage"
              v-model:page-size="postPageSize"
            :loading="postLoading"
            :show-action-column="true"
            :total="postTotal"
            @edit="handleEditPost"
            @refresh="fetchPosts"
            @view="handleViewPost"
            @selection-change="handlePostSelectionChange"
          />
      </el-tab-pane>

      <el-tab-pane label="评论管理" name="comment">
        <!-- 内联 CommentManagement 的内容 -->
        <div class="comment-management-container">
          <div class="header"
               style="margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
            <!-- <h3>评论管理</h3> -->
            <div class="filter-actions">
              <el-select v-model="commentFilterStatus" clearable placeholder="评论状态"
                         @change="handleCommentFilterChange">
                <el-option label="全部" value=""/>
                <el-option :value="1" label="正常"/>
                <el-option :value="0" label="待审核"/>
                <el-option :value="-1" label="已删除"/>
              </el-select>
              <el-input
                  v-model="commentSearchKeyword"
                  clearable
                  placeholder="搜索评论内容"
                  style="width: 200px; margin-left: 10px;"
                  @keyup.enter="handleCommentSearch"
              >
                <template #suffix>
                  <el-icon class="el-input__icon" @click="handleCommentSearch">
                    <Search/>
                  </el-icon>
                </template>
              </el-input>
            </div>
          </div>

          <el-table
              v-loading="commentLoading"
              :data="comments"
              border
              style="width: 100%"
          >
            <el-table-column type="expand">
              <template #default="props">
                <div class="comment-detail" style="padding: 10px 20px;">
                  <p><strong>评论内容：</strong></p>
                  <p>{{ props.row.content }}</p>
                  <p v-if="props.row.postTitle"><strong>帖子标题：</strong> {{ props.row.postTitle }}</p>
                  <p v-if="props.row.parentId"><strong>回复对象：</strong> {{ props.row.replyToName }}</p>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="评论内容" min-width="200" show-overflow-tooltip>
              <template #default="scope">
                {{ truncateCommentContent(scope.row.content) }}
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
                {{ formatDateTime(scope.row.createTime) }}
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
                    @click="viewCommentPost(scope.row)"
                >
                  查看帖子
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页控件 -->
          <div v-if="commentTotal > 0" class="pagination-container"
               style="margin-top: 20px; display: flex; justify-content: flex-end;">
            <el-pagination
                v-model:current-page="commentCurrentPage"
                v-model:page-size="commentPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="commentTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleCommentSizeChange"
                @current-change="handleCommentCurrentChange"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 移除分类管理 Tab -->
      <!-- <el-tab-pane label="分类管理" name="category">
        <CategoryManagement/>
      </el-tab-pane> -->
    </el-tabs>

    <!-- 编辑帖子对话框 -->
    <DialogWrapper v-model:visible="editPostDialogVisible" :close-on-click-modal="false" title="编辑帖子" width="70%">
      <el-form ref="editPostFormRef" :model="currentPost" :rules="editPostRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="currentPost.title" placeholder="请输入标题"/>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <RichTextEditor v-model="currentPost.content" @onCreated="handleEditorReady"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editPostDialogVisible = false">取消</el-button>
          <el-button :loading="editSubmitting" type="primary" @click="submitEditPost">保存</el-button>
        </span>
      </template>
    </DialogWrapper>

    <!-- 查看帖子对话框 (移除，因为原组件已删除) -->
    <!-- <NoticeDetailDialog v-model="viewPostDialogVisible" :notice-id="currentPostId"/> -->

  </PageContainer>
</template>

<script setup>
import {onBeforeUnmount, onMounted, reactive, ref, computed, watch, h, resolveComponent} from 'vue'
import {
  ElButton,
  ElCard,
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
import {ArrowDown, ChatDotRound, Plus, Refresh, Search, Menu} from '@element-plus/icons-vue'
// Assume API functions are correctly defined
import {
  deletePost as deletePostAdmin,
  getPosts as getPostsAdmin,
  updatePost as updatePostAdmin,
  setPostTop,
  setPostEssence,
  getCommentsAdmin,
  updateCommentStatusAdmin
} from '@/api/post'
// Import child components if they exist and are used
// 移除 CategoryManagement 导入
// import CategoryManagement from '@/components/forum/CategoryManagement.vue' // Corrected path
// import CommentManagement from '@/components/forum/CommentManagement.vue' // Corrected path
import RichTextEditor from '@/views/ui/RichTextEditor.vue' // 更新路径
// import NoticeDetailDialog from '@/components/common/NoticeDetailDialog.vue' 
import {formatDateTime} from '@/utils/formatters' // Corrected import path
import {useRouter} from 'vue-router'
import DialogWrapper from '@/views/ui/DialogWrapper.vue';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import FilterForm from '@/views/ui/AdvancedFilterForm.vue';
import TableView from '@/views/ui/TableView.vue';

// Tab state
const activeTab = ref('post');

// --- Post Management State ---
const postLoading = ref(false);
const postList = ref([]);
const postTotal = ref(0);
const postCurrentPage = ref(1);
const postPageSize = ref(10);
const postFilters = reactive({
  keyword: '',
  status: ''
});
const selectedPosts = ref([]); // 用于存储表格选中的行

// --- Comment Management State (Migrated) ---
const commentLoading = ref(false);
const comments = ref([]);
const commentTotal = ref(0);
const commentCurrentPage = ref(1);
const commentPageSize = ref(10);
const commentFilterStatus = ref('');
const commentSearchKeyword = ref('');

// --- Dialog and Form State ---
const editPostDialogVisible = ref(false);
const editSubmitting = ref(false);
const currentPost = reactive({id: null, title: '', content: ''});
const currentPostId = ref(null); // 用于查看详情
const editPostFormRef = ref(null);
let editorInstance = null; // 存储富文本编辑器实例

// --- Computed Properties for Components ---

// 帖子状态选项
const POST_STATUS_OPTIONS = [
  {label: '全部', value: ''},
  {label: '正常', value: 'PUBLISHED'},
  {label: '待审核', value: 'PENDING'},
  {label: '已删除', value: 'DELETED'},
  {label: '私密', value: 'PRIVATE'}
];

// 帖子筛选表单配置
const postFilterItems = computed(() => [
  {type: 'input', label: '关键词', prop: 'keyword', placeholder: '标题/内容/作者', props: {clearable: true}},
  {
    type: 'select',
    label: '状态',
    prop: 'status',
    placeholder: '选择状态',
    options: POST_STATUS_OPTIONS,
    props: {clearable: true}
  }
]);

// 帖子表格列配置
const postTableColumns = computed(() => [
  {type: 'selection', width: 55}, // 选择列
  {prop: 'title', label: '标题', minWidth: 200, showOverflowTooltip: true},
  {prop: 'authorName', label: '作者', width: 120},
  {
    label: '浏览/评论/点赞', width: 150,
    formatter: (row) => `${row.viewCount || 0} / ${row.commentCount || 0} / ${row.likeCount || 0}`
  },
  {
    prop: 'status', label: '状态', width: 100,
    slots: {
      default: (scope) =>
          h(resolveComponent('ElTag'), {
            type: getPostStatusType(scope.row.status)
          }, () => formatPostStatus(scope.row.status))
    }
  },
  {
    label: '置顶/精华', width: 100,
    slots: {
      default: (scope) => [
        scope.row.isTop ? h(resolveComponent('ElTag'), {size: 'small', style: 'margin-right: 5px;'}, () => '顶') : null,
        scope.row.isEssence ? h(resolveComponent('ElTag'), {size: 'small', type: 'warning'}, () => '精') : null
      ].filter(v => v !== null) // Filter out nulls if only one tag is present
    }
  },
  {
    prop: 'createTime', label: '发布时间', width: 160,
    formatter: (row) => formatDateTime(row.createTime)
  }
]);

// 帖子表格操作列配置
const postActionColumnConfig = computed(() => ({
  width: 280,
  fixed: 'right',
  buttons: [
    {label: '查看', type: 'primary', size: 'small', event: 'view'},
    {label: '编辑', size: 'small', event: 'edit'},
    {
      label: '更多',
      size: 'small',
      isDropdown: true,
      items: [
        {label: (row) => row.isTop ? '取消置顶' : '置顶', command: 'toggleTop'},
        {label: (row) => row.isEssence ? '取消加精' : '加精', command: 'toggleEssence'},
        {label: '删除', command: 'deleteSoft', divided: true},
        {label: '彻底删除', command: 'deleteHard', divided: true},
      ],
      onCommand: handleMoreCommand, // 处理下拉菜单命令
    }
  ]
}));

// --- Methods ---
const router = useRouter();

// Fetch Posts
const fetchPosts = async () => {
  postLoading.value = true;
  try {
    const params = {
      page: postCurrentPage.value,
      size: postPageSize.value,
      ...postFilters
    };
    const res = await getPostsAdmin(params);
    if (res && res.data) {
      postList.value = res.data.records || [];
      postTotal.value = res.data.total || 0;
    } else {
      postList.value = [];
      postTotal.value = 0;
      // ElMessage.error('获取帖子列表失败，响应数据格式不正确'); // Optional: user notification
    }
  } catch (error) {
    console.error("获取帖子列表失败:", error);
    // 错误由拦截器处理
  } finally {
    postLoading.value = false;
  }
};

// Fetch Comments
const fetchComments = async () => {
  commentLoading.value = true;
  try {
    const params = {
      page: commentCurrentPage.value,
      size: commentPageSize.value,
      status: commentFilterStatus.value !== '' ? commentFilterStatus.value : null,
      keyword: commentSearchKeyword.value || null
    };
    const res = await getCommentsAdmin(params);
    if (res.code === 200 && res.data) {
      // 兼容不同分页格式
      comments.value = res.data.records || res.data.rows || res.data.list || [];
      commentTotal.value = res.data.total || 0;
    } else {
      ElMessage.error(res.message || '获取评论列表失败');
      comments.value = [];
      commentTotal.value = 0;
    }
  } catch (error) {
    console.error('获取评论列表失败:', error);
    ElMessage.error('获取评论列表失败');
    comments.value = [];
    commentTotal.value = 0;
  } finally {
    commentLoading.value = false;
  }
};

// Handle Tab Change
const handleTabChange = (tab) => {
  activeTab.value = tab;
  if (activeTab.value === 'post' && postList.value.length === 0) {
    fetchPosts();
  }
  if (activeTab.value === 'comment' && comments.value.length === 0) {
    fetchComments();
  }
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
  currentPost.id = null;
  currentPost.title = '';
  currentPost.content = '';
  editPostDialogVisible.value = true; // 使用同一个编辑框
  if (editorInstance) {
    editorInstance.setHtml('');
  }
};

const handleViewPost = (row) => {
  currentPostId.value = row.id;
  // viewPostDialogVisible.value = true;
};

const handleEditPost = (row) => {
  currentPost.id = row.id;
  currentPost.title = row.title;
  currentPost.content = row.content || ''; // 加载内容
  editPostDialogVisible.value = true;
};

const submitEditPost = async () => {
  if (!editPostFormRef.value) return;
  await editPostFormRef.value.validate(async (valid) => {
    if (valid) {
      editSubmitting.value = true;
      try {
        const postData = {...currentPost};
        await updatePostAdmin(postData.id, {title: postData.title, content: postData.content});
        ElMessage.success('保存成功');
          editPostDialogVisible.value = false;
        fetchPosts(); // 刷新列表
      } catch (error) {
        console.error("保存帖子失败:", error);
        // 错误由拦截器处理
      } finally {
        editSubmitting.value = false;
      }
    } else {
      ElMessage.warning("请检查表单项")
      return false;
    }
  });
};

const handleDeletePost = (row, type = 'soft') => {
  const confirmMessage = type === 'hard'
      ? `确定要彻底删除帖子【${row.title}】吗？此操作不可恢复！`
      : `确定要删除帖子【${row.title}】吗？`;
  const confirmTitle = type === 'hard' ? '警告：彻底删除' : '确认删除';

  ElMessageBox.confirm(confirmMessage, confirmTitle, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deletePostAdmin(row.id, type); // 假设 API 支持 type 参数 ('soft'/'hard')
      ElMessage.success('删除成功');
      fetchPosts(); // 刷新列表
    } catch (error) {
      console.error("删除帖子失败:", error);
      // 错误由拦截器处理
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const handleToggleTop = async (row) => {
  try {
    await setPostTop(row.id, !row.isTop);
    ElMessage.success(`${!row.isTop ? '置顶' : '取消置顶'}成功`);
    fetchPosts(); // 刷新列表
  } catch (error) {
    console.error("操作失败:", error);
  }
};

const handleToggleEssence = async (row) => {
  try {
    await setPostEssence(row.id, !row.isEssence);
    ElMessage.success(`${!row.isEssence ? '加精' : '取消加精'}成功`);
    fetchPosts(); // 刷新列表
  } catch (error) {
    console.error("操作失败:", error);
  }
};

// Utility Functions
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
  if (activeTab.value === 'comment') {
    fetchComments();
  }
});

// 帖子筛选搜索
const handlePostSearch = () => {
  postCurrentPage.value = 1;
  fetchPosts();
};

// 帖子筛选重置
const handlePostReset = () => {
  postFilters.keyword = '';
  postFilters.status = '';
  postCurrentPage.value = 1;
  fetchPosts();
};

// 处理帖子表格选中项变化
const handlePostSelectionChange = (selection) => {
  selectedPosts.value = selection;
  // console.log('Selected posts:', selection);
};

// 处理更多操作下拉菜单命令
const handleMoreCommand = async (command, row) => {
  switch (command) {
    case 'toggleTop':
      await handleToggleTop(row);
      break;
    case 'toggleEssence':
      await handleToggleEssence(row);
      break;
    case 'deleteSoft':
      await handleDeletePost(row, 'soft');
      break;
    case 'deleteHard':
      await handleDeletePost(row, 'hard');
      break;
  }
};

// Editor instance for RichTextEditor
const handleEditorReady = (editor) => {
  editorInstance = editor;
};

// 监听分页变化 (TableView 内部处理)
watch([postCurrentPage, postPageSize], () => {
  fetchPosts();
}, {immediate: false});

// 清理编辑器实例
onBeforeUnmount(() => {
  if (editorInstance) {
    editorInstance.destroy();
    editorInstance = null;
  }
});

// --- Comment Methods (Migrated and Renamed) ---
const truncateCommentContent = (content) => {
  if (!content) return '';
  const maxLength = 50;
  return content.length > maxLength ? content.substring(0, maxLength) + '...' : content;
};

const handleApproveComment = async (comment) => {
  try {
    await updateCommentStatusAdmin(comment.id, comment.postId, 1);
    ElMessage.success('评论已通过审核');
    fetchComments(); // 刷新列表
  } catch (error) {
    console.error('审核评论失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '操作失败');
  }
};

const handleDeleteComment = (comment) => {
  ElMessageBox.confirm(`确定要删除此评论吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateCommentStatusAdmin(comment.id, comment.postId, -1);
      ElMessage.success('评论已删除');
      fetchComments(); // 刷新列表
    } catch (error) {
      console.error('删除评论失败:', error);
      ElMessage.error(error.response?.data?.message || error.message || '删除失败');
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const handleRestoreComment = async (comment) => {
  try {
    await updateCommentStatusAdmin(comment.id, comment.postId, 1);
    ElMessage.success('评论已恢复');
    fetchComments(); // 刷新列表
  } catch (error) {
    console.error('恢复评论失败:', error);
    ElMessage.error(error.response?.data?.message || error.message || '操作失败');
  }
};

const viewCommentPost = (comment) => {
  if (comment.postId) {
    // 路由到帖子详情页
    // window.open(`/forum/post/${comment.postId}`, '_blank'); // 或使用 router.push
    router.push(`/forum/post/${comment.postId}`);
  } else {
    ElMessage.warning('无法获取帖子信息');
  }
};

const handleCommentFilterChange = () => {
  commentCurrentPage.value = 1;
  fetchComments();
};

const handleCommentSearch = () => {
  commentCurrentPage.value = 1;
  fetchComments();
};

const handleCommentSizeChange = (val) => {
  commentPageSize.value = val;
  commentCurrentPage.value = 1;
  fetchComments();
};

const handleCommentCurrentChange = (val) => {
  commentCurrentPage.value = val;
  fetchComments();
};

// Watch for tab changes to load comments if needed
watch(activeTab, (newTab) => {
  if (newTab === 'comment' && comments.value.length === 0) { // Only load if comments haven't been loaded yet
    fetchComments();
  }
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

.dialog-footer {
  text-align: right;
}
</style>