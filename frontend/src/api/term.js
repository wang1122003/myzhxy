import request from '@/utils/request';

// API Endpoints for Term
const API = {
    GET_ALL: '/term/list', // 获取所有学期列表
    GET_PAGE: '/term/page', // 分页获取学期
    GET_BY_ID: (id) => `/term/${id}`, // 根据ID获取学期
    ADD: '/term', // 添加学期
    UPDATE: '/term', // 更新学期 (ID在请求体中)
    DELETE: (id) => `/term/${id}`, // 删除学期
    SET_CURRENT: (id) => `/term/current/${id}`, // 设置当前学期
    GET_CURRENT: '/term/current' // 获取当前学期
};

/**
 * 获取所有学期列表 (支持分页)
 * @param {object} params 查询参数，例如 { pageNum: 1, pageSize: 10 } or undefined for all
 * @returns Promise
 */
export function getAllTerms(params) {
    let url;
    let method = 'get'; // 默认是 GET

    if (params && (params.pageNum !== undefined || params.pageSize !== undefined)) {
        url = API.GET_PAGE;
    } else {
        // 如果 params 为空或不含分页参数，则获取所有列表
        url = API.GET_ALL;
    }
    return request({
        url: url, // request 工具会自动添加 /api 前缀
        method: method,
        params: params // 将 params 传递给 GET 请求
    });
}

/**
 * 获取指定ID的学期信息
 * @param {number} id 学期ID
 * @returns Promise
 */
export function getTermById(id) {
    return request({
        url: API.GET_BY_ID(id),
        method: 'get'
    });
}


/**
 * 添加新学期
 * @param {object} data 学期数据
 * @returns Promise
 */
export function addTerm(data) {
    return request({
        url: API.ADD,
        method: 'post',
        data
    });
}

/**
 * 更新学期信息
 * @param {object} data 学期数据 (应该包含id)
 * @returns Promise
 */
export function updateTerm(data) { // id 通常在 data 对象中
    return request({
        url: API.UPDATE, // 后端 updateTerm(@RequestBody Term term)
        method: 'put',
        data
    });
}

/**
 * 删除学期
 * @param {number} id 学期ID
 * @returns Promise
 */
export function deleteTermById(id) {
    return request({
        url: API.DELETE(id),
        method: 'delete'
    });
}

/**
 * 设置当前学期
 * @param {number} id 学期ID
 * @returns Promise
 */
export function setCurrentTerm(id) {
    return request({
        url: API.SET_CURRENT(id),
        method: 'put'
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