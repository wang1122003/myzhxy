import request from '@/utils/request'

// 论坛相关 API 端点 (帖子, 评论, 标签, 分类)
const API = {
    // 帖子相关
    GET_ALL_POSTS: '/forum/posts', // 获取所有帖子 (分页)
    GET_POST_BY_ID: (id) => `/forum/posts/${id}`, // 根据 ID 获取帖子详情
    GET_POSTS_BY_USER: (userId) => `/forum/posts/user/${userId}`, // 获取指定用户的帖子
    GET_MY_POSTS: '/forum/posts/my', // 获取当前登录用户的帖子
    SEARCH_POSTS: '/forum/posts/search', // 搜索帖子
    ADD_POST: '/forum/posts', // 添加新帖子
    UPDATE_POST: (id) => `/forum/posts/${id}`, // 更新指定帖子
    DELETE_POST: (id) => `/forum/posts/${id}`, // 删除指定帖子
    GET_HOT_POSTS: '/forum/posts/hot', // 获取热门帖子
    INCREMENT_VIEW: (id) => `/forum/posts/${id}/view`, // 增加帖子浏览量
    LIKE_POST: (id) => `/forum/posts/${id}/like`, // 点赞帖子
    UNLIKE_POST: (id) => `/forum/posts/${id}/unlike`, // 取消点赞帖子
    // SET_POST_TOP: (id) => `/forum/posts/${id}/top`, // 设置/取消置顶 (假设接口)
    // SET_POST_ESSENCE: (id) => `/forum/posts/${id}/essence`, // 设置/取消精华 (假设接口)
    // GET_TOP_POSTS: '/forum/posts/top', // 获取置顶帖子 (假设接口)
    // GET_ESSENCE_POSTS: '/forum/posts/essence', // 获取精华帖子 (假设接口)
    // UPDATE_POST_STATUS: (id) => `/forum/posts/${id}/status`, // 更新帖子状态 (假设接口)

    // 评论相关
    GET_COMMENT: (id) => `/forum/comments/${id}`, // 获取单个评论
    GET_COMMENTS_BY_USER: (userId) => `/forum/comments/user/${userId}`, // 获取用户的评论
    GET_ROOT_COMMENTS: (postId) => `/forum/posts/${postId}/root-comments`, // 获取帖子的根评论 (分页)
    GET_CHILD_COMMENTS: (parentId) => `/forum/comments/${parentId}/children`, // 获取子评论 (分页)
    ADD_COMMENT_DIRECT: '/forum/comments', // 直接添加评论 (需要 postId 和 parentId)
    // ADD_COMMENT_TO_POST: (postId) => `/forum/posts/${postId}/comments`, // 向帖子添加根评论 (旧接口?)
    UPDATE_COMMENT: (id) => `/forum/comments/${id}`, // 更新评论
    DELETE_COMMENT_DIRECT: (id) => `/forum/comments/${id}`, // 直接删除评论
    DELETE_COMMENT_LEGACY: (commentId) => `/forum/comments/${commentId}`, // 旧版删除评论接口?
    BATCH_DELETE_COMMENTS: '/forum/comments/batch', // 批量删除评论
    LIKE_COMMENT: (id) => `/forum/comments/${id}/like`, // 点赞评论
    UNLIKE_COMMENT: (id) => `/forum/comments/${id}/unlike`, // 取消点赞评论
    // UPDATE_COMMENT_STATUS: (id) => `/forum/comments/${id}/status`, // 更新评论状态 (假设接口)

    // 标签相关
    GET_ALL_TAGS: '/forum/tags', // 获取所有标签
    GET_TAG_BY_ID: (id) => `/forum/tags/${id}`, // 根据 ID 获取标签
    SEARCH_TAGS: '/forum/tags/search', // 搜索标签
    ADD_TAG: '/forum/tags', // 添加标签
    UPDATE_TAG: (id) => `/forum/tags/${id}`, // 更新标签
    DELETE_TAG: (id) => `/forum/tags/${id}`, // 删除标签
    BATCH_DELETE_TAGS: '/forum/tags/batch', // 批量删除标签
    // GET_POST_TAGS: (postId) => `/forum/posts/${postId}/tags`, // 获取帖子的标签 (假设接口)
    // ADD_TAGS_TO_POST: (postId) => `/forum/posts/${postId}/tags`, // 给帖子添加标签 (假设接口)
    // REMOVE_TAG_FROM_POST: (postId, tagId) => `/forum/posts/${postId}/tags/${tagId}`, // 从帖子移除标签 (假设接口)

    // 分类 (或板块) 相关
    GET_CATEGORIES: '/forum/categories', // 获取所有分类/板块
    ADD_CATEGORY: '/forum/categories', // 添加分类/板块
    UPDATE_CATEGORY: (id) => `/forum/categories/${id}`, // 更新分类/板块
    DELETE_CATEGORY: (id) => `/forum/categories/${id}`, // 删除分类/板块

    // 论坛整体 (可能重叠或未使用)
    GET_AVAILABLE_FORUMS: '/forum/forums' // 获取可用的论坛/板块列表?
};

// --- 帖子 API ---

// 获取帖子列表 (分页, 过滤)
export function getPosts(params) {
    return request({
        url: API.GET_ALL_POSTS,
        method: 'get',
        params
    })
}

// 获取所有帖子 (getPosts 的别名，功能相同)
export function getAllPosts(params) {
    return getPosts(params); // 直接调用 getPosts
}

// 获取帖子详情
export function getPostById(id) {
    return request({
        url: API.GET_POST_BY_ID(id),
        method: 'get'
    })
}

