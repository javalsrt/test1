-- ============================================================
-- RAG 向量存储表
-- 使用方式：mysql -u root -p znxsglTest < rag_tables.sql
-- ============================================================

USE znxsglTest;

-- 文档向量表（存储 AI 分析后的文本块 + 语义向量）
CREATE TABLE IF NOT EXISTS document_vector (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(200) NOT NULL COMMENT '课程名',
    doc_name VARCHAR(200) DEFAULT '' COMMENT '文档名称',
    content_chunk TEXT COMMENT '文本块内容',
    embedding MEDIUMTEXT COMMENT '向量嵌入(JSON数组，支持1024维以上)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course (course_name),
    INDEX idx_course_content (course_name, content_chunk(100))
) ENGINE=InnoDB;
