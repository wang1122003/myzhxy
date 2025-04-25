package com.campus.utils;

import com.campus.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 初始化工具类，用于系统启动时执行一些初始化操作
 */
@Component
public class InitializationUtils {

    @Autowired
    private StorageProperties storageProperties;

    /**
     * 初始化上传目录
     * 确保文件上传所需的目录结构存在
     */
    @PostConstruct
    public void init() {
        // 获取基础上传路径
        String basePath = storageProperties.getLocation();

        // 检查基础上传路径是否已配置
        if (basePath == null || basePath.trim().isEmpty()) {
            // 基础上传路径未配置，无法继续初始化
            return;
        }

        // 确保基础目录存在
        Path baseDir = Paths.get(basePath);
        createDirectoryIfNotExists(baseDir);

        // 创建子目录
        // 1. 用户头像目录
        createDirectoryIfNotExists(baseDir.resolve("avatars"));

        // 2. 帖子图片目录
        createDirectoryIfNotExists(baseDir.resolve("posts"));

        // 3. 课程资料目录
        createDirectoryIfNotExists(baseDir.resolve("courses"));

        // 4. 通知附件目录
        createDirectoryIfNotExists(baseDir.resolve("notifications"));

        // 5. 临时文件目录
        createDirectoryIfNotExists(baseDir.resolve("temp"));

        // 6. 校园活动图片目录
        createDirectoryIfNotExists(baseDir.resolve("activities"));

        // 7. 其他文件目录
        createDirectoryIfNotExists(baseDir.resolve("others"));
    }

    /**
     * 创建目录（如果不存在）
     *
     * @param dirPath 目录路径
     */
    private void createDirectoryIfNotExists(Path dirPath) {
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (Exception e) {
            // 创建目录失败
        }
    }
} 