import axios from 'axios'
import {ElMessage, ElMessageBox} from 'element-plus'
import router from '@/router'
// import {useUserStore} from '@/store/user' // 导入 user store
// 从 @/stores/userStore 获取 token state
import {useUserStore} from '@/stores/userStore';

// 创建axios实例
const request = axios.create({
    baseURL: '/api', // 强制使用代理，不再使用环境变量
    timeout: 15000 // 请求超时时间，增加到15秒
})

// 记录是否正在刷新token
let isRefreshing = false;
// 等待token刷新的请求队列
let waitingQueue = [];

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 优先从 Pinia store 获取 token
        const userStore = useUserStore();
        const token = userStore.token; // Access token from Pinia store

        if (token) { // 直接检查导入的 token ref 的值
            // 获取token并添加到请求头
            config.headers.Authorization = `Bearer ${token}`
        }

        // 添加请求日志，用于调试
        console.log('请求数据 >>>>>>>>>', {
            url: config.url,
            method: config.method,
            params: config.params,
            data: config.data,
            headers: config.headers
        });
        
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

        // 如果返回的是二进制数据或文件下载，直接返回
        if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
            return response;
        }

        // 如果是Result包装对象且成功状态，直接返回data
        if (res.code !== undefined) {
            if (res.code === 200) {
                return res.data;
            } else {
                // 业务失败 (code 不是 200)
                const defaultMessage = `操作失败 (代码: ${res.code})`;
                ElMessage.error(res.message || defaultMessage);
                return Promise.reject(new Error(res.message || defaultMessage));
            }
        }

        // 直接返回响应数据(兼容纯数据返回)
        return res;
    },
    error => {
        console.log('请求错误', error);
        const {response} = error;
        let message = '连接服务器失败';

        if (response) {
            const originalRequest = error.config;
            // 尝试从响应中获取错误信息
            if (response.data && response.data.message) {
                message = response.data.message;
            } else {
                switch (response.status) {
                    case 400:
                        message = '请求错误';
                        break;
                    case 401:
                        message = '未授权，请重新登录';
                        break;
                    case 403:
                        message = '拒绝访问';
                        break;
                    case 404:
                        message = '请求的资源不存在';
                        break;
                    case 500:
                        message = '服务器内部错误';
                        break;
                    default:
                        message = `请求失败(${response.status})`;
                }
            }

            // 401 未授权
            if (response.status === 401) {
                // 检查原始请求是否携带了 Authorization 头
                if (originalRequest.headers['Authorization']) {
                    // 如果携带了 Token，说明可能是 Token 过期，尝试刷新
                    console.log('检测到 401 错误且原始请求带有 Token，尝试刷新...');
                    return handleTokenExpired(error);
                } else {
                    // 如果原始请求没有 Token，说明是未登录访问受保护资源
                    console.log('检测到 401 错误但原始请求未带 Token，引导用户登录...');
                    // 调用 handleUnauthorized 会提示并引导到登录页
                    handleUnauthorized();
                    return Promise.reject(error); // 拒绝原始请求
            }
            }
        }

        // 其他非 401 HTTP 错误
        ElMessage.error(message);
        return Promise.reject(error);
    }
)

// 刷新Token的方法
const refreshToken = async () => {
    try {
        // 使用Pinia store的refreshToken方法
        const userStore = useUserStore();
        const newToken = await userStore.refreshToken();
        return newToken;
    } catch (err) {
        console.error('刷新token时出错:', err);
        // 刷新失败时，应引导用户重新登录
        handleUnauthorized();
        throw err;
    }
};

// 处理token过期
const handleTokenExpired = async (error) => {
    const originalRequest = error.config;

    // 防止重复刷新
    if (!isRefreshing) {
        isRefreshing = true;

        try {
            console.log('尝试刷新 token...');
            // 尝试刷新token
            const newToken = await refreshToken();
            console.log('Token 刷新成功:', newToken);

            // 处理队列中的请求
            waitingQueue.forEach(callback => callback(newToken));
            waitingQueue = [];

            // 更新当前请求的token并重试
            originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
            // 清除标志位后重试
            isRefreshing = false;
            return request(originalRequest); // 使用 request 实例重试，确保应用新的拦截器

        } catch (refreshError) {
            // 刷新token失败，已经在 refreshToken 中处理了 handleUnauthorized
            console.error('无法刷新token，需要重新登录:', refreshError);
            // 不需要再次调用 handleUnauthorized，因为 refreshToken 内部会调用
            // 清除标志位
            isRefreshing = false;
            return Promise.reject(refreshError);
        }
        // finally {
        //     // 在请求重试或失败后清除标志位
        //     // isRefreshing = false; // 移到 try 和 catch 内部确保正确时机清除
        // }
    } else {
        // 创建一个Promise，加入等待队列
        return new Promise((resolve) => {
            waitingQueue.push((token) => {
                originalRequest.headers['Authorization'] = `Bearer ${token}`;
                resolve(request(originalRequest)); // 使用 request 实例重试
            });
        });
    }
};

// 处理未授权情况
let isHandlingUnauthorized = false; // 添加标志位防止重复处理
const handleUnauthorized = () => {
    // 避免多次弹出提示 或 在已处理时再次处理
    if (isHandlingUnauthorized) return;
    isHandlingUnauthorized = true;

    // 使用 Pinia store 进行登出操作
    const userStore = useUserStore(); 

    ElMessageBox.confirm(
        '登录状态已过期，您可以取消继续留在该页面，或者重新登录',
        '系统提示',
        {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(() => {
        userStore.logout() // 调用 store 的 logout action 清理状态和 sessionStorage
        // 不需要手动清理 sessionStorage，logout action 会处理
        
        // 跳转到登录页，并携带当前页面路径
        router.push({
            path: '/', // 假设登录页是根路径，如果不是请修改
            query: {
                redirect: router.currentRoute.value.fullPath
            }
        });
    }).catch(() => {
        // 用户点击取消，可以不执行任何操作，或者根据需要处理
        console.log('用户取消了重新登录');
    }).finally(() => {
        isHandlingUnauthorized = false; // 重置标志位
    })
}

export default request