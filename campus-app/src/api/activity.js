import request from '@/utils/request'

// API Endpoints for Activity
const API = {
    GET_ALL: '/activities',
    GET_BY_ID: (id) => `/activities/${id}`,
    GET_BY_TYPE: (activityType) => `/activities/type/${activityType}`,
    GET_BY_STATUS: (status) => `/activities/status/${status}`,
    GET_ONGOING: '/activities/ongoing',
    GET_UPCOMING: '/activities/upcoming',
    ADD: '/activities',
    ADD_WITH_POSTER: '/activities/with-poster', // Assumes backend combines data and poster upload or handles separately
    UPDATE: (id) => `/activities/${id}`,
    DELETE: (id) => `/activities/${id}`,
    BATCH_DELETE: '/activities/batch',
    UPDATE_STATUS: (id, status) => `/activities/${id}/status/${status}`,
    UPLOAD_POSTER: '/activities/poster/upload', // Likely a separate call before ADD_WITH_POSTER or UPDATE?
    GET_BY_PUBLISHER: (publisherId) => `/activities/publisher/${publisherId}`,
    GET_STUDENT_ACTIVITIES: '/activities/student/my',
    JOIN_ACTIVITY: (id) => `/activities/join/${id}`,
    CANCEL_JOIN: (id) => `/activities/cancel/${id}`,
    RATE_ACTIVITY: (id) => `/activities/rate/${id}`,
    GET_ENROLLMENTS: (id) => `/admin/activities/enrollments/${id}` // Note: path starts with /admin
};

// 获取所有活动 (可带分页或过滤参数)
export function getAllActivities(params) {
    return request({
        url: API.GET_ALL,
        method: 'get',
        params
    });
}

// 根据ID获取活动详情
export function getActivityById(id) {
    return request({
        url: API.GET_BY_ID(id),
        method: 'get'
    });
}

// 根据类型获取活动
export function getActivitiesByType(activityType, params) {
    return request({
        url: API.GET_BY_TYPE(activityType),
        method: 'get',
        params
    });
}

// 根据状态获取活动
export function getActivitiesByStatus(status, params) {
    return request({
        url: API.GET_BY_STATUS(status),
        method: 'get',
        params
    });
}

// 获取正在进行的活动
export function getOngoingActivities(params) {
    return request({
        url: API.GET_ONGOING,
        method: 'get',
        params
    });
}

// 获取即将开始的活动
export function getUpcomingActivities(params) {
    return request({
        url: API.GET_UPCOMING,
        method: 'get',
        params
    });
}

// 添加活动 (不含海报，或海报单独上传)
export function addActivity(data) {
    return request({
        url: API.ADD,
        method: 'post',
        data
    });
}

// 添加活动 (包含海报，具体实现依赖后端)
export function addActivityWithPoster(formData) { // Assuming formData includes activity data and poster file
    return request({
        url: API.ADD_WITH_POSTER,
        method: 'post',
        data: formData, // Send as FormData
        headers: {'Content-Type': 'multipart/form-data'}
    });
}

// 更新活动
export function updateActivity(id, data) {
    return request({
        url: API.UPDATE(id),
        method: 'put',
        data
    });
}

// 删除活动
export function deleteActivity(id) {
    return request({
        url: API.DELETE(id),
        method: 'delete'
    });
}

// 批量删除活动
export function batchDeleteActivities(ids) {
    return request({
        url: API.BATCH_DELETE,
        method: 'delete', // Or POST
        data: ids
    });
}

// 更新活动状态
export function updateActivityStatus(id, status) {
    return request({
        url: API.UPDATE_STATUS(id, status),
        method: 'put' // Or PATCH
    });
}

// 上传活动海报 (可能需要返回文件URL，用于后续添加到活动数据中)
// export function uploadActivityPoster(file) { // 功能已移至 file.js/uploadActivityPosterFile
//     const formData = new FormData();
//     formData.append('file', file); // Backend needs to expect 'file' field
//     return request({
//         url: API.UPLOAD_POSTER,
//         method: 'post',
//         data: formData,
//         headers: { 'Content-Type': 'multipart/form-data' }
//     });
// }

// 根据发布者获取活动
export function getActivitiesByPublisher(publisherId, params) {
    return request({
        url: API.GET_BY_PUBLISHER(publisherId),
        method: 'get',
        params
    });
}

// 获取当前学生参加/报名的活动
export function getMyActivities(params) {
    return request({
        url: API.GET_STUDENT_ACTIVITIES,
        method: 'get',
        params
    });
}

// 报名参加活动
export function joinActivity(id) {
    return request({
        url: API.JOIN_ACTIVITY(id),
        method: 'post' // Assuming POST to join
    });
}

// 取消报名活动
export function cancelJoinActivity(id) {
    return request({
        url: API.CANCEL_JOIN(id),
        method: 'post' // Assuming POST to cancel
    });
}

// 评价活动
export function rateActivity(id, ratingData) {
    return request({
        url: API.RATE_ACTIVITY(id),
        method: 'post', // Assuming POST to rate
        data: ratingData
    });
}

// 获取活动报名列表 (管理员视角) - (后端缺失)
// export function getActivityEnrollments(id, params) {
//     return request({
//         url: API.GET_ENROLLMENTS(id),
//         method: 'get',
//         params
//     });
// } 