-- ============================================================
-- seed_all.sql — 一套完整种子数据
-- 执行: mysql -u root -p < seed_all.sql (无需 USE，脚本自带 USE)
-- 密码: 所有用户密码 = 123456 (后端启动后自动 BCrypt 加密)
-- ============================================================

USE znxsglTest;

-- ============================================================
-- 0a. 创建聊天消息表（如果不存在）
-- ============================================================
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(200) NOT NULL,
    user_id BIGINT NOT NULL,
    sender_name VARCHAR(100) DEFAULT '',
    sender_role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chat_course (course_name),
    INDEX idx_chat_user (user_id),
    INDEX idx_chat_course_time (course_name, created_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 0b. 创建文档向量表（RAG 知识库）
CREATE TABLE IF NOT EXISTS document_vector (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(200) NOT NULL,
    doc_name VARCHAR(200) DEFAULT '',
    content_chunk TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course (course_name),
    INDEX idx_course_content (course_name, content_chunk(100))
) ENGINE=InnoDB;

-- 0c. 创建专注计时表
CREATE TABLE IF NOT EXISTS focus_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    duration_seconds INT DEFAULT 0,
    started_at DATETIME,
    finished_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 0d. 创建学生状态表（教师端查看专注状态）
CREATE TABLE IF NOT EXISTS student_status (
    user_id BIGINT PRIMARY KEY,
    status VARCHAR(20) DEFAULT 'idle' COMMENT 'focusing/idle',
    last_active DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 0e. 确保 schedule 表结构完整
-- ============================================================
-- 添加 status 列（1=正常 0=已下架）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'znxsglTest' AND TABLE_NAME = 'schedule' AND COLUMN_NAME = 'status');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE schedule ADD COLUMN status TINYINT DEFAULT 1 COMMENT ''1=正常 0=已下架'' AFTER weeks',
    'SELECT ''status 已存在'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 course_name 列（忽略已存在错误）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'znxsglTest' AND TABLE_NAME = 'schedule' AND COLUMN_NAME = 'course_name');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE schedule ADD COLUMN course_name VARCHAR(100) COMMENT ''课程名称'' AFTER course_id, ALGORITHM=INPLACE',
    'SELECT ''course_name 已存在'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- 确保 course_id 可空
ALTER TABLE schedule MODIFY course_id BIGINT NULL;

-- ============================================================
-- 1. 清空所有数据表（保留结构）
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE student_status;
TRUNCATE TABLE focus_session;
TRUNCATE TABLE document_vector;
TRUNCATE TABLE chat_message;
TRUNCATE TABLE schedule;
TRUNCATE TABLE course_class;
TRUNCATE TABLE course;
TRUNCATE TABLE user;
TRUNCATE TABLE teacher;
TRUNCATE TABLE class_info;
TRUNCATE TABLE department;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 2. 院系/部门 (2个)
-- ============================================================
INSERT INTO department (dept_name, dept_code, dept_type) VALUES
('计算机与信息工程学院', 'CS', '教学单位'),
('教育科学学院',           'EDU', '教学单位'),
('学生工作处',             'STU', '行政单位');

-- ============================================================
-- 3. 班级 (2个班)
-- ============================================================
INSERT INTO class_info (class_name, major, department, grade) VALUES
('计算机科学与技术2023级1班', '计算机科学与技术', '计算机与信息工程学院', '2023'),
('教育技术学2023级1班',     '教育技术学',     '教育科学学院',         '2023');

-- ============================================================
-- 4. 教师 (8人)
-- ============================================================
INSERT INTO teacher (teacher_no, real_name, dept_id, title, email) VALUES
('T2023001', '张明远', 1, '副教授', 'zhangmy@univ.edu.cn'),
('T2023002', '李伟强', 1, '讲师',   'liwq@univ.edu.cn'),
('T2023003', '王丽华', 1, '教授',   'wanglh@univ.edu.cn'),
('T2023004', '陈建峰', 1, '讲师',   'chenjf@univ.edu.cn'),
('T2023005', '吴志远', 1, '副教授', 'wuzy@univ.edu.cn'),
('T2023006', '冯海涛', 1, '讲师',   'fengh@univ.edu.cn'),
('T2023007', '郑国栋', 3, '讲师',   'zhenggd@univ.edu.cn'),
('T2023008', '赵雅文', 2, '副教授', 'zhaoyw@univ.edu.cn');

-- ============================================================
-- 5. 用户 (17人: 管理员1 + 教师8 + 学生8)

