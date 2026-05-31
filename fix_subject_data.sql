-- ========================================
-- 修复科目分类问题的 SQL 脚本
-- ========================================

-- 1. 清除错题分析缓存（旧缓存中科目全是"其他"）
DELETE FROM wrong_analysis_cache;

-- 2. 修复已有数据：quiz_answer.subject 为空时，从 quiz_session.subject 回填
UPDATE quiz_answer a
JOIN quiz_session s ON a.session_id = s.id
SET a.subject = s.subject
WHERE (a.subject IS NULL OR a.subject = '')
  AND s.subject IS NOT NULL AND s.subject != '';

-- 3. 验证修复结果
SELECT 
    a.subject AS 'answer_subject',
    s.subject AS 'session_subject',
    COUNT(*) AS 'count'
FROM quiz_answer a
JOIN quiz_session s ON a.session_id = s.id
WHERE a.is_correct IN (0, -1, -2)
GROUP BY a.subject, s.subject
ORDER BY count DESC;

-- 4. 如果还有 subject 为空的错题，统一标记为"综合"
UPDATE quiz_answer a
JOIN quiz_session s ON a.session_id = s.id
SET a.subject = '综合'
WHERE (a.subject IS NULL OR a.subject = '')
  AND a.is_correct IN (0, -1, -2);
