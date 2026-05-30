package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("quiz_session")
public class QuizSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String subject;
    private String subjectType;
    private Integer sessionNo;
    private Integer totalQuestions;
    private Integer answeredCount;
    private Integer correctCount;
    private Integer skipCount;
    private Integer totalDurationSec;
    private String scores;
    private String strengths;
    private String weaknesses;
    private String suggestion;
    private String studyPlan;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { userId = v; }
    public String getSubject() { return subject; }
    public void setSubject(String v) { subject = v; }
    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String v) { subjectType = v; }
    public Integer getSessionNo() { return sessionNo; }
    public void setSessionNo(Integer v) { sessionNo = v; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer v) { totalQuestions = v; }
    public Integer getAnsweredCount() { return answeredCount; }
    public void setAnsweredCount(Integer v) { answeredCount = v; }
    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer v) { correctCount = v; }
    public Integer getSkipCount() { return skipCount; }
    public void setSkipCount(Integer v) { skipCount = v; }
    public Integer getTotalDurationSec() { return totalDurationSec; }
    public void setTotalDurationSec(Integer v) { totalDurationSec = v; }
    public String getScores() { return scores; }
    public void setScores(String v) { scores = v; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String v) { strengths = v; }
    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String v) { weaknesses = v; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String v) { suggestion = v; }
    public String getStudyPlan() { return studyPlan; }
    public void setStudyPlan(String v) { studyPlan = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
