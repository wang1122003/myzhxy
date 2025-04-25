import request from '@/utils/request'

// 统一存放API端点，代替原来的 api-endpoints.js 中的 USER_API 部分
const API = {
    LOGIN: '/users/login',
    REGISTER: '/users/register',
    GET_CURRENT_USER: '/users/check-session',
    UPDATE_CURRENT_USER: '/users/profile',
    CHANGE_PASSWORD: '/users/change-password',
    GET_USER_LIST: '/users',
    ADD_USER: '/users/register',
    UPDATE_USER: (id) => `/users/${id}`,
    DELETE_USER: (id) => `/users/${id}`,
    RESET_PASSWORD: (id) => `/users/${id}/reset-password`,
    CHECK_PERMISSION: '/users/check-permission', // Assuming backend endpoint exists based on api-endpoints.js
    GET_PERMISSIONS: '/users/permissions', // Assuming backend endpoint exists based on api-endpoints.js
    LOGOUT: '/users/logout',
    UPLOAD_AVATAR: '/users/upload-avatar'
}

// 登录
export function login(data) {
    return request({
        url: API.LOGIN,
        method: 'post',
        data
    })
}

// 注册
export function register(data) {
    return request({
        url: API.REGISTER,
        method: 'post',
        data
    })
}

// 获取当前用户信息
export function getCurrentUser() {
    return request({
        url: API.GET_CURRENT_USER,
        method: 'get'
    })
}

// 更新当前用户信息 (Profile)
export function updateCurrentUser(data) {
    return request({
        url: API.UPDATE_CURRENT_USER,
        method: 'put', // Assuming PUT for profile update
        data
    })
}

// 修改密码
export function changePassword(data) {
    return request({
        url: API.CHANGE_PASSWORD,
        method: 'post',
        params: data
    })
}

// 获取用户列表 (分页或全量，根据后端实现调整)
export function getUserList(params) {
    return request({
        url: API.GET_USER_LIST,
        method: 'get',
        params // Pass query params like page, size, filters
    })
}

// 添加用户
export function addUser(data) {
    return request({
        url: API.ADD_USER,
        method: 'post',
        data
    })
}

// 更新用户信息
export function updateUser(id, data) {
    return request({
        url: API.UPDATE_USER(id),
        method: 'put',
        data
    })
}

// 删除用户
export function deleteUser(id) {
    return request({
        url: API.DELETE_USER(id),
        method: 'delete'
    })
}

// 重置用户密码
export function resetPassword(id, data) {
    return request({
        url: API.RESET_PASSWORD(id),
        method: 'post',
        data
    })
}

// 检查权限 (根据后端实现调整)
export function checkPermission(operation) {
    return request({
        url: API.CHECK_PERMISSION,
        method: 'get', // Or POST?
        params: {operation}
    });
}

// 获取权限列表 (根据后端实现调整)
export function getPermissions() {
    return request({
        url: API.GET_PERMISSIONS,
        method: 'get'
    });
}

// 登出
export function logout() {
    return request({
        url: API.LOGOUT,
        method: 'post' // Assuming POST for logout
    })
}

// --- 以下为示例，根据原有 user.js 或 controller 实际情况增删改 ---

// export function fetchUserRoles(userId) {
//   return request({
//     url: `/users/${userId}/roles`, // Example: Endpoint might not exist in api-endpoints.js yet
//     method: 'get'
//   })
// }

// export function updateUserStatus(userId, status) {
//   return request({
//     url: `/users/${userId}/status`, // Example: Endpoint might not exist in api-endpoints.js yet
//     method: 'put',
//     data: { status }
//   })
// }

// 更新学生个人信息 (使用通用接口)
export function updateStudentProfile(data) {
    return request({
        url: API.UPDATE_CURRENT_USER, // 改为使用通用更新接口
        method: 'put',
        data
    })
}

// 更新教师个人信息 (使用通用接口)
export function updateTeacherProfile(data) {
    return request({
        url: API.UPDATE_CURRENT_USER, // 改为使用通用更新接口
        method: 'put',
        data
    })
}

// 更新管理员个人信息 (使用通用接口)
export function updateAdminProfile(data) {
    return request({
        url: API.UPDATE_CURRENT_USER, // 改为使用通用更新接口
        method: 'put',
        data
    })
}

// --- 管理员操作 --- 

// 获取教师列表 (用于下拉选择)
export function getTeacherSelectList(params) {
    // Assuming getUserList can filter by role or a new endpoint exists
    // e.g., GET /api/users/teachers
    return request({
        url: API.GET_USER_LIST, // Reusing GET_USER_LIST
        method: 'get',
        params: {...params, role: 'teacher'} // Add role filter if supported
    })
}

// 添加getUserInfo函数 (这个看起来是 mock 数据，暂时保留，但建议后续移除或整合)
export function getUserInfo() {
    return Promise.resolve({
        data: {
            id: 1,
            username: 'teacher1',
            role: 'teacher',
            name: '张老师',
            email: 'teacher1@example.com',
            phone: '13812345678',
            avatar: ''
        }
    });
}

/**
 * 获取用户个人资料
 * @param {number} userId 用户ID
 * @returns {Promise} 请求响应promise
 */
export function getUserProfile(userId) {
    return request({
        url: `/users/${userId}`,
        method: 'get'
    })
}

/**
 * 更新用户个人资料
 * @param {object} data 用户资料数据
 * @returns {Promise} 请求响应promise
 */
export function updateUserProfile(data) {
    return request({
        url: '/users/profile',
        method: 'put',
        data
    })
}

/**
 * 获取用户个人资料 (可能用于管理员视角)
 * @param {number} userId 用户ID
 * @returns {Promise} 请求响应promise
 */
export function getUserProfileById(userId) {
    return request({
        url: API.UPDATE_USER(userId), // 使用管理员更新接口的路径，但方法是GET
        method: 'get'
    })
}

/**
 * 根据ID获取用户信息 (管理员用)
 * @param {number} userId 用户ID
 * @returns {Promise} 请求响应promise
 */
export function getUserById(userId) {
    return request({
        url: API.UPDATE_USER(userId), // 使用管理员更新接口的路径，但方法是GET
        method: 'get'
    })
}

/**
 * 根据用户名获取用户信息
 * @param {string} username 用户名
 * @returns {Promise} 请求响应promise
 */
export function getUserByUsername(username) {
    return request({
        url: `/users/username/${username}`,
        method: 'get'
    })
}

/**
 * 获取教师列表
 * @param {object} params 查询参数
 * @returns {Promise} 请求响应promise
 */
export function getTeacherList(params) {
    return request({
        url: API.GET_USER_LIST, // 使用通用用户列表接口
        method: 'get',
        params: { ...params, userType: 'Teacher' } // 通过参数指定用户类型
    })
} 