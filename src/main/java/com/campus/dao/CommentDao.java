package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Comment;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 论坛评论数据访问接口
 */
@Repository
public interface CommentDao extends BaseMapper<Comment> {
    /**
     * 根据ID查询评论
     * @param id 评论ID
     * @return 评论对象
     */
    Comment findById(Long id);
    
    /**
     * 查询所有评论
     * @return 评论列表
     */
    List<Comment> findAll();
    
    /**
     * 根据帖子ID查询评论
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Comment> findByPostId(Long postId);
    
    /**
     * 根据作者ID查询评论
     * @param authorId 作者ID
     * @return 评论列表
     */
    List<Comment> findByAuthorId(Long authorId);
    
    /**
     * 根据用户ID查询评论
     * @param userId 用户ID
     * @return 评论列表
     */
    List<Comment> findByUserId(Long userId);
    
    /**
     * 根据父评论ID查询回复
     * @param parentId 父评论ID
     * @return 评论列表
     */
    List<Comment> findByParentId(Long parentId);
    
    /**
     * 根据帖子ID和父评论ID查询回复
     * @param postId 帖子ID
     * @param parentId 父评论ID
     * @return 评论列表
     */
    List<Comment> findByPostIdAndParentId(@Param("postId") Long postId, @Param("parentId") Long parentId);
    
    /**
     * 根据帖子ID查询根评论（父评论ID为null的评论）
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Comment> findRootCommentsByPostId(Long postId);
    
    /**
     * 根据作者ID和帖子ID查询评论
     * @param authorId 作者ID
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Comment> findByAuthorIdAndPostId(@Param("authorId") Long authorId, @Param("postId") Long postId);
    
    /**
     * 分页查询评论
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 评论列表
     */
    List<Comment> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 获取评论总数
     * @return 评论数量
     */
    int getCount();
    
    /**
     * 添加评论
     * @param comment 评论对象
     * @return 影响行数
     */
    int insert(Comment comment);
    
    /**
     * 更新评论
     * @param comment 评论对象
     * @return 影响行数
     */
    int update(Comment comment);
    
    /**
     * 删除评论
     * @param id 评论ID
     * @return 影响行数
     */
    int delete(Long id);
    
    /**
     * 根据帖子ID删除评论
     * @param postId 帖子ID
     * @return 影响行数
     */
    int deleteByPostId(Long postId);
    
    /**
     * 批量删除评论
     * @param ids 评论ID数组
     * @return 影响行数
     */
    int batchDelete(Long[] ids);
    
    /**
     * 更新评论状态
     * @param id 评论ID
     * @param status 评论状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 更新评论点赞数
     * @param id 评论ID
     * @param likeCount 点赞数
     * @return 影响行数
     */
    int updateLikeCount(@Param("id") Long id, @Param("likeCount") Integer likeCount);
    
    /**
     * 更新评论回复数
     * @param id 评论ID
     * @param replyCount 回复数
     * @return 影响行数
     */
    int updateReplyCount(@Param("id") Long id, @Param("replyCount") Integer replyCount);
    
    /**
     * 获取帖子评论数
     * @param postId 帖子ID
     * @return 评论数量
     */
    int getPostCommentCount(Long postId);
    
    /**
     * 获取用户评论数
     * @param userId 用户ID
     * @return 评论数量
     */
    int getUserCommentCount(Long userId);
    
    /**
     * 获取热门评论
     * @param postId 帖子ID
     * @param limit 数量限制
     * @return 评论列表
     */
    List<Comment> getHotComments(@Param("postId") Long postId, @Param("limit") Integer limit);
}