package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("quiz_answer")
public class QuizAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Integer questionIndex;
    private String questionType;
    private String subject;
    private String question;
    private String options;
    private String userAnswer;
    private String correctAnswer;
    private Integer isCorrect;
    private Integer durationSec;
    private Integer modifiedCount;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long v) { sessionId = v; }
    public Integer getQuestionIndex() { return questionIndex; }
    public void setQuestionIndex(Integer v) { questionIndex = v; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String v) { questionType = v; }
    public String getSubject() { return subject; }
    public void setSubject(String v) { subject = v; }
    public String getQuestion() { return question; }
    public void setQuestion(String v) { question = v; }
    public String getOptions() { return options; }
    public void setOptions(String v) { options = v; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String v) { userAnswer = v; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String v) { correctAnswer = v; }
    public Integer getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Integer v) { isCorrect = v; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer v) { durationSec = v; }
    public Integer getModifiedCount() { return modifiedCount; }
    public void setModifiedCount(Integer v) { modifiedCount = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
