// import { request } from '@/utils/request'
import request from './request'; // Use relative path since it's in the same directory
import {
    ADD_CATEGORY,
    CREATE_POST,
    CREATE_POST_CATEGORY,
    DELETE_CATEGORY,
    DELETE_COMMENT_PERMANENT,
    DELETE_POST,
    DELETE_POST_CATEGORY,
    FORUM_API_BASE,
    GET_ALL_CATEGORIES,
    GET_ALL_COMMENTS,
    GET_AVAILABLE_FORUMS,
    GET_ESSENCE_POSTS,
    GET_HOT_POSTS,
    GET_MY_POSTS,
    GET_POST_CATEGORIES,
    GET_POST_DETAIL,
    GET_POSTS,
    GET_TOP_POSTS,
    GET_USER_POSTS,
    LIKE_COMMENT,
    LIKE_POST,
    SEARCH_POSTS,
    SET_POST_ESSENCE,
    SET_POST_TOP,
    UNLIKE_COMMENT,
    UNLIKE_POST,
    UPDATE_CATEGORY,
    UPDATE_COMMENT_STATUS,
    UPDATE_POST,
    UPDATE_POST_CATEGORY,
    UPDATE_POST_STATUS,
    UPLOAD_FILE_URL,
    UPLOAD_POST_IMAGE_URL
} from './api-endpoints'

const FORUM_API = FORUM_API_BASE || '/api/forum' // Define FORUM_API or use a default

/**
 * Forum Categories API
 */
export function getAvailableForums() {
    return request({
        url: `${FORUM_API}${GET_AVAILABLE_FORUMS}`,
        method: 'get'
    })
}

export function getForumCategories(params) {
    return request({
        url: `${FORUM_API}${GET_POST_CATEGORIES}`,
        method: 'get',
        params
    })
}

/**
 * Post Categories API
 */
export function createForumCategory(data) {
    return request({
        url: `${FORUM_API}${CREATE_POST_CATEGORY}`,
        method: 'post',
        data
    })
}

export function updateForumCategory(id, data) {
    return request({
        url: `${FORUM_API}${UPDATE_POST_CATEGORY}/${id}`,
        method: 'put',
        data
    })
}

// ... other functions ...

export function deleteForumCategory(id) {
    return request({
        url: `${FORUM_API}${DELETE_POST_CATEGORY}/${id}`,
        method: 'delete'
    })
}

export function updateForumCategoryStatus(id, data) {
    return request({
        url: `${FORUM_API}/categories/${id}/status`,
        method: 'put',
        data
    });
}

/**
 * Post API
 */
// 获取帖子列表 (所有)
export function getAllPosts(params) {
    return request({
        url: `${FORUM_API}${GET_POSTS}`,
        method: 'get',
        params
    })
}

// 创建帖子
export function createPost(data) {
    return request({
        url: `${FORUM_API}${CREATE_POST}`,
        method: 'post',
        data
    })
}

// 获取帖子详情
export function getPostById(id) {
    return request({
        url: `${FORUM_API}${GET_POST_DETAIL}/${id}`,
        method: 'get'
    })
}

// 更新帖子
export function updatePost(id, data) {
    return request({
        url: `${FORUM_API}${UPDATE_POST}/${id}`,
        method: 'put',
        data
    })
}

// 删除帖子
export function deletePost(id) {
    return request({
        url: `${FORUM_API}${DELETE_POST}/${id}`,
        method: 'delete'
    })
}

// 获取我的帖子列表
export function getMyPosts(params) {
    return request({
        url: `${FORUM_API}${GET_MY_POSTS}`,
        method: 'get',
        params
    })
}

// 获取指定用户的帖子列表
export function getUserPosts(userId, params) {
    return request({
        url: `${FORUM_API}${GET_USER_POSTS}/${userId}`,
        method: 'get',
        params
    })
}

// 点赞帖子
export function likePost(id) {
    return request({
        url: `${FORUM_API}${LIKE_POST}/${id}`,
        method: 'post'
    })
}

// 取消点赞帖子
export function unlikePost(id) {
    return request({
        url: `${FORUM_API}${UNLIKE_POST}/${id}`,
        method: 'delete'
    })
}

// 搜索帖子
export function searchPosts(params) {
    return request({
        url: `${FORUM_API}${SEARCH_POSTS}`,
        method: 'get',
        params
    })
}

// 获取热门帖子
export function getHotPosts(params) {
    return request({
        url: `${FORUM_API}${GET_HOT_POSTS}`,
        method: 'get',
        params
    })
}

// 获取置顶帖子
export function getTopPosts() {
    return request({
        url: `${FORUM_API}${GET_TOP_POSTS}`,
        method: 'get'
    })
}

