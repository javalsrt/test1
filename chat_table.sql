-- ============================================================
-- 聊天消息表
-- ============================================================
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(200) NOT NULL COMMENT '课程名',
    user_id BIGINT NOT NULL COMMENT '发送者用户ID',
    sender_name VARCHAR(100) DEFAULT '' COMMENT '发送者姓名',
    sender_role VARCHAR(20) NOT NULL COMMENT 'student/teacher/ai',
    content TEXT NOT NULL COMMENT '消息内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    INDEX idx_course (course_name),
    INDEX idx_user_id (user_id),
    INDEX idx_course_time (course_name, created_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='课程聊天消息';
