package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Comment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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
    Comment getCommentById(Long id);
    
    /**
     * 查询所有评论
     * @return 评论列表
     */
    List<Comment> getAllComments();
    
    /**
     * 根据帖子ID查询评论
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Comment> getCommentsByPostId(@Param("postId") Long postId);
    
    /**
     * 根据作者ID查询评论
     * @param authorId 作者ID
     * @param parentIdStatus 父评论ID状态，true-查询父评论为null的评论，false-查询父评论不为null的评论，null-查询所有评论
     * @return 评论列表
     */
    List<Comment> getCommentsByAuthorId(@Param("authorId") Long authorId, @Param("parentIdStatus") Boolean parentIdStatus);
    
    /**
     * 根据父评论ID查询子评论
     * @param parentId 父评论ID
     * @return 评论列表
     */
    List<Comment> getCommentsByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据帖子ID查询根评论（父评论ID为null的评论）
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Comment> getRootCommentsByPostId(@Param("postId") Long postId);
    
    /**
     * 获取帖子的评论数量
     * @param postId 帖子ID
     * @return 评论数量
     */
    int getCommentCountByPostId(@Param("postId") Long postId);
    
    /**
     * 获取用户的评论数量
     * @param authorId 用户ID
     * @return 评论数量
     */
    int getCommentCountByAuthorId(@Param("authorId") Long authorId);
    
    /**
     * 添加评论
     * @param comment 评论对象
     * @return 影响行数
     */
    int addComment(Comment comment);

    /**
     * 批量添加评论
     *
     * @param comments 评论列表
     * @return 影响行数
     */
    int batchAddComments(@Param("list") List<Comment> comments);
    
    /**
     * 更新评论
     * @param comment 评论对象
     * @return 影响行数
     */
    int updateComment(Comment comment);

    /**
     * 批量更新评论状态
     *
     * @param commentIds 评论ID列表
     * @param status     状态值
     * @return 影响行数
     */
    int batchUpdateCommentStatus(@Param("commentIds") List<Long> commentIds, @Param("status") Integer status);
    
    /**
     * 删除评论
     * @param id 评论ID
     * @return 影响行数
     */
    int deleteComment(@Param("id") Long id);

    /**
     * 批量删除评论
     *
     * @param commentIds 评论ID列表
     * @return 影响行数
     */
    int batchDeleteComments(@Param("commentIds") Long[] commentIds);
    
    /**
     * 根据帖子ID删除评论
     * @param postId 帖子ID
     * @return 影响行数
     */
    int deleteCommentsByPostId(@Param("postId") Long postId);
    
    /**
     * 更新评论状态
     * @param id 评论ID
     * @param status 状态值
     * @return 影响行数
     */
    int updateCommentStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 增加点赞数
     * @param id 评论ID
     * @return 影响行数
     */
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 减少点赞数
     * @param id 评论ID
     * @return 影响行数
     */
    int decrementLikeCount(@Param("id") Long id);

    /**
     * 获取评论统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getCommentStats();
    
    /**
     * 获取帖子的评论统计信息
     * @param postId 帖子ID
     * @return 统计信息
     */
    Map<String, Object> getPostCommentStats(@Param("postId") Long postId);

    /**
     * 获取作者评论统计信息
     * @param authorId 作者ID
     * @return 统计信息
     */
    Map<String, Object> getAuthorCommentStats(@Param("authorId") Long authorId);

    /**
     * 获取评论发布趋势
     *
     * @return 趋势数据
     */
    List<Map<String, Object>> getCommentPublishTrend();

    /**
     * 获取热门评论
     * @param postId 帖子ID (可为null)
     * @param limit 数量限制
     * @return 评论列表
     */
    List<Map<String, Object>> getHotComments(@Param("postId") Long postId, @Param("limit") Integer limit);
}