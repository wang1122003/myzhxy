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

    <el-tabs v-model="activeTab" type="border-card" @tab-click="handleTabClick">
      <el-tab-pane label="帖子管理" name="posts">
        <!-- 搜索和筛选 -->
        <el-card class="filter-card">
          <el-form
              :inline="true"
              :model="searchParams"
              @submit.prevent="handleSearch"
          >
            <el-form-item label="关键词">
              <el-input
                  v-model="searchParams.keyword"
                  clearable
                  placeholder="帖子标题/内容/作者"
                  style="width: 250px;"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                  v-model="searchParams.status"
                  clearable
                  collapse-tags
                  multiple
                  placeholder="帖子状态"
                  style="width: 200px;"
              >
                <el-option
                    label="置顶"
                    value="isTop"
                />
                <el-option
                    label="加精"
                    value="isEssence"
                />
                <el-option
                    label="锁定"
                    value="isLocked"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button
                  type="primary"
                  @click="handleSearch"
              >
                查询
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 帖子列表 -->
        <el-card class="post-list-card">
          <el-table
              v-loading="loading"
              :data="forumList"
              style="width: 100%"
          >
            <el-table-column type="expand">
              <template #default="props">
                <div style="padding: 10px 20px;">
                  <p><strong>内容预览:</strong></p>
                  <p>{{ truncateContent(props.row.content) }}</p>
                  <!-- 可以添加查看完整内容或回复列表的链接 -->
                </div>
              </template>
            </el-table-column>
            <el-table-column
                label="标题"
                min-width="250"
                prop="title"
                show-overflow-tooltip
            />
            <el-table-column
                label="作者"
                prop="authorName"
                width="120"
            />
            <el-table-column
                label="板块/分类"
                prop="category"
                width="120"
            />
            <el-table-column
                label="状态"
                width="180"
            >
              <template #default="scope">
                <el-tag
                    v-if="scope.row.isTop"
                    size="small"
                    style="margin-right: 5px;"
                    type="danger"
                >
                  置顶
                </el-tag>
                <el-tag
                    v-if="scope.row.isEssence"
                    size="small"
                    style="margin-right: 5px;"
                    type="warning"
                >
                  加精
                </el-tag>
                <el-tag
                    v-if="scope.row.isLocked"
                    size="small"
                    type="info"
                >
                  锁定
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
                label="浏览"
                prop="viewCount"
                sortable
                width="80"
            />
            <el-table-column
                label="点赞"
                prop="likeCount"
                sortable
                width="80"
            />
            <el-table-column
                label="评论"
                prop="commentCount"
                sortable
                width="80"
            />
            <el-table-column
                label="发布时间"
                prop="createTime"
                width="180"
            >
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                width="280"
            >
              <template #default="scope">
                <el-button
                    size="small"
                    @click="toggleTop(scope.row)"
                >
                  {{
                    scope.row.isTop ? '取消置顶' : '置顶'
                  }}
                </el-button>
                <el-button
                    size="small"
                    @click="toggleEssence(scope.row)"
                >
                  {{
                    scope.row.isEssence ? '取消加精' : '加精'
                  }}
                </el-button>
                <el-button
                    size="small"
                    @click="toggleLock(scope.row)"
                >
                  {{
                    scope.row.isLocked ? '解锁' : '锁定'
                  }}
                </el-button>
                <el-button
                    size="small"
                    type="primary"
                    @click="handleEditPost(scope.row)"
                >
                  编辑
                </el-button>
                <el-button
                    size="small"
                    type="danger"
                    @click="handleDeletePost(scope.row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div
              v-if="total > 0"
              class="pagination-container"
          >
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
        </el-card>
      </el-tab-pane>

      <!-- 新增的管理标签页 -->
      <el-tab-pane label="板块管理" name="categories">
        <CategoryManagement @refresh="fetchForumCategories"/>
      </el-tab-pane>

      <el-tab-pane label="评论管理" name="comments">
        <CommentManagement/>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑帖子对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="dialogTitle"
        top="5vh"
        width="80%"
        @close="handleDialogClose"
    >
      <el-form
          ref="postFormRef"
          :model="postForm"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item
            label="标题"
            prop="title"
        >
          <el-input
              v-model="postForm.title"
              placeholder="请输入帖子标题"
          />
        </el-form-item>
        <el-form-item
            label="板块/分类"
            prop="forumId"
        >
          <el-select
              v-model="postForm.forumId"
              :loading="loadingCategories"
              filterable
              placeholder="选择板块"
              style="width: 100%;"
          >
            <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
            label="内容"
            prop="content"
        >
          <!-- Replace el-input with WangEditor -->
          <div style="border: 1px solid #ccc; width: 100%;">
            <Toolbar
                :default-config="toolbarConfig"
                :editor="editorRef"
                mode="default"
                style="border-bottom: 1px solid #ccc"
            />
            <Editor
                v-model="postForm.content"
                :default-config="editorConfig"
                mode="default"
                style="height: 400px; overflow-y: hidden;"
                @onCreated="handleEditorCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
              :loading="submitting"
              type="primary"
              @click="submitPostForm"
          >{{ dialogTitle }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, shallowRef} from 'vue'