// 获取精华帖子
export function getEssencePosts() {
    return request({
        url: `${FORUM_API}${GET_ESSENCE_POSTS}`,
        method: 'get'
    })
}

// 设置/取消置顶
export function setPostTop(id, data) { // data should be { isTop: 0 or 1 }
    return request({
        url: `${FORUM_API}${SET_POST_TOP}/${id}`,
        method: 'put',
        data
    })
}

// 设置/取消精华
export function setPostEssence(id, data) { // data should be { isEssence: 0 or 1 }
    return request({
        url: `${FORUM_API}${SET_POST_ESSENCE}/${id}`,
        method: 'put',
        data
    })
}

// 更新帖子状态
export function updatePostStatus(id, data) { // data should be { status: ... }
    return request({
        url: `${FORUM_API}${UPDATE_POST_STATUS}/${id}`,
        method: 'put',
        data
    })
}

/**
 * Comment API
 */
// 获取帖子的评论列表
export function getPostComments(postId, params) {
    return request({
        url: `${FORUM_API}/posts/${postId}/comments`,
        method: 'get',
        params
    })
}

// 发表评论
export function addComment(postId, data) {
    return request({
        url: `${FORUM_API}/posts/${postId}/comments`,
        method: 'post',
        data
    })
}

// 删除评论 (逻辑删除)
export function deleteComment(commentId) {
    return request({
        url: `${FORUM_API}/comments/${commentId}`,
        method: 'delete'
    })
}

// 获取所有评论 (管理端)
export function getAllComments(params) {
    return request({
        url: `${FORUM_API}${GET_ALL_COMMENTS}`,
        method: 'get',
        params
    })
}

// 更新评论状态 (管理端)
export function updateCommentStatus(commentId, data) {
    return request({
        url: `${FORUM_API}${UPDATE_COMMENT_STATUS}/${commentId}`,
        method: 'put',
        data
    })
}

// 永久删除评论 (管理端)
export function deleteCommentPermanent(commentId) {
    return request({
        url: `${FORUM_API}${DELETE_COMMENT_PERMANENT}/${commentId}`,
        method: 'delete'
    })
}

/**
 * Category API (assuming these are forum categories, matching previous naming)
 */
export function getAllCategories(params) {
    return request({
        url: `${FORUM_API}${GET_ALL_CATEGORIES}`,
        method: 'get',
        params
    })
}

export function addCategory(data) {
    return request({
        url: `${FORUM_API}${ADD_CATEGORY}`,
        method: 'post',
        data
    })
}

export function updateCategory(id, data) {
    return request({
        url: `${FORUM_API}${UPDATE_CATEGORY}/${id}`,
        method: 'put',
        data
    })
}

export function deleteCategory(id) {
    return request({
        url: `${FORUM_API}${DELETE_CATEGORY}/${id}`,
        method: 'delete'
    })
}

/**
 * File Upload API (for forum)
 */
export function uploadFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: `${FORUM_API}${UPLOAD_FILE_URL}`,
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

export function uploadPostImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: `${FORUM_API}${UPLOAD_POST_IMAGE_URL}`,
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

export function viewPost(postId) {
    return request.post(incrementViewCount(postId));
}

export function likeComment(commentId) {
    return request.post(`${LIKE_COMMENT}/${commentId}`);
}

export function cancelLikeComment(commentId) {
    return request.delete(`${UNLIKE_COMMENT}/${commentId}`);
}

// 添加论坛视图增量计数
export const incrementViewCount = (postId) => {
    // 注意：这里直接返回了URL字符串，可能需要修改为调用 request 函数
    // return request({ url: `/forum/posts/${postId}/view`, method: 'put' });
    return `/forum/posts/${postId}/view`;
}

// 用户状态管理mock
export const useUserStore = () => {
    // 这是一个 Mock 函数，返回固定的用户信息
    return {
        userInfo: {
            id: 1,
            username: '测试用户',
            role: 'admin'
        }
    };
};

// 管理后台论坛列表查询 (Mock)
export const getForumList = (params) => {
    // 这是一个 Mock 函数，仅打印日志并返回空列表
    console.log('查询论坛列表 (Mock)', params);
    return Promise.resolve({
        data: {
            list: [],
            total: 0
        }
    });
};

// 添加论坛帖子 (Mock)
export const addPost = (data) => {
    // 这是一个 Mock 函数，仅打印日志并返回成功信息
    console.log('添加论坛帖子 (Mock)', data);
    return Promise.resolve({
        code: 200,
        data: {id: Math.random().toString(36).substring(2, 10)},
        message: '添加成功 (Mock)'
    });
};