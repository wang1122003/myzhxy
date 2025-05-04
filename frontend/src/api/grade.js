import request from '@/utils/request'

// API Endpoints for Grade/Score Management
const API = {
    GET_MY_SCORES: '/scores/me',
    GET_COURSE_SCORES: (courseId) => `/scores/course/${courseId}`,
    GET_STUDENT_COURSE_SCORE: (studentId, courseId) => `/scores/student/${studentId}/course/${courseId}`,
    RECORD_SCORE: '/scores', // POST for new, PUT for update
    BATCH_DELETE: '/scores/batch',
    GET_STATS: '/scores/stats',
    GET_COURSE_STATS: (courseId) => `/scores/stats/course/${courseId}`,
    EXPORT: (courseId) => `/scores/export/${courseId}`,
    IMPORT: '/scores/import' // Assumes POST method
};

/**
 * 获取当前登录学生的成绩列表
 * @param {object} params - 查询参数，例如 { page: 1, size: 10 }
 */
export function getMyScores(params) {
    console.log('调用getMyScores API，参数:', params);
    return request({
        url: API.GET_MY_SCORES,
        method: 'get',
        params
    }).then(response => {
        console.log('成绩API返回数据:', response);
        return response;
    }).catch(error => {
        console.error('成绩API请求失败:', error);
        throw error;
    });
}

/**
 * 获取指定课程的所有学生成绩列表 (教师用)
 * @param {number} courseId - 课程ID
 * @param {object} params - 查询参数，例如 { page: 1, size: 10 }
 */
export function getCourseScores(courseId, params) {
    return request({
        url: API.GET_COURSE_SCORES(courseId),
        method: 'get',
        params // e.g., studentName filter or pagination?
    });
}

/**
 * 获取特定学生特定课程的成绩记录
 * @param {number} studentId - 学生ID
 * @param {number} courseId - 课程ID
 */
export function getStudentCourseScore(studentId, courseId) {
    return request({
        url: API.GET_STUDENT_COURSE_SCORE(studentId, courseId),
        method: 'get'
    });
}

/**
 * 记录或更新单个学生的成绩
 * @param {object} data - 成绩数据，至少包含 studentId, courseId, score。 如果包含 id 则为更新，否则为新增。
 */
export function recordScore(data) {
    // 后端使用 POST 同时处理创建和更新
    return request({
        url: API.RECORD_SCORE,
        method: 'post', // Defaulting to POST, use PUT for updates if needed
        data
    });
}

/**
 * 批量删除成绩
 * @param {Array<number>} ids - 要删除的成绩ID数组
 */
export function batchDeleteScores(ids) {
    return request({
        url: API.BATCH_DELETE,
        method: 'delete', // Or POST
        data: ids // Send IDs in request body
    });
}

/**
 * 获取成绩统计信息 (总体) - (后端缺失)
 * @param {object} params - 查询参数，例如 { termId: '2023-2024-1' }
 */
// export function getScoreStats(params) {
//     return request({
//         url: API.GET_STATS,
//         method: 'get',
//         params
//     });
// }

/**
 * 获取某门课程的成绩统计信息
 * @param {number} courseId - 课程ID
 * @param {object} params - 查询参数，例如 { studentName: 'John Doe' }
 */
export function getCourseScoreStats(courseId, params) {
    return request({
        url: API.GET_COURSE_STATS(courseId),
        method: 'get',
        params
    });
}

/**
 * 导出课程成绩 - (后端缺失)
 * @param {number} courseId - 课程ID
 * @param {object} params - 查询参数，例如 { studentName: 'John Doe' }
 */
// export function exportCourseScores(courseId, params) {
//     // Similar to file download, might get URL or trigger blob download
//     return request({
//         url: API.EXPORT(courseId),
//         method: 'get',
//         params,
//         responseType: 'blob' // Assuming backend sends file directly
//     });
//     // Add blob handling similar to file download if needed
// }

/**
 * 导入成绩 - (后端缺失)
 * @param {File} file - 要导入的成绩文件
 * @param {number} courseId - 课程ID
 */
// export function importScores(file, courseId) { // courseId might be in query params or formData
//     const formData = new FormData();
//     formData.append('file', file);
//     // Add courseId to formData if needed by backend
//     // formData.append('courseId', courseId);

//     return request({
//         url: API.IMPORT,
//         method: 'post',
//         params: { courseId }, // Example: send courseId as query param
//         data: formData,
//         headers: { 'Content-Type': 'multipart/form-data' }
//     });
// }

/**
 * 获取教师课程下的学生成绩列表
 * @param {Object} params 查询参数，包括courseId和termId
 * @returns {Promise}
 */
export function getCourseStudents(params) {
    // 首先确保courseId是有效值
    if (!params.courseId) {
        console.error('课程ID不能为空:', params);
        return Promise.reject(new Error('课程ID不能为空'));
    }

    const courseId = params.courseId;

    // 根据是否有termId决定使用哪个API
    const url = params.termId
        ? `/scores/course/${courseId}/term`
        : `/scores/course/${courseId}`;

    console.log('请求课程学生成绩，URL:', url, '参数:', params);

    return request({
        url,
        method: 'get',
        params: params.termId ? {termInfo: params.termId} : {}
    }).then(response => {
        // 记录原始响应以便调试
        console.log('成绩API原始响应:', response);

        // 直接返回响应，让组件处理数据结构
        return response;
    }).catch(error => {
        console.error('获取课程学生成绩失败:', error);
        throw error;
    });
}

/**
 * 保存学生成绩
 * @param {Array} data 成绩数据数组
 * @returns {Promise}
 */
export function saveStudentGrades(data) {
    // 如果是数组，则需要循环调用单个成绩保存接口
    if (Array.isArray(data)) {
        // 创建一个包含所有保存请求的Promise数组
        const savePromises = data.map(scoreItem => {
            return request({
                url: '/scores',
                method: 'post',
                data: scoreItem
            });
        });

        // 返回所有请求的Promise.all结果
        return Promise.all(savePromises);
    } else {
        // 单个成绩保存
        return request({
            url: '/scores',
            method: 'post',
            data
        });
    }
}

/**
 * 导出成绩单
 * @param {Object} params 导出参数
 * @returns {Promise}
 */
export function exportGradeSheet(params) {
    return request({
        url: '/scores/export',
        method: 'get',
        params,
        responseType: 'blob' // 导出文件需要设置responseType为blob
    });
}

/**
 * 获取学生成绩统计数据
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getStudentGradeStats(params) {
    return request({
        url: '/scores/stats/student',
        method: 'get',
        params
    });
}

/**
 * 获取课程成绩统计数据
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getCourseGradeStats(params) {
    return request({
        url: '/scores/stats/course',
        method: 'get',
        params
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

// --- 移除或重构教师评分项相关 API (与 MyGrades.vue 相关) ---
/*
export function getCourseGradeItems(courseId) { ... }
export function createGradeItem(data) { ... }
export function updateGradeItem(itemId, data) { ... }
export function deleteGradeItem(itemId) { ... }
export function submitStudentGrades(data) { ... } // Replaced by recordStudentScore
*/ 