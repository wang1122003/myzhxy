package com.campus.utils;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 系统初始化工具类
 * 负责在应用启动时执行所有必要的初始化操作
 */
public class InitializationUtils {

    private static final Logger logger = LoggerFactory.getLogger(InitializationUtils.class);

    // 标记系统是否已初始化
    private static boolean initialized = false;

    /**
     * 获取系统初始化状态
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * 设置系统初始化状态
     */
    public static void setInitialized(boolean status) {
        initialized = status;
    }

    /**
     * 初始化文件上传目录
     *
     * @param basePath 基础路径
     */
    public static void initUploadDirectories(String basePath) {
        String baseDir = basePath != null && !basePath.isEmpty() ? basePath : "uploads";

        // 建立目录结构
        String imageDir = baseDir + "/images";
        String documentDir = baseDir + "/documents";
        String avatarDir = baseDir + "/avatars";
        String posterDir = baseDir + "/posters";
        String courseDir = baseDir + "/courses";
        String postDir = baseDir + "/posts";
        String tempDir = baseDir + "/temp";

        // 创建所有需要的目录
        createDirectoryIfNotExists(baseDir);
        createDirectoryIfNotExists(imageDir);
        createDirectoryIfNotExists(documentDir);
        createDirectoryIfNotExists(avatarDir);
        createDirectoryIfNotExists(posterDir);
        createDirectoryIfNotExists(courseDir);
        createDirectoryIfNotExists(postDir);
        createDirectoryIfNotExists(tempDir);

        logger.info("文件上传目录初始化完成: {}", baseDir);
    }

    /**
     * 创建目录（如果不存在）
     *
     * @param dirPath 目录路径
     */
    private static void createDirectoryIfNotExists(String dirPath) {
        // 处理Windows系统下的路径问题
        if (dirPath.startsWith("/") && dirPath.length() > 2 && dirPath.charAt(2) == ':') {
            dirPath = dirPath.substring(1); // 去掉开头的'/'
        }

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
     * 应用初始化组件
     * 在应用启动时执行必要的初始化操作
     */
    @Component
    public static class SystemInitializer implements ApplicationListener<ContextRefreshedEvent> {

        private static final Logger logger = LoggerFactory.getLogger(SystemInitializer.class);

        @Value("${file.upload.path:uploads}")
        private String uploadPath;

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            // 使用双重检查锁定模式确保只初始化一次
            if (!initialized) {
                synchronized (SystemInitializer.class) {
                    if (!initialized) {
                        logger.info("开始系统初始化...");

                        // 初始化文件上传目录
                        logger.info("初始化文件上传目录: {}", uploadPath);
                        initUploadDirectories(uploadPath);

                        // 可以在这里添加其他初始化操作

                        logger.info("系统初始化完成");
                        initialized = true;
                    }
                }
            }
        }
    }

    /**
     * Web应用初始化组件
     * 在Web容器启动时执行必要的初始化操作
     */
    public static class WebContextInitializer implements ServletContextListener {

        private static final Logger logger = LoggerFactory.getLogger(WebContextInitializer.class);

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            logger.info("Web应用初始化开始...");

            try {
                // 初始化文件上传目录
                logger.info("开始初始化文件上传目录");
                FileUtils.createDirectoryIfNotExists(FileUtils.getBaseUploadDir());
                FileUtils.createDirectoryIfNotExists(FileUtils.IMAGE_UPLOAD_DIR);
                FileUtils.createDirectoryIfNotExists(FileUtils.DOCUMENT_UPLOAD_DIR);
                FileUtils.createDirectoryIfNotExists(FileUtils.AVATAR_UPLOAD_DIR);
                FileUtils.createDirectoryIfNotExists(FileUtils.POSTER_UPLOAD_DIR);
                FileUtils.createDirectoryIfNotExists(FileUtils.COURSE_MATERIAL_DIR);
                FileUtils.createDirectoryIfNotExists(FileUtils.POST_IMAGE_DIR);
                FileUtils.createDirectoryIfNotExists(FileUtils.TEMP_DIR);
                logger.info("文件上传目录初始化完成");

                // 可以在这里添加其他初始化操作

                logger.info("Web应用初始化完成");
            } catch (Exception e) {
                logger.error("Web应用初始化失败", e);
            }
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            logger.info("Web应用正在关闭...");
            // 可以在这里添加资源清理代码
            logger.info("Web应用已关闭");
        }
    }
} 