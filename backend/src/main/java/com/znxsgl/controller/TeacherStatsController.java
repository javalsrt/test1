package com.znxsgl.controller;

import com.znxsgl.websocket.ScheduleWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherStatsController {

    private final JdbcTemplate jdbc;
    private final ScheduleWebSocketHandler wsHandler;

    public TeacherStatsController(JdbcTemplate jdbc, ScheduleWebSocketHandler wsHandler) {
        this.jdbc = jdbc;
        this.wsHandler = wsHandler;
    }

    /** 教师数据总览 */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication auth) {
        Long teacherUserId = (Long) auth.getPrincipal();
        Long realTeacherId = getRealTeacherId(teacherUserId);

        // 1. 教师所教课程的班级ID
        List<Long> classIds = jdbc.queryForList(
            "SELECT DISTINCT cc.class_id FROM course_class cc " +
            "JOIN course c ON c.id = cc.course_id " +
            "WHERE c.teacher_id = ?", Long.class, realTeacherId);

        if (classIds.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("totalStudents", 0);
            empty.put("onlineToday", 0);
            empty.put("avgFocusMinutes", 0);
            empty.put("students", Collections.emptyList());
            return ResponseEntity.ok(empty);
        }

        // 构建 IN 子句
        StringBuilder placeholders = new StringBuilder();
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < classIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
            params.add(classIds.get(i));
        }

        // 2. 总学生数
        int totalStudents = jdbc.queryForObject(
            "SELECT COUNT(*) FROM user WHERE class_id IN (" + placeholders + ") AND role = 1",
            Integer.class, params.toArray());

        // 3. 实时在线人数（基于 WebSocket 连接状态）
        Set<Long> onlineIds = wsHandler.getOnlineStudentIds();
        List<Long> studentIds = jdbc.queryForList(
            "SELECT id FROM user WHERE class_id IN (" + placeholders + ") AND role = 1",
            Long.class, params.toArray());
        int onlineToday = 0;
        for (Long sid : studentIds) {
            if (onlineIds.contains(sid)) onlineToday++;
        }

        // 4. 平均学习时长（分钟）
        Double avgSecObj = jdbc.queryForObject(
            "SELECT AVG(t.sec) FROM (" +
            "  SELECT SUM(f.duration_seconds) AS sec FROM focus_session f " +
            "  WHERE f.user_id IN (SELECT id FROM user WHERE class_id IN (" + placeholders + ") AND role = 1) " +
            "  AND DATE(f.finished_at) = CURDATE() " +
            "  GROUP BY f.user_id" +
            ") t", Double.class, params.toArray());
        int avgFocusMinutes = avgSecObj != null ? (int)(avgSecObj / 60) : 0;

        // 5. 活跃学生列表（按今日专注时长排序）
        List<Map<String, Object>> students = jdbc.queryForList(
            "SELECT u.id, u.real_name AS realName, u.student_no AS studentNo, " +
            "  ci.class_name AS className, " +
            "  COALESCE(SUM(f.duration_seconds), 0) AS todaySeconds, " +
            "  COALESCE(SUM(f2.duration_seconds), 0) AS totalSeconds " +
            "FROM user u " +
            "LEFT JOIN class_info ci ON ci.id = u.class_id " +
            "LEFT JOIN focus_session f ON f.user_id = u.id AND DATE(f.finished_at) = CURDATE() " +
            "LEFT JOIN focus_session f2 ON f2.user_id = u.id " +
            "WHERE u.class_id IN (" + placeholders + ") AND u.role = 1 " +
            "GROUP BY u.id, u.real_name, u.student_no, ci.class_name " +
            "ORDER BY todaySeconds DESC LIMIT 20",
            params.toArray());

        // 用 WebSocket 实时在线状态替换数据库 last_login 判断
        for (Map<String, Object> s : students) {
            Object idObj = s.get("id");
            Long uid = idObj instanceof Number ? ((Number) idObj).longValue() : null;
            s.put("online", uid != null && onlineIds.contains(uid) ? 1 : 0);
        }

        // 5. 刷题正确率
        double quizAccuracy = 0;
        try {
            Double acc = jdbc.queryForObject(
                "SELECT IFNULL(AVG(correct), 0) FROM (" +
                " SELECT CASE WHEN user_answer = correct_answer THEN 100 ELSE 0 END AS correct" +
                " FROM quiz_answer WHERE session_id IN (" +
                "  SELECT id FROM quiz_session WHERE user_id IN (SELECT id FROM user WHERE class_id IN (" + placeholders + ") AND role = 1)" +
                " )) t", Double.class, params.toArray());
            quizAccuracy = acc != null ? acc : 0;
        } catch(Exception ignored) {}

        // 6. 各科目正确率
        List<Map<String, Object>> subjectAccuracy = new ArrayList<>();
        try {
            subjectAccuracy = jdbc.queryForList(
                "SELECT COALESCE(qa.subject, '其他') AS name," +
                " ROUND(AVG(CASE WHEN qa.user_answer = qa.correct_answer THEN 100 ELSE 0 END), 1) AS accuracy," +
                " COUNT(*) AS total" +
                " FROM quiz_answer qa" +
                " JOIN quiz_session qs ON qs.id = qa.session_id" +
                " WHERE qs.user_id IN (SELECT id FROM user WHERE class_id IN (" + placeholders + ") AND role = 1)" +
                " GROUP BY qa.subject ORDER BY total DESC",
                params.toArray());
        } catch(Exception ignored) {}

        Map<String, Object> result = new HashMap<>();
        result.put("totalStudents", totalStudents);
        result.put("onlineToday", onlineToday);
        result.put("avgFocusMinutes", avgFocusMinutes);
        result.put("quizAccuracy", Math.round(quizAccuracy));
        result.put("subjectAccuracy", subjectAccuracy);
        result.put("students", students);
        return ResponseEntity.ok(result);
    }

    private Long getRealTeacherId(Long userId) {
        try {
            Map<String, Object> u = jdbc.queryForMap("SELECT real_name FROM user WHERE id = ?", userId);
            String realName = (String) u.get("real_name");
            return jdbc.queryForObject(
                "SELECT id FROM teacher WHERE real_name = ? LIMIT 1", Long.class, realName);
        } catch (Exception e) {
            return userId;
        }
    }

    /** 班级统计（学习统计页面） */
    @GetMapping("/class-stats/{classId}")
    public ResponseEntity<Map<String, Object>> classStats(@PathVariable Long classId, Authentication auth) {
        // 班级学生专注排行
        List<Map<String, Object>> focusRank = jdbc.queryForList(
            "SELECT u.id, u.real_name AS name, u.student_no AS studentNo, " +
            "  COALESCE(SUM(f.duration_seconds), 0) AS totalSeconds " +
            "FROM user u LEFT JOIN focus_session f ON f.user_id = u.id " +
            "WHERE u.class_id = ? AND u.role = 1 " +
            "GROUP BY u.id ORDER BY totalSeconds DESC", classId);

        // 该班级各课程提问量
        List<Map<String, Object>> courseQuestions = jdbc.queryForList(
            "SELECT cm.course_name AS name, COUNT(*) AS count " +
            "FROM chat_message cm JOIN user u ON u.id = cm.user_id " +
            "WHERE u.class_id = ? AND cm.sender_role = 'student' " +
            "GROUP BY cm.course_name ORDER BY count DESC", classId);

        // 班级学生总数
        int total = jdbc.queryForObject(
            "SELECT COUNT(*) FROM user WHERE class_id = ? AND role = 1", Integer.class, classId);

        Map<String, Object> result = new HashMap<>();
        result.put("focusRank", focusRank);
        result.put("courseQuestions", courseQuestions);
        result.put("totalStudents", total);
        return ResponseEntity.ok(result);
    }

    /** 学生个人统计 */
    @GetMapping("/student-stats/{studentId}")
    public ResponseEntity<Map<String, Object>> studentStats(@PathVariable Long studentId) {
        // 基本信息
        Map<String, Object> info = jdbc.queryForMap(
            "SELECT u.real_name AS name, u.student_no AS studentNo, ci.class_name AS className " +
            "FROM user u LEFT JOIN class_info ci ON ci.id = u.class_id WHERE u.id = ?", studentId);

        // 7天专注趋势
        List<Map<String, Object>> focusTrend = jdbc.queryForList(
            "SELECT DATE(finished_at) AS date, SUM(duration_seconds)/60 AS minutes " +
            "FROM focus_session WHERE user_id = ? AND finished_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(finished_at) ORDER BY date", studentId);

        // 总专注时长
        int totalFocus = jdbc.queryForObject(
            "SELECT COALESCE(SUM(duration_seconds),0) FROM focus_session WHERE user_id = ?",
            Integer.class, studentId);

        // 各课程提问次数
        List<Map<String, Object>> questions = jdbc.queryForList(
            "SELECT course_name AS name, COUNT(*) AS count FROM chat_message " +
            "WHERE user_id = ? AND sender_role = 'student' " +
            "GROUP BY course_name ORDER BY count DESC", studentId);

        // 本学期登录天数
        int loginDays = jdbc.queryForObject(
            "SELECT COUNT(DISTINCT DATE(last_login)) FROM user WHERE id = ? " +
            "AND last_login >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)", Integer.class, studentId);

        // 班级平均专注（用于对比）
        Double classAvgSec = jdbc.queryForObject(
            "SELECT AVG(t.sec) FROM (SELECT SUM(f.duration_seconds) AS sec FROM focus_session f " +
            "WHERE f.user_id IN (SELECT id FROM user WHERE class_id = " +
            "(SELECT class_id FROM user WHERE id = ?) AND role = 1) GROUP BY f.user_id) t",
            Double.class, studentId);

        // 最新六维评分
        List<Map<String, Object>> scores = new ArrayList<>();
        try {
            Map<String, Object> scoreRow = jdbc.queryForMap(
                "SELECT s.scores FROM quiz_session s WHERE s.user_id = ? AND s.status = 'evaluated' ORDER BY s.created_at DESC LIMIT 1",
                studentId);
            if (scoreRow != null && scoreRow.get("scores") != null) {
                String scoresJson = scoreRow.get("scores").toString();
                // scores是JSON Map格式 {"逻辑思维力":8, "判断决策力":7, ...}
                // 转化为列表
                scoresJson = scoresJson.replaceAll("[{}\"]", "");
                for (String pair : scoresJson.split(",")) {
                    String[] kv = pair.split(":");
                    if (kv.length == 2) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", kv[0].trim());
                        try { item.put("value", Integer.parseInt(kv[1].trim())); }
                        catch (NumberFormatException e) { item.put("value", 0); }
                        scores.add(item);
                    }
                }
            }
        } catch (Exception ignored) {}

        Map<String, Object> result = new HashMap<>();
        result.put("info", info);
        result.put("focusTrend", focusTrend);
        result.put("totalFocusMinutes", totalFocus / 60);
        result.put("questions", questions);
        result.put("loginDays", loginDays);
        result.put("classAvgMinutes", classAvgSec != null ? (int)(classAvgSec / 60) : 0);
        result.put("scores", scores);
        return ResponseEntity.ok(result);
    }

    /** 学习时长趋势（近7天） */
    @GetMapping("/trend")
    public ResponseEntity<List<Map<String, Object>>> getTrend(Authentication auth) {
        Long teacherUserId = (Long) auth.getPrincipal();
        Long realTeacherId = getRealTeacherId(teacherUserId);

        List<Long> userIds = jdbc.queryForList(
            "SELECT DISTINCT u.id FROM user u " +
            "JOIN course_class cc ON cc.class_id = u.class_id " +
            "JOIN course c ON c.id = cc.course_id " +
            "WHERE c.teacher_id = ? AND u.role = 1", Long.class, realTeacherId);

        if (userIds.isEmpty()) return ResponseEntity.ok(Collections.emptyList());

        StringBuilder ph = new StringBuilder();
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            if (i > 0) ph.append(",");
            ph.append("?");
            params.add(userIds.get(i));
        }

        List<Map<String, Object>> trend = jdbc.queryForList(
            "SELECT DATE(finished_at) AS date, " +
            "  COALESCE(SUM(duration_seconds)/60, 0) AS minutes " +
            "FROM focus_session " +
            "WHERE user_id IN (" + ph + ") AND finished_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(finished_at) ORDER BY date",
            params.toArray());

        return ResponseEntity.ok(trend);
    }
}
