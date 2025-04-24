package com.campus.mapper;

import com.campus.entity.Teacher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.enums.StatusEnum;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TeacherMapper extends BaseMapper<Teacher> {
    @Select("SELECT * FROM teacher WHERE status = #{statusCode}")
    List<Teacher> selectTeachersByStatus(@Param("statusCode") Integer statusCode);


}