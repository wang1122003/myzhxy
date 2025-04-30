<template>
  <PageContainer title="校园活动">
    <FilterForm
        :items="filterItems"
        :model="searchParams"
        :show-add-button="false"
        @reset="handleReset"
        @search="handleSearch"
    />

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部活动" name="all"/>
      <el-tab-pane label="我报名的" name="enrolled"/>
      <!-- 可以添加 "已结束" 等 Tab -->
    </el-tabs>

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
  </PageContainer>
</template>

<script setup>
import {computed, onMounted, reactive, ref, watch, h, resolveComponent} from 'vue';
import {useRouter} from 'vue-router';
import {ElMessage, ElMessageBox, ElTag} from 'element-plus';
import {Calendar, Location, Search} from '@element-plus/icons-vue';
// import { getAllActivities, getMyActivities, joinActivity as joinActivityApi, cancelJoinActivity } from '@/api/activity';
// import { studentListActivities, studentGetMyActivities, studentJoinActivity, studentCancelJoinActivity } from '@/api/user';
import {getAllActivities, getMyActivities, joinActivity, cancelJoinActivity} from '@/api/activity'; // Corrected: Use activity.js
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

    // 处理活动列表
    if (activitiesRes && activitiesRes.code === 200) {
      activityList.value = activitiesRes.data?.records || [];
      total.value = activitiesRes.data?.total || 0;
    } else {
      console.warn('活动API返回非标准格式:', activitiesRes);
      // 尝试直接使用返回的数据
      if (Array.isArray(activitiesRes)) {
        activityList.value = activitiesRes;
        total.value = activitiesRes.length;
      } else if (activitiesRes && activitiesRes.records) {
        activityList.value = activitiesRes.records;
        total.value = activitiesRes.total || activitiesRes.records.length;
      } else {
        activityList.value = [];
        total.value = 0;
      }
    }

    // 处理我参与的活动
    if (myActivitiesRes && myActivitiesRes.code === 200) {
      myActivityIds.value = new Set((myActivitiesRes.data || []).map(a => a.id));
    } else if (Array.isArray(myActivitiesRes)) {
      myActivityIds.value = new Set(myActivitiesRes.map(a => a.id));
    } else {
      myActivityIds.value = new Set();
    }
  } catch (error) {
    console.error('获取活动数据失败', error);
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
/* Scoped styles if needed */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 