<template>
  <div class="home-container">
    <el-row :gutter="20">
      <!-- 登录表单 (移动端优先显示) -->
      <el-col :md="8" :sm="24" :xs="24" class="login-col">
        <el-card class="login-card home-card">
          <div class="login-header card-header">
            <h2>用户登录</h2>
          </div>
          <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="rules"
              label-width="0"
          >
            <el-form-item prop="username">
              <el-input
                  v-model="loginForm.username"
                  clearable
                  placeholder="用户名"
              >
                <template #prefix>
                  <el-icon>
                    <User/>
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                  v-model="loginForm.password"
                  clearable
                  placeholder="密码"
                  show-password
                  type="password"
                  @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <el-icon>
                    <Lock/>
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="loginForm.remember">
                记住用户名
              </el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button
                  :loading="loginLoading"
                  style="width: 100%"
                  type="primary"
                  @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 通知公告 -->
      <el-col :md="16" :sm="24" :xs="24" class="notice-col">
        <el-card
            v-loading="loadingNotices || loadingNoticeTypes"
            class="notice-card home-card"
        >
          <template #header>
            <div class="card-header">
              <span>通知公告</span>
              <el-button
                  link
                  type="primary"
                  @click="viewAllNotices"
              >
                查看全部
              </el-button>
            </div>
          </template>
          <el-table
              :data="notices"
              style="width: 100%"
              empty-text=" " 
          >
            <el-table-column
                label="标题"
                prop="title"
                show-overflow-tooltip
            />
            <el-table-column
                label="类型"
                prop="type"
                width="100"
            >
              <template #default="scope">
                <el-tag :type="noticeTypeMapComputed[scope.row.type]?.tag || 'info'">
                  {{ noticeTypeMapComputed[scope.row.type]?.name || '其他' }}
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
                label="操作"
                width="100"
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
          <el-empty
              v-if="notices.length === 0 && !loadingNotices"
              description="暂无通知公告"
          />
        </el-card>
      </el-col>

    </el-row>

    <!-- 公告详情对话框 -->
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
          <!-- 使用 v-html 渲染可能包含 HTML 的内容，注意 XSS 风险 -->
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
import {computed, nextTick, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Lock, User} from '@element-plus/icons-vue'
import {useUserStore} from '@/stores/userStore'
import {getNotificationById, getRecentNotifications} from '@/api/notice'
import {login} from '@/api/user'
import {downloadFile} from '@/api/file'

// defineOptions({
//   name: 'HomePage'
// });

const router = useRouter()
const loginFormRef = ref(null)
const notices = ref([])
const loadingNotices = ref(false)
const loadingNoticeTypes = ref(false)
const loginLoading = ref(false)
const noticeDialogVisible = ref(false)
const loadingNoticeDetail = ref(false)
const downloadingAttachment = reactive({})
const tableHeight = ref('400px')

const currentNotice = reactive({
  id: null,
  title: '',
  publisher: '',
  publishTime: '',
  content: '',
  attachmentFiles: []
})

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  password: [{required: true, message: '请输入密码', trigger: 'blur'}]
}

const userStore = useUserStore(); // 获取 store 实例

// 直接定义静态的类型映射
const noticeTypeMap = {
  "SYSTEM": {name: "系统通知", tag: "info"},
  "COURSE": {name: "教学通知", tag: "success"},
  "ACTIVITY": {name: "活动公告", tag: "warning"},
  "GENERAL": {name: "通用", tag: "danger"}, // 假设数据库存的是 GENERAL
  "OTHER": {name: "其他", tag: "info"} // 添加一个默认的 OTHER 以防万一
};

// computed 保持不变，但现在基于静态 map
const noticeTypeMapComputed = computed(() => {
  // 可以选择直接返回 noticeTypeMap，或者如果需要响应式更新则保持 computed
  return noticeTypeMap;
  // const map = {};
  // noticeTypes.value.forEach(type => {
  //   map[type.typeName] = {name: type.typeName, tag: type.tagType || 'info'} 
  // });
  // return map;
});

onMounted(async () => {
  const rememberedUsername = localStorage.getItem('username');
  if (rememberedUsername) {
    loginForm.username = rememberedUsername;
    loginForm.remember = true;
  }

  // 只需获取通知，不再需要获取类型
  try {
    await fetchNotices();
    // await Promise.all([
    //   fetchNotices(),
    //   fetchNoticeTypes() // 移除类型获取
    // ]);
  } catch (error) {
    console.error("Error during initial data fetch:", error);
  }

  // 调整表格高度的逻辑
  await nextTick(() => {
    const noticeCard = document.querySelector('.notice-card .el-card__body');
    if (noticeCard) {
      const calculatedHeight = noticeCard.clientHeight - 40;
      tableHeight.value = `${Math.max(calculatedHeight, 200)}px`;
    }
  });
});

const fetchNotices = async () => {
  loadingNotices.value = true
  try {
    // 成功时，res 直接是数据数组
    const res = await getRecentNotifications()
    // 直接使用返回的数据，不再检查 code 或 success
    notices.value = Array.isArray(res) ? res.slice(0, 5) : []
    // if (res.code === 200 || res.success === true) {
    //   // 确保res.data存在且是数组
    //   notices.value = Array.isArray(res.data) ? res.data.slice(0, 5) : []
    // } else {
    //   console.error('获取最近通知失败:', res.message)
    //   notices.value = []
    // }
  } catch (error) {
    // catch 块仍然处理网络错误或拦截器抛出的业务错误
    console.error('获取最近通知异常:', error) 
    notices.value = []
  } finally {
    loadingNotices.value = false
  }
}

