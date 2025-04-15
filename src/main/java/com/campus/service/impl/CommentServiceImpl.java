package com.campus.service.impl;

import com.campus.dao.CommentDao;
import com.campus.entity.Comment;
import com.campus.service.CommentService;
import com.campus.service.PostService;
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

    @Autowired
    private CommentDao commentDao;
    
    @Autowired
    private PostService postService;

    @Override
    public Comment getCommentById(Long id) {
        return commentDao.findById(id);
    }

    @Override
    public List<Comment> getAllComments() {
        return commentDao.findAll();
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentDao.findByPostId(postId);
    }

    @Override
    public List<Comment> getCommentsByUserId(Long userId) {
        return commentDao.findByUserId(userId);
    }

    @Override
    public List<Comment> getCommentsByParentId(Long parentId) {
        return commentDao.findByParentId(parentId);
    }

    @Override
    public List<Comment> getRootCommentsByPostId(Long postId) {
        return commentDao.findRootCommentsByPostId(postId);
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
        
        // 如果没有设置父评论ID，默认为0（一级评论）
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        
        // 如果没有设置回复评论ID，默认为0
        if (comment.getReplyId() == null) {
            comment.setReplyId(0L);
        }
        
        // 如果没有设置回复用户ID，默认为0
        if (comment.getReplyUserId() == null) {
            comment.setReplyUserId(0L);
        }
        
        boolean result = commentDao.insert(comment) > 0;
        
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
        
        return commentDao.update(comment) > 0;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long id) {
        return commentDao.delete(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteComments(Long[] ids) {
        return commentDao.batchDelete(ids) > 0;
    }

    @Override
    @Transactional
    public boolean deleteCommentsByPostId(Long postId) {
        return commentDao.deleteByPostId(postId) > 0;
    }

    @Override
    @Transactional
    public boolean updateCommentStatus(Long id, Integer status) {
        return commentDao.updateStatus(id, status) > 0;
    }

    @Override
    @Transactional
    public boolean incrementLikeCount(Long id) {
        Comment comment = commentDao.findById(id);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            comment.setUpdateTime(new Date());
            return commentDao.update(comment) > 0;
        }
        return false;
    }
}