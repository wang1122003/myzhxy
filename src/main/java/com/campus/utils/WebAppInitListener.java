package com.campus.utils;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web应用初始化监听器
 * 已废弃，初始化逻辑已移至SecurityUtils$AppInitializer
 * 仅保留此类以兼容现有日志
 */
public class WebAppInitListener implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(WebAppInitListener.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Web应用初始化开始...");
        logger.info("Web应用初始化已被禁用，使用SecurityUtils$AppInitializer代替");
        logger.info("Web应用初始化完成");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 不执行任何操作
    }
} 