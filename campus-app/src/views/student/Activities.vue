<template>
  <div class="activities-container">
    <div class="page-header">
      <h2>校园活动</h2>
      <div>
        <el-input
            v-model="searchKeyword"
            placeholder="搜索活动"
            style="width: 300px"
            @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon>
                <Search/>
              </el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <el-tabs
        v-model="activeName"
        @tab-click="handleTabClick"
    >
      <el-tab-pane
          label="全部活动"
          name="all"
      />
      <el-tab-pane
          label="我报名的"
          name="enrolled"
      />
    </el-tabs>

    <el-row :gutter="20">
      <el-col
          v-for="activity in displayActivities"
          :key="activity.id"
          :span="8"
      >
        <el-card
            class="activity-card"
            shadow="hover"
        >
          <img
              :src="activity.coverImage || 'https://via.placeholder.com/300x150'"
              class="activity-image"
          >
          <div class="activity-content">
            <h3 class="activity-title">
              {{ activity.title }}
            </h3>
            <p class="activity-time">
              <el-icon>
                <Calendar/>
              </el-icon>
              {{ formatDate(activity.startTime) }} - {{ formatDate(activity.endTime) }}
            </p>
            <p class="activity-location">
              <el-icon>
                <Location/>
              </el-icon>
              {{ activity.location }}
            </p>
            <p class="activity-description">
              {{ activity.description }}
            </p>
            <div class="activity-footer">
              <el-tag
                  v-if="activity.status === 1"
                  type="success"
              >
                报名中
              </el-tag>
              <el-tag
                  v-else-if="activity.status === 2"
                  type="warning"
              >
                进行中
              </el-tag>
              <el-tag
                  v-else-if="activity.status === 3"
                  type="info"
              >
                已结束
              </el-tag>

              <div class="activity-actions">
                <el-button
                    size="small"
                    type="primary"
                    @click="viewActivityDetail(activity.id)"
                >
                  查看详情
                </el-button>
                <el-button
                    v-if="!activity.enrolled && activity.status === 1"
                    size="small"
                    type="success"
                    @click="joinActivity(activity.id)"
                >
                  报名
                </el-button>
                <el-button
                    v-if="activity.enrolled && activity.status === 1"
                    size="small"
                    type="danger"
                    @click="cancelJoin(activity.id)"
                >
                  取消报名
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination-container">
      <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          background
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Calendar, Location, Search} from '@element-plus/icons-vue'
import {
  cancelJoinActivity,
  getAllActivities,
  getMyActivities,
  joinActivity as joinActivityApi
} from '@/api/activity'

export default {
  name: 'StudentActivities',
  components: {
    Calendar,
    Location,
    Search
  },
  setup() {
    const router = useRouter()
    const activities = ref([])
    const enrolledActivities = ref([])
    const searchKeyword = ref('')
    const activeName = ref('all')
    const currentPage = ref(1)
    const pageSize = ref(9)
    const total = ref(0)

    onMounted(() => {
      fetchActivities()
      fetchEnrolledActivities()
    })

    const fetchActivities = () => {
      getAllActivities({
        page: currentPage.value,
        pageSize: pageSize.value,
        keyword: searchKeyword.value
      }).then(response => {
        activities.value = response.data.list
        total.value = response.data.total
      }).catch(() => {
        ElMessage.error('获取活动列表失败')
      })
    }

    const fetchEnrolledActivities = () => {
      getMyActivities().then(response => {
        enrolledActivities.value = response.data

        // 标记已报名的活动
        activities.value.forEach(activity => {
          if (enrolledActivities.value.find(item => item.id === activity.id)) {
            activity.enrolled = true
          } else {
            activity.enrolled = false
          }
        })
      }).catch(() => {
        ElMessage.error('获取已报名活动失败')
      })
    }

    const displayActivities = computed(() => {
      if (activeName.value === 'all') {
        return activities.value
      } else {
        return enrolledActivities.value
      }
    })

    const handleSearch = () => {
      currentPage.value = 1
      fetchActivities()
    }

    const handleTabClick = () => {
      currentPage.value = 1
      if (activeName.value === 'all') {
        fetchActivities()
      } else {
        fetchEnrolledActivities()
      }
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      if (activeName.value === 'all') {
        fetchActivities()
      } else {
        fetchEnrolledActivities()
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    }

    const viewActivityDetail = (id) => {
      router.push(`/student/activity/${id}`)
    }

    const joinActivity = (id) => {
      joinActivityApi(id).then(() => {
        ElMessage.success('报名成功')
        fetchActivities()
        fetchEnrolledActivities()
      }).catch(() => {
        ElMessage.error('报名失败')
      })
    }

    const cancelJoin = (id) => {
      cancelJoinActivity(id).then(() => {
        ElMessage.success('取消报名成功')
        fetchActivities()
        fetchEnrolledActivities()
      }).catch(() => {
        ElMessage.error('取消报名失败')
      })
    }

    return {
      activities,
      enrolledActivities,
      searchKeyword,
      activeName,
      currentPage,
      pageSize,
      total,
      displayActivities,
      handleSearch,
      handleTabClick,
      handleCurrentChange,
      formatDate,
      viewActivityDetail,
      joinActivity,
      cancelJoin
    }
  }
}
</script>

<style scoped>
.activities-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.activity-card {
  margin-bottom: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.activity-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.activity-content {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.activity-title {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: bold;
}

.activity-time,
.activity-location {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 14px;
  color: #606266;
}

.activity-time .el-icon,
.activity-location .el-icon {
  margin-right: 5px;
}

.activity-description {
  margin-bottom: 15px;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.activity-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 