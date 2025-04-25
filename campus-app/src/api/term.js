import request from '@/utils/request';
import {TERM_API} from './api-endpoints'; // 假设 API 端点定义在 api-endpoints.js

/**
 * 获取所有学期列表
 * @param {object} params 查询参数，例如 { sortByCreateDesc: true }
 * @returns Promise
 */
export function getTermList(params) {
    return request({
        url: TERM_API.GET_ALL,
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
        url: TERM_API.GET_CURRENT,
        method: 'get'
    });
}

// 未来可以添加创建、更新、删除学期的 API 函数 