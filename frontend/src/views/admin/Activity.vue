<template>
  <div class="activity-management-container">
    <div class="page-header">
      <h2>活动管理</h2>
      <el-button
          type="primary"
          @click="handleAddActivity"
      >
        <el-icon>
          <Plus/>
        </el-icon>
        发布活动
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
              placeholder="活动标题/地点"
              style="width: 250px;"
          />
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
                label="报名中"
            />
            <el-option
                :value="2"
                label="进行中"
            />
            <el-option
                :value="3"
                label="已结束"
            />
            <el-option
                :value="0"
                label="已取消"
            />
          </el-select>
        </el-form-item>
        <!-- 可以添加按时间范围筛选 -->
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

    <!-- 活动列表 -->
    <el-card class="activity-list-card">
      <BaseTable :table-data="activityList"
          v-loading="loading"
          :data="activityList"
          style="width: 100%"
      >
        <el-table-column
            label="活动标题"
            min-width="200"
            prop="title"
            show-overflow-tooltip
        />
        <el-table-column
            label="活动地点"
            prop="location"
            width="150"
        />
        <el-table-column
            label="开始时间"
            prop="startTime"
            width="180"
        >
          <template #default="scope">
            {{ formatTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column
            label="结束时间"
            prop="endTime"
            width="180"
        >
          <template #default="scope">
            {{ formatTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column
            label="报名人数"
            prop="currentParticipants"
            width="100"
        />
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
            width="220"
        >
          <template #default="scope">
            <el-button
                size="small"
                type="success"
                @click="viewEnrollments(scope.row)"
            >
              报名情况
            </el-button>
            <el-button
                size="small"
                type="primary"
                @click="handleEditActivity(scope.row)"
            >
              编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDeleteActivity(scope.row)"
            >
              删除
            </el-button>
            <!-- 可以添加取消活动等操作 -->
          </template>
        </el-table-column>
      </BaseTable>

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

    <!-- 添加/编辑活动对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="dialogTitle"
        top="5vh"
        width="80%"
        @close="handleDialogClose"
    >
      <el-form
          ref="activityFormRef"
          v-loading="loadingForm"
          :model="activityForm"
          :rules="activityFormRules"
          label-width="100px"
      >
        <el-form-item
            label="活动标题"
            prop="title"
        >
          <el-input
              v-model="activityForm.title"
              placeholder="请输入活动标题"
          />
        </el-form-item>
        <el-form-item
            label="活动地点"
            prop="location"
        >
          <el-input
              v-model="activityForm.location"
              placeholder="请输入活动地点"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
                label="开始时间"
                prop="startTime"
            >
              <el-date-picker
                  v-model="activityForm.startTime"
                  placeholder="选择开始时间"
                  style="width: 100%;"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                label="结束时间"
                prop="endTime"
            >
              <el-date-picker
                  v-model="activityForm.endTime"
                  placeholder="选择结束时间"
                  style="width: 100%;"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item
            label="活动描述"
            prop="description"
        >
          <div style="border: 1px solid #ccc">
            <Toolbar
                :default-config="toolbarConfig"
                :editor="editorRef"
                mode="default"
                style="border-bottom: 1px solid #ccc"
            />
            <Editor
                v-model="activityForm.description"
                :default-config="editorConfig"
                mode="default"
                style="height: 300px; overflow-y: hidden;"
                @onCreated="handleEditorCreated"
            />
          </div>
        </el-form-item>
        <el-form-item
            label="封面图片"
            prop="posterUrl"
        >
          <el-upload
              :action="uploadPosterUrl"
              :before-upload="beforePosterUpload"
              :file-list="posterFileList"
              :headers="uploadHeaders"
              :limit="1"
              :on-error="handleUploadError"
              :on-preview="handlePosterPreview"
              :on-remove="handlePosterRemove"
              :on-success="handlePosterSuccess"
              :show-file-list="true"
              list-type="picture-card"
          >
            <el-icon>
              <Plus/>
            </el-icon>
            <template #tip>
              <div class="el-upload__tip">
                只能上传一张封面图，建议尺寸 750x300，大小不超过 2MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item
            label="状态"
            prop="status"
        >
          <el-select
              v-model="activityForm.status"
              placeholder="设置活动状态"
          >
            <el-option
                :value="1"
                label="报名中"
            />
            <el-option
                :value="2"
                label="进行中"
            />
            <el-option
                :value="3"
                label="已结束"
            />
            <el-option
                :value="0"
                label="已取消"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
              :loading="submitting"
              type="primary"
              @click="submitActivityForm"
          >确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看报名情况对话框 (占位符) -->
    <el-dialog
        v-model="enrollmentDialogVisible"
        title="活动报名情况"
        width="60%"
    >
      <p>报名列表待实现...</p>
      <!-- 显示报名学生列表 -->
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="enrollmentDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog v-model="imagePreviewVisible">
      <img
          :src="imagePreviewUrl"
          alt="Preview Image"
          style="display: block; max-width: 100%; margin: 0 auto;"
      >
    </el-dialog>
  </div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, reactive, ref, shallowRef} from 'vue';
import {
  ElButton,
  ElCard,
  ElCol,
  ElDatePicker,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElRow,
  ElSelect,
  ElTableColumn,
  ElTag,
  ElUpload
} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import '@wangeditor/editor/dist/css/style.css';
import {Editor, Toolbar} from '@wangeditor/editor-for-vue';
import {addActivity, deleteActivity, getActivityById, getAllActivities, updateActivity,} from '@/api/activity';
import {getToken} from '@/utils/auth';

const loading = ref(false);
const activityList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchParams = reactive({
  keyword: '',
  status: null
});

const dialogVisible = ref(false);
const dialogTitle = ref('发布活动');
const enrollmentDialogVisible = ref(false);
const isEditMode = ref(false);
const loadingForm = ref(false);
const submitting = ref(false);
const activityFormRef = ref(null);

// Activity Form Data
const activityForm = ref({
  id: null,
  title: '',
  location: '',
  startTime: null,
  endTime: null,
  description: '',
  posterUrl: '', // Store the URL of the uploaded poster
  status: 1, // Default to '报名中'
});

// Poster Upload State
const posterFileList = ref([]); // For el-upload :file-list
const imagePreviewVisible = ref(false);
const imagePreviewUrl = ref('');
const uploadPosterUrl = ref('/activities/poster/upload'); // Using path directly, was ACTIVITY_API.UPLOAD_POSTER
const uploadHeaders = ref({Authorization: `Bearer ${getToken()}`});

// Editor State
const editorRef = shallowRef();
const toolbarConfig = {};
const editorConfig = {
  placeholder: '请输入活动详情...',
  // Configure image upload within editor if needed
};

// Editor created callback
const handleEditorCreated = (editor) => {
  editorRef.value = editor;
};
// Destroy editor before component unmount
onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor == null) return;
  editor.destroy();
});

