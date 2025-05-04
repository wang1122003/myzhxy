package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ClassroomDao;
import com.campus.entity.Classroom;
import com.campus.entity.Schedule;
import com.campus.entity.ScheduleDTO;
import com.campus.enums.ClassroomStatus;
import com.campus.exception.CustomException;
import com.campus.service.ClassroomService;
import com.campus.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教室服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomServiceImpl extends ServiceImpl<ClassroomDao, Classroom> implements ClassroomService {

    private final ScheduleService scheduleService;
    private final ClassroomDao classroomDao;

    @Override
    public Classroom getClassroomById(Long id) {
        return getById(id);
    }

    @Override
    public Classroom getClassroomByName(String name) {
        LambdaQueryWrapper<Classroom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Classroom::getName, name);
        return getOne(queryWrapper);
    }

    @Override
    public List<Classroom> getAllClassrooms() {
        return list();
    }

    @Override
    public IPage<Classroom> getClassroomsByPage(int page, int size, String keyword, String building, Integer status, String roomType) {
        Page<Classroom> pager = new Page<>(page, size);
        QueryWrapper<Classroom> queryWrapper = new QueryWrapper<>();

        // 构建查询条件
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like("name", keyword));
        }
        if (StringUtils.isNotBlank(building)) {
            queryWrapper.like("building", building);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (StringUtils.isNotBlank(roomType)) {
            queryWrapper.eq("room_type", roomType); // 修正为实际数据库字段名，假设是 room_type
        }
        queryWrapper.orderByAsc("building", "name"); // 按building和name排序，不再使用不存在的room_number字段

        return classroomDao.selectPage(pager, queryWrapper);
    }

    @Override
    public List<Classroom> getAvailableClassrooms() {
        LambdaQueryWrapper<Classroom> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Classroom::getStatus, ClassroomStatus.NORMAL);
        return list(queryWrapper);
    }

    @Override
    @Transactional
    public boolean addClassroom(Classroom classroom) {
        if (getClassroomByName(classroom.getName()) != null) {
            throw new CustomException("教室名称已存在");
        }
        return save(classroom);
    }

    @Override
    @Transactional
    public boolean updateClassroom(Classroom classroom) {
        Classroom existing = getClassroomByName(classroom.getName());
        if (existing != null && !existing.getId().equals(classroom.getId())) {
            throw new CustomException("教室名称已存在");
        }
        return updateById(classroom);
    }

    @Override
    @Transactional
    public boolean deleteClassroom(Long id) {
        if (scheduleService.isAnyClassroomScheduled(Collections.singletonList(id))) {
            log.warn("Attempted to delete scheduled classroom with ID: {}", id);
            throw new RuntimeException("教室已被排课，无法删除");
        }
        return removeById(id);
    }

    public boolean checkClassroomAvailability(Long classroomId, Date startTime, Date endTime, String dayOfWeek) {
        // checkClassroomAvailability 方法尚未完全实现，总是返回 true
        log.warn("checkClassroomAvailability is not fully implemented.");
        return true;
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        ClassroomStatus classroomStatus = ClassroomStatus.fromCode(status);
        if (classroomStatus == null) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        if (ids != null && !ids.isEmpty()) {
            LambdaUpdateWrapper<Classroom> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.in(Classroom::getId, ids)
                    .set(Classroom::getStatus, classroomStatus);
            update(updateWrapper);
        }
    }

    @Transactional
    public boolean batchDeleteClassrooms(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        if (scheduleService.isAnyClassroomScheduled(ids)) {
            throw new CustomException("无法删除，部分或全部所选教室已被排课占用");
        }
        return removeByIds(ids);
    }

    @Override
    public Map<String, Object> getClassroomUsage(Long classroomId, String termInfo, String dateStr) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取教室信息
            Classroom classroom = null;
            if (classroomId != null) {
                classroom = this.getById(classroomId);
                if (classroom == null) {
                    throw new CustomException("教室不存在");
                }
                result.put("classroom", classroom);
            }

            // 2. 处理日期
            LocalDate queryDate = null;
            int weekday = -1;
            if (StringUtils.isNotBlank(dateStr)) {
                queryDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                // 获取星期几 (Java中LocalDate.getDayOfWeek()返回1-7，对应周一至周日)
                weekday = queryDate.getDayOfWeek().getValue();
            }

            // 3. 获取排课数据
            List<Schedule> schedules;
            if (classroomId != null && StringUtils.isNotBlank(termInfo)) {
                // 如果同时提供了教室ID和学期，则查询特定教室在特定学期的排课
                schedules = scheduleService.getSchedulesByClassroomIdAndTerm(classroomId, termInfo);

                // 如果指定了日期，进一步过滤
                if (weekday > 0) {
                    final int finalWeekday = weekday;
                    schedules = schedules.stream()
                            .filter(s -> s.getDayOfWeek() != null && s.getDayOfWeek().equals(finalWeekday))
                            .collect(Collectors.toList());
                }
            } else if (classroomId != null) {
                // 只有教室ID，查询该教室的所有排课
                schedules = scheduleService.getSchedulesByClassroomId(classroomId);
            } else {
                // 如果没有提供教室ID，返回空数组
                schedules = new ArrayList<>();
            }

            // 4. 构建返回数据
            // 将Schedule转换为ScheduleDTO
            List<ScheduleDTO> scheduleDTOs = schedules.stream()
                    .map(ScheduleDTO::fromSchedule)
                    .collect(Collectors.toList());

            // 按星期几分组
            Map<Integer, List<ScheduleDTO>> weekdayUsage = scheduleDTOs.stream()
                    .collect(Collectors.groupingBy(ScheduleDTO::getWeekday));

            result.put("weekdayUsage", weekdayUsage);

            // 5. 添加利用率统计
            if (classroomId != null && StringUtils.isNotBlank(termInfo)) {
                // 假设一天有12个可用时间段
                int totalSlotsPerDay = 12;
                int totalSlots = totalSlotsPerDay * 7; // 一周总时间段
                int occupiedSlots = schedules.size();
                double usageRate = (double) occupiedSlots / totalSlots;

                Map<String, Object> statistics = new HashMap<>();
                statistics.put("totalSlots", totalSlots);
                statistics.put("occupiedSlots", occupiedSlots);
                statistics.put("usageRate", usageRate);

                result.put("statistics", statistics);
            }

            return result;

        } catch (Exception e) {
            log.error("获取教室使用情况失败", e);
            throw new RuntimeException("获取教室使用情况失败: " + e.getMessage());
        }
    }

    @Override
    public List<Classroom> getAvailableRooms(String dateStr, String timeSlot, Integer weekdayParam,
                                             String termInfo, String building, String roomType,
                                             Integer minCapacity) {
        try {
            // 1. 参数处理
            // 使用final变量来存储weekday的值
            final Integer weekday;
            // 如果提供了日期，则从日期中获取星期几，否则使用提供的weekday参数
            if (StringUtils.isNotBlank(dateStr) && weekdayParam == null) {
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                weekday = date.getDayOfWeek().getValue();
            } else {
                weekday = weekdayParam;
            }

            // 解析时间段
            LocalTime startTime = null;
            LocalTime endTime = null;
            if (StringUtils.isNotBlank(timeSlot)) {
                String[] times = timeSlot.split("-");
                if (times.length == 2) {
                    startTime = LocalTime.parse(times[0], DateTimeFormatter.ofPattern("HH:mm"));
                    endTime = LocalTime.parse(times[1], DateTimeFormatter.ofPattern("HH:mm"));
                }
            }

            // 2. 获取所有可用状态的教室
            LambdaQueryWrapper<Classroom> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(Classroom::getStatus, ClassroomStatus.NORMAL);

            // 应用其他筛选条件
            if (StringUtils.isNotBlank(building)) {
                queryWrapper.eq(Classroom::getBuilding, building);
            }

            if (StringUtils.isNotBlank(roomType)) {
                try {
                    Integer roomTypeCode = Integer.parseInt(roomType);
                    queryWrapper.eq(Classroom::getRoomType, roomTypeCode);
                } catch (NumberFormatException e) {
                    log.warn("Invalid roomType provided: " + roomType);
                }
            }

            if (minCapacity != null && minCapacity > 0) {
                queryWrapper.ge(Classroom::getCapacity, minCapacity);
            }

            List<Classroom> allAvailableRooms = this.list(queryWrapper);

            // 3. 如果没有提供时间相关参数，直接返回结果
            if (weekday == null || termInfo == null || startTime == null || endTime == null) {
                return allAvailableRooms;
            }

            // 4. 获取所有在此时间段已被占用的教室ID
            List<Long> occupiedClassroomIds = new ArrayList<>();

            // 遍历所有教室，检查是否与现有排课冲突
            for (Classroom classroom : allAvailableRooms) {
                List<Schedule> schedules = scheduleService.getSchedulesByClassroomIdAndTerm(classroom.getId(), termInfo);

                // 筛选出同一天的排课
                List<Schedule> sameWeekdaySchedules = schedules.stream()
                        .filter(s -> s.getDayOfWeek() != null && s.getDayOfWeek().equals(weekday))
                        .collect(Collectors.toList());

                // 将Schedule转换为ScheduleDTO进行时间比较
                for (Schedule schedule : sameWeekdaySchedules) {
                    ScheduleDTO dto = ScheduleDTO.fromSchedule(schedule);
                    if (dto == null || dto.getStartTime() == null || dto.getEndTime() == null) {
                        continue;
                    }

                    // 解析DTO中的时间字符串
                    LocalTime scheduleStart = LocalTime.parse(dto.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime scheduleEnd = LocalTime.parse(dto.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));

                    // 判断时间是否重叠
                    if (!(!endTime.isAfter(scheduleStart) || !startTime.isBefore(scheduleEnd))) {
                        occupiedClassroomIds.add(classroom.getId());
                        break;
                    }
                }
            }

            // 5. 过滤出可用的教室
            return allAvailableRooms.stream()
                    .filter(classroom -> !occupiedClassroomIds.contains(classroom.getId()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取可用教室失败", e);
            throw new RuntimeException("获取可用教室失败: " + e.getMessage());
        }
    }
}