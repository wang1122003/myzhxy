package com.campus.controller;

import com.campus.entity.Comment;
import com.campus.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 论坛评论控制器
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取评论详情
     * 
     * @param id 评论ID
     * @return 评论详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Comment> getComment(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        if (comment != null) {
            return ResponseEntity.ok(comment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根据帖子ID获取评论
     * 
     * @param postId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    /**
     * 根据用户ID获取评论
     * 
     * @param userId 用户ID
     * @return 评论列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Comment>> getCommentsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(commentService.getCommentsByUserId(userId));
    }

    /**
     * 获取帖子的一级评论
     * 
     * @param postId 帖子ID
     * @return 一级评论列表
     */
    @GetMapping("/post/{postId}/root")
    public ResponseEntity<List<Comment>> getRootCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getRootCommentsByPostId(postId));
    }

    /**
     * 获取子评论
     * 
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<Comment>> getChildComments(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentService.getCommentsByParentId(parentId));
    }

    /**
     * 添加评论
     * 
     * @param comment 评论信息
     * @return 添加结果
     */
    @PostMapping
    public ResponseEntity<String> addComment(@RequestBody Comment comment) {
        if (commentService.addComment(comment)) {
            return ResponseEntity.ok("评论成功");
        } else {
            return ResponseEntity.badRequest().body("评论失败");
        }
    }

    /**
     * 更新评论
     * 
     * @param id 评论ID
     * @param comment 评论信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        comment.setId(id);
        if (commentService.updateComment(comment)) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.badRequest().body("更新失败");
        }
    }

    /**
     * 删除评论
     * 
     * @param id 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        if (commentService.deleteComment(id)) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.badRequest().body("删除失败");
        }
    }

    /**
     * 批量删除评论
     * 
     * @param ids 评论ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<String> batchDeleteComments(@RequestBody Long[] ids) {
        if (commentService.batchDeleteComments(ids)) {
            return ResponseEntity.ok("批量删除成功");
        } else {
            return ResponseEntity.badRequest().body("批量删除失败");
        }
    }

    /**
     * 点赞评论
     * 
     * @param id 评论ID
     * @return 点赞结果
     */
    @PutMapping("/{id}/like")
    public ResponseEntity<String> likeComment(@PathVariable Long id) {
        if (commentService.incrementLikeCount(id)) {
            return ResponseEntity.ok("点赞成功");
        } else {
            return ResponseEntity.badRequest().body("点赞失败");
        }
    }
}