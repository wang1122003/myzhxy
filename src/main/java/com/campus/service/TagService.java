package com.campus.service;

import java.util.List;

import com.campus.entity.Tag;

/**
 * 标签服务接口
 */
public interface TagService {
    
    /**
     * 根据ID获取标签
     * @param id 标签ID
     * @return 标签对象
     */
    Tag getTagById(Integer id);
    
    /**
     * 获取所有标签
     * @return 标签列表
     */
    List<Tag> getAllTags();
    
    /**
     * 根据帖子ID获取标签
     * @param postId 帖子ID
     * @return 标签列表
     */
    List<Tag> getTagsByPostId(Integer postId);
    
    /**
     * 添加标签
     * @param tag 标签对象
     * @return 是否成功
     */
    boolean addTag(Tag tag);
    
    /**
     * 更新标签
     * @param tag 标签对象
     * @return 是否成功
     */
    boolean updateTag(Tag tag);
    
    /**
     * 删除标签
     * @param id 标签ID
     * @return 是否成功
     */
    boolean deleteTag(Integer id);
    
    /**
     * 批量删除标签
     * @param ids 标签ID列表
     * @return 是否成功
     */
    boolean batchDeleteTags(List<Integer> ids);
} 