/**
 * API 工具函数集
 * 用于统一处理API请求和响应
 */
import request from '@/utils/request';
import {ElMessage} from 'element-plus';
import {formatDateTime} from '@/utils/formatters';

/**
 * 通用列表查询方法
 * @param {string} url - API 路径
 * @param {Object} params - 查询参数，包含分页、关键字等
 * @param {Object} options - 附加选项
 * @returns {Promise} - 返回处理后的数据
 */
export function fetchList(url, params = {}, options = {}) {
    return request({
        url,
        method: 'get',
        params
    }).then(res => {
        if (res.code === 200) {
            const data = res.data || {};

            // 处理MyBatisPlus分页结果
            if (data.records && Array.isArray(data.records)) {
                return {
                    list: data.records,
                    total: data.total || 0,
                    pages: data.pages || 0,
                    currentPage: data.current || 1,
                    pageSize: data.size || 10
                };
            }

            // 处理自定义Map返回结果
            if (data.list && Array.isArray(data.list)) {
                return data;
            }

            // 直接返回数组结果
            if (Array.isArray(data)) {
                return {
                    list: data,
                    total: data.length,
                    currentPage: 1
                };
            }

            return {list: [], total: 0};
        }

        // 处理错误情况
        if (options.showError !== false) {
            ElMessage.error(res.message || '获取数据失败');
        }

        return {list: [], total: 0};
    }).catch(error => {
        console.error('获取列表数据失败:', error);
        if (options.showError !== false) {
            ElMessage.error('获取数据失败，请检查网络连接');
        }
        return {list: [], total: 0};
    });
}

/**
 * 通用详情获取方法
 * @param {string} url - API 路径
 * @param {string|number} id - 记录ID
 * @param {Object} options - 附加选项
 * @returns {Promise} - 返回处理后的详情数据
 */
export function fetchDetail(url, id, options = {}) {
    return request({
        url: `${url}/${id}`,
        method: 'get'
    }).then(res => {
        if (res.code === 200) {
            return res.data;
        }

        if (options.showError !== false) {
            ElMessage.error(res.message || '获取详情失败');
        }

        return null;
    }).catch(error => {
        console.error('获取详情数据失败:', error);
        if (options.showError !== false) {
            ElMessage.error('获取详情失败，请检查网络连接');
        }
        return null;
    });
}

/**
 * 通用添加方法
 * @param {string} url - API 路径
 * @param {Object} data - 提交数据
 * @param {Object} options - 附加选项
 * @returns {Promise} - 返回处理结果
 */
export function createItem(url, data, options = {}) {
    const successMsg = options.successMsg || '添加成功';
    const errorMsg = options.errorMsg || '添加失败';

    return request({
        url,
        method: 'post',
        data
    }).then(res => {
        if (res.code === 200) {
            if (options.showSuccess !== false) {
                ElMessage.success(successMsg);
            }
            return {success: true, data: res.data};
        }

        if (options.showError !== false) {
            ElMessage.error(res.message || errorMsg);
        }

        return {success: false, message: res.message};
    }).catch(error => {
        console.error('添加数据失败:', error);
        if (options.showError !== false) {
            ElMessage.error(error.message || errorMsg);
        }
        return {success: false, message: error.message};
    });
}

/**
 * 通用更新方法
 * @param {string} url - API 路径
 * @param {string|number} id - 记录ID
 * @param {Object} data - 更新数据
 * @param {Object} options - 附加选项
 * @returns {Promise} - 返回处理结果
 */
export function updateItem(url, id, data, options = {}) {
    const successMsg = options.successMsg || '更新成功';
    const errorMsg = options.errorMsg || '更新失败';

    return request({
        url: `${url}/${id}`,
        method: 'put',
        data
    }).then(res => {
        if (res.code === 200) {
            if (options.showSuccess !== false) {
                ElMessage.success(successMsg);
            }
            return {success: true, data: res.data};
        }

        if (options.showError !== false) {
            ElMessage.error(res.message || errorMsg);
        }

        return {success: false, message: res.message};
    }).catch(error => {
        console.error('更新数据失败:', error);
        if (options.showError !== false) {
            ElMessage.error(error.message || errorMsg);
        }
        return {success: false, message: error.message};
    });
}

/**
 * 通用删除方法
 * @param {string} url - API 路径
 * @param {string|number} id - 记录ID
 * @param {Object} options - 附加选项
 * @returns {Promise} - 返回处理结果
 */
export function deleteItem(url, id, options = {}) {
    const successMsg = options.successMsg || '删除成功';
    const errorMsg = options.errorMsg || '删除失败';

    return request({
        url: `${url}/${id}`,
        method: 'delete'
    }).then(res => {
        if (res.code === 200) {
            if (options.showSuccess !== false) {
                ElMessage.success(successMsg);
            }
            return {success: true};
        }

        if (options.showError !== false) {
            ElMessage.error(res.message || errorMsg);
        }

        return {success: false, message: res.message};
    }).catch(error => {
        console.error('删除数据失败:', error);
        if (options.showError !== false) {
            ElMessage.error(error.message || errorMsg);
        }
        return {success: false, message: error.message};
    });
}

/**
 * 通用批量删除方法
 * @param {string} url - API 路径
 * @param {Array} ids - ID数组
 * @param {Object} options - 附加选项
 * @returns {Promise} - 返回处理结果
 */
export function batchDelete(url, ids, options = {}) {
    const successMsg = options.successMsg || '批量删除成功';
    const errorMsg = options.errorMsg || '批量删除失败';

    return request({
        url: `${url}/batch`,
        method: 'delete',
        data: ids
    }).then(res => {
        if (res.code === 200) {
            if (options.showSuccess !== false) {
                ElMessage.success(successMsg);
            }
            return {success: true};
        }

        if (options.showError !== false) {
            ElMessage.error(res.message || errorMsg);
        }

        return {success: false, message: res.message};
    }).catch(error => {
        console.error('批量删除数据失败:', error);
        if (options.showError !== false) {
            ElMessage.error(error.message || errorMsg);
        }
        return {success: false, message: error.message};
    });
}

/**
 * 处理响应结果
 * @param {Object} res - 响应对象
 * @param {Function} successCallback - 成功回调
 * @param {Function} errorCallback - 错误回调
 * @returns {boolean} - 是否成功
 */
export function handleResponse(res, successCallback, errorCallback) {
    if (res.code === 200) {
        if (typeof successCallback === 'function') {
            successCallback(res.data);
        }
        return true;
    } else {
        if (typeof errorCallback === 'function') {
            errorCallback(res.message);
        }
        return false;
    }
}

/**
 * 格式化日期时间（为了保持兼容性，保留此函数）
 * @param {string|Date} time - 时间
 * @param {string} format - 格式
 * @returns {string} - 格式化的日期时间字符串
 */
export function formatTime(time, format = 'datetime') {
    return formatDateTime(time, format);
}

export default {
    fetchList,
    fetchDetail,
    createItem,
    updateItem,
    deleteItem,
    batchDelete,
    handleResponse,
    formatTime
}; 