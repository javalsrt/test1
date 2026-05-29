package com.znxsgl.student.model;

import com.google.gson.annotations.SerializedName;

public class ChatMsgDto {
    @SerializedName("id")
    private long id;
    @SerializedName("courseName")
    private String courseName;
    @SerializedName("userId")
    private long userId;
    @SerializedName("senderName")
    private String senderName;
    @SerializedName("senderRole")
    private String senderRole;
    @SerializedName("content")
    private String content;
    @SerializedName("createdAt")
    private String createdAt;

    public long getId() { return id; }
    public void setId(long v) { id = v; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String v) { courseName = v; }
    public long getUserId() { return userId; }
    public void setUserId(long v) { userId = v; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String v) { senderName = v; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String v) { senderRole = v; }
    public String getContent() { return content; }
    public void setContent(String v) { content = v; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String v) { createdAt = v; }
}
