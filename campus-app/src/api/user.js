import request from '../utils/request'
import {USER_API} from './api-endpoints'

// 用户登录
export function login(data) {
    return request({
        url: USER_API.LOGIN,
        method: 'post',
        data: data
    })
}

// 用户注册
export function register(data) {
    return request({
        url: USER_API.REGISTER,
        method: 'post',
        data
    })
}

// 更新学生个人信息 (使用通用接口)
export function updateStudentProfile(data) {
    return request({
        url: USER_API.UPDATE_CURRENT_USER, // 改为使用通用更新接口
        method: 'put',
        data
    })
}

// 更新教师个人信息 (使用通用接口)
export function updateTeacherProfile(data) {
    return request({
        url: USER_API.UPDATE_CURRENT_USER, // 改为使用通用更新接口
        method: 'put',
        data
    })
}

// 更新管理员个人信息 (使用通用接口)
export function updateAdminProfile(data) {
    return request({
        url: USER_API.UPDATE_CURRENT_USER, // 改为使用通用更新接口
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
        url: USER_API.GET_USER_LIST, // Reusing GET_USER_LIST
        method: 'get',
        params: {...params, role: 'teacher'} // Add role filter if supported
    })
}

// 更新用户信息 (管理员用)
export function updateUser(id, data) {
    return request({
        url: USER_API.UPDATE_USER.replace(':id', id),
        method: 'put',
        data
    })
}

// 修改密码 (个人用)
export function changePassword(data) {
    return request({
        url: USER_API.CHANGE_PASSWORD,
        method: 'put',
        data
    })
}

// 获取用户列表 (管理员用)
export function getUserList(params) {
    return request({
        url: USER_API.GET_USER_LIST,
        method: 'get',
        params
    })
}

// 添加用户 (管理员用)
export function addUser(data) {
    return request({
        url: USER_API.ADD_USER,
        method: 'post',
        data
    })
}

// 删除用户 (管理员用)
export function deleteUser(id) {
    return request({
        url: USER_API.DELETE_USER.replace(':id', id),
        method: 'delete'
    })
}

// 重置用户密码 (管理员用)
export function resetPassword(id) {
    return request({
        url: USER_API.RESET_PASSWORD.replace(':id', id), // 使用刚添加的 RESET_PASSWORD
        method: 'put'
    })
}

// --- 权限和登出 --- 

// 检查权限 (可能已废弃，使用后端 Filter)
export function checkPermission(permission) {
    return request({
        url: USER_API.CHECK_PERMISSION,
        method: 'get',
        params: {permission}
    })
}

// 获取权限列表 (可能已废弃)
export function getPermissions() {
    return request({
        url: USER_API.GET_PERMISSIONS,
        method: 'get'
    })
}

// 登出
export function logout() {
    return request({
        url: USER_API.LOGOUT,
        method: 'post'
    })
}

// 更新用户个人信息 (通用 profile 接口) - 改用 updateCurrentUser
export function updateCurrentUserProfile(data) { // 重命名以避免与通用更新混淆，或直接调用通用更新
    return request({
        url: USER_API.UPDATE_CURRENT_USER, // 使用通用更新接口
        method: 'put',
        data
    })
}

// 更新用户状态 (管理员用)
export function updateUserStatus(id, status) {
    return request({
        url: `${USER_API.UPDATE_USER.replace(':id', id)}/status`,
        method: 'put',
        data: {status}
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
 * 上传用户头像
 * @param {FormData} formData 包含头像文件的表单数据
 * @returns {Promise} 请求响应promise
 */
export function uploadAvatar(formData) {
    return request({
        url: '/users/upload-avatar',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * 根据ID获取用户信息
 * @param {number} userId 用户ID
 * @returns {Promise} 请求响应promise
 */
export function getUserById(userId) {
    return request({
        url: `/users/${userId}`,
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
        url: USER_API.GET_USER_LIST, // 使用通用用户列表接口
        method: 'get',
        params: { ...params, userType: 'Teacher' } // 通过参数指定用户类型
    })
} 