package com.campus.service;

import com.campus.dto.PageResult;
import com.campus.entity.Comment;
import com.campus.vo.CommentVO;

import java.util.List;

/**
 * 论坛评论服务接口
 */
public interface CommentService {
    
    /**
     * 根据ID查询评论信息
     * @param id 评论ID
     * @return 评论实体
     */
    Comment getCommentById(Long id);
    
    /**
     * 查询所有评论列表
     * @return 评论列表
     */
    List<Comment> getAllComments();
    
    /**
     * 根据帖子ID查询评论列表
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Comment> getCommentsByPostId(Long postId);
    
    /**
     * 根据用户ID查询评论列表
     * @param userId 用户ID
     * @return 评论列表
     */
    List<Comment> getCommentsByUserId(Long userId);
    
    /**
     * 根据父评论ID查询子评论列表
     * @param parentId 父评论ID
     * @return 评论列表
     */
    List<Comment> getCommentsByParentId(Long parentId);
    
    /**
     * 查询帖子的一级评论列表
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Comment> getRootCommentsByPostId(Long postId);
    
    /**
     * 添加评论
     * @param comment 评论实体
     * @return 是否成功
     */
    boolean addComment(Comment comment);
    
    /**
     * 更新评论信息
     * @param comment 评论实体
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

    /**
     * 减少评论点赞次数
     *
     * @param id 评论ID
     * @return 是否成功
     */
    boolean decrementLikeCount(Long id);

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean likeComment(Long commentId);

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean cancelLikeComment(Long commentId);

    List<Comment> getReplies(Long parentId);

    /**
     * 获取所有评论（分页，管理端使用）
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     * @param postId   帖子ID (可选)
     * @param authorId 作者ID (可选)
     * @param keyword  关键词 (可选, 搜索内容)
     * @return 分页评论结果 (包含作者信息)
     */
    PageResult<CommentVO> getAllCommentsPaginated(int pageNo, int pageSize, Long postId, Long authorId, String keyword);
}