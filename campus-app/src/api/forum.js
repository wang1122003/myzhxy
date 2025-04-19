// import { request } from '@/utils/request'
import request from './request'; // Use relative path since it's in the same directory
import {FILE_API, FORUM_API} from './api-endpoints';

//const FORUM_API_BASE = FORUM_API.BASE || '/api/forum' // Base path could be part of the object or defined here
const API_BASE = '/api'; // Assuming a common base like /api

/**
 * Forum Categories API
 */
export function getAvailableForums() {
    return request({
        url: `${API_BASE}${FORUM_API.GET_AVAILABLE_FORUMS}`,
        method: 'get'
    });
}

export function getForumCategories(params) {
    return request({
        // Assuming GET_POST_CATEGORIES is actually for forum categories
        url: `${API_BASE}${FORUM_API.GET_CATEGORIES}`, // Or potentially COMMON_API.GET_POST_CATEGORIES?
        method: 'get',
        params
    });
}

/**
 * Post Categories API (Maybe Forum Categories?)
 */
export function createForumCategory(data) {
    return request({
        url: `${API_BASE}${FORUM_API.ADD_CATEGORY}`,
        method: 'post',
        data
    });
}

export function updateForumCategory(id, data) {
    // Assuming UPDATE_CATEGORY needs an ID in the path
    const url = `${API_BASE}${FORUM_API.UPDATE_CATEGORY}`.includes(':id')
        ? `${API_BASE}${FORUM_API.UPDATE_CATEGORY.replace(':id', id)}`
        : `${API_BASE}${FORUM_API.UPDATE_CATEGORY}/${id}`; // Or adjust based on actual endpoint structure
    return request({
        url: url,
        method: 'put',
        data
    });
}

export function deleteForumCategory(id) {
    // Assuming DELETE_CATEGORY needs an ID
    const url = `${API_BASE}${FORUM_API.DELETE_CATEGORY}`.includes(':id')
        ? `${API_BASE}${FORUM_API.DELETE_CATEGORY.replace(':id', id)}`
        : `${API_BASE}${FORUM_API.DELETE_CATEGORY}/${id}`; // Or adjust based on actual endpoint structure
    return request({
        url: url,
        method: 'delete'
    });
}

export function updateForumCategoryStatus(id, data) {
    return request({
        // No specific endpoint for category status in FORUM_API, construct manually or add one
        url: `${API_BASE}/forum/categories/${id}/status`,
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
        url: `${API_BASE}${FORUM_API.GET_ALL}`,
        method: 'get',
        params
    });
}

// 创建帖子
export function createPost(data) {
    return request({
        url: `${API_BASE}${FORUM_API.ADD}`,
        method: 'post',
        data
    });
}

// 获取帖子详情
export function getPostById(id) {
    return request({
        url: `${API_BASE}${FORUM_API.GET_BY_ID.replace(':id', id)}`,
        method: 'get'
    });
}

// 更新帖子
export function updatePost(id, data) {
    return request({
        url: `${API_BASE}${FORUM_API.UPDATE.replace(':id', id)}`,
        method: 'put',
        data
    });
}

// 删除帖子
export function deletePost(id) {
    return request({
        url: `${API_BASE}${FORUM_API.DELETE.replace(':id', id)}`,
        method: 'delete'
    });
}

// 获取我的帖子列表
export function getMyPosts(params) {
    return request({
        url: `${API_BASE}${FORUM_API.GET_MY_POSTS}`,
        method: 'get',
        params
    });
}

// 获取指定用户的帖子列表
export function getUserPosts(userId, params) {
    return request({
        url: `${API_BASE}${FORUM_API.GET_BY_USER.replace(':userId', userId)}`,
        method: 'get',
        params
    });
}

// 点赞帖子
export function likePost(id) {
    return request({
        url: `${API_BASE}${FORUM_API.LIKE_POST.replace(':id', id)}`,
        method: 'post'
    });
}

