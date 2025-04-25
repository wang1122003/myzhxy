import request from '@/utils/request'

// API Endpoints for Notification
const API = {
    GET_ALL: '/notifications/all',
    GET_PAGE: '/notifications/page',
    GET_BY_ID: (id) => `/notifications/${id}`,
    GET_BY_TYPE: (noticeType) => `/notifications/type/${noticeType}`,
    GET_BY_STATUS: (status) => `/notifications/status/${status}`,
    GET_RECENT: '/notifications/recent',
    GET_TOP: '/notifications/top',
    GET_BY_PUBLISHER: (publisherId) => `/notifications/publisher/${publisherId}`,
    ADD: '/notifications',
    UPDATE: (id) => `/notifications/${id}`,
    DELETE: (id) => `/notifications/${id}`,
    BATCH_DELETE: '/notifications/batch',
    UPDATE_STATUS: (id) => `/notifications/status/${id}` // Path format seems different, verify controller mapping
};

// 获取所有通知 (非分页)
export function getAllNotifications(params) {
    return request({
        url: API.GET_ALL,
        method: 'get',
        params
    });
}

// 获取通知列表 (分页)
export function getNotificationsPage(params) {
    return request({
        url: API.GET_PAGE,
        method: 'get',
        params
    });
}

// 根据ID获取通知详情
export function getNotificationById(id) {
    return request({
        url: API.GET_BY_ID(id),
        method: 'get'
    });
}

// 根据类型获取通知
export function getNotificationsByType(noticeType, params) {
    return request({
        url: API.GET_BY_TYPE(noticeType),
        method: 'get',
        params
    });
}

// 根据状态获取通知
export function getNotificationsByStatus(status, params) {
    return request({
        url: API.GET_BY_STATUS(status),
        method: 'get',
        params
    });
}

// 获取最近通知
export function getRecentNotifications(params) { // params might include limit
    return request({
        url: API.GET_RECENT,
        method: 'get',
        params
    });
}

// 获取置顶通知
export function getTopNotifications(params) { // params might include limit
    return request({
        url: API.GET_TOP,
        method: 'get',
        params
    });
}

// 根据发布者获取通知
export function getNotificationsByPublisher(publisherId, params) {
    return request({
        url: API.GET_BY_PUBLISHER(publisherId),
        method: 'get',
        params
    });
}

// 添加通知
export function addNotification(data) {
    return request({
        url: API.ADD,
        method: 'post',
        data
    });
}

// 更新通知
export function updateNotification(id, data) {
    return request({
        url: API.UPDATE(id),
        method: 'put',
        data
    });
}

// 删除通知
export function deleteNotification(id) {
    return request({
        url: API.DELETE(id),
        method: 'delete'
    });
}

// 批量删除通知
export function batchDeleteNotifications(ids) {
    return request({
        url: API.BATCH_DELETE,
        method: 'delete', // Or POST
        data: ids // 后端是 @RequestBody List<Long> ids, data 方式匹配
    });
}

// 更新通知状态 (发布/撤销等)
export function updateNotificationStatus(id, status) {
    return request({
        url: API.UPDATE_STATUS(id),
        method: 'put', // Or PATCH
        // data: { status } // 原方式，发送 Request Body
        params: {status} // 修正：使用 params 匹配后端的 @RequestParam
    });
}

// --- Potential additions/missing based on Controller --- 
// // Mark notification as read
// export function markNotificationRead(id) {
//     return request({
//         url: `/notifications/${id}/read`, // Example endpoint
//         method: 'post'
//     });
// }

// // Get unread notification count
// export function getUnreadNotificationCount() {
//     return request({
//         url: '/notifications/unread/count', // Example endpoint
//         method: 'get'
//     });
// }