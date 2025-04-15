package com.campus.service;

import java.util.List;

import com.campus.entity.Comment;

/**
 * 论坛评论服务接口
 */
public interface CommentService {
    
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
    List<Comment> getCommentsByPostId(Long postId);
    
    /**
     * 根据用户ID查询评论
     * @param userId 用户ID
     * @return 评论列表
     */
    List<Comment> getCommentsByUserId(Long userId);
    
    /**
     * 根据父评论ID查询子评论
     * @param parentId 父评论ID
     * @return 评论列表
     */
    List<Comment> getCommentsByParentId(Long parentId);
    
    /**
     * 查询帖子的一级评论
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Comment> getRootCommentsByPostId(Long postId);
    
    /**
     * 添加评论
     * @param comment 评论对象
     * @return 是否成功
     */
    boolean addComment(Comment comment);
    
    /**
     * 更新评论
     * @param comment 评论对象
     * @return 是否成功
     */
    boolean updateComment(Comment comment);
    
    /**
     * 删除评论
     * @param id 评论ID
     * @return 是否成功
     */
    boolean deleteComment(Long id);
    
    /**
     * 批量删除评论
     * @param ids 评论ID数组
     * @return 是否成功
     */
    boolean batchDeleteComments(Long[] ids);
    
    /**
     * 根据帖子ID删除评论
     * @param postId 帖子ID
     * @return 是否成功
     */
    boolean deleteCommentsByPostId(Long postId);
    
    /**
     * 更新评论状态
     * @param id 评论ID
     * @param status 状态值
     * @return 是否成功
     */
    boolean updateCommentStatus(Long id, Integer status);
    
    /**
     * 增加评论点赞次数
     * @param id 评论ID
     * @return 是否成功
     */
    boolean incrementLikeCount(Long id);
}