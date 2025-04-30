<template>
  <div class="notice-management-container">
    <el-card class="page-container">
      <template #header>
        <div class="header">
          <span>公告管理</span>
          <el-button :icon="Plus" type="primary" @click="handleAdd">发布公告</el-button>
        </div>
      </template>

      <!-- 筛选区域 -->
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="标题">
          <el-input v-model="filters.title" clearable placeholder="请输入公告标题关键词"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="请选择状态">
            <el-option label="全部" value=""/>
            <el-option label="已发布" value="PUBLISHED"/>
            <el-option label="草稿" value="DRAFT"/>
            <el-option label="已撤回" value="RECALLED"/>
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.type" clearable placeholder="请选择类型">
            <el-option label="全部" value=""/>
            <el-option label="系统通知" value="SYSTEM"/>
            <el-option label="教学通知" value="ACADEMIC"/>
            <el-option label="活动通知" value="ACTIVITY"/>
            <el-option label="其他通知" value="OTHER"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="fetchNotices">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 公告列表 -->
      <el-table
          v-loading="loading"
          :data="noticeList"
          style="width: 100%"
      >
        <el-table-column label="标题" min-width="200" prop="title" show-overflow-tooltip/>
        <el-table-column label="类型" prop="type" width="120">
          <template #default="scope">{{ formatNoticeType(scope.row.type) }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getNoticeStatusType(scope.row.status)">
              {{ formatNoticeStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布者" prop="publisherName" width="120"/>
        <el-table-column label="发布时间" prop="publishTime" width="160">
          <template #default="scope">{{ formatDateTime(scope.row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="阅读量" prop="readCount" width="80"/>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleView(scope.row)">查看</el-button>
            <el-button
                :disabled="scope.row.status === 'RECALLED'"
                size="small"
                @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
                v-if="scope.row.status === 'PUBLISHED'"
                size="small"
                type="warning"
                @click="handleRecall(scope.row)"
            >
              撤回
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无公告数据"/>
        </template>
      </el-table>

      <!-- 分页 -->
      <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          class="pagination-container"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 添加/编辑 对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEditMode ? '编辑公告' : '发布公告'"
        :close-on-click-modal="false"
        width="70%"
        top="5vh"
        @close="handleDialogClose"
    >
      <el-form ref="noticeFormRef" :model="currentNotice" :rules="formRules" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="currentNotice.title" placeholder="请输入公告标题"/>
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-select v-model="currentNotice.type" placeholder="请选择公告类型" style="width: 100%;">
            <el-option label="系统通知" value="SYSTEM"/>
            <el-option label="教学通知" value="ACADEMIC"/>
            <el-option label="活动通知" value="ACTIVITY"/>
            <el-option label="其他通知" value="OTHER"/>
          </el-select>
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <RichTextEditor v-model="currentNotice.content" @update:modelValue="handleEditorChange"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="currentNotice.status">
            <el-radio label="PUBLISHED">直接发布</el-radio>
            <el-radio label="DRAFT">存为草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button :loading="submitting" type="primary" @click="handleSubmit">
            {{ isEditMode ? '保存' : '发布' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看公告详情对话框 -->
    <NoticeDetailDialog v-model="viewDialogVisible" :notice-id="currentNoticeId"/>

  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Search} from '@element-plus/icons-vue'
import {
  addNotification,
  deleteNotification,
  getNotificationById,
  getNotificationsPage,
  updateNotification,
  updateNotificationStatus
} from '@/api/notice'
import {formatDateTime} from '@/utils/formatters.js'
import RichTextEditor from '@/components/common/RichTextEditor.vue'
import NoticeDetailDialog from '@/components/common/NoticeDetailDialog.vue'

defineOptions({name: 'AdminNoticeManagement'})

// -------- 数据定义 --------
const loading = ref(false)
const noticeList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const filters = reactive({
  title: '',
  status: '',
  type: ''
})

const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEditMode = ref(false)
const submitting = ref(false)
const currentNotice = ref({
  id: null,
  title: '',
  content: '',
  type: 'SYSTEM',
  status: 'PUBLISHED' // 默认直接发布
})
const currentNoticeId = ref(null)
const noticeFormRef = ref(null) // 表单引用

// -------- 表单验证规则 --------
const formRules = {
  title: [{required: true, message: '请输入公告标题', trigger: 'blur'}],
  type: [{required: true, message: '请选择公告类型', trigger: 'change'}],
  content: [{required: true, message: '请输入公告内容', trigger: 'blur'}], // 可能需要更复杂的验证
  status: [{required: true, message: '请选择状态', trigger: 'change'}]
}

// -------- 方法 --------

// 获取公告列表
const fetchNotices = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      title: filters.title || null,
      status: filters.status || null,
      type: filters.type || null
    }
    const res = await getNotificationsPage(params)
    noticeList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

// 处理富文本编辑器内容变化
const handleEditorChange = (newContent) => {
  if (currentNotice.value) {
    currentNotice.value.content = newContent
  }
}

// 处理添加公告
const handleAdd = () => {
  isEditMode.value = false
  currentNotice.value = {
    id: null,
    title: '',
    content: '',
    type: 'SYSTEM',
    status: 'PUBLISHED'
  }
  dialogVisible.value = true
  // 重置表单校验状态
  noticeFormRef.value?.resetFields()
}

// 处理编辑公告
const handleEdit = async (row) => {
  isEditMode.value = true
  resetForm()
  try {
    const res = await getNotificationById(row.id)
    if (res.code === 200 && res.data) {
      currentNotice.value = {...res.data}
      dialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取公告详情失败')
    }
  } catch (error) {
    console.error('获取公告详情失败', error)
    ElMessage.error('获取公告详情失败')
  }
}

// 处理查看公告
const handleView = (row) => {
  currentNoticeId.value = row.id
  viewDialogVisible.value = true
}

// 处理撤回公告
const handleRecall = async (row) => {
  await ElMessageBox.confirm('确定要撤回该公告吗？撤回后将变为草稿状态。 ', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    await updateNotificationStatus(row.id, 'DRAFT')
    ElMessage.success('公告撤回成功')
    fetchNotices() // 刷新列表
  } catch (error) {
    console.error('撤回公告失败:', error)
    ElMessage.error('撤回公告失败')
  }
}

// 处理删除公告
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该公告吗？此操作不可恢复。 ', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    await deleteNotification(row.id)
    ElMessage.success('公告删除成功')
    fetchNotices() // 刷新列表
  } catch (error) {
    console.error('删除公告失败:', error)
    ElMessage.error('删除公告失败')
  }
}