import {
  ElButton,
  ElCard,
  ElDialog,
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
import {ChatDotRound, Menu, Plus} from '@element-plus/icons-vue'
import {createPost, deletePost, getForumCategories, searchPosts, updatePost} from '@/api/forum'
import {getToken} from '@/utils/auth'
import '@wangeditor/editor/dist/css/style.css'
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import CategoryManagement from '@/components/forum/CategoryManagement.vue'
import CommentManagement from '@/components/forum/CommentManagement.vue'

const forumList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  keyword: '',
  status: null
});

const loading = ref(false);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const currentPostId = ref(null);
const postFormRef = ref(null);
const postForm = reactive({
  title: '',
  forumId: null,
  content: ''
});
const rules = ref({
  title: [{required: true, message: '请输入帖子标题', trigger: 'blur'}],
  forumId: [{required: true, message: '请选择板块', trigger: 'change'}],
  content: [
    {required: true, message: '请输入帖子内容', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        const editor = editorRef.value;
        if (editor && editor.isEmpty()) {
          callback(new Error('请输入帖子内容'));
        } else {
          callback();
        }
      }, trigger: 'blur'
    }
  ]
});

const submitting = ref(false);

const editorRef = shallowRef();
const toolbarConfig = {};
const editorConfig = {
  placeholder: '请输入帖子内容...',
  MENU_CONF: {
    uploadImage: {
      server: '/api/forum/posts/upload/image',
      fieldName: 'file',
      headers: {Authorization: `Bearer ${getToken()}`},
      customInsert(res, insertFn) {
        if (res.code === 200 && res.data) {
          let imageUrl = res.data;
          if (imageUrl && !imageUrl.startsWith('http') && !imageUrl.startsWith('/')) {
            imageUrl = `${window.location.origin}/${imageUrl}`;
          } else if (imageUrl && imageUrl.startsWith('/uploads')) {
            imageUrl = `${window.location.origin}${imageUrl}`;
          }
          insertFn(imageUrl, 'image', imageUrl);
        } else {
          ElMessage.error(res.message || '图片上传失败');
        }
      },
      onError(file, err, res) {
        console.error('上传图片错误:', err, res);
        ElMessage.error('上传图片错误: ' + (res?.message || err.message));
      },
    }
  }
};

const handleEditorCreated = (editor) => {
  editorRef.value = editor
};

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
});

const categories = ref([]);
const loadingCategories = ref(false);

const fetchForumCategories = async () => {
  loadingCategories.value = true;
  try {
    const res = await getForumCategories();
    if (res.success && Array.isArray(res.data)) {
      categories.value = res.data;
    } else if (Array.isArray(res)) {
      categories.value = res;
    } else {
      console.warn("获取板块列表返回的数据格式不符合预期:", res);
      categories.value = [];
      ElMessage.warning('获取板块列表数据格式有误');
    }
  } catch (error) {
    console.error("获取板块列表失败:", error);
    ElMessage.error('获取板块列表失败');
    categories.value = [];
  } finally {
    loadingCategories.value = false;
  }
};

const fetchPosts = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || null,
    };
    if (searchParams.status && searchParams.status.length > 0) {
      params.isTop = searchParams.status.includes('isTop') ? true : undefined;
      params.isEssence = searchParams.status.includes('isEssence') ? true : undefined;
      params.isLocked = searchParams.status.includes('isLocked') ? true : undefined;
    }

    const res = await searchPosts(params); 
    forumList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取论坛列表失败", error);
    ElMessage.error("获取论坛列表失败");
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchPosts();
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchPosts();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchPosts();
};

