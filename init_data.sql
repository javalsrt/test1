-- ============================================================
-- 初始化：插入测试用户数据 (BCrypt 加密密码：123456)
-- 运行前确认表已创建
-- ============================================================

-- 先创建班级
INSERT IGNORE INTO class_info (id, class_name, major, department, grade) VALUES
(1, '计算机科学与技术2023级1班', '计算机科学与技术', '信息工程学院', '2023'),
(2, '计算机科学与技术2023级2班', '计算机科学与技术', '信息工程学院', '2023');

-- 创建测试学生 (密码：123456)
INSERT IGNORE INTO user (student_no, username, password_hash, real_name, role, class_id, major, grade, status)
VALUES
('20240101001', 'student1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张明', 1, 1, '计算机科学与技术', '2023', 1),
('20240101002', 'student2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李婷', 1, 1, '计算机科学与技术', '2023', 1),
('20240102001', 'student3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王浩', 1, 2, '计算机科学与技术', '2023', 1),
('20240102002', 'student4', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '陈雪', 1, 2, '计算机科学与技术', '2023', 1);

-- 创建测试教师 (密码：123456)
INSERT IGNORE INTO user (username, password_hash, real_name, role, email, status)
VALUES
('teacher1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张明远', 2, 'zhangmy@university.edu.cn', 1),
('teacher2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李伟强', 2, 'liwq@university.edu.cn', 1);

-- 创建管理员 (密码：123456)
INSERT IGNORE INTO user (username, password_hash, real_name, role, email, status)
VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 3, 'admin@university.edu.cn', 1);
