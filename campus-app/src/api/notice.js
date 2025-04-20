import request from './request'
import {NOTICE_API} from './api-endpoints'

const API_BASE = '/api'; // Define the base path

// 获取所有通知
export function getAllNotifications(params) {
    return request({
        url: `${API_BASE}${NOTICE_API.GET_ALL}`,
        method: 'get',
        params
    })
}

// 获取通知详情
export function getNotificationById(id) {
    return request({
        url: `${API_BASE}${NOTICE_API.GET_BY_ID.replace(':id', id)}`,
        method: 'get'
    })
}

// 添加通知
export function addNotification(data) {
    return request({
        url: `${API_BASE}${NOTICE_API.ADD}`,
        method: 'post',
        data
    })
}

// 更新通知
export function updateNotification(id, data) {
    return request({
        url: `${API_BASE}${NOTICE_API.UPDATE.replace(':id', id)}`,
        method: 'put',
        data
    })
}

// 删除通知
export function deleteNotification(id) {
    return request({
        url: `${API_BASE}${NOTICE_API.DELETE.replace(':id', id)}`,
        method: 'delete'
    })
}

// 获取通知列表（分页）
export function getNotificationList(params) {
    return request({
        url: `${API_BASE}${NOTICE_API.GET_PAGE}`,
        method: 'get',
        params
    })
}

// 获取最近通知
export function getRecentNotifications() {
    return request({
        url: `${API_BASE}${NOTICE_API.GET_RECENT}`,
        method: 'get'
    })
}

// 获取置顶通知
export function getTopNotifications() {
    return request({
        url: `${API_BASE}${NOTICE_API.GET_TOP}`,
        method: 'get'
    })
}

// 更新通知状态
export function updateNotificationStatus(id, status) {
    return request({
        url: `${API_BASE}${NOTICE_API.UPDATE_STATUS.replace(':id', id).replace(':status', status)}`,
        method: 'put'
    })
}

// 上传通知附件
export function uploadNotificationAttachment(data) {
    return request({
        url: `${API_BASE}${NOTICE_API.UPLOAD_ATTACHMENT}`,
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