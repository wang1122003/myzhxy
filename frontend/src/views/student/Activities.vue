<template>
  <!-- 页面容器，提供统一的标题和布局 -->
  <PageContainer title="校园活动">
    <!-- 筛选区域容器 -->
    <div class="filter-container">
      <!-- 筛选表单组件 -->
      <FilterForm
          :items="filterItems"
          :model="searchParams"
          :show-add-button="false"
          @reset="handleReset"
          @search="handleSearch"
      />
    </div>

    <!-- Element Plus 标签页，用于切换"全部活动"和"我报名的" -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部活动" name="all"/>
      <el-tab-pane label="我报名的" name="enrolled"/>
      <!-- 可以根据需求添加更多 Tab，例如 "已结束" 等 -->
    </el-tabs>

    <!-- 移动端卡片视图，当 isMobileView 为 true 时显示 -->
    <div v-if="isMobileView" class="activities-mobile-view">
      <!-- 加载状态指示器 -->
      <div v-loading="loading">
        <!-- 当活动列表为空时显示空状态提示 -->
        <el-empty v-if="activityList.length === 0" description="暂无活动数据"/>

        <!-- 活动卡片列表 -->
        <div v-else class="activity-cards">
          <!-- 遍历活动列表，为每个活动创建一个卡片 -->
          <el-card
              v-for="activity in activityList"
              :key="activity.id"
              class="activity-card"
              @click="viewActivityDetail(activity)"
          >
            <!-- 卡片内容容器 -->
            <div class="activity-card-content">
              <!-- 活动海报图片，如果存在则显示 -->
              <el-image
                  v-if="activity.posterUrl"
                  :src="activity.posterUrl"
                  class="activity-poster"
                  fit="cover"
              />
              <!-- 如果没有海报，则显示占位符 -->
              <div v-else class="activity-poster activity-poster-empty">
                <el-icon>
                  <Picture/>
                </el-icon>
              </div>

              <!-- 活动信息区域 -->
              <div class="activity-info">
                <!-- 活动标题 -->
                <h3 class="activity-title">{{ activity.title }}</h3>
                <!-- 活动元数据（地点、时间） -->
                <div class="activity-meta">
                  <!-- 活动地点 -->
                  <p class="activity-location">
                    <el-icon>
                      <Location/>
                    </el-icon>
                    {{ activity.location || '地点未知' }}
                  </p>
                  <!-- 活动开始时间，格式化显示 -->
                  <p class="activity-time">
                    <el-icon>
                      <Calendar/>
                    </el-icon>
                    {{ formatDateTime(activity.startTime, 'MM-DD HH:mm') }}
                  </p>
                </div>

                <!-- 活动卡片底部（状态标签、操作按钮） -->
                <div class="activity-footer">
                  <!-- 活动状态标签 -->
                  <el-tag :type="getStatusTagType(activity)" size="small">
                    {{ formatStatus(activity) }}
                  </el-tag>

                  <!-- 操作按钮区域 -->
                  <div class="action-buttons">
                    <!-- 如果已报名且活动正在报名中，显示"取消报名"按钮 -->
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
                    <!-- 如果未报名、活动可报名且有名额，显示"报名"按钮 -->
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

        <!-- 移动端分页组件 -->
        <div class="mobile-pagination">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 桌面端表格视图，当 isMobileView 为 false 时显示 -->
    <div v-else>
      <!-- 表格视图组件 -->
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
// 引入活动相关的 API 函数 (使用 activity.js 中的函数)
import {cancelJoinActivity, getAllActivities, getMyActivities, joinActivity} from '@/api/activity';
// 引入日期时间格式化工具函数
import {formatDateTime} from '@/utils/formatters';
// 引入自定义的页面容器、表格视图和筛选表单组件
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import TableView from '@/views/ui/TableView.vue';
import FilterForm from '@/views/ui/AdvancedFilterForm.vue';

// --- 响应式状态定义 ---
const router = useRouter(); // Vue Router 实例
const loading = ref(false); // 页面或表格加载状态
const activityList = ref([]); // 当前显示的活动列表数据
const myActivityIds = ref(new Set()); // 存储当前学生已报名活动 ID 的 Set 集合，用于快速判断是否已报名
const total = ref(0); // 活动总数，用于分页
const currentPage = ref(1); // 当前页码
const pageSize = ref(10); // 每页显示的活动数量
const searchParams = reactive({keyword: ''}); // 搜索参数，目前只有关键词
const activeTab = ref('all'); // 当前激活的标签页，'all' 或 'enrolled'
const actionLoading = ref(null); // 控制报名/取消报名按钮的加载状态，格式为 { type: 'join'/'cancel', id: activityId }
const isMobileView = ref(window.innerWidth < 768); // 判断是否为移动端视图