// 处理对话框关闭
const handleDialogClose = () => {
  noticeFormRef.value?.clearValidate() // 关闭时清除校验
}

// 处理提交 (添加/编辑)
const handleSubmit = async () => {
  if (!noticeFormRef.value) return
  await noticeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEditMode.value) {
          // 编辑
          await updateNotification(currentNotice.value.id, currentNotice.value)
          ElMessage.success('公告更新成功')
        } else {
          // 添加
          await addNotification(currentNotice.value)
          ElMessage.success('公告发布成功')
        }
        dialogVisible.value = false
        fetchNotices() // 刷新列表
      } catch (error) {
        console.error('提交公告失败:', error)
        ElMessage.error(isEditMode.value ? '更新公告失败' : '发布公告失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1 // 切换每页条数时，回到第一页
  fetchNotices()
}

// 处理当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchNotices()
}

// -------- 格式化函数 --------

// 格式化状态并返回对应的 ElTag 类型
const getNoticeStatusType = (status) => {
  switch (status) {
    case 'PUBLISHED':
    case 1: // 假设 API 可能返回数字 1 代表已发布
      return 'success';
    case 'DRAFT':
    case 0: // 假设 API 可能返回数字 0 代表草稿
      return 'warning';
    case 'RECALLED': // 假设有撤回状态
      return 'info';
    default:
      return 'info'; // 对于未知或 null 状态，返回默认类型
  }
};

// 格式化状态文本
const formatNoticeStatus = (status) => {
  switch (status) {
    case 'PUBLISHED':
    case 1:
      return '已发布';
    case 'DRAFT':
    case 0:
      return '草稿';
    case 'RECALLED':
      return '已撤回';
    default:
      return status || '未知';
  }
};

// 格式化类型文本
const formatNoticeType = (type) => {
  switch (type) {
    case 'SYSTEM':
      return '系统通知';
    case 'ACADEMIC':
      return '教学通知';
    case 'ACTIVITY':
      return '活动通知';
    case 'OTHER':
      return '其他通知';
    default:
      return type || '未知';
  }
};

// 重置表单函数 (如果需要的话)
const resetForm = () => {
  currentNotice.value = {
    id: null,
    title: '',
    content: '',
    type: 'SYSTEM',
    status: 'PUBLISHED'
  };
  noticeFormRef.value?.resetFields(); // 重置校验状态
};

// -------- 生命周期钩子 --------
onMounted(() => {
  fetchNotices()
})

</script>

<style lang="scss" scoped>
.notice-management-container {
  .page-container {
    min-height: calc(100vh - 110px); // Adjust based on layout
  }

  .header {
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
}
</style>