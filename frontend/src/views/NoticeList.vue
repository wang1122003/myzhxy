<template>
  <div class="notice-list-container">
    <el-page-header
        title="返回首页"
        @back="goBack"
    >
      <template #content>
        <span class="text-large font-600 mr-3">通知公告</span>
      </template>
    </el-page-header>

    <el-card class="notice-list-card">
      <el-table
          v-loading="loadingNotices"
          :data="noticeList"
          height="calc(100vh - 260px)"
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
            prop="publisherName"
            show-overflow-tooltip
            width="150"
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
            label="操作"
            width="100"
            fixed="right"
        >
          <template #default="scope">
            <el-button
                link
                type="primary"
                @click="viewNotice(scope.row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div
          v-if="totalNotices > 0"
          class="pagination-container"
      >
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalNotices"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>

      <el-empty
          v-if="noticeList.length === 0 && !loadingNotices"
          description="暂无通知公告"
      />
    </el-card>

    <!-- 公告详情对话框 (复用 Home.vue 的结构) -->
    <el-dialog
        v-model="noticeDialogVisible"
        :title="currentNotice.title"
        append-to-body
        top="5vh"
        width="60%"
    >
      <div
          v-if="currentNotice.id"
          v-loading="loadingNoticeDetail"
      >
        <div class="notice-content">
          <div class="notice-info">
            <span>发布人：{{ currentNotice.publisherName }}</span>
            <span>发布时间：{{ formatTime(currentNotice.publishTime) }}</span>
          </div>
          <el-divider/>
          <div
              class="notice-text"
              v-html="currentNotice.content"
          />
          <div
              v-if="currentNotice.attachmentFiles && currentNotice.attachmentFiles.length"
              class="notice-attachments"
          >
            <el-divider/>
            <h4>附件：</h4>
            <ul>
              <li
                  v-for="file in currentNotice.attachmentFiles"
                  :key="file.url || file.name" 
              >
                <el-link
                    :disabled="downloadingAttachment[file.id || file.url]"
                    :loading="downloadingAttachment[file.id || file.url]" 
                    type="primary"
                    @click="downloadAttachment(file)"
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
          <el-button @click="noticeDialogVisible = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {useRouter} from 'vue-router';
import {
  ElButton,
  ElCard,
  ElDialog,
  ElDivider,
  ElEmpty,
  ElLink,
  ElMessage,
  ElPageHeader,
  ElPagination,
  ElTable,
  ElTableColumn,
  ElTag
} from 'element-plus';
import {getNotificationById, getNotificationsPage} from '@/api/notice';
import {downloadFile} from '@/api/file';

const router = useRouter();

// 加载状态
const loadingNotices = ref(false);
const loadingNoticeDetail = ref(false);
const downloadingAttachment = reactive({});

// 数据
const noticeList = ref([]);
const totalNotices = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const currentNotice = reactive({
  id: null,
  title: '',
  publisher: '',
  publishTime: '',
  content: '',
  attachments: []
});

// 对话框可见性
const noticeDialogVisible = ref(false);

// 直接定义静态的类型映射 (与 HomePage 保持一致)
const noticeTypeMap = {
  "SYSTEM": {name: "系统通知", tag: "info"},
  "COURSE": {name: "教学通知", tag: "success"},
  "ACTIVITY": {name: "活动公告", tag: "warning"},
  "GENERAL": {name: "通用", tag: "danger"},
  "OTHER": {name: "其他", tag: "info"}
};

// computed 保持不变，但现在基于静态 map
const noticeTypeMapComputed = computed(() => {
  return noticeTypeMap;
});

// 获取通知列表
const fetchNotices = async () => {
  loadingNotices.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
      // 可以添加其他筛选参数，例如 type
    };
    // getNotificationsPage 返回的是 IPage 对象
    const res = await getNotificationsPage(params);
    // 使用 IPage 的 records 和 total 字段
    noticeList.value = res.records || [];
    // noticeList.value = res.list || []; // 旧的错误访问方式
    totalNotices.value = res.total || 0;
  } catch (error) {
    console.error("获取通知列表失败", error);
    ElMessage.error("获取通知列表失败");
  } finally {
    loadingNotices.value = false;
  }
};

// 查看通知详情 (复用 Home.vue 逻辑)
const viewNotice = async (row) => {
  loadingNoticeDetail.value = true;
  noticeDialogVisible.value = true;
  currentNotice.id = null; // 重置详情
  try {
    // 成功时 res 直接是数据对象
    const res = await getNotificationById(row.id);
    // 直接将返回的数据对象合并到 currentNotice
    Object.assign(currentNotice, res);
    // Object.assign(currentNotice, res.data); // 旧的访问方式
  } catch (err) {
    console.error("获取通知详情失败", err);
    ElMessage.error("获取通知详情失败");
    noticeDialogVisible.value = false;
  } finally {
    loadingNoticeDetail.value = false;
  }
};

// 下载附件 (复用 Home.vue 逻辑)
const downloadAttachment = async (file) => {
  if (!file || !file.id) {
    ElMessage.warning('无效的文件信息');
    return;
  }
  downloadingAttachment[file.id] = true;
  try {
    await downloadFile(file.id);
    // downloadFile 函数内部处理下载逻辑和成功提示
  } catch (error) {
    console.error("下载附件失败", error);
    ElMessage.error(`下载附件 ${file.name} 失败`);
  } finally {
    downloadingAttachment[file.id] = false;
  }
};

// 格式化时间 (复用 Home.vue 逻辑)
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  try {
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {hour12: false});
  } catch (e) {
    return timeStr;
  }
};

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1; // 页大小改变时回到第一页
  fetchNotices();
};

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchNotices();
};

// 返回首页
const goBack = () => {
  router.push('/');
};

// 组件挂载后加载数据
onMounted(async () => {
  // 只需获取通知列表
  await fetchNotices();
  // await Promise.all([
  //   fetchNotices(),
  //   fetchNoticeTypes() // 移除类型获取
  // ]);
});

</script>

<style scoped>
.notice-list-container {
  padding: 20px;
}

.el-page-header {
  margin-bottom: 20px;
}

.notice-list-card {
  /* 可以添加一些卡片样式 */
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 复用 Home.vue 的详情样式 */
.notice-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 15px;
}

.notice-info {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.notice-text {
  line-height: 1.8;
  margin-top: 15px;
  white-space: pre-wrap;
}

.notice-attachments {
  margin-top: 20px;
}

.notice-attachments h4 {
  margin-bottom: 10px;
}

.notice-attachments ul {
  list-style: none;
  padding-left: 0;
}

.notice-attachments li {
  margin-bottom: 5px;
}

.dialog-footer {
  text-align: right;
}

.el-link.is-loading {
  cursor: not-allowed;
  opacity: 0.6;
}

.notice-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
  cursor: pointer;
  color: #303133;

  &:hover {
    color: #409EFF;
  }
}

.notice-meta {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;

  span + span {
    margin-left: 15px;
  }
}
</style> 