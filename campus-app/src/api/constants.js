// 论坛API基础路径
export const FORUM_API_BASE = '/api/forum';

// 帖子相关
export const GET_POSTS = '/posts';
export const CREATE_POST = '/post';
export const GET_POST_DETAIL = '/post/:id';
export const UPDATE_POST = '/post/:id';
export const DELETE_POST = '/post/:id';
export const GET_MY_POSTS = '/posts/my';
export const GET_USER_POSTS = '/posts/user/:id';
export const LIKE_POST = '/post/:id/like';
export const UNLIKE_POST = '/post/:id/unlike';
export const SEARCH_POSTS = '/posts/search';
export const GET_HOT_POSTS = '/posts/hot';
export const GET_TOP_POSTS = '/posts/top';
export const GET_ESSENCE_POSTS = '/posts/essence';
export const SET_POST_TOP = '/post/:id/top';
export const SET_POST_ESSENCE = '/post/:id/essence';
export const UPDATE_POST_STATUS = '/post/:id/status';

// 评论相关
export const GET_POST_COMMENTS = '/posts/:id/comments';
export const DELETE_POST_COMMENT = '/comments/:id';
export const GET_ALL_COMMENTS = '/comments';
export const UPDATE_COMMENT_STATUS = '/comments/:id/status';
export const DELETE_COMMENT_PERMANENT = '/comments/:id';

// 分类相关
export const GET_POST_CATEGORIES = '/categories';
export const CREATE_POST_CATEGORY = '/categories';
export const UPDATE_POST_CATEGORY = '/categories/:id';
export const DELETE_POST_CATEGORY = '/categories/:id';
export const GET_ALL_CATEGORIES = '/categories/all';
export const ADD_CATEGORY = '/categories';
export const UPDATE_CATEGORY = '/categories/:id';
export const DELETE_CATEGORY = '/categories/:id';

// 文件相关
export const UPLOAD_POST_IMAGE_URL = '/upload/image';
export const UPLOAD_FILE_URL = '/upload/file';

// 提供一个辅助函数用于增加浏览量
export const incrementViewCount = (postId) => {
    // 这里应该是实际的API调用
    console.log(`Incrementing view count for post: ${postId}`);
    return Promise.resolve();
};

// 用户状态管理mock
export const useUserStore = () => {
    return {
        userInfo: {
            id: 1,
            username: '测试用户',
            role: 'admin'
        }
    };
};

// 管理后台论坛相关函数
export const getForumList = (params) => {
    console.log('查询论坛列表', params);
    return Promise.resolve({
        data: {
            list: [],
            total: 0
        }
    });
};

export const addPost = (data) => {
    console.log('添加论坛帖子', data);
    return Promise.resolve({
        code: 200,
        data: {id: Math.random().toString(36).substring(2, 10)},
        message: '添加成功'
    });
}; 