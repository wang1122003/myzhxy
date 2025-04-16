import axios from 'axios'
import {ElMessage} from 'element-plus'

// 创建axios实例
const request = axios.create({
    baseURL: '/api', // 设置API前缀
    timeout: 10000 // 请求超时时间
})

// 请求拦截器
request.interceptors.request.use(
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
request.interceptors.response.use(
    response => {
        const res = response.data

        // 如果响应成功，直接返回数据
        if (res.code === 0 || res.code === 200 || !res.code) {
            return res
        }

        // 否则返回错误信息
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
    },
    error => {
        if (error.response && error.response.status === 401) {
            // 未授权，清除token，返回登录页
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            localStorage.removeItem('role')
            ElMessage.error('登录已过期，请重新登录')
            setTimeout(() => {
                window.location.href = '/login'
            }, 1500)
        } else {
            // 其他错误
            ElMessage.error(error.message || '网络错误')
        }
        return Promise.reject(error)
    }
)

export default request 