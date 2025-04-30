<template>
  <PageContainer title="校园活动">
    <div class="filter-container">
      <FilterForm
          :items="filterItems"
          :model="searchParams"
          :show-add-button="false"
          @reset="handleReset"
          @search="handleSearch"
      />
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部活动" name="all"/>
      <el-tab-pane label="我报名的" name="enrolled"/>
      <!-- 可以添加 "已结束" 等 Tab -->
    </el-tabs>

    <!-- 移动端卡片视图 -->
    <div v-if="isMobileView" class="activities-mobile-view">
      <div v-loading="loading">
        <el-empty v-if="activityList.length === 0" description="暂无活动数据"/>

        <div v-else class="activity-cards">
          <el-card
              v-for="activity in activityList"
              :key="activity.id"
              class="activity-card"
              @click="viewActivityDetail(activity)"
          >
            <div class="activity-card-content">
              <el-image
                  v-if="activity.posterUrl"
                  :src="activity.posterUrl"
                  class="activity-poster"
                  fit="cover"
              />
              <div v-else class="activity-poster activity-poster-empty">
                <el-icon>
                  <Picture/>
                </el-icon>
              </div>

              <div class="activity-info">
                <h3 class="activity-title">{{ activity.title }}</h3>
                <div class="activity-meta">
                  <p class="activity-location">
                    <el-icon>
                      <Location/>
                    </el-icon>
                    {{ activity.location || '地点未知' }}
                  </p>
                  <p class="activity-time">
                    <el-icon>
                      <Calendar/>
                    </el-icon>
                    {{ formatDateTime(activity.startTime, 'MM-DD HH:mm') }}
                  </p>
                </div>

                <div class="activity-footer">
                  <el-tag :type="getStatusTagType(activity)" size="small">
                    {{ formatStatus(activity) }}
                  </el-tag>

                  <div class="action-buttons">
                    <el-button
                        v-if="myActivityIds.has(activity.id) && (activity.status === 'PUBLISHED' || activity.status === 1)"
                        :loading="actionLoading?.id === activity.id && actionLoading?.type === 'cancel'"
                        link
                        size="small"
                        type="danger"
                        @click.stop="handleCancelJoin(activity)"
                    >
                      取消报名
                    </el-button>
                    <el-button
                        v-else-if="!myActivityIds.has(activity.id) && (activity.status === 'PUBLISHED' || activity.status === 1) &&
                      (activity.maxParticipants === 0 || (activity.currentParticipants || 0) < activity.maxParticipants)"
                        :loading="actionLoading?.id === activity.id && actionLoading?.type === 'join'"
                        link
                        size="small"
                        type="success"
                        @click.stop="handleJoinActivity(activity)"
                    >
                      报名
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <div class="mobile-pagination">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="fetchData"
          />
        </div>
      </div>
    </div>

    <!-- 桌面端表格视图 -->
    <div v-else>
      <TableView
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :action-column-config="actionColumnConfig"
          :columns="tableColumns"
          :data="activityList"
          :loading="loading"
          :total="total"
          @refresh="fetchData"
          @view-detail="viewActivityDetail"
          @join-activity="handleJoinActivity"
          @cancel-join="handleCancelJoin"
      />
    </div>
  </PageContainer>
</template>

<script setup>
import {computed, h, onMounted, reactive, ref, resolveComponent, watch} from 'vue';
import {useRouter} from 'vue-router';
import {ElMessage, ElMessageBox, ElTag} from 'element-plus';
import {Calendar, Location, Picture} from '@element-plus/icons-vue';
// import { getAllActivities, getMyActivities, joinActivity as joinActivityApi, cancelJoinActivity } from '@/api/activity';
// import { studentListActivities, studentGetMyActivities, studentJoinActivity, studentCancelJoinActivity } from '@/api/user';
import {cancelJoinActivity, getAllActivities, getMyActivities, joinActivity} from '@/api/activity'; // Corrected: Use activity.js
import {formatDateTime} from '@/utils/formatters';
import PageContainer from '@/components/common/EnhancedPageContainer.vue';
import TableView from '@/components/common/TableView.vue';
import FilterForm from '@/components/common/AdvancedFilterForm.vue';

