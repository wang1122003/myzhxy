/**
 * 全局常量定义
 */

/**
 * 用户类型
 */
export const USER_TYPES = {
    ADMIN: 'ADMIN',
    TEACHER: 'TEACHER',
    STUDENT: 'STUDENT'
};

/**
 * 用户类型显示文本
 */
export const USER_TYPE_LABELS = {
    [USER_TYPES.ADMIN]: '管理员',
    [USER_TYPES.TEACHER]: '教师',
    [USER_TYPES.STUDENT]: '学生'
};

/**
 * 用户状态
 */
export const USER_STATUS = {
    ACTIVE: 'ACTIVE',
    INACTIVE: 'INACTIVE'
};

/**
 * 用户状态显示文本
 */
export const USER_STATUS_LABELS = {
    [USER_STATUS.ACTIVE]: '正常',
    [USER_STATUS.INACTIVE]: '禁用'
};

/**
 * 活动状态
 */
export const ACTIVITY_STATUS = {
    CANCELLED: '0',
    ENROLLING: '1',
    ONGOING: '2',
    FINISHED: '3'
};

/**
 * 活动状态显示文本
 */
export const ACTIVITY_STATUS_LABELS = {
    [ACTIVITY_STATUS.CANCELLED]: '已取消',
    [ACTIVITY_STATUS.ENROLLING]: '报名中',
    [ACTIVITY_STATUS.ONGOING]: '进行中',
    [ACTIVITY_STATUS.FINISHED]: '已结束'
};

/**
 * 课程状态
 */
export const COURSE_STATUS = {
    PENDING: 'PENDING',
    ACTIVE: 'ACTIVE',
    FINISHED: 'FINISHED',
    CANCELLED: 'CANCELLED'
};

/**
 * 课程状态显示文本
 */
export const COURSE_STATUS_LABELS = {
    [COURSE_STATUS.PENDING]: '未开始',
    [COURSE_STATUS.ACTIVE]: '进行中',
    [COURSE_STATUS.FINISHED]: '已结束',
    [COURSE_STATUS.CANCELLED]: '已取消'
};

/**
 * 星期选项
 */
export const WEEKDAY_OPTIONS = [
    {label: '周一', value: 1},
    {label: '周二', value: 2},
    {label: '周三', value: 3},
    {label: '周四', value: 4},
    {label: '周五', value: 5},
    {label: '周六', value: 6},
    {label: '周日', value: 7}
];

/**
 * 通用分页参数
 */
export const DEFAULT_PAGINATION = {
    pageSize: 10,
    pageSizes: [10, 20, 50, 100],
    layout: 'total, sizes, prev, pager, next, jumper'
};

export default {
    USER_TYPES,
    USER_TYPE_LABELS,
    USER_STATUS,
    USER_STATUS_LABELS,
    ACTIVITY_STATUS,
    ACTIVITY_STATUS_LABELS,
    COURSE_STATUS,
    COURSE_STATUS_LABELS,
    WEEKDAY_OPTIONS,
    DEFAULT_PAGINATION
};