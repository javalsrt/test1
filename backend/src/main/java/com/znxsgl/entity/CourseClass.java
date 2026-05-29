package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;

@TableName("course_class")
public class CourseClass {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long classId;
    private String semester;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}