// --- State ---
const router = useRouter();
const loading = ref(false);
const activityList = ref([]); // 当前显示的活动列表
const myActivityIds = ref(new Set()); // 我报名的活动 ID 集合
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10); // TableView 默认分页大小
const searchParams = reactive({keyword: ''});
const activeTab = ref('all'); // 'all' or 'enrolled'
const actionLoading = ref(null); // 用于按钮 loading 状态 { type: 'join'/'cancel', id: activityId }
const isMobileView = ref(window.innerWidth < 768);

// 检测窗口大小变化
window.addEventListener('resize', () => {
  isMobileView.value = window.innerWidth < 768;
});

// --- Computed Properties ---

// FilterForm 配置
const filterItems = computed(() => [
  {
    type: 'input',
    label: '关键词',
    prop: 'keyword',
    placeholder: '搜索活动标题/地点',
    props: {clearable: true, style: {width: '300px'}}
  }
]);

// TableView 列配置
const tableColumns = computed(() => [
  {
    prop: 'posterUrl',
    label: '封面',
    width: 100,
    slots: { // 使用 slot 自定义渲染图片
      default: (scope) => {
        const url = scope.row.posterUrl;
        return url
            ? h(resolveComponent('el-image'), {
              src: url,
              fit: 'cover',
              style: 'width: 80px; height: 50px; border-radius: 4px;',
              lazy: true,
              previewSrcList: [url],
              previewTeleported: true
            })
            : h('span', {style: 'color: #ccc;'}, '无封面');
      }
    }
  },
  {prop: 'title', label: '活动标题', minWidth: 200, showOverflowTooltip: true},
  {prop: 'location', label: '活动地点', width: 150},
  {
    prop: 'timeRange',
    label: '活动时间',
    width: 320,
    formatter: (row) => `${formatDateTime(row.startTime, 'YYYY-MM-DD HH:mm')} 至 ${formatDateTime(row.endTime, 'YYYY-MM-DD HH:mm')}`
  },
  {
    prop: 'enrollDeadline',
    label: '报名截止',
    width: 160,
    formatter: (row) => formatDateTime(row.enrollDeadline, 'YYYY-MM-DD HH:mm')
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
      default: (scope) => h(resolveComponent('el-tag'), {
        type: getStatusTagType(scope.row),
        size: 'small'
      }, () => formatStatus(scope.row))
    }
  },
]);

// TableView 操作列配置
const actionColumnConfig = computed(() => ({
  width: 150,
  fixed: 'right',
  buttons: (row) => {
    const actions = [];
    actions.push({label: '查看详情', type: 'primary', link: true, event: 'view-detail'});

    const enrolled = myActivityIds.value.has(row.id);
    const canEnroll = row.status === 'PUBLISHED' || row.status === 1; // 兼容旧的状态码或新状态 PUBLISHED
    const isLoading = actionLoading.value?.id === row.id;

    if (enrolled && canEnroll) { // 报名中且已报名，可以取消
      actions.push({
        label: '取消报名',
        type: 'danger',
        link: true,
        event: 'cancel-join',
        loading: isLoading && actionLoading.value?.type === 'cancel'
      });
    } else if (!enrolled && canEnroll && (row.maxParticipants === 0 || (row.currentParticipants || 0) < row.maxParticipants)) { // 未报名、可报名、有名额
      actions.push({
        label: '报名',
        type: 'success',
        link: true,
        event: 'join-activity',
        loading: isLoading && actionLoading.value?.type === 'join'
      });
    }
    return actions;
  }
}));

// --- Methods ---

