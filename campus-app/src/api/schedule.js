import request from '../utils/request'
import { SCHEDULE_API } from './api-endpoints' // 引入 API 定义

// 获取所有课表
export function getAllSchedules(params) {
    return request({
        url: SCHEDULE_API.GET_ALL,
        method: 'get',
        params
    })
}

// 获取课表详情
export function getScheduleById(id) {
    return request({
        url: SCHEDULE_API.GET_BY_ID.replace(':id', id),
        method: 'get'
    })
}

// 添加课表
export function addSchedule(data) {
    return request({
        url: SCHEDULE_API.ADD,
        method: 'post',
        data
    })
}

// 更新课表
export function updateSchedule(id, data) {
    return request({
        url: SCHEDULE_API.UPDATE.replace(':id', id),
        method: 'put',
        data
    })
}

// 删除课表
export function deleteSchedule(id) {
    return request({
        url: SCHEDULE_API.DELETE.replace(':id', id),
        method: 'delete'
    })
}

// 获取课表列表（分页）
export function getScheduleList(params) {
    return request({
        url: SCHEDULE_API.GET_ALL,
        method: 'get',
        params
    })
}

// 获取教师课表
export function getTeacherSchedule(params) {
    return request({
        url: SCHEDULE_API.GET_TEACHER,
        method: 'get',
        params
    })
}

// 获取学生课表
export function getStudentSchedule(params) {
    return request({
        url: SCHEDULE_API.GET_STUDENT,
        method: 'get',
        params
    })
}

// 获取教室课表
export function getClassroomSchedule(params) {
    return request({
        url: SCHEDULE_API.GET_CLASSROOM,
        method: 'get',
        params
    })
}

// 管理员自动生成课表
export function generateSchedule(data) {
    return request({
        url: SCHEDULE_API.GENERATE,
        method: 'post',
        data
    })
}

// 管理员创建课表
export function createSchedule(data) {
    return request({
        url: SCHEDULE_API.ADD,
        method: 'post',
        data
    })
}

// 批量生成课表
export function batchGenerateSchedule(data) {
    return request({
        url: SCHEDULE_API.BATCH_GENERATE,
        method: 'post',
        data
    })
}

// 批量删除课表
export function batchDeleteSchedule(ids) {
    return request({
        url: SCHEDULE_API.BATCH_DELETE,
        method: 'delete',
        data: {ids}
    })
}

// 检查课表冲突
export function checkScheduleConflict(data) {
    return request({
        url: SCHEDULE_API.CHECK_CONFLICT,
        method: 'post',
        data
    })
}

// 获取教师周课表
export function getTeacherWeeklySchedule(params) {
    return request({
        url: SCHEDULE_API.GET_TEACHER_WEEKLY,
        method: 'get',
        params
    })
}

// 获取学生周课表
export function getStudentWeeklySchedule(params) {
    return request({
        url: SCHEDULE_API.GET_STUDENT_WEEKLY,
        method: 'get',
        params
    })
}

// 获取教室周课表
export function getClassroomWeeklySchedule(params) {
    return request({
        url: SCHEDULE_API.GET_CLASSROOM_WEEKLY,
        method: 'get',
        params
    })
} 