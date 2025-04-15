package com.campus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * 属性配置类
 * 用于确保application.properties文件被正确加载
 */
@Configuration
@PropertySource("classpath:application.properties")
public class PropertySourceConfig {

    /**
     * 确保@Value注解能够正确解析占位符
     * @return PropertySourcesPlaceholderConfigurer实例
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
} 