package com.campus.config;

import com.campus.service.FileService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置类
 * 负责在应用启动时初始化文件存储目录
 */
@Configuration
public class FileStorageConfig implements InitializingBean {

    @Autowired
    private FileService fileService;
    
    /**
     * 应用启动时初始化文件目录
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        fileService.init();
    }
} 