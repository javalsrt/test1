package com.znxsgl.controller;

import com.znxsgl.entity.Course;
import com.znxsgl.entity.Schedule;
import com.znxsgl.mapper.CourseMapper;
import com.znxsgl.mapper.ScheduleMapper;
import com.znxsgl.service.DashScopeService;
import com.znxsgl.websocket.ScheduleWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/schedule/import")
public class ScheduleImportController {

    private final DashScopeService ai;
    private final JdbcTemplate jdbc;
    private final ScheduleMapper scheduleMapper;
    private final CourseMapper courseMapper;
    private final ScheduleWebSocketHandler wsHandler;
    private final Path uploadDir;

    public ScheduleImportController(DashScopeService ai, JdbcTemplate jdbc,
                                     ScheduleMapper scheduleMapper,
                                     CourseMapper courseMapper,
                                     ScheduleWebSocketHandler wsHandler) {
        this.ai = ai;
        this.jdbc = jdbc;
        this.scheduleMapper = scheduleMapper;
        this.courseMapper = courseMapper;
        this.wsHandler = wsHandler;
        try {
            uploadDir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "znxsgl_schedule"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 第一步：上传文件 → AI 提取课表 → 校验 → 返回预览 */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> preview(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        try {
            String fileName = file.getOriginalFilename();
            Path saved = uploadDir.resolve(UUID.randomUUID() + "_" + fileName);
            file.transferTo(saved.toFile());

            // 读取文件内容
            String text;
            if (isImage(fileName)) {
                byte[] bytes = Files.readAllBytes(saved);
                String base64 = Base64.getEncoder().encodeToString(bytes);
                String mime = file.getContentType() != null ? file.getContentType() : "image/jpeg";
                text = ai.analyzeImage(buildExtractPrompt(), "data:" + mime + ";base64," + base64);
            } else {
                String lname = fileName.toLowerCase();
                if (lname.endsWith(".xlsx") || lname.endsWith(".xlsm")) {
                    text = extractXlsxText(saved);
                } else if (lname.endsWith(".docx")) {
                    text = extractDocxText(saved);
                } else if (lname.endsWith(".pdf")) {
                    text = extractPdfText(saved);
                } else if (lname.endsWith(".xls")) {
                    text = "（旧版 .xls 格式暂不支持，请另存为 .xlsx）";
                } else {
                    text = new String(Files.readAllBytes(saved));
                }

                if (text == null || text.trim().isEmpty()) {
                    text = "（文件为空或无法解析）";
                }

                // 解码 HTML/XML 数字实体 (&#xxxxx; → 对应字符)，减小 AI 输入体积
                text = decodeXmlEntities(text);

                System.out.println("=== 课表导入文件内容前200字: " + text.substring(0, Math.min(200, text.length())));
                String aiPrompt = "文件内容：\n" + (text.length() > 8000 ? text.substring(0, 8000) : text);
                text = ai.chat(buildExtractPrompt(), aiPrompt);
            }

            Files.deleteIfExists(saved);

            // 解析 AI 返回的 JSON
            List<Map<String, Object>> items = parseAIResponse(text);
            List<Map<String, Object>> errors = new ArrayList<>();
            List<Map<String, Object>> preview = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> item = items.get(i);
                Map<String, Object> result = validateItem(item, i + 1);
                if (result.containsKey("errors")) {
                    errors.add(result);
                } else {
                    preview.add(result);
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("total", items.size());
            resp.put("success", preview.size());
            resp.put("errors", errors);
            resp.put("preview", preview);
            resp.put("raw", text.substring(0, Math.min(300, text.length())) + "...");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("error", "处理失败：" + e.getMessage()));
        }
    }

    /** 第二步：确认导入（所有课程统一存入"已下架"，由教师手动上架时检测冲突） */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirm(
            @RequestBody List<Map<String, Object>> items,
            Authentication auth) {
        int imported = 0;
        int skipped = 0;
        List<String> messages = new ArrayList<>();

        Long userId = (Long) auth.getPrincipal();
        Long teacherId = resolveTeacherId(userId);
        if (teacherId == null) {
            return ResponseEntity.ok(Map.of("error", "未找到教师身份，请确认当前账户为教师账户"));
        }

        for (Map<String, Object> item : items) {
            String courseName = (String) item.get("courseName");
            int dayOfWeek = ((Number) item.get("dayOfWeek")).intValue();
            String startTime = (String) item.get("startTime");
            String endTime = (String) item.get("endTime");
            int startNode = ((Number) item.get("startNode")).intValue();
            int step = ((Number) item.get("step")).intValue();
            String classroom = (String) item.getOrDefault("classroom", "");
            String semester = (String) item.getOrDefault("semester", "2025-2026-2");
            String weeksJson = (String) item.getOrDefault("weeks", "[]");
            String className = (String) item.get("className");
            // 课时数（AI可能返回 credit 或 totalHours）
            Object creditObj = item.getOrDefault("credit", item.getOrDefault("totalHours", null));
            int credit = creditObj != null ? ((Number) creditObj).intValue() : 2; // 默认2课时

            // 确保 course 表中存在此课程（含课时）
            Long courseId = ensureCourse(courseName, teacherId, semester, credit);

            // 查找班级ID
            Long classId = null;
            if (className != null && !className.isEmpty()) {
                classId = matchClass(className);
            }

            if (classId == null) {
                skipped++;
                messages.add(courseName + " 未匹配到班级「" + (className != null ? className : "未知") + "」，已跳过");
                continue;
            }

            // 确保 course_class 关联
            ensureCourseClass(courseId, classId, semester);

            // 查找班级学生
            List<Long> studentIds = jdbc.queryForList(
                "SELECT id FROM user WHERE class_id = ? AND role = 1", Long.class, classId);
            if (studentIds.isEmpty()) {
                skipped++;
                messages.add(courseName + "(" + className + ") 班级中无学生用户，已跳过");
                continue;
            }

            // 所有课程统一以 status=0（已下架）存入，等待教师手动上架
            for (Long sid : studentIds) {
                Schedule s = new Schedule();
                s.setUserId(sid);
                s.setCourseId(courseId);
                s.setCourseName(courseName);
                s.setDayOfWeek(dayOfWeek);
                s.setStartTime(LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm")));
                s.setEndTime(LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm")));
                s.setStartNode(startNode);
                s.setStep(step);
                s.setClassroom(classroom);
                s.setSemester(semester);
                s.setWeeks(weeksJson);
                s.setStatus(0); // 统一已下架
                scheduleMapper.insert(s);
            }

            imported++;
            messages.add(courseName + "(" + className + " 周" + dayOfWeek + " " + startTime + "-" + endTime + ") 已存入「已下架课程」");
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("imported", imported);
        resp.put("skipped", skipped);
        resp.put("messages", messages);
        return ResponseEntity.ok(resp);
    }

    /** 通过 userId 反查 teacher 表的 id */
    private Long resolveTeacherId(Long userId) {
        List<Long> ids = jdbc.queryForList(
            "SELECT t.id FROM teacher t " +
            "JOIN user u ON u.real_name = t.real_name " +
            "WHERE u.id = ? LIMIT 1", Long.class, userId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    /** 确保 course 表中存在该课程，不存在则创建（含课时） */
    private Long ensureCourse(String courseName, Long teacherId, String semester, int credit) {
        // 先查是否存在同教师同课程名
        List<Long> ids = jdbc.queryForList(
            "SELECT id FROM course WHERE course_name = ? AND teacher_id = ? LIMIT 1",
            Long.class, courseName, teacherId);
        if (!ids.isEmpty()) {
            // 更新课时（如果已有记录的 credit 为 null 或 0）
            jdbc.update("UPDATE course SET credit = ? WHERE id = ? AND (credit IS NULL OR credit = 0)",
                new java.math.BigDecimal(credit), ids.get(0));
            return ids.get(0);
        }

        // 不存在则创建
        Course course = new Course();
        course.setCourseName(courseName);
        course.setTeacherId(teacherId);
        course.setSemester(semester);
        course.setCredit(new java.math.BigDecimal(credit));
        courseMapper.insert(course);
        return course.getId();
    }

    /** 确保 course_class 关联表存在记录 */
    private void ensureCourseClass(Long courseId, Long classId, String semester) {
        int count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM course_class WHERE course_id = ? AND class_id = ?",
            Integer.class, courseId, classId);
        if (count == 0) {
            jdbc.update("INSERT INTO course_class (course_id, class_id, semester) VALUES (?, ?, ?)",
                courseId, classId, semester != null ? semester : "2025-2026-2");
        }
    }

    /** 增强班级名称匹配：支持简写如"计算机2301班"匹配"计算机科学与技术2023级1班" */
    private Long matchClass(String className) {
        // 策略1: 直接 LIKE 模糊匹配
        List<Long> ids = jdbc.queryForList(
            "SELECT id FROM class_info WHERE class_name LIKE CONCAT('%',?,'%') LIMIT 1",
            Long.class, className);
        if (!ids.isEmpty()) return ids.get(0);

        // 策略2: 提取中文前缀 + 数字部分，在内存中做匹配
        // "计算机2301班" → 中文前缀="计算机", 数字部分="2301"
        String chinesePrefix = "";
        StringBuilder allDigits = new StringBuilder();
        for (char c : className.toCharArray()) {
            if (Character.isDigit(c)) {
                allDigits.append(c);
            } else if (allDigits.length() == 0 && c != '班' && c != '级') {
                chinesePrefix += c;
            }
        }
        String numStr = allDigits.toString();
        System.out.println("=== 班级匹配: 输入=" + className + ", 中文前缀=" + chinesePrefix + ", 数字=" + numStr);

        // 拉取所有班级，在 Java 内存中匹配
        List<Map<String, Object>> allClasses = jdbc.queryForList(
            "SELECT id, class_name FROM class_info");
        for (Map<String, Object> row : allClasses) {
            Long cid = ((Number) row.get("id")).longValue();
            String dbName = (String) row.get("class_name");

            // 检查数据库班级名是否包含中文前缀（支持别名映射）
            if (matchesClassPrefix(chinesePrefix, dbName)) {
                // 提取数据库班级名中的数字
                StringBuilder dbDigits = new StringBuilder();
                for (char c : dbName.toCharArray()) {
                    if (Character.isDigit(c)) dbDigits.append(c);
                }
                String dbNum = dbDigits.toString();

                if (numStr.length() >= 4 && dbNum.length() >= 6) {
                    // "2301" vs "202301"
                    // 比较：输入年级(23) 对应 数据库年级(2023 的后两位), 输入班号(01) 对应 数据库班号
                    String inputGrade = numStr.substring(0, 2);   // "23"
                    String inputClass = numStr.substring(2);       // "01"
                    String dbGrade = dbNum.substring(0, 4);        // "2023"
                    String dbClass = dbNum.substring(4);           // "01"

                    if (dbGrade.endsWith(inputGrade) && dbClass.equals(inputClass)) {
                        System.out.println("=== 班级匹配成功: " + className + " → " + dbName + " (id=" + cid + ")");
                        return cid;
                    }
                    // 去掉前导0再比较
                    String inputClassNoPad = inputClass.replaceFirst("^0+", "");
                    String dbClassNoPad = dbClass.replaceFirst("^0+", "");
                    if (dbGrade.endsWith(inputGrade) && dbClassNoPad.equals(inputClassNoPad)) {
                        System.out.println("=== 班级匹配成功(去0): " + className + " → " + dbName + " (id=" + cid + ")");
                        return cid;
                    }
                } else if (numStr.length() >= 2 && dbNum.length() >= 4) {
                    // 只匹配年级
                    String inputGrade = numStr.substring(0, 2);
                    String dbGrade = dbNum.substring(0, 4);
                    if (dbGrade.endsWith(inputGrade)) {
                        System.out.println("=== 班级匹配成功(仅年级): " + className + " → " + dbName + " (id=" + cid + ")");
                        return cid;
                    }
                }
            }
        }

        System.out.println("=== 班级匹配失败: " + className);
        return null;
    }

    /** 中文前缀匹配，支持别名（如"计教"→"教育技术学"、"计科"→"计算机科学与技术"） */
    private boolean matchesClassPrefix(String inputPrefix, String dbClassName) {
        if (inputPrefix.isEmpty()) return false;
        // 直接包含
        if (dbClassName.contains(inputPrefix)) return true;
        // 别名映射
        if (inputPrefix.contains("计教") && dbClassName.contains("教育技术学")) return true;
        if (inputPrefix.contains("计科") && dbClassName.contains("计算机科学与技术")) return true;
        if (inputPrefix.contains("计算机") && dbClassName.contains("计算机")) return true;
        if (inputPrefix.contains("教育") && dbClassName.contains("教育")) return true;
        return false;
    }

    // ===== 下架/排课 =====

    @PostMapping("/hide")
    public ResponseEntity<Map<String, String>> hide(@RequestBody Map<String, Object> body) {
        String courseName = (String) body.get("courseName");
        jdbc.update("UPDATE schedule SET status = 0 WHERE course_name = ?", courseName);
        return ResponseEntity.ok(Map.of("msg", courseName + " 已下架"));
    }

    /** 清除指定课程在指定班级的排课（不选空位 = 不排课） */
    @PostMapping("/clear-class-schedule")
    public ResponseEntity<Map<String, String>> clearClassSchedule(@RequestBody Map<String, Object> body) {
        String courseName = (String) body.get("courseName");
        Object classIdObj = body.get("classId");
        Long classId = classIdObj != null ? ((Number) classIdObj).longValue() : null;
        if (classId == null) {
            return ResponseEntity.ok(Map.of("error", "请指定班级"));
        }
        jdbc.update("DELETE s FROM schedule s JOIN user u ON u.id = s.user_id " +
            "WHERE s.course_name = ? AND u.class_id = ?", courseName, classId);
        return ResponseEntity.ok(Map.of("msg", "已清除 " + courseName + " 在该班级的排课"));
    }

    /** 上架课程：教师已选好时间段，按指定班级上架 */
    @SuppressWarnings("unchecked")
    @PostMapping("/unhide")
    public ResponseEntity<Map<String, String>> unhide(@RequestBody Map<String, Object> body, Authentication auth) {
        String courseName = (String) body.get("courseName");
        Long userId = (Long) auth.getPrincipal();
        List<Map<String, Object>> selectedSlots = (List<Map<String, Object>>) body.get("slots");
        // 指定班级ID（可选，不传则上架所有关联班级）
        Object classIdObj = body.get("classId");
        Long targetClassId = classIdObj != null ? ((Number) classIdObj).longValue() : null;

        if (selectedSlots != null && !selectedSlots.isEmpty()) {
            Long teacherId = resolveTeacherId(userId);
            if (teacherId == null) {
                return ResponseEntity.ok(Map.of("error", "未找到教师身份"));
            }

            // 查找课程ID和课时
            List<Map<String, Object>> courseRows = jdbc.queryForList(
                "SELECT id, credit FROM course WHERE course_name = ? AND teacher_id = ?",
                courseName, teacherId);
            if (courseRows.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "未找到课程"));
            }
            Long courseId = ((Number) courseRows.get(0).get("id")).longValue();
            int totalCredit = courseRows.get(0).get("credit") != null
                ? ((Number) courseRows.get(0).get("credit")).intValue() : 2;

            // 验证课时
            int totalSelectedCredit = 0;
            for (Map<String, Object> slot : selectedSlots) {
                Object creditObj = slot.get("credit");
                totalSelectedCredit += (creditObj != null) ? ((Number) creditObj).intValue() : 2;
            }
            if (totalSelectedCredit > totalCredit) {
                return ResponseEntity.ok(Map.of("error",
                    "课时超限！该课程只有 " + totalCredit + " 课时，您选择了 " + totalSelectedCredit + " 课时"));
            }

            // 查找关联班级
            List<Map<String, Object>> classList;
            if (targetClassId != null) {
                classList = jdbc.queryForList(
                    "SELECT cc.class_id, ci.class_name FROM course_class cc " +
                    "JOIN class_info ci ON ci.id = cc.class_id " +
                    "WHERE cc.course_id = ? AND cc.class_id = ?", courseId, targetClassId);
            } else {
                classList = jdbc.queryForList(
                    "SELECT cc.class_id, ci.class_name FROM course_class cc " +
                    "JOIN class_info ci ON ci.id = cc.class_id " +
                    "WHERE cc.course_id = ?", courseId);
            }
            if (classList.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "课程未关联指定班级"));
            }

            // 对每个班级创建 schedule
            int totalCreated = 0;
            for (Map<String, Object> cl : classList) {
                Long classId = ((Number) cl.get("class_id")).longValue();
                String className = (String) cl.get("class_name");

                // 删除该班级该课程的所有旧记录（包括 status=0 下架记录和 status=1 占位记录）
                jdbc.update("DELETE s FROM schedule s JOIN user u ON u.id = s.user_id " +
                    "WHERE s.course_name = ? AND u.class_id = ?", courseName, classId);

                List<Long> studentIds = jdbc.queryForList(
                    "SELECT id FROM user WHERE class_id = ? AND role = 1", Long.class, classId);

                for (Map<String, Object> slot : selectedSlots) {
                    int dayOfWeek = ((Number) slot.get("dayOfWeek")).intValue();
                    String startTime = (String) slot.get("startTime");
                    String endTime = (String) slot.get("endTime");
                    int startNode = ((Number) slot.get("startNode")).intValue();
                    int step = ((Number) slot.get("step")).intValue();
                    String classroom = (String) slot.getOrDefault("classroom", "");
                    String semester = (String) slot.getOrDefault("semester", "2025-2026-2");
                    String weeksJson = (String) slot.getOrDefault("weeks", "[]");

                    // 检测冲突
                    if (checkConflictDetailed(classId, dayOfWeek, startTime, endTime, weeksJson)) {
                        return ResponseEntity.ok(Map.of("error",
                            "排课冲突！" + className + " 周" + dayOfWeek + " " + startTime + "-" + endTime + " 与已有课程冲突"));
                    }

                    for (Long sid : studentIds) {
                        jdbc.update(
                            "INSERT INTO schedule (user_id, course_id, course_name, day_of_week, " +
                            "start_time, end_time, start_node, step, classroom, semester, weeks, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)",
                            sid, courseId, courseName, dayOfWeek, startTime, endTime,
                            startNode, step, classroom, semester, weeksJson);
                        totalCreated++;
                    }
                }
            }
            // 排课成功后，给所有相关班级学生发送通知
            for (Map<String, Object> cl : classList) {
                String className = (String) cl.get("class_name");
                sendScheduleNotify(courseName, className, teacherId, selectedSlots);
            }

            return ResponseEntity.ok(Map.of("msg", courseName + " 已上架（共 " + totalCreated + " 条课表记录）"));
        }

        // 没有传 slots：直接上架不排课
        // 先删除所有旧的 status=0 记录（导入时创建的时间段记录），然后创建占位记录标记上架
        jdbc.update("DELETE s FROM schedule s JOIN user u ON u.id = s.user_id " +
            "WHERE s.course_name = ? AND s.status = 0", courseName);

        // 为每个关联班级创建占位 schedule（day_of_week=0），标记课程已上架但不排课
        Long teacherId = resolveTeacherId(userId);
        if (teacherId != null) {
            List<Map<String, Object>> courseRows = jdbc.queryForList(
                "SELECT id FROM course WHERE course_name = ? AND teacher_id = ?",
                courseName, teacherId);
            if (!courseRows.isEmpty()) {
                Long courseId = ((Number) courseRows.get(0).get("id")).longValue();
                // 查找关联班级
                List<Map<String, Object>> classList;
                if (targetClassId != null) {
                    classList = jdbc.queryForList(
                        "SELECT cc.class_id FROM course_class cc WHERE cc.course_id = ? AND cc.class_id = ?",
                        courseId, targetClassId);
                } else {
                    classList = jdbc.queryForList(
                        "SELECT cc.class_id FROM course_class cc WHERE cc.course_id = ?", courseId);
                }
                int totalCreated = 0;
                for (Map<String, Object> cl : classList) {
                    Long classId = ((Number) cl.get("class_id")).longValue();
                    List<Long> studentIds = jdbc.queryForList(
                        "SELECT id FROM user WHERE class_id = ? AND role = 1", Long.class, classId);
                    for (Long sid : studentIds) {
                        jdbc.update(
                            "INSERT INTO schedule (user_id, course_id, course_name, day_of_week, " +
                            "start_time, end_time, start_node, step, classroom, semester, weeks, status) " +
                            "VALUES (?, ?, ?, 0, '', '', 0, 0, '', ?, '[]', 1)",
                            sid, courseId, courseName, "2025-2026-2");
                        totalCreated++;
                    }
                }
                return ResponseEntity.ok(Map.of("msg", courseName + " 已上架（未排课，请在在线课程中手动排课）"));
            }
        }
        return ResponseEntity.ok(Map.of("msg", courseName + " 已上架"));
    }

    /** 详细冲突检测：检查目标班级学生在指定时间段+周次范围内是否已有在线课程 */
    private boolean checkConflictDetailed(Long classId, int dayOfWeek, String startTime, String endTime, String weeksJson) {
        // 解析周次数组
        List<Integer> weekList = new ArrayList<>();
        if (weeksJson != null && weeksJson.startsWith("[")) {
            String nums = weeksJson.replaceAll("[\\[\\]\\s]", "");
            for (String n : nums.split(",")) {
                try { weekList.add(Integer.parseInt(n.trim())); } catch (NumberFormatException ignored) {}
            }
        }

        // 查询同班级学生在同一天同一时间段内的在线课程（status=1）
        String sql = "SELECT s.id, s.weeks, s.start_time, s.end_time, s.course_name " +
            "FROM schedule s " +
            "JOIN user u ON u.id = s.user_id " +
            "WHERE u.class_id = ? AND s.day_of_week = ? AND s.status = 1 " +
            "AND ((s.start_time < ? AND s.end_time > ?))";
        List<Map<String, Object>> existing = jdbc.queryForList(
            sql, classId, dayOfWeek, endTime, startTime);

        for (Map<String, Object> existingRow : existing) {
            String existingWeeks = (String) existingRow.get("weeks");
            // 解析已有课程的周次
            List<Integer> existingWeekList = new ArrayList<>();
            if (existingWeeks != null && existingWeeks.startsWith("[")) {
                String nums = existingWeeks.replaceAll("[\\[\\]\\s]", "");
                for (String n : nums.split(",")) {
                    try { existingWeekList.add(Integer.parseInt(n.trim())); } catch (NumberFormatException ignored) {}
                }
            }
            // 检查周次是否有重叠
            for (int w : weekList) {
                if (existingWeekList.contains(w)) {
                    return true; // 存在冲突
                }
            }
        }
        return false;
    }

    @GetMapping("/hidden")
    public ResponseEntity<List<Map<String, Object>>> hidden(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(jdbc.queryForList(
            "SELECT DISTINCT s.course_name AS courseName, s.semester, s.day_of_week, " +
            "s.start_time, s.end_time, s.classroom " +
            "FROM schedule s " +
            "JOIN course c ON c.course_name = s.course_name " +
            "JOIN teacher t ON t.id = c.teacher_id " +
            "JOIN user u ON u.real_name = t.real_name " +
            "WHERE s.status = 0 AND u.id = ? " +
            "ORDER BY s.course_name",
            userId));
    }

    /** 移除课程：从数据库彻底删除该课程相关的 schedule、course_class、course 记录 */
    @PostMapping("/remove")
    public ResponseEntity<Map<String, String>> remove(@RequestBody Map<String, Object> body, Authentication auth) {
        String courseName = (String) body.get("courseName");
        Long userId = (Long) auth.getPrincipal();
        Long teacherId = resolveTeacherId(userId);
        if (teacherId == null) {
            return ResponseEntity.ok(Map.of("error", "未找到教师身份"));
        }

        // 1. 根据课程名和教师ID查找 course 记录
        List<Long> courseIds = jdbc.queryForList(
            "SELECT id FROM course WHERE course_name = ? AND teacher_id = ?",
            Long.class, courseName, teacherId);

        if (courseIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "未找到该课程"));
        }

        Long courseId = courseIds.get(0);

        // 2. 删除 schedule 表中该课程的所有记录
        jdbc.update("DELETE FROM schedule WHERE course_name = ?", courseName);

        // 3. 删除 course_class 关联
        jdbc.update("DELETE FROM course_class WHERE course_id = ?", courseId);

        // 4. 删除 course 表记录
        jdbc.update("DELETE FROM course WHERE id = ?", courseId);

        return ResponseEntity.ok(Map.of("msg", courseName + " 已移除"));
    }

