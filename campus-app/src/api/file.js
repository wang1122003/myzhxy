import request from '@/utils/request'

// API Endpoints for File Management
const API = {
    UPLOAD_IMAGE: '/file/upload/image',
    UPLOAD_IMAGE_BY_TYPE: (type) => `/file/upload/image/${type}`,
    UPLOAD_ACTIVITY_POSTER: '/file/upload/activity/poster',
    UPLOAD_USER_AVATAR: (userId) => `/file/upload/avatar/${userId}`,
    UPLOAD_POST_IMAGE: '/file/upload/post/image',
    UPLOAD_DOCUMENT: '/file/upload/document',
    UPLOAD_COURSE_MATERIAL: (courseId) => `/file/upload/course/material/${courseId}`,
    DELETE_FILE: '/file/delete', // Needs file identifier (path/id) in params or data
    GET_FILE_INFO: '/file/info', // Needs file identifier in params
    DOWNLOAD_FILE: '/file/download', // Needs file identifier in params or path
    GET_TEMP_URL: '/file/temp-url', // Needs file identifier in params
    LIST_DIRECTORY: '/file/manager/list', // Needs directory path in params
    BATCH_DELETE: '/file/manager/batch-delete',
    GET_FILE_STATS: '/file/manager/stats'
};

// --- Upload Functions ---

// Generic file upload helper
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

// 上传活动海报 (Note: activity.js also has uploadActivityPoster, decide where it belongs)
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

// --- File Management Functions ---

// 删除文件 (需要后端确定如何标识文件，路径或ID?)
export function deleteFile(fileIdentifier) { // e.g., { filePath: 'path/to/file.jpg' } or { fileId: 'xyz' }
    return request({
        url: API.DELETE_FILE,
        method: 'delete', // Or POST
        params: fileIdentifier // Or data: fileIdentifier if using POST/DELETE with body
    });
}

// 获取文件信息
export function getFileInfo(fileIdentifier) { // e.g., { filePath: 'path/to/file.jpg' }
    return request({
        url: API.GET_FILE_INFO,
        method: 'get',
        params: fileIdentifier
    });
}

// 下载文件 (可能直接通过 <a> 标签实现，或此函数获取下载地址)
// This function likely gets a temporary URL or triggers download, 
// actual download might happen via window.location or <a> tag
export function downloadFile(fileIdentifier) { // e.g., { filePath: 'path/to/file.jpg' }
    // Option 1: Get download URL/token
    // return request({ url: API.DOWNLOAD_FILE, method: 'get', params: fileIdentifier }); 

    // Option 2: Trigger download directly if backend returns file stream
    // Requires responseType: 'blob' and handling the blob data
    return request({
        url: API.DOWNLOAD_FILE,
        method: 'get',
        params: fileIdentifier,
        responseType: 'blob' // Example if backend sends file directly
    }).then(response => {
        // Process blob data (e.g., create Object URL and trigger download)
        // Placeholder: needs implementation based on actual use case
        console.log('Blob received', response);
        // Example download trigger:
        // const url = window.URL.createObjectURL(new Blob([response]));
        // const link = document.createElement('a');
        // link.href = url;
        // link.setAttribute('download', 'filename.ext'); // Extract filename
        // document.body.appendChild(link);
        // link.click();
        // link.remove();
    });
}

// 获取文件临时访问URL
export function getTempFileUrl(fileIdentifier) { // e.g., { filePath: 'path/to/file.jpg' }
    return request({
        url: API.GET_TEMP_URL,
        method: 'get',
        params: fileIdentifier
    });
}

// 列出目录内容
export function listDirectory(params) { // e.g., { path: '/uploads/images' }
    return request({
        url: API.LIST_DIRECTORY,
        method: 'get',
        params
    });
}

// 批量删除文件
export function batchDeleteFiles(data) { // e.g., { filePaths: ['path1', 'path2'] } or { fileIds: ['id1', 'id2'] }
    return request({
        url: API.BATCH_DELETE,
        method: 'delete', // Or POST
        data // Send identifiers in request body
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