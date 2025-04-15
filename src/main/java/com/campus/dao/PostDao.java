package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Post;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 论坛帖子数据访问接口
 */
@Repository
public interface PostDao extends BaseMapper<Post> {
    /**
     * 根据ID查询帖子
     * @param id 帖子ID
     * @return 帖子对象
     */
    Post findById(Long id);
    
    /**
     * 查询所有帖子
     * @return 帖子列表
     */
    List<Post> findAll();
    
    /**
     * 根据作者ID查询帖子
     * @param authorId 作者ID
     * @return 帖子列表
     */
    List<Post> findByAuthorId(Long authorId);
    
    /**
     * 根据论坛ID查询帖子
     * @param forumId 论坛ID
     * @return 帖子列表
     */
    List<Post> findByForumId(Long forumId);
    
    /**
     * 根据状态查询帖子
     * @param status 帖子状态
     * @return 帖子列表
     */
    List<Post> findByStatus(Integer status);
    
    /**
     * 查询置顶帖子
     * @return 帖子列表
     */
    List<Post> findTop();
    
    /**
     * 查询精华帖子
     * @return 帖子列表
     */
    List<Post> findEssence();
    
    /**
     * 查询热门帖子
     * @param limit 数量限制
     * @return 帖子列表
     */
    List<Post> findHot(Integer limit);
    
    /**
     * 根据分类查询帖子
     * @param category 分类
     * @return 帖子列表
     */
    List<Post> findByCategory(String category);
    
    /**
     * 根据标签ID查询帖子
     * @param tagId 标签ID
     * @return 帖子列表
     */
    List<Post> findByTagId(Long tagId);
    
    /**
     * 分页查询帖子
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 帖子列表
     */
    List<Post> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 获取帖子总数
     * @return 帖子数量
     */
    int getCount();
    
    /**
     * 添加帖子
     * @param post 帖子对象
     * @return 影响行数
     */
    int insert(Post post);
    
    /**
     * 更新帖子
     * @param post 帖子对象
     * @return 影响行数
     */
    int update(Post post);
    
    /**
     * 删除帖子
     * @param id 帖子ID
     * @return 影响行数
     */
    int delete(Long id);
    
    /**
     * 批量删除帖子
     * @param ids 帖子ID数组
     * @return 影响行数
     */
    int batchDelete(Long[] ids);
    
    /**
     * 更新帖子状态
     * @param id 帖子ID
     * @param status 帖子状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 更新帖子浏览数
     * @param id 帖子ID
     * @param viewCount 浏览数
     * @return 影响行数
     */
    int updateViewCount(@Param("id") Long id, @Param("viewCount") Integer viewCount);
    
    /**
     * 更新帖子点赞数
     * @param id 帖子ID
     * @param likeCount 点赞数
     * @return 影响行数
     */
    int updateLikeCount(@Param("id") Long id, @Param("likeCount") Integer likeCount);
    
    /**
     * 更新帖子评论数
     * @param id 帖子ID
     * @param commentCount 评论数
     * @return 影响行数
     */
    int updateCommentCount(@Param("id") Long id, @Param("commentCount") Integer commentCount);
    
    /**
     * 获取热门帖子
     * @param limit 数量限制
     * @return 帖子列表
     */
    List<Post> getHotPosts(@Param("limit") Integer limit);
    
    /**
     * 获取最新帖子
     * @param limit 数量限制
     * @return 帖子列表
     */
    List<Post> getRecentPosts(@Param("limit") Integer limit);
    
    /**
     * 搜索帖子
     * @param keyword 关键词
     * @return 帖子列表
     */
    List<Post> searchPosts(@Param("keyword") String keyword);
    
    /**
     * 获取用户帖子数
     * @param userId 用户ID
     * @return 帖子数量
     */
    int getUserPostCount(Long userId);
    
    /**
     * 获取分类帖子数
     * @param category 分类
     * @return 帖子数量
     */
    int getCategoryPostCount(String category);
    
    /**
     * 获取标签帖子数
     * @param tagId 标签ID
     * @return 帖子数量
     */
    int getTagPostCount(Long tagId);
    
    /**
     * 获取帖子总数
     * @return 帖子总数
     */
    int countAll();
    
    /**
     * 根据作者ID获取帖子总数
     * @param authorId 作者ID
     * @return 帖子总数
     */
    int countByAuthorId(Long authorId);
    
    /**
     * 根据分类获取帖子总数
     * @param category 分类
     * @return 帖子总数
     */
    int countByCategory(String category);
    
    /**
     * 根据关键词获取帖子总数
     * @param keyword 关键词
     * @return 帖子总数
     */
    int countByKeyword(String keyword);
    
    /**
     * 根据状态获取帖子总数
     * @param status 状态
     * @return 帖子总数
     */
    int countByStatus(Integer status);
    
    /**
     * 根据作者ID分页查询帖子
     * @param authorId 作者ID
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 帖子列表
     */
    List<Post> findByAuthorIdAndPage(@Param("authorId") Long authorId, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 根据分类分页查询帖子
     * @param category 分类
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 帖子列表
     */
    List<Post> findByCategoryAndPage(@Param("category") String category, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 根据关键词分页查询帖子
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 帖子列表
     */
    List<Post> findByKeywordAndPage(@Param("keyword") String keyword, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 根据帖子ID查询评论
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Map<String, Object>> findCommentsByPostId(Long postId);
    
    /**
     * 插入评论
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param content 评论内容
     * @return 影响行数
     */
    int insertComment(@Param("postId") Long postId, @Param("userId") Long userId, @Param("content") String content);
    
    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 影响行数
     */
    int deleteComment(Long commentId);
    
    /**
     * 增加浏览量
     * @param id 帖子ID
     * @return 影响行数
     */
    int incrementViews(Long id);
    
    /**
     * 插入点赞
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int insertLike(@Param("postId") Long postId, @Param("userId") Long userId);
    
    /**
     * 删除点赞
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteLike(@Param("postId") Long postId, @Param("userId") Long userId);
    
    /**
     * 根据帖子ID查询标签
     * @param postId 帖子ID
     * @return 标签列表
     */
    List<Map<String, Object>> findTagsByPostId(Long postId);
    
    /**
     * 插入帖子标签
     * @param postId 帖子ID
     * @param tagIds 标签ID数组
     * @return 影响行数
     */
    int insertPostTags(@Param("postId") Long postId, @Param("tagIds") Long[] tagIds);
    
    /**
     * 删除帖子标签
     * @param postId 帖子ID
     * @param tagId 标签ID
     * @return 影响行数
     */
    int deletePostTag(@Param("postId") Long postId, @Param("tagId") Long tagId);
}