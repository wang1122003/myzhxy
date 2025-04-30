/**
 * 通用格式化工具
 */

/**
 * 格式化日期时间
 * @param {string|Date} time - 时间
 * @param {string} format - 格式类型：'datetime'完整日期时间, 'date'仅日期, 'time'仅时间
 * @returns {string} 格式化后的日期字符串
 */
export function formatDateTime(time, format = 'datetime') {
    if (!time) return '-';
    try {
        const date = new Date(time);

        const options = {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour12: false
        };

        if (format === 'datetime' || format === 'full') {
            Object.assign(options, {
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        } else if (format === 'time') {
            return date.toLocaleTimeString('zh-CN', {
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                hour12: false
            });
        }

        return date.toLocaleString('zh-CN', options);
    } catch (e) {
        return time;
    }
}

/**
 * 格式化用户类型
 * @param {string} userType - 用户类型枚举值
 * @returns {string} 格式化后的用户类型文本
 */
export function formatUserType(userType) {
    const typeMap = {
        'ADMIN': '管理员',
        'TEACHER': '教师',
        'STUDENT': '学生'
    };
    return typeMap[userType] || userType;
}

/**
 * 获取用户类型对应的标签类型
 * @param {string} userType - 用户类型枚举值
 * @returns {string} 对应的标签类型
 */
export function getUserTypeTagType(userType) {
    const typeMap = {
        'ADMIN': 'danger',
        'TEACHER': 'warning',
        'STUDENT': 'success'
    };
    return typeMap[userType] || 'info';
}

/**
 * 格式化活动状态
 * @param {number|string} status - 活动状态值
 * @returns {string} 格式化后的状态文本
 */
export function formatActivityStatus(status) {
    const statusMap = {
        '0': '已取消',
        '1': '报名中',
        '2': '进行中',
        '3': '已结束'
    };
    return statusMap[status] !== undefined ? statusMap[status] : '未知';
}

/**
 * 获取活动状态对应的标签类型
 * @param {number|string} status - 活动状态值
 * @returns {string} 对应的标签类型
 */
export function getActivityStatusTagType(status) {
    const typeMap = {
        '0': 'danger',  // 已取消
        '1': 'success', // 报名中
        '2': 'warning', // 进行中
        '3': 'info'     // 已结束
    };
    return typeMap[status] || 'info';
}

/**
 * 格式化课程状态
 * @param {string} status - 课程状态
 * @returns {string} 格式化后的状态文本
 */
export function formatCourseStatus(status) {
    const statusMap = {
        'ACTIVE': '进行中',
        'PENDING': '未开始',
        'FINISHED': '已结束',
        'CANCELLED': '已取消'
    };
    return statusMap[status] || status;
}

/**
 * 格式化星期几
 * @param {number} weekday - 星期几(1-7)
 * @returns {string} 格式化后的星期几
 */
export function formatWeekday(weekday) {
    const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
    const index = parseInt(weekday) - 1;
    return index >= 0 && index < weekdays.length ? weekdays[index] : `星期${weekday}`;
}

/**
 * 格式化文件大小
 * @param {number} bytes - 文件大小 (字节)
 * @param {number} decimals - 保留的小数位数
 * @returns {string} 格式化后的文件大小字符串
 */
export function formatFileSize(bytes, decimals = 2) {
    if (bytes === 0 || bytes === null || bytes === undefined) return '0 Bytes';

    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

export default {
    formatDateTime,
    formatUserType,
    getUserTypeTagType,
    formatActivityStatus,
    getActivityStatusTagType,
    formatCourseStatus,
    formatWeekday
}; 