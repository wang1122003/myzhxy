<template>
  <PageContainer title="活动管理">
    <template #header-actions>
      <el-button
          type="primary"
          :icon="Plus"
          @click="handleAddActivity"
      >
        发布活动
      </el-button>
    </template>

    <!-- 搜索和筛选 -->
    <FilterForm
          :model="searchParams"
          :items="filterItems"
          @reset="handleReset"
          @search="handleSearch"
    />

    <!-- 活动列表 -->
    <TableView
        :action-column-config="actionColumnConfig"
          :data="activityList"
        :columns="tableColumns"
        :loading="loading"
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
        :total="total"
        @delete="handleDeleteActivity"
        @edit="handleEditActivity"
        @refresh="fetchActivities"
        />

    <!-- 添加/编辑活动对话框 -->
    <DialogWrapper
        v-model:visible="dialogVisible"
        :close-on-click-modal="false"
        :title="dialogTitle"
        top="5vh"
        width="80%"
        @close="handleDialogClose"
        destroy-on-close
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
                label="报名截止"
                prop="enrollDeadline"
            >
              <el-date-picker
                  v-model="activityForm.enrollDeadline"
                  placeholder="选择报名截止时间"
                  style="width: 100%;"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
                label="人数限制"
                prop="maxParticipants"
            >
              <el-input-number
                  v-model="activityForm.maxParticipants"
                  :min="0"
                  placeholder="0表示不限制"
                  style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动封面" prop="posterUrl">
          <!-- Comment out ImageUpload component -->
          <!--
          <ImageUpload
            v-model="activityForm.posterUrl"
            :limit="1"
            :fileSize="5"
            :uploadFunction="uploadActivityPosterFile"
          />
          -->
          <el-input v-model="activityForm.posterUrl" placeholder="暂时禁用图片上传"></el-input>
          <!-- Keep input for validation, or remove validation -->
          <!-- <el-input v-model="activityForm.posterUrl" style="display: none;" /> --> <!-- 用于表单验证 -->
        </el-form-item>
        <el-form-item
            label="活动描述"
            prop="description"
        >
          <div style="border: 1px solid #ccc; z-index: 100;">
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
                style="height: 400px; overflow-y: hidden;"
                @onChange="handleEditorChange"
                @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="activityForm.status">
            <el-radio :label="1">发布（开放报名）</el-radio>
            <el-radio :label="2">预告（暂不报名）</el-radio>
            <el-radio :label="0">草稿（暂不发布）</el-radio>
          </el-radio-group>
        </el-form-item>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              :loading="submitting"
              @click="submitActivityForm"
          >
            {{ dialogTitle.includes('发布') ? '发布' : '更新' }}
          </el-button>
      </div>
      </template>
    </DialogWrapper>

    <!-- 查看报名列表对话框 (内联替代 EnrollmentListDialog) -->
    <DialogWrapper
        v-model:visible="enrollmentDialogVisible"
        destroy-on-close
        title="活动报名列表"
        top="5vh"
        width="70%"
    >
      <div v-loading="enrollmentLoading">
        <div class="toolbar" style="margin-bottom: 15px; display: flex; justify-content: space-between;">
          <span>共 {{ totalEnrollments }} 人报名</span>
          <!-- Export button removed/commented out -->
        </div>
        <TableView
            v-model:current-page="enrollmentCurrentPage"
            v-model:page-size="enrollmentPageSize"
            :columns="enrollmentTableColumns"
            :data="enrollmentList"
            :loading="enrollmentLoading"
            :show-action-column="false"
            :total="totalEnrollments"
            @refresh="fetchEnrollments"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="enrollmentDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </DialogWrapper>

  </PageContainer>
</template>

<script setup>
import {computed, h, onBeforeUnmount, onMounted, reactive, ref, resolveComponent, shallowRef, watch} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Upload, Search, View, Edit, Delete, Download} from '@element-plus/icons-vue'
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import {
  getAllActivities,
  getActivityById,
  addActivity,
  updateActivity,
  deleteActivity,
  updateActivityStatus,
  getActivityEnrollments // 确保导入
} from '@/api/activity'
import {downloadFile} from '@/api/file' // 确保导入
import {useUserStore} from '@/stores/userStore'
import {formatDateTime} from '@/utils/formatters' // 确保导入
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue'
import TableView from '@/views/ui/TableView.vue'
import FilterForm from '@/views/ui/AdvancedFilterForm.vue'
import RichTextEditor from '@/views/ui/RichTextEditor.vue'
import DialogWrapper from '@/views/ui/DialogWrapper.vue';
// 移除 EnrollmentListDialog 导入
// import EnrollmentListDialog from '@/components/admin/EnrollmentListDialog.vue' // 引入报名列表弹窗组件

