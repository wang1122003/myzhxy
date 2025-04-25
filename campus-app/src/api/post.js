import request from '@/utils/request'

// API Endpoints for Forum (Posts, Comments, Tags, Categories)
const API = {
    // Posts
    GET_ALL_POSTS: '/forum/posts',
    GET_POST_BY_ID: (id) => `/forum/posts/${id}`,
    GET_POSTS_BY_USER: (userId) => `/forum/posts/user/${userId}`,
    GET_MY_POSTS: '/forum/posts/my',
    SEARCH_POSTS: '/forum/posts/search',
    ADD_POST: '/forum/posts',
    UPDATE_POST: (id) => `/forum/posts/${id}`,
    DELETE_POST: (id) => `/forum/posts/${id}`,
    GET_HOT_POSTS: '/forum/posts/hot',
    INCREMENT_VIEW: (id) => `/forum/posts/${id}/view`,
    LIKE_POST: (id) => `/forum/posts/${id}/like`,
    UNLIKE_POST: (id) => `/forum/posts/${id}/unlike`,

    // Comments
    GET_COMMENT: (id) => `/forum/comments/${id}`,
    GET_COMMENTS_BY_USER: (userId) => `/forum/comments/user/${userId}`,
    GET_ROOT_COMMENTS: (postId) => `/forum/posts/${postId}/root-comments`,
    GET_CHILD_COMMENTS: (parentId) => `/forum/comments/${parentId}/children`,
    ADD_COMMENT_DIRECT: '/forum/comments',
    UPDATE_COMMENT: (id) => `/forum/comments/${id}`,
    DELETE_COMMENT_DIRECT: (id) => `/forum/comments/${id}`,
    DELETE_COMMENT_LEGACY: (commentId) => `/forum/comments/${commentId}`,
    BATCH_DELETE_COMMENTS: '/forum/comments/batch',
    LIKE_COMMENT: (id) => `/forum/comments/${id}/like`,
    UNLIKE_COMMENT: (id) => `/forum/comments/${id}/unlike`,

    // Tags
    GET_ALL_TAGS: '/forum/tags',
    GET_TAG_BY_ID: (id) => `/forum/tags/${id}`,
    SEARCH_TAGS: '/forum/tags/search',
    ADD_TAG: '/forum/tags',
    UPDATE_TAG: (id) => `/forum/tags/${id}`,
    DELETE_TAG: (id) => `/forum/tags/${id}`,
    BATCH_DELETE_TAGS: '/forum/tags/batch',

    // Categories
    GET_CATEGORIES: '/forum/categories',
    ADD_CATEGORY: '/forum/categories',
    UPDATE_CATEGORY: (id) => `/forum/categories/${id}`,
    DELETE_CATEGORY: (id) => `/forum/categories/${id}`,

    // Forums
    GET_AVAILABLE_FORUMS: '/forum/forums'
};

// 获取帖子列表
export function getPosts(params) {
  return request({
      url: API.GET_ALL_POSTS,
    method: 'get',
    params
  })
}

// 获取所有帖子（别名，功能同 getPosts）
export function getAllPosts(params) {
  return request({
      url: API.GET_ALL_POSTS,
    method: 'get',
    params
  })
}

// 获取帖子详情
export function getPostById(id) {
  return request({
      url: API.GET_POST_BY_ID(id),
    method: 'get'
  })
}

// 获取帖子评论
export function getPostComments(postId, params) {
  return request({
      url: API.GET_POST_COMMENTS(postId),
    method: 'get',
    params
  })
}

// 创建帖子
export function createPost(data) {
  return request({
      url: API.ADD_POST,
    method: 'post',
    data
  })
}

// 更新帖子
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

// 添加评论
export function addComment(postId, content) {
  return request({
      url: API.ADD_COMMENT_TO_POST(postId),
    method: 'post',
    data: { content }
  })
}

// 添加回复评论
export function createComment(data) {
  return request({
      url: API.ADD_COMMENT_DIRECT,
    method: 'post',
    data
  })
}

