package com.campus.controller;

import com.campus.entity.Post;
import com.campus.service.PostService;
import com.campus.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子控制器
 * 处理帖子和评论相关功能
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

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
     * @param page      页码
     * @param size      每页数量
     * @param keyword   关键词 (可选)
     * @param sortBy    排序依据 (可选, 默认为 createTime)
     * @return 帖子列表分页数据
     */
    @GetMapping("")
    public Result<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "createTime") String sortBy) {

        log.info("获取帖子列表请求: page={}, size={}, keyword={}, sortBy={}", page, size, keyword, sortBy);

        Map<String, Object> params = new java.util.HashMap<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            params.put("keyword", keyword);
        }
        params.put("sortBy", sortBy);

        Map<String, Object> pageResult = postService.findPageMap(params, page, size);
        log.info("获取帖子列表结果: total={}, size={}",
                pageResult.get("total"),
                pageResult.get("rows") != null ? ((List) pageResult.get("rows")).size() : 0);
        
        return Result.success(pageResult);
    }

    /**
     * 创建帖子
     *
     * @param post 帖子信息
     * @return 创建结果
     */
    @PostMapping("")
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
    @PutMapping("/{id}")
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
    @DeleteMapping("/{id}")
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
    @GetMapping("/my")
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
     * @param page   页码
     * @param size   每页数量
     * @return 用户帖子列表
     */
    @GetMapping("/user/{userId}")
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
     * @param page    页码
     * @param size    每页数量
     * @return 搜索结果
     */
    @GetMapping("/search")
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
    @PostMapping("/{id}/like")
    public Result<Void> likePost(@PathVariable Long id) {
        boolean success = postService.likePost(id);
        return success ? Result.success() : Result.error("点赞失败");
    }

    /**
     * 取消点赞
     *
     * @param id 帖子ID
     * @return 取消点赞结果
     */
    @DeleteMapping("/{id}/unlike")
    public Result<Void> unlikePost(@PathVariable Long id) {
        boolean success = postService.unlikePost(id);
        return success ? Result.success() : Result.error("取消点赞失败");
    }

    /**
     * 增加帖子浏览量
     *
     * @param id 帖子ID
     * @return 操作结果
     */
    @PostMapping("/{id}/view")
    public Result<Void> incrementViewCount(@PathVariable Long id) {
        boolean success = postService.incrementViewCount(id);
        return success ? Result.success() : Result.error("操作失败");
    }

    /**
     * 获取帖子的评论列表
     *
     * @param postId 帖子ID
     * @return 评论列表 (注意：此版本未分页)
     */
    @GetMapping("/{postId}/comments")
    public Result<List<Map<String, Object>>> getCommentsForPost(@PathVariable Long postId) {
        log.info("请求帖子 {} 的评论列表", postId);
        try {
            List<Map<String, Object>> comments = postService.getCommentsByPostId(postId);
            log.info("帖子 {} 的评论列表获取成功，数量: {}", postId, comments.size());
            return Result.success(comments);
        } catch (Exception e) {
            log.error("获取帖子 {} 评论列表失败: {}", postId, e.getMessage(), e);
            return Result.error("获取评论列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门帖子
     *
     * @param limit 数量限制
     * @return 热门帖子列表
     */
    @GetMapping("/hot")
    public Result<List<Post>> getHotPosts(@RequestParam(defaultValue = "10") int limit) {
        log.info("此功能已禁用: 获取热门帖子, 参数 limit: {}", limit);
        // 返回空列表而非错误，避免前端报错
        return Result.success(new ArrayList<>());
    }

    /**
     * 管理员获取所有帖子（包括已删除的）
     *
     * @param page    页码
     * @param size    每页数量
     * @param status  状态 (可选)
     * @param keyword 关键词 (可选)
     * @return 帖子列表分页数据
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public Result<Map<String, Object>> getAdminPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = postService.getAdminPosts(page, size, status, keyword);
        return Result.success(result);
    }

    /**
     * 管理员更新帖子状态
     *
     * @param id      帖子ID
     * @param payload 包含状态信息的请求体
     * @return 更新结果
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}/status")
    public Result<Void> updatePostStatusAdmin(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Integer status = (Integer) payload.get("status");
        if (status == null) {
            return Result.error("状态参数不能为空");
        }
        boolean success = postService.updatePostStatus(id, status);
        return success ? Result.success() : Result.error("更新状态失败");
    }

    /**
     * 管理员设置帖子精华状态
     *
     * @param id      帖子ID
     * @param payload 包含精华状态的请求体
     * @return 设置结果
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}/essence")
    public Result<Void> setPostEssence(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Integer essence = (Integer) payload.get("essence");
        if (essence == null) {
            return Result.error("精华状态参数不能为空");
        }
        boolean success = postService.setPostEssence(id, essence);
        return success ? Result.success() : Result.error("设置精华状态失败");
    }

    /**
     * 管理员设置帖子置顶状态
     *
     * @param id      帖子ID
     * @param payload 包含置顶状态的请求体
     * @return 设置结果
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}/top")
    public Result<Void> setPostTop(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Integer top = (Integer) payload.get("top");
        if (top == null) {
            return Result.error("置顶状态参数不能为空");
        }
        boolean success = postService.setPostTop(id, top);
        return success ? Result.success() : Result.error("设置置顶状态失败");
    }

    /**
     * 管理员获取评论列表（跨帖子的所有评论）
     *
     * @param page    页码
     * @param size    每页数量
     * @param status  状态筛选 (可选)
     * @param keyword 关键词筛选 (可选)
     * @return 评论列表分页数据
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/comments")
    public Result<Map<String, Object>> getAdminComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        Map<String, Object> params = new HashMap<>();
        if (status != null) {
            params.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            params.put("keyword", keyword);
        }

        Map<String, Object> result = postService.getCommentsForAdmin(params, page, size);
        return Result.success(result);
    }

    /**
     * 管理员更新评论状态
     *
     * @param postId    帖子ID
     * @param commentId 评论ID
     * @param payload   包含状态信息的请求体
     * @return 更新结果
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/comments/{postId}/{commentId}/status")
    public Result<Void> updateCommentStatusAdmin(
            @PathVariable Long postId,
            @PathVariable String commentId,
            @RequestBody Map<String, Object> payload) {

        Integer status = (Integer) payload.get("status");
        if (status == null) {
            return Result.error("状态参数不能为空");
        }

        boolean success = postService.updateCommentStatusAdmin(postId, commentId, status);
        return success ? Result.success() : Result.error("更新评论状态失败");
    }
}