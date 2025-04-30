import request from '@/utils/request'

// 论坛相关 API 端点 (帖子, 评论, 标签)
const API = {
    // 帖子相关
    GET_ALL_POSTS: '/posts', // 获取所有帖子 (分页)
    GET_POST_BY_ID: (id) => `/posts/${id}`, // 根据 ID 获取帖子详情
    GET_POSTS_BY_USER: (userId) => `/posts/user/${userId}`, // 获取指定用户的帖子
    GET_MY_POSTS: '/posts/my', // 获取当前登录用户的帖子
    SEARCH_POSTS: '/posts/search', // 搜索帖子
    ADD_POST: '/posts', // 添加新帖子
    UPDATE_POST: (id) => `/posts/${id}`, // 更新指定帖子
    DELETE_POST: (id) => `/posts/${id}`, // 删除指定帖子
    GET_HOT_POSTS: '/posts/hot', // 获取热门帖子
    INCREMENT_VIEW: (id) => `/posts/${id}/view`, // 增加帖子浏览量
    LIKE_POST: (id) => `/posts/${id}/like`, // 点赞帖子
    UNLIKE_POST: (id) => `/posts/${id}/unlike`, // 取消点赞帖子

    // 管理员接口
    GET_ADMIN_POSTS: '/posts/admin', // 管理员获取所有帖子
    UPDATE_POST_STATUS_ADMIN: (id) => `/posts/admin/${id}/status`, // 管理员更新帖子状态
    SET_POST_ESSENCE_ADMIN: (id) => `/posts/admin/${id}/essence`, // 管理员设置精华
    SET_POST_TOP_ADMIN: (id) => `/posts/admin/${id}/top`, // 管理员设置置顶
    
    // 评论相关
    GET_ROOT_COMMENTS: (postId) => `/posts/${postId}/comments`, // 获取帖子的根评论 (分页)
    GET_CHILD_COMMENTS: (parentId) => `/posts/comments/${parentId}/children`, // 获取子评论 (分页)
    GET_COMMENTS_BY_USER: (userId) => `/posts/comments/user/${userId}`, // 获取用户的评论
    ADD_COMMENT_DIRECT: '/posts/comments', // 直接添加评论 (需要 postId 和 parentId)
    UPDATE_COMMENT: (id) => `/posts/comments/${id}`, // 更新评论
    DELETE_COMMENT: (id) => `/posts/comments/${id}`, // 删除评论
    LIKE_COMMENT: (id) => `/posts/comments/${id}/like`, // 点赞评论
    UNLIKE_COMMENT: (id) => `/posts/comments/${id}/unlike`, // 取消点赞评论
    GET_ADMIN_COMMENTS: '/posts/admin/comments', // 管理员获取评论列表
    UPDATE_COMMENT_STATUS_ADMIN: (id) => `/posts/admin/comments/${id}/status`, // 管理员更新评论状态

    // 标签相关 (标签是内嵌在Post实体中的，通过Post API操作)

    // 移除分类相关 API 定义
    // GET_AVAILABLE_FORUMS: '/posts/forums',
    // GET_FORUM_CATEGORIES: '/posts/forums/categories'
};

// --- 帖子基本操作 API ---

// 获取帖子列表 (分页, 过滤)
// 移除 params 中的 category
export function getPosts(params) {
    return request({
        url: API.GET_ALL_POSTS,
        method: 'get',
        params // 移除 category
    })
}

// 获取所有帖子 (getPosts 的别名，功能相同)
export function getAllPosts(params) {
    return getPosts(params);
}

// 获取帖子详情
export function getPostById(id) {
    return request({
        url: API.GET_POST_BY_ID(id),
        method: 'get'
    })
}

// 创建新帖子
export function createPost(data) {
    return request({
        url: API.ADD_POST,
        method: 'post',
        data
    })
}

// 更新帖子信息
export function updatePost(id, data) {
    return request({
        url: API.UPDATE_POST(id),
        method: 'put',
        data
    })
}

// 删除帖子
export function deletePost(id) {
    return request({
        url: API.DELETE_POST(id),
        method: 'delete'
    })
}