--    密码字段存明文 "123456"，后端启动时 DataInitializer 自动 BCrypt 加密
-- ============================================================
INSERT INTO user (username, password_hash, real_name, role, email, status) VALUES
-- 管理员
('admin',    '123456', '系统管理员', 3, 'admin@univ.edu.cn', 1),
-- 教师（使用真实姓名拼音简写作为用户名）
('zhangmy',  '123456', '张明远', 2, 'zhangmy@univ.edu.cn',  1),
('liwq',     '123456', '李伟强', 2, 'liwq@univ.edu.cn',     1),
('wanglh',   '123456', '王丽华', 2, 'wanglh@univ.edu.cn',   1),
('chenjf',   '123456', '陈建峰', 2, 'chenjf@univ.edu.cn',   1),
('wuzy',     '123456', '吴志远', 2, 'wuzy@univ.edu.cn',     1),
('fengh',    '123456', '冯海涛', 2, 'fengh@univ.edu.cn',    1),
('zhenggd',  '123456', '郑国栋', 2, 'zhenggd@univ.edu.cn',  1),
('zhaoyw',   '123456', '赵雅文', 2, 'zhaoyw@univ.edu.cn',   1);

-- 学生（计算机2301班 4人 + 计教2301班 4人）
INSERT INTO user (username, student_no, password_hash, real_name, role, class_id, major, grade, status) VALUES
-- 计算机2301班 (class_id=1)
('student1', '20240101001', '123456', '张明',   1, 1, '计算机科学与技术', '2023', 1),
('student2', '20240101002', '123456', '李婷',   1, 1, '计算机科学与技术', '2023', 1),
('student3', '20240101003', '123456', '王浩',   1, 1, '计算机科学与技术', '2023', 1),
('student4', '20240101004', '123456', '陈雪',   1, 1, '计算机科学与技术', '2023', 1),
-- 计教2301班 (class_id=2)
('student5', '20250102001', '123456', '刘晓雨', 1, 2, '教育技术学',     '2023', 1),
('student6', '20250102002', '123456', '赵海龙', 1, 2, '教育技术学',     '2023', 1),
('student7', '20250102003', '123456', '孙悦然', 1, 2, '教育技术学',     '2023', 1),
('student8', '20250102004', '123456', '周明达', 1, 2, '教育技术学',     '2023', 1);

-- ============================================================
-- 6. 课程 (14门)
--    列顺序: course_name, course_no, teacher_id, dept_id, semester, course_type, credit, description
-- ============================================================
INSERT INTO course (course_name, course_no, teacher_id, dept_id, semester, course_type, credit, description) VALUES
('数据结构与算法基础',   'CS101', 3, 1, '2025-2026-2', '必修', 4, '线性表、树、图、排序算法'),
('Python程序设计',       'CS102', 4, 1, '2025-2026-2', '必修', 3, 'Python基础语法与数据分析'),
('微信小程序设计',       'CS103', 2, 1, '2025-2026-2', '必修', 3, 'WXML/WXSS/JS开发'),
('高级网站技术',         'CS104', 5, 1, '2025-2026-2', '限选', 3, '前端框架与后端API'),
('多媒体课件设计与开发', 'CS105', 1, 1, '2025-2026-2', '限选', 2, '课件制作工具与设计'),
('计算机综合实训',       'CS106', 6, 1, '2025-2026-2', '必修', 4, '综合项目开发实践'),
-- 教育类课程
('教师书写技能',         'EDU201', 8, 2, '2025-2026-2', '必修', 2, '板书与书写训练'),
('教师口语',             'EDU202', 8, 2, '2025-2026-2', '必修', 2, '普通话与教学表达'),
('微课制作',             'EDU203', 8, 2, '2025-2026-2', '限选', 2, '微课视频制作'),
('教师资格考试实务',     'EDU204', 8, 2, '2025-2026-2', '限选', 2, '教师资格考试辅导'),
('班主任工作技能训练',   'EDU205', 2, 1, '2025-2026-2', '限选', 1, '班级管理技能'),
-- 公共课
('中国近现代史纲要',     'PUB101', 7, 3, '2025-2026-2', '必修', 3, '中国近现代史'),
('形势与政策',           'PUB102', 7, 3, '2025-2026-2', '必修', 1, '时事政策教育'),
('安全教育(六)',          'PUB103', 7, 3, '2025-2026-2', '必修', 1, '校园安全教育');

-- ============================================================
-- 7. 课程-班级分配

--    计算机2301班: CS核心6门
--    计教2301班: CS核心6门 + 教育4门 + 班主任1门 + 公共课3门 = 14门
-- ============================================================
INSERT INTO course_class (course_id, class_id, semester)
SELECT c.id, 1, '2025-2026-2' FROM course c WHERE c.course_no IN ('CS101','CS102','CS103','CS104','CS105','CS106');

