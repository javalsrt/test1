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
    /** 向量嵌入，JSON数组格式存储（text-embedding-v3: 1024维） */
    private String embedding;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String v) { courseName = v; }
    public String getDocName() { return docName; }
    public void setDocName(String v) { docName = v; }
    public String getContentChunk() { return contentChunk; }
    public void setContentChunk(String v) { contentChunk = v; }
    public String getEmbedding() { return embedding; }
    public void setEmbedding(String v) { embedding = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
