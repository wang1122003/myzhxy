package com.campus.config;

import com.campus.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 应用初始化配置
 * 在应用启动时执行必要的初始化操作
 */
@Component
public class AppInitializer implements ApplicationListener<ContextRefreshedEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(AppInitializer.class);
    
    @Autowired
    private FileService fileService;
    
    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 初始化文件上传目录
        logger.info("初始化文件上传目录: {}", uploadPath);
        fileService.init();
        
        // 其他系统初始化操作可以在这里添加
        
        logger.info("系统初始化完成");
    }
} 