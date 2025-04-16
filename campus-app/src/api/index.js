import axios from 'axios'
import {ElMessage} from 'element-plus'

// 创建axios实例
const instance = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// 请求拦截器
instance.interceptors.request.use(
    config => {
        // 获取token并添加到请求头
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
instance.interceptors.response.use(
    response => {
        // 如果响应成功，且code为0，则返回数据
        if (response.data && response.data.code === 0) {
            return response.data
        }

        // 否则返回错误信息
        ElMessage.error(response.data.message || '请求失败')
        return Promise.reject(response.data)
    },
    error => {
        if (error.response && error.response.status === 401) {
            // 未授权，清除token，返回登录页
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            ElMessage.error('登录已过期，请重新登录')
            window.location.href = '/login'
        } else {
            // 其他错误
            ElMessage.error(error.message || '网络错误')
        }
        return Promise.reject(error)
    }
)

export default instance 