// 获取帖子的评论 (根评论分页)
export function getRootComments(postId, params) {
    return request({
        url: API.GET_ROOT_COMMENTS(postId),
        method: 'get',
        params // 分页参数
    })
}

// 获取子评论 (分页)
export function getChildComments(parentId, params) {
    return request({
        url: API.GET_CHILD_COMMENTS(parentId),
        method: 'get',
        params // 分页参数
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

// 点赞帖子
export function likePost(id) {
    return request({
        url: API.LIKE_POST(id),
        method: 'post'
    })
}

// 取消点赞帖子
export function cancelLikePost(id) {
    // 注意：接口文档是 DELETE，但之前的实现是 POST，以接口文档为准，改为 DELETE
    return request({
        url: API.UNLIKE_POST(id),
        method: 'delete' // 改为 DELETE
    })
}

// --- 评论 API ---

// 添加评论 (通用，需要 postId 和 parentId)
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
        data // data 通常包含 content
    })
}

// 删除评论
export function deleteComment(commentId) {
    return request({
        url: API.DELETE_COMMENT_DIRECT(commentId), // 使用直接删除接口
        method: 'delete'
    })
}

// 批量删除评论
export function batchDeleteComments(ids) {
    return request({
        url: API.BATCH_DELETE_COMMENTS,
        method: 'post', // 或 delete，取决于后端设计
        data: {ids} // 将 ID 数组放在请求体中
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
    // 接口文档是 POST，保持 POST
    return request({
        url: API.UNLIKE_COMMENT(commentId),
        method: 'post' // 接口文档指定为 POST
    })
}

// --- 其他论坛 API ---

// 获取热门帖子
export function getHotPosts(params) {
    return request({
        url: API.GET_HOT_POSTS,
        method: 'get',
        params
    })
}

// 获取置顶帖子 (需要后端支持)
// export function getTopPosts(params) {
//   return request({
//     url: API.GET_TOP_POSTS,
//     method: 'get',
//     params
//   })
// }

// 获取精华帖子 (需要后端支持)
// export function getEssencePosts(params) {
//   return request({
//     url: API.GET_ESSENCE_POSTS,
//     method: 'get',
//     params
//   })
// }

// 设置帖子置顶状态 (需要后端支持)
// export function setPostTop(id, isTop = true) {
//   return request({
//     url: API.SET_POST_TOP(id),
//     method: 'put',
//     data: { top: isTop }
//   })
// }

// 设置帖子精华状态 (需要后端支持)
// export function setPostEssence(id, isEssence = true) {
//   return request({
//     url: API.SET_POST_ESSENCE(id),
//     method: 'put',
//     data: { essence: isEssence }
//   })
// }

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

// 获取论坛分类/板块列表
export function getForumCategories(params) {
    return request({
        url: API.GET_CATEGORIES,
        method: 'get',
        params
    })
}

// 获取可用的论坛/板块列表 (与 getForumCategories 可能重复?)
export function getAvailableForums(params) {
    return request({
        url: API.GET_AVAILABLE_FORUMS,
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

// 更新评论状态 (需要后端支持)
// export function updateCommentStatus(commentId, data) {
//   return request({
//     url: API.UPDATE_COMMENT_STATUS(commentId),
//     method: 'put',
//     data // e.g., { status: 'APPROVED' }
//   })
// }

// 增加帖子浏览量
export function incrementViewCount(id) {
    return request({
        url: API.INCREMENT_VIEW(id),
        method: 'post' // 通常用 POST 避免缓存问题
    })
}

// --- 分类/板块 API ---

// 创建分类/板块
export function createForumCategory(data) {
    return request({
        url: API.ADD_CATEGORY,
        method: 'post',
        data
    })
}

// 更新分类/板块
export function updateForumCategory(id, data) {
    return request({
        url: API.UPDATE_CATEGORY(id),
        method: 'put',
        data
    })
}

// 删除分类/板块
export function deleteForumCategory(id) {
    return request({
        url: API.DELETE_CATEGORY(id),
        method: 'delete'
    })
}

// --- 标签 API ---

// 获取帖子的标签 (需要后端支持)
// export function getPostTags(postId) {
//   return request({
//     url: API.GET_POST_TAGS(postId),
//     method: 'get'
//   })
// }

// 给帖子添加标签 (需要后端支持)
// export function addTagsToPost(postId, data) {
//   return request({
//     url: API.ADD_TAGS_TO_POST(postId),
//     method: 'post',
//     data // e.g., { tagIds: [1, 2] } or { tags: ['tag1', 'tag2'] }
//   })
// }

// 从帖子移除标签 (需要后端支持)
// export function removeTagFromPost(postId, tagId) {
//   return request({
//     url: API.REMOVE_TAG_FROM_POST(postId, tagId),
//     method: 'delete'
//   })
// }

// 获取所有标签
export function getAllTags(params) {
    return request({
        url: API.GET_ALL_TAGS,
        method: 'get',
        params
    })
}

// 根据 ID 获取标签
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
        method: 'post', // 或 delete
        data: {ids}
    })
}

// --- 其他辅助 API ---

// 获取指定分类下的帖子 (如果需要)
// export function getPostsByCategory(category, params) {
//   return request({
//     url: `/forum/categories/${category}/posts`, // 假设的端点
//     method: 'get',
//     params
//   })
// }

// 获取帖子统计信息 (如果需要)
// export function getPostStats(params) {
//   return request({
//     url: '/forum/posts/stats', // 假设的端点
//     method: 'get',
//     params
//   })
// }

// 更新帖子状态 (如果需要单独接口)
// export function updatePostStatus(id, status) {
//   return request({
//     url: API.UPDATE_POST_STATUS(id),
//     method: 'put',
//     data: { status }
//   })
// } 