const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loginLoading.value = true
      try {
        const response = await login({
          username: loginForm.username,
          password: loginForm.password
        })

        if (response && response.token && response.user) {
          // 使用userStore设置token和用户信息
          const userStore = useUserStore()
          userStore.setToken(response.token)
          userStore.setUserInfo(response.user)
          
          // 根据 remember 状态存储或移除用户名
          if (loginForm.remember) {
            localStorage.setItem('username', loginForm.username)
          } else {
            localStorage.removeItem('username')
          }

          ElMessage.success('登录成功')

          // 根据角色重定向
          const role = userStore.userRole()
          console.log('[HomePage] Redirecting based on role:', role)
          if (role === 'admin') {
            router.push('/admin/notice')
          } else if (role === 'teacher') {
            router.push('/teacher')
          } else if (role === 'student') {
            router.push('/student')
          } else {
            router.push('/') // Fallback to home if role is unexpected
          }
        } else {
          // 使用后端返回的错误消息，如果存在的话
          ElMessage.error(response?.message || '登录失败，请检查用户名或密码')
        }
      } catch (error) {
        console.error('登录请求失败:', error)
        // 检查 error 对象是否有更具体的后端错误信息
        const backendMessage = error?.response?.data?.message || error?.message
        ElMessage.error(backendMessage || '登录请求失败')
      } finally {
        loginLoading.value = false
      }
    } else {
      console.log('表单验证失败')
      return false
    }
  })
}

// --- 通知相关方法 (需要实现) ---
// 查看通知详情
const viewNotice = async (row) => {
  loadingNoticeDetail.value = true
  noticeDialogVisible.value = true
  currentNotice.id = null // 重置详情，避免显示旧数据
  try {
    // 成功时 res 直接是数据对象
    const res = await getNotificationById(row.id)
    // 直接将返回的数据对象合并到 currentNotice
    Object.assign(currentNotice, res)
    // Object.assign(currentNotice, res.data) // 旧的访问方式
  } catch (err) {
    console.error("获取通知详情失败", err)
    ElMessage.error("获取通知详情失败")
    noticeDialogVisible.value = false
  } finally {
    loadingNoticeDetail.value = false
  }
}

// 查看全部通知 (跳转到通知列表页)
const viewAllNotices = () => {
  router.push('/notices') // 跳转到 /notices 路由
}

// 格式化时间 (简单实现)
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    // 可以根据需要调整格式
    return date.toLocaleString('zh-CN', {hour12: false})
  } catch (e) {
    return timeStr // 格式化失败返回原值
  }
}

// 下载附件
const downloadAttachment = async (file) => {
  if (!file || !file.id) {
    ElMessage.warning('无效的文件信息')
    return
  }
  downloadingAttachment[file.id] = true
  try {
    const response = await downloadFile(file.id)
    // 从响应头获取文件名
    const contentDisposition = response.headers['content-disposition']
    let filename = file.name // 默认使用文件对象中的名字
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename\*?=UTF-8''(.*)$/i) || contentDisposition.match(/filename="(.*)"$/i)
      if (filenameMatch && filenameMatch[1]) {
        filename = decodeURIComponent(filenameMatch[1])
      }
    }

    // 创建 Blob 对象和下载链接
    const blob = new Blob([response.data], {type: response.headers['content-type']})
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success(`附件 ${filename} 下载成功`)
  } catch (error) {
    console.error("下载附件失败", error)
    ElMessage.error(`下载附件 ${file.name} 失败`)
  } finally {
    downloadingAttachment[file.id] = false
  }
}

onBeforeUnmount(() => {
  // 组件卸载前移除监听器
  // window.removeEventListener('resize', calculateTableHeight); // calculateTableHeight 已移除，无需再移除监听器
});
</script>

<script>
export default {
  name: 'HomePage'
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.home-card {
  margin-bottom: 20px;
}

.notice-card {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.login-header h2 {
  margin: 0;
  font-size: inherit;
  font-weight: inherit;
}

.el-table {
  font-size: 14px;
}

.el-table .el-button {
  font-size: 13px;
}

.el-form-item {
  margin-bottom: 18px;
}

.el-input {
  font-size: 14px;
}

.el-button {
  font-size: 14px;
}

.notice-content {
  line-height: 1.8;
}

.notice-info {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.notice-info span + span {
  margin-left: 20px;
}

.notice-text {
  font-size: 15px;
  color: #303133;
}

.notice-attachments h4 {
  margin-top: 15px;
  margin-bottom: 10px;
  font-size: 14px;
}
.notice-attachments ul {
  list-style: none;
  padding-left: 0;
}
.notice-attachments li {
  margin-bottom: 5px;
}

.notice-attachments .el-link {
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 991px) {
  .notice-col,
  .login-col {
    width: 100%;
  }

  .notice-col {
    margin-top: 20px;
  }
}
</style> 