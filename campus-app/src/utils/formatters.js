/**
 * 日期时间格式化工具
 */

/**
 * 格式化日期时间
 * @param {string|Date} dateTime 日期时间字符串或Date对象
 * @param {string} format 格式化模式，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(dateTime, format = 'YYYY-MM-DD HH:mm:ss') {
    if (!dateTime) return '';

    const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime;

    if (!(date instanceof Date) || isNaN(date)) {
        return '';
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds);
}

/**
 * 格式化日期
 * @param {string|Date} dateTime 日期时间字符串或Date对象
 * @returns {string} 格式化后的日期字符串 (YYYY-MM-DD)
 */
export function formatDate(dateTime) {
    return formatDateTime(dateTime, 'YYYY-MM-DD');
}

/**
 * 格式化时间
 * @param {string|Date} dateTime 日期时间字符串或Date对象
 * @returns {string} 格式化后的时间字符串 (HH:mm:ss)
 */
export function formatTime(dateTime) {
    return formatDateTime(dateTime, 'HH:mm:ss');
}

/**
 * 格式化文件大小
 * @param {number} size 文件大小 (字节)
 * @returns {string} 格式化后的文件大小字符串
 */
export function formatFileSize(size) {
    if (size < 1024) {
        return size + 'B'
    } else if (size < 1024 * 1024) {
        return (size / 1024).toFixed(2) + 'KB'
    } else if (size < 1024 * 1024 * 1024) {
        return (size / (1024 * 1024)).toFixed(2) + 'MB'
    } else {
        return (size / (1024 * 1024 * 1024)).toFixed(2) + 'GB'
    }
} 