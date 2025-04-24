package com.campus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 到本地文件系统
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:${file.upload.path}/").setCachePeriod(3600);
        // 其他静态资源可按需添加
    }
}