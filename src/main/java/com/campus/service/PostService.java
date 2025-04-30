package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Post;

import java.util.List;
import java.util.Map;

/**
 * 论坛帖子服务接口
 */
public interface PostService extends IService<Post> {

    /**
     * 根据ID查询帖子 (包含作者信息)
     *
     * @param id 帖子ID
     * @return 帖子对象
     */
    Post getPostById(Long id);

    /**
     * 分页获取所有帖子 (包含作者信息)
     *
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页数据 (Map for controller compatibility)
     */
    Map<String, Object> getAllPosts(int page, int size);

    /**
     * 根据用户ID查询帖子 (分页, 包含作者信息)
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 帖子分页数据 (Map for controller compatibility)
     */
    Map<String, Object> getPostsByUserId(Long userId, int page, int size);

    /**
     * 获取当前登录用户的帖子 (分页, 包含作者信息)
     *
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页数据 (Map for controller compatibility)
     */
    Map<String, Object> getMyPosts(int page, int size);

    /**
     * 搜索帖子 (分页, 包含作者信息)
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页大小
     * @return 帖子分页数据 (Map for controller compatibility)
     */
    Map<String, Object> searchPosts(String keyword, int page, int size);

    /**
     * 获取帖子的评论列表
     * 注意：评论存储在 Post 实体的 JSON 字段中
     *
     * @param postId 帖子ID
     * @return 评论列表 (List of Maps)
     */
    List<Map<String, Object>> getCommentsByPostId(Long postId);

    /**
     * 添加评论到帖子的 JSON 字段
     *
     * @param postId  帖子ID
     * @param content 评论内容
     * @return 是否成功
     */
    boolean addComment(Long postId, String content);

    /**
     * 从帖子的 JSON 字段中删除评论
     *
     * @param postId    帖子ID
     * @param commentId 评论ID (UUID String)
     * @return 是否成功
     */
    boolean deleteComment(Long postId, String commentId);

    /**
     * 获取热门帖子 (按某种规则排序，如浏览量、点赞数, 包含作者信息)
     *
     * @param limit 数量限制
     * @return 热门帖子列表
     */
    List<Post> getHotPosts(int limit);

    /**
     * 点赞帖子 (增加点赞数)
     *
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean likePost(Long id);

    /**
     * 取消点赞 (减少点赞数)
     *
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean unlikePost(Long id);

    /**
     * 获取帖子统计信息 (总数、活跃数等)
     *
     * @return 统计信息Map
     */
    Map<String, Object> getPostStats();

    /**
     * 根据作者ID查询所有帖子 (包含作者信息)
     *
     * @param authorId 作者ID
     * @return 帖子列表
     */
    List<Post> getPostsByAuthorId(Long authorId);

    /**
     * 查询置顶帖子 (包含作者信息)
     *
     * @return 帖子列表
     */
    List<Post> getTopPosts();

    /**
     * 查询精华帖子 (包含作者信息)
     *
     * @return 帖子列表
     */
    List<Post> getEssencePosts();

    /**
     * 批量删除帖子
     *
     * @param ids 帖子ID数组
     * @return 是否成功
     */
    boolean batchDeletePosts(Long[] ids);

    /**
     * 更新帖子状态
     *
     * @param id     帖子ID
     * @param status 状态值 (通常为 0 或 1)
     * @return 是否成功
     */
    boolean updatePostStatus(Long id, Integer status);

    /**
     * 设置帖子置顶状态
     *
     * @param id    帖子ID
     * @param isTop 是否置顶 (通常为 0 或 1)
     * @return 是否成功
     */
    boolean setPostTop(Long id, Integer isTop);

    /**
     * 设置帖子精华状态
     *
     * @param id        帖子ID
     * @param isEssence 是否精华 (通常为 0 或 1)
     * @return 是否成功
     */
    boolean setPostEssence(Long id, Integer isEssence);

    /**
     * 增加帖子浏览次数
     *
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long id);

    /**
     * 添加新帖子 (需要设置作者ID, 创建时间等)
     *
     * @param post 帖子对象
     * @return 是否成功
     */
    boolean createPost(Post post);

    /**
     * 更新帖子信息 (需要权限检查)
     *
     * @param post 帖子对象
     * @return 是否成功
     */
    boolean updatePost(Post post);

    /**
     * 删除帖子 (需要权限检查)
     *
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean deletePost(Long id);

    /**
     * 分页查询帖子列表并返回为Map结构 (通用方法, 包含作者信息)
     *
     * @param params 查询参数 (包含过滤条件)
     * @param page   当前页码
     * @param size   每页数量
     * @return 分页结果的Map表示 (包含 total 和 rows)
     */
    Map<String, Object> findPageMap(Map<String, Object> params, int page, int size);

    /**
     * 获取帖子详情 (包含作者信息, 并增加浏览次数)
     *
     * @param id 帖子ID
     * @return 帖子对象
     */
    Post getPostDetail(Long id);

    // --- Admin Comment Management Methods ---

    /**
     * [Admin] 分页获取评论列表 (跨帖子)
     *
     * @param params 查询参数 (status, keyword)
     * @param page   当前页码
     * @param size   每页数量
     * @return 评论列表分页结果 (包含评论详情、作者信息、帖子信息等)
     */
    Map<String, Object> getCommentsForAdmin(Map<String, Object> params, int page, int size);

    /**
     * [Admin] 更新指定帖子内指定评论的状态
     *
     * @param postId    帖子ID
     * @param commentId 评论ID (String)
     * @param newStatus 新的状态值
     * @return 是否成功
     */
    boolean updateCommentStatusAdmin(Long postId, String commentId, Integer newStatus);

    // --- Admin Management Methods ---

    /**
     * [Admin] 分页获取所有帖子 (包括不同状态的)
     *
     * @param page    当前页码
     * @param size    每页数量
     * @param status  状态 (可选, 过滤)
     * @param keyword 关键词 (可选, 搜索标题或内容)
     * @return 帖子列表分页结果 (Map for controller compatibility)
     */
    Map<String, Object> getAdminPosts(int page, int size, Integer status, String keyword);

    /**
     * 获取热门标签
     *
     * @param limit 限制返回的标签数量
     * @return 标签列表，每个标签包含名称和数量
     */
    List<Map<String, Object>> getPopularTags(int limit);
}