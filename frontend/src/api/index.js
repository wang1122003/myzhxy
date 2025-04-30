import axios from 'axios'
import {ElMessage} from 'element-plus'
import * as userApi from './user';
import * as courseApi from './course';
import * as scheduleApi from './schedule';
import * as classroomApi from './classroom';
import * as activityApi from './activity';
import * as noticeApi from './notice';
import * as forumApi from './post'; // 假设 post.js 现在包含所有论坛 API
import * as fileApi from './file';
import * as commonApi from './common';
import * as gradeApi from './grade';
import * as termApi from './term';
import * as courseSelectionApi from './courseSelection';
import {useUserStore} from '@/stores/userStore';

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

// 用于防止多次重复刷新token的标志
let isRefreshing = false;
// 等待token刷新的失败请求队列
let failedQueue = [];

// 处理队列中的请求
const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error); // 如果刷新出错，拒绝队列中的请求
        } else {
            prom.resolve(token); // 如果刷新成功，用新token解决队列中的请求
        }
    });
    failedQueue = []; // 清空队列
};

// 响应拦截器
instance.interceptors.response.use(
    response => {
        // 如果响应成功，且code为0，则返回数据
        if (response.data && response.data.code === 0) {
            return response.data
        }

        // 业务错误，但非token问题
        ElMessage.error(response.data.message || '请求失败')
        return Promise.reject(response.data)
    },
    async error => {
        const originalRequest = error.config;

        // 检查是否是401错误，并且没有标记为重试
        if (error.response && error.response.status === 401 && !originalRequest._retry) {

            if (isRefreshing) {
                // 如果正在刷新token，则将此请求加入队列
                return new Promise(function (resolve, reject) {
                    failedQueue.push({resolve, reject});
                }).then(token => {
                    // 刷新成功后，使用新token重试此请求
                    originalRequest.headers['Authorization'] = 'Bearer ' + token;
                    return instance(originalRequest);
                }).catch(err => {
                    // 如果刷新过程中出现错误，则传递错误
                    return Promise.reject(err);
                });
            }

            originalRequest._retry = true; // 标记请求为已重试
            isRefreshing = true; // 标记正在刷新token

            const refreshToken = localStorage.getItem('refreshToken'); // 假设refreshToken存储在localStorage
            const userStore = useUserStore(); // 获取用户store实例

            if (!refreshToken) {
                // 如果没有找到refreshToken，则直接登出
                console.error("未找到刷新令牌 (refreshToken)，执行登出。");
                userStore.logout();
                ElMessage.error('会话已过期，请重新登录');
                window.location.href = '/'; // 重定向到登录页
                isRefreshing = false;
                processQueue(new Error('No refresh token')); // 拒绝队列中的所有请求
                return Promise.reject(new Error('尝试刷新时无可用刷新令牌。'));
            }

            try {
                // 使用原始axios或新实例发起刷新请求，避免进入拦截器循环
                const refreshResponse = await axios.post('/api/auth/refresh', { // 如果需要，调整接口地址
                    refreshToken: refreshToken
                });

                // 检查刷新请求是否成功，并且返回了新的token
                if (refreshResponse.data && refreshResponse.data.code === 0 && refreshResponse.data.data.token) {
                    const newAccessToken = refreshResponse.data.data.token;
                    const newRefreshToken = refreshResponse.data.data.refreshToken; // 检查后端是否返回了新的refreshToken

                    // 更新存储的token
                    localStorage.setItem('token', newAccessToken);
                    if (newRefreshToken) {
                        localStorage.setItem('refreshToken', newRefreshToken);
                    }

                    // 更新axios实例的默认header和原始请求的header
                    instance.defaults.headers.common['Authorization'] = `Bearer ${newAccessToken}`;
                    originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;

                    isRefreshing = false; // 标记刷新结束
                    processQueue(null, newAccessToken); // 处理队列中的请求，使用新的accessToken

                    // 使用新token重试原始请求
                    return instance(originalRequest);

                } else {
                    // 如果服务器端刷新失败（例如，refreshToken无效）
                    throw new Error(refreshResponse.data.message || '刷新token失败');
                }

            } catch (refreshError) {
                // 捕获刷新过程中发生的任何错误
                console.error("Token刷新失败:", refreshError);
                userStore.logout(); // 登出用户
                ElMessage.error('会话已过期，请重新登录');
                window.location.href = '/'; // 重定向到登录页
                isRefreshing = false; // 标记刷新结束
                processQueue(refreshError); // 拒绝队列中的所有请求
                return Promise.reject(refreshError); // 拒绝原始请求的Promise
            }
        } else if (error.response && error.response.status === 401 && originalRequest._retry) {
            // 如果重试后仍然是401错误（可能刷新失败了），则执行登出
            console.error("重试后仍收到401，可能刷新失败。执行登出。");
            const userStore = useUserStore();
            userStore.logout();
            ElMessage.error('会话已过期，请重新登录');
            window.location.href = '/';
            return Promise.reject(error);
        }

        // 处理其他错误（非401或刷新过程中的错误）
        ElMessage.error(error.response?.data?.message || error.message || '网络错误');
        return Promise.reject(error);
    }
)

// 重新导出所有API
export {
    userApi,
    courseApi,
    scheduleApi,
    classroomApi,
    activityApi,
    noticeApi,
    forumApi, // 导出为forumApi，尽管文件名是post.js
    fileApi,
    commonApi,
    gradeApi,
    termApi,
    courseSelectionApi
};

// 可选：导出一个包含所有API的对象
const api = {
    user: userApi,
    course: courseApi,
    schedule: scheduleApi,
    classroom: classroomApi,
    activity: activityApi,
    notice: noticeApi,
    forum: forumApi, // 保持命名一致
    file: fileApi,
    common: commonApi,
    grade: gradeApi,
    term: termApi,
    courseSelection: courseSelectionApi
};

export default api; 