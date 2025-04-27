package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 论坛帖子数据访问接口
 */
@Mapper
@Repository
public interface PostDao extends BaseMapper<Post> {
    /**
     * 查询所有帖子 (如果 BaseMapper 的 selectList 不能满足特定需求)
     *
     * @return 帖子列表
     */
    List<Post> findAll();

    /**
     * 根据作者ID查询帖子
     *
     * @param authorId 作者ID
     * @return 帖子列表
     */
    List<Post> findByAuthorId(Long authorId);

    /**
     * 根据状态查询帖子
     *
     * @param status 帖子状态 (保留，可能用于复杂查询)
     * @return 帖子列表
     */
    List<Post> findByStatus(String status);

    /**
     * 查询置顶帖子
     *
     * @return 帖子列表
     */
    List<Post> findTop();

    /**
     * 查询精华帖子
     *
     * @return 帖子列表
     */
    List<Post> findEssence();

    /**
     * 查询热门帖子
     *
     * @param limit 数量限制
     * @return 帖子列表
     */
    List<Post> getHotPosts(@Param("limit") Integer limit);

    /**
     * 分页查询帖子 (保留，可能用于自定义分页逻辑)
     *
     * @param offset 偏移量
     * @param limit  数量限制
     * @return 帖子列表
     */
    List<Post> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 获取最新帖子
     *
     * @param limit 数量限制
     * @return 帖子列表
     */
    List<Post> getRecentPosts(@Param("limit") Integer limit);

    /**
     * 搜索帖子
     *
     * @param keyword 关键词
     * @return 帖子列表
     */
    List<Post> searchPosts(@Param("keyword") String keyword);

    /**
     * 获取用户帖子数 (保留，特定计数)
     *
     * @param userId 用户ID
     * @return 帖子数量
     */
    int getUserPostCount(Long userId);

    /**
     * 获取帖子总数 (保留，特定计数)
     *
     * @return 帖子总数
     */
    int countAll();

    /**
     * 根据作者ID获取帖子总数
     *
     * @param authorId 作者ID
     * @return 帖子总数
     */
    int countByAuthorId(Long authorId);

    /**
     * 根据关键词获取帖子总数
     *
     * @param keyword 关键词
     * @return 帖子总数
     */
    int countByKeyword(String keyword);

    /**
     * 根据状态获取帖子总数
     *
     * @param status 状态
     * @return 帖子总数
     */
    int countByStatus(String status);

    /**
     * 根据作者ID分页查询帖子
     *
     * @param authorId 作者ID
     * @param offset   偏移量
     * @param limit    数量限制
     * @return 帖子列表
     */
    List<Post> findByAuthorIdAndPage(@Param("authorId") Long authorId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据分类查询帖子
     *
     * @param category 分类名称
     * @return 帖子列表
     */
    List<Post> findByCategory(@Param("category") String category);

    /**
     * 根据分类分页查询帖子
     *
     * @param category 分类名称
     * @param offset   偏移量
     * @param limit    数量限制
     * @return 帖子列表
     */
    List<Post> findByCategoryAndPage(@Param("category") String category, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据分类获取帖子总数
     *
     * @param category 分类名称
     * @return 帖子总数
     */
    int countByCategory(@Param("category") String category);

    /**
     * 更新帖子的浏览量
     *
     * @param id 帖子ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 分页查询帖子（支持多种条件）
     *
     * @param page   分页信息
     * @param params 查询条件Map
     * @return 帖子分页数据
     */
    IPage<Map<String, Object>> findPageMap(Page<?> page, @Param("params") Map<String, Object> params);

    /**
     * 查询热门帖子
     *
     * @param limit 数量限制
     * @return 热门帖子列表
     */
    List<Post> findHotPosts(@Param("limit") int limit);

    /**
     * 获取所有可用的论坛类型
     *
     * @return 论坛类型列表
     */
    @Select("SELECT DISTINCT forum_type FROM post WHERE status = 'PUBLISHED'")
    List<String> listAvailableForumTypes();
}