INSERT INTO course_class (course_id, class_id, semester)
SELECT c.id, 2, '2025-2026-2' FROM course c WHERE c.course_no LIKE 'CS%' OR c.course_no LIKE 'EDU%' OR c.course_no LIKE 'PUB%';

-- ============================================================
-- 8. 课表数据

--    作息:
--      第一大节 08:30-09:55 (1-2节)  |  第二大节 10:10-11:35 (3-4节)
--      第三大节 11:40-12:20 (5节)    |  ←← 午休 →→
--      第四大节 14:20-15:45 (6-7节)  |  第五大节 16:00-17:25 (8-9节)
--      第六大节 19:10-21:30 (10-12节)
--    周次: 1-18周全有课
-- ============================================================

-- ===== 计算机2301班 (student1-4) =====
-- 周一
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '数据结构与算法基础', 1, '08:30', '09:55', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, 'Python程序设计', 1, '10:10', '11:35', 3, 2, '武1-316(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '微信小程序设计', 1, '11:40', '12:20', 5, 1, '地信-B402(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '高级网站技术', 1, '14:20', '15:45', 6, 2, '地信-B301(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');

-- 周二
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '高级网站技术', 2, '08:30', '09:55', 1, 2, '地信-B301(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, 'Python程序设计', 2, '10:10', '11:35', 3, 2, '武1-316(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '数据结构与算法基础', 2, '11:40', '12:20', 5, 1, '地信-B403(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '微信小程序设计', 2, '14:20', '15:45', 6, 2, '地信-B402(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 2, '16:00', '17:25', 8, 2, '地信-B303(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');

-- 周三
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '数据结构与算法基础', 3, '08:30', '09:55', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '多媒体课件设计与开发', 3, '10:10', '11:35', 3, 2, '实验3-301(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, 'Python程序设计', 3, '11:40', '12:20', 5, 1, '武1-316(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 3, '14:20', '15:45', 6, 2, '地信-B303(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');

-- 周四
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '高级网站技术', 4, '08:30', '09:55', 1, 2, '地信-B301(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '多媒体课件设计与开发', 4, '10:10', '11:35', 3, 2, '实验3-301(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '微信小程序设计', 4, '14:20', '15:45', 6, 2, '地信-B402(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');

-- 周五
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 5, '10:10', '11:35', 3, 2, '地信-B303(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 5, '11:40', '12:20', 5, 1, '地信-B303(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, 'Python程序设计', 5, '14:20', '15:45', 6, 2, '武1-316(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, 'Python程序设计', 5, '16:00', '17:25', 8, 2, '武1-316(机房)', '2025-2026-2', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student1','student2','student3','student4');

-- ===== 计教2301班 (student5-8) =====
-- 周一
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '多媒体课件设计与开发', 1, '10:10', '11:35', 3, 2, '实验3-301(机房)', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '多媒体课件设计与开发', 1, '11:40', '12:20', 5, 1, '实验3-301(机房)', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, 'Python程序设计', 1, '14:20', '15:45', 6, 2, '武1-316(机房)', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, 'Python程序设计', 1, '16:00', '17:25', 8, 2, '武1-316(机房)', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');

-- 周一 第9周 计算机综合实训
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 1, '19:10', '20:35', 10, 2, '地信-B303(机房)', '2025-2026-2', '[9]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');

-- 周二 常规课 (1-18周，第7周周二下午被高级网站技术替换，第9周全天被班主任替换)
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '微信小程序设计', 2, '08:30', '09:55', 1, 2, '地信-B402(机房)', '2025-2026-2', '[1,2,3,4,5,6,8,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '微信小程序设计', 2, '10:10', '11:35', 3, 2, '地信-B402(机房)', '2025-2026-2', '[1,2,3,4,5,6,8,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '教师口语', 2, '14:20', '15:45', 6, 2, '武1-114', '2025-2026-2', '[1,2,3,4,5,6,8,10,11,12,13,14,15,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第7周周二 高级网站技术 替换 教师口语
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '高级网站技术', 2, '14:20', '15:45', 6, 2, '地信-B301(机房)', '2025-2026-2', '[7]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '高级网站技术', 2, '16:00', '17:25', 8, 2, '地信-B301(机房)', '2025-2026-2', '[7]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第8周 安全教育
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '安全教育(六)', 2, '16:00', '17:25', 8, 2, '实验2-A504', '2025-2026-2', '[8]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第9周周二 班主任工作技能训练
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '班主任工作技能训练', 2, '08:30', '09:55', 1, 2, '武1-203', '2025-2026-2', '[9]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '班主任工作技能训练', 2, '10:10', '11:35', 3, 2, '武1-203', '2025-2026-2', '[9]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');

-- 周三
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '数据结构与算法基础', 3, '08:30', '09:55', 1, 2, '地信-B403(机房)', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '数据结构与算法基础', 3, '10:10', '11:35', 3, 2, '地信-B403(机房)', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '中国近现代史纲要', 3, '14:20', '16:40', 6, 3, '实验2-A402', '2025-2026-2', '[1,2,3,4,6,10,11,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第14周 形势与政策 (15:05-16:40)
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '形势与政策', 3, '15:05', '16:40', 7, 2, '实验2-A402', '2025-2026-2', '[14]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第15周 形势与政策 (14:20-16:40)
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '形势与政策', 3, '14:20', '16:40', 6, 3, '实验2-A402', '2025-2026-2', '[15]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第9周 计算机综合实训 (周三晚)
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 3, '19:10', '20:35', 10, 2, '地信-B303(机房)', '2025-2026-2', '[9]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');

