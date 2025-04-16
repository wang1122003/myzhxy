package com.campus.service.impl;

import com.campus.service.FileService;
import com.campus.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件服务实现类
 */
@Service
public class FileServiceImpl implements FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        return FileUtils.uploadFile(file, FileUtils.IMAGE_UPLOAD_DIR);
    }
    
    @Override
    public String uploadImage(MultipartFile file, String type) throws IOException {
        validateImageFile(file);
        String directory;
        
        switch (type.toLowerCase()) {
            case "avatar":
                directory = FileUtils.AVATAR_UPLOAD_DIR;
                break;
            case "poster":
                directory = FileUtils.POSTER_UPLOAD_DIR;
                break;
            case "post":
                directory = FileUtils.POST_IMAGE_DIR;
                break;
            default:
                directory = FileUtils.IMAGE_UPLOAD_DIR + "/" + type;
                // 确保目录存在
                FileUtils.createDirectoryIfNotExists(directory);
        }
        
        return FileUtils.uploadFile(file, directory);
    }
    
    @Override
    public String uploadActivityPoster(MultipartFile file) throws IOException {
        validateImageFile(file);
        return FileUtils.uploadFile(file, FileUtils.POSTER_UPLOAD_DIR);
    }
    
    @Override
    public String uploadUserAvatar(MultipartFile file, Long userId) throws IOException {
        validateImageFile(file);
        String userAvatarDir = FileUtils.AVATAR_UPLOAD_DIR + "/" + userId;
        FileUtils.createDirectoryIfNotExists(userAvatarDir);
        return FileUtils.uploadFile(file, userAvatarDir);
    }
    
    @Override
    public String uploadPostImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        return FileUtils.uploadFile(file, FileUtils.POST_IMAGE_DIR);
    }
    
    @Override
    public String uploadDocument(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        if (!FileUtils.isDocument(file.getOriginalFilename())) {
            throw new IllegalArgumentException("请上传合法的文档文件");
        }
        
        return FileUtils.uploadFile(file, FileUtils.DOCUMENT_UPLOAD_DIR);
    }
    
    @Override
    public String uploadCourseMaterial(MultipartFile file, Long courseId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        String courseMaterialDir = FileUtils.COURSE_MATERIAL_DIR + "/" + courseId;
        FileUtils.createDirectoryIfNotExists(courseMaterialDir);
        return FileUtils.uploadFile(file, courseMaterialDir);
    }
    
    @Override
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        return FileUtils.deleteFile(filePath);
    }
    
    @Override
    public String getFileInfo(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }
        
        try {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("name", file.getName());
            fileInfo.put("path", filePath);
            fileInfo.put("size", file.length());
            fileInfo.put("readableSize", FileUtils.getReadableFileSize(file.length()));
            fileInfo.put("type", FileUtils.getFileType(file.getName()));
            fileInfo.put("lastModified", file.lastModified());
            fileInfo.put("isDirectory", file.isDirectory());
            
            return objectMapper.writeValueAsString(fileInfo);
        } catch (Exception e) {
            logger.error("获取文件信息失败", e);
            throw new RuntimeException("获取文件信息失败");
        }
    }
    
    @Override
    public boolean downloadFile(String filePath, OutputStream outputStream) {
        if (filePath == null || filePath.isEmpty() || outputStream == null) {
            return false;
        }
        
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                logger.error("下载文件失败，文件不存在: {}", filePath);
                return false;
            }
            
            Files.copy(path, outputStream);
            logger.info("文件下载成功: {}", filePath);
            return true;
        } catch (IOException e) {
            logger.error("文件下载失败: {}", filePath, e);
            return false;
        }
    }
    
    @Override
    public String generateTempUrl(String filePath, long expireTime) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }
        
        return FileUtils.generateTempUrl(filePath, expireTime);
    }
    
    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        if (!FileUtils.isImage(file.getOriginalFilename())) {
            throw new IllegalArgumentException("请上传合法的图片文件");
        }
    }
} 