    // ===== 内部方法 =====

    /** 解码 HTML/XML 数字字符实体（&#xxxxx; → Unicode 字符），减小 AI 输入体积 */
    private String decodeXmlEntities(String text) {
        if (text == null || !text.contains("&#")) return text;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '&' && i + 2 < text.length() && text.charAt(i + 1) == '#') {
                int end = text.indexOf(';', i);
                if (end > i + 2) {
                    try {
                        String digits = text.substring(i + 2, end);
                        int codePoint;
                        if (digits.startsWith("x") || digits.startsWith("X")) {
                            codePoint = Integer.parseInt(digits.substring(1), 16);
                        } else {
                            codePoint = Integer.parseInt(digits);
                        }
                        sb.appendCodePoint(codePoint);
                        i = end + 1;
                        continue;
                    } catch (NumberFormatException ignored) {}
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private Map<String, Object> validateItem(Map<String, Object> item, int row) {
        List<String> msgs = new ArrayList<>();
        String courseName = (String) item.get("courseName");
        String dayOfWeek = String.valueOf(item.getOrDefault("dayOfWeek", ""));
        String startTime = (String) item.get("startTime");
        String endTime = (String) item.get("endTime");
        String startNode = String.valueOf(item.getOrDefault("startNode", ""));
        String step = String.valueOf(item.getOrDefault("step", ""));
        String classroom = (String) item.getOrDefault("classroom", "");
        String weeks = String.valueOf(item.getOrDefault("weeks", ""));
        String className = (String) item.getOrDefault("className", "");

        if (courseName == null || courseName.trim().isEmpty()) msgs.add("缺少课程名称");
        if (className == null || className.trim().isEmpty()) msgs.add("缺少对应班级");
        try { Integer.parseInt(dayOfWeek.replaceAll("[^0-9]", "")); } catch (Exception e) { msgs.add("缺少或格式错误：星期几"); }
        try { LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm")); } catch (Exception e) { msgs.add("缺少或格式错误：开始时间"); }
        try { LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm")); } catch (Exception e) { msgs.add("缺少或格式错误：结束时间"); }
        try { Integer.parseInt(startNode.replaceAll("[^0-9]", "")); } catch (Exception e) { msgs.add("缺少或格式错误：开始节次"); }
        try { Integer.parseInt(step.replaceAll("[^0-9]", "")); } catch (Exception e) { msgs.add("缺少或格式错误：课时数"); }
        if (weeks.isEmpty() || weeks.equals("[]")) msgs.add("缺少周次信息");
        if (classroom == null || classroom.trim().isEmpty()) msgs.add("缺少教室信息");

        if (!msgs.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("row", row);
            err.put("courseName", courseName != null ? courseName : "(未知)");
            err.put("errors", msgs);
            return err;
        }

        // 格式化后的数据
        Map<String, Object> result = new HashMap<>();
        result.put("courseName", courseName);
        result.put("className", className);
        result.put("dayOfWeek", Integer.parseInt(dayOfWeek.replaceAll("[^0-9]", "")));
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("startNode", Integer.parseInt(startNode.replaceAll("[^0-9]", "")));
        result.put("step", Integer.parseInt(step.replaceAll("[^0-9]", "")));
        result.put("classroom", classroom);
        result.put("weeks", weeks.startsWith("[") ? weeks : "[" + weeks + "]");
        result.put("semester", item.getOrDefault("semester", "2025-2026-2"));
        return result;
    }

    /** 解析 AI 返回的 JSON 数组（处理截断情况） */
    private List<Map<String, Object>> parseAIResponse(String text) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return items;
        try {
            // 尝试找到 JSON 数组
            int start = text.indexOf("[");
            if (start < 0) return items;

            // 找最后一个完整的 JSON 对象（} 结尾）
            int pos = start;
            while (pos < text.length()) {
                int objStart = text.indexOf("{", pos);
                if (objStart < 0) break;
                // 找匹配的 }
                int depth = 0;
                int objEnd = objStart;
                for (int i = objStart; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (c == '{') depth++;
                    else if (c == '}') { depth--; if (depth == 0) { objEnd = i; break; } }
                }
                if (depth == 0 && objEnd > objStart) {
                    try {
                        String json = text.substring(objStart, objEnd + 1);
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        Map m = mapper.readValue(json, Map.class);
                        items.add(new HashMap<>(m));
                    } catch (Exception e) {
                        System.out.println("=== 单条JSON解析跳过: " + e.getMessage().substring(0, Math.min(50, e.getMessage().length())));
                    }
                    pos = objEnd + 1;
                } else {
                    pos = objStart + 1;
                }
            }
        } catch (Exception e) {
            System.out.println("=== JSON解析失败: " + e.getMessage());
        }
        return items;
    }

    private String buildExtractPrompt() {
        return "你是一个课表数据提取助手。请从文件中提取所有课程安排，返回纯JSON数组（不要markdown标记）。\n\n" +
            "每条记录必须包含以下字段（缺一不可）：\n" +
            "- courseName: 课程名称\n" +
            "- className: 班级名称\n" +
            "- dayOfWeek: 星期几（数字，1=周一...7=周日）\n" +
            "- startTime: 开始时间（HH:mm格式）\n" +
            "- endTime: 结束时间（HH:mm格式）\n" +
            "- startNode: 开始节次（数字）\n" +
            "- step: 课时数（数字，本时间段持续几个小节）\n" +
            "- credit: 该课程的总课时/学分（数字，如8表示8课时，4表示4课时）\n" +
            "- classroom: 教室\n" +
            "- weeks: 周次（JSON数组如[1,2,3]）\n" +
            "- semester: 学期（如2025-2026-2）\n\n" +
            "注意：credit是该课程整个学期的总课时，step是当前时间段的持续节数。\n" +
            "如果文件中没有总课时信息，请根据课程名称推测（如程序设计类通常4-8课时）。\n\n" +
            "格式示例：\n" +
            "[{\"courseName\":\"高等数学\",\"className\":\"计算机2301班\",\"dayOfWeek\":1,\"startTime\":\"08:30\",\"endTime\":\"09:55\",\"startNode\":1,\"step\":2,\"credit\":6,\"classroom\":\"A301\",\"weeks\":[1,2,3],\"semester\":\"2025-2026-2\"}]";
    }

    private boolean isImage(String name) {
        if (name == null) return false;
        String l = name.toLowerCase();
        return l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png") || l.endsWith(".bmp");
    }

    /** XLSX 解析：从 ZIP 中提取 sheet 数据为纯文本表格 */
    private String extractXlsxText(Path file) throws Exception {
        List<String> sharedStrings = new ArrayList<>();
        StringBuilder result = new StringBuilder();

        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(file.toFile()))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 读取共享字符串表
                if (entry.getName().equals("xl/sharedStrings.xml")) {
                    String xml = new String(zis.readAllBytes());
                    for (String part : xml.split("<t[^>]*>")) {
                        int end = part.indexOf("</t>");
                        if (end >= 0) sharedStrings.add(part.substring(0, end));
                    }
                }
                // 读取第一个 sheet
                if (entry.getName().equals("xl/worksheets/sheet1.xml")) {
                    String xml = new String(zis.readAllBytes());
                    String[] rows = xml.split("<row[^>]*>");
                    for (String row : rows) {
                        if (!row.contains("<c ")) continue;
                        // 提取引用类型为 s（共享字符串）的单元格值
                        String[] cells = row.split("<c ");
                        StringBuilder rowText = new StringBuilder();
                        for (String cell : cells) {
                            if (cell.contains("t=\"s\"") && cell.contains("<v>")) {
                                int vi = cell.indexOf("<v>") + 3;
                                int ve = cell.indexOf("</v>", vi);
                                if (vi >= 3 && ve > vi) {
                                    try {
                                        int idx = Integer.parseInt(cell.substring(vi, ve).trim());
                                        if (idx < sharedStrings.size()) {
                                            if (rowText.length() > 0) rowText.append(" | ");
                                            rowText.append(sharedStrings.get(idx));
                                        }
                                    } catch (NumberFormatException ignored) {}
                                }
                            }
                            // 提取内联字符串（t="inlineStr" 或 t="str"）
                            if ((cell.contains("t=\"inlineStr\"") || cell.contains("t=\"str\"")) && cell.contains("<t>")) {
                                int ti = cell.indexOf("<t>") + 3;
                                int te = cell.indexOf("</t>", ti);
                                if (ti >= 3 && te > ti) {
                                    if (rowText.length() > 0) rowText.append(" | ");
                                    rowText.append(cell.substring(ti, te));
                                }
                            }
                        }
                        if (rowText.length() > 0) result.append(rowText).append("\n");
                    }
                }
            }
        }
        return result.toString().trim();
    }

