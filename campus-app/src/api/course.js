import request from '@/utils/request'

// API Endpoints for Course
const API = {
    GET_ALL: '/courses',
    GET_BY_ID: (id) => `/courses/${id}`,
    GET_BY_NO: (courseNo) => `/courses/no/${courseNo}`,
    GET_BY_TYPE: (courseType) => `/courses/type/${courseType}`,
    GET_BY_COLLEGE: (collegeId) => `/courses/college/${collegeId}`,
    ADD: '/courses',
    UPDATE: (id) => `/courses/${id}`,
    DELETE: (id) => `/courses/${id}`,
    BATCH_DELETE: '/courses/batch',
    UPDATE_STATUS: (id, status) => `/courses/${id}/status/${status}`
};

// 获取所有课程 (可带分页或过滤参数)
export function getAllCourses(params) {
    return request({
        url: API.GET_ALL,
        method: 'get',
        params
    });
}

// 根据ID获取课程
export function getCourseById(id) {
    return request({
        url: API.GET_BY_ID(id),
        method: 'get'
    });
}

// 根据课程编号获取课程
export function getCourseByNo(courseNo) {
    return request({
        url: API.GET_BY_NO(courseNo),
        method: 'get'
    });
}

// 根据课程类型获取课程
export function getCoursesByType(courseType, params) {
    return request({
        url: API.GET_BY_TYPE(courseType),
        method: 'get',
        params
    });
}

// 根据学院ID获取课程
export function getCoursesByCollege(collegeId, params) {
    return request({
        url: API.GET_BY_COLLEGE(collegeId),
        method: 'get',
        params
    });
}

// 添加课程
export function addCourse(data) {
    return request({
        url: API.ADD,
        method: 'post',
        data
    });
}

// 更新课程
export function updateCourse(id, data) {
    return request({
        url: API.UPDATE(id),
        method: 'put',
        data
    });
}

// 删除课程
export function deleteCourse(id) {
    return request({
        url: API.DELETE(id),
        method: 'delete'
    });
}

// 批量删除课程
export function batchDeleteCourses(ids) {
    return request({
        url: API.BATCH_DELETE,
        method: 'delete', // Or 'post' depending on backend
        data: ids // Send IDs in request body
    });
}

// 更新课程状态
export function updateCourseStatus(id, status) {
    return request({
        url: API.UPDATE_STATUS(id, status),
        method: 'put' // Or 'patch'
    });
}

// 获取所有课程 (用于下拉选择)
export function getAllCoursesForSelect(params) {
    return request({
        url: API.GET_ALL, // 使用定义的路径
        method: 'get',
        params
    })
}

// 获取所有课程 (分页列表)
export function getCourseList(params) {
    return request({
        url: API.GET_ALL, // 使用定义的路径
        method: 'get',
        params
    })
}

// 教师获取自己的课程列表
export function getTeacherCourses(params) {
    return request({
        url: API.GET_ALL, // 使用定义的路径
        method: 'get',
        params
    })
}

// 学生获取自己的课程列表
export function getStudentCourses(params) {
    return request({
        url: API.GET_ALL, // 使用定义的路径
        method: 'get',
        params
    })
} 