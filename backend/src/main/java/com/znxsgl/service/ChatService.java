package com.znxsgl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.znxsgl.dto.ChatMessageDTO;
import com.znxsgl.dto.StudentAskStatsDTO;
import com.znxsgl.entity.ChatMessage;
import com.znxsgl.entity.Course;
import com.znxsgl.entity.User;
import com.znxsgl.mapper.ChatMessageMapper;
import com.znxsgl.mapper.CourseMapper;
import com.znxsgl.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageMapper chatMapper;
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;

    public ChatService(ChatMessageMapper chatMapper, UserMapper userMapper,
                       CourseMapper courseMapper) {
        this.chatMapper = chatMapper;
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
    }

    // 获取课程聊天记录（学生看：自己的消息 + 教师消息 + 所有AI回复）
    public List<ChatMessageDTO> getMessages(String courseName, Long userId) {
        List<ChatMessage> msgs = chatMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getCourseName, courseName)
                        .and(w -> w.eq(ChatMessage::getUserId, userId)
                                .or().eq(ChatMessage::getSenderRole, "teacher")
                                .or().eq(ChatMessage::getSenderRole, "ai"))
                        .orderByAsc(ChatMessage::getCreatedAt));
        return msgs.stream().map(this::toDto).collect(Collectors.toList());
    }

    // 获取课程公开聊天（群聊，不过滤userId）
    public List<ChatMessageDTO> getPublicMessages(String courseName) {
        List<ChatMessage> msgs = chatMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getCourseName, courseName)
                        .orderByAsc(ChatMessage::getCreatedAt));
        return msgs.stream().map(this::toDto).collect(Collectors.toList());
    }

    // 发送消息
    public ChatMessageDTO sendMessage(String courseName, Long userId, String content, String senderRole) {
        User user = userMapper.selectById(userId);

        ChatMessage msg = new ChatMessage();
        msg.setCourseName(courseName);
        msg.setUserId(userId);
        msg.setSenderName(user != null ? user.getRealName() : "系统");
        msg.setSenderRole(senderRole);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        chatMapper.insert(msg);

        return toDto(msg);
    }

    /** 教师查看学生提问统计（验证教师拥有该课程） */
    public List<StudentAskStatsDTO> getAskStats(String courseName, Long teacherUserId, Long classId) {
        // 验证教师是否教这门课
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getCourseName, courseName)
                        .eq(Course::getTeacherId, teacherUserId));
        if (courses.isEmpty()) {
            throw new IllegalArgumentException("无权查看该课程的统计数据");
        }
        return chatMapper.countStudentAsks(courseName, classId);
    }

    private ChatMessageDTO toDto(ChatMessage m) {
        ChatMessageDTO d = new ChatMessageDTO();
        d.setId(m.getId());
        d.setCourseName(m.getCourseName());
        d.setUserId(m.getUserId());
        d.setSenderName(m.getSenderName());
        d.setSenderRole(m.getSenderRole());
        d.setContent(m.getContent());
        d.setCreatedAt(m.getCreatedAt());
        return d;
    }

    /** 标记该学生在指定课程的所有教师消息为已读 */
    public void markAsRead(String courseName, Long userId) {
        chatMapper.markAsRead(courseName, userId);
    }
}
