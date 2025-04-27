import {computed, ref} from 'vue';

const TokenKey = 'Authorization-Token'; // 用于在 localStorage 中存储令牌的键名
const UserInfoKey = 'user'; // 用于存储用户信息的键名
const RoleKey = 'role'; // 用于存储角色的键名

// --- State (响应式引用) ---
const token = ref(localStorage.getItem(TokenKey) || null);
const userInfo = ref(JSON.parse(localStorage.getItem(UserInfoKey) || 'null'));

// --- Getters (计算属性) ---
const isLoggedIn = computed(() => !!token.value);
// 确保在访问属性前 userInfo.value 存在
const userRole = computed(() => userInfo.value?.userType?.toLowerCase() || null);
const userAvatar = computed(() => userInfo.value?.avatarUrl || '');
const userRealName = computed(() => userInfo.value?.realName || '');
const userId = computed(() => userInfo.value?.id || null); // 添加 userId getter

// --- Actions/Functions (操作/函数) ---

/**
 * 在响应式引用和 localStorage 中设置令牌。
 * @param {string | null} newToken 要存储的令牌，或 null 表示移除。
 */
function setToken(newToken) {
    token.value = newToken;
    if (newToken) {
        localStorage.setItem(TokenKey, newToken);
    } else {
        localStorage.removeItem(TokenKey);
    }
}

/**
 * 在响应式引用和 localStorage 中设置用户信息。
 * 同时处理单独存储角色信息。
 * @param {object | null} newUserInfo 用户信息对象，或 null 表示移除。
 */
function setUserInfo(newUserInfo) {
    // 添加日志，检查传入的用户信息
    console.log('[auth.js] setUserInfo received:', JSON.stringify(newUserInfo));
    userInfo.value = newUserInfo;
    if (newUserInfo) {
        localStorage.setItem(UserInfoKey, JSON.stringify(newUserInfo));
        // 如果角色信息存在，单独存储
        if (newUserInfo.userType) {
            localStorage.setItem(RoleKey, newUserInfo.userType.toLowerCase());
        } else {
            localStorage.removeItem(RoleKey);
        }
    } else {
        localStorage.removeItem(UserInfoKey);
        localStorage.removeItem(RoleKey);
    }
}

/**
 * 通过设置令牌和用户信息来处理登录，然后返回用户角色。
 * @param {object} userData 包含令牌和用户信息的对象。
 * @returns {string | null} 小写的用户角色 ('admin', 'teacher', 'student')，如果无效则返回 null。
 */
function login(userData) {
    console.log('[auth.js] login called, setting state...'); // 日志确认调用
    if (userData && userData.token && userData.user) {
        setToken(userData.token);
        setUserInfo(userData.user);

        // 直接从传入的 user 对象获取角色并返回
        const role = userData.user?.userType?.toLowerCase() || null;
        console.log('[auth.js] login finished, returning role:', role); // 日志确认返回的角色
        return role;
    } else {
        console.error('[auth.js] Login failed: Invalid user data received.', userData);
        setToken(null); // 确保无效登录也清理 token
        setUserInfo(null);
        return null; // 登录失败返回 null
    }
}

/**
 * 通过清除令牌和用户信息来处理登出。
 */
function logout() {
    console.log('[auth.js] logout called'); // 添加日志
    setToken(null);
    setUserInfo(null);
    // 显式清除 localStorage，以防 set* 函数失败
    localStorage.removeItem(TokenKey);
    localStorage.removeItem(UserInfoKey);
    localStorage.removeItem(RoleKey);
    // 可选：重定向或清除其他敏感数据
}

/**
 * 从响应式状态获取当前用户信息。
 * 如果令牌存在但用户信息缺失/无效，则执行登出。
 * 不执行 API 调用。
 * @returns {Promise<object|null>} 一个解析为用户信息或 null 的 Promise。
 */
async function fetchUserInfo() {
    if (!token.value) {
        // 如果没有令牌，确保用户信息也被清除
        if (userInfo.value) setUserInfo(null);
        return null;
    }

    if (userInfo.value) {
        // 可选验证：检查基本信息（如 userType）是否存在
        if (!userInfo.value.userType) {
            console.warn('存储的用户信息不完整 (缺少 userType)，正在登出。');
            logout();
            return null;
        }
        return userInfo.value; // 返回现有信息
    } else {
        // 令牌存在但存储中无用户信息 (例如，localStorage 被外部清除)
        console.warn('找到令牌但存储中缺少用户信息，正在登出。');
        logout(); // 清除无效状态
        return null;
    }
}

/**
 * 从 localStorage 获取令牌 (原始函数，现在可能多余)。
 * 考虑直接使用导出的 'token' 响应式引用。
 * @returns {string | null} 令牌，如果未找到则返回 null。
 */
function getToken() {
    // 可以简单地返回响应式引用的值
    return token.value;
    // 或者继续从 localStorage 读取，但如果响应式引用是数据源则不太理想
    // return localStorage.getItem(TokenKey);
}

/**
 * 从 localStorage 移除令牌 (原始函数，现在可能多余)。
 * 考虑使用 logout() 或 setToken(null)。
 */
function removeToken() {
    // 只需调用 setToken(null) 来清除状态和 localStorage
    setToken(null);
    // 或者只从存储中移除，但这不会更新响应式状态:
    // localStorage.removeItem(TokenKey);
}

/**
 * 提供用户存储的 hook，供组件和视图使用。
 * @returns {Object} 包含用户状态和方法的对象。
 */
function useUserStore() {
    return {
        // 状态
        token,
        userInfo,
        isLoggedIn,
        userRole,
        userAvatar,
        userRealName,
        userId,
        
        // 方法
        login,
        logout,
        setToken,
        setUserInfo,
        fetchUserInfo
    };
}

export {
    // Reactive State & Getters (响应式状态和计算属性，可直接在组件/setup 中使用)
    token,
    userInfo,
    isLoggedIn,
    userRole,
    userAvatar,
    userRealName,
    userId,

    // Actions/Mutators (操作/修改器)
    setToken,
    setUserInfo,
    login,
    logout,
    fetchUserInfo,

    // Original auth functions (原始认证函数，可能多余，保留用于兼容性或移除)
    getToken,
    removeToken,

    // 用户存储 Hook
    useUserStore
}; 