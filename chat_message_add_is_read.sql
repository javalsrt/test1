-- 给 chat_message 表添加 is_read 字段
ALTER TABLE chat_message ADD COLUMN is_read TINYINT(1) DEFAULT 0 NOT NULL AFTER content;