// 监听窗口大小变化，动态更新 isMobileView
window.addEventListener('resize', () => {
  isMobileView.value = window.innerWidth < 768;
});

// --- 计算属性 ---

// 筛选表单 (FilterForm) 的配置项
const filterItems = computed(() => [
  {
    type: 'input', // 输入框类型
    label: '关键词', // 标签
    prop: 'keyword', // 对应 searchParams 中的属性
    placeholder: '搜索活动标题/地点', // 占位符
    props: {clearable: true, style: {width: '300px'}} // 传递给 ElInput 的属性
  }
]);

// 表格视图 (TableView) 的列配置
const tableColumns = computed(() => [
  {
    prop: 'posterUrl', // 对应数据源的属性
    label: '封面', // 列标题
    width: 100, // 列宽度
    slots: { // 使用 slot 自定义单元格渲染
      default: (scope) => { // scope 包含 row, column, $index 等信息
        const url = scope.row.posterUrl;
        // 如果有海报 URL，则渲染 el-image 组件
        return url
            ? h(resolveComponent('el-image'), { // 使用 h 函数创建 VNode
              src: url,
              fit: 'cover', // 图片填充方式
              style: 'width: 80px; height: 50px; border-radius: 4px;', // 样式
              lazy: true, // 懒加载
              previewSrcList: [url], // 开启图片预览，提供预览列表
              previewTeleported: true // 图片预览插入到 body
            })
            // 否则显示 "无封面" 文本
            : h('span', {style: 'color: #ccc;'}, '无封面');
      }
    }
  },
  {prop: 'title', label: '活动标题', minWidth: 200, showOverflowTooltip: true}, // 活动标题，最小宽度，超出显示 tooltip
  {prop: 'location', label: '活动地点', width: 150}, // 活动地点
  {
    prop: 'timeRange', // 自定义属性名，方便 formatter 处理
    label: '活动时间', // 列标题
    width: 320, // 列宽度
    // 使用 formatter 函数格式化显示时间范围
    formatter: (row) => `${formatDateTime(row.startTime, 'YYYY-MM-DD HH:mm')} 至 ${formatDateTime(row.endTime, 'YYYY-MM-DD HH:mm')}`
  },
  {
    prop: 'enrollDeadline', // 报名截止时间
    label: '报名截止',
    width: 160,
    formatter: (row) => formatDateTime(row.enrollDeadline, 'YYYY-MM-DD HH:mm') // 格式化显示
  },
  {
    label: '报名/名额', // 列标题
    width: 120,
    // 格式化显示报名人数和总名额
    formatter: (row) => `${row.currentParticipants || 0} / ${row.maxParticipants > 0 ? row.maxParticipants : '不限'}`
  },
  {
    prop: 'status', // 活动状态
    label: '状态',
    width: 100,
    slots: { // 使用 slot 自定义渲染状态标签
      default: (scope) => h(resolveComponent('el-tag'), { // 渲染 ElTag 组件
        type: getStatusTagType(scope.row), // 根据活动状态获取标签类型 (颜色)
        size: 'small' // 小尺寸标签
      }, () => formatStatus(scope.row)) // 标签内容为格式化后的状态文本
    }
  },
]);

