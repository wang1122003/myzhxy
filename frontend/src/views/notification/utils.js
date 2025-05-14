/**
 * 格式化通知类型
 * @param {string} type 通知类型代码
 * @returns {string} 格式化后的类型名称
 */
export const formatNoticeType = (type) => {
    const typeMap = {
        SCHOOL: '学校通知',
        COLLEGE: '学院通知',
        COURSE: '课程通知',
        SYSTEM: '系统消息',
        ACTIVITY: '活动通知',
        GENERAL: '通用通知'
    };
    return typeMap[type] || type || '未知类型';
};

/**
 * 获取通知类型对应的标签类型
 * @param {string} type 通知类型代码
 * @returns {string} Element Plus 标签类型
 */
export const getNoticeTypeTag = (type) => {
    const typeMap = {
        SCHOOL: '',
        COLLEGE: 'success',
        COURSE: 'warning',
        SYSTEM: 'info',
        ACTIVITY: 'danger',
        GENERAL: 'primary'
    };
    return typeMap[type] || 'info';
};

/**
 * 标准化通知对象，确保字段一致性
 * @param {Object} notice 原始通知对象
 * @returns {Object} 标准化后的通知对象
 */
export const normalizeNotice = (notice) => {
    if (!notice) return null;

    // 创建一个新对象而不是修改原对象
    const normalized = {...notice};

    // 标准化附件字段
    if (!Array.isArray(normalized.attachments)) {
        normalized.attachments = normalized.attachmentFiles || [];
    }

    // 确保通知类型字段使用 noticeType (与后端保持一致)
    if (!normalized.noticeType && normalized.type) {
        normalized.noticeType = normalized.type;
    }

    // 确保发布者名称字段
    if (!normalized.publisherName && normalized.publisher) {
        normalized.publisherName = normalized.publisher;
    }

    return normalized;
};

/**
 * 格式化日期时间，支持相对时间和绝对时间
 * @param {string|Date} dateString 日期字符串或对象
 * @param {boolean} useRelative 是否使用相对时间 (今天/昨天/周几)
 * @returns {string} 格式化后的日期时间
 */
export const formatDateTime = (dateString, useRelative = false) => {
    if (!dateString) return '';

    try {
        const date = new Date(dateString);
        const now = new Date();

        if (!useRelative) {
            // 标准格式: 2023-09-01 08:30:00
            return date.toLocaleString('zh-CN', {hour12: false});
        }

        // 相对时间显示
        const diffMs = now - date;
        const diffMinutes = Math.floor(diffMs / (60 * 1000));
        const diffHours = Math.floor(diffMs / (60 * 60 * 1000));
        const diffDays = Math.floor(diffMs / (24 * 60 * 60 * 1000));

        // 1分钟内: 刚刚
        if (diffMinutes < 1) {
            return '刚刚';
        }

        // 1小时内: xx分钟前
        if (diffHours < 1) {
            return `${diffMinutes}分钟前`;
        }

        // 24小时内: xx小时前
        if (diffDays < 1) {
            return `${diffHours}小时前`;
        }

        // 昨天: 昨天 HH:MM
        if (diffDays === 1) {
            return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
        }

        // 7天内: 周几 HH:MM
        if (diffDays < 7) {
            const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
            return `${weekdays[date.getDay()]} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
        }

        // 超过7天: YYYY-MM-DD
        return date.toLocaleDateString('zh-CN');
    } catch (e) {
        console.error('日期格式化错误:', e);
        return dateString;
    }
};

/**
 * 格式化文件大小
 * @param {number} bytes 文件大小(字节)
 * @param {number} decimals 小数位数
 * @returns {string} 格式化后的文件大小
 */
export const formatFileSize = (bytes, decimals = 2) => {
    if (!+bytes) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
}; 