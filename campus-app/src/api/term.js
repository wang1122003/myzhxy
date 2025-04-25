import request from '@/utils/request';

// API Endpoints for Term
const API = {
    GET_ALL: '/terms',
    GET_CURRENT: '/terms/current'
    // Add other term-related endpoints if they exist in the controller
    // e.g., ADD: '/terms', UPDATE: (id) => `/terms/${id}`, DELETE: (id) => `/terms/${id}`
};

/**
 * 获取所有学期列表
 * @param {object} params 查询参数，例如 { sortByCreateDesc: true }
 * @returns Promise
 */
export function getAllTerms(params) {
    return request({
        url: API.GET_ALL,
        method: 'get',
        params
    });
}

/**
 * 获取当前学期
 * @returns Promise
 */
export function getCurrentTerm() {
    return request({
        url: API.GET_CURRENT,
        method: 'get'
    });
}

// 未来可以添加创建、更新、删除学期的 API 函数 