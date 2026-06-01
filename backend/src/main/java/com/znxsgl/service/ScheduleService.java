package com.znxsgl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.znxsgl.dto.StudentCourseDTO;
import com.znxsgl.dto.StudentScheduleDTO;
import com.znxsgl.dto.TeacherCourseDTO;
import com.znxsgl.entity.*;
import com.znxsgl.mapper.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final ScheduleMapper scheduleMapper;
    private final ClassInfoMapper classInfoMapper;
    private final CourseClassMapper courseClassMapper;
    private final TeacherMapper teacherMapper;
    private final JdbcTemplate jdbc;

    public ScheduleService(UserMapper userMapper, CourseMapper courseMapper,
                           ScheduleMapper scheduleMapper, ClassInfoMapper classInfoMapper,
                           CourseClassMapper courseClassMapper, TeacherMapper teacherMapper,
                           JdbcTemplate jdbc) {
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.scheduleMapper = scheduleMapper;
        this.classInfoMapper = classInfoMapper;
        this.courseClassMapper = courseClassMapper;
        this.teacherMapper = teacherMapper;
        this.jdbc = jdbc;
    }

    // ===== 教师：查看所教课程及班级（含下架状态） =====
    public List<TeacherCourseDTO> getTeacherCourses(Long teacherUserId) {
        User user = userMapper.selectById(teacherUserId);
        if (user == null || user.getRole() < 2) return Collections.emptyList();

        Teacher teacher = teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getRealName, user.getRealName()));
        if (teacher == null) return Collections.emptyList();

        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, teacher.getId()));

        List<TeacherCourseDTO> result = new ArrayList<>();
        for (Course course : courses) {
            TeacherCourseDTO dto = new TeacherCourseDTO();
            dto.setCourseId(course.getId());
            dto.setCourseName(course.getCourseName());
            dto.setSemester(course.getSemester());
            dto.setCourseType(course.getCourseType());
            dto.setDescription(course.getDescription());
            dto.setCredit(course.getCredit());

            List<Map<String, Object>> classRows = courseClassMapper.findClassesByCourseId(course.getId());
            List<TeacherCourseDTO.ClazzDTO> clazzList = new ArrayList<>();
            for (Map<String, Object> row : classRows) {
                TeacherCourseDTO.ClazzDTO clazz = new TeacherCourseDTO.ClazzDTO();
                Long cid = ((Number) row.get("classId")).longValue();
                clazz.setClassId(cid);
                clazz.setClassName((String) row.get("className"));
                clazz.setStudentCount(((Number) row.get("studentCount")).intValue());
                // 检查该班级是否已有在线排课记录（排除 day_of_week=0 的占位记录）
                int scheduledCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM schedule s JOIN user u ON u.id = s.user_id " +
                    "WHERE s.course_name = ? AND u.class_id = ? AND s.status = 1 AND s.day_of_week > 0",
                    Integer.class, course.getCourseName(), cid);
                clazz.setScheduled(scheduledCount > 0);
                clazzList.add(clazz);
            }
            dto.setClasses(clazzList);

            // 检查课表表中是否有 status=1 的活跃记录
            Long activeCount = scheduleMapper.selectCount(
                new LambdaQueryWrapper<Schedule>()
                    .eq(Schedule::getCourseName, course.getCourseName())
                    .eq(Schedule::getStatus, 1));
            dto.setActive(activeCount > 0);

            result.add(dto);
        }
        return result;
    }

    // ===== 学生：按周查看课表 =====
    public List<StudentScheduleDTO> getStudentSchedule(Long userId, int week) {
        String semester = "2025-2026-2";
        LambdaQueryWrapper<Schedule> qw = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getUserId, userId)
                .eq(Schedule::getSemester, semester)
                .eq(Schedule::getStatus, 1)
                .gt(Schedule::getDayOfWeek, 0);  // 排除占位记录（day_of_week=0）

        // 按周过滤：weeks 为 JSON 数组，使用 JSON_CONTAINS
        if (week > 0) {
            qw.apply("JSON_CONTAINS(weeks, CAST({0} AS JSON))", week);
        }

        List<Schedule> schedules = scheduleMapper.selectList(qw);

        return schedules.stream().map(s -> {
            StudentScheduleDTO dto = new StudentScheduleDTO();
            dto.setScheduleId(s.getId());
            dto.setCourseId(s.getCourseId());
            dto.setCourseName(s.getCourseName());
            dto.setDayOfWeek(s.getDayOfWeek());
            dto.setStartTime(s.getStartTime());
            dto.setEndTime(s.getEndTime());
            dto.setStartNode(s.getStartNode());
            dto.setStep(s.getStep());
            dto.setClassroom(s.getClassroom());
            dto.setSemester(s.getSemester());
            dto.setWeeks(s.getWeeks());
            dto.setTeacherName("");

            // 尝试从课程表获取教师名
            if (s.getCourseId() != null) {
                Course course = courseMapper.selectById(s.getCourseId());
                if (course != null && course.getTeacherId() != null) {
                    Teacher t = teacherMapper.selectById(course.getTeacherId());
                    if (t != null) dto.setTeacherName(t.getRealName());
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // ===== 学生：查看"我的课程"列表（含上下架状态） =====
    public List<StudentCourseDTO> getStudentCourses(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getClassId() == null) return Collections.emptyList();
        Long classId = user.getClassId();

        // 查询该班级关联的所有课程（通过 course_class 表 + schedule 表兜底）
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT DISTINCT c.id, c.course_name, c.semester, c.course_type, c.description, c.credit, " +
            "t.real_name AS teacher_name " +
            "FROM course c " +
            "LEFT JOIN course_class cc ON cc.course_id = c.id AND cc.class_id = ? " +
            "LEFT JOIN schedule s ON s.course_id = c.id AND s.user_id IN (SELECT id FROM user WHERE class_id = ?) " +
            "LEFT JOIN teacher t ON t.id = c.teacher_id " +
            "WHERE cc.class_id = ? OR s.id IS NOT NULL " +
            "ORDER BY c.course_name",
            classId, classId, classId);

        List<StudentCourseDTO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            StudentCourseDTO dto = new StudentCourseDTO();
            dto.setCourseId(((Number) row.get("id")).longValue());
            dto.setCourseName((String) row.get("course_name"));
            dto.setTeacherName((String) row.get("teacher_name"));
            dto.setSemester((String) row.get("semester"));
            dto.setCourseType((String) row.get("course_type"));
            dto.setDescription((String) row.get("description"));

            // 检查课程是否在线（任意班级有 status=1 且 day_of_week>0 即在线）
            int activeCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM schedule s " +
                "WHERE s.course_name = ? AND s.status = 1 AND s.day_of_week > 0",
                Integer.class, dto.getCourseName());
            dto.setActive(activeCount > 0);

            // 检查课程是否已发布/上架（有 status=1 记录即已上架，含 day_of_week=0 占位）
            int publishedCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM schedule s " +
                "WHERE s.course_name = ? AND s.status = 1",
                Integer.class, dto.getCourseName());
            dto.setPublished(publishedCount > 0);

            // 查询该学生在此课程的未读消息数（仅 is_read=0）
            int unread = jdbc.queryForObject(
                "SELECT COUNT(*) FROM chat_message " +
                "WHERE course_name = ? AND user_id = ? AND sender_role = 'teacher' AND is_read = 0",
                Integer.class, dto.getCourseName(), userId);
            dto.setUnreadCount(unread);

            // 检查本班级是否有排课
            int myScheduleCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM schedule s JOIN user u ON u.id = s.user_id " +
                "WHERE s.course_name = ? AND u.class_id = ? AND s.status = 1 AND s.day_of_week > 0",
                Integer.class, dto.getCourseName(), classId);
            dto.setHasSchedule(myScheduleCount > 0);

            // 查询排课摘要（本班级的）
            if (myScheduleCount > 0) {
                List<Map<String, Object>> scheduleRows = jdbc.queryForList(
                    "SELECT DISTINCT s.day_of_week, s.start_time, s.end_time FROM schedule s " +
                    "JOIN user u ON u.id = s.user_id " +
                    "WHERE s.course_name = ? AND u.class_id = ? AND s.status = 1 AND s.day_of_week > 0 " +
                    "ORDER BY s.day_of_week LIMIT 3",
                    dto.getCourseName(), classId);
                List<String> infos = new ArrayList<>();
                String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
                for (Map<String, Object> sr : scheduleRows) {
                    int dow = ((Number) sr.get("day_of_week")).intValue();
                    infos.add(dayNames[dow] + " " + sr.get("start_time") + "-" + sr.get("end_time"));
                }
                dto.setScheduleInfo(String.join("；", infos));
            }

            result.add(dto);
        }
        return result;
    }
}
