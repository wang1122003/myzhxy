package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Forum;

import java.util.List;

/**
 * 论坛板块服务接口
 */
public interface ForumService extends IService<Forum> {

    /**
     * 获取所有启用的板块列表，按 sortOrder 排序
     *
     * @return 板块列表
     */
    List<Forum> listAvailableForums();

    // 可根据需要添加其他业务方法，例如：
    // Forum findByName(String name);
    // boolean updateStatus(Long id, Integer status);
} 