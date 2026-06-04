package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseName;
    private Long userId;
    private String senderName;
    private String senderRole;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;
    private Long mentionUserId; // @私密消息的目标学生ID（null=公开消息）

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
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer v) { isRead = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public Long getMentionUserId() { return mentionUserId; }
    public void setMentionUserId(Long v) { mentionUserId = v; }
}
