import request from '../utils/request'
import {NOTICE_API} from './api-endpoints'

// 获取所有通知
export function getAllNotifications(params) {
    return request({
        url: NOTICE_API.GET_ALL, // 移除 API_BASE
        method: 'get',
        params
    })
}

// 获取通知详情
export function getNotificationById(id) {
    return request({
        url: NOTICE_API.GET_BY_ID.replace(':id', id), // 移除 API_BASE
        method: 'get'
    })
}

// 添加通知
export function addNotification(data) {
    return request({
        url: NOTICE_API.ADD, // 移除 API_BASE
        method: 'post',
        data
    })
}

// 更新通知
export function updateNotification(id, data) {
    return request({
        url: NOTICE_API.UPDATE.replace(':id', id), // 移除 API_BASE
        method: 'put',
        data
    })
}

// 删除通知
export function deleteNotification(id) {
    return request({
        url: NOTICE_API.DELETE.replace(':id', id), // 移除 API_BASE
        method: 'delete'
    })
}

// 获取通知列表（分页）
export function getNotificationList(params) {
    return request({
        url: NOTICE_API.GET_PAGE, // 移除 API_BASE
        method: 'get',
        params
    })
}

// 获取最近通知
export function getRecentNotifications() {
    return request({
        url: NOTICE_API.GET_RECENT, // 移除 API_BASE
        method: 'get'
    })
}

// 获取置顶通知
export function getTopNotifications() {
    return request({
        url: NOTICE_API.GET_TOP, // 移除 API_BASE
        method: 'get'
    })
}

// 更新通知状态
export function updateNotificationStatus(id, status) {
    return request({
        url: NOTICE_API.UPDATE_STATUS.replace(':id', id).replace(':status', status), // 移除 API_BASE
        method: 'put'
    })
}

// 上传通知附件
export function uploadNotificationAttachment(data) {
    return request({
        url: NOTICE_API.UPLOAD_ATTACHMENT, // 移除 API_BASE
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 为了向后兼容，保留旧方法名称，但调用新方法
export const getAllNotices = getAllNotifications;
export const getNoticeById = getNotificationById;
export const addNotice = addNotification;
export const updateNotice = updateNotification;
export const deleteNotice = deleteNotification;
export const getNoticeList = getNotificationList;
export const getRecentNotices = getRecentNotifications;
export const getTopNotices = getTopNotifications;
export const updateNoticeStatus = updateNotificationStatus;
export const uploadNoticeAttachment = uploadNotificationAttachment;