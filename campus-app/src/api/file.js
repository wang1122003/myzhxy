import request from '@/utils/request'

// 文件管理 API 端点
const API = {
    UPLOAD_IMAGE: '/file/upload/image',
    UPLOAD_IMAGE_BY_TYPE: (type) => `/file/upload/image/${type}`,
    UPLOAD_ACTIVITY_POSTER: '/file/upload/activity/poster',
    UPLOAD_USER_AVATAR: (userId) => `/file/upload/avatar/${userId}`,
    UPLOAD_POST_IMAGE: '/file/upload/post/image',
    UPLOAD_DOCUMENT: '/file/upload/document',
    UPLOAD_COURSE_MATERIAL: (courseId) => `/file/upload/course/material/${courseId}`,
    DELETE_FILE: '/file/delete', // 需要在 params 或 data 中提供文件标识符 (路径/ID)
    GET_FILE_INFO: '/file/info', // 需要在 params 中提供文件标识符
    DOWNLOAD_FILE: '/file/download', // 需要在 params 或路径中提供文件标识符
    GET_TEMP_URL: '/file/temp-url', // 需要在 params 中提供文件标识符
    LIST_DIRECTORY: '/file/manager/list', // 需要在 params 中提供目录路径
    BATCH_DELETE: '/file/manager/batch-delete',
    GET_FILE_STATS: '/file/manager/stats'
};

// --- 上传函数 ---

// 通用文件上传助手
function uploadFile(url, file, fieldName = 'file') {
    const formData = new FormData();
    formData.append(fieldName, file);
    return request({
        url: url,
        method: 'post',
        data: formData,
        headers: {'Content-Type': 'multipart/form-data'}
    });
}

// 上传通用图片
export function uploadImage(file) {
    return uploadFile(API.UPLOAD_IMAGE, file);
}

// 按类型上传图片
export function uploadImageByType(file, type) {
    return uploadFile(API.UPLOAD_IMAGE_BY_TYPE(type), file);
}

// 上传活动海报 (注意: activity.js 也有 uploadActivityPoster, 需要确定归属)
export function uploadActivityPosterFile(file) {
    return uploadFile(API.UPLOAD_ACTIVITY_POSTER, file);
}

// 上传用户头像
export function uploadUserAvatar(file, userId) {
    return uploadFile(API.UPLOAD_USER_AVATAR(userId), file);
}

// 上传帖子图片
export function uploadPostImage(file) {
    return uploadFile(API.UPLOAD_POST_IMAGE, file);
}

// 上传文档
export function uploadDocument(file) {
    return uploadFile(API.UPLOAD_DOCUMENT, file);
}

// 上传课程资料
export function uploadCourseMaterial(file, courseId) {
    return uploadFile(API.UPLOAD_COURSE_MATERIAL(courseId), file);
}

// --- 文件管理函数 ---

// 删除文件 (需要后端确定如何标识文件，例如路径或ID?)
export function deleteFile(fileIdentifier) { // 例如: { filePath: 'path/to/file.jpg' } 或 { fileId: 'xyz' }
    return request({
        url: API.DELETE_FILE,
        method: 'delete', // 或 POST
        params: fileIdentifier // 如果使用带请求体的 POST/DELETE, 则为 data: fileIdentifier
    });
}

// 获取文件信息
export function getFileInfo(fileIdentifier) { // 例如: { filePath: 'path/to/file.jpg' }
    return request({
        url: API.GET_FILE_INFO,
        method: 'get',
        params: fileIdentifier
    });
}

// 下载文件 (可能直接通过 <a> 标签实现，或此函数获取下载地址)
// 此函数可能获取临时 URL 或触发下载,
// 实际下载可能通过 window.location 或 <a> 标签发生
export function downloadFile(fileIdentifier) { // 例如: { filePath: 'path/to/file.jpg' }
    // 选项 1: 获取下载 URL/令牌
    // return request({ url: API.DOWNLOAD_FILE, method: 'get', params: fileIdentifier });

    // 选项 2: 如果后端返回文件流，则直接触发下载
    // 需要 responseType: 'blob' 并处理 blob 数据
    return request({
        url: API.DOWNLOAD_FILE,
        method: 'get',
        params: fileIdentifier,
        responseType: 'blob' // 示例: 如果后端直接发送文件
    }).then(response => {
        // 处理 blob 数据 (例如, 创建对象 URL 并触发下载)
        // 占位符: 需要根据实际用例实现
        console.log('Blob received', response);
        // 下载触发示例:
        // const url = window.URL.createObjectURL(new Blob([response]));
        // const link = document.createElement('a');
        // link.href = url;
        // link.setAttribute('download', 'filename.ext'); // 提取文件名
        // document.body.appendChild(link);
        // link.click();
        // link.remove();
    });
}

// 获取文件临时访问URL
export function getTempFileUrl(fileIdentifier) { // 例如: { filePath: 'path/to/file.jpg' }
    return request({
        url: API.GET_TEMP_URL,
        method: 'get',
        params: fileIdentifier
    });
}

// 列出目录内容
export function listDirectory(params) { // 例如: { path: '/uploads/images' }
    return request({
        url: API.LIST_DIRECTORY,
        method: 'get',
        params
    });
}

// 批量删除文件
export function batchDeleteFiles(data) { // 例如: { filePaths: ['path1', 'path2'] } 或 { fileIds: ['id1', 'id2'] }
    return request({
        url: API.BATCH_DELETE,
        method: 'delete', // 或 POST
        data // 在请求体中发送标识符
    });
}

// 获取文件统计信息
export function getFileStats(params) {
    return request({
        url: API.GET_FILE_STATS,
        method: 'get',
        params
    });
}

// 获取当前用户上传的文件
export function getMyFiles(params) {
    return request({
        url: '/file/my',
        method: 'get',
        params
    });
}

// 获取教学资源列表 (后端缺失)
export function getResourceList(params) {
    return request({
        url: '/resource/list',
        method: 'get',
        params
    })
}

// 管理员管理文件 (后端缺失)
// export function adminFileList(params) {
//     return request({
//         url: '/admin/file/list',
//         method: 'get',
//         params
//     })
// }

// 管理员删除文件 (后端缺失)
// export function adminDeleteFile(id) {
//     return request({
//         url: `/admin/file/${id}`,
//         method: 'delete'
//     })
// } 