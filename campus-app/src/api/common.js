import request from '@/utils/request'

// API Endpoints for Common Lookups
const API = {
    GET_COLLEGES: '/common/colleges',
    GET_DEPARTMENTS: '/common/departments',
    GET_MAJORS: '/common/majors',
    GET_CLASSES: '/common/classes',
    GET_TERMS: '/common/terms',
    GET_WEEKDAYS: '/common/weekdays',
    GET_TIME_SLOTS: '/common/time-slots',
    GET_ROOM_TYPES: '/common/room-types',
    GET_COURSE_TYPES: '/common/course-types',
    GET_ACTIVITY_TYPES: '/common/activity-types',
    GET_NOTICE_TYPES: '/notifications/types',
    GET_POST_CATEGORIES: '/common/post-categories' // Matches FORUM_API.GET_CATEGORIES?
};

// 获取学院列表
export function getColleges(params) {
    return request({
        url: API.GET_COLLEGES,
        method: 'get',
        params
    });
}

// 获取系别列表 (可能需要 collegeId 参数)
export function getDepartments(params) {
    return request({
        url: API.GET_DEPARTMENTS,
        method: 'get',
        params
    });
}

// 获取专业列表 (可能需要 departmentId 参数)
export function getMajors(params) {
    return request({
        url: API.GET_MAJORS,
        method: 'get',
        params
    });
}

// 获取班级列表 (可能需要 majorId 参数)
export function getClasses(params) {
    return request({
        url: API.GET_CLASSES,
        method: 'get',
        params
    });
}

// 获取学期列表
export function getTerms(params) {
    return request({
        url: API.GET_TERMS,
        method: 'get',
        params
    });
}

// 获取星期列表 (可能直接在前端定义)
export function getWeekdays(params) {
    return request({
        url: API.GET_WEEKDAYS,
        method: 'get',
        params
    });
}

// 获取时间段/节次列表 (可能直接在前端定义)
export function getTimeSlots(params) {
    return request({
        url: API.GET_TIME_SLOTS,
        method: 'get',
        params
    });
}

// 获取教室类型列表
export function getRoomTypes(params) {
    return request({
        url: API.GET_ROOM_TYPES,
        method: 'get',
        params
    });
}

// 获取课程类型列表
export function getCourseTypes(params) {
    return request({
        url: API.GET_COURSE_TYPES,
        method: 'get',
        params
    });
}

// 获取活动类型列表
export function getActivityTypes(params) {
    return request({
        url: API.GET_ACTIVITY_TYPES,
        method: 'get',
        params
    });
}

// 获取通知类型列表
export function getNoticeTypes(params) {
    return request({
        url: API.GET_NOTICE_TYPES,
        method: 'get',
        params
    });
}

// 获取帖子分类列表 (与 forum API 中的 getCategories 重复，需确认)
export function getPostCategories(params) {
    return request({
        url: API.GET_POST_CATEGORIES,
        method: 'get',
        params
    });
}