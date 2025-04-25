package com.campus.controller;

// import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// import com.campus.entity.Comment; // Removed Comment dependency
import com.campus.entity.Post;
// import com.campus.service.CommentService; // Removed CommentService dependency
import com.campus.service.ForumService;
import com.campus.service.PostService;
import com.campus.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论坛功能统一控制器
 * 整合帖子、评论和板块的功能
 */
@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private PostService postService;

    // @Autowired
    // private CommentService commentService; // Removed CommentService injection

    @Autowired
    private ForumService forumService;

    // =============== 板块/分类相关 API (新增/修改) ===============

    /**
     * 获取所有论坛分类类型
     *
     * @return 分类类型列表
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getForumCategories() {
        List<String> forumTypes = forumService.listAvailableForumTypes();
        List<Map<String, Object>> categories = new java.util.ArrayList<>();
        for (int i = 0; i < forumTypes.size(); i++) {
            Map<String, Object> category = new HashMap<>();
            category.put("id", i + 1);
            category.put("name", forumTypes.get(i));
            categories.add(category);
        }
        return Result.success(categories);
    }

    /**
     * 获取所有可用的板块列表 (用于下拉列表等)
     * @deprecated 此接口依赖不存在的 forum 表，请使用 /categories 接口替代
     * @return 板块列表
     */
    @Deprecated
    @GetMapping("/forums")
    public ResponseEntity<?> getAvailableForums() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("This endpoint is deprecated, use /categories instead.");
    }


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
     * @param keyword 关键词 (可选)
     * @param forumType 板块类型 (可选)
     * @param tag 标签 (可选)
     * @param sortBy 排序依据 (可选, 默认为 createTime)
     * @return 帖子列表分页数据
     */
    @GetMapping("/posts")
    public Result<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String forumType,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false, defaultValue = "createTime") String sortBy) {

        Map<String, Object> params = new java.util.HashMap<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            params.put("keyword", keyword);
        }
        if (forumType != null && !forumType.trim().isEmpty()) {
            params.put("forumType", forumType);
        }
        if (tag != null && !tag.trim().isEmpty()) {
            params.put("tag", tag);
        }
        params.put("sortBy", sortBy);

        Map<String, Object> pageResult = postService.findPageMap(params, page, size);
        return Result.success(pageResult);
    }

    /**
     * 创建帖子
     *
     * @param post 帖子信息
     * @return 创建结果
     */
    @PostMapping("/posts")
    public Result<Void> createPost(@RequestBody Post post) {
        boolean success = postService.createPost(post);
        return success ? Result.success(null) : Result.error("创建帖子失败");
    }

    /**
     * 更新帖子
     *
     * @param id   帖子ID
     * @param post 帖子信息
     * @return 更新结果
     */
    @PutMapping("/posts/{id}")
    public Result<String> updatePost(@PathVariable Long id, @RequestBody Post post) {
        Post existingPost = postService.getPostById(id);
        if (existingPost == null) {
            return Result.error("帖子不存在");
        }


        post.setId(id); // 保留从路径设置ID

        boolean result = postService.updatePost(post);
        return result ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除帖子
     *
     * @param id 帖子ID
     * @return 删除结果
     */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        boolean success = postService.deletePost(id);
        return success ? Result.success() : Result.error("删除帖子失败");
    }

    /**
     * 获取我的帖子
     *
     * @param page 页码
     * @param size 每页数量
     * @return 我的帖子列表
     */
    @GetMapping("/posts/my")
    public Result<Map<String, Object>> getMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = postService.getMyPosts(page, size);
        return Result.success(result);
    }

    /**
     * 获取用户的帖子
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 用户帖子列表
     */
    @GetMapping("/posts/user/{userId}")
    public Result<Map<String, Object>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = postService.getPostsByUserId(userId, page, size);
        return Result.success(result);
    }

    /**
     * 搜索帖子
     *
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @return 搜索结果
     */
    @GetMapping("/posts/search")
    public Result<Map<String, Object>> searchPosts(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = postService.searchPosts(keyword, page, size);
        return Result.success(result);
    }

    /**
     * 点赞帖子
     *
     * @param id 帖子ID
     * @return 点赞结果
     */
    @PostMapping("/posts/{id}/like")
    public Result<Void> likePost(@PathVariable Long id) {
        boolean success = postService.likePost(id);
        return success ? Result.success() : Result.error("点赞失败");
    }

    /**
     * 取消点赞帖子
     *
     * @param id 帖子ID
     * @return 取消点赞结果
     */
    @DeleteMapping("/posts/{id}/unlike")
    public Result<Void> unlikePost(@PathVariable Long id) {
        boolean success = postService.unlikePost(id);
        return success ? Result.success() : Result.error("取消点赞失败");
    }

    /**
     * 增加帖子浏览量
     *
     * @param id 帖子ID
     * @return 增加结果
     */
    @PostMapping("/posts/{id}/view")
    public Result<Void> incrementViewCount(@PathVariable Long id) {
        boolean success = postService.incrementViews(id);
        return success ? Result.success() : Result.error("操作失败");
    }

    /**
     * 获取热门帖子
     *
     * @param limit 返回数量限制，默认10
     * @return 热门帖子列表
     */
    @GetMapping("/posts/hot")
    public Result<List<Post>> getHotPosts(@RequestParam(defaultValue = "10") int limit) {
        List<Post> posts = postService.getHotPosts(limit);
        return Result.success(posts);
    }

    /**
     * 获取帖子的评论 (已注释)
     */
    /*
    @GetMapping("/posts/{postId}/comments")
    public Result<List<Comment>> getPostComments(@PathVariable Long postId) {
        // List<Comment> comments = commentService.getCommentsByPostId(postId);
        // return Result.success(comments);
        return Result.error("评论功能暂未实现");
    }
    */

    /**
     * 发表评论 (已注释)
     */
    /*
    @PostMapping("/posts/{postId}/comments")
    public Result<Comment> addComment(@PathVariable Long postId, @RequestBody Comment comment) {
        comment.setPostId(postId);
        // boolean success = commentService.addComment(comment);
        // return success ? Result.success(comment) : Result.error("发表评论失败");
        return Result.error("评论功能暂未实现");
    }
    */

    /**
     * 删除评论 (已注释)
     */
    /*
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        // boolean success = commentService.deleteComment(commentId);
        // return success ? Result.success() : Result.error("删除评论失败");
         return Result.error("评论功能暂未实现");
    }
    */

    /**
     * 点赞评论 (已注释)
     */
    /*
    @PostMapping("/comments/{commentId}/like")
    public Result<Void> likeComment(@PathVariable Long commentId) {
        // boolean success = commentService.likeComment(commentId, getCurrentUserId()); // Assuming a method to get current user ID
        // return success ? Result.success() : Result.error("点赞失败");
         return Result.error("评论功能暂未实现");
    }
    */

    /**
     * 取消点赞评论 (已注释)
     */
    /*
    @DeleteMapping("/comments/{commentId}/unlike")
    public Result<Void> unlikeComment(@PathVariable Long commentId) {
        // boolean success = commentService.unlikeComment(commentId, getCurrentUserId()); // Assuming a method to get current user ID
        // return success ? Result.success() : Result.error("取消点赞失败");
         return Result.error("评论功能暂未实现");
    }
    */

    // =============== 管理端评论 API (移除或注释掉) ===============
    /**
     * 获取所有评论（管理端分页） (已注释)
     */
    /*
    @GetMapping("/comments/manage")
    public Result<Page<Map<String, Object>>> getAllCommentsManaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        // Page<Map<String, Object>> resultPage = commentService.getAllCommentsManaged(page, size, status, keyword);
        // return Result.success(resultPage);
        return Result.error("评论功能暂未实现");
    }
    */

    /**
     * 获取指定用户的评论（管理端分页） (已注释)
     */
    /*
    @GetMapping("/comments/user/{userId}")
    public Result<Page<Map<String, Object>>> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Page<Map<String, Object>> resultPage = commentService.getCommentsByUserIdWithPostInfo(userId, page, size);
        // return Result.success(resultPage);
        return Result.error("评论功能暂未实现");
    }
    */
}