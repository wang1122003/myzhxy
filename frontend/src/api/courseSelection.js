import request from '@/utils/request'

// API Endpoints for Course Selection
const API = {
    SELECT: '/course-selections/select',
    DROP: '/course-selections/drop',
    GET_BY_STUDENT: (userId) => `/course-selections/student/${userId}`,
    GET_BY_ID: (id) => `/course-selections/${id}`,
    PAGE: '/course-selections/page'
};

/**
 * 学生选课
 * @param {object} data - 包含 userId, courseId, termInfo
 */
export function selectCourse(data) {
    // 确保必要的参数存在
    if (!data.userId || !data.courseId || !data.termInfo) {
        throw new Error('学生ID、课程ID和学期信息不能为空');
    }
    
    return request({
        url: API.SELECT,
        method: 'post',
        data
    });
}

/**
 * 学生退选
 * @param {object} params - 包含 userId, courseId, termInfo
 */
export function dropCourse(params) {
    return request({
        url: API.DROP,
        method: 'post', // 后端是 @PostMapping + @RequestParam
        params // 使用 params 匹配 @RequestParam
    });
}

/**
 * 获取学生的选课列表
 * @param {number} userId - 学生ID
 */
export function getStudentSelections(userId) {
    return request({
        url: API.GET_BY_STUDENT(userId),
        method: 'get'
    });
}

/**
 * 获取选课记录详情
 * @param {number} id - 选课记录ID
 */
export function getSelectionById(id) {
    return request({
        url: API.GET_BY_ID(id),
        method: 'get'
    });
}

/**
 * 分页查询选课记录 (管理员用)
 * @param {object} params - 查询参数
 */
export function getSelectionsPage(params) {
    return request({
        url: API.PAGE,
        method: 'get',
        params
    });
} 