// --- Constants & Enums ---
const ACTIVITY_STATUS = {
  DRAFT: 0,
  PUBLISHED: 1, // 报名中 (替代旧的1)
  UPCOMING: 2, // 预告 (替代旧的2 进行中)
  ONGOING: 3, // 进行中 （新增状态，如果需要区分报名结束和活动开始的话）
  FINISHED: 4, // 已结束 (替代旧的3)
  CANCELLED: 5 // 已取消 (替代旧的0)
}
const ACTIVITY_STATUS_OPTIONS = [
  // { value: '', label: '全部' }, // FilterForm 内部处理
  {value: ACTIVITY_STATUS.PUBLISHED, label: '报名中'},
  {value: ACTIVITY_STATUS.UPCOMING, label: '预告'},
  {value: ACTIVITY_STATUS.ONGOING, label: '进行中'},
  {value: ACTIVITY_STATUS.FINISHED, label: '已结束'},
  {value: ACTIVITY_STATUS.CANCELLED, label: '已取消'}
];

// --- Reactive State ---
const loading = ref(false)
const activityList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchParams = reactive({
  keyword: '',
  status: null
})

const dialogVisible = ref(false)
const dialogTitle = ref('发布活动')
const activityFormRef = ref(null)
const loadingForm = ref(false)
const submitting = ref(false)
const activityForm = reactive({
  id: null,
  title: '',
  location: '',
  startTime: null,
  endTime: null,
  enrollDeadline: null,
  maxParticipants: 0,
  description: '',
  posterUrl: '',
  status: ACTIVITY_STATUS.PUBLISHED // 默认发布状态
})

// 活动报名列表对话框相关状态
const enrollmentDialogVisible = ref(false)
const enrollmentLoading = ref(false)
const enrollmentList = ref([])
const totalEnrollments = ref(0)
const enrollmentCurrentPage = ref(1)
const enrollmentPageSize = ref(10)
const currentActivityIdForEnrollment = ref(null)

// 富文本编辑器实例 (必须用 shallowRef)
const editorRef = shallowRef()
const toolbarConfig = {}
const editorConfig = {
  placeholder: '请输入活动详情...',
  MENU_CONF: {
    // 配置图片上传（如果编辑器需要）
    // uploadImage: {
    //     server: '/api/file/upload/post/image', // 与 file.js 中的接口对应
    //     fieldName: 'file',
    //     maxFileSize: 5 * 1024 * 1024, // 5M
    //     // 可选的其他配置...
    // }
  }
}

// --- Computed Properties ---

// FilterForm 配置
const filterItems = computed(() => [
  {
    type: 'input',
    label: '关键词',
    prop: 'keyword',
    placeholder: '活动标题/地点',
    props: {clearable: true, style: {width: '250px'}}
  },
  {
    type: 'select',
    label: '状态',
    prop: 'status',
    placeholder: '选择状态',
    options: ACTIVITY_STATUS_OPTIONS,
    props: {clearable: true, style: {width: '150px'}} // 调整宽度
  }
])

// TableView 列配置
const tableColumns = computed(() => [
  {prop: 'title', label: '活动标题', minWidth: 200, showOverflowTooltip: true},
  {prop: 'location', label: '活动地点', width: 150},
  {
    prop: 'startTime',
    label: '开始时间',
    width: 180,
    formatter: (row) => formatDateTime(row.startTime)
  },
  {
    prop: 'endTime',
    label: '结束时间',
    width: 180,
    formatter: (row) => formatDateTime(row.endTime)
  },
  {
    prop: 'enrollDeadline',
    label: '报名截止',
    width: 180,
    formatter: (row) => formatDateTime(row.enrollDeadline)
  },
  {
    label: '报名/名额', width: 120,
    formatter: (row) => `${row.currentParticipants || 0} / ${row.maxParticipants > 0 ? row.maxParticipants : '不限'}`
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    slots: {
      default: (scope) =>
          h(resolveComponent('ElTag'), {
            type: getStatusTagType(scope.row.status)
          }, () => formatStatus(scope.row.status))
        }
  },
  {
    prop: 'createTime',
    label: '发布时间',
    width: 180,
    formatter: (row) => formatDateTime(row.createTime)
    }
])

