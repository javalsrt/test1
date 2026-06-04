-- 签到相关表
CREATE TABLE IF NOT EXISTS check_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(200) NOT NULL,
    created_by BIGINT,
    creator_name VARCHAR(100),
    password VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME,
    active INT DEFAULT 1,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS check_in_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    check_in_id BIGINT NOT NULL,
    student_id BIGINT,
    student_name VARCHAR(100),
    checked_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 为 chat_message 补充索引
ALTER TABLE chat_message ADD COLUMN IF NOT EXISTS mention_user_id BIGINT NULL;
ALTER TABLE chat_message ADD INDEX IF NOT EXISTS idx_course_name (course_name);
ALTER TABLE chat_message ADD INDEX IF NOT EXISTS idx_created_at (created_at);