-- 周四
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '教师书写技能', 4, '08:30', '09:55', 1, 2, '武2-108', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '教师书写技能', 4, '10:10', '11:35', 3, 2, '智慧板书实训室', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '教师资格考试实务', 4, '14:20', '17:25', 6, 4, '武1-214', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第5周 高级网站技术 替换周四
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '高级网站技术', 4, '14:20', '15:45', 6, 2, '地信-B301(机房)', '2025-2026-2', '[5]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '高级网站技术', 4, '16:00', '17:25', 8, 2, '地信-B301(机房)', '2025-2026-2', '[5]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第11周 计算机综合实训 替换周四下午
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 4, '14:20', '15:45', 6, 2, '地信-B301(机房)', '2025-2026-2', '[11]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 4, '16:00', '17:25', 8, 2, '地信-B301(机房)', '2025-2026-2', '[11]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第15周 计算机综合实训 替换周四上午
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 4, '08:30', '09:55', 1, 2, '地信-B303(机房)', '2025-2026-2', '[15]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 4, '10:10', '11:35', 3, 2, '地信-B303(机房)', '2025-2026-2', '[15]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');

-- 周五
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '微课制作', 5, '08:30', '09:55', 1, 2, '实验3-301(机房)', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '微课制作', 5, '10:10', '11:35', 3, 2, '实验3-301(机房)', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '高级网站技术', 5, '14:20', '15:45', 6, 2, '地信-B301(机房)', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '高级网站技术', 5, '16:00', '17:25', 8, 2, '地信-B301(机房)', '2025-2026-2', '[1,2,3,4,6,10,12,13,16,17,18]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第9周 计算机综合实训 (周五上午)
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 5, '10:10', '11:35', 3, 2, '地信-B303(机房)', '2025-2026-2', '[9]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 5, '11:40', '12:20', 5, 1, '地信-B303(机房)', '2025-2026-2', '[9]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第11周 计算机综合实训 (周五下午)
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 5, '14:20', '15:45', 6, 2, '地信-B301(机房)', '2025-2026-2', '[11]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 5, '16:00', '17:25', 8, 2, '地信-B301(机房)', '2025-2026-2', '[11]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第15周 计算机综合实训 (周五上午)
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '计算机综合实训', 5, '08:30', '09:55', 1, 2, '地信-B303(机房)', '2025-2026-2', '[15]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');
-- 第15周 中国近现代史纲要 (周六网课)
INSERT INTO schedule (user_id, course_name, day_of_week, start_time, end_time, start_node, step, classroom, semester, weeks)
SELECT u.id, '中国近现代史纲要', 6, '08:30', '11:35', 1, 4, '网络虚拟教室武鸣003', '2025-2026-2', '[15]'
FROM user u WHERE u.username IN ('student5','student6','student7','student8');

-- ============================================================
-- 9. 验证
-- ============================================================
SELECT '=== 数据统计 ===' AS '';
SELECT '院系数' AS '', COUNT(*) AS '' FROM department
UNION ALL SELECT '班级数', COUNT(*) FROM class_info
UNION ALL SELECT '教师数', COUNT(*) FROM teacher
UNION ALL SELECT '用户数', COUNT(*) FROM user
UNION ALL SELECT '课程数', COUNT(*) FROM course
UNION ALL SELECT '课表记录', COUNT(*) FROM schedule;

SELECT '=== 账号密码 ===' AS '';
SELECT username AS '用户名', real_name AS '姓名',
       CASE role WHEN 1 THEN '学生' WHEN 2 THEN '教师' WHEN 3 THEN '管理员' END AS '角色',
       '123456' AS '密码'
FROM user ORDER BY role, username;
