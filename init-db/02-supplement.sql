-- ============================================================
-- Docker 环境初始化脚本
-- 由 Docker 的 docker-entrypoint-initdb.d 自动执行
-- ============================================================

-- seed_all.sql 已包含 USE znxsglTest，但 Docker 环境下数据库已由环境变量创建
USE znxsglTest;

-- 补充: chat_message 表的 is_read 字段
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'znxsglTest' AND TABLE_NAME = 'chat_message' AND COLUMN_NAME = 'is_read');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE chat_message ADD COLUMN is_read TINYINT(1) DEFAULT 0 NOT NULL AFTER content',
    'SELECT ''is_read 已存在'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
