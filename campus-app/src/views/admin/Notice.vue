<template>
  <div class="notice-management-container">
    <div class="page-header">
      <h2>通知管理</h2>
      <el-button
          type="primary"
          @click="handleAddNotice"
      >
        <el-icon>
          <Plus/>
        </el-icon>
        发布通知
      </el-button>
    </div>

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
              placeholder="通知标题"
              style="width: 250px;"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select
              v-model="searchParams.type"
              :loading="loadingNoticeTypes"
              clearable
              placeholder="选择类型"
              style="width: 150px;"
          >
            <el-option
                v-for="type in noticeTypes"
                :key="type.typeCode"
                :label="type.typeName"
                :value="type.typeCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
              v-model="searchParams.status"
              clearable
              placeholder="选择状态"
              style="width: 120px;"
          >
            <el-option
                :value="1"
                label="已发布"
            />
            <el-option
                :value="0"
                label="草稿"
            />
            <el-option
                :value="2"
                label="已撤回"
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

    <!-- 通知列表 -->
    <el-card class="notice-list-card">
      <el-table
          v-loading="loading"
          :data="noticeList"
          style="width: 100%"
      >
        <el-table-column
            label="标题"
            min-width="300"
            prop="title"
            show-overflow-tooltip
        />
        <el-table-column
            label="类型"
            prop="type"
            width="120"
        >
          <template #default="scope">
            <el-tag :type="noticeTypeMapComputed[scope.row.type]?.tag || 'info'">
              {{ noticeTypeMapComputed[scope.row.type]?.name || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
            label="发布人"
            prop="publisher"
            width="120"
        />
        <el-table-column
            label="发布时间"
            prop="publishTime"
            width="180"
        >
          <template #default="scope">
            {{ formatTime(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column
            label="状态"
            prop="status"
            width="100"
        >
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
            label="创建时间"
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
            width="200"
        >
          <template #default="scope">
            <el-button
                v-if="scope.row.status === 0 || scope.row.status === 1"
                size="small"
                type="warning"
                @click="togglePublishStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '撤回' : '发布' }}
            </el-button>
            <el-button
                size="small"
                type="primary"
                @click="handleEditNotice(scope.row)"
            >
              编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDeleteNotice(scope.row)"
            >
              删除
            </el-button>
            <!-- 可以添加置顶等操作 -->
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

    <!-- 添加/编辑通知对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="dialogTitle"
        top="5vh"
        width="80%"
        @close="handleDialogClose"
    >
      <el-form
          ref="noticeFormRef"
          v-loading="loadingForm"
          :model="noticeForm"
          :rules="noticeFormRules"
          label-width="80px"
      >
        <el-form-item
            label="标题"
            prop="title"
        >
          <el-input
              v-model="noticeForm.title"
              placeholder="请输入通知标题"
          />
        </el-form-item>
        <el-form-item
            label="类型"
            prop="type"
        >
          <el-select
              v-model="noticeForm.type"
              :loading="loadingNoticeTypes"
              placeholder="选择通知类型"
          >
            <el-option
                v-for="type in noticeTypes"
                :key="type.typeCode"
                :label="type.typeName"
                :value="type.typeCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item
            label="内容"
            prop="content"
        >
          <div style="border: 1px solid #ccc">
            <Toolbar
                :default-config="toolbarConfig"
                :editor="editorRef"
                mode="default"
                style="border-bottom: 1px solid #ccc"
            />
            <Editor
                v-model="noticeForm.content"
                :default-config="editorConfig"
                mode="default"
                style="height: 400px; overflow-y: hidden;"
                @onCreated="handleEditorCreated"
            />
          </div>
        </el-form-item>
        <el-form-item
            label="附件"
            prop="attachments"
        >
          <el-upload
              v-model:file-list="fileList"
              :action="uploadAttachmentUrl"
              :headers="uploadHeaders"
              :on-error="handleUploadError"
              :on-preview="handleAttachmentPreview"
              :on-remove="handleAttachmentRemove"
              :on-success="handleAttachmentSuccess"
              list-type="text"
              multiple
          >
            <el-button type="primary">
              点击上传附件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                单个文件不超过 50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
              :loading="submitting"
              @click="saveDraft"
          >存为草稿</el-button>
          <el-button
              :loading="submitting"
              type="primary"
              @click="submitNoticeForm"
          >发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, reactive, ref, shallowRef} from 'vue';
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
  ElTag,
  ElUpload
} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import '@wangeditor/editor/dist/css/style.css';
import {Editor, Toolbar} from '@wangeditor/editor-for-vue';
import {addNotice, deleteNotice, getNoticeById, getNoticeList, updateNotice, updateNoticeStatus} from '@/api/notice';
import {getNoticeTypes} from '@/api/common';
import {getToken} from '@/utils/auth';
import {deleteFile as deleteAttachmentFile} from '@/api/file';
import {FILE_API} from '@/api/api-endpoints';

const loading = ref(false);
const loadingNoticeTypes = ref(false);
const noticeList = ref([]);
const noticeTypes = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  keyword: '',
  type: '',
  status: null
});

const dialogVisible = ref(false);
const dialogTitle = ref('发布通知');

const isEditMode = ref(false);
const loadingForm = ref(false);
const submitting = ref(false);
const noticeFormRef = ref(null);
const noticeForm = ref({
  id: null,
  title: '',
  type: '',
  content: '',
  status: 0,
  attachments: [],
});
const fileList = ref([]);

const editorRef = shallowRef();
const toolbarConfig = {};
const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {
    uploadImage: {
      server: FILE_API.UPLOAD_IMAGE,
      fieldName: 'file',
      headers: {Authorization: `Bearer ${getToken()}`},
    },
    uploadAttachment: {
      server: FILE_API.UPLOAD_ATTACHMENT,
      fieldName: 'file',
      headers: {Authorization: `Bearer ${getToken()}`},
    },
  },
};

