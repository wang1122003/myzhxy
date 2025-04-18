<template>
  <div class="activity-detail">
    <el-page-header
        title="返回列表"
        @back="goBack"
    >
      <template #content>
        <span class="text-large font-600 mr-3"> 活动详情 </span>
      </template>
    </el-page-header>
    <el-card
        v-loading="loading"
        class="activity-content"
    >
      <template v-if="activity">
        <div class="activity-header">
          <h2>{{ activity.title }}</h2>
          <div class="activity-meta">
            <span>发布时间: {{ formatTime(activity.publishTime) }}</span>
            <span>活动时间: {{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</span>
            <span>活动地点: {{ activity.location }}</span>
          </div>
          <div class="activity-status">
            <el-tag :type="getStatusType(activity.status)">
              {{ getStatusName(activity.status) }}
            </el-tag>
            <el-tag
                v-if="activity.enrolled"
                style="margin-left: 10px;"
                type="success"
            >
              已报名
            </el-tag>
          </div>
        </div>
        <el-divider/>
        <div class="activity-body">
          <!-- 封面图 -->
          <el-image
              v-if="activity.coverImage"
              :src="activity.coverImage"
              fit="cover"
              style="width: 100%; height: 300px; margin-bottom: 20px;"
          />
          <!-- 描述内容 -->
          <div
              class="activity-description"
              v-html="activity.description"
          />
        </div>
        <el-divider/>
        <div class="activity-footer">
          <!-- 根据活动状态和报名状态显示按钮 -->
          <el-button
              v-if="activity.status === 1 && !activity.enrolled"
              :loading="registerLoading"
              type="primary"
              @click="handleRegister"
          >
            报名参加
          </el-button>
          <el-button
              v-if="activity.status === 1 && activity.enrolled"
              :loading="cancelLoading"
              type="danger"
              @click="handleCancel"
          >
            取消报名
          </el-button>
          <span
              v-if="activity.status !== 1"
              class="info-text"
          >活动已开始或已结束，无法报名/取消报名</span>
        </div>
      </template>
      <el-empty
          v-else-if="!loading"
          description="活动不存在或已被删除"
      />
    </el-card>
  </div>
</template>

<script>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElDivider, ElEmpty, ElImage, ElMessage, ElMessageBox, ElPageHeader, ElTag} from 'element-plus'
import {cancelJoinActivity, getActivityById, joinActivity} from '@/api/activity' // 假设 API 在这里

export default {
  name: 'ActivityDetail',
  components: {
    ElEmpty,
    ElPageHeader,
    ElDivider,
    ElImage,
    ElTag
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(true)
    const registerLoading = ref(false)
    const cancelLoading = ref(false)
    const activity = ref(null)
    const activityId = ref(route.params.id)

    const fetchActivityDetail = async () => {
      loading.value = true
      try {
        const res = await getActivityById(activityId.value)
        // 假设 API 返回的 data 中包含活动详情，以及 enrolled 字段指示当前用户是否报名
        activity.value = res.data
      } catch (error) {
        console.error("获取活动详情失败", error)
        ElMessage.error('获取活动详情失败')
        activity.value = null // 获取失败设置为空
      } finally {
        loading.value = false
      }
    }

    // 报名逻辑
    const handleRegister = async () => {
      registerLoading.value = true
      try {
        await joinActivity(activityId.value)
        ElMessage.success('报名成功')
        await fetchActivityDetail() // 报名成功后刷新详情，更新 enrolled 状态
      } catch (error) {
        console.error("报名失败", error)
        ElMessage.error('报名失败，请稍后再试')
      } finally {
        registerLoading.value = false
      }
    }

    // 取消报名逻辑
    const handleCancel = async () => {
      ElMessageBox.confirm('确定要取消报名吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        cancelLoading.value = true
        try {
          await cancelJoinActivity(activityId.value)
          ElMessage.success('取消报名成功')
          await fetchActivityDetail() // 取消成功后刷新详情
        } catch (error) {
          console.error("取消报名失败", error)
          ElMessage.error('取消报名失败，请稍后再试')
        } finally {
          cancelLoading.value = false
        }
      }).catch(() => {
        ElMessage.info('操作已取消')
      })
    }

    // 格式化时间
    const formatTime = (timeStr) => {
      if (!timeStr) return ''
      try {
        const date = new Date(timeStr)
        return date.toLocaleString('zh-CN', {hour12: false})
      } catch (e) {
        return timeStr
      }
    }

    // 活动状态转换 (硬编码，最好由 API 提供或配置)
    const statusMap = {
      1: {name: '报名中', type: 'success'},
      2: {name: '进行中', type: 'warning'},
      3: {name: '已结束', type: 'info'}
    }
    const getStatusName = (status) => statusMap[status]?.name || '未知状态'
    const getStatusType = (status) => statusMap[status]?.type || 'info'

    // 返回上一页
    const goBack = () => {
      router.back()
    }

    onMounted(() => {
      fetchActivityDetail()
    })

    return {
      loading,
      registerLoading,
      cancelLoading,
      activity,
      handleRegister,
      handleCancel,
      formatTime,
      getStatusName,
      getStatusType,
      goBack
    }
  }
}
</script>

<style scoped>
.activity-detail {
  padding: 20px;
}

.el-page-header {
  margin-bottom: 20px;
}
.activity-content {
  /* margin-top: 20px; */ /* PageHeader 已有间距 */
  min-height: 300px; /* 防止加载时塌陷 */
}

.activity-header {
  margin-bottom: 20px;
}

.activity-header h2 {
  margin-bottom: 15px;
}

.activity-meta {
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
}

.activity-meta span {
  margin-right: 20px;
}

.activity-status {
  margin-top: 10px;
}

.activity-body {
  margin: 20px 0;
}

.activity-description {
  margin-top: 20px;
  line-height: 1.8;
  white-space: pre-wrap; /* 保留换行 */
}

.activity-footer {
  margin-top: 30px;
}

.info-text {
  color: #909399;
  font-size: 14px;
}

/* el-empty 默认居中 */
</style> 