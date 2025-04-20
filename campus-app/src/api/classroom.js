import request from './request'
// import {CLASSROOM_API} from './api-endpoints'

// 获取教室列表（分页）
export function getClassroomList(params) {
    return request({
        url: '/api/classrooms',
        method: 'get',
        params
    })
}

// 获取教室详情
export function getClassroomById(id) {
    return request({
        url: `/api/classrooms/${id}`,
        method: 'get'
    })
}

// 添加教室
export function addClassroom(data) {
    return request({
        url: '/api/classrooms',
        method: 'post',
        data
    })
}

// 更新教室
export function updateClassroom(id, data) {
    return request({
        url: `/api/classrooms/${id}`,
        method: 'put',
        data
    })
}

// 删除教室
export function deleteClassroom(id) {
    return request({
        url: `/api/classrooms/${id}`,
        method: 'delete'
    })
}

// 获取可用教室列表
export function getAvailableClassrooms(params) {
    return request({
        url: '/api/classrooms/available',
        method: 'get',
        params
    })
} 