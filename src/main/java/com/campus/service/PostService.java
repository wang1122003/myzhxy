package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.dto.PageResult;
import com.campus.entity.Post;

import java.util.List;
import java.util.Map;

/**
 * 论坛帖子服务接口
 */
public interface PostService extends IService<Post> {
    
    /**
     * 根据ID查询帖子
     * @param id 帖子ID
     * @return 帖子对象
     */
    Post getPostById(Long id);
    
    /**
     * 获取所有帖子
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页数据
     */
    Map<String, Object> getAllPosts(int page, int size);
    
    /**
     * 根据用户ID查询帖子
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页数据
     */
    Map<String, Object> getPostsByUserId(Long userId, int page, int size);
    
    /**
     * 获取当前登录用户的帖子
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页数据
     */
    Map<String, Object> getMyPosts(int page, int size);
    
    /**
     * 搜索帖子
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页数据
     */
    Map<String, Object> searchPosts(String keyword, int page, int size);
    
    /**
     * 获取帖子的评论
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Map<String, Object>> getCommentsByPostId(Long postId);
    
    /**
     * 添加评论
     * @param postId 帖子ID
     * @param content 评论内容
     * @return 是否成功
     */
    boolean addComment(Long postId, String content);
    
    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean deleteComment(Long commentId);
    
    /**
     * 获取热门帖子
     * @param limit 数量限制
     * @return 热门帖子列表
     */
    List<Post> getHotPosts(int limit);
    
    /**
     * 增加帖子浏览量
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean incrementViews(Long id);
    
    /**
     * 点赞帖子
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean likePost(Long id);
    
    /**
     * 取消点赞
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean unlikePost(Long id);
    
    /**
     * 获取帖子统计信息
     * @return 统计信息
     */
    Map<String, Object> getPostStats();
    
    /**
     * 查询所有帖子
     * @return 帖子列表
     */
    List<Post> getAllPosts();
    
    /**
     * 根据作者ID查询帖子
     * @param authorId 作者ID
     * @return 帖子列表
     */
    List<Post> getPostsByAuthorId(Long authorId);
    
    /**
     * 根据板块ID查询帖子
     * @param forumId 板块ID
     * @return 帖子列表
     */
    List<Post> getPostsByForumId(Long forumId);
    
    /**
     * 根据状态查询帖子
     * @param status 状态
     * @return 帖子列表
     */
    List<Post> getPostsByStatus(Integer status);
    
    /**
     * 查询置顶帖子
     * @return 帖子列表
     */
    List<Post> getTopPosts();
    
    /**
     * 查询精华帖子
     * @return 帖子列表
     */
    List<Post> getEssencePosts();
    
    /**
     * 批量删除帖子
     * @param ids 帖子ID数组
     * @return 是否成功
     */
    boolean batchDeletePosts(Long[] ids);
    
    /**
     * 更新帖子状态
     * @param id 帖子ID
     * @param status 状态值
     * @return 是否成功
     */
    boolean updatePostStatus(Long id, Integer status);
    
    /**
     * 设置帖子置顶状态
     * @param id 帖子ID
     * @param isTop 是否置顶
     * @return 是否成功
     */
    boolean setPostTop(Long id, Integer isTop);
    
    /**
     * 设置帖子精华状态
     * @param id 帖子ID
     * @param isEssence 是否精华
     * @return 是否成功
     */
    boolean setPostEssence(Long id, Integer isEssence);
    
    /**
     * 增加帖子浏览次数
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long id);
    
    /**
     * 增加帖子评论次数
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean incrementCommentCount(Long id);
    
    /**
     * 增加帖子点赞次数
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean incrementLikeCount(Long id);
    
    /**
     * 添加帖子
     * @param post 帖子对象
     * @return 是否成功
     */
    boolean addPost(Post post);
    
    /**
     * 更新帖子
     * @param post 帖子对象
     * @return 是否成功
     */
    boolean updatePost(Post post);
    
    /**
     * 删除帖子
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean deletePost(Long id);

    /**
     * 分页查询帖子列表
     *
     * @param params 查询参数 (包含过滤条件)
     * @param page   当前页码
     * @param size   每页数量
     * @return 分页结果
     */
    PageResult<Post> findPage(Map<String, Object> params, int page, int size);

    /**
     * 根据板块ID获取帖子
     *
     * @param forumId 板块ID
     * @param page    页码
     * @param size    每页大小
     * @return 帖子分页数据 (Map 格式，适配 Controller)
     */
    Map<String, Object> getPostsByForumId(Long forumId, int page, int size);

    /**
     * 分页查询帖子列表
     *
     * @param page    分页参数
     * @param forumId 板块ID
     * @return 帖子分页列表
     */
    IPage<Post> getPostPage(Page<Post> page, Long forumId);

    /**
     * 根据ID获取帖子详情
     *
     * @param id 帖子ID
     * @return 帖子详情
     */
    Post getPostDetail(Long id);

    /**
     * 创建帖子
     *
     * @param post 帖子信息
     * @return 是否成功
     */
    boolean createPost(Post post);

    /**
     * 根据标签查询帖子
     *
     * @param tag  标签名
     * @param page 分页参数
     * @return 帖子分页列表
     */
    IPage<Post> getPostsByTag(String tag, Page<Post> page);

    /**
     * 获取热门标签
     *
     * @param limit 限制数量
     * @return 热门标签列表
     */
    List<String> getHotTags(int limit);
}