import {defineStore} from 'pinia';
import {ref, computed} from 'vue';
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
    const isLoggedIn = computed(() => !!token.value);
    const userRole = computed(() => userInfo.value?.userType?.toLowerCase() || null);
    const userAvatar = computed(() => userInfo.value?.avatarUrl || '');
    const userRealName = computed(() => userInfo.value?.realName || '');

    const userId = computed(() => {
        console.log("[userStore] (computed) 尝试获取用户ID，当前userInfo:", userInfo.value);

        if (userInfo.value) {
            // 尝试解析数字ID（如果缓存的用户信息中只有字符串形式的ID）
            const id = userInfo.value.id ||
                userInfo.value.userId ||
                userInfo.value.user_id ||
                userInfo.value.teacherId ||
                userInfo.value.studentId;

            // 如果角色是学生但ID字段不存在，检查是否有单独的studentId字段
            if (userRole.value === 'student' && userInfo.value.student && userInfo.value.student.id) {
                console.log("[userStore] (computed) 从student子对象获取ID:", userInfo.value.student.id);
                return Number(userInfo.value.student.id);
            }

            // 如果角色是教师但ID字段不存在，检查是否有单独的teacherId字段
            if (userRole.value === 'teacher' && userInfo.value.teacher && userInfo.value.teacher.id) {
                console.log("[userStore] (computed) 从teacher子对象获取ID:", userInfo.value.teacher.id);
                return Number(userInfo.value.teacher.id);
            }

            console.log("[userStore] (computed) 返回用户ID:", id);
            return id ? Number(id) : null; // 确保返回数字或null
        }

        console.warn("[userStore] (computed) userInfo为空，无法获取用户ID");
        return null;
    });
    const hasSessionError = computed(() => sessionError.value);

    // Actions
    function setUserInfo(newUserInfo) {
        if (newUserInfo) {
            userInfo.value = newUserInfo;
            localStorage.setItem('userInfo', JSON.stringify(newUserInfo));
        } else {
            userInfo.value = {}; // 设置为空对象而不是null，以避免userInfo.value?.property 访问出错
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
        if (userInfo.value && newUserInfo) { // 应该检查 userInfo.value 是否为真，而不是 newUserInfo
            userInfo.value.avatarUrl = newAvatarUrl;
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
        }
    }

    async function login(credentials) {
        // credentials 应该包含: { username, password, remember (optional) }
        console.log("[userStore] Login action called with credentials:", credentials);
        try {
            const response = await apiUserLogin({
                username: credentials.username,
                password: credentials.password
            });

            // 安全地访问 token
            let newToken = null;
            if (response && response.data && response.data.token) { // 确保 response, response.data 都存在
                newToken = response.data.token;
            } else if (response && response.token) {
                // 兼容拦截器可能直接返回 data 部分，或者API直接返回token的情况
                newToken = response.token;
            } else {
                console.warn("[userStore] Login API response structure unexpected:", response);
            }

            if (newToken) {
                setToken(newToken);
                await fetchAndSetUserInfo(); // 获取并设置用户信息
                console.log('[userStore] 登录成功，已获取用户信息。');

                // 处理记住用户名
                if (credentials.remember) {
                    localStorage.setItem('username', credentials.username);
                    console.log('[userStore] Username remembered:', credentials.username);
                } else {
                    localStorage.removeItem('username');
                    console.log('[userStore] Username not remembered or removed.');
                }

                return userRole.value; // 返回角色
            } else {
                console.error('[userStore] 登录失败：未从响应中获取到token。', response);
                logout();
                return null;
            }
        } catch (error) {
            console.error('[userStore] 登录 API 调用失败:', error);
            logout();
            return null;
        }
    }

    async function logout() {
        try {
            if (isLoggedIn.value) { // 调用computed属性的.value
                await apiUserLogout().catch(error => {
                    console.warn('[userStore] 登出API调用失败，但会继续清理本地状态:', error);
                });
            }
        } finally {
            console.log('[userStore] 清理用户状态...');
            setToken(''); // 首先清除 token ref
            setUserInfo({}); // 然后清除 userInfo ref (设置为空对象)
            // localStorage的清理已在setToken和setUserInfo内部处理
            console.log('[userStore] 用户状态已完全清除');
        }
    }

    async function refreshToken() {
        if (!token.value) {
            console.error('[userStore] 无token可供刷新');
            throw new Error('没有token可供刷新');
        }
        try {
            console.log('[userStore] 尝试刷新token...');
            const response = await apiRefreshToken();
            let newToken = null;
            if (response && typeof response === 'object') {
                if (response.token) {
                    newToken = response.token;
                } else if (response.data && response.data.token) {
                    newToken = response.data.token;
                } else if (response.code === 200 && response.data) { // 兼容直接返回 token 字符串或包含 token 的对象
                    newToken = (typeof response.data === 'string' && response.data) || response.data.token;
                }
            }
            if (newToken) {
                console.log('[userStore] 刷新token成功');
                setToken(newToken);
                return newToken;
            } else {
                console.error('[userStore] 刷新token失败：响应中未找到token', response);
                throw new Error('刷新token失败：响应中未找到token');
            }
        } catch (error) {
            console.error('[userStore] 刷新token失败:', error);
            setToken('');
            throw error;
        }
    }

    async function fetchAndSetUserInfo() {
        if (!token.value) {
            console.warn('[userStore] fetchAndSetUserInfo在没有token的情况下被调用。');
            return; // 或者 throw new Error('No token available');
        }
        try {
            console.log('[userStore] 正在获取用户信息...');
            const userData = await getCurrentUser();
            // 确保userData有效，并且包含一个可识别的ID字段
            const idField = userData?.id || userData?.userId || userData?.user_id || userData?.teacherId || userData?.studentId;

            if (userData && idField) { 
                console.log('[userStore] 用户信息已获取:', userData);
                setUserInfo(userData);
                setSessionError(false); 
            } else {
                console.error('[userStore] 获取用户信息失败: 拦截器返回的数据无效或缺少ID', userData);
                // 在这里不应该抛出错误然后被外部捕获导致登出，而应该标记会话错误
                setSessionError(true);
                // 可以选择清除本地可能无效的用户信息
                // setUserInfo({}); 
                // 抛出一个特定的错误类型如果需要
                // throw new Error('获取用户信息失败：数据无效或缺少ID');
            }
        } catch (error) {
            console.error('[userStore] 获取用户信息时发生错误:', error);
            setSessionError(true);
            // 不再向上抛出错误，避免App.vue中的全局错误处理器强制登出
            // throw error; 
        }
    }

    return {
        userInfo,
        token,
        sessionError,
        isLoggedIn,
        userRole,
        userAvatar,
        userRealName,
        userId,
        hasSessionError,
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