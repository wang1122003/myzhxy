package com.campus.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 应用程序配置类
 * 用于集中管理应用程序的配置信息和组件扫描
 */
@Configuration
@ComponentScan({"com.campus.service", "com.campus.config"})
@PropertySource({"classpath:application.properties", "classpath:jdbc.properties"})
public class AppConfig {
    // 配置Bean可以添加在这里
} 