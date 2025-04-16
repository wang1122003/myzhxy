<template>
  <div class="forum-management-container">
    <div class="page-header">
      <h2>论坛管理</h2>
      <!-- 可以添加板块管理等入口 -->
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchParams" @submit.prevent="handleSearch">
        <el-form-item label="关键词">
          <el-input v-model="searchParams.keyword" clearable placeholder="帖子标题/内容/作者" style="width: 250px;"/>
        </el-form-item>
        <el-form-item label="板块/分类">
          <el-input v-model="searchParams.category" clearable placeholder="输入板块名称" style="width: 180px;"/>
          <!-- 或者使用下拉选择已有的板块 -->
          <!-- <el-select v-model="searchParams.category" placeholder="选择板块" clearable style="width: 180px;">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.name" />
          </el-select> -->
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" clearable collapse-tags multiple placeholder="帖子状态"
                     style="width: 200px;">
            <el-option label="置顶" value="isTop"/>
            <el-option label="加精" value="isEssence"/>
            <el-option label="锁定" value="isLocked"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 帖子列表 -->
    <el-card class="post-list-card">
      <el-table v-loading="loading" :data="forumList" style="width: 100%">
        <el-table-column type="expand">
          <template #default="props">
            <div style="padding: 10px 20px;">
              <p><strong>内容预览:</strong></p>
              <p>{{ truncateContent(props.row.content) }}</p>
              <!-- 可以添加查看完整内容或回复列表的链接 -->
            </div>
          </template>
        </el-table-column>
        <el-table-column label="标题" min-width="250" prop="title" show-overflow-tooltip/>
        <el-table-column label="作者" prop="authorName" width="120"/>
        <el-table-column label="板块/分类" prop="category" width="120"/>
        <el-table-column label="状态" width="180">
          <template #default="scope">
            <el-tag v-if="scope.row.isTop" size="small" style="margin-right: 5px;" type="danger">置顶</el-tag>
            <el-tag v-if="scope.row.isEssence" size="small" style="margin-right: 5px;" type="warning">加精</el-tag>
            <el-tag v-if="scope.row.isLocked" size="small" type="info">锁定</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="浏览" prop="viewCount" sortable width="80"/>
        <el-table-column label="点赞" prop="likeCount" sortable width="80"/>
        <el-table-column label="评论" prop="commentCount" sortable width="80"/>
        <el-table-column label="发布时间" prop="createTime" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="280">
          <template #default="scope">
            <el-button size="small" @click="toggleTop(scope.row)">{{
                scope.row.isTop ? '取消置顶' : '置顶'
              }}
            </el-button>
            <el-button size="small" @click="toggleEssence(scope.row)">{{
                scope.row.isEssence ? '取消加精' : '加精'
              }}
            </el-button>
            <el-button size="small" @click="toggleLock(scope.row)">{{
                scope.row.isLocked ? '解锁' : '锁定'
              }}
            </el-button>
            <el-button size="small" type="primary" @click="handleEditPost(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeletePost(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
    </el-card>

    <!-- 编辑帖子对话框 (占位符) -->
    <el-dialog v-model="dialogVisible" :title="isEditMode ? '编辑帖子' : '添加帖子'" top="5vh" width="70%"
               @close="resetForm">
      <el-form ref="postFormRef" :model="postForm" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题"/>
        </el-form-item>
        <el-form-item label="板块/分类" prop="category">
          <el-input v-model="postForm.category" placeholder="请输入或选择板块"/>
          <!-- TODO: 替换为板块选择器 -->
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
              v-model="postForm.content"
              type="textarea"
              :rows="10"
              placeholder="请输入帖子内容"
          />
          <!-- TODO: 替换为富文本编辑器 -->
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPostForm">{{ isEditMode ? '更新' : '创建' }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {
  ElButton,
  ElCard,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag
} from 'element-plus'
import {deleteForum, getForumList, updateForum} from '@/api/forum'