// 删除评论
export function deleteComment(commentId) {
  return request({
      url: API.DELETE_COMMENT_LEGACY(commentId),
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

// 获取热门帖子
export function getHotPosts(params) {
  return request({
      url: API.GET_HOT_POSTS,
    method: 'get',
      params
  })
}

// 获取置顶帖子
export function getTopPosts(params) {
  return request({
      url: API.GET_TOP_POSTS,
      method: 'get',
      params
  })
}

// 获取精华帖子
export function getEssencePosts(params) {
  return request({
      url: API.GET_ESSENCE_POSTS,
      method: 'get',
      params
  })
}

// 设置帖子置顶状态
export function setPostTop(id, isTop = true) {
  return request({
      url: API.SET_POST_TOP(id),
    method: 'put',
      data: {top: isTop}
  })
}

// 设置帖子精华状态
export function setPostEssence(id, isEssence = true) {
  return request({
      url: API.SET_POST_ESSENCE(id),
    method: 'put',
      data: {essence: isEssence}
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

// 获取论坛分类
export function getForumCategories(params) {
  return request({
      url: API.GET_CATEGORIES,
    method: 'get',
    params
  })
}

// 获取可用论坛
export function getAvailableForums(params) {
  return request({
      url: API.GET_AVAILABLE_FORUMS,
      method: 'get',
      params
  })
}

// 获取所有评论
export function getAllComments(userId, params) {
  return request({
      url: API.GET_COMMENTS_BY_USER(userId),
    method: 'get',
    params
  })
}

// 更新评论状态
export function updateCommentStatus(commentId, data) {
  return request({
      url: API.UPDATE_COMMENT(commentId),
    method: 'put',
    data
  })
}

// 更新帖子浏览量
export function viewPost(id) {
  return request({
      url: API.INCREMENT_VIEW(id),
    method: 'post'
  })
}

// 创建论坛分类
export function createForumCategory(data) {
  return request({
      url: API.ADD_CATEGORY,
    method: 'post',
    data
  })
}

// 更新论坛分类
export function updateForumCategory(id, data) {
  return request({
      url: API.UPDATE_CATEGORY(id),
    method: 'put',
    data
  })
}

// 删除论坛分类
export function deleteForumCategory(id) {
  return request({
      url: API.DELETE_CATEGORY(id),
      method: 'delete'
  })
}

// 获取帖子标签
export function getPostTags(postId) {
    return request({
        url: API.GET_POST_TAGS(postId),
        method: 'get'
    })
}

// 添加帖子标签
export function addTagsToPost(postId, data) {
    return request({
        url: API.ADD_TAGS_TO_POST(postId),
        method: 'post',
        data
    })
}

// 删除帖子标签
export function removeTagFromPost(postId, tagId) {
    return request({
        url: API.REMOVE_TAG_FROM_POST(postId, tagId),
    method: 'delete'
  })
}

// 获取所有标签
export function getAllTags(params) {
    return request({
        url: API.GET_ALL_TAGS,
        method: 'get',
        params
    })
}

// 获取标签详情
export function getTagById(id) {
    return request({
        url: API.GET_TAG_BY_ID(id),
        method: 'get'
    })
}

// 搜索标签
export function searchTags(params) {
    return request({
        url: API.SEARCH_TAGS,
        method: 'get',
        params
    })
}

// 添加标签
export function addTag(data) {
    return request({
        url: API.ADD_TAG,
        method: 'post',
        data
    })
}

// 更新标签
export function updateTag(id, data) {
    return request({
        url: API.UPDATE_TAG(id),
        method: 'put',
        data
    })
}

// 删除标签
export function deleteTag(id) {
    return request({
        url: API.DELETE_TAG(id),
        method: 'delete'
    })
}

// 批量删除标签
export function batchDeleteTags(ids) {
    return request({
        url: API.BATCH_DELETE_TAGS,
        method: 'delete',
        data: ids
    })
}

// 根据分类获取帖子
export function getPostsByCategory(category, params) {
    return getPosts({...params, forumType: category});
}

// 获取帖子统计
export function getPostStats(params) {
    return request({
        url: API.GET_POST_STATS,
        method: 'get',
        params
    })
}

// 更新帖子状态
export function updatePostStatus(id, status) {
    return request({
        url: API.UPDATE_POST_STATUS(id),
        method: 'put',
        data: {status}
    })
} 