<template>
  <PageContainer :title="activity.title || '活动详情'" @back="goBack">
    <template #actions>
        <el-button
            v-if="activity.status === 'PUBLISHED' && !activity.enrolled"
            :loading="joinLoading"
            type="primary"
            @click="handleJoin"
        >
          立即报名
        </el-button>
        <el-button
            v-if="activity.status === 'PUBLISHED' && activity.enrolled"
            :loading="cancelLoading"
            type="danger"
            @click="handleCancel"
        >
          取消报名
        </el-button>
    </template>

    <el-card
        v-loading="loading"
        class="activity-detail-card"
        shadow="never" 
    >
      <template v-if="activity.id">
        <div class="activity-header">
          <!-- Title moved to PageContainer -->
          <!-- <h1 class="activity-title">{{ activity.title }}</h1> -->
          <div class="activity-status">
            <el-tag :type="getStatusTagType(activity)" size="small">
              {{ formatStatus(activity) }}
            </el-tag>
          </div>
        </div>

        <div v-if="activity.posterUrl" class="activity-cover">
          <el-image
              :preview-src-list="[activity.posterUrl]"
              alt="活动封面"
              :src="activity.posterUrl"
              fit="cover"
              lazy
              preview-teleported
              style="width: 100%; height: 250px; border-radius: 4px; margin-bottom: 20px;"
          />
        </div>

        <el-descriptions :column="2" border size="small">
          <el-descriptions-item>
            <template #label>
              <el-icon>
                <Calendar/>
              </el-icon>
              活动时间
            </template>
            {{ formatDateTime(activity.startTime, 'YYYY-MM-DD HH:mm') }} 至
            {{ formatDateTime(activity.endTime, 'YYYY-MM-DD HH:mm') }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <el-icon>
                <Location/>
              </el-icon>
              活动地点
            </template>
            {{ activity.location }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <el-icon>
                <AlarmClock/>
              </el-icon>
              报名截止
            </template>
            {{ formatDateTime(activity.enrollDeadline, 'YYYY-MM-DD HH:mm') }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <el-icon>
                <User/>
              </el-icon>
              主办方
            </template>
            {{ activity.organizer || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label>
              <el-icon>
                <Tickets/>
              </el-icon>
              报名情况
            </template>
            {{ activity.currentParticipants || 0 }} / {{
              activity.maxParticipants > 0 ? activity.maxParticipants : '不限'
            }}
          </el-descriptions-item>
          <!-- Add other relevant meta info here -->
        </el-descriptions>

        <el-divider content-position="left">活动详情</el-divider>
        <div
            class="activity-content ql-editor"
            v-html="activity.content"
        />

        <!-- Attachments section (if applicable) -->
        <div
            v-if="activity.attachments && activity.attachments.length > 0"
            class="activity-attachments"
        >
          <el-divider content-position="left">相关附件</el-divider>
          <div
              v-for="(attachment, index) in activity.attachments"
              :key="index"
              class="attachment-item"
          >
            <el-button
                :loading="downloadLoading === attachment.id"
                link
                type="primary"
                @click="downloadAttachment(attachment)"
            >
              <el-icon>
                <Download/>
              </el-icon>
              {{ attachment.filename }} ({{ formatFileSize(attachment.size) }})
            </el-button>
          </div>
        </div>

        <!-- Enrollment Info -->
        <template v-if="activity.enrolled">
          <el-divider content-position="left">我的报名信息</el-divider>
          <div class="enrollment-info">
            <p>您已于 {{ formatDateTime(activity.enrollTime) }} 成功报名此活动！</p>
            <!-- Add participation status if available from API -->
            <!--
             <p>参与状态: 
               <el-tag :type="getParticipationStatusType(activity.participationStatus)" size="small">
                 {{ formatParticipationStatus(activity.participationStatus) }}
                </el-tag>
             </p>
            -->
          </div>
        </template>

        <!-- Participant List (Optional) -->
        <!-- Consider if showing participants is necessary/appropriate -->
        <!--
        <template v-if="activity.participants && activity.participants.length > 0">
          <el-divider content-position="left">其他参与者</el-divider>
          <div class="participant-list">
            <el-tooltip v-for="participant in activity.participants.slice(0, 15)" :key="participant.id" :content="participant.username || '未知用户'" placement="top">
               <el-avatar :src="participant.avatarUrl" size="small" style="margin: 2px;"></el-avatar>
            </el-tooltip>
            <span v-if="activity.participants.length > 15" style="margin-left: 5px; color: #909399;">...等 {{ activity.participants.length }} 人</span>
          </div>
        </template>
        -->

      </template>
      <el-empty v-else-if="!loading" description="未找到活动详情或加载失败"/>
    </el-card>

    <!-- Removed Participants Dialog for simplicity, can be added back if needed -->

  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {
  ElMessage,
  ElMessageBox,
  ElTag,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElDivider,
  ElButton,
  ElIcon,
  ElImage,
  ElAvatar,
  ElEmpty,
  ElTooltip
} from 'element-plus';
import {
  AlarmClock, ArrowLeft, Calendar, Download, Location, Tickets, User
} from '@element-plus/icons-vue';
import {getActivityById, joinActivity, cancelJoinActivity} from '@/api/activity'; // Corrected: Use activity.js
import {downloadFile} from '@/api/file'; // Corrected: Use file.js for download
import {formatDateTime} from '@/utils/formatters'; // Corrected import path
import {formatFileSize} from '@/utils/formatters'; // Corrected import path
import Quill from 'quill';

// --- State ---
const route = useRoute();
const router = useRouter();
const activityId = ref(route.params.id);
const activity = ref({});
const loading = ref(true);
const joinLoading = ref(false);
const cancelLoading = ref(false);
const downloadLoading = ref(null); // Track which attachment is downloading

// --- Methods ---

const goBack = () => {
  router.push('/student/activities');
};

const fetchActivityDetail = async () => {
  loading.value = true;
  try {
    const res = await getActivityById(activityId); // Corrected
    activity.value = res.data || {};
    // Ensure attachments is an array
    if (!Array.isArray(activity.value.attachments)) {
      activity.value.attachments = [];
    }
  } catch (error) {
    console.error('获取活动详情失败:', error);
    ElMessage.error('获取活动详情失败');
    activity.value = {}; // Clear activity data on error
  } finally {
    loading.value = false;
  }
};

// Reusing status formatting logic from Activities.vue (or import from a util)
const formatStatus = (act) => {
  if (act.enrolled) return '已报名';
  switch (act.status) {
    case 'PUBLISHED':
      return (act.maxParticipants > 0 && (act.currentParticipants || 0) >= act.maxParticipants) ? '名额已满' : '报名中';
    case 'ONGOING':
      return '进行中';
    case 'FINISHED':
      return '已结束';
    case 'CANCELLED':
      return '已取消';
    case 'UPCOMING':
      return '即将开始';
    default:
      return '未知';
  }
};

const getStatusTagType = (act) => {
  if (act.enrolled) return 'success';
  switch (act.status) {
    case 'PUBLISHED':
      return (act.maxParticipants > 0 && (act.currentParticipants || 0) >= act.maxParticipants) ? 'danger' : 'primary';
    case 'ONGOING':
      return 'warning';
    case 'FINISHED':
      return 'info';
    case 'CANCELLED':
      return 'danger';
    case 'UPCOMING':
      return 'info';
    default:
      return '';
  }
};

const handleJoin = async () => {
  joinLoading.value = true;
  try {
    await joinActivity(activityId); // Corrected
    ElMessage.success('报名成功！');
    await fetchActivityDetail(); // Refresh details
  } catch (error) {
    console.error('报名失败:', error);
    // Error handled by interceptor
  } finally {
    joinLoading.value = false;
  }
};

const handleCancel = async () => {
  cancelLoading.value = true;
  try {
    await ElMessageBox.confirm(`确定要取消报名活动【${activity.value.title}】吗？`, '确认取消', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning',
    });
    await cancelJoinActivity(activityId); // Corrected
    ElMessage.success('取消报名成功。');
    await fetchActivityDetail(); // Refresh details
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消报名失败:', error);
      // Error handled by interceptor
    }
  } finally {
    cancelLoading.value = false;
  }
};

const downloadAttachment = async (attachment) => {
  downloadLoading.value = attachment.id;
  try {
    // Use generic downloadFile, assuming attachment has id and filename
    await downloadFile({fileId: attachment.id}, attachment.filename); // Corrected
  } catch (error) {
    console.error("下载附件失败:", error);
    // Error handled by interceptor or download util
  } finally {
    downloadLoading.value = null;
  }
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchActivityDetail();
});

</script>

<style scoped>
/* Use Quill classes to style rich text content */
.activity-content :deep(.ql-editor) {
  padding: 0; /* Remove default padding if needed */
  line-height: 1.8;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.activity-status {
  margin-left: 15px;
}

.activity-content {
  margin-top: 20px;
  padding: 15px;
  background-color: #fdfdfd;
  border: 1px solid #eee;
  border-radius: 4px;
  min-height: 150px;
}

.activity-attachments {
  margin-top: 20px;
}

.attachment-item {
  margin-bottom: 5px;
}

.enrollment-info {
  margin-top: 10px;
  padding: 15px;
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  color: #67c23a;
}

.participant-list {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

/* Remove old styles if not needed */
</style> 