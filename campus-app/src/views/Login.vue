<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <h2>校园管理系统</h2>
      </div>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              placeholder="密码"
              prefix-icon="el-icon-lock"
              show-password
              type="password"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button style="width: 100%" type="primary" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {login} from '@/api/user'

export default {
  name: 'LoginPage',
  setup() {
    const router = useRouter()
    const loginFormRef = ref(null)
    const loginForm = reactive({
      username: '',
      password: '',
      remember: false
    })

    const rules = {
      username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
      password: [{required: true, message: '请输入密码', trigger: 'blur'}]
    }

    const handleLogin = () => {
      loginFormRef.value.validate((valid) => {
        if (valid) {
          login(loginForm).then(response => {
            const {token, user} = response.data
            localStorage.setItem('token', token)
            localStorage.setItem('user', JSON.stringify(user))
            if (loginForm.remember) {
              localStorage.setItem('username', loginForm.username)
            } else {
              localStorage.removeItem('username')
            }
            ElMessage.success('登录成功')
            router.push('/')
          }).catch(() => {
            ElMessage.error('用户名或密码错误')
          })
        }
      })
    }

    return {
      loginFormRef,
      loginForm,
      rules,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.login-card {
  width: 400px;
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 0;
  color: #409EFF;
}
</style>
