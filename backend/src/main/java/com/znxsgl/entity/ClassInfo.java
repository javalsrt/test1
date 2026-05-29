package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;

@TableName("class_info")
public class ClassInfo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String className;
    private String major;
    private String department;
    private String grade;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}