const truncateContent = (content) => {
  const maxLength = 100;
  if (!content) return '';
  return content.length > maxLength ? content.substring(0, maxLength) + '...' : content;
}

const handleDialogClose = () => {
  if (postFormRef.value) {
    postFormRef.value.resetFields();
  }
  postForm.title = '';
  postForm.forumId = null;
  postForm.content = '';
  if (editorRef.value) {
    editorRef.value.setHtml('');
  }
  currentPostId.value = null;
  isEditMode.value = false;
  submitting.value = false;
};

const toggleTop = async (row) => {
  const targetStatus = !row.isTop;
  try {
    await updatePost(row.id, {isTop: targetStatus});
    ElMessage.success(targetStatus ? '置顶成功' : '取消置顶成功');
    row.isTop = targetStatus;
  } catch (error) {
    ElMessage.error('操作失败');
    }
};

const toggleEssence = async (row) => {
  const targetStatus = !row.isEssence;
  try {
    await updatePost(row.id, {isEssence: targetStatus});
    ElMessage.success(targetStatus ? '加精成功' : '取消加精成功');
    row.isEssence = targetStatus;
  } catch (error) {
    ElMessage.error('操作失败');
    }
};

const toggleLock = async (row) => {
  const targetStatus = !row.isLocked;
  try {
    await updatePost(row.id, {isLocked: targetStatus});
    ElMessage.success(targetStatus ? (row.isLocked ? '解锁成功' : '锁定成功') : (row.isLocked ? '锁定失败' : '解锁失败'));
    row.isLocked = targetStatus;
  } catch (error) {
    ElMessage.error('操作失败');
    }
};

const handleEditPost = async (row) => {
  isEditMode.value = true;
  dialogVisible.value = true;
  currentPostId.value = row.id;

  if (postFormRef.value) postFormRef.value.clearValidate();
  postForm.title = '';
  postForm.forumId = null;
  postForm.content = '';
  if (editorRef.value) editorRef.value.setHtml('');

  await fetchForumCategories();
  dialogVisible.value = true;

  postForm.title = row.title;
  postForm.forumId = row.forumId;

  await nextTick();
  if (editorRef.value) {
    editorRef.value.setHtml(row.content || '');
  } else {
    postForm.content = row.content || '';
  }
};

const handleDeletePost = (row) => {
  ElMessageBox.confirm(`确定要删除帖子 "${row.title}" 吗? 这将同时删除帖子的所有回复。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePost(row.id);
      ElMessage.success('删除成功');
      fetchPosts();
    } catch (error) {
      console.error("删除帖子失败", error);
      ElMessage.error("删除帖子失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const handleAddPost = () => {
  isEditMode.value = false;
  currentPostId.value = null;
  if (postFormRef.value) {
    postFormRef.value.resetFields();
  }
  postForm.title = '';
  postForm.forumId = null;
  postForm.content = '';
  if (editorRef.value) {
    editorRef.value.setHtml('');
  }
  fetchForumCategories();
  dialogVisible.value = true;
};

const dialogTitle = computed(() => (isEditMode.value ? '编辑帖子' : '发布新帖子'));

const submitPostForm = async () => {
  if (!postFormRef.value) return;
  await postFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const editor = editorRef.value;
        if (!editor) throw new Error("编辑器未初始化");

        const postData = {
          title: postForm.title,
          forumId: postForm.forumId,
          content: editor.getHtml(),
        };

        if (isEditMode.value && currentPostId.value) {
          await updatePost(currentPostId.value, postData);
          ElMessage.success('帖子更新成功');
        } else {
          await createPost(postData);
          ElMessage.success('帖子发布成功');
        }
        dialogVisible.value = false;
        fetchPosts();
      } catch (error) {
        console.error("提交帖子失败", error);
        ElMessage.error(isEditMode.value ? '更新帖子失败' : '发布帖子失败');
      } finally {
        submitting.value = false;
      }
    }
  });
};

const formatTime = (timeStr) => {
  if (!timeStr) return '-';
  try {
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {hour12: false});
  } catch (e) {
    return timeStr;
  }
};

// 标签页配置
const activeTab = ref('posts') // 默认显示帖子管理

// 处理标签页切换
const handleTabClick = (tab) => {
  console.log('当前标签页:', tab.props.name)
}

onMounted(() => {
  fetchForumCategories();
  handleSearch();
});

</script>

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

.filter-card {
  margin-bottom: 20px;
}

.post-list-card {
  /* 样式 */
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