// 取消点赞帖子
export function unlikePost(id) {
    // Assuming UNLIKE_POST needs the id in the path or body
    const url = `${API_BASE}${FORUM_API.UNLIKE_POST}`.includes(':id')
        ? `${API_BASE}${FORUM_API.UNLIKE_POST.replace(':id', id)}`
        : `${API_BASE}${FORUM_API.UNLIKE_POST}/${id}`; // Adjust as needed
    return request({
        url: url,
        method: 'delete' // Or 'post' depending on API design
    });
}

// 搜索帖子
export function searchPosts(params) {
    return request({
        url: `${API_BASE}${FORUM_API.SEARCH}`,
        method: 'get',
        params
    });
}

// 获取热门帖子
export function getHotPosts(params) {
    return request({
        url: `${API_BASE}${FORUM_API.GET_HOT_POSTS}`,
        method: 'get',
        params
    });
}

// 获取置顶帖子
export function getTopPosts() {
    return request({
        url: `${API_BASE}${FORUM_API.GET_TOP}`,
        method: 'get'
    });
}

// 获取精华帖子
export function getEssencePosts() {
    return request({
        url: `${API_BASE}${FORUM_API.GET_ESSENCE}`,
        method: 'get'
    });
}

// 设置/取消置顶
export function setPostTop(id, data) { // data should be { isTop: 0 or 1 }
    const url = `${API_BASE}${FORUM_API.SET_TOP}`.includes(':id')
        ? `${API_BASE}${FORUM_API.SET_TOP.replace(':id', id)}`
        : `${API_BASE}${FORUM_API.SET_TOP}/${id}`; // Adjust as needed
    return request({
        url: url,
        method: 'put',
        data
    });
}

// 设置/取消精华
export function setPostEssence(id, data) { // data should be { isEssence: 0 or 1 }
    const url = `${API_BASE}${FORUM_API.SET_ESSENCE}`.includes(':id')
        ? `${API_BASE}${FORUM_API.SET_ESSENCE.replace(':id', id)}`
        : `${API_BASE}${FORUM_API.SET_ESSENCE}/${id}`; // Adjust as needed
    return request({
        url: url,
        method: 'put',
        data
    });
}

// 更新帖子状态
export function updatePostStatus(id, data) { // data should be { status: ... }
    const url = `${API_BASE}${FORUM_API.UPDATE_STATUS}`.includes(':id')
        ? `${API_BASE}${FORUM_API.UPDATE_STATUS.replace(':id', id)}`
        : `${API_BASE}${FORUM_API.UPDATE_STATUS}/${id}`; // Adjust as needed
    return request({
        url: url,
        method: 'put',
        data
    });
}

/**
 * Comment API
 */
// 获取帖子的评论列表
export function getPostComments(postId, params) {
    return request({
        url: `${API_BASE}${FORUM_API.GET_COMMENTS.replace(':postId', postId)}`,
        method: 'get',
        params
    });
}

// 发表评论
export function addComment(postId, data) {
    return request({
        url: `${API_BASE}${FORUM_API.ADD_COMMENT.replace(':postId', postId)}`,
        method: 'post',
        data
    });
}

// 点赞评论
export function likeComment(commentId) {
    return request({
        url: `${API_BASE}${FORUM_API.LIKE_COMMENT.replace(':id', commentId)}`,
        method: 'post'
    });
}

// 取消点赞评论 (假设 endpoint 存在且需要 ID)
export function unlikeComment(commentId) {
    const url = `${API_BASE}${FORUM_API.UNLIKE_COMMENT}`.includes(':id')
        ? `${API_BASE}${FORUM_API.UNLIKE_COMMENT.replace(':id', commentId)}`
        : `${API_BASE}${FORUM_API.UNLIKE_COMMENT}/${commentId}`; // Adjust as needed
    return request({
        url: url,
        method: 'delete' // Or 'post'
    });
}

// 删除评论 (逻辑删除/更新状态?)
export function deleteComment(commentId) {
    // Assuming DELETE_COMMENT means logical delete/update status
    const url = `${API_BASE}${FORUM_API.DELETE_COMMENT_DIRECT.replace(':id', commentId)}`; // Use direct delete endpoint
    return request({
        url: url,
        method: 'delete'
        // If it's status update, method might be PUT and need data
        // url: `${API_BASE}/forum/comments/${commentId}/status`
        // method: 'put',
        // data: { status: 'deleted' } // Example
    });
}

