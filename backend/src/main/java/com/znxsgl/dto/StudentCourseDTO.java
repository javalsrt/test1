package com.znxsgl.dto;

/**
 * 学生端"我的课程"列表 DTO
 */
public class StudentCourseDTO {
    private Long courseId;
    private String courseName;
    private String teacherName;
    private String semester;
    private String courseType;
    private String description;
    private boolean active;        // 是否在线（任意班级有排课）
    private boolean published;     // 是否已发布/上架（有 status=1 记录，含占位）
    private boolean hasSchedule;   // 本班级是否有排课
    private String scheduleInfo;   // 排课信息摘要
    private int unreadCount;       // 未读消息数（>0 显示红点）

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long v) { this.courseId = v; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String v) { this.courseName = v; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String v) { this.teacherName = v; }
    public String getSemester() { return semester; }
    public void setSemester(String v) { this.semester = v; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String v) { this.courseType = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean v) { this.published = v; }
    public boolean isHasSchedule() { return hasSchedule; }
    public void setHasSchedule(boolean v) { this.hasSchedule = v; }
    public String getScheduleInfo() { return scheduleInfo; }
    public void setScheduleInfo(String v) { this.scheduleInfo = v; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int v) { this.unreadCount = v; }
}
