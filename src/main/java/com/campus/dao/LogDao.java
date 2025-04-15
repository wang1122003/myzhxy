package com.campus.dao;

import org.apache.ibatis.annotations.Param;

import com.campus.entity.Log;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 日志数据访问接口
 */
public interface LogDao {
    
    /**
     * 获取所有日志
     * @return 日志列表
     */
    List<Log> getAllLogs();
    
    /**
     * 根据ID获取日志
     * @param id 日志ID
     * @return 日志对象
     */
    Log getLogById(Integer id);
    
    /**
     * 根据用户ID获取日志
     * @param userId 用户ID
     * @return 日志列表
     */
    List<Log> getLogsByUserId(Integer userId);
    
    /**
     * 根据模块获取日志
     * @param module 模块名称
     * @return 日志列表
     */
    List<Log> getLogsByModule(String module);
    
    /**
     * 根据操作类型获取日志
     * @param operation 操作类型
     * @return 日志列表
     */
    List<Log> getLogsByOperation(String operation);
    
    /**
     * 根据状态获取日志
     * @param status 状态（0失败，1成功）
     * @return 日志列表
     */
    List<Log> getLogsByStatus(Integer status);
    
    /**
     * 根据时间范围获取日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<Log> getLogsByTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * 搜索日志
     * @param keyword 关键词
     * @return 日志列表
     */
    List<Log> searchLogs(String keyword);
    
    /**
     * 添加日志
     * @param log 日志对象
     * @return 影响行数
     */
    int addLog(Log log);
    
    /**
     * 批量添加日志
     * @param logs 日志列表
     * @return 影响行数
     */
    int batchAddLogs(@Param("logs") List<Log> logs);
    
    /**
     * 删除日志
     * @param id 日志ID
     * @return 影响行数
     */
    int deleteLog(Integer id);
    
    /**
     * 批量删除日志
     * @param ids 日志ID列表
     * @return 影响行数
     */
    int batchDeleteLogs(@Param("ids") List<Integer> ids);
    
    /**
     * 清空日志
     * @return 影响行数
     */
    int clearLogs();
    
    /**
     * 获取日志统计信息
     * @return 统计信息Map
     */
    Map<String, Object> getLogStats();
    
    /**
     * 获取模块统计信息
     * @return 统计信息列表
     */
    List<Map<String, Object>> getModuleStats();
    
    /**
     * 获取用户操作统计信息
     * @return 统计信息列表
     */
    List<Map<String, Object>> getUserOperationStats();
    
    /**
     * 获取IP访问统计信息
     * @return 统计信息列表
     */
    List<Map<String, Object>> getIpStats();
    
    /**
     * 获取时间统计信息
     * @return 统计信息列表
     */
    List<Map<String, Object>> getTimeStats();
} 