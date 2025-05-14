import request from '@/utils/request'

// 通知相关 API 端点
const API = {
    GET_ALL: '/notifications/all', // 获取所有通知 (不分页)
    GET_PAGE: '/notifications/page', // 获取通知列表 (分页)
    GET_BY_ID: (id) => `/notifications/${id}`, // 根据 ID 获取通知详情
    GET_BY_TYPE: (noticeType) => `/notifications/type/${noticeType}`, // 根据类型获取通知
    GET_BY_STATUS: (status) => `/notifications/status/${status}`, // 根据状态获取通知
    GET_RECENT: '/notifications/recent', // 获取最近通知
    GET_TOP: '/notifications/top', // 获取置顶通知
    GET_BY_PUBLISHER: (publisherId) => `/notifications/publisher/${publisherId}`, // 根据发布者获取通知
    ADD: '/notifications', // 添加通知
    UPDATE: (id) => `/notifications/${id}`, // 更新通知
    DELETE: (id) => `/notifications/${id}`, // 删除通知
    BATCH_DELETE: '/notifications/batch', // 批量删除通知
    UPDATE_STATUS: (id) => `/notifications/status/${id}`, // 更新通知状态 (发布/草稿等)
    UPDATE_TOP: (id) => `/notifications/top/${id}` // 更新通知置顶状态
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
export function getRecentNotifications(params) { // params 可能包含 limit 参数
    return request({
        url: API.GET_RECENT,
        method: 'get',
        params
    });
}

// 获取置顶通知
export function getTopNotifications(params) { // params 可能包含 limit 参数
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
        method: 'delete', // 或 POST
        data: ids // 后端接收 @RequestBody List<Long> ids，使用 data 匹配
    });
}

// 更新通知状态 (发布/撤销等)
export function updateNotificationStatus(id, status) {
    return request({
        url: API.UPDATE_STATUS(id),
        method: 'put', // 或 PATCH
        params: {status} // 使用 params 匹配后端的 @RequestParam
    });
}

// 更新通知置顶状态
export function updateNotificationTopStatus(id, isTop) {
    return request({
        url: API.UPDATE_TOP(id),
        method: 'put',
        params: {isTop}
    });
}

// --- 可能缺少或需要根据 Controller 添加的接口 --- 
// // 标记通知为已读 (示例)
// export function markNotificationRead(id) {
//     return request({
//         url: `/notifications/${id}/read`, // 示例端点
//         method: 'post'
//     });
// }

// // 获取未读通知数量 (示例)
// export function getUnreadNotificationCount() {
//     return request({
//         url: '/notifications/unread/count', // 示例端点
//         method: 'get'
//     });
// }