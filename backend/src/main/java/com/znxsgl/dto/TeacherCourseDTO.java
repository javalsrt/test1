package com.znxsgl.dto;

import java.math.BigDecimal;
import java.util.List;

public class TeacherCourseDTO {
    private Long courseId;
    private String courseName;
    private String semester;
    private List<ClazzDTO> classes;
    private String courseType;
    private String description;
    private boolean active;
    private BigDecimal credit; // 课时/学分

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public List<ClazzDTO> getClasses() { return classes; }
    public void setClasses(List<ClazzDTO> classes) { this.classes = classes; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }

    public static class ClazzDTO {
        private Long classId;
        private String className;
        private int studentCount;
        private boolean scheduled; // 该班级是否已排课

        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public int getStudentCount() { return studentCount; }
        public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
        public boolean isScheduled() { return scheduled; }
        public void setScheduled(boolean scheduled) { this.scheduled = scheduled; }
    }
}
