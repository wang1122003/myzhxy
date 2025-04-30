package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserDao extends BaseMapper<User> {
    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据学工号查询用户
     *
     * @param userNo 学工号
     * @return 用户对象
     */
    User findByUserNo(String userNo);

    /**
     * 根据用户类型查询用户
     *
     * @param userType 用户类型 (String, e.g., "Student", "Teacher")
     * @return 用户列表
     */
    List<User> findByUserType(String userType);

    /**
     * 根据状态查询用户
     *
     * @param status 状态
     * @return 用户列表
     */
    List<User> findByStatus(String status);

    /**
     * 搜索用户
     *
     * @param keyword 关键词
     * @return 用户列表
     */
    List<User> searchUsers(@Param("keyword") String keyword);

    /**
     * 获取状态用户数
     *
     * @param status 状态
     * @return 用户数量
     */
    int getStatusUserCount(String status);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return > 0 if exists
     */
    Integer countByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return > 0 if exists
     */
    Integer countByEmail(String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return > 0 if exists
     */
    Integer countByPhone(String phone);

    /**
     * 检查学工号是否存在
     *
     * @param userNo 学工号
     * @return > 0 if exists
     */
    Integer countByUserNo(String userNo);

    /**
     * 查询所有有效的用户ID列表
     * (可以根据需要添加过滤条件，例如只查询 status='Active' 的用户)
     *
     * @return 用户ID列表
     */
    List<Long> findAllUserIds();
}