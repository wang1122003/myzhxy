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

        List<Post> posts = postDao.selectList(queryWrapper);

        return posts.stream()
                .map(Post::getForumType)
                .distinct()
                .collect(Collectors.toList());
    }

    // 实现其他自定义方法...
} 