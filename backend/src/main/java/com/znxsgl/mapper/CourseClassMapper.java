package com.znxsgl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.znxsgl.entity.CourseClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseClassMapper extends BaseMapper<CourseClass> {

    @Select("SELECT ci.id AS classId, ci.class_name AS className, " +
            "(SELECT COUNT(*) FROM user u WHERE u.class_id = ci.id AND u.role = 1) AS studentCount " +
            "FROM course_class cc " +
            "JOIN class_info ci ON ci.id = cc.class_id " +
            "WHERE cc.course_id = #{courseId} AND cc.semester = '2025-2026-2'")
    List<Map<String, Object>> findClassesByCourseId(@Param("courseId") Long courseId);
}
