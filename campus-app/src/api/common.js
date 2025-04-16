import request from './request'
import {COMMON_API} from './api-endpoints'

// 获取学院列表
export function getColleges() {
    return request({
        url: COMMON_API.GET_COLLEGES,
        method: 'get'
    })
}

// 获取系部列表
export function getDepartments() {
    return request({
        url: COMMON_API.GET_DEPARTMENTS,
        method: 'get'
    })
}

// 获取专业列表
export function getMajors() {
    return request({
        url: COMMON_API.GET_MAJORS,
        method: 'get'
    })
}

// 获取班级列表
export function getClasses() {
    return request({
        url: COMMON_API.GET_CLASSES,
        method: 'get'
    })
}

// 获取学期列表
export function getTerms() {
    return request({
        url: COMMON_API.GET_TERMS,
        method: 'get'
    })
}

// 获取星期列表
export function getWeekdays() {
    return request({
        url: COMMON_API.GET_WEEKDAYS,
        method: 'get'
    })
}

// 获取时间段列表
export function getTimeSlots() {
    return request({
        url: COMMON_API.GET_TIME_SLOTS,
        method: 'get'
    })
}

// 获取教室类型列表
export function getRoomTypes() {
    return request({
        url: COMMON_API.GET_ROOM_TYPES,
        method: 'get'
    })
}

// 获取课程类型列表
export function getCourseTypes() {
    return request({
        url: COMMON_API.GET_COURSE_TYPES,
        method: 'get'
    })
}

// 获取活动类型列表
export function getActivityTypes() {
    return request({
        url: COMMON_API.GET_ACTIVITY_TYPES,
        method: 'get'
    })
}

// 获取通知类型列表
export function getNoticeTypes() {
    return request({
        url: COMMON_API.GET_NOTICE_TYPES,
        method: 'get'
    })
}

// 获取帖子分类列表
export function getPostCategories() {
    return request({
        url: COMMON_API.GET_POST_CATEGORIES,
        method: 'get'
    })
} 