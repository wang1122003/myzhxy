import {computed, ref} from 'vue';

const TokenKey = 'Authorization-Token'; // Key for storing token in localStorage
const UserInfoKey = 'user'; // Key for storing user info
const RoleKey = 'role'; // Key for storing role

// --- State (Reactive Refs) ---
const token = ref(localStorage.getItem(TokenKey) || null);
const userInfo = ref(JSON.parse(localStorage.getItem(UserInfoKey) || 'null'));

// --- Getters (Computed Refs) ---
const isLoggedIn = computed(() => !!token.value);
// Ensure userInfo.value exists before accessing properties
const userRole = computed(() => userInfo.value?.userType?.toLowerCase() || null);
const userAvatar = computed(() => userInfo.value?.avatarUrl || '');
const userRealName = computed(() => userInfo.value?.realName || '');
const userId = computed(() => userInfo.value?.id || null); // Add userId getter

// --- Actions/Functions ---

/**
 * Sets the token in ref and localStorage.
 * @param {string | null} newToken The token to store, or null to remove.
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
 * Sets the user info in ref and localStorage.
 * Also handles storing the role separately.
 * @param {object | null} newUserInfo The user info object, or null to remove.
 */
function setUserInfo(newUserInfo) {
    // 添加日志，检查传入的用户信息
    console.log('[auth.js] setUserInfo received:', JSON.stringify(newUserInfo));
    userInfo.value = newUserInfo;
    if (newUserInfo) {
        localStorage.setItem(UserInfoKey, JSON.stringify(newUserInfo));
        // Store role separately if it exists
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
 * Processes login by setting token and user info, then returns the user role.
 * @param {object} userData Object containing token and user info.
 * @returns {string | null} The lowercase user role ('admin', 'teacher', 'student') or null if invalid.
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
        setToken(null); // 确保无效登录也清理token
        setUserInfo(null);
        return null; // 登录失败返回 null
    }
}

/**
 * Processes logout by clearing token and user info.
 */
function logout() {
    console.log('[auth.js] logout called'); // 添加日志
    setToken(null);
    setUserInfo(null);
    // Explicitly clear localStorage just in case set* functions failed
    localStorage.removeItem(TokenKey);
    localStorage.removeItem(UserInfoKey);
    localStorage.removeItem(RoleKey);
    // Optionally: redirect or clear other sensitive data
}

/**
 * Gets the current user info from the reactive state.
 * If token exists but user info is missing/invalid, performs logout.
 * Does NOT perform an API call.
 * @returns {Promise<object|null>} A promise resolving to user info or null.
 */
async function fetchUserInfo() {
    if (!token.value) {
        // If no token, ensure user info is also cleared
        if (userInfo.value) setUserInfo(null);
        return null;
    }

    if (userInfo.value) {
        // Optional validation: Check if essential info like userType exists
        if (!userInfo.value.userType) {
            console.warn('Stored user info is incomplete (missing userType). Logging out.');
            logout();
            return null;
        }
        return userInfo.value; // Return existing info
    } else {
        // Token exists but no user info (e.g., localStorage cleared externally)
        console.warn('Token found but user info is missing in store. Logging out.');
        logout(); // Clear invalid state
        return null;
    }
}

/**
 * Gets the token from localStorage (original function, potentially redundant now).
 * Consider using the exported 'token' ref directly.
 * @returns {string | null} The token or null if not found.
 */
function getToken() {
    // Can simply return the reactive ref's value
    return token.value;
    // Or keep reading from localStorage, though less ideal if ref is the source of truth
    // return localStorage.getItem(TokenKey);
}

/**
 * Removes the token from localStorage (original function, potentially redundant now).
 * Consider using logout() or setToken(null).
 */
function removeToken() {
    // Simply call setToken with null to clear state and localStorage
    setToken(null);
    // Or just remove from storage, but doesn't update the reactive state:
    // localStorage.removeItem(TokenKey);
}

export {
    // Reactive State & Getters (directly usable in components/setup)
    token,
    userInfo,
    isLoggedIn,
    userRole,
    userAvatar,
    userRealName,
    userId,

    // Actions/Mutators
    setToken,
    setUserInfo,
    login,
    logout,
    fetchUserInfo,

    // Original auth functions (potentially redundant, keep for compatibility or remove)
    getToken,
    removeToken
}; 