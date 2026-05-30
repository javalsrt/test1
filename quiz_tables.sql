-- ============================================================
-- 答题系统表
-- ============================================================
USE znxsglTest;

-- 答题会话主表
CREATE TABLE IF NOT EXISTS quiz_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject VARCHAR(100) COMMENT '答题科目',
    subject_type VARCHAR(20) COMMENT '专业/公共',
    session_no INT DEFAULT 1 COMMENT '用户第几次答题',
    total_questions INT COMMENT '总题数',
    answered_count INT DEFAULT 0 COMMENT '已答题数',
    correct_count INT DEFAULT 0 COMMENT '正确数（客观题）',
    skip_count INT DEFAULT 0 COMMENT '跳过/不会',
    total_duration_sec INT DEFAULT 0 COMMENT '总耗时秒',
    scores JSON COMMENT '六维评分',
    strengths JSON COMMENT '长处',
    weaknesses JSON COMMENT '短处',
    suggestion TEXT COMMENT 'AI学习建议',
    study_plan JSON COMMENT '学习计划',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/completed/evaluated',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    INDEX idx_user (user_id, created_at)
);

-- 每题作答详情
CREATE TABLE IF NOT EXISTS quiz_answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    question_index INT COMMENT '题号 1-N',
    question_type VARCHAR(20) COMMENT '单选/判断/解析/填空',
    subject VARCHAR(100),
    question TEXT COMMENT '题目',
    options JSON COMMENT '选项列表',
    user_answer TEXT COMMENT '学生答案',
    correct_answer VARCHAR(500) COMMENT '正确答案',
    is_correct TINYINT COMMENT '1对 0错 -1不会 -2跳过',
    duration_sec INT DEFAULT 0 COMMENT '本题耗时秒',
    modified_count INT DEFAULT 0 COMMENT '修改次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES quiz_session(id),
    INDEX idx_session (session_id)
);
