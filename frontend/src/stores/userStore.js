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
    const userId = () => {
        // 更健壮的用户ID获取，添加详细日志以便排查问题
        console.log("[userStore] 尝试获取用户ID，当前userInfo:", userInfo.value);

        if (userInfo.value) {
            // 尝试解析数字ID（如果缓存的用户信息中只有字符串形式的ID）
            const id = userInfo.value.id ||
                userInfo.value.userId ||
                userInfo.value.user_id ||
                userInfo.value.teacherId ||
                userInfo.value.studentId;

            // 如果角色是学生但ID字段不存在，检查是否有单独的studentId字段
            if (userRole() === 'student' && userInfo.value.student && userInfo.value.student.id) {
                console.log("[userStore] 从student子对象获取ID:", userInfo.value.student.id);
                return userInfo.value.student.id;
            }

            // 如果角色是教师但ID字段不存在，检查是否有单独的teacherId字段
            if (userRole() === 'teacher' && userInfo.value.teacher && userInfo.value.teacher.id) {
                console.log("[userStore] 从teacher子对象获取ID:", userInfo.value.teacher.id);
                return userInfo.value.teacher.id;
            }

            console.log("[userStore] 返回用户ID:", id);
            return id || null;
        }

        console.warn("[userStore] userInfo为空，无法获取用户ID");
        return null;
    };
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
            const userData = await getCurrentUser(); // 拦截器已返回 data 部分

            // ！！！ 直接使用拦截器处理后的 userData ！！！
            if (userData && userData.userId) { // 简单检查 userData 是否有效 (例如检查 userId)
                console.log('[userStore] 用户信息已获取:', userData);
                setUserInfo(userData); // 直接设置用户信息
                setSessionError(false); // 获取成功，清除错误标记
            } else {
                // 如果 userData 无效或缺少关键信息
                console.error('[userStore] 获取用户信息失败: 拦截器返回的数据无效', userData);
                throw new Error('获取用户信息失败：数据无效');
            }
        } catch (error) {
            console.error('[userStore] 获取用户信息时发生错误:', error);
            // 在捕获到任何获取用户信息的错误时，仅抛出错误，不强制登出
            // setSessionError(true); // 可以考虑在这里设置会话错误
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