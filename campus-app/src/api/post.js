import request from '@/utils/request'
import {FORUM_API} from './api-endpoints'

// 获取帖子列表
export function getPosts(params) {
  return request({
    url: FORUM_API.GET_ALL,
    method: 'get',
    params
  })
}

// 获取所有帖子（别名，功能同 getPosts）
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

// 获取帖子评论
export function getPostComments(postId, params) {
  return request({
    url: FORUM_API.GET_COMMENTS.replace(':postId', postId),
    method: 'get',
    params
  })
}

// 创建帖子
export function createPost(data) {
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

// 点赞帖子
export function likePost(id) {
  return request({
    url: FORUM_API.LIKE_POST.replace(':id', id),
    method: 'post'
  })
}

// 取消点赞帖子
export function cancelLikePost(id) {
  return request({
    url: FORUM_API.UNLIKE_POST.replace(':id', id),
    method: 'post'
  })
}

// 添加评论
export function addComment(postId, content) {
  return request({
    url: FORUM_API.ADD_COMMENT.replace(':postId', postId),
    method: 'post',
    data: { content }
  })
}

// 添加回复评论
export function createComment(data) {
  return request({
    url: FORUM_API.ADD_COMMENT_DIRECT,
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

// 点赞评论
export function likeComment(commentId) {
  return request({
    url: FORUM_API.LIKE_COMMENT.replace(':id', commentId),
    method: 'post'
  })
}

// 取消点赞评论
export function cancelLikeComment(commentId) {
  return request({
    url: FORUM_API.UNLIKE_COMMENT.replace(':id', commentId),
    method: 'post'
  })
}

// 获取热门帖子
export function getHotPosts(limit = 10) {
  return request({
    url: FORUM_API.GET_HOT_POSTS,
    method: 'get',
    params: { limit }
  })
}

// 获取置顶帖子
export function getTopPosts() {
  return request({
    url: FORUM_API.GET_TOP,
    method: 'get'
  })
}

// 获取精华帖子
export function getEssencePosts() {
  return request({
    url: FORUM_API.GET_ESSENCE,
    method: 'get'
  })
}

// 设置帖子置顶状态
export function setPostTop(id, isTop) {
  return request({
    url: FORUM_API.SET_TOP.replace(':id', id),
    method: 'put',
    data: { isTop }
  })
}

// 设置帖子精华状态
export function setPostEssence(id, isEssence) {
  return request({
    url: FORUM_API.SET_ESSENCE.replace(':id', id),
    method: 'put',
    data: { isEssence }
  })
}

// 搜索帖子
export function searchPosts(params) {
  return request({
    url: FORUM_API.SEARCH,
    method: 'get',
    params
  })
}

// 获取我的帖子列表
export function getMyPosts(params) {
  return request({
    url: FORUM_API.GET_MY_POSTS,
    method: 'get',
    params
  })
}

// 获取指定用户的帖子列表
export function getUserPosts(userId, params) {
  return request({
    url: FORUM_API.GET_BY_USER.replace(':userId', userId),
    method: 'get',
    params
  })
}

// 获取论坛分类
export function getForumCategories(params) {
  return request({
    url: FORUM_API.GET_CATEGORIES,
    method: 'get',
    params
  })
}

// 获取可用版块列表
export function getAvailableForums() {
  return request({
    url: FORUM_API.GET_AVAILABLE_FORUMS,
    method: 'get'
  })
}

// 获取所有评论
export function getAllComments(userId, params) {
  return request({
    url: FORUM_API.GET_COMMENTS_BY_USER.replace(':userId', userId),
    method: 'get',
    params
  })
}

// 更新评论状态
export function updateCommentStatus(commentId, data) {
  return request({
    url: FORUM_API.UPDATE_COMMENT.replace(':id', commentId),
    method: 'put',
    data
  })
}

// 更新帖子浏览量
export function viewPost(id) {
  return request({
    url: FORUM_API.INCREMENT_VIEWS.replace(':id', id),
    method: 'post'
  })
}

// 创建论坛分类
export function createForumCategory(data) {
  return request({
    url: FORUM_API.CREATE_CATEGORY,
    method: 'post',
    data
  })
}

// 更新论坛分类
export function updateForumCategory(id, data) {
  return request({
    url: FORUM_API.UPDATE_CATEGORY.replace(':id', id),
    method: 'put',
    data
  })
}

// 删除论坛分类
export function deleteForumCategory(id) {
  return request({
    url: FORUM_API.DELETE_CATEGORY.replace(':id', id),
    method: 'delete'
  })
} 