import axios from 'axios'
import {ElMessage, ElMessageBox} from 'element-plus'
import router from '@/router'
// import {useUserStore} from '@/store/user' // 导入 user store
// 从 @/utils/auth 导入 token
import {token as authToken} from '@/utils/auth'; // 移除 logout

// 创建axios实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/campus', // 修正 baseURL，移除末尾的 /api
    timeout: 15000 // 请求超时时间，增加到15秒
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        if (authToken.value) { // 直接检查导入的 token ref 的值
            // 获取token并添加到请求头
            config.headers.Authorization = `Bearer ${authToken.value}`
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
        const config = response.config // Get request config

        // 开发环境下添加响应日志，帮助调试
        console.log(`响应数据 for ${config.url}:`, res)

        // 如果响应成功，直接返回数据 - 增加更多可能的成功码或不严格检查code
        if (res.code === 0 || res.code === 200 || !res.code || res.data) {
            return res
        }

        // 处理业务错误码
        console.warn(`业务错误 for ${config.url}: code=${res.code}, message=${res.message}`); // 添加警告日志
        switch (res.code) {
            case 401: // 未授权 (业务码)
                console.error(`触发 handleUnauthorized due to res.code === 401 for ${config.url}`); // 明确日志来源
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
        const config = error.config // Get request config
        // console.error('请求失败:', error.message) // 可以保留或注释

        if (error.response) {
            // console.error('状态码:', error.response.status) // 可以保留或注释
            // console.error('响应数据:', error.response.data) // 可以保留或注释
            console.warn(`HTTP错误 for ${config?.url}: status=${error.response.status}`); // 添加警告日志

            // 根据HTTP状态码处理不同错误
            switch (error.response.status) {
                case 401: // 未授权 (HTTP状态码)
                    console.error(`触发 handleUnauthorized due to error.response.status === 401 for ${config?.url}`); // 明确日志来源
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
let isHandlingUnauthorized = false; // 添加标志位防止重复处理
const handleUnauthorized = () => {
    // 避免多次弹出提示 或 在已处理时再次处理
    if (isHandlingUnauthorized) return;
    isHandlingUnauthorized = true;

    // 修正useUserStore未定义的问题
    // const userStore = useUserStore(); // 获取 store 实例

    ElMessageBox.confirm(
        '登录状态已过期，您可以取消继续留在该页面，或者重新登录',
        '系统提示',
        {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(() => {
        // userStore.logout() // 调用 store 的 logout action 清理状态
        // 直接清理localStorage中的数据
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        localStorage.removeItem('role')

        // 跳转到登录页，并携带当前页面路径
        router.push({
            path: '/',
            query: {
                redirect: router.currentRoute.value.fullPath
            }
        })
    }).catch(() => {
        // 用户点击取消，可以不执行任何操作，或者根据需要处理
        console.log('用户取消了重新登录');
    }).finally(() => {
        isHandlingUnauthorized = false; // 重置标志位
    })
}

export default request