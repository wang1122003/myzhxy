import request from '@/utils/request'

// 课表相关 API 端点
const API = {
    GET_ALL: '/schedules', // 获取所有课表 (分页, 过滤)
    GET_BY_ID: (id) => `/schedules/${id}`, // 根据 ID 获取课表详情
    ADD: '/schedules', // 添加课表
    UPDATE: (id) => `/schedules/${id}`, // 更新课表
    DELETE: (id) => `/schedules/${id}`, // 删除课表
    BATCH_DELETE: '/schedules/batch', // 批量删除课表
    CHECK_CONFLICT: '/schedules/check-conflict', // 检查排课冲突
    GET_TEACHER_WEEKLY: '/schedules/teacher-weekly', // 获取教师周课表
    GET_STUDENT_WEEKLY: '/schedules/student-weekly', // 获取学生周课表
    GET_CLASSROOM_WEEKLY: '/schedules/classroom-weekly', // 获取教室周课表
    GET_STUDENT_SCHEDULE_BY_USER_ID: '/schedules/student', // 根据用户 ID 获取学生课表 (通常是当前登录学生)
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

// 获取教师课表 (修正：调用分页接口 + 教师相关参数)
export function getTeacherSchedule(params) {
    // 期望 params 中包含 teacherId 或类似过滤条件
    return getSchedulesPage(params);
}

// 获取学生课表 (修正：调用分页接口 + 学生相关参数，或使用专用接口)
export function getStudentSchedule(params) {
    // 如果是获取 *当前* 登录学生的课表，推荐使用 getSchedulesByStudent
    // 如果是管理员获取 *指定* 学生的课表，可以使用 getSchedulesPage 并传入 studentId
    return getSchedulesPage(params); // 或者调用 getSchedulesByStudent()
}

// 获取教室课表 (修正：调用分页接口 + 教室相关参数)
export function getClassroomSchedule(params) {
    // 期望 params 中包含 classroomId 或类似过滤条件
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
        method: 'delete', // 通常批量删除用 DELETE 或 POST
        data: ids // 将 ID 数组放在请求体中
    })
}

// 检查课表冲突
export function checkScheduleConflict(data) {
    return request({
        url: API.CHECK_CONFLICT,
        method: 'post',
        data // 包含要检查的排课信息
    })
}

// 获取教师周课表
export function getTeacherWeeklySchedule(params) {
    // params 可能包含 teacherId, weekStartDate 等
    return request({
        url: API.GET_TEACHER_WEEKLY,
        method: 'get',
        params
    })
}

// 获取学生周课表
export function getStudentWeeklySchedule(params) {
    // params 可能包含 studentId, weekStartDate 等
    return request({
        url: API.GET_STUDENT_WEEKLY,
        method: 'get',
        params
    })
}

// 获取教室周课表
export function getClassroomWeeklySchedule(params) {
    // params 可能包含 classroomId, weekStartDate 等
    return request({
        url: API.GET_CLASSROOM_WEEKLY,
        method: 'get',
        params
    })
}

// 获取某个教师的课表列表 (修正：调用分页接口 + 教师 ID 参数)
export function getSchedulesByTeacher(params) {
    // 确保 params 中包含 teacherId
    return getSchedulesPage(params);
}

// 获取某个学生的课表列表 (非周课表，使用专用接口)
export function getSchedulesByStudent(params) {
    // 适用于获取当前登录学生的课表，或需要特定处理的场景
    return request({
        url: API.GET_STUDENT_SCHEDULE_BY_USER_ID,
        method: 'get',
        params // 可能包含学期等参数
    })
}

// 获取某个课程的排课列表 (修正：调用分页接口 + 课程 ID 参数)
export function getSchedulesByCourse(courseId, params) {
    return getSchedulesPage({...params, courseId});
}

// 获取某个教室的排课列表 (修正：调用分页接口 + 教室 ID 参数)
export function getSchedulesByClassroom(classroomId, params) {
    return getSchedulesPage({...params, classroomId});
} 