// --- Poster Upload Handlers ---
const handlePosterSuccess = (response, uploadFile) => {
  if (response.code === 200 && response.data?.url) {
    activityForm.value.posterUrl = response.data.url;
    // Update fileList for display (ensure only one item)
    posterFileList.value = [{name: uploadFile.name, url: response.data.url}];
    ElMessage.success('封面上传成功');
  } else {
    ElMessage.error(response.message || '封面上传失败');
    posterFileList.value = []; // Clear list on error
  }
};

const handlePosterRemove = (uploadFile, uploadFiles) => {
  activityForm.value.posterUrl = '';
  posterFileList.value = [];
  // Optional: Call backend to delete the uploaded poster if needed
  // const posterId = ... // Get ID if stored
  // if (posterId) deletePoster(posterId);
};

const handlePosterPreview = (uploadFile) => {
  imagePreviewUrl.value = uploadFile.url || activityForm.value.posterUrl;
  imagePreviewVisible.value = true;
};

const beforePosterUpload = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('封面图片只支持 JPG/PNG/GIF 格式!');
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('封面图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

const handleUploadError = (error) => {
  console.error("上传错误", error);
  ElMessage.error('上传失败');
};
// --- End Poster Upload Handlers ---

// Form Validation Rules
const activityFormRules = reactive({
  title: [{required: true, message: '请输入活动标题', trigger: 'blur'}],
  location: [{required: true, message: '请输入活动地点', trigger: 'blur'}],
  startTime: [{required: true, message: '请选择开始时间', trigger: 'change'}],
  endTime: [
    {required: true, message: '请选择结束时间', trigger: 'change'},
    {
      validator: (rule, value, callback) => {
        if (activityForm.value.startTime && value && new Date(value) <= new Date(activityForm.value.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'));
        } else {
          callback();
        }
      }, trigger: 'change'
    }
  ],
  description: [
    {required: true, message: '请输入活动描述', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        const editor = editorRef.value;
        if (editor && editor.isEmpty()) {
          callback(new Error('请输入活动描述'));
        } else {
          callback();
        }
      }, trigger: 'blur'
    }
  ],
  // Poster is not strictly required for the form to submit?
  // posterUrl: [{ required: true, message: '请上传封面图片', trigger: 'change' }],
  status: [{required: true, message: '请设置活动状态', trigger: 'change'}],
});

// Dialog Close Handler
const handleDialogClose = () => {
  if (activityFormRef.value) {
    activityFormRef.value.resetFields();
  }
  activityForm.value = {
    id: null,
    title: '',
    location: '',
    startTime: null,
    endTime: null,
    description: '',
    posterUrl: '',
    status: 1
  };
  posterFileList.value = [];
  isEditMode.value = false;
  loadingForm.value = false;
};

// 获取活动列表
const fetchActivities = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchParams // Add search filters
    };
    // Use getAllActivities instead of getActivityList
    const res = await getAllActivities(params);
    activityList.value = res.data.records || [];
    total.value = res.data.total || 0;
  } catch (error) {
    console.error("获取活动列表失败", error);
    ElMessage.error("获取活动列表失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchActivities();
};

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchActivities();
};

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchActivities();
};

