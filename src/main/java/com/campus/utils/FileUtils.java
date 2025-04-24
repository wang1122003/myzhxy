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
 * 文件工具类，用于处理文件上传、下载等操作 (无状态)
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 创建目录（如果不存在）
     *
     * @param dirPath 目录路径 (应为完整绝对路径)
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
     * @param targetDirectory 目标目录 (应为完整绝对路径)
     * @return 保存后的文件的完整绝对路径
     */
    public static String uploadFile(MultipartFile file, String targetDirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (targetDirectory == null || targetDirectory.trim().isEmpty()) {
            throw new IllegalArgumentException("目标目录不能为空");
        }

        Path directoryPath = Paths.get(targetDirectory);

        createDirectoryIfNotExists(targetDirectory);

        String originalFilename = file.getOriginalFilename();

        String uniqueFileName = generateUniqueFileName(originalFilename);

        Path filePath = directoryPath.resolve(uniqueFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("文件上传成功: {}", filePath.toString());
            return filePath.toString();
        } catch (IOException e) {
            logger.error("文件上传失败: {}", filePath.toString(), e);
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
        String nameWithoutExtension = lastDotIndex != -1 ? originalFilename.substring(0, lastDotIndex) : originalFilename;
        return UUID.randomUUID() + extension;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件的完整绝对路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        Path path = Paths.get(filePath);
        try {
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                logger.info("文件删除成功: {}", filePath);
            } else {
                logger.warn("尝试删除文件但文件不存在: {}", filePath);
            }
            return deleted;
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
     * @param sourcePath 源文件完整绝对路径
     * @param targetPath 目标文件完整绝对路径
     * @return 是否复制成功
     */
    public static boolean copyFile(String sourcePath, String targetPath) {
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        try {
            Path targetDir = target.getParent();
            if (targetDir != null && !Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
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
               fileType.equals("pdf") || fileType.equals("txt") ||
               fileType.equals("xls") || fileType.equals("xlsx") ||
               fileType.equals("ppt") || fileType.equals("pptx");
    }

    /**
     * 从完整路径获取文件名
     * @param filePath 完整文件路径
     * @return 文件名，如果路径无效则返回null
     */
    public static String getFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        try {
            Path path = Paths.get(filePath);
            return path.getFileName().toString();
        } catch (Exception e) {
            logger.error("从路径提取文件名失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 安全地读取文件输入流
     * @param filePath 完整文件路径
     * @return InputStream, 如果文件不存在或不可读则返回null
     * @throws IOException 如果发生IO错误
     */
    public static InputStream getFileInputStream(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        Path path = Paths.get(filePath);
        if (Files.exists(path) && Files.isReadable(path)) {
            return Files.newInputStream(path);
        } else {
            logger.warn("尝试读取文件但文件不存在或不可读: {}", filePath);
            return null;
        }
    }

    /**
     * 将输入流写入文件
     * @param inputStream 输入流 (此方法会关闭输入流)
     * @param filePath 目标文件的完整路径
     * @return 是否写入成功
     */
    public static boolean writeToFile(InputStream inputStream, String filePath) {
        if (inputStream == null || filePath == null || filePath.isEmpty()) {
            return false;
        }
        Path targetPath = Paths.get(filePath);
        Path parentDir = targetPath.getParent();
        if (parentDir != null) {
            createDirectoryIfNotExists(parentDir.toString());
        }

        try {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("输入流成功写入文件: {}", filePath);
            return true;
        } catch (IOException e) {
            logger.error("将输入流写入文件失败: {}", filePath, e);
            return false;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("关闭输入流失败", e);
            }
        }
    }
} 