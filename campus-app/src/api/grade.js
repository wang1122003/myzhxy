import request from '../utils/request'
import {GRADE_API} from './api-endpoints'

/**
 * 获取当前登录学生的成绩列表
 * @param {object} params - 查询参数，例如 { semester: '2023-2024-1' }
 */
export function getMyGrades(params) {
    return request({
        url: GRADE_API.GET_MY_SCORES,
        method: 'get',
        params
    })
}

/**
 * 获取指定课程的所有学生成绩列表 (教师用)
 * @param {number} courseId - 课程ID
 * @param {object} params - 查询参数，例如 { page: 1, size: 10 }
 */
export function getCourseScores(courseId, params) {
    return request({
        url: GRADE_API.GET_COURSE_SCORES.replace(':courseId', courseId),
        method: 'get',
        params
    })
}

/**
 * 记录或更新单个学生的成绩
 * @param {object} data - 成绩数据，至少包含 studentId, courseId, score。 如果包含 id 则为更新，否则为新增。
 */
export function recordStudentScore(data) {
    const method = data.id ? 'put' : 'post';
    const url = data.id ? `${GRADE_API.RECORD_SCORE}/${data.id}` : GRADE_API.RECORD_SCORE;
    return request({
        url: url,
        method: method,
        data
    })
}

/**
 * 批量删除成绩
 * @param {Array<number>} ids - 要删除的成绩ID数组
 */
export function batchDeleteScores(ids) {
    return request({
        url: GRADE_API.BATCH_DELETE,
        method: 'delete',
        data: ids // Pass IDs in the request body for DELETE
    })
}

/**
 * 导出成绩
 * @param {number} courseId - 课程ID 
 */
export function exportGrades(courseId) {
    return request({
        url: GRADE_API.EXPORT.replace(':courseId', courseId),
        method: 'get',
        responseType: 'blob'
    })
}

/**
 * 导入成绩
 * @param {FormData} formData - 包含Excel文件的表单数据
 */
export function importGrades(formData) {
    return request({
        url: GRADE_API.IMPORT,
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * 获取课程成绩统计信息
 * @param {number} courseId - 课程ID
 */
export function getCourseScoreStats(courseId) {
    return request({
        url: GRADE_API.GET_COURSE_STATS.replace(':courseId', courseId),
        method: 'get'
    });
}

// --- 移除或注释掉不再使用的旧 API 函数 ---
/*
// 获取课程学生成绩列表 (旧，使用 getCourseScores 替代)
export function getCourseStudents(params) {
    return request({
        url: GRADE_API.GET_COURSE_STUDENTS, // Obsolete endpoint
        method: 'get',
        params
    })
}

// 保存学生成绩 (旧，使用 recordStudentScore 替代)
export function saveStudentGrades(data) {
    return request({
        url: GRADE_API.SAVE_BATCH, // Obsolete endpoint
        method: 'post',
        data
    })
}
*/

// --- 保留可能仍然需要的 API (例如统计)

// --- 移除或重构教师评分项相关 API (与 Grade.vue 相关) ---
/*
export function getCourseGradeItems(courseId) { ... }
export function createGradeItem(data) { ... }
export function updateGradeItem(itemId, data) { ... }
export function deleteGradeItem(itemId) { ... }
export function submitStudentGrades(data) { ... } // Replaced by recordStudentScore
*/ 