import request from '@/utils/request'

// 课程相关的API端点
const API = {
    GET_ALL: '/courses', // 获取所有课程
    GET_BY_ID: (id) => `/courses/${id}`, // 根据ID获取课程
    GET_BY_NO: (courseNo) => `/courses/no/${courseNo}`, // 根据课程编号获取课程
    GET_BY_TYPE: (courseType) => `/courses/type/${courseType}`, // 根据课程类型获取课程
    GET_BY_COLLEGE: (collegeId) => `/courses/college/${collegeId}`, // 根据学院获取课程
    ADD: '/courses', // 添加课程
    UPDATE: (id) => `/courses/${id}`, // 更新课程
    DELETE: (id) => `/courses/${id}`, // 删除课程
    BATCH_DELETE: '/courses/batch', // 批量删除课程
    UPDATE_STATUS: (id, status) => `/courses/${id}/status/${status}` // 更新课程状态
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
        method: 'delete', // 或 'post'，取决于后端实现
        data: ids // 在请求体中发送ID
    });
}

// 更新课程状态
export function updateCourseStatus(id, status) {
    return request({
        url: API.UPDATE_STATUS(id, status),
        method: 'put' // 或 'patch'
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
    const userStore = window._pinia && window._pinia.state.value.user;
    const userId = userStore ? userStore.userInfo.id : null;

    if (!userId) {
        console.warn('获取学生课程: 无法获取用户ID');
        return Promise.reject(new Error('用户未登录或ID不可用'));
    }
    
    return request({
        url: `/course-selections/student/${userId}`, // 使用选课API获取学生已选课程
        method: 'get',
        params
    });
} 