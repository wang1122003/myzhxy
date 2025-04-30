import {defineStore} from 'pinia';
import {ref} from 'vue';
import {
    getCurrentUser,
    login as apiUserLogin,
    logout as apiUserLogout,
    refreshToken as apiRefreshToken
} from '@/api/user';

export const useUserStore = defineStore('user', () => {
    // 状态 - 使用localStorage实现持久化
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'));
    const token = ref(localStorage.getItem('token') || '');
    const sessionError = ref(false);  // 新增：会话错误状态

    // 计算属性（Getters）
    const isLoggedIn = () => !!token.value;
    const userRole = () => userInfo.value?.userType?.toLowerCase() || null;
    const userAvatar = () => userInfo.value?.avatarUrl || '';
    const userRealName = () => userInfo.value?.realName || '';
    const userId = () => userInfo.value?.id || null;
    const hasSessionError = () => sessionError.value;  // 新增：会话错误检查

    // Actions
    function setUserInfo(newUserInfo) {
        if (newUserInfo) {
            userInfo.value = newUserInfo;
            localStorage.setItem('userInfo', JSON.stringify(newUserInfo));
        } else {
            userInfo.value = {};
            localStorage.removeItem('userInfo');
        }
    }

    function setToken(newToken) {
        token.value = newToken;
        if (newToken) {
            localStorage.setItem('token', newToken);
        } else {
            localStorage.removeItem('token');
        }
    }

    function setSessionError(hasError) {
        sessionError.value = hasError;
    }

    function setAvatar(newAvatarUrl) {
        if (userInfo.value && newAvatarUrl) {
            userInfo.value.avatarUrl = newAvatarUrl;
            // 更新localStorage中的用户信息
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
        }
    }

    async function login(credentials) {
        try {
            const response = await apiUserLogin(credentials);
            const newToken = response.data.token;
            if (newToken) {
                setToken(newToken);
                await fetchAndSetUserInfo();
                console.log('[userStore] 登录成功，已获取用户信息。');
                return userRole();
            } else {
                console.error('[userStore] 登录失败：未收到token。');
                logout();
                return null;
            }
        } catch (error) {
            console.error('[userStore] 登录失败:', error);
            logout();
            return null;
        }
    }

    async function logout() {
        try {
            // 尝试调用登出API（如果有）
            if (isLoggedIn()) {
                await apiUserLogout().catch(error => {
                    console.warn('[userStore] 登出API调用失败，但会继续清理本地状态:', error);
                });
            }
        } finally {
            // 无论API是否成功，都清除本地状态
            console.log('[userStore] 清理用户状态...');

            // 先将token和用户信息引用置空
            setToken('');
            setUserInfo({});

            // 再删除localStorage中的数据
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');

            console.log('[userStore] 用户状态已完全清除');
        }
    }

    // 尝试刷新token
    async function refreshToken() {
        if (!token.value) {
            throw new Error('没有token可供刷新');
        }

        try {
            const response = await apiRefreshToken();
            if (response.code === 0 && response.data && response.data.token) {
                const newToken = response.data.token;
                setToken(newToken);
                return newToken;
            } else {
                throw new Error(response.message || '刷新token失败');
            }
        } catch (error) {
            console.error('[userStore] 刷新token失败:', error);
            throw error;
        }
    }

    // 从API获取用户信息并更新store的action
    async function fetchAndSetUserInfo() {
        if (!token.value) {
            console.warn('[userStore] fetchAndSetUserInfo在没有token的情况下被调用。');
            return;
        }
        try {
            console.log('[userStore] 正在获取用户信息...');
            const response = await getCurrentUser();
            if (response.code === 0 && response.data) {
                console.log('[userStore] 用户信息已获取:', response.data);
                setUserInfo(response.data);
                setSessionError(false); // 获取成功，清除错误标记
            } else {
                console.error('[userStore] 获取用户信息失败: 无效的响应或无数据', response);
                // 如果响应码不是0或没有数据，仅抛出错误，不强制登出
                throw new Error(response.message || '获取用户信息失败：无效响应');
            }
        } catch (error) {
            console.error('[userStore] 获取用户信息时发生错误:', error);
            // 在捕获到任何获取用户信息的错误时，仅抛出错误，不强制登出
            throw error;
        }
    }

    return {
        // 状态
        userInfo,
        token,
        sessionError,

        // Getters
        isLoggedIn,
        userRole,
        userAvatar,
        userRealName,
        userId,
        hasSessionError,

        // Actions
        setUserInfo,
        setToken,
        setSessionError,
        setAvatar,
        login,
        logout,
        refreshToken,
        fetchAndSetUserInfo
    };
}); 