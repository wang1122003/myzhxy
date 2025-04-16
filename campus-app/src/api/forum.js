import request from './request'
import {FORUM_API} from './api-endpoints'

// =============== 帖子相关 API ===============

// 获取所有帖子
export function getAllPosts(params) {
    return request({
        url: FORUM_API.GET_ALL,
        method: 'get',
        params
    })
}

// 获取帖子详情
export function getPostById(id) {
    return request({
        url: FORUM_API.GET_BY_ID.replace(':id', id),
        method: 'get'
    })
}

// 添加帖子
export function addPost(data) {
    return request({
        url: FORUM_API.ADD,
        method: 'post',
        data
    })
}

// 更新帖子
export function updatePost(id, data) {
    return request({
        url: FORUM_API.UPDATE.replace(':id', id),
        method: 'put',
        data
    })
}

// 删除帖子
export function deletePost(id) {
    return request({
        url: FORUM_API.DELETE.replace(':id', id),
        method: 'delete'
    })
}

// 获取用户帖子
export function getPostsByUserId(userId, params) {
    return request({
        url: FORUM_API.GET_BY_USER.replace(':userId', userId),
        method: 'get',
        params
    })
}

// 获取我的帖子
export function getMyPosts(params) {
    return request({
        url: FORUM_API.GET_MY_POSTS,
        method: 'get',
        params
    })
}

// 获取分类帖子
export function getPostsByCategory(category, params) {
    return request({
        url: FORUM_API.GET_BY_CATEGORY.replace(':category', category),
        method: 'get',
        params
    })
}

// 搜索帖子
export function searchPosts(keyword, page = 1, size = 10) {
    return request({
        url: FORUM_API.SEARCH,
        method: 'get',
        params: {
            keyword,
            page,
            size
        }
    })
}

// 获取帖子评论
export function getPostComments(postId) {
    return request({
        url: FORUM_API.GET_COMMENTS.replace(':postId', postId),
        method: 'get'
    })
}

// 添加评论
export function addComment(postId, data) {
    return request({
        url: FORUM_API.ADD_COMMENT.replace(':postId', postId),
        method: 'post',
        data
    })
}

// 删除评论
export function deleteComment(commentId) {
    return request({
        url: FORUM_API.DELETE_COMMENT.replace(':commentId', commentId),
        method: 'delete'
    })
}

// 获取热门帖子
export function getHotPosts(limit = 5) {
    return request({
        url: FORUM_API.GET_HOT_POSTS,
        method: 'get',
        params: {limit}
    })
}

// 增加帖子浏览量
export function incrementPostViews(id) {
    return request({
        url: FORUM_API.INCREMENT_VIEWS.replace(':id', id),
        method: 'put'
    })
}

// 点赞帖子
export function likePost(id, isLike = true) {
    return request({
        url: FORUM_API.LIKE_POST.replace(':id', id),
        method: 'put',
        params: {isLike}
    })
}

// 获取帖子标签
export function getPostTags(postId) {
    return request({
        url: FORUM_API.GET_TAGS.replace(':postId', postId),
        method: 'get'
    })
}

// 添加标签到帖子
export function addTagsToPost(postId, tagIds) {
    return request({
        url: FORUM_API.ADD_TAGS.replace(':postId', postId),
        method: 'post',
        data: tagIds
    })
}

// 从帖子移除标签
export function removeTagFromPost(postId, tagId) {
    return request({
        url: FORUM_API.REMOVE_TAG.replace(':postId', postId).replace(':tagId', tagId),
        method: 'delete'
    })
}

// 获取帖子统计信息
export function getPostStats() {
    return request({
        url: FORUM_API.GET_POST_STATS,
        method: 'get'
    })
}

// 上传帖子图片
export function uploadPostImage(data) {
    return request({
        url: FORUM_API.UPLOAD_IMAGE,
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// =============== 评论相关 API ===============

// 获取评论详情
export function getCommentById(id) {
    return request({
        url: FORUM_API.GET_COMMENT.replace(':id', id),
        method: 'get'
    })
}

// 获取用户的评论
export function getCommentsByUserId(userId) {
    return request({
        url: FORUM_API.GET_COMMENTS_BY_USER.replace(':userId', userId),
        method: 'get'
    })
}

// 获取帖子的一级评论
export function getRootComments(postId) {
    return request({
        url: FORUM_API.GET_ROOT_COMMENTS.replace(':postId', postId),
        method: 'get'
    })
}

// 获取子评论
export function getChildComments(parentId) {
    return request({
        url: FORUM_API.GET_CHILD_COMMENTS.replace(':parentId', parentId),
        method: 'get'
    })
}

// 直接添加评论
export function addCommentDirect(data) {
    return request({
        url: FORUM_API.ADD_COMMENT_DIRECT,
        method: 'post',
        data
    })
}

// 更新评论
export function updateComment(id, data) {
    return request({
        url: FORUM_API.UPDATE_COMMENT.replace(':id', id),
        method: 'put',
        data
    })
}

// 直接删除评论
export function deleteCommentDirect(id) {
    return request({
        url: FORUM_API.DELETE_COMMENT_DIRECT.replace(':id', id),
        method: 'delete'
    })
}

// 批量删除评论
export function batchDeleteComments(ids) {
    return request({
        url: FORUM_API.BATCH_DELETE_COMMENTS,
        method: 'delete',
        data: ids
    })
}

// 点赞评论
export function likeComment(id) {
    return request({
        url: FORUM_API.LIKE_COMMENT.replace(':id', id),
        method: 'put'
    })
}

// =============== 标签相关 API ===============

// 获取所有标签
export function getAllTags() {
    return request({
        url: FORUM_API.GET_ALL_TAGS,
        method: 'get'
    })
}

// 获取标签详情
export function getTagById(id) {
    return request({
        url: FORUM_API.GET_TAG_BY_ID.replace(':id', id),
        method: 'get'
    })
}

// 搜索标签
export function searchTags(keyword) {
    return request({
        url: FORUM_API.SEARCH_TAGS,
        method: 'get',
        params: {keyword}
    })
}

// 添加标签
export function addTag(data) {
    return request({
        url: FORUM_API.ADD_TAG,
        method: 'post',
        data
    })
}

// 更新标签
export function updateTag(id, data) {
    return request({
        url: FORUM_API.UPDATE_TAG.replace(':id', id),
        method: 'put',
        data
    })
}

// 删除标签
export function deleteTag(id) {
    return request({
        url: FORUM_API.DELETE_TAG.replace(':id', id),
        method: 'delete'
    })
}

// 批量删除标签
export function batchDeleteTags(ids) {
    return request({
        url: FORUM_API.BATCH_DELETE_TAGS,
        method: 'delete',
        data: ids
    })
} 