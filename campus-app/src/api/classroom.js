import request from '@/utils/request'

// 教室相关的 API 端点
const API = {
    GET_PAGE: '/classrooms', // 获取教室列表 (分页)
    GET_ALL: '/classrooms/all', // 获取所有教室 (非分页)
    GET_BY_ID: (id) => `/classrooms/${id}`, // 根据 ID 获取教室详情
    ADD: '/classrooms', // 添加教室
    UPDATE: (id) => `/classrooms/${id}`, // 更新教室
    DELETE: (id) => `/classrooms/${id}`, // 删除教室
    BATCH_DELETE: '/classrooms/batch', // 批量删除教室
    UPDATE_STATUS: (id, status) => `/classrooms/${id}/status/${status}`, // 更新教室状态
    GET_AVAILABLE: '/classrooms/available', // 获取可用教室
    // GET_BY_BUILDING: (building) => `/classrooms/building/${building}`, // 原路径，与后端不符
    // GET_BY_TYPE: (roomType) => `/classrooms/type/${roomType}`, // 后端缺失
    // GET_BY_CAPACITY: '/classrooms/capacity' // 后端缺失
};

// 获取教室列表 (分页)
export function getClassroomsPage(params) {
    return request({
        url: API.GET_PAGE,
        method: 'get',
        params
    });
}

// 获取所有教室 (非分页)
export function getAllClassroomsList(params) {
    return request({
        url: API.GET_ALL,
        method: 'get',
        params // 后端 /all 不接受分页参数，params 可能多余
    });
}

// 根据ID获取教室详情
export function getClassroomById(id) {
    return request({
        url: API.GET_BY_ID(id),
        method: 'get'
    });
}

// 添加教室
export function addClassroom(data) {
    return request({
        url: API.ADD,
        method: 'post',
        data
    });
}

// 更新教室
export function updateClassroom(id, data) {
    return request({
        url: API.UPDATE(id),
        method: 'put',
        data
    });
}

// 删除教室
export function deleteClassroom(id) {
    return request({
        url: API.DELETE(id),
        method: 'delete'
    });
}

// 批量删除教室
export function batchDeleteClassrooms(ids) {
    return request({
        url: API.BATCH_DELETE,
        method: 'delete', // 或 POST
        data: ids // 在请求体中发送 ID 列表
    });
}

// 更新教室状态
export function updateClassroomStatus(id, status) {
    return request({
        url: API.UPDATE_STATUS(id, status),
        method: 'put' // 或 PATCH
    });
}

// 获取可用教室 (可能需要时间段等参数)
export function getAvailableClassrooms(params) {
    return request({
        url: API.GET_AVAILABLE,
        method: 'get',
        params
    });
}

// 根据教学楼获取教室 (修正：使用分页接口+参数)
export function getClassroomsByBuilding(building, params) {
    return request({
        url: API.GET_PAGE, // 调用分页接口
        method: 'get',
        params: {...params, building} // 添加 building 参数
    });
}

// 根据类型获取教室 (修正：使用分页接口+参数)
export function getClassroomsByType(roomType, params) {
    // console.warn('Backend API for getClassroomsByType is missing.');
    return getClassroomsPage({...params, roomType}); // 调用分页接口并添加 roomType 参数
    // return request({
    //     url: API.GET_BY_TYPE(roomType),
    //     method: 'get',
    //     params
    // });
    // return Promise.reject('Backend API missing');
}

// 根据容量获取教室 (后端缺失此接口)
export function getClassroomsByCapacity(params) { // 参数应包含最小/最大容量
    console.warn('根据容量获取教室的后端 API 缺失。');
    // return request({
    //     url: API.GET_BY_CAPACITY,
    //     method: 'get',
    //     params
    // });
    return Promise.reject('后端 API 缺失');
}