const handleEditorCreated = (editor) => {
  editorRef.value = editor;
};

onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor == null) return;
  editor.destroy();
});

const uploadAttachmentUrl = ref(FILE_API.UPLOAD_DOCUMENT || '/api/file/upload/document');
const uploadHeaders = ref({Authorization: `Bearer ${getToken()}`});

const handleAttachmentSuccess = (response, uploadFile, uploadFiles) => {
  if (response.code === 200 && response.data) {
    noticeForm.value.attachments.push({
      id: response.data.id,
      name: response.data.filename || uploadFile.name,
      url: response.data.url,
      uid: uploadFile.uid
    });
    fileList.value = uploadFiles;
    ElMessage.success('附件上传成功');
  } else {
    ElMessage.error(response.message || '附件上传失败');
    const fileIndex = uploadFiles.findIndex(f => f.uid === uploadFile.uid);
    if (fileIndex > -1) {
      uploadFiles.splice(fileIndex, 1);
    }
    fileList.value = uploadFiles;
  }
};

const handleAttachmentRemove = async (uploadFile, uploadFiles) => {
  const attachmentIndex = noticeForm.value.attachments.findIndex(att => att.uid === uploadFile.uid || att.name === uploadFile.name);
  if (attachmentIndex > -1) {
    const attachmentId = noticeForm.value.attachments[attachmentIndex].id;
    if (attachmentId) {
      try {
        await deleteAttachmentFile(attachmentId);
        ElMessage.success('附件已从服务器删除');
      } catch (error) {
        console.error("删除服务器附件失败", error);
      }
    }
    noticeForm.value.attachments.splice(attachmentIndex, 1);
    fileList.value = uploadFiles;
  } else {
    fileList.value = uploadFiles;
  }
};

const handleAttachmentPreview = (uploadFile) => {
  const attachment = noticeForm.value.attachments.find(att => att.uid === uploadFile.uid || att.name === uploadFile.name);
  if (attachment?.url) {
    window.open(attachment.url, '_blank');
  } else {
    ElMessage.warning('无法预览该文件');
  }
};

const handleUploadError = (error, uploadFile, uploadFiles) => {
  console.error("上传错误", error);
  ElMessage.error('附件上传失败');
  fileList.value = uploadFiles;
};

const noticeFormRules = reactive({
  title: [{required: true, message: '请输入通知标题', trigger: 'blur'}],
  type: [{required: true, message: '请选择通知类型', trigger: 'change'}],
  content: [
    {required: true, message: '请输入通知内容', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        const editor = editorRef.value;
        if (editor && editor.isEmpty()) {
          callback(new Error('请输入通知内容'));
        } else {
          callback();
        }
      }, trigger: 'blur'
    }
  ],
});

const handleDialogClose = () => {
  if (noticeFormRef.value) {
    noticeFormRef.value.resetFields();
  }
  noticeForm.value = {
    id: null, title: '', type: '', content: '', status: 0, attachments: []
  };
  fileList.value = [];
  isEditMode.value = false;
  loadingForm.value = false;
};

const handleAddNotice = () => {
  isEditMode.value = false;
  dialogTitle.value = '发布通知';
  noticeForm.value = {id: null, title: '', type: '', content: '', status: 0, attachments: []};
  fileList.value = [];
  if (noticeFormRef.value) {
    noticeFormRef.value.clearValidate();
  }
  dialogVisible.value = true;
  fetchNoticeTypes();
};

