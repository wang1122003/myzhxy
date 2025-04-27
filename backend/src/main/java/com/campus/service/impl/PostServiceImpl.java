package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.PostDao;
import com.campus.dao.UserDao;
import com.campus.entity.Post;
import com.campus.entity.User;
import com.campus.exception.AuthenticationException;
import com.campus.exception.CustomException;
import com.campus.service.PostService;
import com.campus.service.UserService;
import com.campus.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.time.LocalDateTime;

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
            throw new AuthenticationException("无权修改此帖子");
        }

        post.setLastUpdateTime(new Date());
        post.setUserId(existingPost.getUserId());
        post.setCreationTime(existingPost.getCreationTime());
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
        if (!existingPost.getUserId().equals(currentUserId)) {
            throw new AuthenticationException("无权删除此帖子");
        }
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
                User user = userService.findByUsername(username);
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
        queryWrapper.like(StringUtils.hasText(keyword), Post::getTitle, keyword)
                .or()
                .like(StringUtils.hasText(keyword), Post::getContent, keyword);
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
        Post post = this.getById(postId);
        if (post == null || !StringUtils.hasText(post.getCommentsJson())) {
            return Collections.emptyList();
        }
        try {
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {
            };
            List<Map<String, Object>> comments = objectMapper.readValue(post.getCommentsJson(), typeRef);

            if (comments != null) {
                comments.forEach(comment -> {
                    Object userIdObj = comment.get("userId");
                    if (userIdObj instanceof Number) {
                        User commentAuthor = userService.getUserById(((Number) userIdObj).longValue());
                        if (commentAuthor != null) {
                            comment.put("authorName", commentAuthor.getRealName() != null ? commentAuthor.getRealName() : commentAuthor.getUsername());
                        } else {
                            comment.put("authorName", "未知用户");
                        }
                    }
                });
            }

            return comments != null ? comments : Collections.emptyList();
        } catch (IOException e) {
            log.error("解析帖子评论JSON失败, postId: {}, json: {}", postId, post.getCommentsJson(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public boolean addComment(Long postId, String content) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationException("无法获取当前用户信息，请重新登录后评论");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }

        Post post = this.getById(postId);
        if (post == null) {
            throw new ResourceNotFoundException("帖子不存在: " + postId);
        }

        try {
            List<Map<String, Object>> comments;
            String existingJson = post.getCommentsJson();

            if (StringUtils.hasText(existingJson) && !existingJson.trim().equals("null")) {
                try {
                    TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {
                    };
                    comments = objectMapper.readValue(existingJson, typeRef);
                    if (comments == null) {
                        comments = new ArrayList<>();
                    }
                } catch (IOException parseEx) {
                    log.error("解析现有评论JSON失败, postId: {}, json: {}", postId, existingJson, parseEx);
                    comments = new ArrayList<>();
                }
            } else {
                comments = new ArrayList<>();
            }

            Map<String, Object> newComment = new HashMap<>();
            newComment.put("id", UUID.randomUUID().toString());
            newComment.put("userId", currentUserId);
            newComment.put("content", content);
            newComment.put("creationTime", new Date());

            comments.add(newComment);

            String updatedJson = objectMapper.writeValueAsString(comments);

            post.setCommentsJson(updatedJson);
            post.setCommentCount(comments.size());
            post.setLastUpdateTime(new Date());

            boolean updated = this.updateById(post);
            return updated;

        } catch (IOException e) {
            log.error("序列化评论JSON失败, postId: {}", postId, e);
            throw new CustomException("添加评论失败 (JSON序列化错误)");
        } catch (Exception e) {
            log.error("添加评论时发生未知错误, postId: {}", postId, e);
            throw new CustomException("添加评论时发生未知错误: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteComment(Long postId, String commentIdStr) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationException("请登录后操作");
        }
        if (!StringUtils.hasText(commentIdStr)) {
            throw new IllegalArgumentException("评论ID不能为空");
        }

        Post post = this.getById(postId);
        if (post == null) {
            throw new ResourceNotFoundException("帖子不存在: " + postId);
        }
        if (!StringUtils.hasText(post.getCommentsJson())) {
            throw new ResourceNotFoundException("帖子没有评论可删除");
        }

        try {
            List<Map<String, Object>> comments;
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {
            };
            try {
                comments = objectMapper.readValue(post.getCommentsJson(), typeRef);
            } catch (IOException parseEx) {
                log.error("解析现有评论JSON失败 (删除时), postId: {}, json: {}", postId, post.getCommentsJson(), parseEx);
                throw new CustomException("删除评论失败 (无法解析评论数据)");
            }

            if (comments.isEmpty()) {
                throw new ResourceNotFoundException("帖子没有评论可删除");
            }

            Map<String, Object> commentToRemove = null;
            Iterator<Map<String, Object>> iterator = comments.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> comment = iterator.next();
                Object idObj = comment.get("id");
                if (idObj instanceof String && commentIdStr.equals(idObj)) {
                    Object authorIdObj = comment.get("userId");
                    if (authorIdObj instanceof Number && ((Number) authorIdObj).longValue() == currentUserId) {
                        commentToRemove = comment;
                        iterator.remove();
                        break;
                    } else {
                        log.warn("用户 {} 尝试删除不属于自己的评论 (commentId: {}, authorId: {}), postId: {}", currentUserId, commentIdStr, authorIdObj, postId);
                        throw new AuthenticationException("无权删除此评论");
                    }
                }
            }

            if (commentToRemove == null) {
                log.warn("尝试删除评论但未找到, postId: {}, commentId: {}", postId, commentIdStr);
                throw new ResourceNotFoundException("评论未找到: " + commentIdStr);
            }

            String updatedJson = objectMapper.writeValueAsString(comments);

            post.setCommentsJson(updatedJson);
            post.setCommentCount(comments.size());
            post.setLastUpdateTime(new Date());

            return this.updateById(post);

        } catch (IOException e) {
            log.error("序列化评论JSON失败 (删除时), postId: {}", postId, e);
            throw new CustomException("删除评论失败 (JSON序列化错误)");
        } catch (AuthenticationException | ResourceNotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除评论时发生未知错误, postId: {}, commentId: {}", postId, commentIdStr, e);
            throw new CustomException("删除评论时发生未知错误: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getPostStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", count());
        stats.put("activePosts", count(new LambdaQueryWrapper<Post>().eq(Post::getStatus, 1)));
        stats.put("topPosts", count(new LambdaQueryWrapper<Post>().eq(Post::getIsTop, 1)));
        stats.put("essencePosts", count(new LambdaQueryWrapper<Post>().eq(Post::getIsEssence, 1)));
        return stats;
    }

    @Override
    public Map<String, Object> getPostsByCategory(String category, int page, int size) {
        Page<Post> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getCategory, category);
        queryWrapper.orderByDesc(Post::getCreationTime);

        IPage<Post> resultPage = this.page(pageRequest, queryWrapper);
        resultPage.getRecords().forEach(this::loadAuthorInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("rows", resultPage.getRecords());
        return result;
    }

    @Override
    public Post getPostDetail(Long id) {
        Post post = getById(id);
        if (post != null) {
            incrementViewCount(id);
            loadAuthorInfo(post);
        }
        return post;
    }

    @Override
    public IPage<Post> getPostsByTag(String tag, Page<Post> page) {
        if (!StringUtils.hasText(tag)) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Post::getTags, '"' + tag + '"');
        queryWrapper.orderByDesc(Post::getCreationTime);

        IPage<Post> resultPage = this.page(page, queryWrapper);
        resultPage.getRecords().forEach(this::loadAuthorInfo);
        return resultPage;
    }

    @Override
    public List<String> getHotTags(int limit) {
        List<Post> postsWithTags = list(new LambdaQueryWrapper<Post>().select(Post::getTags).isNotNull(Post::getTags));
        Map<String, Integer> tagCounts = new HashMap<>();

        for (Post post : postsWithTags) {
            List<String> tags = post.getTags();
            if (tags != null) {
                for (String tag : tags) {
                    if (StringUtils.hasText(tag)) {
                        tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                    }
                }
            }
        }

        return tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit > 0 ? limit : 10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void loadAuthorInfo(Post post) {
        if (post != null && post.getUserId() != null) {
            try {
                User author = userService.getUserById(post.getUserId());
                if (author != null) {
                    post.setAuthorName(StringUtils.hasText(author.getRealName()) ? author.getRealName() : author.getUsername());
                } else {
                    log.warn("无法加载帖子作者信息，用户ID不存在: {}", post.getUserId());
                    post.setAuthorName("未知用户");
                }
            } catch (Exception e) {
                log.error("加载帖子作者信息时出错, userId: {}", post.getUserId(), e);
                post.setAuthorName("信息加载错误");
            }
        }
    }

    @Override
    public List<String> listAvailableForumTypes() {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT category")
                .isNotNull("category")
                .ne("category", "");

        List<Object> categoryObjects = postDao.selectObjs(queryWrapper);

        return categoryObjects.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean likePost(Long id) {
        Post post = getById(id);
        if (post == null) {
            throw new ResourceNotFoundException("帖子不存在: " + id);
        }
        boolean success = update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .setSql("like_count = like_count + 1"));
        if (!success) {
            log.warn("点赞帖子失败 (可能并发更新或数据库错误): {}", id);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean unlikePost(Long id) {
        Post post = getById(id);
        if (post == null) {
            throw new ResourceNotFoundException("帖子不存在: " + id);
        }
        boolean success = update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .gt(Post::getLikeCount, 0)
                .setSql("like_count = like_count - 1"));

        if (!success) {
            Post currentPost = getById(id);
            if (currentPost != null && currentPost.getLikeCount() == 0) {
                log.info("尝试取消点赞时帖子点赞数已为0, postId: {}", id);
                return true;
            } else {
                log.warn("取消点赞帖子失败 (可能并发更新或数据库错误): {}", id);
            }
        }
        return success;
    }

    @Override
    @Transactional
    public boolean batchDeletePosts(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return true;
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new AuthenticationException("请登录后操作");
        }

        List<Long> idsList = Arrays.asList(ids);
        List<Post> postsToDelete = listByIds(idsList);

        if (postsToDelete.size() != idsList.size()) {
            List<Long> foundIds = postsToDelete.stream().map(Post::getId).collect(Collectors.toList());
            List<Long> notFoundIds = idsList.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            log.warn("批量删除帖子时，部分帖子未找到: {}", notFoundIds);
        }

        for (Post post : postsToDelete) {
            if (!post.getUserId().equals(currentUserId)) {
                log.error("用户 {} 尝试批量删除不属于自己的帖子 (ID: {})", currentUserId, post.getId());
                throw new AuthenticationException("无权删除部分或全部选定帖子 (ID: " + post.getId() + ")");
            }
        }

        return removeByIds(idsList);
    }

    @Override
    @Transactional
    public boolean updatePostStatus(Long id, Integer status) {
        Post post = getById(id);
        if (post == null) {
            throw new ResourceNotFoundException("帖子不存在: " + id);
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("无效的帖子状态值: " + status);
        }

        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .set(Post::getStatus, status)
                .set(Post::getLastUpdateTime, new Date()));
    }

    @Override
    @Transactional
    public boolean setPostTop(Long id, Integer isTop) {
        Post post = getById(id);
        if (post == null) {
            throw new ResourceNotFoundException("帖子不存在: " + id);
        }
        if (isTop == null || (isTop != 0 && isTop != 1)) {
            throw new IllegalArgumentException("无效的置顶状态值: " + isTop);
        }

        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .set(Post::getIsTop, isTop)
                .set(Post::getLastUpdateTime, new Date()));
    }

    @Override
    @Transactional
    public boolean setPostEssence(Long id, Integer isEssence) {
        Post post = getById(id);
        if (post == null) {
            throw new ResourceNotFoundException("帖子不存在: " + id);
        }
        if (isEssence == null || (isEssence != 0 && isEssence != 1)) {
            throw new IllegalArgumentException("无效的精华状态值: " + isEssence);
        }

        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .set(Post::getIsEssence, isEssence)
                .set(Post::getLastUpdateTime, new Date()));
    }

    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .setSql("view_count = view_count + 1"));
    }

    @Override
    public Map<String, Object> findPageMap(Map<String, Object> params, int page, int size) {
        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = Wrappers.<Post>lambdaQuery();

        String title = (String) params.get("title");
        String authorName = (String) params.get("authorName");
        String category = (String) params.get("category");
        Integer status = params.containsKey("status") ? (Integer) params.get("status") : null;
        Integer isTop = params.containsKey("isTop") ? (Integer) params.get("isTop") : null;
        Integer isEssence = params.containsKey("isEssence") ? (Integer) params.get("isEssence") : null;

        if (StringUtils.hasText(title)) {
            queryWrapper.like(Post::getTitle, title);
        }
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(Post::getCategory, category);
        }
        if (status != null) {
            queryWrapper.eq(Post::getStatus, status);
        }
        if (isTop != null) {
            queryWrapper.eq(Post::getIsTop, isTop);
        }
        if (isEssence != null) {
            queryWrapper.eq(Post::getIsEssence, isEssence);
        }

        if (StringUtils.hasText(authorName)) {
            List<User> users = userDao.selectList(Wrappers.<User>lambdaQuery().like(User::getUsername, authorName));
            if (!users.isEmpty()) {
                List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
                queryWrapper.in(Post::getUserId, userIds);
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("total", 0L);
                result.put("rows", Collections.emptyList());
                return result;
            }
        }

        queryWrapper.orderByDesc(Post::getIsTop, Post::getIsEssence, Post::getCreationTime);

        IPage<Post> resultPage = this.page(postPage, queryWrapper);

        List<Post> posts = resultPage.getRecords();
        if (!posts.isEmpty()) {
            List<Long> authorIds = posts.stream().map(Post::getUserId).distinct().collect(Collectors.toList());
            Map<Long, User> authorMap = userDao.selectBatchIds(authorIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
            posts.forEach(p -> {
                User author = authorMap.get(p.getUserId());
                if (author != null) {
                    p.setAuthorName(author.getUsername());
                }
            });
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("rows", posts);
        return result;
    }

    // --- Admin Comment Management Implementation ---

    @Override
    public Map<String, Object> getCommentsForAdmin(Map<String, Object> params, int page, int size) {
        Integer filterStatus = (Integer) params.get("status");
        String keyword = (String) params.get("keyword");
        log.info("Fetching comments for admin. Page: {}, Size: {}, Status: {}, Keyword: {}", page, size, filterStatus, keyword);

        // 1. 获取有评论的帖子 (优化：只查询 comments_json 非空/非 '[]' 的帖子)
        LambdaQueryWrapper<Post> postQuery = Wrappers.<Post>lambdaQuery()
                .isNotNull(Post::getCommentsJson)
                .ne(Post::getCommentsJson, "[]");
        // 如果需要按帖子分类等过滤，可以在这里添加条件
        List<Post> postsWithComments = list(postQuery);
        log.debug("Found {} posts with non-empty comments field.", postsWithComments.size());

        List<CommentPojo> allFilteredComments = new ArrayList<>();

        // 2. 遍历帖子，解析并过滤评论
        for (Post post : postsWithComments) {
            if (!StringUtils.hasText(post.getCommentsJson())) continue; // Double check

            try {
                List<CommentPojo> comments = objectMapper.readValue(
                        post.getCommentsJson(),
                        new TypeReference<List<CommentPojo>>() {}
                );

                for (CommentPojo comment : comments) {
                    // 3. 应用过滤条件
                    boolean statusMatch = (filterStatus == null) || filterStatus.equals(comment.getStatus());
                    boolean keywordMatch = !StringUtils.hasText(keyword) ||
                            (comment.getContent() != null && comment.getContent().toLowerCase().contains(keyword.toLowerCase()));

                    if (statusMatch && keywordMatch) {
                        // 4. 补充评论信息
                        comment.setPostId(post.getId());
                        comment.setPostTitle(post.getTitle());
                        if (comment.getUserId() != null) {
                            try {
                                User author = userService.getUserById(comment.getUserId());
                                comment.setAuthorName(author != null ? (author.getRealName() != null ? author.getRealName() : author.getUsername()) : "未知用户(" + comment.getUserId() + ")");
                            } catch (Exception userEx) {
                                log.warn("Error fetching user info for userId {}: {}", comment.getUserId(), userEx.getMessage());
                                comment.setAuthorName("用户加载错误");
                            }
                        } else {
                             comment.setAuthorName("匿名用户");
                        }
                        allFilteredComments.add(comment);
                    }
                }
            } catch (IOException e) {
                log.error("解析帖子 ID {} 的评论 JSON 失败: {}. JSON: {}", post.getId(), e.getMessage(), post.getCommentsJson().substring(0, Math.min(200, post.getCommentsJson().length())));
            }
        }
        log.debug("Total filtered comments across all posts: {}", allFilteredComments.size());

        // 5. 手动进行分页 (可以考虑按评论创建时间排序)
        // Comparator<CommentPojo> comparator = Comparator.comparing(c -> c.getCreationTime(), Comparator.nullsLast(Comparator.reverseOrder())); // Requires creationTime to be Comparable
        // allFilteredComments.sort(comparator);

        int total = allFilteredComments.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<CommentPojo> paginatedComments = (fromIndex < total && fromIndex >= 0)
                ? allFilteredComments.subList(fromIndex, toIndex)
                : Collections.emptyList();
        log.info("Returning {} comments for page {} (total found: {}).", paginatedComments.size(), page, total);

        // 6. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", (long) total);
        result.put("rows", paginatedComments); // Use CommentPojo list directly
        return result;
    }

    @Override
    @Transactional
    public boolean updateCommentStatusAdmin(Long postId, String commentId, Integer newStatus) {
        log.info("Attempting to update status for comment {} in post {} to {}", commentId, postId, newStatus);
        if (postId == null || !StringUtils.hasText(commentId) || newStatus == null) {
            log.error("Invalid arguments for updating comment status: postId={}, commentId={}, newStatus={}", postId, commentId, newStatus);
            throw new IllegalArgumentException("无效的参数：postId, commentId 和 newStatus 不能为空");
        }

        Post post = getById(postId);
        if (post == null) {
            log.error("Update comment status failed: Post not found with ID {}", postId);
            throw new ResourceNotFoundException("帖子不存在: " + postId);
        }

        if (!StringUtils.hasText(post.getCommentsJson()) || "[]".equals(post.getCommentsJson())) {
            log.warn("Update comment status: Post {} has no comments field or it's empty.", postId);
            throw new ResourceNotFoundException("帖子没有评论可更新状态");
        }

        try {
            List<CommentPojo> comments = objectMapper.readValue(
                    post.getCommentsJson(),
                    new TypeReference<List<CommentPojo>>() {}
            );

            Optional<CommentPojo> commentToUpdate = comments.stream()
                    .filter(c -> commentId.equals(c.getId()))
                    .findFirst();

            if (commentToUpdate.isPresent()) {
                CommentPojo comment = commentToUpdate.get();
                log.debug("Found comment to update: {}", comment);
                comment.setStatus(newStatus);
                log.info("Updated status for comment {} in post {} to {}", commentId, postId, newStatus);
            } else {
                log.error("Update comment status failed: Comment with ID {} not found in post {}", commentId, postId);
                throw new ResourceNotFoundException("评论未找到: " + commentId);
            }

            // 将更新后的列表序列化回 JSON
            String updatedCommentsJson = objectMapper.writeValueAsString(comments);

            // 更新帖子 (只更新必要字段)
            boolean success = update(Wrappers.<Post>lambdaUpdate()
                    .eq(Post::getId, postId)
                    .set(Post::getCommentsJson, updatedCommentsJson)
                    .set(Post::getLastUpdateTime, new Date()));

            if (!success) {
                log.error("Database update failed for post {} after updating comment status.", postId);
                // 这个错误比较严重，可能需要更具体的异常类型或处理
                throw new CustomException("更新数据库失败");
            }

            log.info("Successfully updated status for comment {} in post {}", commentId, postId);
            return true;

        } catch (IOException e) {
            log.error("JSON processing error while updating comment status for post {}: {}", postId, e.getMessage(), e);
            throw new CustomException("更新评论状态失败：处理评论数据时出错");
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
             throw e; // Re-throw specific exceptions
        } catch (Exception e) {
            log.error("Unexpected error while updating comment status for post {}: {}", postId, e.getMessage(), e);
            throw new CustomException("更新评论状态时发生未知错误");
        }
    }
}