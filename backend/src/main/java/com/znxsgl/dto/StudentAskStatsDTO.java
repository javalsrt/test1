package com.znxsgl.dto;

/**
 * 学生提问统计（教师端查看）
 */
public class StudentAskStatsDTO {
    private Long userId;
    private String studentNo;
    private String realName;
    private String className;
    private int askCount;

    public Long getUserId() { return userId; }
    public void setUserId(Long v) { userId = v; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String v) { studentNo = v; }
    public String getRealName() { return realName; }
    public void setRealName(String v) { realName = v; }
    public String getClassName() { return className; }
    public void setClassName(String v) { className = v; }
    public int getAskCount() { return askCount; }
    public void setAskCount(int v) { askCount = v; }
}
