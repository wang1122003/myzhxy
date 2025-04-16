<template>
  <div class="home-container">
    <!-- 未登录状态显示简介和登录框 -->
    <div v-if="!isLoggedIn" class="login-section">
      <el-row :gutter="20">
        <el-col :span="16">
          <div class="system-intro">
            <h1>欢迎使用校园管理系统</h1>
            <p>
              本系统提供学生、教师和管理员全方位的校园信息管理功能，包括课程管理、成绩查询、校园活动、文件管理等多种服务。</p>
            <div class="feature-list">
              <div class="feature-item">
                <el-icon>
                  <Calendar/>
                </el-icon>
                <div class="feature-text">
                  <h3>课程管理</h3>
                  <p>便捷查询课表，掌握课程信息</p>
                </div>
              </div>
              <div class="feature-item">
                <el-icon>
                  <DocumentChecked/>
                </el-icon>
                <div class="feature-text">
                  <h3>成绩查询</h3>
                  <p>实时查看成绩，跟踪学习进度</p>
                </div>
              </div>
              <div class="feature-item">
                <el-icon>
                  <Star/>
                </el-icon>
                <div class="feature-text">
                  <h3>校园活动</h3>
                  <p>参与丰富活动，拓展校园生活</p>
                </div>
              </div>
              <div class="feature-item">
                <el-icon>
                  <ChatDotRound/>
                </el-icon>
                <div class="feature-text">
                  <h3>校园论坛</h3>
                  <p>交流学习心得，分享校园生活</p>
                </div>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <el-card class="login-card">
            <div class="login-header">
              <h2>用户登录</h2>
            </div>
            <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
              <el-form-item prop="username">
                <el-input
                    v-model="loginForm.username"
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
                    placeholder="密码"
                    show-password
                    type="password"
                >
                  <template #prefix>
                    <el-icon>
                      <Lock/>
                    </el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
              </el-form-item>
              <el-form-item>
                <el-button style="width: 100%" type="primary" @click="handleLogin">登录</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 已登录状态显示通知和快速导航 -->
    <div v-else>
      <el-row :gutter="20">
        <!-- 通知公告 -->
        <el-col :span="16">
          <el-card class="notice-card">
            <template #header>
              <div class="card-header">
                <span>通知公告</span>
                <el-button type="text" @click="viewAllNotices">查看全部</el-button>
              </div>
            </template>
            <el-table :data="notices" style="width: 100%">
              <el-table-column label="标题" prop="title"/>
              <el-table-column label="类型" prop="type" width="100">
                <template #default="scope">
                  <el-tag :type="getTypeTag(scope.row.type)">
                    {{ getTypeName(scope.row.type) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="发布时间" prop="publishTime" width="180"/>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button type="text" @click="viewNotice(scope.row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <!-- 快速导航 -->
        <el-col :span="8">
          <el-card class="quick-nav-card">
            <template #header>
              <div class="card-header">
                <span>快速导航</span>
              </div>
            </template>
            <div class="nav-grid">
              <div class="nav-item" @click="navigateTo('forum')">
                <el-icon>
                  <ChatDotRound/>
                </el-icon>
                <span>校园论坛</span>
              </div>
              <div class="nav-item" @click="navigateTo('schedule')">
                <el-icon>
                  <Calendar/>
                </el-icon>
                <span>课表查询</span>
              </div>
              <div class="nav-item" @click="navigateTo('activity')">
                <el-icon>
                  <Star/>
                </el-icon>
                <span>活动中心</span>
              </div>
              <div class="nav-item" @click="navigateTo('file')">
                <el-icon>
                  <Folder/>
                </el-icon>
                <span>文件中心</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 公告详情对话框 -->
    <el-dialog
        v-model="noticeDialogVisible"
        :title="currentNotice.title"
        width="50%"
    >
      <div class="notice-content">
        <div class="notice-info">
          <span>发布人：{{ currentNotice.publisher }}</span>
          <span>发布时间：{{ currentNotice.publishTime }}</span>
        </div>
        <div class="notice-text">{{ currentNotice.content }}</div>
        <div v-if="currentNotice.attachments && currentNotice.attachments.length" class="notice-attachments">
          <h4>附件：</h4>
          <ul>
            <li v-for="file in currentNotice.attachments" :key="file.id">
              <el-link :href="file.url" target="_blank">{{ file.name }}</el-link>
            </li>
          </ul>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getNoticeById, getRecentNotices} from '@/api/notice'
import {login} from '@/api/user'
import {Calendar, ChatDotRound, DocumentChecked, Folder, Lock, Star, User} from '@element-plus/icons-vue'

export default {
  name: 'HomePage',
  components: {
    ChatDotRound,
    Calendar,
    Star,
    Folder,
    User,
    Lock,
    DocumentChecked
  },
  setup() {
    const router = useRouter()
    const loginFormRef = ref(null)
    const notices = ref([])
    const noticeDialogVisible = ref(false)
    const currentNotice = reactive({
      title: '',
      publisher: '',
      publishTime: '',
      content: '',
      attachments: []
    })

    // 登录表单
    const loginForm = reactive({
      username: '',
      password: '',
      remember: false
    })

    // 表单验证规则
    const rules = {
      username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
      password: [{required: true, message: '请输入密码', trigger: 'blur'}]
    }

    // 计算属性：是否已登录
    const isLoggedIn = computed(() => {
      return !!localStorage.getItem('token')
    })

    onMounted(() => {
      if (isLoggedIn.value) {
        fetchNotices()
      }
    })

    // 获取通知列表
    const fetchNotices = () => {
      getRecentNotices().then(response => {
        notices.value = response.data
      }).catch((error) => {
        console.error('获取公告列表失败', error)
        ElMessage.error('获取公告列表失败')
      })
    }

    // 处理登录
    const handleLogin = () => {
      loginFormRef.value.validate((valid) => {
        if (valid) {
          login(loginForm).then(response => {
            const {token, user} = response.data
            localStorage.setItem('token', token)
            localStorage.setItem('user', JSON.stringify(user))
            localStorage.setItem('role', user.role)

            if (loginForm.remember) {
              localStorage.setItem('username', loginForm.username)
            } else {
              localStorage.removeItem('username')
            }

            ElMessage.success('登录成功')

            // 根据用户角色进行路由跳转
            if (user.role === 'student') {
              router.push('/student')
            } else if (user.role === 'teacher') {
              router.push('/teacher')
            } else if (user.role === 'admin') {
              router.push('/admin')
            } else {
              // 刷新页面，显示已登录的首页内容
              window.location.reload()
            }
          }).catch(() => {
            ElMessage.error('用户名或密码错误')
          })
        }
      })
    }

    const getTypeTag = (type) => {
      const tags = {
        notice: 'info',
        academic: 'primary',
        activity: 'success',
        urgent: 'danger'
      }
      return tags[type] || 'info'
    }

    const getTypeName = (type) => {
      const names = {
        notice: '通知公告',
        academic: '教务通知',
        activity: '活动通知',
        urgent: '紧急通知'
      }
      return names[type] || type
    }

    const viewNotice = (notice) => {
      getNoticeById(notice.id).then(response => {
        Object.assign(currentNotice, response.data)
        noticeDialogVisible.value = true
      }).catch((error) => {
        console.error('获取公告详情失败', error)
        ElMessage.error('获取公告详情失败')
      })
    }

    const viewAllNotices = () => {
      router.push('/notice')
    }

    const navigateTo = (path) => {
      router.push(`/${path}`)
    }

    return {
      notices,
      noticeDialogVisible,
      currentNotice,
      loginForm,
      loginFormRef,
      rules,
      isLoggedIn,
      getTypeTag,
      getTypeName,
      viewNotice,
      viewAllNotices,
      navigateTo,
      handleLogin
    }
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

/* 系统介绍样式 */
.system-intro {
  padding: 30px;
  background-color: #fff;
  border-radius: 4px;
  height: 100%;
}

.system-intro h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 20px;
}

.system-intro p {
  font-size: 16px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 30px;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
}

.feature-item .el-icon {
  font-size: 36px;
  color: #409EFF;
  margin-right: 15px;
}

.feature-text h3 {
  font-size: 18px;
  margin: 0 0 10px 0;
  color: #303133;
}

.feature-text p {
  font-size: 14px;
  margin: 0;
  color: #606266;
}

/* 登录卡片样式 */
.login-card {
  padding: 20px;
  height: 100%;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 0;
  color: #409EFF;
  font-size: 24px;
}

/* 通知和导航样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notice-card,
.quick-nav-card {
  margin-bottom: 20px;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-item:hover {
  background-color: #f5f7fa;
}

.nav-item .el-icon {
  font-size: 24px;
  margin-bottom: 10px;
  color: #409EFF;
}

.notice-content {
  padding: 20px;
}

.notice-info {
  margin-bottom: 20px;
  color: #909399;
}

.notice-info span {
  margin-right: 20px;
}

.notice-text {
  line-height: 1.6;
  margin-bottom: 20px;
}

.notice-attachments {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.notice-attachments h4 {
  margin-bottom: 10px;
}

.notice-attachments ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-attachments li {
  margin-bottom: 5px;
}
</style> 