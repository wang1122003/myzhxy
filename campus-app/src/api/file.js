import request from './request'
import {FILE_API} from './api-endpoints'

// 获取文件列表
export function getFileList(params) {
    return request({
        url: '/file/list',
        method: 'get',
        params
    })
}

// 上传文件
export function uploadFile(data) {
    return request({
        url: '/file/upload',
        method: 'post',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data
    })
}

// 下载文件
export function downloadFile(id) {
    return request({
        url: `/file/download/${id}`,
        method: 'get',
        responseType: 'blob'
    })
}

// 删除文件
export function deleteFile(id) {
    return request({
        url: `/file/${id}`,
        method: 'delete'
    })
}

// 获取教学资源列表
export function getResourceList(params) {
    return request({
        url: '/resource/list',
        method: 'get',
        params
    })
}

// 管理员管理文件
export function adminFileList(params) {
    return request({
        url: '/admin/file/list',
        method: 'get',
        params
    })
}

// 管理员删除文件
export function adminDeleteFile(id) {
    return request({
        url: `/admin/file/${id}`,
        method: 'delete'
    })
}

// 上传图片
export function uploadImage(data) {
    return request({
        url: FILE_API.UPLOAD_IMAGE,
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 按类型上传图片
export function uploadImageByType(type, data) {
    return request({
        url: FILE_API.UPLOAD_IMAGE_BY_TYPE.replace(':type', type),
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 上传活动海报
export function uploadActivityPoster(data) {
    return request({
        url: FILE_API.UPLOAD_ACTIVITY_POSTER,
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 上传用户头像
export function uploadUserAvatar(userId, data) {
    return request({
        url: FILE_API.UPLOAD_USER_AVATAR.replace(':userId', userId),
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 上传帖子图片
export function uploadPostImage(data) {
    return request({
        url: FILE_API.UPLOAD_POST_IMAGE,
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 上传文档
export function uploadDocument(data) {
    return request({
        url: FILE_API.UPLOAD_DOCUMENT,
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 上传课程资料
export function uploadCourseMaterial(courseId, data) {
    return request({
        url: FILE_API.UPLOAD_COURSE_MATERIAL.replace(':courseId', courseId),
        method: 'post',
        data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 获取文件信息
export function getFileInfo(filePath) {
    return request({
        url: FILE_API.GET_FILE_INFO,
        method: 'get',
        params: {filePath}
    })
}

// 下载文件（新）
export function downloadFileByPath(filePath) {
    return request({
        url: FILE_API.DOWNLOAD_FILE,
        method: 'get',
        params: {filePath},
        responseType: 'blob'
    })
}

// 获取临时访问URL
export function getTempUrl(filePath, expireTime) {
    return request({
        url: FILE_API.GET_TEMP_URL,
        method: 'get',
        params: {
            filePath,
            expireTime
        }
    })
}

// =============== 从文件管理器合并的API ===============

// 获取目录结构
export function listDirectory(directory = '') {
    return request({
        url: FILE_API.LIST_DIRECTORY,
        method: 'get',
        params: {directory}
    })
}

// 批量删除文件
export function batchDeleteFiles(fileIds) {
    return request({
        url: FILE_API.BATCH_DELETE,
        method: 'delete',
        data: fileIds
    })
}

// 获取文件统计信息
export function getFileStats() {
    return request({
        url: FILE_API.GET_FILE_STATS,
        method: 'get'
    })
} 