// --- 帖子互动 API ---

// 点赞帖子
export function likePost(id) {
    return request({
        url: API.LIKE_POST(id),
        method: 'post'
    })
}

// 取消点赞帖子
export function cancelLikePost(id) {
    return request({
        url: API.UNLIKE_POST(id),
        method: 'delete'
    })
}

// 增加帖子浏览量
export function incrementViewCount(id) {
    return request({
        url: API.INCREMENT_VIEW(id),
        method: 'post'
    })
}

// --- 特殊帖子列表 API ---

// 获取热门帖子
export function getHotPosts(params) {
    return request({
        url: API.GET_HOT_POSTS,
        method: 'get',
        params
    })
}

// 搜索帖子
export function searchPosts(params) {
    return request({
        url: API.SEARCH_POSTS,
        method: 'get',
        params
    })
}

// 获取我的帖子列表
export function getMyPosts(params) {
    return request({
        url: API.GET_MY_POSTS,
        method: 'get',
        params
    })
}

// 获取指定用户的帖子列表
export function getUserPosts(userId, params) {
    return request({
        url: API.GET_POSTS_BY_USER(userId),
        method: 'get',
        params
    })
}

// --- 评论 API ---

// 获取帖子的评论 (根评论分页)
export function getRootComments(postId, params) {
    return request({
        url: API.GET_ROOT_COMMENTS(postId),
        method: 'get',
        params
    })
}

// 获取子评论 (分页)
export function getChildComments(parentId, params) {
    return request({
        url: API.GET_CHILD_COMMENTS(parentId),
        method: 'get',
        params
    })
}

// 获取指定用户的评论列表
export function getUserComments(userId, params) {
    return request({
        url: API.GET_COMMENTS_BY_USER(userId),
        method: 'get',
        params
    })
}

// 添加评论
export function createComment(data) {
    return request({
        url: API.ADD_COMMENT_DIRECT,
        method: 'post',
        data // data 应包含 postId, content, parentId (可选, 根评论为 null)
    })
}

// 更新评论内容
export function updateComment(id, data) {
    return request({
        url: API.UPDATE_COMMENT(id),
        method: 'put',
        data
    })
}

// 删除评论
export function deleteComment(commentId) {
    return request({
        url: API.DELETE_COMMENT(commentId),
        method: 'delete'
    })
}

// 点赞评论
export function likeComment(commentId) {
    return request({
        url: API.LIKE_COMMENT(commentId),
        method: 'post'
    })
}

// 取消点赞评论
export function cancelLikeComment(commentId) {
    return request({
        url: API.UNLIKE_COMMENT(commentId),
        method: 'post'
    })
}

// --- 管理员 API ---

// 获取管理员帖子列表 (分页, 过滤)
// 移除 params 中的 category
export function getPostsAdmin(params) {
    return request({
        url: API.GET_ADMIN_POSTS,
        method: 'get',
        params // 移除 category
    })
}

// 管理员获取所有评论
export function getCommentsAdmin(params) {
    return request({
        url: API.GET_ADMIN_COMMENTS,
        method: 'get',
        params
    })
}

// 管理员更新评论状态
export function updateCommentStatusAdmin(commentId, status) {
    return request({
        url: API.UPDATE_COMMENT_STATUS_ADMIN(commentId),
        method: 'put',
        data: {status}
    })
}

// 设置帖子精华状态 (管理员)
export function setPostEssence(id, isEssence = true) {
    return request({
        url: API.SET_POST_ESSENCE_ADMIN(id),
        method: 'put',
        data: {essence: isEssence}
    })
}

// 设置帖子置顶状态 (管理员)
export function setPostTop(id, isTop = true) {
    return request({
        url: API.SET_POST_TOP_ADMIN(id),
        method: 'put',
        data: {top: isTop}
    })
}

// 移除论坛分类管理 API 函数
// export function getForumCategories() { ... }
// export function createForumCategory(data) { ... }
// export function updateForumCategory(id, data) { ... }
// export function deleteForumCategory(id) { ... } 