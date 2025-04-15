package com.campus.service.impl;

import com.campus.dao.TagMapper;
import com.campus.entity.Tag;
import com.campus.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Arrays;

/**
 * 标签服务实现类
 */
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public Tag getTagById(Integer id) {
        return tagMapper.selectTagById(id.longValue());
    }

    @Override
    public List<Tag> getAllTags() {
        return tagMapper.selectAllTags();
    }

    @Override
    public List<Tag> getTagsByPostId(Integer postId) {
        return tagMapper.selectTagsByPostId(postId.longValue());
    }

    @Override
    @Transactional
    public boolean addTag(Tag tag) {
        return tagMapper.insertTag(tag) > 0;
    }

    @Override
    @Transactional
    public boolean updateTag(Tag tag) {
        return tagMapper.updateTag(tag) > 0;
    }

    @Override
    @Transactional
    public boolean deleteTag(Integer id) {
        return tagMapper.deleteTagById(id.longValue()) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteTags(List<Integer> ids) {
        // 将Integer列表转换为Long数组
        Long[] longIds = ids.stream()
                .map(Integer::longValue)
                .toArray(Long[]::new);
        return tagMapper.batchDeleteTags(longIds) > 0;
    }
} 