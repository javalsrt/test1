package com.znxsgl.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ChatMessageDTO {
    private Long id;
    private String courseName;
    private Long userId;
    private String senderName;
    private String senderRole;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String v) { courseName = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { userId = v; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String v) { senderName = v; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String v) { senderRole = v; }
    public String getContent() { return content; }
    public void setContent(String v) { content = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
