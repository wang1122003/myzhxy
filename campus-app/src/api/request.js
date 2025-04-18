import axios from 'axios'
import {ElMessage, ElMessageBox} from 'element-plus'
import router from '@/router'

// 创建axios实例
const request = axios.create({
    baseURL: 'http://localhost:8080/campus/api', // 添加 /campus 上下文路径
    timeout: 15000 // 请求超时时间，增加到15秒
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 获取token并添加到请求头
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }

        // 在开发环境下添加请求日志
        if (process.env.NODE_ENV === 'development') {
            console.log('发送请求:', config.url)
            console.log('请求方法:', config.method)
            console.log('请求参数:', config.params || config.data)
        }
        
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data

        // 在开发环境下添加响应日志
        if (process.env.NODE_ENV === 'development') {
            console.log('响应数据:', res)
        }

        // 如果响应成功，直接返回数据
        if (res.code === 0 || res.code === 200 || !res.code) {
            return res
        }

        // 根据业务状态码处理不同的错误情况
        switch (res.code) {
            case 401: // 未授权
                handleUnauthorized()
                break
            case 403: // 禁止访问
                ElMessage.error('您没有权限访问该资源')
                break
            case 400: // 请求参数错误
                ElMessage.error(res.message || '请求参数错误')
                break
            default:
                ElMessage.error(res.message || '请求失败')
        }
        
        return Promise.reject(new Error(res.message || '请求失败'))
    },
    error => {
        console.error('请求失败:', error.message)

        if (error.response) {
            console.error('状态码:', error.response.status)
            console.error('响应数据:', error.response.data)

            // 根据HTTP状态码处理不同错误
            switch (error.response.status) {
                case 401: // 未授权
                    handleUnauthorized()
                    break
                case 403: // 禁止访问
                    ElMessage.error('您没有权限访问该资源')
                    break
                case 404: // 资源不存在
                    ElMessage.error('请求的资源不存在')
                    break
                case 500: // 服务器错误
                    ElMessage.error('服务器错误，请稍后重试')
                    break
                default:
                    ElMessage.error('请求失败，请稍后重试')
            }
        } else if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
            // 处理请求超时
            ElMessage.error('请求超时，请检查网络连接')
        } else if (!window.navigator.onLine) {
            // 处理网络断开
            ElMessage.error('网络连接已断开，请检查网络设置')
        } else {
            // 其他错误
            ElMessage.error(error.message || '网络错误')
        }

        return Promise.reject(error)
    }
)

// 处理未授权情况
const handleUnauthorized = () => {
    // 避免多次弹出提示
    if (localStorage.getItem('isShowingAuthError')) return

    localStorage.setItem('isShowingAuthError', 'true')

    ElMessageBox.confirm(
        '登录状态已过期，您可以取消继续留在该页面，或者重新登录',
        '系统提示',
        {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        localStorage.removeItem('role')
        localStorage.removeItem('isShowingAuthError')

        // 跳转到登录页，并携带当前页面路径
        router.push({
            path: '/',
            query: {
                redirect: router.currentRoute.value.fullPath
            }
        })
    }).catch(() => {
        localStorage.removeItem('isShowingAuthError')
    })
}

export default request 