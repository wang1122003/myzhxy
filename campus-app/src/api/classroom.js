import request from '../utils/request'
import {CLASSROOM_API} from './api-endpoints' // 引入 API 定义

// 获取教室列表（分页）
export function getClassroomList(params) {
    return request({
        url: CLASSROOM_API.GET_ALL, // 直接使用定义的路径
        method: 'get',
        params
    })
}

// 获取教室详情
export function getClassroomById(id) {
    return request({
        url: CLASSROOM_API.GET_BY_ID.replace(':id', id), // 使用定义的路径
        method: 'get'
    })
}

// 添加教室
export function addClassroom(data) {
    return request({
        url: CLASSROOM_API.ADD, // 使用定义的路径
        method: 'post',
        data
    })
}

// 更新教室
export function updateClassroom(id, data) {
    return request({
        url: CLASSROOM_API.UPDATE.replace(':id', id), // 使用定义的路径
        method: 'put',
        data
    })
}

// 删除教室
export function deleteClassroom(id) {
    return request({
        url: CLASSROOM_API.DELETE.replace(':id', id), // 使用定义的路径
        method: 'delete'
    })
}

// 获取可用教室列表
export function getAvailableClassrooms(params) {
    return request({
        url: CLASSROOM_API.GET_AVAILABLE, // 使用定义的路径
        method: 'get',
        params
    })
}