// TableView 操作列配置
const actionColumnConfig = computed(() => ({
  width: 250, // 调整宽度以适应按钮
  fixed: 'right',
  buttons: [
    {label: '报名情况', type: 'success', size: 'small', event: 'viewEnrollments'}, // 自定义事件
    {label: '编辑', type: 'primary', size: 'small', event: 'edit'},
    {label: '删除', type: 'danger', size: 'small', event: 'delete'}
    // 可以添加更多操作，如 '取消活动'
  ]
}))

// 新增：报名列表表格列配置
const enrollmentTableColumns = computed(() => [
  {prop: 'student.studentId', label: '学号', width: 150},
  {prop: 'student.realName', label: '姓名', width: 120},
  {prop: 'student.className', label: '班级', minWidth: 180},
  {
    prop: 'enrollTime',
    label: '报名时间',
    width: 180,
    formatter: (row) => formatDateTime(row.enrollTime) // formatDateTime 应已存在
  }
]);

// --- Methods ---

// 获取活动列表
const fetchActivities = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchParams
    }
    const res = await getAllActivities(params)
    if (res && res.data) {
      if (res.data.records) { // Standard IPage format
        activityList.value = res.data.records;
        total.value = res.data.total;
      } else if (res.data.list) { // Custom format { list: [], total: ...}
        activityList.value = res.data.list;
        total.value = res.data.total;
      } else if (Array.isArray(res.data)) { // Direct array if no pagination on backend
        activityList.value = res.data;
        total.value = res.data.length;
      } else {
        console.warn('Unknown activity data structure:', res.data);
        activityList.value = [];
        total.value = 0;
      }
    } else {
      activityList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取活动列表失败:", error)
    // 错误由拦截器处理
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchActivities()
}

// 处理重置
const handleReset = () => {
  searchParams.keyword = '';
  searchParams.status = null;
  currentPage.value = 1;
  fetchActivities();
}

// 格式化状态
const formatStatus = (status) => {
  const found = ACTIVITY_STATUS_OPTIONS.find(opt => opt.value === status);
  return found ? found.label : '未知';
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case ACTIVITY_STATUS.PUBLISHED:
      return 'success' // 报名中
    case ACTIVITY_STATUS.UPCOMING:
      return 'info' // 预告
    case ACTIVITY_STATUS.ONGOING:
      return 'primary' // 进行中
    case ACTIVITY_STATUS.FINISHED:
      return 'warning' // 已结束
    case ACTIVITY_STATUS.CANCELLED:
      return 'danger' // 已取消
    default:
      return ''
  }
}

// --- 活动表单和弹窗 --- 

// 重置表单
const resetForm = () => {
  if (activityFormRef.value) {
    activityFormRef.value.resetFields();
  }
  activityForm.id = null;
  activityForm.title = '';
  activityForm.location = '';
  activityForm.startTime = null;
  activityForm.endTime = null;
  activityForm.enrollDeadline = null;
  activityForm.maxParticipants = 0;
  activityForm.description = '';
  activityForm.posterUrl = '';
  activityForm.status = ACTIVITY_STATUS.PUBLISHED; // 重置为默认
  // 清空编辑器内容
  if (editorRef.value) {
    editorRef.value.setHtml('');
  }
}

// 打开添加弹窗
const handleAddActivity = () => {
  resetForm();
  dialogTitle.value = '发布新活动'
  dialogVisible.value = true
}

// 打开编辑弹窗
const handleEditActivity = (row) => {
  resetForm(); // 先重置
  dialogTitle.value = '编辑活动'
  // 填充数据
  Object.assign(activityForm, {
    id: row.id,
    title: row.title,
    location: row.location,
    startTime: row.startTime, // 后端返回的格式应能被 date-picker 识别
    endTime: row.endTime,
    enrollDeadline: row.enrollDeadline,
    maxParticipants: row.maxParticipants,
    description: row.description || '',
    posterUrl: row.posterUrl || '',
    status: row.status
  });
  dialogVisible.value = true;
  // 编辑器内容在 dialog 打开后设置，确保编辑器已渲染
  watch(dialogVisible, (newVal) => {
    if (newVal && editorRef.value && activityForm.description) {
      editorRef.value.setHtml(activityForm.description);
    }
  }, {immediate: true, once: true});
}

