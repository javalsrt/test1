package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("check_in")
public class CheckIn {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String courseName;
    private Long createdBy;
    private String creatorName;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer active; // 0=closed, 1=active

    @TableLogic
    private Integer deleted;
}
