package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.CourseSelection;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 选课信息服务接口
 * </p>
 */
public interface CourseSelectionService extends IService<CourseSelection> {

    /**
     * 根据学生ID查询选课记录 (填充关联信息)
     *
     * @param userId 学生ID
     * @return 选课记录列表 (包含 courseName, studentName)
     */
    List<CourseSelection> getByStudentId(Long userId);

    /**
     * 根据课程ID查询选课记录 (填充关联信息)
     *
     * @param courseId 课程ID
     * @return 选课记录列表 (包含 courseName, studentName)
     */
    List<CourseSelection> getByCourseId(Long courseId);

    /**
     * 学生选课
     *
     * @param courseSelection 选课信息 (至少包含 userId, courseId, termInfo)
     * @return 是否选课成功
     */
    boolean selectCourse(CourseSelection courseSelection);

    /**
     * 学生退选课程
     *
     * @param userId   学生ID
     * @param courseId 课程ID
     * @param termInfo 学期信息
     * @return 是否退选成功
     */
    boolean dropCourse(Long userId, Long courseId, String termInfo);

    /**
     * 分页查询选课记录 (填充关联信息)
     *
     * @param current 当前页
     * @param size    每页大小
     * @param params  查询参数 (Map, key 为字段名)
     * @return 分页结果 (IPage<CourseSelection>)
     */
    IPage<CourseSelection> pageQuery(Long current, Long size, Map<String, Object> params);

    /**
     * 检查学生是否已选该课程
     *
     * @param userId   学生ID
     * @param courseId 课程ID
     * @param termInfo 学期信息
     * @return 是否已选
     */
    boolean isCourseTaken(Long userId, Long courseId, String termInfo);

    /**
     * 根据学生ID和学期信息查询选课记录
     *
     * @param userId   学生ID
     * @param termInfo 学期信息
     * @return 选课记录列表
     */
    List<CourseSelection> getByStudentIdAndTerm(Long userId, String termInfo);

    /**
     * 根据课程ID和学期信息查询选课记录
     *
     * @param courseId 课程ID
     * @param termInfo 学期信息
     * @return 选课记录列表
     */
    List<CourseSelection> getByCourseIdAndTerm(Long courseId, String termInfo);

    /**
     * 获取某门课的选课人数
     *
     * @param courseId 课程ID
     * @param termInfo 学期信息
     * @return 选课人数
     */
    int getCourseSelectionCount(Long courseId, String termInfo);
}