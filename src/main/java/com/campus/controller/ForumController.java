package com.campus.controller;

import com.campus.entity.Comment;
import com.campus.entity.Post;
import com.campus.entity.Tag;
import com.campus.service.CommentService;
import com.campus.service.PostService;
import com.campus.service.TagService;
import com.campus.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 论坛功能统一控制器
 * 整合帖子、评论和标签的所有功能
 */
@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;

    // =============== 帖子相关 API ===============

    /**
     * 获取帖子详情
     *
     * @param id 帖子ID
     * @return 帖子详情信息
     */
    @GetMapping("/posts/{id}")
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
    @GetMapping("/posts")
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
     * @param page   页码
     * @param size   每页数量
     * @return 该用户的帖子列表分页数据
     */
    @GetMapping("/posts/user/{userId}")
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
    @GetMapping("/posts/my")
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
     * @param page     页码
     * @param size     每页数量
     * @return 指定分类的帖子列表分页数据
     */
    @GetMapping("/posts/category/{category}")
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
     * @param page    页码
     * @param size    每页数量
     * @return 搜索结果分页数据
     */
    @GetMapping("/posts/search")
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
    @PostMapping("/posts")
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
     * @param id   帖子ID
     * @param post 帖子信息
     * @return 更新结果
     */
    @PutMapping("/posts/{id}")
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
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        boolean result = postService.deletePost(id);
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
    @GetMapping("/posts/hot")
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
    @PutMapping("/posts/{id}/views")
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
     * @param id     帖子ID
     * @param isLike 是否点赞
     * @return 点赞结果
     */
    @PutMapping("/posts/{id}/like")
    public ResponseEntity<String> likePost(
            @PathVariable Long id,
            @RequestParam(value = "isLike", defaultValue = "true") boolean isLike) {
        boolean result = isLike ?
                postService.likePost(id) :
                postService.unlikePost(id);

        if (result) {
            return ResponseEntity.ok(isLike ? "点赞成功" : "取消点赞成功");
        } else {
            return ResponseEntity.badRequest().body(isLike ? "点赞失败" : "取消点赞失败");
        }
    }

    /**
     * 获取帖子统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/posts/stats")
    public ResponseEntity<Map<String, Object>> getPostStats() {
        Map<String, Object> stats = postService.getPostStats();
        return ResponseEntity.ok(stats);
    }

    // =============== 评论相关 API ===============

    /**
     * 获取评论详情
     *
     * @param id 评论ID
     * @return 评论详情
     */
    @GetMapping("/comments/{id}")
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
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Map<String, Object>>> getCommentsByPostId(@PathVariable Long postId) {
        List<Map<String, Object>> comments = postService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 根据用户ID获取评论
     *
     * @param userId 用户ID
     * @return 评论列表
     */
    @GetMapping("/comments/user/{userId}")
    public ResponseEntity<List<Comment>> getCommentsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(commentService.getCommentsByUserId(userId));
    }

    /**
     * 获取帖子的一级评论
     *
     * @param postId 帖子ID
     * @return 一级评论列表
     */
    @GetMapping("/posts/{postId}/root-comments")
    public ResponseEntity<List<Comment>> getRootCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getRootCommentsByPostId(postId));
    }

    /**
     * 获取子评论
     *
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    @GetMapping("/comments/{parentId}/children")
    public ResponseEntity<List<Comment>> getChildComments(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentService.getCommentsByParentId(parentId));
    }

    /**
     * 添加评论
     *
     * @param comment 评论信息
     * @return 添加结果
     */
    @PostMapping("/comments")
    public ResponseEntity<String> addComment(@RequestBody Comment comment) {
        if (commentService.addComment(comment)) {
            return ResponseEntity.ok("评论成功");
        } else {
            return ResponseEntity.badRequest().body("评论失败");
        }
    }

    /**
     * 添加帖子评论
     *
     * @param postId  帖子ID
     * @param comment 评论内容
     * @return 添加结果
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<String> addPostComment(
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
     * 更新评论
     *
     * @param id      评论ID
     * @param comment 评论信息
     * @return 更新结果
     */
    @PutMapping("/comments/{id}")
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
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        if (commentService.deleteComment(id)) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.badRequest().body("删除失败");
        }
    }

    /**
     * 删除帖子评论
     *
     * @param commentId 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/posts/comments/{commentId}")
    public ResponseEntity<String> deletePostComment(@PathVariable Long commentId) {
        boolean result = postService.deleteComment(commentId);
        if (result) {
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
    @DeleteMapping("/comments/batch")
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
    @PutMapping("/comments/{id}/like")
    public ResponseEntity<String> likeComment(@PathVariable Long id) {
        if (commentService.incrementLikeCount(id)) {
            return ResponseEntity.ok("点赞成功");
        } else {
            return ResponseEntity.badRequest().body("点赞失败");
        }
    }

    // =============== 标签相关 API ===============

    /**
     * 获取标签列表
     *
     * @return 标签列表
     */
    @GetMapping("/tags")
    public List<Tag> getTagList() {
        return tagService.getAllTags();
    }

    /**
     * 根据ID获取标签
     *
     * @param id 标签ID
     * @return 标签对象
     */
    @GetMapping("/tags/{id}")
    public Tag getTagById(@PathVariable Integer id) {
        return tagService.getTagById(id);
    }

    /**
     * 根据帖子ID获取标签
     *
     * @param postId 帖子ID
     * @return 标签列表
     */
    @GetMapping("/posts/{postId}/tags")
    public ResponseEntity<List<Map<String, Object>>> getPostTags(@PathVariable Long postId) {
        List<Map<String, Object>> tags = postService.getPostTags(postId);
        return ResponseEntity.ok(tags);
    }

    /**
     * 添加标签到帖子
     *
     * @param postId 帖子ID
     * @param tagIds 标签ID数组
     * @return 添加结果
     */
    @PostMapping("/posts/{postId}/tags")
    public ResponseEntity<String> addTagsToPost(
            @PathVariable Long postId,
            @RequestBody Long[] tagIds) {
        boolean result = postService.addTagsToPost(postId, tagIds);
        if (result) {
            return ResponseEntity.ok("标签添加成功");
        } else {
            return ResponseEntity.badRequest().body("标签添加失败");
        }
    }

    /**
     * 从帖子移除标签
     *
     * @param postId 帖子ID
     * @param tagId  标签ID
     * @return 移除结果
     */
    @DeleteMapping("/posts/{postId}/tags/{tagId}")
    public ResponseEntity<String> removeTagFromPost(
            @PathVariable Long postId,
            @PathVariable Long tagId) {
        boolean result = postService.removeTagFromPost(postId, tagId);
        if (result) {
            return ResponseEntity.ok("标签移除成功");
        } else {
            return ResponseEntity.badRequest().body("标签移除失败");
        }
    }

    /**
     * 搜索标签
     *
     * @param keyword 关键词
     * @return 标签列表
     */
    @GetMapping("/tags/search")
    public List<Tag> searchTags(@RequestParam("keyword") String keyword) {
        // 搜索获取标签
        List<Tag> allTags = tagService.getAllTags();
        List<Tag> result = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return allTags;
        }

        String lowerKeyword = keyword.toLowerCase();
        for (Tag tag : allTags) {
            if (tag.getName().toLowerCase().contains(lowerKeyword)) {
                result.add(tag);
            }
        }

        return result;
    }

    /**
     * 添加标签
     *
     * @param tag 标签对象
     * @return 添加结果
     */
    @PostMapping("/tags")
    public Result addTag(@RequestBody Tag tag) {
        // 设置创建时间和更新时间
        tag.setCreateTime(new Date());
        tag.setUpdateTime(new Date());

        boolean success = tagService.addTag(tag);

        if (success) {
            return Result.success("标签添加成功");
        } else {
            return Result.error("标签添加失败");
        }
    }

    /**
     * 更新标签
     *
     * @param id  标签ID
     * @param tag 标签对象
     * @return 更新结果
     */
    @PutMapping("/tags/{id}")
    public Result updateTag(@PathVariable Integer id, @RequestBody Tag tag) {
        // 设置ID和更新时间
        tag.setId(id);
        tag.setUpdateTime(new Date());

        boolean success = tagService.updateTag(tag);

        if (success) {
            return Result.success("标签更新成功");
        } else {
            return Result.error("标签更新失败");
        }
    }

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 删除结果
     */
    @DeleteMapping("/tags/{id}")
    public Result deleteTag(@PathVariable Integer id) {
        boolean success = tagService.deleteTag(id);

        if (success) {
            return Result.success("标签删除成功");
        } else {
            return Result.error("标签删除失败");
        }
    }

    /**
     * 批量删除标签
     *
     * @param ids 标签ID列表
     * @return 删除结果
     */
    @DeleteMapping("/tags/batch")
    public Result batchDeleteTags(@RequestBody List<Integer> ids) {
        boolean success = tagService.batchDeleteTags(ids);

        if (success) {
            return Result.success("标签批量删除成功");
        } else {
            return Result.error("标签批量删除失败");
        }
    }
} 