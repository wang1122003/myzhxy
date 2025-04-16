import request from './request'
import {NOTICE_API} from './api-endpoints'

// 获取所有通知
export function getAllNotices(params) {
    return request({
        url: NOTICE_API.GET_ALL,
        method: 'get',
        params
    })
}

// 获取通知详情
export function getNoticeById(id) {
    return request({
        url: NOTICE_API.GET_BY_ID.replace(':id', id),
        method: 'get'
    })
}

// 添加通知
export function addNotice(data) {
    return request({
        url: NOTICE_API.ADD,
        method: 'post',
        data
    })
}

// 更新通知
export function updateNotice(id, data) {
    return request({
        url: NOTICE_API.UPDATE.replace(':id', id),
        method: 'put',
        data
    })
}

// 删除通知
export function deleteNotice(id) {
    return request({
        url: NOTICE_API.DELETE.replace(':id', id),
        method: 'delete'
    })
}

// 获取通知列表（分页）
export function getNoticeList(params) {
    return request({
        url: NOTICE_API.GET_LIST,
        method: 'get',
        params
    })
}

// 获取最近通知
export function getRecentNotices() {
    return request({
        url: NOTICE_API.GET_RECENT,
        method: 'get'
    })
}

// 获取置顶通知
export function getTopNotices() {
    return request({
        url: NOTICE_API.GET_TOP,
        method: 'get'
    })
}

// 更新通知状态
export function updateNoticeStatus(id, status) {
    return request({
        url: NOTICE_API.UPDATE_STATUS.replace(':id', id).replace(':status', status),
        method: 'put'
    })
}

// 上传通知附件
export function uploadNoticeAttachment(data) {
    return request({
        url: NOTICE_API.UPLOAD_ATTACHMENT,
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
} 