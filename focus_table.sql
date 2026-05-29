-- ============================================================
-- 专注模式计时记录表
-- 使用方式：mysql -u root -p znxsglTest < focus_table.sql
-- ============================================================
USE znxsglTest;

CREATE TABLE IF NOT EXISTS focus_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '学生ID',
    duration_seconds INT DEFAULT 0 COMMENT '计时秒数',
    started_at DATETIME COMMENT '开始时间',
    finished_at DATETIME COMMENT '结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_user_time (user_id, finished_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB;
