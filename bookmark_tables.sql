-- 收藏题目表
CREATE TABLE IF NOT EXISTS question_bookmark (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question_type VARCHAR(20) COMMENT '题型',
    subject VARCHAR(100) COMMENT '科目',
    question TEXT COMMENT '题目内容',
    user_answer TEXT COMMENT '学生答案',
    correct_answer VARCHAR(500) COMMENT '正确答案',
    knowledge TEXT COMMENT '知识点解析',
    error_reason TEXT COMMENT '错因分析',
    improve TEXT COMMENT '改进建议',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

-- 明白标记字段（如果不存在则添加）
ALTER TABLE quiz_answer ADD COLUMN IF NOT EXISTS understood TINYINT DEFAULT 0 COMMENT '0未标记 1已明白';

-- 错题分析结果缓存（基于最新session_id，标记明白不触发刷新）
CREATE TABLE IF NOT EXISTS wrong_analysis_cache (
    user_id BIGINT PRIMARY KEY,
    cache_hash VARCHAR(50) DEFAULT '0' COMMENT '最新测评session_id',
    analysis_json MEDIUMTEXT COMMENT '分析结果JSON',
    updated_at DATETIME
);
