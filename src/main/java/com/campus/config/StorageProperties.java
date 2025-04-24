package com.campus.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class StorageProperties {

    /**
     * 文件上传存储的根目录
     * 从属性文件中读取 'file.upload.path' 的值
     */
    @Value("${file.upload.path}")
    private String location;

} 