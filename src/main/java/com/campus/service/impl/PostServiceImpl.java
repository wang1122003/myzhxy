package com.campus.service.impl;

import com.campus.dao.PostDao;
import com.campus.entity.Post;
import com.campus.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论坛帖子服务实现类
 */
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostDao postDao;

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
        // 设置初始值
        Date now = new Date();
        post.setCreateTime(now);
        post.setUpdateTime(now);
        
        // 初始化计数
        post.setViewCount(0);
        post.setCommentCount(0);
        post.setLikeCount(0);
        
        // 如果没有设置状态，默认为已发布
        if (post.getStatus() == null) {
            post.setStatus(1);
        }
        
        // 如果没有设置置顶和精华，默认为否
        if (post.getIsTop() == null) {
            post.setIsTop(0);
        }
        if (post.getIsEssence() == null) {
            post.setIsEssence(0);
        }
        
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
        // 获取当前用户ID，这里假设有一个获取当前用户ID的方法
        Long currentUserId = getCurrentUserId();
        return getPostsByUserId(currentUserId, page, size);
    }
    
    // 获取当前用户ID的辅助方法
    private Long getCurrentUserId() {
        // 这里应该从安全上下文中获取当前用户ID
        // 实际实现可能依赖于Spring Security或自定义的用户身份管理
        // 这里简化实现，返回一个默认值
        return 1L; // 实际应用中应替换为真实逻辑
    }
    
    @Override
    public Map<String, Object> getPostsByCategory(String category, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取总数和分页数据
        int total = postDao.countByCategory(category);
        List<Post> posts = postDao.findByCategoryAndPage(category, offset, size);
        
        result.put("total", total);
        result.put("rows", posts);
        return result;
    }
    
    @Override
    public Map<String, Object> searchPosts(String keyword, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 获取总数和分页数据
        int total = postDao.countByKeyword(keyword);
        List<Post> posts = postDao.findByKeywordAndPage(keyword, offset, size);
        
        result.put("total", total);
        result.put("rows", posts);
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getCommentsByPostId(Long postId) {
        return postDao.findCommentsByPostId(postId);
    }
    
    @Override
    @Transactional
    public boolean addComment(Long postId, String content) {
        // 获取当前用户ID
        Long currentUserId = getCurrentUserId();
        // 添加评论
        boolean result = postDao.insertComment(postId, currentUserId, content) > 0;
        // 如果添加成功，增加帖子评论计数
        if (result) {
            incrementCommentCount(postId);
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
        // 获取当前用户ID
        Long currentUserId = getCurrentUserId();
        // 添加点赞记录
        boolean result = postDao.insertLike(id, currentUserId) > 0;
        // 如果添加成功，增加帖子点赞计数
        if (result) {
            incrementLikeCount(id);
        }
        return result;
    }
    
    @Override
    @Transactional
    public boolean unlikePost(Long id) {
        // 获取当前用户ID
        Long currentUserId = getCurrentUserId();
        // 删除点赞记录
        return postDao.deleteLike(id, currentUserId) > 0;
    }
    
    @Override
    public List<Map<String, Object>> getPostTags(Long postId) {
        return postDao.findTagsByPostId(postId);
    }
    
    @Override
    @Transactional
    public boolean addTagsToPost(Long postId, Long[] tagIds) {
        if (tagIds == null || tagIds.length == 0) {
            return false;
        }
        return postDao.insertPostTags(postId, tagIds) > 0;
    }
    
    @Override
    @Transactional
    public boolean removeTagFromPost(Long postId, Long tagId) {
        return postDao.deletePostTag(postId, tagId) > 0;
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
}