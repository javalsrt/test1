package com.znxsgl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("check_in_record")
public class CheckInRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkInId;
    private Long studentId;
    private String studentName;
    private LocalDateTime checkedAt;
}
