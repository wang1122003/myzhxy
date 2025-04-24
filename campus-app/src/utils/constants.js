export const STATUS_MAPPING = {
    // 用户状态
    USER: {
        PENDING: '待审核',
        ACTIVE: '正常',
        LOCKED: '已锁定',
        ARCHIVED: '已归档'
    },

    // 教师状态
    TEACHER: {
        UNVERIFIED: '未认证',
        VERIFIED: '已认证',
        SUSPENDED: '已停用'
    },

    // 课程状态
    COURSE: {
        DRAFT: '草稿',
        PUBLISHED: '已发布',
        CLOSED: '已结课'
    }
}

export const ROLE_TYPES = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员'
}