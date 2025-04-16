import request from '@/api/request'

// 获取所有课表
export function getAllSchedules(params) {
    return request({
        url: '/schedules',
        method: 'get',
        params
    })
}

// 获取课表详情
export function getScheduleById(id) {
    return request({
        url: `/schedules/${id}`,
        method: 'get'
    })
}

// 添加课表
export function addSchedule(data) {
    return request({
        url: '/schedules',
        method: 'post',
        data
    })
}

// 更新课表
export function updateSchedule(id, data) {
    return request({
        url: `/schedules/${id}`,
        method: 'put',
        data
    })
}

// 删除课表
export function deleteSchedule(id) {
    return request({
        url: `/schedules/${id}`,
        method: 'delete'
    })
}

// 获取课表列表（分页）
export function getScheduleList(params) {
    return request({
        url: '/schedules',
        method: 'get',
        params
    })
}

// 获取教师课表
export function getTeacherSchedule(params) {
    return request({
        url: '/schedules/teacher',
        method: 'get',
        params
    })
}

// 获取学生课表
export function getStudentSchedule(params) {
    return request({
        url: '/schedules/student',
        method: 'get',
        params
    })
}

// 获取教室课表
export function getClassroomSchedule(params) {
    return request({
        url: '/schedules/classroom',
        method: 'get',
        params
    })
}

// 管理员自动生成课表
export function generateSchedule(data) {
    return request({
        url: '/admin/schedule/generate',
        method: 'post',
        data
    })
}

// 管理员创建课表
export function createSchedule(data) {
    return request({
        url: '/schedules',
        method: 'post',
        data
    })
}

// 批量生成课表
export function batchGenerateSchedule(data) {
    return request({
        url: '/schedules/batch-generate',
        method: 'post',
        data
    })
}

// 批量删除课表
export function batchDeleteSchedule(ids) {
    return request({
        url: '/schedules/batch',
        method: 'delete',
        data: {ids}
    })
} 