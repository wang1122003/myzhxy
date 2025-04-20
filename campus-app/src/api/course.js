import request from './request'
import {COURSE_API} from './api-endpoints'

// 添加统一的路径前缀
const API_PREFIX = '/api';  // 修改为/api前缀

// 获取所有课程
export function getAllCourses(params) {
    return request({
        url: `${API_PREFIX}${COURSE_API.GET_ALL}`,
        method: 'get',
        params
    })
}

// 获取课程详情
export function getCourseById(id) {
    return request({
        url: `${API_PREFIX}${COURSE_API.GET_BY_ID.replace(':id', id)}`,
        method: 'get'
    })
}

// 根据课程编号获取课程
export function getCourseByNo(courseNo) {
    return request({
        url: `${API_PREFIX}${COURSE_API.GET_BY_NO.replace(':courseNo', courseNo)}`,
        method: 'get'
    })
}

// 根据类型获取课程
export function getCourseByType(courseType) {
    return request({
        url: `${API_PREFIX}${COURSE_API.GET_BY_TYPE.replace(':courseType', courseType)}`,
        method: 'get'
    })
}

// 根据学院获取课程
export function getCourseByCollege(collegeId) {
    return request({
        url: `${API_PREFIX}${COURSE_API.GET_BY_COLLEGE.replace(':collegeId', collegeId)}`,
        method: 'get'
    })
}

// 添加课程
export function addCourse(data) {
    return request({
        url: `${API_PREFIX}${COURSE_API.ADD}`,
        method: 'post',
        data
    })
}

// 更新课程
export function updateCourse(id, data) {
    return request({
        url: `${API_PREFIX}${COURSE_API.UPDATE.replace(':id', id)}`,
        method: 'put',
        data
    })
}

// 删除课程
export function deleteCourse(id) {
    return request({
        url: `${API_PREFIX}${COURSE_API.DELETE.replace(':id', id)}`,
        method: 'delete'
    })
}

// 批量删除课程
export function batchDeleteCourses(ids) {
    return request({
        url: `${API_PREFIX}${COURSE_API.BATCH_DELETE}`,
        method: 'delete',
        data: {ids}
    })
}

// 更新课程状态
export function updateCourseStatus(id, status) {
    return request({
        url: `${API_PREFIX}${COURSE_API.UPDATE_STATUS.replace(':id', id).replace(':status', status)}`,
        method: 'put'
    })
}

// 教师获取自己的课程列表
export function getTeacherCourses(params) {
    return request({
        url: `${API_PREFIX}${COURSE_API.GET_ALL}`,
        method: 'get',
        params
    })
}

// 学生获取自己的课程列表
export function getStudentCourses(params) {
    return request({
        url: `${API_PREFIX}${COURSE_API.GET_ALL}`,
        method: 'get',
        params
    })
}

// 获取所有课程 (用于下拉选择)
export function getAllCoursesForSelect(params) {
    return request({
        url: `${API_PREFIX}${COURSE_API.GET_ALL}`,
        method: 'get',
        params
    })
}

// 获取所有课程 (分页列表)
export function getCourseList(params) {
    return request({
        url: `${API_PREFIX}${COURSE_API.GET_ALL}`,
        method: 'get',
        params
    })
} 