// 格式化状态
const formatStatus = (activity) => {
  const enrolled = myActivityIds.value.has(activity.id);
  // 优先判断是否已报名
  if (enrolled) return '已报名';

  // 根据后端状态判断
  switch (activity.status) {
    case 1: // 假设 1 是报名中 (兼容旧代码)
    case 'PUBLISHED':
      return (activity.maxParticipants > 0 && (activity.currentParticipants || 0) >= activity.maxParticipants)
          ? '名额已满'
          : '报名中';
    case 2: // 假设 2 是进行中
    case 'ONGOING':
      return '进行中';
    case 3: // 假设 3 是已结束
    case 'FINISHED':
      return '已结束';
    case 0: // 假设 0 是已取消
    case 'CANCELLED':
      return '已取消';
    case 'UPCOMING': // 新增: 预告
      return '即将开始';
    default:
      return '未知';
  }
};

// 获取状态标签类型
const getStatusTagType = (activity) => {
  const enrolled = myActivityIds.value.has(activity.id);
  if (enrolled) return 'success';

  switch (activity.status) {
    case 1:
    case 'PUBLISHED':
      return (activity.maxParticipants > 0 && (activity.currentParticipants || 0) >= activity.maxParticipants)
          ? 'danger'
          : 'primary';
    case 2:
    case 'ONGOING':
      return 'warning';
    case 3:
    case 'FINISHED':
      return 'info';
    case 0:
    case 'CANCELLED':
      return 'danger';
    case 'UPCOMING':
      return 'info';
    default:
      return '';
  }
};