// 获取所有评论 (管理端?)
export function getAllComments(params) {
    // No specific endpoint in FORUM_API, maybe admin-specific?
    return request({
        url: `${API_BASE}/forum/comments/all`, // Example endpoint
        method: 'get',
        params
    });
}

// 更新评论状态 (管理端?)
export function updateCommentStatus(commentId, data) {
    // No specific endpoint in FORUM_API, construct manually or add one
    return request({
        url: `${API_BASE}/forum/comments/${commentId}/status`,
        method: 'put',
        data
    });
}

// 永久删除评论 (管理端?)
export function deleteCommentPermanent(commentId) {
    // Assuming direct delete endpoint is for permanent deletion
    const url = `${API_BASE}${FORUM_API.DELETE_COMMENT_DIRECT.replace(':id', commentId)}`;
    return request({
        url: url,
        method: 'delete'
    });
}


/**
 * Category API (Admin?)
 */
export function getAllCategories(params) {
    return request({
        url: `${API_BASE}${FORUM_API.GET_CATEGORIES}`,
        method: 'get',
        params
    });
}

// Note: addCategory, updateCategory, deleteCategory are duplicates of
// createForumCategory, updateForumCategory, deleteForumCategory above.
// Decide which naming convention to use and remove duplicates.
// Assuming the 'ForumCategory' names are preferred based on earlier functions.

/*
export function addCategory(data) {
    return request({
        url: `${API_BASE}${FORUM_API.ADD_CATEGORY}`,
        method: 'post',
        data
    });
}

export function updateCategory(id, data) {
     const url = `${API_BASE}${FORUM_API.UPDATE_CATEGORY}`.includes(':id')
        ? `${API_BASE}${FORUM_API.UPDATE_CATEGORY.replace(':id', id)}`
        : `${API_BASE}${FORUM_API.UPDATE_CATEGORY}/${id}`; // Adjust as needed
    return request({
        url: url,
        method: 'put',
        data
    });
}

export function deleteCategory(id) {
     const url = `${API_BASE}${FORUM_API.DELETE_CATEGORY}`.includes(':id')
        ? `${API_BASE}${FORUM_API.DELETE_CATEGORY.replace(':id', id)}`
        : `${API_BASE}${FORUM_API.DELETE_CATEGORY}/${id}`; // Adjust as needed
    return request({
        url: url,
        method: 'delete'
    });
}
*/

/**
 * File Upload API
 */
export function uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);
    return request({
        url: `${API_BASE}${FILE_API.UPLOAD_FILE || '/upload/file'}`, // Use FILE_API if available
        method: 'post',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data: formData
    });
}

export function uploadPostImage(file) {
    const formData = new FormData();
    formData.append('image', file); // Usually 'image' or 'file'
    return request({
        url: `${API_BASE}${FILE_API.UPLOAD_POST_IMAGE || '/upload/post/image'}`, // Use FILE_API if available
        method: 'post',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data: formData
    });
}

// -- Utility/Other Functions --

// Example: Increment post view count
export function viewPost(postId) {
    return request({
        url: `${API_BASE}/forum/posts/${postId}/view`,
        method: 'post'
    });
}


// --- Placeholder/Example functions potentially moved from elsewhere ---

// These seem out of place or might be duplicates/unused imports.
// Review and remove/refactor if necessary.

// export const likeComment = (commentId) => {
//     // Duplicate of function above
// };

// export const cancelLikeComment = (commentId) => {
//     // Duplicate of unlikeComment above?
// };

// export const incrementViewCount = (postId) => {
//     // Duplicate of viewPost above
// };

// export const useUserStore = () => {
//     // Likely belongs in a store/state management file
// };

// export const getForumList = (params) => {
//     // Duplicate of getAvailableForums or getAllPosts?
// };

// export const addPost = (data) => {
//     // Duplicate of createPost
// };