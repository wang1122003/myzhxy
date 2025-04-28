import axios from 'axios'
import {ElMessage, ElMessageBox} from 'element-plus'
import router from '@/router'
// import {useUserStore} from '@/store/user' // 导入 user store
// 从 @/utils/auth 导入 token
import {token as authToken} from '@/utils/auth'; // 移除 logout

// 创建axios实例
const request = axios.create({
    baseURL: '/api', // 强制使用代理，不再使用环境变量
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
        const res = response.data;
        // 记录请求日志以便调试
        console.log('API Response:', res);

        // 处理成功响应
        if (res.code === 0 || res.code === 200) {
            return res;
        }

        // 处理特殊情况：有些接口返回success=true且data为空数组也是正常的
        if (res.success === true) {
            return res;
        }

        // 处理业务逻辑错误
        if (res.message) {
            ElMessage.error(res.message);
        } else {
            ElMessage.error('请求失败，请联系管理员');
        }
        return Promise.reject(new Error(res.message || '未知错误'));
    },
    error => {
        // 处理HTTP错误
        let message = '服务器连接失败';
        if (error.response) {
            if (error.response.status === 401) {
                message = '登录已过期，请重新登录';
                handleUnauthorized();
            } else if (error.response.status === 403) {
                message = '无权访问该资源';
            } else if (error.response.status === 404) {
                message = '请求的资源不存在';
            } else if (error.response.status === 500) {
                message = '服务器内部错误';
            } else {
                message = `请求错误(${error.response.status})`;
            }
            // 如果有详细错误信息，优先使用详细信息
            if (error.response.data && error.response.data.message) {
                message = error.response.data.message;
            }
        } else if (error.request) {
            message = '服务器未响应';
        }

        console.error('请求错误:', error);
        ElMessage.error(message);
        return Promise.reject(error);
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