// 修改组件名称为多词组合
defineOptions({
  name: 'ForumManagement'
})

// 修复 ref 值访问问题
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
  category: '',
  content: ''
});
const rules = ref({
  title: [{required: true, message: '请输入帖子标题', trigger: 'blur'}],
  category: [{required: true, message: '请输入或选择板块', trigger: 'blur'}],
  content: [{required: true, message: '请输入帖子内容', trigger: 'blur'}]
});

// 获取帖子列表
const fetchForums = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || null,
      status: searchParams.status
    };
    const res = await getForumList(params);
    forumList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取论坛列表失败", error);
    ElMessage.error("获取论坛列表失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchForums();
};

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchForums();
};

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchForums();
};

// 截断内容预览
const truncateContent = (content) => {
  const maxLength = 100;
  if (!content) return '';
  return content.length > maxLength ? content.substring(0, maxLength) + '...' : content;
}

// 重置表单
const resetForm = () => {
  postFormRef.value?.resetFields();
  postForm.title = '';
  postForm.category = '';
  postForm.content = '';
  currentPostId.value = null;
  isEditMode.value = false;
};

// --- 操作按钮逻辑 (占位符或调用API) ---

const toggleTop = async (row) => {
  const targetStatus = !row.isTop;
  try {
    await updateForum(row.id, {isTop: targetStatus});
    ElMessage.success(targetStatus ? '置顶成功' : '取消置顶成功');
    row.isTop = targetStatus; // 直接更新前端状态
  } catch (error) {
    ElMessage.error('操作失败');
    }
};

const toggleEssence = async (row) => {
  const targetStatus = !row.isEssence;
  try {
    await updateForum(row.id, {isEssence: targetStatus});
    ElMessage.success(targetStatus ? '加精成功' : '取消加精成功');
    row.isEssence = targetStatus;
  } catch (error) {
    ElMessage.error('操作失败');
    }
};

const toggleLock = async (row) => {
  const targetStatus = !row.isLocked;
  try {
    await updateForum(row.id, {isLocked: targetStatus});
    ElMessage.success(targetStatus ? '锁定成功' : '解锁成功');
    row.isLocked = targetStatus;
  } catch (error) {
    ElMessage.error('操作失败');
    }
};

const handleEditPost = (row) => {
  dialogVisible.value = true;
  isEditMode.value = true;
  currentPostId.value = row.id;
  postForm.title = row.title;
  postForm.category = row.category;
  postForm.content = row.content;
};

const handleDeletePost = (row) => {
  ElMessageBox.confirm(`确定要删除帖子 "${row.title}" 吗? 这将同时删除帖子的所有回复。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteForum(row.id);
      ElMessage.success('删除成功');
      fetchForums(); // 刷新列表
    } catch (error) {
      console.error("删除帖子失败", error);
      ElMessage.error("删除帖子失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const submitPostForm = async () => {
  if (!postFormRef.value) return;
  await postFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const postData = {
          title: postForm.title,
          category: postForm.category,
          content: postForm.content,
        };

        if (isEditMode.value && currentPostId.value) {
          await updateForum(currentPostId.value, postData);
          ElMessage.success('帖子更新成功');
          dialogVisible.value = false;
          // 找到列表中对应的项并更新，避免全量刷新
          const index = forumList.value.findIndex(p => p.id === currentPostId.value);
          if (index !== -1) {
            forumList.value[index] = {...forumList.value[index], ...postData};
          } else {
            fetchForums(); // 如果找不到，还是刷新一下列表
          }
        } else {
          ElMessage.info('添加帖子功能待实现');
        }
      } catch (error) {
        console.error("提交帖子失败", error);
        ElMessage.error(isEditMode.value ? '更新帖子失败' : '添加帖子失败');
      }
    }
  });
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-';
  try {
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {hour12: false});
  } catch (e) {
    return timeStr;
  }
};

// 组件挂载后加载数据
onMounted(() => {
  fetchForums();
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