// 删除活动
const handleDeleteActivity = (row) => {
  ElMessageBox.confirm(`确定要删除活动【${row.title}】吗？`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteActivity(row.id)
      ElMessage.success('删除成功')
      fetchActivities() // 刷新列表
    } catch (error) {
      console.error("删除活动失败:", error)
      // 错误由拦截器处理
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 查看报名列表
const handleViewEnrollments = (activity) => {
  currentActivityIdForEnrollment.value = activity.id;
  enrollmentCurrentPage.value = 1; // 重置页码
  enrollmentDialogVisible.value = true;
  fetchEnrollments(); // 加载数据
  // enrollmentDialogVisible.value = true; // 在加载后显示，避免看到空数据
};

// 弹窗关闭回调
const handleDialogClose = () => {
  resetForm(); // 关闭时重置表单
  // 销毁编辑器实例（如果每次打开都重新创建）
  // const editor = editorRef.value;
  // if (editor == null) return;
  // editor.destroy();
  // editorRef.value = null;
}

// 编辑器创建回调
const handleCreated = (editor) => {
  editorRef.value = editor // 记录 editor 实例
}

// 编辑器内容变化回调 (用于表单验证触发)
const handleEditorChange = () => {
  if (activityFormRef.value) {
    activityFormRef.value.validateField('description');
  }
};

// 提交活动表单
const submitActivityForm = async () => {
  if (!activityFormRef.value) return
  await activityFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      loadingForm.value = true
      try {
        const dataToSubmit = {...activityForm};

        if (activityForm.id) {
          // 更新活动
          await updateActivity(activityForm.id, dataToSubmit)
          ElMessage.success('更新成功')
        } else {
          // 添加活动
          await addActivity(dataToSubmit)
          ElMessage.success('发布成功')
        }
        dialogVisible.value = false
        fetchActivities() // 刷新列表
      } catch (error) {
        console.error("提交活动失败:", error)
        // 错误由拦截器处理
      } finally {
        submitting.value = false
        loadingForm.value = false
      }
    } else {
      ElMessage.warning('请检查表单必填项')
      return false
    }
  })
}

// 表单验证规则
const activityFormRules = reactive({
  title: [{required: true, message: '请输入活动标题', trigger: 'blur'}],
  location: [{required: true, message: '请输入活动地点', trigger: 'blur'}],
  startTime: [{required: true, message: '请选择开始时间', trigger: 'change'}],
  endTime: [
    {required: true, message: '请选择结束时间', trigger: 'change'},
    {
      validator: (rule, value, callback) => {
        if (activityForm.startTime && value && value <= activityForm.startTime) {
          callback(new Error('结束时间必须晚于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  enrollDeadline: [
    {required: true, message: '请选择报名截止时间', trigger: 'change'},
    {
      validator: (rule, value, callback) => {
        if (activityForm.startTime && value && value >= activityForm.startTime) {
          callback(new Error('报名截止时间必须早于活动开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  description: [{required: true, message: '请输入活动描述', trigger: 'change'}],
  // posterUrl: [{ required: true, message: '请上传活动封面', trigger: 'change' }], // 验证隐藏的 input - Commented out validation
  status: [{required: true, message: '请选择活动状态', trigger: 'change'}]
})

// 新增：获取报名列表
const fetchEnrollments = async () => {
  if (!currentActivityIdForEnrollment.value) return;
  enrollmentLoading.value = true;
  try {
    const params = {
      page: enrollmentCurrentPage.value,
      size: enrollmentPageSize.value,
    };
    const res = await getActivityEnrollments(currentActivityIdForEnrollment.value, params); // API 已导入
    enrollmentList.value = res.data?.records || [];
    totalEnrollments.value = res.data?.total || 0;
  } catch (error) {
    console.error('获取报名列表失败:', error);
    ElMessage.error('获取报名列表失败');
    enrollmentList.value = [];
    totalEnrollments.value = 0;
  } finally {
    enrollmentLoading.value = false;
  }
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchActivities()
})

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

// 监听分页变化 (TableView 内部处理)
watch([currentPage, pageSize], () => {
  fetchActivities();
}, {immediate: false});

// 添加对报名列表分页的监听
watch([enrollmentCurrentPage, enrollmentPageSize], () => {
  if (enrollmentDialogVisible.value && currentActivityIdForEnrollment.value) {
    fetchEnrollments();
  }
}, {immediate: false});

</script>

<style scoped>
/* 保留或添加特定样式 */
.dialog-footer {
  text-align: right;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>