// 用户相关接口
export const USER_API = {
    LOGIN: '/users/login',
    REGISTER: '/users/register',
    GET_CURRENT_USER: '/users/current',
    CHANGE_PASSWORD: '/users/change-password',
    GET_USER_LIST: '/users/list',
    ADD_USER: '/users',
    UPDATE_USER: '/users/:id',
    DELETE_USER: '/users/:id',
    CHECK_PERMISSION: '/users/check-permission',
    GET_PERMISSIONS: '/users/permissions',
    LOGOUT: '/users/logout'
}

// 课程相关接口
export const COURSE_API = {
    GET_ALL: '/courses',
    GET_BY_ID: '/courses/:id',
    GET_BY_NO: '/courses/no/:courseNo',
    GET_BY_TYPE: '/courses/type/:courseType',
    GET_BY_COLLEGE: '/courses/college/:collegeId',
    ADD: '/courses',
    UPDATE: '/courses/:id',
    DELETE: '/courses/:id',
    BATCH_DELETE: '/courses/batch',
    UPDATE_STATUS: '/courses/:id/status/:status'
}

// 教室相关接口
export const CLASSROOM_API = {
    GET_ALL: '/classrooms',
    GET_BY_ID: '/classrooms/:id',
    ADD: '/classrooms',
    UPDATE: '/classrooms/:id',
    DELETE: '/classrooms/:id',
    BATCH_DELETE: '/classrooms/batch',
    UPDATE_STATUS: '/classrooms/:id/status/:status',
    GET_AVAILABLE: '/classrooms/available',
    GET_BY_BUILDING: '/classrooms/building/:building',
    GET_BY_TYPE: '/classrooms/type/:roomType',
    GET_BY_CAPACITY: '/classrooms/capacity'
}

// 课表相关接口
export const SCHEDULE_API = {
    GET_ALL: '/schedules',
    GET_BY_ID: '/schedules/:id',
    ADD: '/schedules',
    UPDATE: '/schedules/:id',
    DELETE: '/schedules/:id',
    BATCH_DELETE: '/schedules/batch',
    BATCH_GENERATE: '/schedules/batch-generate',
    CHECK_CONFLICT: '/schedules/check-conflict',
    GET_TEACHER_WEEKLY: '/schedules/teacher-weekly',
    GET_STUDENT_WEEKLY: '/schedules/student-weekly',
    GET_CLASSROOM_WEEKLY: '/schedules/classroom-weekly'
}

// 活动相关接口
export const ACTIVITY_API = {
    GET_ALL: '/activities',
    GET_BY_ID: '/activities/:id',
    GET_BY_TYPE: '/activities/type/:activityType',
    GET_BY_STATUS: '/activities/status/:status',
    GET_ONGOING: '/activities/ongoing',
    GET_UPCOMING: '/activities/upcoming',
    ADD: '/activities',
    ADD_WITH_POSTER: '/activities/with-poster',
    UPDATE: '/activities/:id',
    DELETE: '/activities/:id',
    BATCH_DELETE: '/activities/batch',
    UPDATE_STATUS: '/activities/:id/status/:status',
    UPLOAD_POSTER: '/activities/poster/upload'
}

// 通知相关接口
export const NOTICE_API = {
    GET_ALL: '/notices',
    GET_BY_ID: '/notices/:id',
    GET_BY_TYPE: '/notices/type/:noticeType',
    GET_BY_STATUS: '/notices/status/:status',
    GET_RECENT: '/notices/recent',
    GET_TOP: '/notices/top',
    GET_BY_PUBLISHER: '/notices/publisher/:publisherId',
    ADD: '/notices',
    UPDATE: '/notices/:id',
    DELETE: '/notices/:id',
    BATCH_DELETE: '/notices/batch',
    UPDATE_STATUS: '/notices/:id/status/:status'
}

