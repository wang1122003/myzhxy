package com.campus.entity;

import lombok.Data;

/**
 * 课表数据传输对象，用于前端展示
 */
@Data
public class ScheduleDTO {
    /**
     * 课表ID
     */
    private Long id;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教师名称
     */
    private String teacherName;

    /**
     * 教室名称
     */
    private String classroomName;

    /**
     * 星期几 (1-7)
     */
    private Integer weekday;

    /**
     * 上课开始时间 (格式: HH:mm)
     */
    private String startTime;

    /**
     * 上课结束时间 (格式: HH:mm)
     */
    private String endTime;

    /**
     * 学期信息
     */
    private String termInfo;

    /**
     * 从Schedule实体转换为DTO
     *
     * @param schedule Schedule实体
     * @return ScheduleDTO对象
     */
    public static ScheduleDTO fromSchedule(Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setCourseName(schedule.getCourseName());
        dto.setTeacherName(schedule.getTeacherName());
        dto.setClassroomName(schedule.getClassroomName());
        dto.setWeekday(schedule.getDayOfWeek());

        // 转换时间格式
        if (schedule.getStartTime() != null) {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm");
            dto.setStartTime(format.format(schedule.getStartTime()));
        }

        if (schedule.getEndTime() != null) {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm");
            dto.setEndTime(format.format(schedule.getEndTime()));
        }

        dto.setTermInfo(schedule.getTermInfo());

        return dto;
    }
} 