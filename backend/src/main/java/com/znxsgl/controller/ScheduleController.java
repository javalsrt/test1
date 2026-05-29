package com.znxsgl.controller;

import com.znxsgl.dto.StudentCourseDTO;
import com.znxsgl.dto.StudentScheduleDTO;
import com.znxsgl.dto.TeacherCourseDTO;
import com.znxsgl.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final JdbcTemplate jdbc;

    public ScheduleController(ScheduleService scheduleService, JdbcTemplate jdbc) {
        this.scheduleService = scheduleService;
        this.jdbc = jdbc;
    }

    // 教师：查看所教课程
    @GetMapping("/teacher/courses")
    public ResponseEntity<List<TeacherCourseDTO>> getTeacherCourses(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(scheduleService.getTeacherCourses(userId));
    }

    // 学生：查看我的课程列表（含上下架状态）
    @GetMapping("/student/courses")
    public ResponseEntity<List<StudentCourseDTO>> getStudentCourses(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(scheduleService.getStudentCourses(userId));
    }

    // 学生：查看我的课表（?week=13）
    @GetMapping("/student/my")
    public ResponseEntity<List<StudentScheduleDTO>> getMySchedule(
            Authentication auth,
            @RequestParam(defaultValue = "0") int week) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(scheduleService.getStudentSchedule(userId, week));
    }

    // 教师：按班级+周查看课表（用于排课时空位查看，week=0 表示查所有周）
    @GetMapping("/teacher/class-schedule")
    public ResponseEntity<Map<String, Object>> getClassSchedule(
            @RequestParam Long classId,
            @RequestParam int week,
            Authentication auth) {
        List<Map<String, Object>> schedules;
        if (week > 0) {
            schedules = jdbc.queryForList(
                "SELECT s.id, s.course_name, s.day_of_week, s.start_time, s.end_time, " +
                "s.start_node, s.step, s.classroom, s.weeks, s.course_id " +
                "FROM schedule s " +
                "JOIN user u ON u.id = s.user_id " +
                "WHERE u.class_id = ? AND s.status = 1 AND s.day_of_week > 0 AND JSON_CONTAINS(s.weeks, CAST(? AS JSON)) " +
                "ORDER BY s.day_of_week, s.start_time",
                classId, week);
        } else {
            schedules = jdbc.queryForList(
                "SELECT s.id, s.course_name, s.day_of_week, s.start_time, s.end_time, " +
                "s.start_node, s.step, s.classroom, s.weeks, s.course_id " +
                "FROM schedule s " +
                "JOIN user u ON u.id = s.user_id " +
                "WHERE u.class_id = ? AND s.status = 1 AND s.day_of_week > 0 " +
                "ORDER BY s.day_of_week, s.start_time",
                classId);
        }

        // 获取班级信息
        Map<String, Object> classInfo = jdbc.queryForMap(
            "SELECT id, class_name FROM class_info WHERE id = ?", classId);

        Map<String, Object> result = new HashMap<>();
        result.put("classId", classId);
        result.put("className", classInfo.get("class_name"));
        result.put("week", week);
        result.put("schedules", schedules);
        return ResponseEntity.ok(result);
    }

    // 教师：获取课程在指定班级的未上架 schedule 记录（用于查看课程原始时间段）
    @GetMapping("/teacher/course-schedule")
    public ResponseEntity<List<Map<String, Object>>> getCourseSchedule(
            @RequestParam String courseName,
            @RequestParam Long classId,
            Authentication auth) {
        List<Map<String, Object>> records = jdbc.queryForList(
            "SELECT DISTINCT s.day_of_week, s.start_time, s.end_time, s.start_node, s.step, " +
            "s.classroom, s.weeks, s.semester, s.course_name " +
            "FROM schedule s " +
            "JOIN user u ON u.id = s.user_id " +
            "WHERE s.course_name = ? AND u.class_id = ? AND s.status = 0",
            courseName, classId);
        return ResponseEntity.ok(records);
    }
}