// 表格视图 (TableView) 操作列的配置
const actionColumnConfig = computed(() => ({
  width: 150, // 列宽度
  fixed: 'right', // 固定在右侧
  buttons: (row) => { // 动态生成按钮配置的函数，接收当前行数据 row
    const actions = []; // 存储按钮配置的数组
    // 所有活动都显示"查看详情"按钮
    actions.push({label: '查看详情', type: 'primary', link: true, event: 'view-detail'});

    const enrolled = myActivityIds.value.has(row.id); // 判断当前学生是否已报名该活动
    const canEnroll = row.status === 'PUBLISHED' || row.status === 1; // 判断活动是否处于可报名状态 (兼容旧的状态码或新状态 PUBLISHED)
    const isLoading = actionLoading.value?.id === row.id; // 判断当前行是否正在进行操作 (报名/取消)

    // 如果已报名且活动可报名，添加"取消报名"按钮
    if (enrolled && canEnroll) {
      actions.push({
        label: '取消报名',
        type: 'danger', // 红色按钮
        link: true, // 链接样式
        event: 'cancel-join', // 触发的事件名
        loading: isLoading && actionLoading.value?.type === 'cancel' // 控制按钮加载状态
      });
    }
    // 如果未报名、活动可报名、且有名额（或名额不限），添加"报名"按钮
    else if (!enrolled && canEnroll && (row.maxParticipants === 0 || (row.currentParticipants || 0) < row.maxParticipants)) {
      actions.push({
        label: '报名',
        type: 'success', // 绿色按钮
        link: true,
        event: 'join-activity',
        loading: isLoading && actionLoading.value?.type === 'join'
      });
    }
    return actions; // 返回按钮配置数组
  }
}));

// --- 方法 ---

/**
 * 格式化活动状态显示文本
 * @param {object} activity 活动对象
 * @returns {string} 格式化后的状态文本
 */
const formatStatus = (activity) => {
  const enrolled = myActivityIds.value.has(activity.id);
  // 优先判断当前学生是否已报名
  if (enrolled) return '已报名';

  // 根据后端返回的活动状态进行判断
  switch (activity.status) {
    case 1: // 兼容旧的状态码: 报名中
    case 'PUBLISHED': // 新状态: 已发布/报名中
      // 如果有名额限制且当前报名人数已满
      return (activity.maxParticipants > 0 && (activity.currentParticipants || 0) >= activity.maxParticipants)
          ? '名额已满'
          : '报名中';
    case 2: // 兼容旧的状态码: 进行中
    case 'ONGOING': // 新状态: 进行中
      return '进行中';
    case 3: // 兼容旧的状态码: 已结束
    case 'FINISHED': // 新状态: 已结束
      return '已结束';
    case 0: // 兼容旧的状态码: 已取消
    case 'CANCELLED': // 新状态: 已取消
      return '已取消';
    case 'UPCOMING': // 新增状态: 即将开始
      return '即将开始';
    default: // 未知状态
      return '未知';
  }
};

/**
 * 根据活动状态获取 Element Plus Tag 组件的类型 (颜色)
 * @param {object} activity 活动对象
 * @returns {string} Tag 类型 ('success', 'primary', 'warning', 'info', 'danger')
 */
const getStatusTagType = (activity) => {
  const enrolled = myActivityIds.value.has(activity.id);
  // 如果已报名，显示 success (绿色)
  if (enrolled) return 'success';

  switch (activity.status) {
    case 1:
    case 'PUBLISHED':
      // 如果名额已满，显示 danger (红色)，否则显示 primary (蓝色)
      return (activity.maxParticipants > 0 && (activity.currentParticipants || 0) >= activity.maxParticipants)
          ? 'danger'
          : 'primary';
    case 2:
    case 'ONGOING':
      return 'warning'; // 进行中显示 warning (橙色)
    case 3:
    case 'FINISHED':
      return 'info'; // 已结束显示 info (灰色)
    case 0:
    case 'CANCELLED':
      return 'danger'; // 已取消显示 danger (红色)
    case 'UPCOMING':
      return 'info'; // 即将开始显示 info (灰色)
    default:
      return 'info'; // 默认为 info
  }
};

/**
 * 获取活动数据
 * 根据 activeTab 的值决定是获取所有活动还是我报名的活动
 * 同时会获取当前用户已报名的所有活动 ID，用于状态判断
 * @param {string} [fetchType=activeTab.value] 获取类型 ('all' 或 'enrolled')，默认为当前激活的 tab
 */
