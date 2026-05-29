package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@TableName("schedule")
public class Schedule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long courseId;
    private String courseName;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer startNode;
    private Integer step;
    private String classroom;
    private String semester;
    private String weeks;
    private Integer status;  // 1=正常 0=已下架

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Integer getStartNode() { return startNode; }
    public void setStartNode(Integer startNode) { this.startNode = startNode; }
    public Integer getStep() { return step; }
    public void setStep(Integer step) { this.step = step; }
    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getWeeks() { return weeks; }
    public void setWeeks(String weeks) { this.weeks = weeks; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
