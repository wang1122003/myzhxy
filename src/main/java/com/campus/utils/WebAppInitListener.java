package com.campus.utils;

import com.campus.service.FileService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Web应用初始化监听器
 * 在Web应用启动时执行初始化操作
 */
@WebListener
public class WebAppInitListener implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(WebAppInitListener.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Web应用初始化开始...");
        
        try {
            // 获取Spring应用上下文
            WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
            
            if (ctx != null) {
                // 初始化文件服务
                FileService fileService = ctx.getBean(FileService.class);
                if (fileService != null) {
                    logger.info("开始初始化文件上传目录");
                    fileService.init();
                    logger.info("文件上传目录初始化完成");
                }
                
                // 可以在这里添加其他初始化操作
            }
            
            logger.info("Web应用初始化完成");
        } catch (Exception e) {
            logger.error("Web应用初始化失败", e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Web应用关闭");
    }
} 