const fetchData = async (fetchType = activeTab.value) => {
  loading.value = true; // 开始加载，显示加载状态
  activityList.value = []; // 清空现有列表
  total.value = 0; // 重置总数
  // myActivityIds 在 Promise.all 中获取，这里无需重置，避免闪烁

  try {
    let activitiesApiCall; // 定义获取活动列表的 API 调用 Promise
    // 根据 fetchType 构造不同的 API 请求参数
    if (fetchType === 'enrolled') {
      // 获取我报名的活动
      activitiesApiCall = getMyActivities({
        page: currentPage.value,
        size: pageSize.value,
        // 在"我报名的"Tab 下也支持关键词搜索
        keyword: searchParams.keyword || undefined // 如果 keyword 为空则不传
      });
    } else { // fetchType === 'all' 或其他情况，获取全部活动
      activitiesApiCall = getAllActivities({
        page: currentPage.value,
        size: pageSize.value,
        // 可以在此添加其他筛选条件，如 status
        // status: searchParams.status || undefined,
        keyword: searchParams.keyword || undefined
      });
    }

    // 使用 Promise.all 并发获取活动列表和我报名的活动 ID 列表
    // 注意：getMyActivities() 不带分页参数，获取的是所有已报名的活动 ID
    const [activitiesRes, myActivitiesIdsRes] = await Promise.all([
      activitiesApiCall, // 获取当前页的活动列表 (全部或我报名的)
      getMyActivities({size: 1000}) // 获取所有我报名的活动 ID（用一个较大的 size 来近似获取全部）
    ]);

    // --- 处理活动列表响应 ---
    console.log(`获取到 ${fetchType} 活动数据响应:`, activitiesRes);

    // 优先处理用户提供的直接JSON结构 { list: [], total: N, ... }
    if (activitiesRes && Array.isArray(activitiesRes.list) && typeof activitiesRes.total !== 'undefined') {
      activityList.value = activitiesRes.list;
      total.value = activitiesRes.total;
      // console.log('成功解析直接的活动列表数据。');
      // 如果响应中包含 pageSize 和 currentPage，可以考虑更新组件的 ref，但这需要小心处理以避免无限循环
      // 例如，如果分页组件的 pageSize 是双向绑定的，并且 watch 了 pageSize，则更新 pageSize.value 会触发重新获取数据
      // if (typeof activitiesRes.pageSize === 'number' && pageSize.value !== activitiesRes.pageSize) {
      //   pageSize.value = activitiesRes.pageSize;
      // }
      // if (typeof activitiesRes.currentPage === 'number' && currentPage.value !== activitiesRes.currentPage) {
      //   // currentPage.value = activitiesRes.currentPage; // 更新这个要非常小心 watch 触发的 refetch
      // }
    }
    // 备选：处理常见的 { code: 200, data: { ... } } 包装结构
    else if (activitiesRes && activitiesRes.code === 200 && activitiesRes.data) {
      const data = activitiesRes.data;
      if (Array.isArray(data.list) && typeof data.total !== 'undefined') { // 对应旧格式: { data: { list: [], total, ... } }
        activityList.value = data.list;
        total.value = data.total;
        // console.log('成功解析包装后的活动列表数据 (data.list)。');
      } else if (Array.isArray(data.records) && typeof data.total !== 'undefined') { // 对应新格式: { data: { records: [], total, ... } }
        activityList.value = data.records;
        total.value = data.total;
        // console.log('成功解析包装后的活动列表数据 (data.records)。');
      } else if (Array.isArray(data)) { // 对应直接返回数组: { data: [] }
        activityList.value = data;
        total.value = data.length;
        //  console.log('成功解析包装后的活动列表数据 (data is array)。');
      } else {
        console.warn('未能识别的包装后活动数据格式 (activitiesRes.data):', data);
        ElMessage.error(activitiesRes.message || '活动数据格式错误 (code 200, data unknown)');
        activityList.value = []; // 清空数据
        total.value = 0;
      }
    }
    // 其他所有情况，视为错误或无法处理的格式
    else {
      console.error('API返回错误或未能识别的活动数据格式:', activitiesRes);
      ElMessage.error(activitiesRes?.message || `获取${fetchType === 'enrolled' ? '我报名的' : ''}活动列表失败`);
      activityList.value = []; // 清空数据
      total.value = 0;
    }
    console.log('处理后的活动列表:', activityList.value, '总数:', total.value);

    // --- 处理我报名的活动 ID 响应 ---
    console.log('获取到已报名活动 ID 数据响应:', myActivitiesIdsRes);
    if (myActivitiesIdsRes && myActivitiesIdsRes.code === 200) {
      // 预期格式 { code, msg, data: { records: [{id: 1}, {id: 2}] } } 或 { code, msg, data: {records: []} }
      const data = myActivitiesIdsRes.data;
      if (data && data.records && Array.isArray(data.records)) {
        // 新格式: data: { records: [{activityId: 1}, {activityId: 2}] }
        myActivityIds.value = new Set(data.records.map(a => a.activityId || a.id));
      } else if (Array.isArray(data)) {
        // 旧格式: data: [{activityId: 1}, {activityId: 2}]
        myActivityIds.value = new Set(data.map(a => a.activityId || a.id));
      } else {
        // 无法解析的格式
        console.warn('无法解析的已报名活动数据格式:', data);
        myActivityIds.value = new Set();
      }
    } else if (Array.isArray(myActivitiesIdsRes)) {
      // 兼容直接返回数组的情况
      myActivityIds.value = new Set(myActivitiesIdsRes.map(a => a.activityId || a.id));
    } else {
      // 获取失败或格式错误
      ElMessage.error(myActivitiesIdsRes?.message || '获取已报名活动信息失败');
      myActivityIds.value = new Set();
    }
    console.log('更新后的已报名活动 IDs:', Array.from(myActivityIds.value));

  } catch (error) {
    // 网络错误或其他异常
    console.error(`获取活动数据时发生错误 (类型: ${fetchType}):`, error);
    ElMessage.error('获取活动数据时发生网络或服务器错误');
    // 重置状态
    activityList.value = [];
    total.value = 0;
    myActivityIds.value = new Set();
  } finally {
    loading.value = false; // 加载结束，隐藏加载状态
  }
};

