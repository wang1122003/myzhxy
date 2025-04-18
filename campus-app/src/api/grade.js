import request from './request'
// 假设在 api-endpoints.js 中定义了 GRADE_API
import {GRADE_API} from './api-endpoints'

/**
 * 获取学生成绩列表
 * @param {object} params - 查询参数，例如 { term: '2023-2024-1' }
 */
export function getStudentGrades(params) {
    return request({
        url: GRADE_API.GET_STUDENT_GRADES,
        method: 'get',
        params
    })
}

// 获取课程学生成绩列表
export function getCourseStudents(params) {
    return request({
        url: GRADE_API.GET_COURSE_STUDENTS,
        method: 'get',
        params
    })
}

// 保存学生成绩
export function saveStudentGrades(data) {
    return request({
        url: GRADE_API.SAVE_BATCH,
        method: 'post',
        data
    })
}

// 导出成绩
export function exportGrades(courseId) {
    return request({
        url: GRADE_API.EXPORT.replace(':courseId', courseId),
        method: 'get',
        responseType: 'blob'
    })
}

// 导入成绩
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

// 未来可能需要的其他成绩相关 API
// export function getGradeById(id) { ... }
// export function addGrade(data) { ... }
// export function updateGrade(id, data) { ... }
// export function deleteGrade(id) { ... } 