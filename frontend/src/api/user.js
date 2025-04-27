import request from '@/utils/request'

// 统一存放 API 端点，代替原来的 api-endpoints.js 中的 USER_API 部分
const API = {
    LOGIN: '/users/login',
    REGISTER: '/users/register',
    GET_CURRENT_USER: '/users/check-session', // 获取当前登录用户信息
    UPDATE_CURRENT_USER: '/users/profile', // 更新当前登录用户资料
    CHANGE_PASSWORD: '/users/change-password', // 修改密码
    GET_USER_LIST: '/users', // 获取用户列表 (管理员)
    ADD_USER: '/users/register', // 添加用户 (管理员，可能与注册接口复用)
    UPDATE_USER: (id) => `/users/${id}`, // 更新指定用户信息 (管理员)
    DELETE_USER: (id) => `/users/${id}`, // 删除指定用户 (管理员)
    RESET_PASSWORD: (id) => `/users/${id}/reset-password`, // 重置指定用户密码 (管理员)
    CHECK_PERMISSION: '/users/check-permission', // 检查权限 (假设后端存在此接口)
    GET_PERMISSIONS: '/users/permissions', // 获取权限列表 (假设后端存在此接口)
    LOGOUT: '/users/logout',
    UPLOAD_AVATAR: '/users/upload-avatar' // 上传头像
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

// 更新当前用户信息 (个人资料)
export function updateCurrentUser(data) {
    return request({
        url: API.UPDATE_CURRENT_USER,
        method: 'put', // 假设使用 PUT 方法更新个人资料
        data
    })
}

// 修改密码
export function changePassword(data) {
    return request({
        url: API.CHANGE_PASSWORD,
        method: 'post',
        params: data // 或使用 data，取决于后端接口设计
    })
}

// 获取用户列表 (分页或全量，根据后端实现调整)
export function getUserList(params) {
    return request({
        url: API.GET_USER_LIST,
        method: 'get',
        params // 传递查询参数，如 page, size, filters
    })
}

// 添加用户 (管理员)
export function addUser(data) {
    return request({
        url: API.ADD_USER,
        method: 'post',
        data
    })
}

// 更新用户信息 (管理员)
export function updateUser(id, data) {
    return request({
        url: API.UPDATE_USER(id),
        method: 'put',
        data
    })
}

// 删除用户 (管理员)
export function deleteUser(id) {
    return request({
        url: API.DELETE_USER(id),
        method: 'delete'
    })
}

// 重置用户密码 (管理员)
export function resetPassword(id, data) { // 如果后端需要数据 (例如新密码)，则添加 data 参数
    return request({
        url: API.RESET_PASSWORD(id),
        method: 'post',
        data // 如果需要传递数据的话
    })
}

// 检查权限 (根据后端实现调整)
export function checkPermission(operation) {
    return request({
        url: API.CHECK_PERMISSION,
        method: 'get', // 或 POST?
        params: {operation} // 假设操作作为查询参数传递
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
        method: 'post' // 假设使用 POST 方法登出
    })
}

// --- 以下为示例，根据原有 user.js 或 controller 实际情况增删改 ---
// // 获取用户角色 (示例)
// export function fetchUserRoles(userId) {
//   return request({
//     url: `/users/${userId}/roles`, // 示例：端点可能尚未在 API 对象中定义
//     method: 'get'
//   })
// }
// // 更新用户状态 (示例)
// export function updateUserStatus(userId, status) {
//   return request({
//     url: `/users/${userId}/status`, // 示例：端点可能尚未在 API 对象中定义
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
    // 假设 getUserList 可以按角色过滤，或者存在新的端点
    // 例如：GET /api/users/teachers
    return request({
        url: API.GET_USER_LIST, // 复用获取用户列表接口
        method: 'get',
        params: {...params, role: 'teacher'} // 如果支持，添加角色过滤器
    })
}

// 添加 getUserInfo 函数 (这个看起来是模拟数据，暂时保留，但建议后续移除或整合)
export function getUserInfo() {
    // 注意：这似乎是一个模拟数据函数，不是实际的 API 调用
    // 它总是返回一个固定的教师信息。在实际应用中，应该调用 getCurrentUser。
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
 * 获取用户个人资料 (通过用户ID)
 * @param {number} userId 用户ID
 * @returns {Promise} 请求响应 Promise
 */
export function getUserProfile(userId) {
    // 注意：此函数似乎与下面的 updateUserProfile 使用了不同的端点概念
    // 如果需要获取特定用户的资料（可能管理员视角），请确认后端接口
    // 如果是获取当前登录用户的，应使用 getCurrentUser
    return request({
        url: `/users/${userId}`, // 直接使用用户ID构造 URL
        method: 'get'
    })
}

/**
 * 更新用户个人资料 (当前登录用户)
 * @param {object} data 用户资料数据
 * @returns {Promise} 请求响应 Promise
 */
export function updateUserProfile(data) {
    // 注意：此函数名可能与 updateCurrentUser 功能重叠
    // 建议统一接口或明确命名
    return request({
        url: '/users/profile', // 指向当前用户资料更新端点
        method: 'put',
        data
    })
}

/**
 * 获取用户个人资料 (可能用于管理员视角)
 * @param {number} userId 用户ID
 * @returns {Promise} 请求响应 Promise
 */
export function getUserProfileById(userId) {
    // 注意：此函数复用了 UPDATE_USER 的 URL，方法为 GET，可能不准确
    // 建议后端提供专门的 GET /users/{id}/profile 或类似接口
    return request({
        url: API.UPDATE_USER(userId), // 使用管理员更新接口的路径，但方法是 GET
        method: 'get'
    })
}

/**
 * 根据ID获取用户信息 (管理员用)
 * @param {number} userId 用户ID
 * @returns {Promise} 请求响应 Promise
 */
export function getUserById(userId) {
    // 同上，建议后端提供专门的 GET /users/{id} 接口
    return request({
        url: API.UPDATE_USER(userId), // 使用管理员更新接口的路径，但方法是 GET
        method: 'get'
    })
}

/**
 * 根据用户名获取用户信息 (管理员用，如果后端支持)
 * @param {string} username 用户名
 * @returns {Promise} 请求响应 Promise
 */
export function getUserByUsername(username) {
    // 假设后端支持通过用户名查询，例如 GET /users?username=xxx
    return request({
        url: API.GET_USER_LIST, // 复用列表接口
        method: 'get',
        params: {username} // 添加查询参数
    })
}

/**
 * 获取教师列表 (管理员用，如果后端支持特定接口)
 * @param {object} params 查询参数 (分页、过滤等)
 * @returns {Promise} 请求响应 Promise
 */
export function getTeacherList(params) {
    // 假设后端有专门获取教师列表的接口，例如 GET /users/teachers
    return request({
        url: '/users/teachers', // 或者复用 getUserList 并添加 role='teacher' 过滤器
        method: 'get',
        params
    })
} 