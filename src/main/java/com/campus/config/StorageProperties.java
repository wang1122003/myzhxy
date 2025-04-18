package com.campus.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
// 移除 @Component 注解，将在 XML 中显式定义 Bean
// @Component 
public class StorageProperties {

    /**
     * 文件上传存储的根目录
     * 从属性文件中读取 'storage.location' 的值，如果未定义则默认为 "upload-dir"
     */
    @Value("${storage.location:upload-dir}") // 使用 @Value 注入，并提供默认值
    private String location;

} 