const handleEditNotice = async (row) => {
  isEditMode.value = true;
  dialogTitle.value = '编辑通知';
  loadingForm.value = true;
  noticeForm.value = {id: null, title: '', type: '', content: '', status: 0, attachments: []};
  fileList.value = [];
  if (noticeFormRef.value) {
    noticeFormRef.value.clearValidate();
  }
  dialogVisible.value = true;
  fetchNoticeTypes();
  try {
    const res = await getNoticeById(row.id);
    noticeForm.value = {...res.data};
    fileList.value = noticeForm.value.attachments.map(att => ({
      name: att.name,
      url: att.url,
      id: att.id,
      uid: att.id || Date.now() + Math.random()
    }));
    if (editorRef.value) {
      editorRef.value.setHtml(noticeForm.value.content || '');
    }
  } catch (error) {
    console.error("获取通知详情失败", error);
    ElMessage.error("获取通知详情失败");
    dialogVisible.value = false;
  } finally {
    loadingForm.value = false;
  }
};

const handleDeleteNotice = (row) => {
  ElMessageBox.confirm(`确定要删除通知 "${row.title}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteNotice(row.id);
      ElMessage.success('删除成功');
      fetchNotices();
    } catch (error) {
      console.error("删除通知失败", error);
      ElMessage.error("删除通知失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const togglePublishStatus = async (row) => {
  const targetStatus = row.status === 1 ? 2 : 1;
  const actionText = targetStatus === 1 ? '发布' : '撤回';
  ElMessageBox.confirm(`确定要${actionText}通知 "${row.title}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateNoticeStatus(row.id, targetStatus);
      ElMessage.success(`${actionText}成功`);
      fetchNotices();
    } catch (error) {
      console.error(`${actionText}通知失败`, error);
      ElMessage.error(`${actionText}通知失败`);
    }
  }).catch(() => {
    ElMessage.info('已取消操作');
  });
};

const saveDraft = async () => {
  if (!noticeForm.value.title) {
    ElMessage.warning('请输入通知标题以保存草稿');
    noticeFormRef.value?.validateField('title');
    return;
  }
  submitting.value = true;
  try {
    const dataToSend = prepareSubmitData();
    dataToSend.status = 0;

    if (isEditMode.value) {
      await updateNotice(dataToSend.id, dataToSend);
      ElMessage.success('草稿更新成功');
    } else {
      await addNotice(dataToSend);
      ElMessage.success('草稿保存成功');
    }
    dialogVisible.value = false;
    fetchNotices();
  } catch (error) {
    console.error("保存草稿失败", error);
    ElMessage.error(error?.response?.data?.message || '保存草稿失败');
  } finally {
    submitting.value = false;
  }
};

const submitNoticeForm = () => {
  noticeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const dataToSend = prepareSubmitData();
        dataToSend.status = 1;

        if (isEditMode.value) {
          await updateNotice(dataToSend.id, dataToSend);
          ElMessage.success('通知更新并发布成功');
        } else {
          await addNotice(dataToSend);
          ElMessage.success('通知发布成功');
        }
        dialogVisible.value = false;
        fetchNotices();
      } catch (error) {
        console.error("发布通知失败", error);
        ElMessage.error(error?.response?.data?.message || '发布通知失败');
      } finally {
        submitting.value = false;
      }
    } else {
      console.log('通知表单验证失败');
      return false;
    }
  });
};

const prepareSubmitData = () => {
  const editor = editorRef.value;
  if (!editor) {
    throw new Error("编辑器未初始化");
  }
  const contentHtml = editor.getHtml();
  const attachmentIds = noticeForm.value.attachments.map(att => att.id).filter(id => id != null);

  return {
    ...noticeForm.value,
    content: contentHtml,
    attachmentIds: attachmentIds,
    attachments: undefined
  };
}

const fetchNotices = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchParams.keyword || null,
      type: searchParams.type || null,
      status: searchParams.status
    };
    const res = await getNoticeList(params);
    noticeList.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取通知列表失败", error);
    ElMessage.error("获取通知列表失败");
  } finally {
    loading.value = false;
  }
};

const fetchNoticeTypes = async () => {
  loadingNoticeTypes.value = true;
  try {
    const res = await getNoticeTypes();
    noticeTypes.value = res.data || [];
  } catch (error) {
    console.error("获取通知类型失败", error);
  } finally {
    loadingNoticeTypes.value = false;
  }
};

const noticeTypeMapComputed = computed(() => {
  const map = {};
  noticeTypes.value.forEach(type => {
    map[type.typeCode] = {name: type.typeName, tag: type.tagType || 'info'};
  });
  return map;
});

const handleSearch = () => {
  currentPage.value = 1;
  fetchNotices();
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchNotices();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchNotices();
};

const formatStatus = (status) => {
  const statusMap = {
    1: '已发布',
    0: '草稿',
    2: '已撤回'
  };
  return statusMap[status] !== undefined ? statusMap[status] : '未知';
};

const getStatusTagType = (status) => {
  const typeMap = {
    1: 'success',
    0: 'info',
    2: 'warning'
  };
  return typeMap[status] || 'info';
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

onMounted(() => {
  fetchNotices();
  fetchNoticeTypes();
});

</script>

<script>
export default {
  name: 'NoticeManagement'
}
</script>

<style scoped>
.notice-management-container {
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

.notice-list-card {
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