    private String extractDocxText(Path file) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(file.toFile()))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("word/document.xml")) {
                    String xml = new String(zis.readAllBytes());
                    for (String part : xml.split("<w:t[^>]*>")) {
                        int ei = part.indexOf("</w:t>");
                        if (ei >= 0) sb.append(part, 0, ei);
                    }
                }
            }
        }
        return sb.toString().trim();
    }

    private String extractPdfText(Path file) throws Exception {
        byte[] bytes = Files.readAllBytes(file);
        String content = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        while ((pos = content.indexOf("BT", pos)) >= 0) {
            int end = content.indexOf("ET", pos);
            if (end < 0) break;
            String block = content.substring(pos + 2, end);
            int tjPos = 0;
            while ((tjPos = block.indexOf("Tj", tjPos)) >= 0) {
                int s = block.lastIndexOf("(", tjPos);
                int e = block.indexOf(")", tjPos);
                if (s >= 0 && e > s) sb.append(block, s + 1, e);
                tjPos += 2;
            }
            pos = end + 2;
        }
        return sb.toString().trim();
    }

    /** 排课/调课后，给班级学生发送课程变动通知 */
    private void sendScheduleNotify(String courseName, String className, Long teacherId,
                                     List<Map<String, Object>> slots) {
        try {
            // 获取教师姓名
            String teacherName = jdbc.queryForObject(
                "SELECT real_name FROM teacher WHERE id = ?", String.class, teacherId);
            if (teacherName == null) teacherName = "教师";

            // 生成排课摘要（包含周数 + 星期 + 节次）
            String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            List<String> summaries = new ArrayList<>();
            for (Map<String, Object> slot : slots) {
                int dow = ((Number) slot.get("dayOfWeek")).intValue();
                int startNode = ((Number) slot.get("startNode")).intValue();
                int step = ((Number) slot.get("step")).intValue();
                String weeksJson = (String) slot.getOrDefault("weeks", "[]");

                // 解析 weeks JSON 数组为周数列表
                List<Integer> weekNumbers = parseWeekNumbers(weeksJson);
                String weekStr = weekNumbers.isEmpty() ? "" : formatWeekRange(weekNumbers) + " · ";

                // 节次描述
                int endNode = startNode + step - 1;
                String nodeDesc = startNode + "-" + endNode + "节";

                summaries.add(weekStr + dayNames[dow] + " " + nodeDesc);
            }
            String scheduleSummary = String.join("\n", summaries);
            String toastContent = "📋 " + courseName + " 排课更新\n" + String.join("\n", summaries);

            // 给该班级所有学生插入通知消息（数据库存简洁版）
            String dbContent = "课程「" + courseName + "」排课已更新\n" + String.join("\n", summaries);
            List<Long> studentIds = jdbc.queryForList(
                "SELECT u.id FROM user u JOIN class_info ci ON ci.id = u.class_id " +
                "WHERE ci.class_name = ? AND u.role = 1", Long.class, className);
            for (Long sid : studentIds) {
                jdbc.update(
                    "INSERT INTO chat_message (course_name, user_id, sender_name, sender_role, content, created_at) " +
                    "VALUES (?, ?, ?, 'teacher', ?, NOW())",
                    courseName, sid, teacherName, dbContent);
                // WebSocket 实时推送
                Map<String, Object> wsData = new LinkedHashMap<>();
                wsData.put("courseName", courseName);
                wsData.put("content", toastContent);
                wsData.put("scheduleInfo", scheduleSummary);
                wsHandler.sendToUser(sid, "schedule_update", wsData);
            }
        } catch (Exception e) {
            System.out.println("=== 发送排课通知失败: " + e.getMessage());
        }
    }

    /** 解析 weeks JSON 数组（如 "[1,2,3]"）为周数列表 */
    private List<Integer> parseWeekNumbers(String weeksJson) {
        List<Integer> list = new ArrayList<>();
        if (weeksJson == null || weeksJson.isEmpty() || "[]".equals(weeksJson)) return list;
        String stripped = weeksJson.replaceAll("[\\[\\]\\s]", "");
        if (stripped.isEmpty()) return list;
        for (String s : stripped.split(",")) {
            try { list.add(Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) {}
        }
        return list;
    }

    /** 将周数列表格式化为简洁范围，如 [1,2,3,5,6] → "第1-3,5-6周" */
    private String formatWeekRange(List<Integer> weeks) {
        if (weeks.isEmpty()) return "";
        Collections.sort(weeks);
        StringBuilder sb = new StringBuilder("第");
        int start = weeks.get(0);
        int end = start;
        for (int i = 1; i < weeks.size(); i++) {
            int w = weeks.get(i);
            if (w == end + 1) {
                end = w;
            } else {
                if (start == end) sb.append(start);
                else sb.append(start).append("-").append(end);
                sb.append(",");
                start = end = w;
            }
        }
        if (start == end) sb.append(start);
        else sb.append(start).append("-").append(end);
        sb.append("周");
        return sb.toString();
    }
}
