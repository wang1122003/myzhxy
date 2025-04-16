import request from './request'
import {CLASSROOM_API} from './api-endpoints'

// 获取所有教室
export function getAllClassrooms(params) {
    return request({
        url: CLASSROOM_API.GET_ALL,
        method: 'get',
        params
    })
}

// 获取教室详情
export function getClassroomById(id) {
    return request({
        url: CLASSROOM_API.GET_BY_ID.replace(':id', id),
        method: 'get'
    })
}

// 添加教室
export function addClassroom(data) {
    return request({
        url: CLASSROOM_API.ADD,
        method: 'post',
        data
    })
}

// 更新教室
export function updateClassroom(id, data) {
    return request({
        url: CLASSROOM_API.UPDATE.replace(':id', id),
        method: 'put',
        data
    })
}

// 删除教室
export function deleteClassroom(id) {
    return request({
        url: CLASSROOM_API.DELETE.replace(':id', id),
        method: 'delete'
    })
}

// 获取教室列表（分页）
export function getClassroomList(params) {
    return request({
        url: CLASSROOM_API.GET_LIST,
        method: 'get',
        params
    })
}

// 获取可用教室列表
export function getAvailableClassrooms(params) {
    return request({
        url: CLASSROOM_API.GET_AVAILABLE,
        method: 'get',
        params
    })
} 