// 处理搜索按钮点击事件
const handleSearch = () => {
  currentPage.value = 1; // 搜索时重置到第一页
  fetchData(activeTab.value); // 使用当前激活的 Tab 类型获取数据
};

// 处理重置按钮点击事件
const handleReset = () => {
  searchParams.keyword = ''; // 清空关键词
  currentPage.value = 1; // 重置到第一页
  fetchData(activeTab.value); // 使用当前激活的 Tab 类型获取数据
}

// 处理标签页切换事件
const handleTabChange = (tabName) => {
  // activeTab 已通过 v-model 自动更新
  currentPage.value = 1; // 切换 Tab 时重置到第一页
  fetchData(tabName); // 根据切换到的 Tab 名称获取数据
};

/**
 * 查看活动详情
 * @param {object} activity 活动对象 (由 TableView 或 Card 点击时传递)
 */
const viewActivityDetail = (activity) => {
  router.push(`/student/activity/${activity.id}`); // 跳转到详情页路由
};

/**
 * 处理报名活动按钮点击事件
 * @param {object} activity 活动对象 (由 TableView 或 Card 按钮点击时传递)
 */
const handleJoinActivity = async (activity) => {
  actionLoading.value = {type: 'join', id: activity.id}; // 设置按钮加载状态
  try {
    // 调用报名 API
    const res = await joinActivity(activity.id);
    if (res.code === 200) {
      ElMessage.success('报名成功');
      // 报名成功后刷新当前页数据 (需要重新获取列表和我的报名 ID)
      await fetchData(activeTab.value);
    } else {
      ElMessage.error(res.message || '报名失败');
    }
  } catch (error) {
    // API 请求拦截器通常会处理错误消息，这里可以只 log
    console.error('报名失败:', error);
    // ElMessage.error('报名时发生错误'); // 拦截器会显示错误，这里一般不需要重复显示
  } finally {
    actionLoading.value = null; // 清除按钮加载状态
  }
};

/**
 * 处理取消报名按钮点击事件
 * @param {object} activity 活动对象 (由 TableView 或 Card 按钮点击时传递)
 */
const handleCancelJoin = async (activity) => {
  actionLoading.value = {type: 'cancel', id: activity.id}; // 设置按钮加载状态
  try {
    // 弹出确认框
    await ElMessageBox.confirm(`确定要取消报名活动【${activity.title}】吗？`, '确认取消', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning',
    });
    // 用户确认后，调用取消报名 API
    const res = await cancelJoinActivity(activity.id);
    if (res.code === 200) {
      ElMessage.success('取消报名成功');
      // 取消成功后刷新当前页数据
      await fetchData(activeTab.value);
    } else {
      ElMessage.error(res.message || '取消报名失败');
    }
  } catch (error) {
    // 如果是用户点击了确认框的"取消"按钮 (error === 'cancel')，则不提示错误
    if (error !== 'cancel') {
      console.error('取消报名失败:', error);
      // ElMessage.error('取消报名时发生错误'); // 拦截器会处理
    }
  } finally {
    actionLoading.value = null; // 清除按钮加载状态
  }
};

