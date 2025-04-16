<template>
  <div class="activity-detail">
    <h1>活动详情</h1>
    <div v-loading="loading" class="activity-content">
      <template v-if="activity">
        <div class="activity-header">
          <h2>{{ activity.title }}</h2>
          <div class="activity-meta">
            <span>发布时间: {{ activity.publishTime }}</span>
            <span>活动时间: {{ activity.activityTime }}</span>
            <span>活动地点: {{ activity.location }}</span>
          </div>
        </div>
        <div class="activity-body">
          <el-image v-if="activity.posterUrl" :src="activity.posterUrl" fit="contain"
                    style="max-width: 100%; max-height: 400px;"></el-image>
          <div class="activity-description">{{ activity.description }}</div>
        </div>
        <div class="activity-footer">
          <el-button v-if="canRegister" type="primary" @click="register">报名参加</el-button>
        </div>
      </template>
      <div v-else class="empty-data">
        <p>活动不存在或已被删除</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ActivityDetail',
  data() {
    return {
      loading: true,
      activity: null,
      canRegister: false
    }
  },
  created() {
    // 获取活动ID并加载数据
    this.loading = false;
    this.mockData();
  },
  methods: {
    mockData() {
      // 模拟数据，实际应该从API获取
      this.activity = {
        id: this.$route.params.id,
        title: '校园文化节',
        publishTime: '2023-05-10',
        activityTime: '2023-05-20 14:00',
        location: '大学体育馆',
        posterUrl: '',
        description: '这是一个校园文化活动，欢迎全校师生参加。',
      };
      this.canRegister = true;
    },
    register() {
      // 报名逻辑
      this.$message.success('报名成功');
    }
  }
}
</script>

<style scoped>
.activity-detail {
  padding: 20px;
}

.activity-content {
  margin-top: 20px;
}

.activity-header {
  margin-bottom: 20px;
}

.activity-meta {
  margin-top: 10px;
  color: #666;
}

.activity-meta span {
  margin-right: 15px;
}

.activity-body {
  margin: 20px 0;
}

.activity-description {
  margin-top: 20px;
  line-height: 1.6;
}

.activity-footer {
  margin-top: 30px;
}

.empty-data {
  text-align: center;
  padding: 30px;
  color: #999;
}
</style> 