package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.PostDao;
import com.campus.entity.Post;
import com.campus.entity.User;
import com.campus.exception.AuthenticationException;
import com.campus.exception.CustomException;
import com.campus.service.PostService;
import com.campus.service.UserService;
import com.campus.utils.PageUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 论坛帖子服务实现类
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostDao, Post> implements PostService {

    @Autowired
    private PostDao postDao;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

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
    public List<Post> getAllPosts() {
        List<Post> posts = postDao.findAll();
        posts.forEach(this::loadAuthorInfo);
        return posts;
    }

    @Override
    public List<Post> getPostsByAuthorId(Long authorId) {
        List<Post> posts = postDao.findByAuthorId(authorId);
        posts.forEach(this::loadAuthorInfo);
        return posts;
    }

    @Override
    public List<Post> getPostsByCategory(String category) {
        List<Post> posts = postDao.findByCategory(category);
        posts.forEach(this::loadAuthorInfo);
        return posts;
    }

    @Override
    public List<Post> getPostsByStatus(Integer status) {
        String statusStr = String.valueOf(status);
        List<Post> posts = postDao.findByStatus(statusStr);
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
    public boolean addPost(Post post) {
        Long currentUserId = getCurrentUserIdFromRequest();
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
            return false;
        }
        Long currentUserId = getCurrentUserIdFromRequest();
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
            return true;
        }
        Long currentUserId = getCurrentUserIdFromRequest();
        if (!existingPost.getUserId().equals(currentUserId)) {
            throw new AuthenticationException("无权删除此帖子");
        }
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean batchDeletePosts(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return true;
        }
        return postDao.batchDelete(ids) > 0;
    }

    @Override
    @Transactional
    public boolean updatePostStatus(Long id, Integer status) {
        String statusStr = String.valueOf(status);
        return postDao.updateStatus(id, statusStr) > 0;
    }

    @Override
    @Transactional
    public boolean setPostTop(Long id, Integer isTop) {
        Post post = new Post();
        post.setId(id);
        post.setIsTop(isTop);
        post.setLastUpdateTime(new Date());

        return postDao.update(post) > 0;
    }

    @Override
    @Transactional
    public boolean setPostEssence(Long id, Integer isEssence) {
        Post post = new Post();
        post.setId(id);
        post.setIsEssence(isEssence);
        post.setLastUpdateTime(new Date());

        return postDao.update(post) > 0;
    }

    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        return postDao.incrementViews(id) > 0;
    }

    @Override
    @Transactional
    public boolean incrementCommentCount(Long id) {
        Post post = postDao.findById(id);
        if (post != null) {
            return postDao.updateCommentCount(id, post.getCommentCount() + 1) > 0;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean incrementLikeCount(Long id) {
        Post post = postDao.findById(id);
        if (post != null) {
            return postDao.updateLikeCount(id, post.getLikeCount() + 1) > 0;
        }
        return false;
    }
    
    @Override
    public Map<String, Object> getAllPosts(int page, int size) {
        Map<String, Object> result = new HashMap<>();
        int offset = (page - 1) * size;
        int total = postDao.countAll();
        List<Post> posts = postDao.findByPage(offset, size);
        posts.forEach(this::loadAuthorInfo);
        result.put("total", total);
        result.put("rows", posts);
        return result;
    }
    
    @Override
    public Map<String, Object> getPostsByUserId(Long userId, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        int offset = (page - 1) * size;
        int total = postDao.countByAuthorId(userId);
        List<Post> posts = postDao.findByAuthorIdAndPage(userId, offset, size);
        posts.forEach(this::loadAuthorInfo);
        result.put("total", total);
        result.put("rows", posts);
        return result;
    }
    
    @Override
    public Map<String, Object> getMyPosts(int page, int size) {
        Long currentUserId = getCurrentUserIdFromRequest();
        if (currentUserId == null) {
            throw new AuthenticationException("无法获取当前用户信息，请重新登录后尝试");
        }
        return getPostsByUserId(currentUserId, page, size);
    }

    private Long getCurrentUserIdFromRequest() {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) return null;
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        } else if (userIdAttr instanceof Number) {
            return ((Number) userIdAttr).longValue();
        } else if (userIdAttr instanceof String) {
            try {
                return Long.parseLong((String) userIdAttr);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse userId from request attribute: " + userIdAttr);
                return null;
            }
        }
        log.warn("Unexpected type for userId request attribute: " + userIdAttr.getClass().getName());
        return null;
    }

    private String getCurrentUsernameFromRequest() {
        Object usernameAttr = request.getAttribute("username");
        return (usernameAttr instanceof String) ? (String) usernameAttr : null;
    }
    
    @Override
    public Map<String, Object> searchPosts(String keyword, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        int offset = (page - 1) * size;
        int total = postDao.countByKeyword(keyword);
        List<Post> posts = postDao.searchPosts(keyword);
        posts.forEach(this::loadAuthorInfo);
        result.put("total", total);
        result.put("rows", posts);
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getCommentsByPostId(Long postId) {
        Post post = getById(postId);
        if (post != null && StringUtils.hasText(post.getCommentsJson())) {
            return parseCommentsJson(post.getCommentsJson());
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> parseCommentsJson(String json) {
        log.warn("JsonUtils not implemented - cannot parse comments JSON");
        throw new CustomException("评论功能暂未实现 (JsonUtils missing)");
    }

    private String serializeCommentsJson(List<Map<String, Object>> comments) {
        log.warn("JsonUtils not implemented - cannot serialize comments JSON");
        throw new CustomException("评论功能暂未实现 (JsonUtils missing)");
    }

    @Override
    @Transactional
    public boolean addComment(Long postId, String content) {
        Long currentUserId = getCurrentUserIdFromRequest();
        String currentUsername = getCurrentUsernameFromRequest();
        if (currentUserId == null || currentUsername == null) {
            throw new AuthenticationException("无法获取当前用户信息，请重新登录后评论");
        }

        int affectedRows = postDao.insertComment(postId, currentUserId, currentUsername, content);
        if (affectedRows > 0) {
            incrementCommentCount(postId);
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional
    public boolean deleteComment(Long postId, String commentIdStr) {
        Long commentId;
        try {
            commentId = Long.parseLong(commentIdStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid comment ID format: " + commentIdStr);
            return false;
        }
        int affectedRows = postDao.deleteComment(commentId);
        if (affectedRows > 0) {
            Post post = getById(postId);
            if (post != null && post.getCommentCount() > 0) {
                postDao.updateCommentCount(postId, post.getCommentCount() - 1);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean incrementViews(Long id) {
        return incrementViewCount(id);
    }
    
    @Override
    @Transactional
    public boolean likePost(Long id) {
        Long currentUserId = getCurrentUserIdFromRequest();
        if (currentUserId == null) {
            throw new AuthenticationException("请登录后点赞");
        }
        try {
            int inserted = postDao.insertLike(id, currentUserId);
            if (inserted > 0) {
                Post post = getById(id);
                if (post != null) {
                    postDao.updateLikeCount(id, post.getLikeCount() + 1);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.debug("Failed to insert like (may already exist): " + e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean unlikePost(Long id) {
        Long currentUserId = getCurrentUserIdFromRequest();
        if (currentUserId == null) {
            throw new AuthenticationException("请登录");
        }
        int deleted = postDao.deleteLike(id, currentUserId);
        if (deleted > 0) {
            Post post = getById(id);
            if (post != null && post.getLikeCount() > 0) {
                postDao.updateLikeCount(id, post.getLikeCount() - 1);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public Map<String, Object> getPostStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", postDao.countAll());
        stats.put("activePosts", postDao.countByStatus("1"));
        stats.put("topPosts", count(new LambdaQueryWrapper<Post>().eq(Post::getIsTop, 1)));
        stats.put("essencePosts", count(new LambdaQueryWrapper<Post>().eq(Post::getIsEssence, 1)));
        return stats;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PageUtils.PageResult<Post> findPage(Map<String, Object> params, int page, int size) {
        Page<Post> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();

        String title = (String) params.get("title");
        String category = (String) params.get("category");
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long userId = params.get("userId") != null ? Long.parseLong(params.get("userId").toString()) : null;

        queryWrapper.like(StringUtils.hasText(title), Post::getTitle, title);
        queryWrapper.eq(StringUtils.hasText(category), Post::getCategory, category);
        queryWrapper.eq(status != null, Post::getStatus, status);
        queryWrapper.eq(userId != null, Post::getUserId, userId);

        queryWrapper.orderByDesc(Post::getCreationTime);

        IPage<Post> resultPage = this.page(pageRequest, queryWrapper);
        resultPage.getRecords().forEach(this::loadAuthorInfo);

        return new PageUtils.PageResult<Post>(
                resultPage.getTotal(),
                resultPage.getCurrent(),
                resultPage.getSize(),
                resultPage.getRecords()
        );
    }

    @Override
    public Map<String, Object> getPostsByCategory(String category, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        int offset = (page - 1) * size;
        int total = postDao.countByCategory(category);
        List<Post> posts = postDao.findByCategoryAndPage(category, offset, size);
        posts.forEach(this::loadAuthorInfo);
        result.put("total", total);
        result.put("rows", posts);
        return result;
    }

    @Override
    public IPage<Post> getPostPageByCategory(Page<Post> page, String category) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getCategory, category);
        queryWrapper.orderByDesc(Post::getCreationTime);
        IPage<Post> resultPage = this.page(page, queryWrapper);
        resultPage.getRecords().forEach(this::loadAuthorInfo);
        return resultPage;
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
    @Transactional
    public boolean createPost(Post post) {
        Long currentUserId = getCurrentUserIdFromRequest();
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

        boolean saved = save(post);
        return saved;
    }

    @Override
    public IPage<Post> getPostsByTag(String tag, Page<Post> page) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Post::getTags, '"' + tag + '"');
        queryWrapper.orderByDesc(Post::getCreationTime);
        IPage<Post> resultPage = this.page(page, queryWrapper);
        resultPage.getRecords().forEach(this::loadAuthorInfo);
        return resultPage;
    }

    @Override
    public List<String> getHotTags(int limit) {
        List<Post> allPosts = list(new LambdaQueryWrapper<Post>().select(Post::getTags));
        Map<String, Integer> tagCounts = new HashMap<>();
        for (Post post : allPosts) {
            if (post.getTags() != null) {
                for (String tag : post.getTags()) {
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                }
            }
        }
        return tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void loadAuthorInfo(Post post) {
        if (post != null && post.getUserId() != null) {
            User author = userService.getUserById(post.getUserId());
            if (author != null) {
                post.setAuthorName(author.getRealName() != null ? author.getRealName() : author.getUsername());
                post.setAuthorAvatar(author.getAvatarUrl());
            } else {
                post.setAuthorName("未知用户");
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> findPageMap(Map<String, Object> params, int page, int size) {
        PageUtils.PageResult<Post> pageResult = findPage(params, page, size);
        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("total", pageResult.getTotal());
        mapResult.put("rows", pageResult.getRecords());
        return mapResult;
    }
}