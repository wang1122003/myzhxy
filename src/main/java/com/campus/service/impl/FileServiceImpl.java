package com.campus.service.impl;

import com.campus.service.FileService;
import com.campus.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
import java.util.stream.Stream;

/**
 * 文件服务实现类
 */
@Service
public class FileServiceImpl implements FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Assuming URLs start with "/uploads" based on spring-mvc.xml
    private static final String URL_PREFIX = "/uploads";
    // Injected base path for constructing URLs and deleting files
    @Value("${file.upload.path}")
    private String uploadBasePath; // Physical base path
    
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
        // Existing methods return relative physical path from base upload dir
        String relativeFilePath = FileUtils.uploadFile(file, courseMaterialDir);
        return relativeFilePath; // Return relative path for consistency
    }

    @Override
    public Map<String, String> uploadNoticeAttachment(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传附件不能为空");
        }
        // No specific validation for generic attachments for now

        // Upload the file using the utility method, returns relative path from base upload dir
        String relativeFilePath = FileUtils.uploadFile(file, FileUtils.NOTICE_ATTACHMENT_DIR);

        // Construct the result map
        Map<String, String> result = new HashMap<>();
        result.put("name", file.getOriginalFilename());

        // Construct the accessible URL: URL_PREFIX + path_relative_to_upload_base
        // Example: uploadBasePath = /d:/Workspace/campus/uploads
        //          relativeFilePath = uploads/notices/uuid.pdf
        //          We need to remove the 'uploads' part from relativeFilePath to get the path_relative_to_upload_base
        String pathRelativeToUploadBase = relativeFilePath.substring(FileUtils.getBaseUploadDir().length());
        // Ensure it starts with a slash and replace backslashes
        pathRelativeToUploadBase = (pathRelativeToUploadBase.startsWith("/") ? pathRelativeToUploadBase : "/" + pathRelativeToUploadBase)
                .replace("\\", "/");

        result.put("url", URL_PREFIX + pathRelativeToUploadBase); // e.g., /uploads/notices/uuid.pdf

        return result;
    }
    
    @Override
    public boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            logger.error("相对路径不能为空");
            return false;
        }

        // 构建完整物理路径
        String fullPath = FileUtils.getPhysicalPath(relativePath);

        try {
            File file = new File(fullPath);
            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    logger.info("文件删除成功: {}", relativePath);
                    return true;
                } else {
                    logger.error("文件删除失败: {}", relativePath);
                    return false;
                }
            } else {
                logger.warn("文件不存在或不是一个文件: {}", relativePath);
                return true; // 文件不存在视为删除成功
            }
        } catch (Exception e) {
            logger.error("删除文件时发生错误: {}", relativePath, e);
            return false;
        }
    }

    @Override
    public boolean deleteAll() {
        try {
            // 获取上传根目录
            String uploadDir = FileUtils.getUploadRootDir();
            File directory = new File(uploadDir);

            // 如果目录不存在，直接返回成功
            if (!directory.exists() || !directory.isDirectory()) {
                logger.warn("上传目录不存在: {}", uploadDir);
                return true;
            }

            // 递归删除所有文件和子目录
            boolean success = deleteDirectory(directory);

            if (success) {
                logger.info("成功删除所有上传的文件");
                // 删除后重新创建上传目录
                directory.mkdirs();
            } else {
                logger.error("删除所有上传文件失败");
            }

            return success;
        } catch (Exception e) {
            logger.error("删除所有文件时发生错误", e);
            return false;
        }
    }

    /**
     * 递归删除目录及其内容
     *
     * @param directory 要删除的目录
     * @return 是否删除成功
     */
    private boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        // 如果是根目录，不要删除根目录本身
        if (!directory.getAbsolutePath().equals(FileUtils.getUploadRootDir())) {
            return directory.delete();
        }
        return true;
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
            throw new IllegalArgumentException("上传图片不能为空");
        }
        if (!FileUtils.isImage(file.getOriginalFilename())) {
            throw new IllegalArgumentException("请上传合法的图片文件 (jpg, png, gif, bmp, webp)");
        }
    }

    @Override
    public void init() {
        try {
            File uploadDir = new File(uploadBasePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            logger.info("初始化文件存储目录：{}", uploadBasePath);
        } catch (Exception e) {
            logger.error("初始化存储目录失败", e);
            throw new RuntimeException("无法创建上传文件存储目录");
        }
    }

    @Override
    public String storeFile(MultipartFile file, String... extraParams) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String directory = extraParams.length > 0 ? extraParams[0] : "";
        return FileUtils.uploadFile(file, directory);
    }

    @Override
    public Path load(String filename) {
        return Paths.get(uploadBasePath).resolve(filename);
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            Path root = Paths.get(uploadBasePath);
            return Files.walk(root, 1)
                    .filter(path -> !path.equals(root))
                    .map(root::relativize);
        } catch (IOException e) {
            logger.error("读取文件目录失败", e);
            throw new RuntimeException("无法读取存储的文件", e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("无法读取文件: " + filename);
            }
        } catch (Exception e) {
            logger.error("加载文件资源失败: {}", filename, e);
            throw new RuntimeException("无法读取文件: " + filename, e);
        }
    }
} 