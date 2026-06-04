package com.znxsgl.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final JdbcTemplate jdbc;

    public CheckInController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 教师创建签到 */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, Authentication auth) {
        String courseName = (String) body.get("courseName");
        String password = (String) body.get("password");
        Long userId = (Long) auth.getPrincipal();

        // 获取教师姓名
        String userName;
        try {
            Map<String, Object> user = jdbc.queryForMap("SELECT real_name FROM user WHERE id = ?", userId);
            userName = (String) user.get("real_name");
        } catch (Exception e) {
            userName = "教师";
        }

        // 关闭该课程之前的签到
        jdbc.update("UPDATE check_in SET active = 0 WHERE course_name = ? AND active = 1", courseName);

        // 创建新签到（15分钟有效）
        jdbc.update(
            "INSERT INTO check_in (course_name, created_by, creator_name, password, created_at, expires_at, active) " +
            "VALUES (?, ?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 15 MINUTE), 1)",
            courseName, userId, userName, password);

        Map<String, Object> result = new HashMap<>();
        result.put("msg", "签到已创建，有效期15分钟");
        result.put("password", password);
        return ResponseEntity.ok(result);
    }

    /** 学生验证签到密码 */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestBody Map<String, Object> body) {
        String courseName = (String) body.get("courseName");
        String password = (String) body.get("password");
        String studentName = (String) body.getOrDefault("studentName", "学生");
        Object studentIdObj = body.get("studentId");
        Long studentId = studentIdObj instanceof Number ? ((Number) studentIdObj).longValue() : 0L;

        // 查找有效签到
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT id FROM check_in WHERE course_name = ? AND password = ? AND active = 1 AND expires_at > NOW()",
            courseName, password);

        if (list.isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "签到码无效或已过期"));
        }

        Long checkInId = ((Number) list.get(0).get("id")).longValue();

        // 检查是否已签到
        int count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM check_in_record WHERE check_in_id = ? AND student_id = ?",
            Integer.class, checkInId, studentId);

        if (count > 0) {
            return ResponseEntity.ok(Map.of("error", "您已签到过了"));
        }

        // 记录签到
        jdbc.update(
            "INSERT INTO check_in_record (check_in_id, student_id, student_name, checked_at) VALUES (?, ?, ?, NOW())",
            checkInId, studentId, studentName);

        return ResponseEntity.ok(Map.of("msg", studentName + " 签到成功！", "success", true));
    }

    /** 获取签到状态 */
    @GetMapping("/status/{courseName}")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String courseName) {
        List<Map<String, Object>> active = jdbc.queryForList(
            "SELECT id, password, creator_name, created_at, expires_at FROM check_in " +
            "WHERE course_name = ? AND active = 1 AND expires_at > NOW() LIMIT 1",
            courseName);

        Map<String, Object> result = new HashMap<>();
        if (!active.isEmpty()) {
            Map<String, Object> ci = active.get(0);
            Long id = ((Number) ci.get("id")).longValue();
            int checkedCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM check_in_record WHERE check_in_id = ?", Integer.class, id);
            result.put("active", true);
            result.put("password", ci.get("password"));
            result.put("creatorName", ci.get("creator_name"));
            result.put("checkedCount", checkedCount);
        } else {
            result.put("active", false);
            result.put("checkedCount", 0);
        }
        return ResponseEntity.ok(result);
    }

    /** 教师关闭签到 */
    @PostMapping("/close")
    public ResponseEntity<Map<String, Object>> close(@RequestBody Map<String, Object> body) {
        String courseName = (String) body.get("courseName");
        jdbc.update("UPDATE check_in SET active = 0 WHERE course_name = ? AND active = 1", courseName);
        return ResponseEntity.ok(Map.of("msg", "签到已关闭"));
    }
}
