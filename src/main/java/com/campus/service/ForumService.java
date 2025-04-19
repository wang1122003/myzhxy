package com.campus.service;

import java.util.List;

/**
 * 论坛板块服务接口
 */
public interface ForumService {

    /**
     * 获取所有可用的论坛类型列表
     *
     * @return 论坛类型字符串列表
     */
    List<String> listAvailableForumTypes();

    // 可根据需要添加其他业务方法，例如：
    // Forum findByName(String name);
    // boolean updateStatus(Long id, Integer status);
} 