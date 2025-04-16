import request from './request'
import {USER_API} from './api-endpoints'

// 用户登录
export function login(data) {
    return request({
        url: USER_API.LOGIN,
        method: 'post',
        data
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

// 获取当前用户信息
export function getCurrentUser() {
    return request({
        url: USER_API.GET_CURRENT_USER,
        method: 'get'
    })
}

// 更新用户信息
export function updateUser(id, data) {
    return request({
        url: USER_API.UPDATE_USER.replace(':id', id),
        method: 'put',
        data
    })
}

// 修改密码
export function changePassword(data) {
    return request({
        url: USER_API.CHANGE_PASSWORD,
        method: 'put',
        data
    })
}

// 获取用户列表
export function getUserList(params) {
    return request({
        url: USER_API.GET_USER_LIST,
        method: 'get',
        params
    })
}

// 添加用户
export function addUser(data) {
    return request({
        url: USER_API.ADD_USER,
        method: 'post',
        data
    })
}

// 删除用户
export function deleteUser(id) {
    return request({
        url: USER_API.DELETE_USER.replace(':id', id),
        method: 'delete'
    })
}

// 检查权限
export function checkPermission(permission) {
    return request({
        url: USER_API.CHECK_PERMISSION,
        method: 'get',
        params: {permission}
    })
}

// 获取权限列表
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