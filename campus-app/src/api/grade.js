import request from './request'
// 假设在 api-endpoints.js 中定义了 GRADE_API
// import { GRADE_API } from './api-endpoints'

/**
 * 获取学生成绩列表
 * @param {object} params - 查询参数，例如 { term: '2023-2024-1' }
 */
export function getStudentGrades(params) {
    return request({
        // url: GRADE_API.GET_STUDENT_GRADES, // 替换为实际的 API 端点
        url: '/grades/student', // 临时占位符 URL
        method: 'get',
        params
    })
}

// 未来可能需要的其他成绩相关 API
// export function getGradeById(id) { ... }
// export function addGrade(data) { ... }
// export function updateGrade(id, data) { ... }
// export function deleteGrade(id) { ... } 