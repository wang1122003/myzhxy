package com.campus.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Tag;

import java.util.List;

/**
 * 标签数据访问接口
 */
@Repository
public interface TagMapper extends BaseMapper<Tag> {
    
    /**
     * 根据ID查询标签
     * @param id 标签ID
     * @return 标签对象
     */
    Tag selectTagById(Long id);
    
    /**
     * 查询所有标签
     * @return 标签列表
     */
    List<Tag> selectAllTags();
    
    /**
     * 根据帖子ID查询标签
     * @param postId 帖子ID
     * @return 标签列表
     */
    List<Tag> selectTagsByPostId(Long postId);
    
    /**
     * 插入新标签
     * @param tag 标签对象
     * @return 影响行数
     */
    int insertTag(Tag tag);
    
    /**
     * 更新标签
     * @param tag 标签对象
     * @return 影响行数
     */
    int updateTag(Tag tag);
    
    /**
     * 根据ID删除标签
     * @param id 标签ID
     * @return 影响行数
     */
    int deleteTagById(Long id);
    
    /**
     * 批量删除标签
     * @param ids 标签ID数组
     * @return 影响行数
     */
    int batchDeleteTags(@Param("ids") Long[] ids);
} 