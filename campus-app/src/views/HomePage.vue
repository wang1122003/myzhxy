<template>
  <div class="home-container">
    <el-row :gutter="20">
      <!-- 通知公告 -->
      <el-col :md="16" :sm="24" :xs="24" class="notice-col">
        <el-card
            v-loading="loadingNotices || loadingNoticeTypes"
            class="notice-card home-card"
        >
          <template #header>
            <div class="card-header">
              <span>通知公告</span>
              <!-- 跳转到专门的通知页面，如果存在 -->
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
              :height="tableHeight"
              style="width: 100%"
          >
            <!-- 固定高度以便滚动 -->
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

      <!-- 登录表单 -->
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
            <span>发布人：{{ currentNotice.publisher }}</span>
            <span>发布时间：{{ formatTime(currentNotice.publishTime) }}</span>
          </div>
          <el-divider/>
          <!-- 使用 v-html 渲染可能包含 HTML 的内容，注意 XSS 风险 -->
          <div
              class="notice-text"
              v-html="currentNotice.content"
          />
          <div
              v-if="currentNotice.attachments && currentNotice.attachments.length"
              class="notice-attachments"
          >
            <el-divider/>
            <h4>附件：</h4>
            <ul>
              <li
                  v-for="file in currentNotice.attachments"
                  :key="file.id"
              >
                <!-- 附件下载，添加 loading 状态 -->
                <el-link
                    :disabled="downloadingAttachment[file.id]"
                    :loading="downloadingAttachment[file.id]"
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
import {ElLink, ElMessage} from 'element-plus'
import {getNoticeById, getRecentNotices} from '@/api/notice'
import {login} from '@/api/user'
import {downloadFile} from '@/api/file'
import {getNoticeTypes} from '@/api/common'
import {Lock, User} from '@element-plus/icons-vue'

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
const noticeTypes = ref([])
const tableHeight = ref('400px')

const currentNotice = reactive({
  id: null,
  title: '',
  publisher: '',
  publishTime: '',
  content: '',
  attachments: []
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

// 检查并填充记住的用户名
onMounted(async () => {
  const rememberedUsername = localStorage.getItem('username')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.remember = true
  }
  // 并发获取通知和通知类型
  await Promise.all([
    fetchNotices(),
    fetchNoticeTypes()
  ])

  // 初始化并监听窗口大小变化来调整表格高度
  nextTick(() => {
    calculateTableHeight();
  });
  window.addEventListener('resize', calculateTableHeight);
})

const fetchNotices = async () => {
  loadingNotices.value = true
  try {
    const response = await getRecentNotices()
    notices.value = response.data || []
  } catch (error) {
    console.error("获取通知列表失败", error)
  } finally {
    loadingNotices.value = false
  }
}

const fetchNoticeTypes = async () => {
  loadingNoticeTypes.value = true
  try {
    const res = await getNoticeTypes()
    // 假设 API 返回 { data: [{ typeCode: 1, typeName: '系统通知', tagType: 'info' }, ...] }
    noticeTypes.value = res.data || []
  } catch (error) {
    console.error("获取通知类型失败", error)
  } finally {
    loadingNoticeTypes.value = false
  }
}

const noticeTypeMapComputed = computed(() => {
  const map = {}
  noticeTypes.value.forEach(type => {
    // 假设 API 返回的字段是 typeCode, typeName, tagType
    // 如果字段名不同，需要在这里调整
    map[type.typeCode] = {name: type.typeName, tag: type.tagType || 'info'}
  })
  return map
})

const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loginLoading.value = true
      login(loginForm).then(response => {
        const {token, user} = response.data
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(user))

        // **根据 userType 确定角色字符串并存储**
        let roleString = '';
        if (user && typeof user.userType === 'number') {
          switch (user.userType) {
            case 0:
              roleString = 'admin';
              break;
            case 1: // 假设 1 是学生
              roleString = 'student';
              break;
            case 2: // 假设 2 是教师
              roleString = 'teacher';
              break;
            default:
              roleString = 'unknown'; // 或者其他默认值
          }
          localStorage.setItem('role', roleString)
        } else {
          localStorage.removeItem('role') // 防止残留旧角色或无效角色
        }

        if (loginForm.remember) {
          localStorage.setItem('username', loginForm.username)
        } else {
          localStorage.removeItem('username')
        }

        ElMessage.success('登录成功')

        // **根据角色字符串重定向**
        if (roleString === 'admin') {
          router.push('/admin')
        } else if (roleString === 'teacher') {
          router.push('/teacher')
        } else if (roleString === 'student') {
          router.push('/student')
        } else {
          console.error('登录成功，但角色未知或无效:', user?.userType);
          router.push('/') // 未知角色或无角色，回到首页
        }
      }).catch(() => {
        ElMessage.error('用户名或密码错误')
      }).finally(() => {
        loginLoading.value = false
      })
    }
  })
}

// --- 通知相关方法 (需要实现) ---
// 查看通知详情
const viewNotice = async (row) => {
  loadingNoticeDetail.value = true
  noticeDialogVisible.value = true
  currentNotice.id = null
  try {
    const res = await getNoticeById(row.id)
    // 假设附件信息在 notice 详情中，格式为 [{ id: 1, name: 'xxx.pdf', url: '...' }]
    Object.assign(currentNotice, res.data)
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

// 计算表格高度的函数
const calculateTableHeight = () => {
  const windowHeight = window.innerHeight;
  // 根据窗口高度或其他因素动态计算，例如减去 Header 和 Footer 的高度以及一些边距
  // 这里的计算方式是一个示例，需要根据实际布局调整
  const calculatedHeight = windowHeight - 300; // 假设减去 300px 的其他元素高度
  tableHeight.value = calculatedHeight > 200 ? `${calculatedHeight}px` : '200px'; // 最小高度 200px
};

onBeforeUnmount(() => {
  // 组件卸载前移除监听器
  window.removeEventListener('resize', calculateTableHeight);
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
  height: 100%; /* 让卡片尝试填充其列的高度 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px; /* 统一标题字体大小 */
  font-weight: 600;
}

.login-header h2 {
  margin: 0; /* 移除 h2 的默认 margin */
  font-size: inherit; /* 继承 .card-header 的字体大小 */
  font-weight: inherit; /* 继承 .card-header 的字重 */
}

.el-table {
  font-size: 14px; /* 调整表格字体大小 */
}

.el-table .el-button {
  font-size: 13px; /* 调整表格内按钮字体大小 */
}

.el-form-item {
  margin-bottom: 18px; /* 调整表单项间距 */
}

.el-input,
.el-checkbox {
  font-size: 14px; /* 统一输入框和复选框字体 */
}

.el-button {
  font-size: 14px; /* 统一按钮字体大小 */
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

/* 响应式调整 - 确保在小屏幕上堆叠 */
@media (max-width: 991px) {
  /* Element Plus 的 md 断点 */
  .notice-col,
  .login-col {
    width: 100%; /* 宽度占满 */
  }

  .login-col {
    margin-top: 20px; /* 在登录框上方增加间距 */
  }

  /* 可以考虑在小屏幕上减小表格高度 */
  /* tableHeight ref 的计算逻辑已包含窗口大小变化 */
}
</style> 