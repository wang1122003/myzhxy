<template>
  <PageContainer title="通知公告管理">
    <template #header-actions>
      <el-button type="primary" @click="handleAddNotice">
        <el-icon>
          <Plus/>
        </el-icon>
        发布公告
      </el-button>
      </template>

    <template #filters>
      <FilterForm :model="searchParams" @reset="resetSearch" @search="handleSearch">
        <el-form-item label="公告类型">
          <el-select v-model="searchParams.type" clearable placeholder="选择类型" style="width: 150px;">
            <el-option label="全部" value=""/>
            <el-option
                v-for="item in noticeTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchParams.keyword" clearable placeholder="标题/内容" style="width: 220px;"/>
        </el-form-item>
      </FilterForm>
    </template>

    <TableView
          :data="noticeList"
          v-model="pagination"
          :loading="loading"
          :total="total"
          border
          @page-change="handlePageChange"
      >
      <el-table-column label="标题" min-width="180" prop="title" show-overflow-tooltip/>
        <el-table-column label="类型" prop="type" width="120">
          <template #default="scope">
            <el-tag :type="getNoticeTagType(scope.row.type)">
              {{ formatNoticeType(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
      <el-table-column label="发布时间" prop="publishTime" width="160">
        <template #default="scope">
          {{ formatTime(scope.row.publishTime) }}
        </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' || scope.row.status === '1' ? 'success' : 'info'">
              {{ (scope.row.status === 'ACTIVE' || scope.row.status === '1') ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
      <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <div style="display: flex; gap: 5px;">
              <el-button size="small" type="primary" @click="handleViewNotice(scope.row)">
                预览
              </el-button>
              <el-button size="small" type="warning" @click="handleEditNotice(scope.row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteNotice(scope.row)">
                删除
              </el-button>
            </div>
          </template>
      </el-table-column>
    </TableView>

    <!-- 通知详情对话框 -->
    <DialogWrapper
        v-model:visible="noticeDetailVisible"
        :title="currentNotice.title"
        width="60%"
        @close="noticeDetailVisible = false"
    >
      <div v-if="currentNotice.id" v-loading="loadingNoticeDetail">
        <div class="notice-content">
          <div class="notice-info">
            <span>发布时间：{{ formatTime(currentNotice.publishTime) }}</span>
          </div>
          <el-divider/>
          <div class="notice-text" v-html="currentNotice.content"/>
          <div v-if="currentNotice.attachmentFiles && currentNotice.attachmentFiles.length" class="notice-attachments">
            <el-divider/>
            <h4>附件：</h4>
            <ul>
              <li v-for="file in currentNotice.attachmentFiles" :key="file.id">
                <el-link
                    :disabled="downloadingAttachment[file.id]"
                    :loading="downloadingAttachment[file.id]"
                    type="primary"
                    @click="handleDownloadFile(file)"
                >
                  {{ file.name }}
                </el-link>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="noticeDetailVisible = false">关闭</el-button>
        </span>
      </template>
    </DialogWrapper>

    <!-- 添加/编辑公告对话框 -->
    <DialogWrapper
        v-model:visible="noticeFormVisible"
        :title="isEditMode ? '编辑公告' : '发布公告'"
        width="70%"
        @close="resetForm"
    >
      <FormView
          ref="noticeFormRef"
          :model="noticeForm"
          :rules="noticeFormRules"
          :submitting="submitting"
          @cancel="noticeFormVisible = false"
          @submit="submitNoticeForm"
          class="form-container"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题"/>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="noticeForm.type" placeholder="请选择公告类型" style="width: 100%">
            <el-option
                v-for="item in noticeTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <RichTextEditor v-model="noticeForm.content" style="min-height: 300px"/>
        </el-form-item>
        <el-form-item label="附件" prop="attachments">
          <el-upload
              :action="uploadFileUrl"
              :before-upload="beforeUploadFile"
              :file-list="fileList"
              :headers="uploadHeaders"
              :limit="5"
              :on-exceed="handleExceedFiles"
              :on-remove="handleRemoveFile"
              :on-success="handleUploadSuccess"
              class="upload-demo"
              multiple
          >
            <el-button type="primary">
              <el-icon>
                <Upload/>
              </el-icon>
              添加附件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">文件大小不超过 20MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="noticeForm.status">
            <el-radio :value="'ACTIVE'">立即发布</el-radio>
            <el-radio :value="'DRAFT'">保存为草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="置顶" prop="isTop">
          <el-switch
              v-model="noticeForm.isTop"
              :active-value="1"
              :inactive-value="0"
              active-text="是"
              inactive-text="否"
          />
        </el-form-item>
      </FormView>
    </DialogWrapper>
  </PageContainer>
</template>

<script setup>
import {ref, reactive, computed, onMounted} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Upload} from '@element-plus/icons-vue';
import {
  getNotificationsPage,
  getNotificationById,
  addNotification,
  updateNotification,
  deleteNotification,
  updateNotificationStatus,
  updateNotificationTopStatus
} from '@/api/notice';
import {downloadFile} from '@/api/file';
import {useUserStore} from '@/stores/userStore';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import FilterForm from '@/views/ui/AdvancedFilterForm.vue';
import TableView from '@/views/ui/TableView.vue';
import FormView from '@/views/ui/SmartForm.vue';
import RichTextEditor from '@/views/ui/RichTextEditor.vue';
import {formatDateTime, formatFileSize} from '@/utils/formatters';
import DialogWrapper from '@/views/ui/DialogWrapper.vue';

const userStore = useUserStore();

// 数据加载状态
const loading = ref(false);
const loadingNoticeDetail = ref(false);
const submitting = ref(false);
const downloadingAttachment = reactive({});

// 数据列表
const noticeList = ref([]);
const total = ref(0);
const pagination = ref({
  pageNum: 1,
  pageSize: 10
});

// 搜索参数
const searchParams = reactive({
  keyword: '',
  type: ''
});

// 对话框可见性
const noticeDetailVisible = ref(false);
const noticeFormVisible = ref(false);

// 当前查看的通知
const currentNotice = reactive({
  id: null,
  title: '',
  type: '',
  content: '',
  publisherName: '',
  publishTime: '',
  attachmentFiles: []
});

// 表单数据
const noticeFormRef = ref(null);
const noticeForm = reactive({
  id: null,
  title: '',
  type: 'GENERAL',
  content: '',
  status: 'ACTIVE',
  isTop: 0,
  attachmentIds: []
});

// 附件上传
const fileList = ref([]);
const uploadFileUrl = `${import.meta.env.VITE_API_URL}/file/upload`;
const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${userStore.token}`
  };
});

// 表单验证规则
const noticeFormRules = {
  title: [
    {required: true, message: '请输入公告标题', trigger: 'blur'},
    {min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur'}
  ],
  type: [
    {required: true, message: '请选择公告类型', trigger: 'change'}
  ],
  content: [
    {required: true, message: '请输入公告内容', trigger: 'blur'}
  ]
};

// 通知类型选项
const noticeTypeOptions = [
  {value: 'SYSTEM', label: '系统通知'},
  {value: 'ACADEMIC', label: '教学通知'},
  {value: 'ACTIVITY', label: '活动公告'},
  {value: 'FACILITY', label: '设施维护'},
  {value: 'GENERAL', label: '通用公告'}
];

// 计算属性：判断是编辑还是新增模式
const isEditMode = computed(() => !!noticeForm.id);

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  try {
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {hour12: false});
  } catch (e) {
    return timeStr;
  }
};

// 格式化通知类型
const formatNoticeType = (type) => {
  const typeMap = {
    'SYSTEM': '系统通知',
    'ACADEMIC': '教学通知',
    'ACTIVITY': '活动公告',
    'FACILITY': '设施维护',
    'GENERAL': '通用公告'
  };
  return typeMap[type] || type;
};

// 获取通知类型标签样式
const getNoticeTagType = (type) => {
  const foundType = noticeTypeOptions.find(option => option.value === type);
  return foundType ? foundType.tagType : 'info'; // 返回 'info' 作为默认类型
};

// 获取通知列表
const fetchNoticeList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      keyword: searchParams.keyword || undefined,
      type: searchParams.type || undefined
    };

    const res = await getNotificationsPage(params);

    if (res && res.records) {
      noticeList.value = res.records;
      total.value = res.total;

      // 确保状态字段正确
      noticeList.value.forEach(notice => {
        if (notice.status === undefined || notice.status === null) {
          notice.status = 'ACTIVE'; // 默认为已发布状态
        }
      });
    } else {
      noticeList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取通知列表失败', error);
    ElMessage.error('获取通知列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取通知详情
const handleViewNotice = async (row) => {
  noticeDetailVisible.value = true;
  loadingNoticeDetail.value = true;

  // 先清空当前通知详情
  Object.assign(currentNotice, {
    id: null,
    title: '',
    type: '',
    content: '',
    publisherName: '',
    publishTime: '',
    attachmentFiles: []
  });
  
  try {
    const res = await getNotificationById(row.id);
    // 确保isTop字段值类型一致
    if (res.isTop === 0 || res.isTop === 1) {
      res.isTop = Number(res.isTop);
    }
    Object.assign(currentNotice, res);
  } catch (error) {
    console.error('获取通知详情失败', error);
    ElMessage.error('获取通知详情失败');
    noticeDetailVisible.value = false;
  } finally {
    loadingNoticeDetail.value = false;
  }
};

// 添加通知
const handleAddNotice = () => {
  resetForm();
  noticeFormVisible.value = true;
};

// 编辑通知
const handleEditNotice = async (row) => {
  resetForm();
  loadingNoticeDetail.value = true;
  
  try {
    const res = await getNotificationById(row.id);

    // 确保isTop字段值类型一致
    if (res.isTop === 0 || res.isTop === 1) {
      res.isTop = Number(res.isTop);
    }

    // 填充表单数据
    noticeForm.id = res.id;
    noticeForm.title = res.title;
    noticeForm.type = res.type;
    noticeForm.content = res.content;
    noticeForm.status = res.status;
    noticeForm.isTop = res.isTop;

    // 填充附件列表
    fileList.value = (res.attachmentFiles || []).map(file => ({
      name: file.name,
      url: file.url,
      id: file.id
    }));

    noticeForm.attachmentIds = (res.attachmentFiles || []).map(file => file.id);

    noticeFormVisible.value = true;
  } catch (error) {
    console.error('获取通知详情失败', error);
    ElMessage.error('获取通知详情失败');
  } finally {
    loadingNoticeDetail.value = false;
  }
};

// 删除通知
const handleDeleteNotice = (row) => {
  ElMessageBox.confirm(`确定要删除通知 "${row.title}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteNotification(row.id);
      ElMessage.success('删除成功');
      fetchNoticeList();
    } catch (error) {
      console.error('删除通知失败', error);
      ElMessage.error('删除通知失败');
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 切换置顶状态 - 将此函数保留为内部辅助函数，在编辑通知时可能需要使用
const handleToggleTop = async (id, isTop) => {
  try {
    await updateNotificationTopStatus(id, isTop);
    ElMessage.success(`${isTop ? '置顶' : '取消置顶'}成功`);
    // 刷新列表确保显示最新状态
    await fetchNoticeList();
  } catch (error) {
    console.error('操作失败', error);
    ElMessage.error('操作失败');
  }
};

// 表单提交
const submitNoticeForm = async () => {
  submitting.value = true;

  try {
    // 转换表单数据，确保JSON字段正确
    const formData = {
      ...noticeForm,
      attachmentIds: noticeForm.attachmentIds,
      // 发布人ID设为admin用户ID
      publisherId: 1,
      senderId: 1,
      // 如果是新建，设置发送时间为当前时间
      sendTime: isEditMode.value ? undefined : new Date().toISOString()
    };

    if (isEditMode.value) {
      await updateNotification(formData.id, formData);
      ElMessage.success('更新成功');
    } else {
      await addNotification(formData);
      ElMessage.success('发布成功');
    }

    noticeFormVisible.value = false;
    fetchNoticeList();
  } catch (error) {
    console.error('提交表单失败', error);
    ElMessage.error(error.response?.data?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
};

// 重置表单
const resetForm = () => {
  if (noticeFormRef.value) {
    noticeFormRef.value.resetFields();
  }

  Object.assign(noticeForm, {
    id: null,
    title: '',
    type: 'GENERAL',
    content: '',
    status: 'ACTIVE',
    isTop: 0,
    attachmentIds: []
  });

  fileList.value = [];
};

// 下载附件
const handleDownloadFile = async (file) => {
  if (!file || !file.id) {
    ElMessage.warning('无效的文件信息');
    return;
  }

  downloadingAttachment[file.id] = true;
  try {
    await downloadFile(file.id);
    ElMessage.success('下载成功');
  } catch (error) {
    console.error('下载失败', error);
    ElMessage.error('下载失败');
  } finally {
    downloadingAttachment[file.id] = false;
  }
};

// 文件上传前检查
const beforeUploadFile = (file) => {
  const maxSize = 20 * 1024 * 1024; // 20MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 20MB!');
    return false;
  }
  return true;
};

// 上传成功回调
const handleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    noticeForm.attachmentIds.push(response.data.id);
    ElMessage.success('文件上传成功');
  } else {
    ElMessage.error(response.message || '文件上传失败');
  }
};

// 移除文件
const handleRemoveFile = (file, fileList) => {
  // 从附件ID列表中移除
  const fileId = file.id || (file.response && file.response.data && file.response.data.id);
  if (fileId) {
    const index = noticeForm.attachmentIds.indexOf(fileId);
    if (index !== -1) {
      noticeForm.attachmentIds.splice(index, 1);
    }
  }
};

// 文件数量超限
const handleExceedFiles = () => {
  ElMessage.warning('最多只能上传 5 个文件');
};

// 处理搜索
const handleSearch = () => {
  pagination.value.pageNum = 1;
  fetchNoticeList();
};

// 重置搜索
const resetSearch = () => {
  searchParams.keyword = '';
  searchParams.type = '';
  pagination.value.pageNum = 1;
  fetchNoticeList();
};

// 处理分页变化
const handlePageChange = () => {
  fetchNoticeList();
};

// 加载初始数据
onMounted(() => {
  fetchNoticeList();
});
</script>

<style scoped>
.notice-content {
  padding: 16px;
  max-height: 70vh;
  overflow-y: auto;
  position: relative;
  z-index: 10;
}

.notice-info {
  display: flex;
  justify-content: center;
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
}

.notice-text {
  line-height: 1.6;
  margin: 16px 0;
}

.notice-attachments h4 {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: bold;
}

.notice-attachments ul {
  padding-left: 20px;
  }

.notice-attachments li {
  margin-bottom: 8px;
}

.el-form-item {
  margin-bottom: 20px;
}

.form-container {
  position: relative;
  z-index: 10;
}
</style>