// 论坛相关接口
export const FORUM_API = {
    GET_ALL: '/forum/posts',
    GET_BY_ID: '/forum/posts/:id',
    GET_BY_USER: '/forum/posts/user/:userId',
    GET_MY_POSTS: '/forum/posts/my',
    GET_BY_CATEGORY: '/forum/posts/category/:category',
    SEARCH: '/forum/posts/search',
    ADD: '/forum/posts',
    UPDATE: '/forum/posts/:id',
    DELETE: '/forum/posts/:id',
    GET_COMMENTS: '/forum/posts/:postId/comments',
    ADD_COMMENT: '/forum/posts/:postId/comments',
    DELETE_COMMENT: '/forum/posts/comments/:commentId',
    GET_HOT_POSTS: '/forum/posts/hot',
    INCREMENT_VIEWS: '/forum/posts/:id/views',
    LIKE_POST: '/forum/posts/:id/like',
    GET_TAGS: '/forum/posts/:postId/tags',
    ADD_TAGS: '/forum/posts/:postId/tags',
    REMOVE_TAG: '/forum/posts/:postId/tags/:tagId',
    GET_POST_STATS: '/forum/posts/stats',

    // 评论相关
    GET_COMMENT: '/forum/comments/:id',
    GET_COMMENTS_BY_USER: '/forum/comments/user/:userId',
    GET_ROOT_COMMENTS: '/forum/posts/:postId/root-comments',
    GET_CHILD_COMMENTS: '/forum/comments/:parentId/children',
    ADD_COMMENT_DIRECT: '/forum/comments',
    UPDATE_COMMENT: '/forum/comments/:id',
    DELETE_COMMENT_DIRECT: '/forum/comments/:id',
    BATCH_DELETE_COMMENTS: '/forum/comments/batch',
    LIKE_COMMENT: '/forum/comments/:id/like',

    // 标签相关
    GET_ALL_TAGS: '/forum/tags',
    GET_TAG_BY_ID: '/forum/tags/:id',
    SEARCH_TAGS: '/forum/tags/search',
    ADD_TAG: '/forum/tags',
    UPDATE_TAG: '/forum/tags/:id',
    DELETE_TAG: '/forum/tags/:id',
    BATCH_DELETE_TAGS: '/forum/tags/batch',

    // --- 合并自外部定义 ---
    GET_CATEGORIES: '/forum/categories',
    ADD_CATEGORY: '/forum/categories',
    UPDATE_CATEGORY: '/forum/categories',
    DELETE_CATEGORY: '/forum/categories',
    UNLIKE_POST: '/forum/posts/unlike',
    GET_TOP: '/forum/posts/top',
    GET_ESSENCE: '/forum/posts/essence',
    SET_TOP: '/forum/posts/top',
    SET_ESSENCE: '/forum/posts/essence',
    UPDATE_STATUS: '/forum/posts/status',
    UNLIKE_COMMENT: '/forum/comments/unlike',
    GET_ALL_CATEGORIES: '/forum/categories/all',
    GET_AVAILABLE_FORUMS: '/forum/forums',
    UPLOAD_POST_IMAGE: '/upload/image',
    UPLOAD_FILE: '/upload/file'
}

// 文件相关接口
export const FILE_API = {
    UPLOAD_IMAGE: '/file/upload/image',
    UPLOAD_IMAGE_BY_TYPE: '/file/upload/image/:type',
    UPLOAD_ACTIVITY_POSTER: '/file/upload/activity/poster',
    UPLOAD_USER_AVATAR: '/file/upload/avatar/:userId',
    UPLOAD_POST_IMAGE: '/file/upload/post/image',
    UPLOAD_DOCUMENT: '/file/upload/document',
    UPLOAD_COURSE_MATERIAL: '/file/upload/course/material/:courseId',
    DELETE_FILE: '/file/delete',
    GET_FILE_INFO: '/file/info',
    DOWNLOAD_FILE: '/file/download',
    GET_TEMP_URL: '/file/temp-url',
    LIST_DIRECTORY: '/file/manager/list',
    BATCH_DELETE: '/file/manager/batch-delete',
    GET_FILE_STATS: '/file/manager/stats'
}

// 通用接口
export const COMMON_API = {
    GET_COLLEGES: '/common/colleges',
    GET_DEPARTMENTS: '/common/departments',
    GET_MAJORS: '/common/majors',
    GET_CLASSES: '/common/classes',
    GET_TERMS: '/common/terms',
    GET_WEEKDAYS: '/common/weekdays',
    GET_TIME_SLOTS: '/common/time-slots',
    GET_ROOM_TYPES: '/common/room-types',
    GET_COURSE_TYPES: '/common/course-types',
    GET_ACTIVITY_TYPES: '/common/activity-types',
    GET_NOTICE_TYPES: '/common/notice-types',
    GET_POST_CATEGORIES: '/common/post-categories'
}

// --- 以下内容已移除并合并或移动到 forum.js ---