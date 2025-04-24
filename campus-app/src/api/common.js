import request from '../utils/request'
import {COMMON_API} from './api-endpoints'

// 获取学院列表
export function getColleges() {
    return request({
        url: COMMON_API.GET_COLLEGES, // 移除 API_BASE
        method: 'get'
    })
}

// 获取系部列表
export function getDepartments() {
    return request({
        url: COMMON_API.GET_DEPARTMENTS, // 移除 API_BASE
        method: 'get'
    })
}

// 获取专业列表
export function getMajors() {
    return request({
        url: COMMON_API.GET_MAJORS, // 移除 API_BASE
        method: 'get'
    })
}

// 获取班级列表
export function getClasses() {
    return request({
        url: COMMON_API.GET_CLASSES, // 移除 API_BASE
        method: 'get'
    })
}

// 获取学期列表
export function getTerms() {
    return request({
        url: COMMON_API.GET_TERMS, // 移除 API_BASE
        method: 'get'
    })
}

// 获取星期列表
export function getWeekdays() {
    return request({
        url: COMMON_API.GET_WEEKDAYS, // 移除 API_BASE
        method: 'get'
    })
}

// 获取时间段列表
export function getTimeSlots() {
    return request({
        url: COMMON_API.GET_TIME_SLOTS, // 移除 API_BASE
        method: 'get'
    })
}

// 获取教室类型列表
export function getRoomTypes() {
    return request({
        url: COMMON_API.GET_ROOM_TYPES, // 移除 API_BASE
        method: 'get'
    })
}

// 获取课程类型列表
export function getCourseTypes() {
    return request({
        url: COMMON_API.GET_COURSE_TYPES, // 移除 API_BASE
        method: 'get'
    })
}

// 获取活动类型列表
export function getActivityTypes() {
    return request({
        url: COMMON_API.GET_ACTIVITY_TYPES, // 移除 API_BASE
        method: 'get'
    })
}

// 获取通知类型列表
export function getNoticeTypes() {
    return request({
        url: COMMON_API.GET_NOTICE_TYPES, // 移除 API_BASE
        method: 'get'
    })
}

// 获取帖子分类列表
export function getPostCategories() {
    return request({
        url: COMMON_API.GET_POST_CATEGORIES, // 移除 API_BASE
        method: 'get'
    })
}