package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.PostDao;
import com.campus.dto.PageResult;
import com.campus.entity.Post;
import com.campus.exception.AuthenticationException;
import com.campus.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private HttpServletRequest request;

    @Override
    public Post getPostById(Long id) {
        return postDao.findById(id);
    }

    @Override
    public List<Post> getAllPosts() {
        return postDao.findAll();
    }

    @Override
    public List<Post> getPostsByAuthorId(Long authorId) {
        return postDao.findByAuthorId(authorId);
    }

    @Override
    public List<Post> getPostsByForumId(Long forumId) {
        return postDao.findByForumId(forumId);
    }

    @Override
    public List<Post> getPostsByStatus(Integer status) {
        return postDao.findByStatus(status);
    }

    @Override
    public List<Post> getTopPosts() {
        return postDao.findTop();
    }

    @Override
    public List<Post> getEssencePosts() {
        return postDao.findEssence();
    }

    @Override
    public List<Post> getHotPosts(int limit) {
        return postDao.findHot(limit);
    }

    @Override
    @Transactional
    public boolean addPost(Post post) {
        // 1. Set Author Info
        Long currentUserId = getCurrentUserIdFromRequest();
        String currentUsername = getCurrentUsernameFromRequest();
        if (currentUserId == null || currentUsername == null) {
            throw new AuthenticationException("无法获取当前用户信息，请重新登录后尝试");
        }
        post.setAuthorId(currentUserId);
        // post.setAuthorName(currentUsername); // authorName 和 forumName 应由查询填充，插入时不设置

        // 2. Handle Forum Info (forumId from frontend)
        if (post.getForumId() == null) {
            // Handle error: forumId is required but missing
            log.error("Forum ID is required to create a post.");
            return false; // Cannot create post without forumId
        }
        // forumName will be populated by JOINs when querying, not set on insert
        post.setForumName(null);
        // Removed TagService check and forumName fetching logic here

        // 3. Set initial values & timestamps
        Date now = new Date();
        post.setCreateTime(now);
        post.setUpdateTime(now);
        post.setViewCount(post.getViewCount() == null ? 0 : post.getViewCount());
        post.setCommentCount(post.getCommentCount() == null ? 0 : post.getCommentCount());
        post.setLikeCount(post.getLikeCount() == null ? 0 : post.getLikeCount());
        post.setStatus(post.getStatus() == null ? 1 : post.getStatus()); // Default to published
        post.setIsTop(post.getIsTop() == null ? 0 : post.getIsTop());
        post.setIsEssence(post.getIsEssence() == null ? 0 : post.getIsEssence());

        // 4. Insert into DB
        return postDao.insert(post) > 0;
    }

    @Override
    @Transactional
    public boolean updatePost(Post post) {
        // 设置更新时间
        post.setUpdateTime(new Date());
        
        return postDao.update(post) > 0;
    }

    @Override
    @Transactional
    public boolean deletePost(Long id) {
        return postDao.delete(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeletePosts(Long[] ids) {
        return postDao.batchDelete(ids) > 0;
    }

    @Override
    @Transactional
    public boolean updatePostStatus(Long id, Integer status) {
        return postDao.updateStatus(id, status) > 0;
    }

    @Override
    @Transactional
    public boolean setPostTop(Long id, Integer isTop) {
        Post post = new Post();
        post.setId(id);
        post.setIsTop(isTop);
        post.setUpdateTime(new Date());
        
        return postDao.update(post) > 0;
    }

    @Override
    @Transactional
    public boolean setPostEssence(Long id, Integer isEssence) {
        Post post = new Post();
        post.setId(id);
        post.setIsEssence(isEssence);
        post.setUpdateTime(new Date());
        
        return postDao.update(post) > 0;
    }

    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        Post post = postDao.findById(id);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            post.setUpdateTime(new Date());
            return postDao.update(post) > 0;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean incrementCommentCount(Long id) {
        Post post = postDao.findById(id);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            post.setUpdateTime(new Date());
            return postDao.update(post) > 0;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean incrementLikeCount(Long id) {
        Post post = postDao.findById(id);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            post.setUpdateTime(new Date());
            return postDao.update(post) > 0;
        }
        return false;
    }
    
    @Override
    public Map<String, Object> getAllPosts(int page, int size) {
        Map<String, Object> result = new HashMap<>();
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取总数和分页数据
        int total = postDao.countAll();
        List<Post> posts = postDao.findByPage(offset, size);
        
        result.put("total", total);
        result.put("rows", posts);
        return result;
    }
    
    @Override
    public Map<String, Object> getPostsByUserId(Long userId, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取总数和分页数据
        int total = postDao.countByAuthorId(userId);
        List<Post> posts = postDao.findByAuthorIdAndPage(userId, offset, size);
        
        result.put("total", total);
        result.put("rows", posts);
        return result;
    }
    
    @Override
    public Map<String, Object> getMyPosts(int page, int size) {
        Long currentUserId = getCurrentUserIdFromRequest();
        if (currentUserId == null) {
            throw new AuthenticationException("无法获取当前用户信息，请重新登录后尝试");
            // Or return empty map: return Map.of("total", 0, "rows", List.of());
        }
        return getPostsByUserId(currentUserId, page, size);
    }

    // Helper method to get current user ID from request attribute
    private Long getCurrentUserIdFromRequest() {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        } else if (userIdAttr instanceof Number) {
            return ((Number) userIdAttr).longValue();
        } else if (userIdAttr instanceof String) {
            try {
                return Long.parseLong((String) userIdAttr);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse userId from request attribute: {}", userIdAttr);
                return null;
            }
        }
        log.warn("User ID not found or invalid type in request attribute: {}", userIdAttr);
        return null;
    }

    // Helper method to get current username from request attribute
    private String getCurrentUsernameFromRequest() {
        Object usernameAttr = request.getAttribute("username");
        return (usernameAttr instanceof String) ? (String) usernameAttr : null;
    }
    
    @Override
    public Map<String, Object> searchPosts(String keyword, int page, int size) {
        // Use the generic findPage method for searching
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(keyword)) {
            params.put("keyword", keyword);
        }
        // Assuming searchPosts should only return published posts
        // params.put("status", 1); // Or remove this line if status shouldn't be fixed

        PageResult<Post> pageResult = findPage(params, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("rows", pageResult.getRecords());
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getCommentsByPostId(Long postId) {
        return postDao.findCommentsByPostId(postId);
    }
    
    @Override
    @Transactional
    public boolean addComment(Long postId, String content) {
        Long currentUserId = getCurrentUserIdFromRequest();
        String authorName = getCurrentUsernameFromRequest(); // 获取用户名

        if (currentUserId == null) {
            // 如果无法获取用户ID，也无法获取用户名，抛出异常
            throw new AuthenticationException("无法获取当前用户信息，请重新登录后尝试");
        }
        if (authorName == null) {
            // 如果能获取ID但无法获取用户名（理论上不应发生），记录警告，但可能继续（取决于业务逻辑）
            log.warn("添加评论时无法获取用户 ID: {} 的用户名", currentUserId);
            // 可以选择抛出异常，或使用默认值/占位符
            // throw new AuthenticationException("无法获取当前用户名"); 
            // authorName = "未知用户"; // 或者设置占位符
        }

        // TODO: 考虑添加 authorName 到 comment (已添加 authorName 参数调用)
        // 1. 确认评论表 (e.g., tb_comment) 已添加 author_name 字段。
        // 2. 确认 PostDao.insertComment 方法签名和 SQL 实现已添加 authorName 参数。

        // 调用包含 authorName 的 DAO 方法
        boolean result = postDao.insertComment(postId, currentUserId, authorName, content) > 0; 
        
        if (result) {
            incrementCommentCount(postId); // 评论数 +1
        }
        return result;
    }
    
    @Override
    @Transactional
    public boolean deleteComment(Long commentId) {
        return postDao.deleteComment(commentId) > 0;
    }
    
    @Override
    public boolean incrementViews(Long id) {
        return postDao.incrementViews(id) > 0;
    }
    
    @Override
    @Transactional
    public boolean likePost(Long id) {
        Long currentUserId = getCurrentUserIdFromRequest();
        if (currentUserId == null) {
            throw new AuthenticationException("无法获取当前用户信息，请重新登录后尝试");
            // return false;
        }
        boolean result = postDao.insertLike(id, currentUserId) > 0;
        if (result) {
            incrementLikeCount(id);
        }
        return result;
    }
    
    @Override
    @Transactional
    public boolean unlikePost(Long id) {
        Long currentUserId = getCurrentUserIdFromRequest();
        if (currentUserId == null) {
            throw new AuthenticationException("无法获取当前用户信息，请重新登录后尝试");
            // return false;
        }
        return postDao.deleteLike(id, currentUserId) > 0;
    }
    
    @Override
    public Map<String, Object> getPostStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", postDao.countAll());
        stats.put("published", postDao.countByStatus(1));
        stats.put("draft", postDao.countByStatus(0));
        stats.put("deleted", postDao.countByStatus(2));
        return stats;
    }

    @Override
    @SuppressWarnings("unchecked") // Suppress warning for potential raw type usage with Mybatis-Plus Page/IPage
    public PageResult<Post> findPage(Map<String, Object> params, int page, int size) {
        // 使用 Mybatis-Plus 的 Page 对象
        IPage<Post> mybatisPage = new Page<>(page, size);

        // 2. 构建查询条件
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();

        // 处理 keyword 搜索 (标题、内容)
        if (params.containsKey("keyword") && StringUtils.hasText(params.get("keyword").toString())) {
            String keyword = params.get("keyword").toString();
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
            // 如果需要搜索作者名，需要联表查询或在 Post 实体冗余作者名
            // .or().like(Post::getAuthorName, keyword)); // 假设 Post 实体有 authorName
        }

        // 处理 forumId 过滤
        if (params.containsKey("forumId") && params.get("forumId") != null) {
            try {
                Long forumId = Long.parseLong(params.get("forumId").toString());
                wrapper.eq(Post::getForumId, forumId);
            } catch (NumberFormatException e) {
                // log error or ignore invalid forumId
            }
        }

        // 处理 status 过滤 (假设前端传递的是 isTop, isEssence, isLocked)
        // 注意：Post 实体目前没有 isLocked，这里仅处理 isTop 和 isEssence
        if (params.containsKey("status")) {
            Object statusObj = params.get("status");
            if (statusObj instanceof String statusStr && StringUtils.hasText(statusStr)) {
                if (statusStr.contains("isTop")) wrapper.eq(Post::getIsTop, 1);
                if (statusStr.contains("isEssence")) wrapper.eq(Post::getIsEssence, 1);
                // if (statusStr.contains("isLocked")) wrapper.eq(Post::getStatus, SOME_LOCKED_STATUS); // 如果需要锁定状态
            } else if (statusObj instanceof Iterable) { // 处理前端传数组的情况 ['isTop', 'isEssence']
                ((Iterable<?>) statusObj).forEach(item -> {
                    if ("isTop".equals(item)) wrapper.eq(Post::getIsTop, 1);
                    if ("isEssence".equals(item)) wrapper.eq(Post::getIsEssence, 1);
                    // if ("isLocked".equals(item)) wrapper.eq(Post::getStatus, SOME_LOCKED_STATUS);
                });
            }
        }

        // 默认查询已发布的帖子 (status = 1), 除非明确指定了其他状态查询需求
        // 如果需要查询草稿或已删除，前端参数需要明确指出，并在此处调整逻辑
        if (!params.containsKey("queryAllStatus")) { // 添加一个参数来决定是否查询所有状态
            wrapper.eq(Post::getStatus, 1); // 默认只看已发布的
        }

        // 添加排序条件，例如按置顶、精华、创建时间排序
        wrapper.orderByDesc(Post::getIsTop, Post::getIsEssence, Post::getCreateTime);

        // 3. 执行查询
        IPage<Post> resultPage = this.baseMapper.selectPage(mybatisPage, wrapper);

        // 4. 封装返回结果
        return new PageResult<>(resultPage.getTotal(), resultPage.getRecords(), resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public Map<String, Object> getPostsByForumId(Long forumId, int page, int size) {
        // 复用 findPage 方法
        Map<String, Object> params = new HashMap<>();
        params.put("forumId", forumId);
        PageResult<Post> pageResult = findPage(params, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("rows", pageResult.getRecords());
        return result;
    }

    @Override
    public IPage<Post> getPostPage(Page<Post> page, Long forumId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);

        if (forumId != null && forumId > 0) {
            wrapper.eq(Post::getForumId, forumId);
        }

        // 先按置顶排序，再按创建时间倒序
        wrapper.orderByDesc(Post::getIsTop);
        wrapper.orderByDesc(Post::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    public Post getPostDetail(Long id) {
        Post post = this.getById(id);

        if (post != null) {
            // 增加浏览量
            this.incrementViewCount(id);

            // 在实际项目中，这里应该通过用户服务获取作者信息并填充
            // 可以使用MapStruct或BeanUtils等工具进行对象映射
        }

        return post;
    }

    @Override
    @Transactional
    public boolean createPost(Post post) {
        // 设置默认值
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(1);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());

        // 设置作者ID
        Long currentUserId = getCurrentUserIdFromRequest();
        if (currentUserId == null) {
            throw new AuthenticationException("无法获取当前用户信息，请重新登录后尝试");
        }
        post.setAuthorId(currentUserId);

        return this.save(post);
    }

    @Override
    public IPage<Post> getPostsByTag(String tag, Page<Post> page) {
        // 使用JSON_CONTAINS函数查询包含特定标签的帖子
        // 注意：根据数据库不同，可能需要调整SQL语法
        String sql = "JSON_CONTAINS(tags, JSON_ARRAY(?))";

        return this.page(page, new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .apply(sql, tag)
                .orderByDesc(Post::getCreateTime));
    }

    @Override
    public List<String> getHotTags(int limit) {
        // 这里需要根据数据库类型编写适当的SQL
        // 以MySQL为例，使用JSON_TABLE函数展开JSON数组
        String sql = "SELECT tag, COUNT(*) as count FROM " +
                "(SELECT JSON_UNQUOTE(JSON_EXTRACT(p.tags, '$[*]')) as tag FROM post p WHERE p.status = 1) t " +
                "GROUP BY tag ORDER BY count DESC LIMIT " + limit;

        // 在实际实现中，你可能需要使用原生SQL查询
        // 这里用简化方式模拟实现
        return this.baseMapper.selectMaps(new QueryWrapper<Post>()
                        .select("DISTINCT JSON_UNQUOTE(JSON_EXTRACT(tags, '$[*]')) as tag")
                        .eq("status", 1)
                        .last("LIMIT " + limit))
                .stream()
                .map(map -> map.get("tag").toString())
                .collect(Collectors.toList());
    }
}