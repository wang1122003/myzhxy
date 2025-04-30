package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.PostDao;
import com.campus.dao.UserDao;
import com.campus.entity.Post;
import com.campus.entity.User;
import com.campus.exception.AuthenticationException;
import com.campus.exception.CustomException;
import com.campus.exception.ResourceNotFoundException;
import com.campus.service.PostService;
import com.campus.service.UserService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 论坛帖子服务实现类
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostDao, Post> implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    @Autowired
    private PostDao postDao;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper objectMapper;

    // Define a simple POJO for comments parsed from JSON
    @Data // Lombok annotation for getters/setters/etc.
    @JsonIgnoreProperties(ignoreUnknown = true) // Ignore any extra fields in JSON
    private static class CommentPojo {
        private String id;
        private Long userId;
        private String content;
        // Assuming creationTime in JSON is String, parse if needed, or use Date/LocalDateTime
        // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Add if using Date/LocalDateTime
        private Object creationTime; // Keep as Object or use specific type
        private Integer likeCount;
        private Integer status;
        // Add other fields if present in JSON, e.g., parentId, replyToId

        // Transient fields to be populated
        private String authorName;
        private Long postId;
        private String postTitle;
    }

    @Override
    public Post getPostById(Long id) {
        Post post = getById(id);
        if (post != null) {
            loadAuthorInfo(post);
            // TODO: [评论功能] Need to decide how to handle comments_json field loading
            // loadComments(post); // Commented out as it likely uses JsonUtils or Comment entity
        }
        return post;
    }

    @Override
    public Map<String, Object> getAllPosts(int page, int size) {
        Page<Post> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Post::getCreationTime); // 通常按创建时间降序

        IPage<Post> resultPage = this.page(pageRequest, queryWrapper);
        resultPage.getRecords().forEach(this::loadAuthorInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("rows", resultPage.getRecords());
        return result;
    }

    @Override
    public List<Post> getPostsByAuthorId(Long authorId) {
        List<Post> posts = postDao.findByAuthorId(authorId);
        posts.forEach(this::loadAuthorInfo);
        return posts;
    }

    @Override
    public List<Post> getTopPosts() {
        List<Post> posts = postDao.findTop();
        posts.forEach(this::loadAuthorInfo);
        return posts;
    }

    @Override
    public List<Post> getEssencePosts() {
        List<Post> posts = postDao.findEssence();
        posts.forEach(this::loadAuthorInfo);
        return posts;
    }

    @Override
    public List<Post> getHotPosts(int limit) {
        List<Post> posts = postDao.getHotPosts(limit);
        posts.forEach(this::loadAuthorInfo);
        return posts;
    }

    @Override
    @Transactional
    public boolean createPost(Post post) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationException("无法获取当前用户信息，请登录后再发帖");
        }
        post.setUserId(currentUserId);

        post.setCreationTime(new Date());
        post.setLastUpdateTime(new Date());
        if (post.getViewCount() == null) post.setViewCount(0);
        if (post.getCommentCount() == null) post.setCommentCount(0);
        if (post.getLikeCount() == null) post.setLikeCount(0);

        if (post.getStatus() == null) post.setStatus(1);
        if (post.getIsTop() == null) post.setIsTop(0);
        if (post.getIsEssence() == null) post.setIsEssence(0);

        if (post.getCommentsJson() == null) post.setCommentsJson("[]");

        return save(post);
    }

    @Override
    @Transactional
    public boolean updatePost(Post post) {
        Post existingPost = getById(post.getId());
        if (existingPost == null) {
            throw new ResourceNotFoundException("要更新的帖子不存在: " + post.getId());
        }
        Long currentUserId = getCurrentUserId();
        if (!existingPost.getUserId().equals(currentUserId)) {
            // Add check for Admin role - Admins should be able to update any post
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
            throw new AuthenticationException("无权修改此帖子");
            }
        }

        post.setLastUpdateTime(new Date());
        // Keep original author and creation time
        post.setUserId(existingPost.getUserId());
        post.setCreationTime(existingPost.getCreationTime());
        // Keep counts managed by other methods
        post.setViewCount(existingPost.getViewCount());
        post.setCommentCount(existingPost.getCommentCount());
        post.setLikeCount(existingPost.getLikeCount());

        return updateById(post);
    }

    @Override
    @Transactional
    public boolean deletePost(Long id) {
        Post existingPost = getById(id);
        if (existingPost == null) {
            log.info("尝试删除的帖子不存在，视为成功: {}", id);
            return true;
        }
        Long currentUserId = getCurrentUserId();
        // Allow admin to delete any post
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));

        if (!existingPost.getUserId().equals(currentUserId) && !isAdmin) {
            throw new AuthenticationException("无权删除此帖子");
        }
        // TODO: Implement soft delete by changing status or have a dedicated method
        return removeById(id);
    }

    @Transactional
    public boolean incrementCommentCount(Long id) {
        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .setSql("comment_count = comment_count + 1"));
    }

    @Transactional
    public boolean incrementLikeCount(Long id) {
        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .setSql("like_count = like_count + 1"));
    }

    @Override
    public Map<String, Object> getPostsByUserId(Long userId, int page, int size) {
        Page<Post> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getUserId, userId);
        queryWrapper.orderByDesc(Post::getCreationTime);

        IPage<Post> resultPage = this.page(pageRequest, queryWrapper);
        resultPage.getRecords().forEach(this::loadAuthorInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("rows", resultPage.getRecords());
        return result;
    }

    @Override
    public Map<String, Object> getMyPosts(int page, int size) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationException("用户未登录，无法获取我的帖子");
        }
        return getPostsByUserId(currentUserId, page, size);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                User user = userService.findByUsername(username); // Ensure findByUsername exists and works
                return (user != null) ? user.getId() : null;
            } else if (principal instanceof String) {
                User user = userService.findByUsername((String) principal);
                return (user != null) ? user.getId() : null;
            }
        }
        log.warn("无法从 SecurityContext 获取当前用户ID。");
        return null;
    }

    @Override
    public Map<String, Object> searchPosts(String keyword, int page, int size) {
        Page<Post> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            // Search in title, content, and author's username/realName
            queryWrapper.and(qw -> qw.like(Post::getTitle, keyword)
                    .or().like(Post::getContent, keyword)
                    .or().apply("user_id IN (SELECT id FROM user WHERE username LIKE {0} OR real_name LIKE {0})", "%" + keyword + "%")
            );
        }
        queryWrapper.orderByDesc(Post::getCreationTime);

        IPage<Post> resultPage = this.page(pageRequest, queryWrapper);
        resultPage.getRecords().forEach(this::loadAuthorInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("rows", resultPage.getRecords());
        return result;
    }

    @Override
    public List<Map<String, Object>> getCommentsByPostId(Long postId) {
        Post post = getById(postId);
        if (post == null || post.getCommentsJson() == null || "[]".equals(post.getCommentsJson().trim())) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, Object>> comments = objectMapper.readValue(
                    post.getCommentsJson(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            // Load author names for comments
            Set<Long> userIds = comments.stream()
                    .map(comment -> ((Number) comment.get("userId")).longValue())
                    .collect(Collectors.toSet());

            if (!userIds.isEmpty()) {
                Map<Long, String> userMap = userService.getUsersByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getRealName)); // Assuming real name is preferred

                comments.forEach(comment -> {
                    Long userId = ((Number) comment.get("userId")).longValue();
                    comment.put("authorName", userMap.getOrDefault(userId, "未知用户"));
                });
            }

            return comments;
        } catch (IOException e) {
            log.error("解析帖子 {} 的评论 JSON 失败: {}", postId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public boolean addComment(Long postId, String content) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationException("用户未登录，无法评论");
        }

        Post post = getById(postId);
        if (post == null) {
            throw new ResourceNotFoundException("帖子不存在: " + postId);
        }

        try {
            List<Map<String, Object>> comments = new ArrayList<>();
            if (StringUtils.hasText(post.getCommentsJson()) && !"[]".equals(post.getCommentsJson().trim())) {
                comments = objectMapper.readValue(
                        post.getCommentsJson(),
                        new TypeReference<List<Map<String, Object>>>() {
                        }
                );
            }

            Map<String, Object> newComment = new HashMap<>();
            newComment.put("id", UUID.randomUUID().toString()); // Use UUID for comment ID
            newComment.put("userId", currentUserId);
            newComment.put("content", content);
            newComment.put("creationTime", LocalDateTime.now().toString()); // Store as ISO string
            newComment.put("likeCount", 0);
            newComment.put("status", 1); // Assuming 1 is active

            comments.add(newComment);
            String updatedCommentsJson = objectMapper.writeValueAsString(comments);

            boolean updateSuccess = update(Wrappers.<Post>lambdaUpdate()
                    .eq(Post::getId, postId)
                    .set(Post::getCommentsJson, updatedCommentsJson)
                    // Also increment comment count
                    .setSql("comment_count = comment_count + 1"));

            return updateSuccess;
        } catch (IOException e) {
            log.error("添加评论到帖子 {} 失败: {}", postId, e.getMessage());
            throw new CustomException("处理评论数据时出错", e);
        }
    }

    @Override
    @Transactional
    public boolean deleteComment(Long postId, String commentIdStr) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationException("用户未登录，无法删除评论");
        }

        Post post = getById(postId);
        if (post == null || !StringUtils.hasText(post.getCommentsJson()) || "[]".equals(post.getCommentsJson().trim())) {
            throw new ResourceNotFoundException("帖子或评论不存在");
        }

        try {
            List<Map<String, Object>> comments = objectMapper.readValue(
                    post.getCommentsJson(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            Optional<Map<String, Object>> commentToDeleteOpt = comments.stream()
                    .filter(c -> commentIdStr.equals(c.get("id")))
                    .findFirst();

            if (commentToDeleteOpt.isEmpty()) {
                throw new ResourceNotFoundException("评论不存在: " + commentIdStr);
            }

            Map<String, Object> commentToDelete = commentToDeleteOpt.get();
            Long commentAuthorId = ((Number) commentToDelete.get("userId")).longValue();

            // Check permission: User can delete their own comment, or admin can delete any
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));

            if (!currentUserId.equals(commentAuthorId) && !isAdmin) {
                throw new AuthenticationException("无权删除此评论");
            }

            boolean removed = comments.removeIf(c -> commentIdStr.equals(c.get("id")));

            if (removed) {
                String updatedCommentsJson = objectMapper.writeValueAsString(comments);
                boolean updateSuccess = update(Wrappers.<Post>lambdaUpdate()
                        .eq(Post::getId, postId)
                        .set(Post::getCommentsJson, updatedCommentsJson)
                        // Also decrement comment count
                        .setSql("comment_count = GREATEST(0, comment_count - 1)")); // Ensure count >= 0

                return updateSuccess;
            } else {
                log.warn("尝试删除帖子 {} 中的评论 {} 时未找到该评论", postId, commentIdStr);
                return false; // Should not happen if findFirst was successful
            }
        } catch (IOException e) {
            log.error("删除帖子 {} 中的评论 {} 失败: {}", postId, commentIdStr, e.getMessage());
            throw new CustomException("处理评论数据时出错", e);
        }
    }

    @Override
    public Map<String, Object> getPostStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", postDao.countAll());
        // Add more stats if needed, e.g., active posts, comments count
        // stats.put("activePosts", postDao.countByStatus(1));
        return stats;
    }

    @Override
    public Post getPostDetail(Long id) {
            incrementViewCount(id);
        return getPostById(id);
    }

    @Override
    @Transactional
    public boolean likePost(Long id) {
        // TODO: Add logic to prevent multiple likes from the same user
        // (Requires tracking likes, e.g., in a separate table or user's liked posts list)
        log.info("帖子 {} 被点赞 (计数增加)", id);
        return incrementLikeCount(id);
    }

    @Override
    @Transactional
    public boolean unlikePost(Long id) {
        // TODO: Add logic to check if the user actually liked the post before unliking
        // (Requires tracking likes)
        log.info("帖子 {} 被取消点赞 (计数减少)", id);
        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .setSql("like_count = GREATEST(0, like_count - 1)")); // Ensure count >= 0
    }

    @Override
    @Transactional
    public boolean batchDeletePosts(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return true;
        }
        // TODO: Add permission checks - only admin should batch delete?
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AuthenticationException("无权执行批量删除操作");
        }

        // TODO: Implement soft delete or add pre-delete checks
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional
    public boolean updatePostStatus(Long id, Integer status) {
        // TODO: Add permission checks - only admin?
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AuthenticationException("无权修改帖子状态");
        }
        // TODO: Validate status value (e.g., ensure it's one of the allowed statuses)

        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .set(Post::getStatus, status)
                .set(Post::getLastUpdateTime, new Date()));
    }

    @Override
    @Transactional
    public boolean setPostTop(Long id, Integer isTop) {
        // TODO: Add permission checks - only admin?
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AuthenticationException("无权设置帖子置顶");
        }

        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .set(Post::getIsTop, isTop)
                .set(Post::getLastUpdateTime, new Date()));
    }

    @Override
    @Transactional
    public boolean setPostEssence(Long id, Integer isEssence) {
        // TODO: Add permission checks - only admin?
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AuthenticationException("无权设置帖子精华");
        }

        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .set(Post::getIsEssence, isEssence)
                .set(Post::getLastUpdateTime, new Date()));
    }

    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        // No permission check usually needed for incrementing views
        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .setSql("view_count = view_count + 1"));
    }

    @Override
    public Map<String, Object> findPageMap(Map<String, Object> params, int page, int size) {
        Page<Map<String, Object>> pageRequest = new Page<>(page, size);
        // Call the custom DAO method
        IPage<Map<String, Object>> resultPage = postDao.findPageMap(pageRequest, params);

        Map<String, Object> result = new HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("rows", resultPage.getRecords());
        return result;

        /* Example using BaseMapper and LambdaQueryWrapper (less flexible for joins):
        Page<Post> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = Wrappers.<Post>lambdaQuery();

        // Apply filters from params map
        if (params.containsKey("keyword") && StringUtils.hasText((String)params.get("keyword"))) {
            String keyword = (String) params.get("keyword");
            queryWrapper.and(qw -> qw.like(Post::getTitle, keyword)
                                       .or().like(Post::getContent, keyword)
                                       // TODO: Add author search if needed (requires join or subquery)
            );
        }
        if (params.containsKey("category") && StringUtils.hasText((String)params.get("category"))) {
            queryWrapper.eq(Post::getCategory, params.get("category"));
        }
        if (params.containsKey("tag") && StringUtils.hasText((String)params.get("tag"))) {
            // Assuming tags is a JSON array string
            queryWrapper.apply("JSON_CONTAINS(tags, JSON_ARRAY({0}))", params.get("tag"));
        }
        if (params.containsKey("status")) {
            queryWrapper.eq(Post::getStatus, params.get("status"));
        }
        if (params.containsKey("userId")) {
            queryWrapper.eq(Post::getUserId, params.get("userId"));
        }

        // Apply sorting
        String sortBy = (String) params.getOrDefault("sortBy", "createTime");
        boolean isAsc = "asc".equalsIgnoreCase((String) params.getOrDefault("sortOrder", "desc"));
        switch (sortBy) {
            case "viewCount":
                queryWrapper.orderBy(true, isAsc, Post::getViewCount);
                break;
            case "likeCount":
                queryWrapper.orderBy(true, isAsc, Post::getLikeCount);
                break;
            case "commentCount":
                queryWrapper.orderBy(true, isAsc, Post::getCommentCount);
                break;
            case "updateTime":
                queryWrapper.orderBy(true, isAsc, Post::getLastUpdateTime);
                break;
            case "createTime":
            default:
                queryWrapper.orderBy(true, isAsc, Post::getCreationTime);
                break;
        }

        IPage<Post> resultPage = this.page(pageRequest, queryWrapper);
        resultPage.getRecords().forEach(this::loadAuthorInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("rows", resultPage.getRecords());
        return result;
        */
    }

    // --- Admin Management Methods Implementation ---
    @Override
    public Map<String, Object> getAdminPosts(int page, int size, Integer status, String keyword) {
        Map<String, Object> params = new HashMap<>();
        if (status != null) {
            params.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            params.put("keyword", keyword);
        }
        // Ensure admin query doesn't filter by default active status if not specified
        params.put("ignoreStatusFilter", true); // Example flag for DAO query

        // Use the general findPageMap method
        return findPageMap(params, page, size);
    }

    @Override
    public Map<String, Object> getCommentsForAdmin(Map<String, Object> params, int page, int size) {
        // Goal: Fetch all comments across all posts, paginated, with filters, user info, post info.
        // This is complex with the current JSON storage.
        // Option 1: Iterate through posts (inefficient for pagination/filtering)
        // Option 2: Database-level JSON queries (complex, DB specific)
        // Option 3: Refactor comments to a separate table (Recommended)

        log.warn("getCommentsForAdmin: 基于当前JSON存储的实现效率低下或不完整。");

        // --- Simplified Example (Potentially Inefficient & Incomplete) ---
        // This example fetches ALL posts, extracts ALL comments, then filters/pages in memory.
        // **NOT SUITABLE FOR PRODUCTION with many posts/comments.**

        List<Post> allPosts = list(); // Fetch all posts (could be huge)
        List<CommentPojo> allComments = new ArrayList<>();

        for (Post post : allPosts) {
            if (StringUtils.hasText(post.getCommentsJson()) && !"[]".equals(post.getCommentsJson().trim())) {
            try {
                List<CommentPojo> postComments = objectMapper.readValue(
                        post.getCommentsJson(),
                        new TypeReference<List<CommentPojo>>() {}
                );
                // Add post info to each comment
                postComments.forEach(c -> {
                    c.setPostId(post.getId());
                    c.setPostTitle(post.getTitle());
                });
                allComments.addAll(postComments);
            } catch (IOException e) {
                log.error("解析帖子 {} 的评论 JSON 失败 (管理员获取): {}", post.getId(), e.getMessage());
            }
            }
        }

        // Populate author names
        Set<Long> userIds = allComments.stream().map(CommentPojo::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            userMap = userService.getUsersByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getRealName, (name1, name2) -> name1)); // Use real name
        }
        final Map<Long, String> finalUserMap = userMap;
        allComments.forEach(c -> c.setAuthorName(finalUserMap.getOrDefault(c.getUserId(), "未知用户")));

        // Apply filters (example: status and keyword on content/author)
        String keywordFilter = (String) params.getOrDefault("keyword", "");
        Integer statusFilter = (Integer) params.get("status");

        List<CommentPojo> filteredComments = allComments.stream()
                .filter(c -> statusFilter == null || statusFilter.equals(c.getStatus()))
                .filter(c -> !StringUtils.hasText(keywordFilter) ||
                        (c.getContent() != null && c.getContent().contains(keywordFilter)) ||
                        (c.getAuthorName() != null && c.getAuthorName().contains(keywordFilter)))
                .collect(Collectors.toList());

        // Apply pagination in memory
        int total = filteredComments.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<CommentPojo> paginatedComments = (start < total) ? filteredComments.subList(start, end) : Collections.emptyList();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("rows", paginatedComments);
        return result;
    }

    @Override
    @Transactional
    public boolean updateCommentStatusAdmin(Long postId, String commentId, Integer newStatus) {
        // Requires fetching the post, parsing JSON, updating the specific comment, and saving back.
        Post post = getById(postId);
        if (post == null || !StringUtils.hasText(post.getCommentsJson()) || "[]".equals(post.getCommentsJson().trim())) {
            throw new ResourceNotFoundException("帖子或评论不存在");
        }

        try {
            List<Map<String, Object>> comments = objectMapper.readValue(
                    post.getCommentsJson(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            boolean updated = false;
            for (Map<String, Object> comment : comments) {
                if (commentId.equals(comment.get("id"))) {
                    comment.put("status", newStatus);
                    updated = true;
                    break;
                }
            }

            if (updated) {
                String updatedCommentsJson = objectMapper.writeValueAsString(comments);
                return update(Wrappers.<Post>lambdaUpdate()
                        .eq(Post::getId, postId)
                        .set(Post::getCommentsJson, updatedCommentsJson));
            } else {
                throw new ResourceNotFoundException("评论未找到: " + commentId);
            }
        } catch (IOException e) {
            log.error("管理员更新帖子 {} 评论 {} 状态失败: {}", postId, commentId, e.getMessage());
            throw new CustomException("处理评论数据时出错", e);
        }
    }

    @Override
    public List<Map<String, Object>> getPopularTags(int limit) {
        // Since tag functionality is removed, return an empty list
        return new ArrayList<>();
        // return postDao.getPopularTagCounts(limit); // Original code
    }

    private void loadAuthorInfo(Post post) {
        if (post != null && post.getUserId() != null) {
            User author = userService.getUserById(post.getUserId());
            if (author != null) {
                // 设置 authorName 字段
                post.setAuthorName(StringUtils.hasText(author.getRealName()) ? author.getRealName() : author.getUsername());
                // 如果需要头像，可以类似地设置 authorAvatar 字段
                // post.setAuthorAvatar(author.getAvatarUrl()); 
            } else {
                // Handle case where author is not found
                post.setAuthorName("未知用户");
            }
        }
    }
}