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
import java.text.SimpleDateFormat;
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

    @Override
    public Post getPostById(Long id) {
        Post post = getById(id);
        if (post != null) {
            loadAuthorInfo(post);
            // 加载评论数据
            loadComments(post);
        }
        return post;
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
            // 添加管理员角色检查 - 管理员应该能够更新任何帖子
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
            throw new AuthenticationException("无权修改此帖子");
            }
        }

        post.setLastUpdateTime(new Date());
        // 保持原始作者和创建时间
        post.setUserId(existingPost.getUserId());
        post.setCreationTime(existingPost.getCreationTime());
        // 保持由其他方法管理的计数
        post.setViewCount(existingPost.getViewCount());
        post.setCommentCount(existingPost.getCommentCount());
        post.setLikeCount(existingPost.getLikeCount());

        return updateById(post);
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
        log.debug("开始执行getHotPosts，请求限制数量: {}", limit);
        try {
        List<Post> posts = postDao.getHotPosts(limit);
            log.debug("从DAO获取热门帖子成功，原始数量: {}", posts.size());

            try {
        posts.forEach(this::loadAuthorInfo);
                log.debug("成功加载所有帖子的作者信息");
            } catch (Exception e) {
                log.error("加载帖子作者信息时出错: {}", e.getMessage(), e);
                // 继续执行，至少返回帖子基本信息
            }
            
        return posts;
        } catch (Exception e) {
            log.error("获取热门帖子时出现异常: {}", e.getMessage(), e);
            throw e;
        }
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
    public boolean deletePost(Long id) {
        Post existingPost = getById(id);
        if (existingPost == null) {
            log.info("尝试删除的帖子不存在，视为成功: {}", id);
            return true;
        }
        Long currentUserId = getCurrentUserId();
        // 允许管理员删除任何帖子
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));

        if (!existingPost.getUserId().equals(currentUserId) && !isAdmin) {
            throw new AuthenticationException("无权删除此帖子");
        }

        // 实现软删除 - 将状态改为0（表示已删除）
        existingPost.setStatus(0);
        existingPost.setLastUpdateTime(new Date());
        return updateById(existingPost);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                User user = userService.findByUsername(username); // 确保findByUsername方法存在并工作正常
                return (user != null) ? user.getId() : null;
            } else if (principal instanceof String) {
                User user = userService.findByUsername((String) principal);
                return (user != null) ? user.getId() : null;
            }
        }
        log.warn("无法从 SecurityContext 获取当前用户ID。");
        return null;
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

    @Override
    public Map<String, Object> searchPosts(String keyword, int page, int size) {
        Page<Post> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            // 在标题、内容和作者的用户名/真实姓名中搜索
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

            // 为评论加载作者名称
            Set<Long> userIds = comments.stream()
                    .map(comment -> ((Number) comment.get("userId")).longValue())
                    .collect(Collectors.toSet());

            if (!userIds.isEmpty()) {
                Map<Long, String> userMap = userService.getUsersByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getRealName)); // 假设首选实名

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
            newComment.put("id", UUID.randomUUID().toString()); // 使用UUID作为评论ID
            newComment.put("userId", currentUserId);
            newComment.put("content", content);
            newComment.put("creationTime", LocalDateTime.now().toString()); // 存储为ISO字符串
            newComment.put("likeCount", 0);
            newComment.put("status", 1); // 假设1表示活跃状态

            comments.add(newComment);
            String updatedCommentsJson = objectMapper.writeValueAsString(comments);

            boolean updateSuccess = update(Wrappers.<Post>lambdaUpdate()
                    .eq(Post::getId, postId)
                    .set(Post::getCommentsJson, updatedCommentsJson)
                    // 同时增加评论计数
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

            // 权限检查：用户可以删除自己的评论，或管理员可以删除任何评论
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
                        // 同时减少评论计数
                        .setSql("comment_count = GREATEST(0, comment_count - 1)")); // 确保计数 >= 0

                return updateSuccess;
            } else {
                log.warn("尝试删除帖子 {} 中的评论 {} 时未找到该评论", postId, commentIdStr);
                return false; // 如果findFirst成功，则不应发生
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
        // 如果需要，添加更多统计信息，例如活跃帖子数、评论总数
        // stats.put("activePosts", postDao.countByStatus(1));
        return stats;
    }

    @Override
    public Post getPostDetail(Long id) {
        Post post = getById(id);
        if (post != null) {
            // 添加作者信息
            loadAuthorInfo(post);
            // 加载评论数据
            loadComments(post);
        }
        return post;
    }

    @Override
    @Transactional
    public boolean updatePostStatus(Long id, Integer status) {
        // 检查权限：只有管理员可以修改帖子状态
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AuthenticationException("无权修改帖子状态");
        }

        // 验证状态值是否合法
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            throw new IllegalArgumentException("无效的帖子状态值：" + status + "。允许的值：0(删除)、1(正常)、2(审核中)");
        }

        // 检查帖子是否存在
        Post existingPost = getById(id);
        if (existingPost == null) {
            throw new ResourceNotFoundException("要更新状态的帖子不存在: " + id);
        }

        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .set(Post::getStatus, status)
                .set(Post::getLastUpdateTime, new Date()));
    }

    @Override
    @Transactional
    public boolean likePost(Long id) {
        // 获取当前用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("未登录用户尝试点赞帖子 {}", id);
            throw new AuthenticationException("请登录后再点赞");
        }

        // 检查用户是否已经点赞过该帖子
        // 在实际项目中，我们需要检查用户是否已经点赞过该帖子
        // 为简化实现（因为没有点赞表），我们暂时允许重复点赞，但在日志中记录

        log.info("用户 {} 点赞帖子 {} (未检查是否重复点赞)", userId, id);

        // 增加点赞计数
        try {
            // 在实际项目中应该同时记录点赞关系
            log.info("帖子 {} 被用户 {} 点赞 (计数增加)", id, userId);
            return incrementLikeCount(id);
        } catch (Exception e) {
            log.error("点赞失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean unlikePost(Long id) {
        // 获取当前用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("未登录用户尝试取消点赞帖子 {}", id);
            throw new AuthenticationException("请登录后操作");
        }

        // 在实际项目中，应该先检查用户是否已经点赞过该帖子
        // 由于没有点赞表，我们简化处理，直接减少点赞计数
        log.info("用户 {} 取消点赞帖子 {} (未检查是否已点赞)", userId, id);

        // 减少点赞计数，确保不小于0
        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .setSql("like_count = GREATEST(0, like_count - 1)")); // 确保计数 >= 0
    }

    @Override
    @Transactional
    public boolean batchDeletePosts(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return true;
        }

        // 验证权限：只有管理员才能批量删除帖子
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            log.warn("非管理员用户尝试批量删除帖子");
            throw new AuthenticationException("只有管理员才能执行批量删除操作");
        }

        log.info("管理员执行批量删除帖子: {}", Arrays.toString(ids));

        // 实际应用中应该考虑软删除或者在删除前检查相关依赖，如评论和附件
        // 这里简化处理，直接物理删除
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional
    public boolean setPostTop(Long id, Integer isTop) {
        // 检查权限：只有管理员可以设置帖子置顶
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AuthenticationException("无权设置帖子置顶");
        }

        // 验证置顶值是否合法 (0或1)
        if (isTop == null || (isTop != 0 && isTop != 1)) {
            throw new IllegalArgumentException("无效的置顶值：" + isTop + "。允许的值：0(不置顶)、1(置顶)");
        }

        // 检查帖子是否存在
        Post existingPost = getById(id);
        if (existingPost == null) {
            throw new ResourceNotFoundException("要设置置顶的帖子不存在: " + id);
        }

        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .set(Post::getIsTop, isTop)
                .set(Post::getLastUpdateTime, new Date()));
    }

    @Override
    @Transactional
    public boolean setPostEssence(Long id, Integer isEssence) {
        // 检查权限：只有管理员可以设置帖子精华
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AuthenticationException("无权设置帖子精华");
        }

        // 验证精华值是否合法 (0或1)
        if (isEssence == null || (isEssence != 0 && isEssence != 1)) {
            throw new IllegalArgumentException("无效的精华值：" + isEssence + "。允许的值：0(非精华)、1(精华)");
        }

        // 检查帖子是否存在
        Post existingPost = getById(id);
        if (existingPost == null) {
            throw new ResourceNotFoundException("要设置精华的帖子不存在: " + id);
        }

        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .set(Post::getIsEssence, isEssence)
                .set(Post::getLastUpdateTime, new Date()));
    }

    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        // 通常增加浏览次数不需要权限检查
        return update(Wrappers.<Post>lambdaUpdate()
                .eq(Post::getId, id)
                .setSql("view_count = view_count + 1"));
    }

    // --- 管理员管理方法实现 ---
    @Override
    public Map<String, Object> getAdminPosts(int page, int size, Integer status, String keyword) {
        Map<String, Object> params = new HashMap<>();
        if (status != null) {
            params.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            params.put("keyword", keyword);
        }
        // 确保管理员查询不会默认按活跃状态过滤（如果未指定）
        params.put("ignoreStatusFilter", true); // DAO查询的示例标志

        // 使用通用的findPageMap方法
        return findPageMap(params, page, size);
    }

    @Override
    public Map<String, Object> findPageMap(Map<String, Object> params, int page, int size) {
        log.debug("开始执行findPageMap，参数: page={}, size={}, params={}", page, size, params);

        try {
            Page<Map<String, Object>> pageRequest = new Page<>(page, size);
            IPage<Map<String, Object>> resultPage = postDao.findPageMap(pageRequest, params);

            if (resultPage == null) {
                log.error("postDao.findPageMap返回null");
                return createEmptyResult();
            }

            List<Map<String, Object>> records = resultPage.getRecords();
            if (records == null) {
                log.error("获取到的records列表为null");
                records = new ArrayList<>();
            }

            // 将每个记录转换为前端需要的格式
            for (Map<String, Object> record : records) {
                try {
                    // 添加额外的处理以确保数据格式正确
                    if (record.get("createTime") instanceof Date) {
                        // 已经是Date类型，不需要额外处理
                    } else if (record.get("createTime") instanceof String) {
                        // 如果是字符串，尝试转换为Date
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            record.put("createTime", format.parse((String) record.get("createTime")));
                        } catch (Exception e) {
                            log.warn("日期格式转换失败: {}", e.getMessage());
                        }
                    }

                    // 同样处理updateTime
                    if (record.get("updateTime") instanceof Date) {
                        // 已经是Date类型
                    } else if (record.get("updateTime") instanceof String) {
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            record.put("updateTime", format.parse((String) record.get("updateTime")));
                        } catch (Exception e) {
                            log.warn("日期格式转换失败: {}", e.getMessage());
                        }
                    }

                    // 处理作者信息
                    if (record.get("userId") != null) {
                        Long userId = 0L;
                        Object userIdObj = record.get("userId");
                        if (userIdObj instanceof Long) {
                            userId = (Long) userIdObj;
                        } else if (userIdObj instanceof Integer) {
                            userId = ((Integer) userIdObj).longValue();
                        } else if (userIdObj instanceof String) {
                            try {
                                userId = Long.parseLong((String) userIdObj);
                            } catch (NumberFormatException e) {
                                log.warn("用户ID转换失败: {}", e.getMessage());
                            }
                        }

                        if (userId > 0) {
                            try {
                                User user = userDao.selectById(userId);
                                if (user != null) {
                                    Map<String, Object> author = new HashMap<>();
                                    author.put("id", user.getId());
                                    author.put("username", user.getUsername());
                                    author.put("realName", user.getRealName());
                                    record.put("author", author);
                                }
                            } catch (Exception e) {
                                log.error("加载用户信息时出错: {}", e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("处理帖子记录时出错: {}", e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("rows", records);
            result.put("total", resultPage.getTotal());
            result.put("size", resultPage.getSize());
            result.put("current", resultPage.getCurrent());

            log.debug("findPageMap执行成功，共返回{}条记录", records.size());
            return result;
        } catch (Exception e) {
            log.error("findPageMap执行出错: {}", e.getMessage(), e);
            return createEmptyResult();
        }
    }

    /**
     * 创建空的结果集
     */
    private Map<String, Object> createEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("rows", new ArrayList<>());
        result.put("total", 0);
        result.put("size", 0);
        result.put("current", 1);
        return result;
    }

    @Override
    public Map<String, Object> getCommentsForAdmin(Map<String, Object> params, int page, int size) {
        // 目标：获取所有帖子的所有评论，分页，带过滤条件，用户信息，帖子信息。
        // 使用当前的JSON存储方式，这是一个复杂问题。
        // 选项1：遍历帖子（对分页/过滤效率低）
        // 选项2：数据库级别的JSON查询（复杂，数据库特定）
        // 选项3：将评论重构为单独的表（推荐）

        log.warn("getCommentsForAdmin: 基于当前JSON存储的实现效率低下或不完整。");

        // --- 简化示例（可能低效和不完整）---
        // 此示例获取所有帖子，提取所有评论，然后在内存中进行过滤/分页。
        // **不适合有大量帖子/评论的生产环境。**

        List<Post> allPosts = list(); // 获取所有帖子（可能很大）
        List<CommentPojo> allComments = new ArrayList<>();

        for (Post post : allPosts) {
            if (StringUtils.hasText(post.getCommentsJson()) && !"[]".equals(post.getCommentsJson().trim())) {
            try {
                List<CommentPojo> postComments = objectMapper.readValue(
                        post.getCommentsJson(),
                        new TypeReference<List<CommentPojo>>() {}
                );
                // 为每条评论添加帖子信息
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

        // 填充作者名称
        Set<Long> userIds = allComments.stream().map(CommentPojo::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            userMap = userService.getUsersByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getRealName, (name1, name2) -> name1)); // 使用真实姓名
        }
        final Map<Long, String> finalUserMap = userMap;
        allComments.forEach(c -> c.setAuthorName(finalUserMap.getOrDefault(c.getUserId(), "未知用户")));

        // 应用过滤器（示例：状态和关键词过滤内容/作者）
        String keywordFilter = (String) params.getOrDefault("keyword", "");
        Integer statusFilter = (Integer) params.get("status");

        List<CommentPojo> filteredComments = allComments.stream()
                .filter(c -> statusFilter == null || statusFilter.equals(c.getStatus()))
                .filter(c -> !StringUtils.hasText(keywordFilter) ||
                        (c.getContent() != null && c.getContent().contains(keywordFilter)) ||
                        (c.getAuthorName() != null && c.getAuthorName().contains(keywordFilter)))
                .collect(Collectors.toList());

        // 在内存中应用分页
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
        // 需要获取帖子，解析JSON，更新特定评论，并保存回去。
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
        // 由于标签功能已移除，返回空列表
        return new ArrayList<>();
        // return postDao.getPopularTagCounts(limit); // 原始代码
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
                // 处理作者未找到的情况
                post.setAuthorName("未知用户");
            }
        }
    }

    /**
     * 从comments_json字段加载评论数据到Post对象
     *
     * @param post 帖子对象
     */
    private void loadComments(Post post) {
        if (post == null || !StringUtils.hasText(post.getCommentsJson())) {
            if (post != null) {
                post.setComments(new ArrayList<>());
            }
            return;
        }

        try {
            // 使用ObjectMapper解析评论JSON数据
            List<Map<String, Object>> comments = objectMapper.readValue(
                    post.getCommentsJson(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            // 为每条评论加载作者信息
            for (Map<String, Object> comment : comments) {
                // 获取评论用户ID
                Object userIdObj = comment.get("userId");
                Long userId = null;

                if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                } else if (userIdObj instanceof Integer) {
                    userId = ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    try {
                        userId = Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException e) {
                        // 无法转换用户ID，使用默认值null
                    }
                }

                // 如果存在用户ID，尝试加载用户信息
                if (userId != null) {
                    User user = userDao.selectById(userId);
                    if (user != null) {
                        Map<String, Object> author = new HashMap<>();
                        author.put("id", user.getId());
                        author.put("username", user.getUsername());
                        author.put("realName", user.getRealName());
                        comment.put("author", author);
                    }
                }
            }

            post.setComments(comments);
        } catch (IOException e) {
            log.error("解析帖子评论JSON数据失败: {}", e.getMessage(), e);
            post.setComments(new ArrayList<>());
        }
    }

    /**
     * 硬删除帖子 - 仅限管理员使用
     *
     * @param id 帖子ID
     * @return 是否成功删除
     */
    @Transactional
    public boolean removePostPermanently(Long id) {
        // 检查是否为管理员
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AuthenticationException("只有管理员才能永久删除帖子");
        }

        return removeById(id);
    }

    // 定义一个简单的POJO类用于解析JSON中的评论
    @Data // Lombok注解用于生成getter/setter等
    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中的额外字段
    private static class CommentPojo {
        private String id;
        private Long userId;
        private String content;
        // 假设JSON中的creationTime是字符串，如需要可进行解析，或使用Date/LocalDateTime
        // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 如果使用Date/LocalDateTime请添加此注解
        private Object creationTime; // 保持为Object类型或使用特定类型
        private Integer likeCount;
        private Integer status;
        // 如果JSON中存在其他字段，请添加，例如parentId, replyToId

        // 要填充的临时字段
        private String authorName;
        private Long postId;
        private String postTitle;
    }
}