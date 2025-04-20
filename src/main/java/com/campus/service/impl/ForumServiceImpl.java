package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.dao.PostDao;
import com.campus.entity.Post;
import com.campus.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 论坛板块服务实现类
 */
@Service
public class ForumServiceImpl implements ForumService {

    @Autowired
    private PostDao postDao;

    @Override
    public List<String> listAvailableForumTypes() {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT forum_type");
        queryWrapper.isNotNull("forum_type");
        queryWrapper.ne("forum_type", "");

        // 使用 selectObjs 直接获取 forum_type 列表 (类型为 Object)
        List<Object> forumTypeObjects = postDao.selectObjs(queryWrapper);

        // 将 Object 列表转换为 String 列表
        return forumTypeObjects.stream()
                .filter(obj -> obj != null) // 过滤 null 值
                .map(String::valueOf) // 转换为 String
                .distinct() // 确保去重
                .collect(Collectors.toList());
    }

    // 实现其他自定义方法...
} 