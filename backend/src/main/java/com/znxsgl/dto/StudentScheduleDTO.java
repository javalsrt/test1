package com.znxsgl.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public class StudentScheduleDTO {
    private Long scheduleId;
    private Long courseId;
    private String courseName;
    private String teacherName;
    private Integer dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private Integer startNode;
    private Integer step;
    private String classroom;
    private String semester;
    private String weeks;

    // Getters & Setters
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long v) { this.scheduleId = v; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long v) { this.courseId = v; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String v) { this.courseName = v; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String v) { this.teacherName = v; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer v) { this.dayOfWeek = v; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime v) { this.startTime = v; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime v) { this.endTime = v; }
    public Integer getStartNode() { return startNode; }
    public void setStartNode(Integer v) { this.startNode = v; }
    public Integer getStep() { return step; }
    public void setStep(Integer v) { this.step = v; }
    public String getClassroom() { return classroom; }
    public void setClassroom(String v) { this.classroom = v; }
    public String getSemester() { return semester; }
    public void setSemester(String v) { this.semester = v; }
    public String getWeeks() { return weeks; }
    public void setWeeks(String v) { this.weeks = v; }
}
