package com.campus.controller;

import com.campus.entity.Post;
import com.campus.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 帖子管理控制器
 * 提供论坛帖子的创建、查询、修改和删除等API
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 获取帖子详情
     * 
     * @param id 帖子ID
     * @return 帖子详情信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        if (post != null) {
            return ResponseEntity.ok(post);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取所有帖子
     * 
     * @param page 页码
     * @param size 每页数量
     * @return 帖子列表分页数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> result = postService.getAllPosts(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据用户ID获取帖子
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 该用户的帖子列表分页数据
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getPostsByUserId(
            @PathVariable Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> result = postService.getPostsByUserId(userId, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取当前用户帖子
     * 
     * @param page 页码
     * @param size 每页数量
     * @return 当前用户的帖子列表分页数据
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyPosts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> result = postService.getMyPosts(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据分类获取帖子
     * 
     * @param category 帖子分类
     * @param page 页码
     * @param size 每页数量
     * @return 指定分类的帖子列表分页数据
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getPostsByCategory(
            @PathVariable String category,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> result = postService.getPostsByCategory(category, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 搜索帖子
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @return 搜索结果分页数据
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> result = postService.searchPosts(keyword, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 添加帖子
     * 
     * @param post 帖子信息
     * @return 添加结果
     */
    @PostMapping
    public ResponseEntity<String> addPost(@RequestBody Post post) {
        boolean result = postService.addPost(post);
        if (result) {
            return ResponseEntity.ok("添加成功");
        } else {
            return ResponseEntity.badRequest().body("添加失败");
        }
    }

    /**
     * 更新帖子
     * 
     * @param id 帖子ID
     * @param post 帖子信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(@PathVariable Long id, @RequestBody Post post) {
        post.setId(id);
        boolean result = postService.updatePost(post);
        if (result) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.badRequest().body("更新失败");
        }
    }

    /**
     * 删除帖子
     * 
     * @param id 帖子ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        boolean result = postService.deletePost(id);
        if (result) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.badRequest().body("删除失败");
        }
    }

    /**
     * 获取帖子评论
     * 
     * @param postId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Map<String, Object>>> getCommentsByPostId(@PathVariable Long postId) {
        List<Map<String, Object>> comments = postService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 添加帖子评论
     * 
     * @param postId 帖子ID
     * @param comment 评论内容
     * @return 添加结果
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<String> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> comment) {
        boolean result = postService.addComment(postId, comment.get("content"));
        if (result) {
            return ResponseEntity.ok("评论成功");
        } else {
            return ResponseEntity.badRequest().body("评论失败");
        }
    }

    /**
     * 删除帖子评论
     * 
     * @param commentId 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        boolean result = postService.deleteComment(commentId);
        if (result) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.badRequest().body("删除失败");
        }
    }

    /**
     * 获取热门帖子
     * 
     * @param limit 数量限制
     * @return 热门帖子列表
     */
    @GetMapping("/hot")
    public ResponseEntity<List<Post>> getHotPosts(
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        List<Post> posts = postService.getHotPosts(limit);
        return ResponseEntity.ok(posts);
    }

    /**
     * 增加帖子浏览量
     * 
     * @param id 帖子ID
     * @return 更新结果
     */
    @PutMapping("/{id}/views")
    public ResponseEntity<String> incrementViews(@PathVariable Long id) {
        boolean result = postService.incrementViews(id);
        if (result) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.badRequest().body("更新失败");
        }
    }

    /**
     * 点赞/取消点赞帖子
     * 
     * @param id 帖子ID
     * @param isLike 是否点赞
     * @return 操作结果
     */
    @PutMapping("/{id}/like")
    public ResponseEntity<String> likePost(
            @PathVariable Long id,
            @RequestParam(value = "isLike", defaultValue = "true") boolean isLike) {
        boolean result = isLike ? postService.likePost(id) : postService.unlikePost(id);
        if (result) {
            String message = isLike ? "点赞成功" : "取消点赞成功";
            return ResponseEntity.ok(message);
        } else {
            String message = isLike ? "点赞失败" : "取消点赞失败";
            return ResponseEntity.badRequest().body(message);
        }
    }

    /**
     * 获取帖子标签
     * 
     * @param postId 帖子ID
     * @return 标签列表
     */
    @GetMapping("/{postId}/tags")
    public ResponseEntity<List<Map<String, Object>>> getPostTags(@PathVariable Long postId) {
        List<Map<String, Object>> tags = postService.getPostTags(postId);
        return ResponseEntity.ok(tags);
    }

    /**
     * 为帖子添加标签
     * 
     * @param postId 帖子ID
     * @param tagIds 标签ID数组
     * @return 操作结果
     */
    @PostMapping("/{postId}/tags")
    public ResponseEntity<String> addTagsToPost(
            @PathVariable Long postId,
            @RequestBody Long[] tagIds) {
        boolean result = postService.addTagsToPost(postId, tagIds);
        if (result) {
            return ResponseEntity.ok("添加标签成功");
        } else {
            return ResponseEntity.badRequest().body("添加标签失败");
        }
    }

    /**
     * 从帖子移除标签
     * 
     * @param postId 帖子ID
     * @param tagId 标签ID
     * @return 操作结果
     */
    @DeleteMapping("/{postId}/tags/{tagId}")
    public ResponseEntity<String> removeTagFromPost(
            @PathVariable Long postId,
            @PathVariable Long tagId) {
        boolean result = postService.removeTagFromPost(postId, tagId);
        if (result) {
            return ResponseEntity.ok("移除标签成功");
        } else {
            return ResponseEntity.badRequest().body("移除标签失败");
        }
    }

    /**
     * 获取帖子统计信息
     * 
     * @return 统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPostStats() {
        Map<String, Object> stats = postService.getPostStats();
        return ResponseEntity.ok(stats);
    }
}