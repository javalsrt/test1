package com.znxsgl.dto;

public class LoginResponse {
    private String token;
    private String realName;
    private String username;
    private Integer role;       // 1学生 2教师 3管理员
    private Long userId;
    private String avatarUrl;
    private String message;

    public LoginResponse() {}

    public LoginResponse(String message) {
        this.message = message;
    }

    public static LoginResponse success(String token, String realName, String username,
                                          Integer role, Long userId, String avatarUrl) {
        LoginResponse r = new LoginResponse();
        r.token = token;
        r.realName = realName;
        r.username = username;
        r.role = role;
        r.userId = userId;
        r.avatarUrl = avatarUrl;
        r.message = "登录成功";
        return r;
    }

    // Getters & Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
