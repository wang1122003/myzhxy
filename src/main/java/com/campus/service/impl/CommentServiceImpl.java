package com.campus.service.impl;

import com.campus.dao.CommentDao;
import com.campus.entity.Comment;
import com.campus.service.CommentService;
import com.campus.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 论坛评论服务实现类
 */
@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private CommentDao commentDao;
    
    @Autowired
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Comment getCommentById(Long id) {
        Comment comment = commentDao.getCommentById(id);
        if (comment != null && comment.getRepliesJson() != null) {
            processReplies(comment);
        }
        return comment;
    }

    @Override
    public List<Comment> getAllComments() {
        List<Comment> comments = commentDao.getAllComments();
        comments.forEach(this::processReplies);
        return comments;
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentDao.getCommentsByPostId(postId);
        comments.forEach(this::processReplies);
        return comments;
    }

    @Override
    public List<Comment> getCommentsByUserId(Long userId) {
        List<Comment> comments = commentDao.getCommentsByAuthorId(userId, null);
        comments.forEach(this::processReplies);
        return comments;
    }

    @Override
    public List<Comment> getCommentsByParentId(Long parentId) {
        List<Comment> comments = commentDao.getCommentsByParentId(parentId);
        comments.forEach(this::processReplies);
        return comments;
    }

    @Override
    public List<Comment> getRootCommentsByPostId(Long postId) {
        List<Comment> comments = commentDao.getRootCommentsByPostId(postId);
        comments.forEach(this::processReplies);
        return comments;
    }

    /**
     * 处理评论回复，将JSON字符串转换为对象列表
     */
    private void processReplies(Comment comment) {
        if (comment == null || comment.getRepliesJson() == null || comment.getRepliesJson().isEmpty()) {
            return;
        }

        try {
            Comment[] replies = objectMapper.readValue(comment.getRepliesJson(), Comment[].class);
            comment.setReplies(java.util.Arrays.asList(replies));
        } catch (JsonProcessingException e) {
            log.error("反序列化评论回复失败 (Comment ID: {}): {}", comment.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean addComment(Comment comment) {
        // 设置初始值
        Date now = new Date();
        comment.setCreateTime(now);
        comment.setUpdateTime(now);
        
        // 初始化点赞次数
        comment.setLikeCount(0);
        
        // 如果没有设置状态，默认为正常
        if (comment.getStatus() == null) {
            comment.setStatus(1);
        }

        boolean result = commentDao.addComment(comment) > 0;
        
        // 增加帖子评论次数
        if (result && comment.getPostId() != null) {
            postService.incrementCommentCount(comment.getPostId());
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean updateComment(Comment comment) {
        // 设置更新时间
        comment.setUpdateTime(new Date());

        return commentDao.updateComment(comment) > 0;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long id) {
        return commentDao.deleteComment(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteComments(Long[] ids) {
        return commentDao.batchDeleteComments(ids) > 0;
    }

    @Override
    @Transactional
    public boolean deleteCommentsByPostId(Long postId) {
        return commentDao.deleteCommentsByPostId(postId) > 0;
    }

    @Override
    @Transactional
    public boolean updateCommentStatus(Long id, Integer status) {
        return commentDao.updateCommentStatus(id, status) > 0;
    }

    @Override
    @Transactional
    public boolean incrementLikeCount(Long id) {
        return commentDao.incrementLikeCount(id) > 0;
    }

    @Override
    @Transactional
    public boolean decrementLikeCount(Long id) {
        return commentDao.decrementLikeCount(id) > 0;
    }

    @Override
    @Transactional
    public boolean likeComment(Long commentId) {
        return incrementLikeCount(commentId);
    }

    @Override
    @Transactional
    public boolean cancelLikeComment(Long commentId) {
        return decrementLikeCount(commentId);
    }
}