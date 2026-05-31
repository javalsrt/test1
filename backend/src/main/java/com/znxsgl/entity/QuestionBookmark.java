package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("question_bookmark")
public class QuestionBookmark {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String questionType;
    private String subject;
    private String question;
    private String userAnswer;
    private String correctAnswer;
    private String knowledge;
    private String errorReason;
    private String improve;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { userId = v; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String v) { questionType = v; }
    public String getSubject() { return subject; }
    public void setSubject(String v) { subject = v; }
    public String getQuestion() { return question; }
    public void setQuestion(String v) { question = v; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String v) { userAnswer = v; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String v) { correctAnswer = v; }
    public String getKnowledge() { return knowledge; }
    public void setKnowledge(String v) { knowledge = v; }
    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String v) { errorReason = v; }
    public String getImprove() { return improve; }
    public void setImprove(String v) { improve = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
