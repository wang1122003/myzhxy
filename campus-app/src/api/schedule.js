import request from '@/utils/request'

// API Endpoints for Schedule
const API = {
    GET_ALL: '/schedules',
    GET_BY_ID: (id) => `/schedules/${id}`,
    ADD: '/schedules',
    UPDATE: (id) => `/schedules/${id}`,
    DELETE: (id) => `/schedules/${id}`,
    BATCH_DELETE: '/schedules/batch',
    CHECK_CONFLICT: '/schedules/check-conflict',
    GET_TEACHER_WEEKLY: '/schedules/teacher-weekly',
    GET_STUDENT_WEEKLY: '/schedules/student-weekly',
    GET_CLASSROOM_WEEKLY: '/schedules/classroom-weekly',
    GET_STUDENT_SCHEDULE_BY_USER_ID: '/schedules/student',
};

// 获取课表列表 (分页和过滤)
export function getSchedulesPage(params) {
    return request({
        url: API.GET_ALL,
        method: 'get',
        params
    })
}

// 获取课表详情
export function getScheduleById(id) {
    return request({
        url: API.GET_BY_ID(id),
        method: 'get'
    })
}

// 添加课表
export function addSchedule(data) {
    return request({
        url: API.ADD,
        method: 'post',
        data
    })
}

// 更新课表
export function updateSchedule(id, data) {
    return request({
        url: API.UPDATE(id),
        method: 'put',
        data
    })
}

// 删除课表
export function deleteSchedule(id) {
    return request({
        url: API.DELETE(id),
        method: 'delete'
    })
}

// 获取教师课表 (修正：调用分页接口 + 参数)
export function getTeacherSchedule(params) {
    return getSchedulesPage(params);
}

// 获取学生课表 (修正：调用分页接口 + 参数 or /student)
export function getStudentSchedule(params) {
    return getSchedulesPage(params);
}

// 获取教室课表 (修正：调用分页接口 + 参数)
export function getClassroomSchedule(params) {
    return getSchedulesPage(params);
}

// 管理员创建课表 (复用 addSchedule)
export function createSchedule(data) {
    return addSchedule(data);
}

// 批量删除课表
export function batchDeleteSchedules(ids) {
    return request({
        url: API.BATCH_DELETE,
        method: 'delete',
        data: ids
    })
}

// 检查课表冲突
export function checkScheduleConflict(data) {
    return request({
        url: API.CHECK_CONFLICT,
        method: 'post',
        data
    })
}

// 获取教师周课表
export function getTeacherWeeklySchedule(params) {
    return request({
        url: API.GET_TEACHER_WEEKLY,
        method: 'get',
        params
    })
}

// 获取学生周课表
export function getStudentWeeklySchedule(params) {
    return request({
        url: API.GET_STUDENT_WEEKLY,
        method: 'get',
        params
    })
}

// 获取教室周课表
export function getClassroomWeeklySchedule(params) {
    return request({
        url: API.GET_CLASSROOM_WEEKLY,
        method: 'get',
        params
    })
}

// 获取某个教师的课表列表 (修正：调用分页接口 + 参数)
export function getSchedulesByTeacher(params) {
    return getSchedulesPage(params);
}

// 获取某个学生的课表列表 (非周课表，使用专用接口)
export function getSchedulesByStudent(params) {
    return request({
        url: API.GET_STUDENT_SCHEDULE_BY_USER_ID,
        method: 'get',
        params
    })
}

// 获取某个课程的排课列表 (修正：调用分页接口 + 参数)
export function getSchedulesByCourse(courseId, params) {
    return getSchedulesPage({...params, courseId});
}

// 获取某个教室的排课列表 (修正：调用分页接口 + 参数)
export function getSchedulesByClassroom(classroomId, params) {
    return getSchedulesPage({...params, classroomId});
} 