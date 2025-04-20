package com.campus.controller;

import com.campus.dto.PageResult;
import com.campus.entity.Comment;
import com.campus.entity.Post;
import com.campus.service.CommentService;
import com.campus.service.ForumService;
import com.campus.service.PostService;
import com.campus.utils.Result;
import com.campus.vo.CommentVO;
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

    @Autowired
    private CommentService commentService;

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

        PageResult<Post> pageResult = postService.findPage(params, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("rows", pageResult.getRecords());
        return Result.success(result);
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
    public Result updatePost(@PathVariable Long id, @RequestBody Post post) {
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
    public Result deletePost(@PathVariable Long id) {
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
    public Result getMyPosts(
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
    public Result getUserPosts(
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
    public Result searchPosts(
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
    public Result likePost(@PathVariable Long id) {
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
    public Result unlikePost(@PathVariable Long id) {
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
    public Result incrementViewCount(@PathVariable Long id) {
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
     * 获取帖子评论
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/posts/{postId}/comments")
    public Result getPostComments(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return Result.success(comments);
    }

    /**
     * 发表评论
     *
     * @param postId 帖子ID
     * @param comment 评论内容
     * @return 评论结果
     */
    @PostMapping("/posts/{postId}/comments")
    public Result addComment(@PathVariable Long postId, @RequestBody Comment comment) {
        // User currentUser = authService.getCurrentUserFromRequest(request); // 已移除: 用户应在Service层设置
        // if (currentUser == null) {
        //     return Result.error(401, "请先登录");
        // }

        comment.setPostId(postId); // 保留设置 postId
        // 让Service层处理设置 authorId 和时间戳
        // comment.setAuthorId(currentUser.getId());
        // comment.setCreateTime(new Date());
        // comment.setUpdateTime(new Date());
        // comment.setLikeCount(0);
        // if (comment.getStatus() == null) {
        //     comment.setStatus(1); // 默认为正常状态
        // }

        boolean result = commentService.addComment(comment);
        return result ? Result.success("评论成功", comment) : Result.error("评论失败");
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/comments/{commentId}")
    public Result deleteComment(@PathVariable Long commentId) {
        boolean success = commentService.deleteComment(commentId);
        return success ? Result.success() : Result.error("删除评论失败");
    }

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     * @return 点赞结果
     */
    @PostMapping("/comments/{commentId}/like")
    public Result likeComment(@PathVariable Long commentId) {
        boolean success = commentService.likeComment(commentId);
        return success ? Result.success() : Result.error("点赞失败");
    }

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     * @return 取消点赞结果
     */
    @DeleteMapping("/comments/{commentId}/unlike")
    public Result unlikeComment(@PathVariable Long commentId) {
        boolean success = commentService.cancelLikeComment(commentId);
        return success ? Result.success() : Result.error("取消点赞失败");
    }

    // =============== 评论相关 API ===============

    /**
     * 获取所有评论（分页，管理端使用）
     */
    @GetMapping("/comments/all")
    public Result<PageResult<CommentVO>> getAllComments( // 修改返回类型
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) Long postId,
                                                         @RequestParam(required = false) Long authorId,
                                                         @RequestParam(required = false) String keyword) {
        // 调用返回 PageResult<CommentVO> 的服务方法
        PageResult<CommentVO> pageResult = commentService.getAllCommentsPaginated(page, size, postId, authorId, keyword);
        return Result.success("查询成功", pageResult); // 返回成功结果
    }
} 