// 获取活动列表和参与活动
const fetchData = async () => {
  loading.value = true;
  try {
    // 并行获取活动列表和我参与的活动
    const [activitiesRes, myActivitiesRes] = await Promise.all([
      getAllActivities({
        page: currentPage.value,
        size: pageSize.value,
        status: searchParams.status || undefined,
        keyword: searchParams.keyword || undefined
      }),
      getMyActivities() // 获取我参与的活动
    ]);

    console.log('获取到所有活动数据:', activitiesRes);
    console.log('获取到我的活动数据:', myActivitiesRes);

    // 处理活动列表
    if (activitiesRes && activitiesRes.code === 200) {
      // 标准格式返回
      activityList.value = activitiesRes.data?.records || [];
      total.value = activitiesRes.data?.total || 0;
    } else {
      console.warn('活动API返回非标准格式:', activitiesRes);

      // 处理后端返回的各种可能格式
      if (Array.isArray(activitiesRes)) {
        // 直接返回数组
        activityList.value = activitiesRes;
        total.value = activitiesRes.length;
      } else if (activitiesRes && activitiesRes.list && Array.isArray(activitiesRes.list)) {
        // {list: [...], total: number} 格式
        activityList.value = activitiesRes.list;
        total.value = activitiesRes.total || activitiesRes.list.length;
      } else if (activitiesRes && activitiesRes.records && Array.isArray(activitiesRes.records)) {
        // {records: [...], total: number} 格式
        activityList.value = activitiesRes.records;
        total.value = activitiesRes.total || activitiesRes.records.length;
      } else if (activitiesRes && typeof activitiesRes === 'object') {
        // 尝试从响应中找到数组
        const possibleArrayKeys = ['list', 'records', 'rows', 'data', 'items', 'results'];
        for (const key of possibleArrayKeys) {
          if (activitiesRes[key] && Array.isArray(activitiesRes[key])) {
            activityList.value = activitiesRes[key];
            total.value = activitiesRes.total || activitiesRes.count || activitiesRes[key].length;
            break;
          }
        }

        // 如果没有找到数组，并且对象本身可以当作活动项
        if (activityList.value.length === 0 && activitiesRes.id) {
          activityList.value = [activitiesRes];
          total.value = 1;
        }
      } else {
        activityList.value = [];
        total.value = 0;
      }
    }

    console.log('处理后的活动列表:', activityList.value);

    // 处理我参与的活动
    if (myActivitiesRes && myActivitiesRes.code === 200) {
      // 标准返回格式
      myActivityIds.value = new Set((myActivitiesRes.data || []).map(a => a.id));
    } else if (Array.isArray(myActivitiesRes)) {
      // 直接数组返回
      myActivityIds.value = new Set(myActivitiesRes.map(a => a.id));
    } else if (myActivitiesRes && myActivitiesRes.list && Array.isArray(myActivitiesRes.list)) {
      // {list: [...]} 格式
      myActivityIds.value = new Set(myActivitiesRes.list.map(a => a.id));
    } else if (myActivitiesRes && myActivitiesRes.data && Array.isArray(myActivitiesRes.data)) {
      // {data: [...]} 格式
      myActivityIds.value = new Set(myActivitiesRes.data.map(a => a.id));
    } else if (myActivitiesRes && typeof myActivitiesRes === 'object') {
      // 尝试从响应中找到数组
      const possibleArrayKeys = ['list', 'records', 'rows', 'data', 'items', 'results'];
      for (const key of possibleArrayKeys) {
        if (myActivitiesRes[key] && Array.isArray(myActivitiesRes[key])) {
          myActivityIds.value = new Set(myActivitiesRes[key].map(a => a.id));
          break;
        }
      }
    } else {
      myActivityIds.value = new Set();
    }

    console.log('已报名活动IDs:', Array.from(myActivityIds.value));
    
  } catch (error) {
    console.error('获取活动数据失败', error);
    ElMessage.error('获取活动数据失败');
    activityList.value = [];
    total.value = 0;
    myActivityIds.value = new Set();
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchData();
};

// 处理重置
const handleReset = () => {
  searchParams.keyword = '';
  currentPage.value = 1;
  fetchData();
}

// 处理 Tab 切换
const handleTabChange = (tabName) => {
  // activeTab 已通过 v-model 更新
  currentPage.value = 1; // 切换 Tab 时重置页码
  fetchData();
};

// 查看活动详情
const viewActivityDetail = (activity) => {
  // TableView 会传递 row 数据
  router.push(`/student/activity/${activity.id}`);
};

// 报名活动
const handleJoinActivity = async (activity) => {
  actionLoading.value = {type: 'join', id: activity.id};
  try {
    await joinActivity(activity.id); // Call imported API function
    ElMessage.success('报名成功');
    // 刷新数据
    await fetchData();
  } catch (error) {
    console.error('报名失败:', error);
    // Error handled by interceptor
  } finally {
    actionLoading.value = null;
  }
};

// 取消报名
const handleCancelJoin = async (activity) => {
  actionLoading.value = {type: 'cancel', id: activity.id};
  try {
    await ElMessageBox.confirm(`确定要取消报名活动【${activity.title}】吗？`, '确认取消', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning',
    });
    await cancelJoinActivity(activity.id); // Corrected
    ElMessage.success('取消报名成功');
    // 刷新数据
    await fetchData();
  } catch (error) {
    if (error !== 'cancel') { // 用户点击取消时不提示
      console.error('取消报名失败:', error);
      // Error handled by interceptor
    }
  } finally {
    actionLoading.value = null;
  }
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchData();
});

// 监听分页变化 (TableView v-model 触发)
watch([currentPage, pageSize], () => {
  // 仅在分页变化时重新获取数据，Tab 切换由 handleTabChange 处理
  fetchData();
}, {immediate: false});

</script>

<style scoped>
/* 通用样式 */
.filter-container {
  margin-bottom: 16px;
}

/* 移动端样式 */
.activities-mobile-view {
  margin-top: 16px;
}

.activity-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.activity-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.activity-card-content {
  display: flex;
  flex-direction: column;
}

.activity-poster {
  height: 140px;
  width: 100%;
  border-radius: 4px;
  margin-bottom: 12px;
}

.activity-poster-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 36px;
}

.activity-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.activity-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-meta {
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.activity-location, .activity-time {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.activity-location .el-icon, .activity-time .el-icon {
  margin-right: 4px;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.mobile-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .activity-cards {
    grid-template-columns: 1fr;
  }

  :deep(.el-tabs__item) {
    padding: 0 12px;
  }
}
</style> 