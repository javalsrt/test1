package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("focus_session")
public class FocusSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer durationSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { userId = v; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer v) { durationSeconds = v; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime v) { startedAt = v; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime v) { finishedAt = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
