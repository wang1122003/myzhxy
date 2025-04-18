package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ForumDao;
import com.campus.entity.Forum;
import com.campus.service.ForumService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 论坛板块服务实现类
 */
@Service
public class ForumServiceImpl extends ServiceImpl<ForumDao, Forum> implements ForumService {

    @Override
    public List<Forum> listAvailableForums() {
        LambdaQueryWrapper<Forum> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Forum::getStatus, 1); // 只查询状态为启用的
        wrapper.orderByAsc(Forum::getSortOrder); // 按 sortOrder 升序
        return this.list(wrapper);
    }

    // 实现其他自定义方法...
} 