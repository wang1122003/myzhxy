package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dao.ClassroomDao;
import com.campus.entity.Classroom;
import com.campus.exception.CustomException;
import com.campus.service.ClassroomService;
import com.campus.service.ScheduleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 教室服务实现类
 */
@Service
public class ClassroomServiceImpl implements ClassroomService {

    @Autowired
    private ClassroomDao classroomDao;

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 分页并按条件查询教室 (使用 MyBatis-Plus 分页)
     */
    @Override
    public IPage<Classroom> getClassroomsByPage(int page, int size, String keyword, String building, Integer status) {
        // 1. 创建 MyBatis-Plus 的 Page 对象
        Page<Classroom> pageParam = new Page<>(page, size);

        // 2. 构建查询条件 (使用 LambdaQueryWrapper 更安全)
        LambdaQueryWrapper<Classroom> queryWrapper = new LambdaQueryWrapper<>();
        // 同时匹配教室号或教学楼名
        queryWrapper.and(StringUtils.isNotEmpty(keyword), wrapper -> wrapper
                .like(Classroom::getRoomNo, keyword)
                .or()
                .like(Classroom::getBuildingName, keyword));


        // 添加教学楼和状态的过滤条件
        queryWrapper.eq(StringUtils.isNotEmpty(building), Classroom::getBuildingName, building);
        queryWrapper.eq(status != null, Classroom::getStatus, status);

        // 添加排序，例如按教学楼和教室号排序
        queryWrapper.orderByAsc(Classroom::getBuildingName).orderByAsc(Classroom::getRoomNo);


        // 3. 直接调用并返回结果，移除冗余变量 pageData
        // IPage<Classroom> pageData = classroomDao.selectPage(pageParam, queryWrapper);
        // return pageData;
        return classroomDao.selectPage(pageParam, queryWrapper);
    }

    @Override
    public Classroom getClassroomById(Long id) {
        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        return classroomDao.selectById(id);
    }

    @Override
    public Classroom getClassroomByRoomNo(String roomNo) {
        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        LambdaQueryWrapper<Classroom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Classroom::getRoomNo, roomNo);
        return classroomDao.selectOne(queryWrapper);
    }

    @Override
    public List<Classroom> getAllClassrooms() {
        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        return classroomDao.selectList(null); // 查询所有
    }

    @Override
    public List<Classroom> getClassroomsByBuilding(String building) {
        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        LambdaQueryWrapper<Classroom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Classroom::getBuildingName, building);
        return classroomDao.selectList(queryWrapper);
    }

    @Override
    public List<Classroom> getClassroomsByRoomType(Integer roomType) {
        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        LambdaQueryWrapper<Classroom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Classroom::getRoomType, roomType);
        return classroomDao.selectList(queryWrapper);
    }

    @Override
    public List<Classroom> getClassroomsByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        LambdaQueryWrapper<Classroom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(minCapacity != null, Classroom::getCapacity, minCapacity); // 大于等于 minCapacity
        queryWrapper.le(maxCapacity != null, Classroom::getCapacity, maxCapacity); // 小于等于 maxCapacity
        return classroomDao.selectList(queryWrapper);
    }

    @Override
    public List<Classroom> getAvailableClassrooms() {
        // 这个方法可能需要自定义 SQL 查询，因为涉及到关联查询 `schedule` 表
        // 暂时保持不变，但需要注意 MyBatis-Plus 的 BaseMapper 可能无法直接实现
        // 需要在 ClassroomDao 中定义自定义方法和 SQL
        return classroomDao.findAvailable(1, 1);
    }


    @Override
    @Transactional
    public boolean addClassroom(Classroom classroom) {
        // 设置创建时间和更新时间
        Date now = new Date();
        classroom.setCreateTime(now);
        classroom.setUpdateTime(now);

        // 如果没有设置状态，默认为可用
        if (classroom.getStatus() == null) {
            classroom.setStatus(1);
        }

        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        return classroomDao.insert(classroom) > 0;
    }

    @Override
    @Transactional
    public boolean updateClassroom(Classroom classroom) {
        // 设置更新时间
        classroom.setUpdateTime(new Date());

        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        return classroomDao.updateById(classroom) > 0; // 使用 updateById
    }

    @Override
    @Transactional
    public boolean deleteClassroom(Long id) {
        if (scheduleService.isClassroomInUse(id)) {
            throw new CustomException("无法删除：该教室已被课表引用");
        }
        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        return classroomDao.deleteById(id) > 0; // 使用 deleteById
    }

    @Override
    @Transactional
    public boolean batchDeleteClassrooms(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        List<Long> idList = Arrays.asList(ids); // 转换为 List
        for (Long id : idList) {
            if (scheduleService.isClassroomInUse(id)) {
                Classroom classroom = classroomDao.selectById(id); // 使用 selectById
                String roomIdentifier = (classroom != null)
                        ? classroom.getBuildingName() + "-" + classroom.getRoomNo()
                        : "ID:" + id;
                throw new CustomException("无法删除：教室 '" + roomIdentifier + "' 已被课表引用");
            }
        }
        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        // 注意: deleteBatchIds 在较新版本的 Mybatis-Plus 中可能已弃用，
        // 推荐使用 ServiceImpl 的 removeByIds 或 BaseMapper 的 deleteByIds
        return classroomDao.deleteBatchIds(idList) > 0; 
    }

    @Override
    @Transactional
    public boolean updateClassroomStatus(Long id, Integer status) {
        Classroom classroom = new Classroom();
        classroom.setId(id);
        classroom.setStatus(status);
        classroom.setUpdateTime(new Date());

        // 假设 ClassroomDao 继承了 BaseMapper<Classroom>
        return classroomDao.updateById(classroom) > 0; // 使用 updateById
    }
}