// Handle Add button click
const handleAddActivity = () => {
  isEditMode.value = false;
  dialogTitle.value = '发布活动';
  activityForm.value = {
    id: null,
    title: '',
    location: '',
    startTime: null,
    endTime: null,
    description: '',
    posterUrl: '',
    status: 1
  };
  posterFileList.value = [];
  if (activityFormRef.value) {
    activityFormRef.value.clearValidate();
  }
  // Ensure editor content is cleared
  if (editorRef.value) {
    editorRef.value.setHtml('');
  }
  dialogVisible.value = true;
};

// Handle Edit button click
const handleEditActivity = async (row) => {
  isEditMode.value = true;
  dialogTitle.value = '编辑活动';
  loadingForm.value = true;
  // Reset form first
  activityForm.value = {
    id: null,
    title: '',
    location: '',
    startTime: null,
    endTime: null,
    description: '',
    posterUrl: '',
    status: 1
  };
  posterFileList.value = [];
  if (activityFormRef.value) {
    activityFormRef.value.clearValidate();
  }
  dialogVisible.value = true;
  try {
    const res = await getActivityById(row.id);
    activityForm.value = {...res.data}; // Populate form
    // Populate posterFileList for el-upload
    if (activityForm.value.posterUrl) {
      posterFileList.value = [{name: '封面图', url: activityForm.value.posterUrl}];
    }
    // Set editor content
    if (editorRef.value) {
      editorRef.value.setHtml(activityForm.value.description || '');
    }
  } catch (error) {
    console.error("获取活动详情失败", error);
    ElMessage.error("获取活动详情失败");
    dialogVisible.value = false;
  } finally {
    loadingForm.value = false;
  }
};

// 删除活动
const handleDeleteActivity = (row) => {
  ElMessageBox.confirm(`确定要删除活动 ${row.title} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteActivity(row.id);
      ElMessage.success('删除成功');
      fetchActivities(); // 刷新列表
    } catch (error) {
      console.error("删除活动失败", error);
      ElMessage.error("删除活动失败");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 查看报名情况
const viewEnrollments = async (row) => {
  enrollmentDialogVisible.value = true;
  ElMessage.info(`查看活动 ${row.title} 报名情况功能待实现`);
  // try {
  //     loadingEnrollments.value = true;
  //     const res = await getActivityEnrollments(row.id);
  //     enrollments.value = res.data || [];
  // } catch (error) {
  //     ElMessage.error('获取报名列表失败');
  // } finally {
  //     loadingEnrollments.value = false;
  // }
};

// Submit Form
const submitActivityForm = () => {
  activityFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const editor = editorRef.value;
        if (!editor) throw new Error("编辑器未初始化");

        const dataToSend = {
          ...activityForm.value,
          description: editor.getHtml(), // Get content from editor
        };

        if (isEditMode.value) {
          await updateActivity(dataToSend.id, dataToSend);
          ElMessage.success('活动更新成功');
        } else {
          await addActivity(dataToSend);
          ElMessage.success('活动发布成功');
        }
        dialogVisible.value = false;
        fetchActivities(); // Refresh list
      } catch (error) {
        console.error("提交活动失败", error);
        ElMessage.error(error?.response?.data?.message || '提交活动失败');
      } finally {
        submitting.value = false;
      }
    } else {
      console.log('活动表单验证失败');
      return false;
    }
  });
};

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    1: '报名中',
    2: '进行中',
    3: '已结束',
    0: '已取消' // 假设 0 为取消状态
  };
  return statusMap[status] !== undefined ? statusMap[status] : '未知';
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    1: 'success', // 报名中
    2: 'warning', // 进行中
    3: 'info',    // 已结束
    0: 'danger'   // 已取消
  };
  return typeMap[status] || 'info';
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-';
  try {
    const date = new Date(timeStr);
    // 使用更完整的格式
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  } catch (e) {
    return timeStr;
  }
};

// 组件挂载后加载数据
onMounted(() => {
  fetchActivities();
});

</script>

<script>
export default {
  name: 'ActivityManagement'
}
</script>

<style scoped>
.activity-management-container {
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

.activity-list-card {
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