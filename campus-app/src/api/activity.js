import request from './request'
import {ACTIVITY_API} from './api-endpoints'

// 获取所有活动
export function getAllActivities(params) {
    return request({
        url: ACTIVITY_API.GET_ALL,
        method: 'get',
        params
    })
}

// 获取活动详情
export function getActivityById(id) {
    return request({
        url: ACTIVITY_API.GET_BY_ID.replace(':id', id),
        method: 'get'
    })
}

// 添加活动
export function addActivity(data) {
    return request({
        url: ACTIVITY_API.ADD,
        method: 'post',
        data
    })
}

// 更新活动
export function updateActivity(id, data) {
    return request({
        url: ACTIVITY_API.UPDATE.replace(':id', id),
        method: 'put',
        data
    })
}

// 删除活动
export function deleteActivity(id) {
    return request({
        url: ACTIVITY_API.DELETE.replace(':id', id),
        method: 'delete'
    })
}

// 获取活动列表（分页）
export function getActivityList(params) {
    return request({
        url: ACTIVITY_API.GET_LIST,
        method: 'get',
        params
    })
}

// 获取进行中的活动
export function getOngoingActivities() {
    return request({
        url: ACTIVITY_API.GET_ONGOING,
        method: 'get'
    })
}

// 获取即将开始的活动
export function getUpcomingActivities() {
    return request({
        url: ACTIVITY_API.GET_UPCOMING,
        method: 'get'
    })
}

// 更新活动状态
export function updateActivityStatus(id, status) {
    return request({
        url: ACTIVITY_API.UPDATE_STATUS.replace(':id', id).replace(':status', status),
        method: 'put'
    })
}

// 上传活动海报
export function uploadActivityPoster(data) {
    return request({
        url: ACTIVITY_API.UPLOAD_POSTER,
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 学生报名活动
export function joinActivity(id) {
    return request({
        url: `/activity/join/${id}`,
        method: 'post'
    })
}

// 学生取消报名
export function cancelJoinActivity(id) {
    return request({
        url: `/activity/cancel/${id}`,
        method: 'post'
    })
}

// 学生评价活动
export function rateActivity(id, data) {
    return request({
        url: `/activity/rate/${id}`,
        method: 'post',
        data
    })
}

// 获取学生已报名活动
export function getStudentActivities() {
    return request({
        url: '/student/activities',
        method: 'get'
    })
}

// 管理员获取活动报名情况
export function getActivityEnrollments(id) {
    return request({
        url: `/admin/activity/enrollments/${id}`,
        method: 'get'
    })
} 