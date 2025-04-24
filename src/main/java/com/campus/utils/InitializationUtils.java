package com.campus.utils;

import com.campus.config.StorageProperties;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        // 确保 basePath 不为空或 null
        if (basePath == null || basePath.trim().isEmpty()) {
            logger.error("基础上传路径 (basePath) 未配置或为空，无法初始化上传目录。");
            // 可以选择抛出异常或返回，这里选择记录错误并返回
            return;
            // 或者设置一个安全的默认值，但不推荐硬编码
            // basePath = "./default_storage"; // 示例：相对路径
            // logger.warn("基础上传路径 (basePath) 未配置，使用默认值: {}", basePath);
        }

        // 移除之前可能存在的默认值逻辑
        // String baseDir = basePath != null && !basePath.isEmpty() ? basePath : "uploads";

        // 直接使用传入的 basePath
        String baseDir = basePath.trim();

        // 建立目录结构
        String imageDir = Paths.get(baseDir, "images").toString();
        String documentDir = Paths.get(baseDir, "documents").toString();
        String avatarDir = Paths.get(baseDir, "avatars").toString();
        String posterDir = Paths.get(baseDir, "posters").toString();
        String courseDir = Paths.get(baseDir, "courses").toString();
        String postDir = Paths.get(baseDir, "posts").toString();
        String tempDir = Paths.get(baseDir, "temp").toString();
        String noticeDir = Paths.get(baseDir, "notices").toString(); // 添加之前可能遗漏的 notices 目录

        // 创建所有需要的目录
        createDirectoryIfNotExists(baseDir);
        createDirectoryIfNotExists(imageDir);
        createDirectoryIfNotExists(documentDir);
        createDirectoryIfNotExists(avatarDir);
        createDirectoryIfNotExists(posterDir);
        createDirectoryIfNotExists(courseDir);
        createDirectoryIfNotExists(postDir);
        createDirectoryIfNotExists(tempDir);
        createDirectoryIfNotExists(noticeDir); // 创建 notices 目录

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

        // 移除 @Value 注入
        // @Value("${file.upload.path:uploads}")
        // private String uploadPath;

        // 注入 StorageProperties Bean
        private final StorageProperties storageProperties;

        @Autowired // 使用构造函数注入
        public SystemInitializer(StorageProperties storageProperties) {
            this.storageProperties = storageProperties;
        }

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            // 确保只在 Root WebApplicationContext 初始化完成后执行一次
            // 避免在 Servlet Context 刷新时重复执行 (如果存在多个 Context)
            if (event.getApplicationContext().getParent() == null) {
                // 使用双重检查锁定模式确保只初始化一次
                if (!InitializationUtils.isInitialized()) {
                    synchronized (SystemInitializer.class) {
                        if (!InitializationUtils.isInitialized()) {
                            logger.info("开始系统初始化...");

                            // 从注入的 Bean 获取上传路径
                            String uploadPath = storageProperties.getLocation();
                            if (uploadPath == null || uploadPath.trim().isEmpty()) {
                                logger.error("从 StorageProperties 获取的上传路径为空，无法初始化。请检查 'file.upload.path' 配置。");
                                return; // 或者抛出异常
                            }

                            // 初始化文件上传目录
                            logger.info("初始化文件上传目录: {}", uploadPath);
                            InitializationUtils.initUploadDirectories(uploadPath);

                            // 可以在这里添加其他初始化操作

                            logger.info("系统初始化完成");
                            InitializationUtils.setInitialized(true);
                        }
                    }
                }
            }
        }
    }

    // 移除 WebContextInitializer 类
    /*
    public static class WebContextInitializer implements ServletContextListener {
        // ... 原来的代码 ...
    }
    */
} 