/**
 * 处理分页组件页码变化事件 (适配 el-pagination 的 current-change 事件)
 * @param {number} newPage 变化后的新页码
 */
const handleCurrentChange = (newPage) => {
  // currentPage 已经通过 v-model:current-page 更新
  // 只需要调用 fetchData 获取新页码的数据即可
  fetchData(activeTab.value);
};

// --- 生命周期钩子 ---

// 组件挂载后执行
onMounted(() => {
  fetchData(); // 首次加载数据 (默认获取 'all' 类型)
});

// 监听 currentPage 或 pageSize 的变化 (由 TableView 的 v-model 触发)
watch([currentPage, pageSize], () => {
  // 确保不是首次加载或 Tab 切换触发的，而是真正的分页操作触发
  // fetchData 函数内部会使用最新的 currentPage 和 pageSize
  fetchData(activeTab.value);
}, {immediate: false}); // immediate: false 避免挂载时重复调用

</script>

<style scoped>
/* 通用样式 */
.filter-container {
  margin-bottom: 16px; /* 筛选区域下方外边距 */
}

/* 移动端视图特定样式 */
.activities-mobile-view {
  margin-top: 16px; /* 移动端视图上方外边距 */
}

.activity-cards {
  display: grid; /* 使用网格布局 */
  /* 自动填充列，每列最小宽度 280px，最大宽度 1fr (平均分配剩余空间) */
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px; /* 网格项之间的间距 */
}

.activity-card {
  cursor: pointer; /* 鼠标悬停时显示手型光标 */
  transition: transform 0.3s, box-shadow 0.3s; /* 添加过渡效果 */
}

.activity-card:hover {
  transform: translateY(-5px); /* 鼠标悬停时向上移动 */
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1); /* 添加阴影效果 */
}

.activity-card-content {
  display: flex;
  flex-direction: column; /* 内容垂直排列 */
}

.activity-poster {
  height: 140px; /* 海报固定高度 */
  width: 100%; /* 海报宽度占满卡片 */
  border-radius: 4px; /* 圆角 */
  margin-bottom: 12px; /* 海报下方外边距 */
  object-fit: cover; /* 图片保持比例裁剪填充 */
}

.activity-poster-empty {
  display: flex;
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  background-color: #f5f7fa; /* 浅灰色背景 */
  color: #909399; /* 灰色图标 */
  font-size: 36px; /* 图标大小 */
}

.activity-info {
  flex: 1; /* 占据剩余空间 */
  display: flex;
  flex-direction: column; /* 信息内容垂直排列 */
}

.activity-title {
  font-size: 16px; /* 标题字号 */
  font-weight: bold; /* 粗体 */
  margin-bottom: 8px; /* 标题下方外边距 */
  /* 多行文本溢出显示省略号 (需要 Webkit 内核支持) */
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多显示 2 行 */
  -webkit-box-orient: vertical;
  overflow: hidden; /* 隐藏溢出部分 */
}

.activity-meta {
  margin-bottom: 12px; /* 元数据区域下方外边距 */
  font-size: 14px; /* 字号 */
  color: #606266; /* 字体颜色 */
}

.activity-location, .activity-time {
  display: flex; /* 使用 flex 布局使图标和文本同行 */
  align-items: center; /* 垂直居中对齐 */
  margin-bottom: 4px; /* 行间距 */
}

/* 图标右侧间距 */
.activity-location .el-icon, .activity-time .el-icon {
  margin-right: 4px;
}

.activity-footer {
  display: flex; /* 使用 flex 布局 */
  justify-content: space-between; /* 两端对齐 */
  align-items: center; /* 垂直居中 */
  margin-top: auto; /* 将底部推到卡片最下方 */
}

.action-buttons {
  display: flex;
  gap: 8px; /* 按钮之间的间距 */
}

.mobile-pagination {
  margin-top: 20px; /* 分页器上方外边距 */
  display: flex;
  justify-content: center; /* 分页器居中显示 */
}

/* 响应式设计：屏幕宽度小于 480px 时应用 */
@media (max-width: 480px) {
  .activity-cards {
    grid-template-columns: 1fr; /* 变为单列布局 */
  }

  /* 调整 Tab 的内边距，使其在小屏幕上不占过多空间 */
  :deep(.el-tabs__item) {
    padding: 0 12px;
  }
}
</style> 