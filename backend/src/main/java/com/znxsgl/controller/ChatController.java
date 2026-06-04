package com.znxsgl.controller;

import com.znxsgl.dto.ChatMessageDTO;
import com.znxsgl.dto.StudentAskStatsDTO;
import com.znxsgl.service.ChatService;
import com.znxsgl.service.DashScopeService;
import com.znxsgl.service.RagService;
import com.znxsgl.websocket.ScheduleWebSocketHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final DashScopeService aiService;
    private final RagService ragService;
    private final ScheduleWebSocketHandler wsHandler;
    private final JdbcTemplate jdbc;

    public ChatController(ChatService chatService, DashScopeService aiService,
                          RagService ragService, ScheduleWebSocketHandler wsHandler,
                          JdbcTemplate jdbc) {
        this.chatService = chatService;
        this.aiService = aiService;
        this.ragService = ragService;
        this.wsHandler = wsHandler;
        this.jdbc = jdbc;
    }

    // 获取课程聊天记录（个人：学生看自己的AI对话，按userId过滤）
    @GetMapping("/{courseName}")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(
            @PathVariable String courseName, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(chatService.getMessages(courseName, userId));
    }

    // 获取课程公开聊天（群聊：教师/学生都能看到所有人的消息）
    @GetMapping("/{courseName}/public")
    public ResponseEntity<List<ChatMessageDTO>> getPublicMessages(@PathVariable String courseName) {
        return ResponseEntity.ok(chatService.getPublicMessages(courseName));
    }

    // 发送消息
    @PostMapping("/send")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String courseName = body.get("courseName");
        String content = body.get("content");
        String senderRole = body.getOrDefault("senderRole", "student");

        ChatMessageDTO msg = chatService.sendMessage(courseName, userId, content, senderRole);

        // 推送实时通知给课程所有学生
        try {
            List<Long> studentIds = jdbc.queryForList(
                "SELECT DISTINCT u.id FROM user u " +
                "JOIN course_class cc ON cc.class_id = u.class_id " +
                "JOIN course c ON c.id = cc.course_id " +
                "WHERE c.course_name = ? AND u.role = 1 AND u.id != ?",
                Long.class, courseName, userId);
            String preview;
            if (content.startsWith("[image]")) {
                preview = "📷 [图片]";
            } else if (content.startsWith("[file]")) {
                String fn = content.substring(6);
                int pSep = fn.indexOf('|');
                String fname = pSep > 0 ? fn.substring(0, pSep) : fn;
                preview = "📄 [文件] " + (fname.length() > 20 ? fname.substring(0, 20) + "..." : fname);
            } else {
                preview = content.length() > 60 ? content.substring(0, 60) + "..." : content;
            }
            for (Long sid : studentIds) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("courseName", courseName);
                data.put("senderName", msg.getSenderName());
                data.put("content", preview);
                data.put("senderRole", senderRole);
                wsHandler.sendToUser(sid, "chat_update", data);
            }
        } catch (Exception ignored) {}

        return ResponseEntity.ok(msg);
    }

    // RAG 对话（自动检索课程知识库）
    @PostMapping("/rag")
    public ResponseEntity<ChatMessageDTO> ragChat(@RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String courseName = body.get("courseName");
        String content = body.get("content");

        // 1. 保存学生消息
        chatService.sendMessage(courseName, userId, content, "student");

        // 2. RAG 检索相关知识（表不存在时优雅降级）
        String ragContext = "";
        try {
            ragContext = ragService.retrieveContext(courseName, content);
        } catch (Exception e) {
            System.out.println("=== RAG 检索失败（可能表未创建）: " + e.getMessage());
        }

        // 3. 构建带 RAG 上下文的 prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是《").append(courseName).append("》课程的智能助教。");
        prompt.append("请只回答与这门课程相关的问题。");
        if (!ragContext.isEmpty()) {
            prompt.append("\n\n").append(ragContext);
            prompt.append("\n请根据以上参考内容回答学生问题。如果参考内容与问题无关，请基于你的知识回答。");
        }

        // 4. 调用 AI
        String aiReply = aiService.chat(prompt.toString(), content);
        return ResponseEntity.ok(chatService.sendMessage(courseName, userId, aiReply, "ai"));
    }

    // 上传文件并 AI 分析
    @PostMapping("/upload")
    public ResponseEntity<ChatMessageDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseName") String courseName,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();

        // 1. 保存用户消息（文件上传提示）
        String hint = "📎 正在分析《" + file.getOriginalFilename() + "》...";
        chatService.sendMessage(courseName, userId, hint, "student");

        // 2. 分析文件
        String analysisResult = ragService.uploadAndAnalyze(courseName, file);

        // 3. 保存分析结果
        return ResponseEntity.ok(chatService.sendMessage(courseName, userId, analysisResult, "ai"));
    }

    // 教师查看学生提问统计
    @GetMapping("/stats/{courseName}")
    public ResponseEntity<?> getAskStats(
            @PathVariable String courseName,
            @RequestParam(required = false) Long classId,
            Authentication auth) {
        Long teacherUserId = (Long) auth.getPrincipal();
        try {
            List<StudentAskStatsDTO> stats = chatService.getAskStats(courseName, teacherUserId, classId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    /** 学生进入课程详情时，标记该课程所有教师消息为已读 */
    @PostMapping("/read")
    public ResponseEntity<Map<String, String>> markAsRead(@RequestBody Map<String, String> body,
                                                           Authentication auth) {
        String courseName = body.get("courseName");
        Long userId = (Long) auth.getPrincipal();
        chatService.markAsRead(courseName, userId);
        return ResponseEntity.ok(Map.of("msg", "已读"));
    }

    /** 简单文件上传（图片/文档），返回可访问 URL */
    @PostMapping("/upload-file")
    public ResponseEntity<Map<String, String>> uploadChatFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseName") String courseName) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/chat/" + courseName;
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());

            String url = "/uploads/chat/" + courseName + "/" + filename;
            return ResponseEntity.ok(Map.of("url", url, "fileName", filename));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "上传失败: " + e.getMessage()));
        }
    }

    /** 获取学生各课程未读消息数量 */
    @GetMapping("/unread")
    public ResponseEntity<List<Map<String, Object>>> getUnreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<Map<String, Object>> result = jdbc.queryForList(
            "SELECT cm.course_name AS courseName, COUNT(*) AS count " +
            "FROM chat_message cm " +
            "WHERE cm.user_id != ? " +
            "AND cm.course_name IN (SELECT c.course_name FROM course c " +
            "  JOIN course_class cc ON cc.course_id = c.id " +
            "  JOIN user u ON u.class_id = cc.class_id WHERE u.id = ?) " +
            "GROUP BY cm.course_name",
            userId, userId);
        return ResponseEntity.ok(result);
    }
}
