package com.campus.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 文件工具类，用于处理文件上传、下载等操作
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    // 图片上传路径
    public static final String IMAGE_UPLOAD_DIR = getBaseUploadDir() + "/images";
    // 文档上传路径
    public static final String DOCUMENT_UPLOAD_DIR = getBaseUploadDir() + "/documents";
    // 用户头像上传路径
    public static final String AVATAR_UPLOAD_DIR = getBaseUploadDir() + "/avatars";
    // 活动海报上传路径
    public static final String POSTER_UPLOAD_DIR = getBaseUploadDir() + "/posters";
    // 课程材料上传路径
    public static final String COURSE_MATERIAL_DIR = getBaseUploadDir() + "/courses";
    // 帖子图片上传路径
    public static final String POST_IMAGE_DIR = getBaseUploadDir() + "/posts";
    // 临时文件路径
    public static final String TEMP_DIR = getBaseUploadDir() + "/temp";
    
    /**
     * 获取基础上传目录，通过配置系统获取
     * 注意：此方法不应直接用于文件操作，应该使用路径常量
     */
    public static String getBaseUploadDir() {
        return "uploads"; // 默认值，应与配置文件和InitializationUtils保持一致
    }
    
    /**
     * 创建目录（如果不存在）
     *
     * @param dirPath 目录路径
     */
    public static void createDirectoryIfNotExists(String dirPath) {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                logger.info("创建目录成功: {}", dirPath);
            } catch (IOException e) {
                logger.error("创建目录失败: {}", dirPath, e);
            }
        }
    }
    
    /**
     * 上传文件
     *
     * @param file 文件
     * @param directory 目标目录
     * @return 文件存储路径
     */
    public static String uploadFile(MultipartFile file, String directory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        // 创建目录
        createDirectoryIfNotExists(directory);
        
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        
        // 生成唯一文件名
        String uniqueFileName = generateUniqueFileName(originalFilename);
        
        // 构建文件保存路径
        String filePath = directory + File.separator + uniqueFileName;
        
        // 保存文件
        try {
            Path targetPath = Paths.get(filePath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("文件上传成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw e;
        }
    }
    
    /**
     * 生成唯一文件名
     *
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    public static String generateUniqueFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        
        int lastDotIndex = originalFilename.lastIndexOf(".");
        String extension = lastDotIndex != -1 ? originalFilename.substring(lastDotIndex) : "";
        return UUID.randomUUID() + extension;
    }
    
    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        Path path = Paths.get(filePath);
        try {
            Files.deleteIfExists(path);
            logger.info("文件删除成功: {}", filePath);
            return true;
        } catch (IOException e) {
            logger.error("文件删除失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 获取文件类型
     *
     * @param fileName 文件名
     * @return 文件类型
     */
    public static String getFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex != -1 ? fileName.substring(lastDotIndex + 1) : "";
    }
    
    /**
     * 获取文件大小的可读形式
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小
     */
    public static String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * 复制文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 是否复制成功
     */
    public static boolean copyFile(String sourcePath, String targetPath) {
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);
        
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            logger.info("文件复制成功: {} -> {}", sourcePath, targetPath);
            return true;
        } catch (IOException e) {
            logger.error("文件复制失败: {} -> {}", sourcePath, targetPath, e);
            return false;
        }
    }
    
    /**
     * 是否为图片文件
     *
     * @param fileName 文件名
     * @return 是否为图片
     */
    public static boolean isImage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        String fileType = getFileType(fileName).toLowerCase();
        return fileType.equals("jpg") || fileType.equals("jpeg") || 
               fileType.equals("png") || fileType.equals("gif") || 
               fileType.equals("bmp") || fileType.equals("webp");
    }
    
    /**
     * 是否为文档文件
     *
     * @param fileName 文件名
     * @return 是否为文档
     */
    public static boolean isDocument(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        String fileType = getFileType(fileName).toLowerCase();
        return fileType.equals("doc") || fileType.equals("docx") || 
               fileType.equals("pdf") || fileType.equals("xls") || 
               fileType.equals("xlsx") || fileType.equals("ppt") || 
               fileType.equals("pptx") || fileType.equals("txt");
    }
    
    /**
     * 获取文件输入流
     *
     * @param filePath 文件路径
     * @return 文件输入流
     */
    public static InputStream getFileInputStream(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.newInputStream(path);
    }
    
    /**
     * 写入输出流到文件
     *
     * @param inputStream 输入流
     * @param filePath 文件路径
     * @return 是否写入成功
     */
    public static boolean writeToFile(InputStream inputStream, String filePath) {
        Path path = Paths.get(filePath);
        
        try {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            logger.info("文件写入成功: {}", filePath);
            return true;
        } catch (IOException e) {
            logger.error("文件写入失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 生成临时访问URL
     *
     * @param filePath 文件路径
     * @param expireTime 过期时间（毫秒）
     * @return 临时访问URL
     */
    public static String generateTempUrl(String filePath, long expireTime) {
        // 实际项目中，这里应该生成带有签名的URL，可以使用JWT或其他方式
        // 简单示例，仅作为演示
        String token = UUID.randomUUID().toString();
        return "/api/file/access?path=" + filePath + "&token=" + token + "&expire=" + (System.currentTimeMillis() + expireTime);
    }
} 