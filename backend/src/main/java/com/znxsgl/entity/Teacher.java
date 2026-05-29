package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;

@TableName("teacher")
public class Teacher {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String teacherNo;
    private String realName;
    private Integer gender;
    private String title;
    private Long deptId;
    private String email;
    private String phone;
    private String avatarUrl;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTeacherNo() { return teacherNo; }
    public void setTeacherNo(String teacherNo) { this.teacherNo = teacherNo; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
