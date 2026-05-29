package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("document_vector")
public class DocumentVector {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseName;
    private String docName;
    private String contentChunk;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String v) { courseName = v; }
    public String getDocName() { return docName; }
    public void setDocName(String v) { docName = v; }
    public String getContentChunk() { return contentChunk; }
    public void setContentChunk(String v) { contentChunk = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
