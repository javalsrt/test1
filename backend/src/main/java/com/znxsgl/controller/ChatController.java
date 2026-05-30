package com.znxsgl.controller;

import com.znxsgl.dto.ChatMessageDTO;
import com.znxsgl.dto.StudentAskStatsDTO;
import com.znxsgl.service.ChatService;
import com.znxsgl.service.DashScopeService;
import com.znxsgl.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final DashScopeService aiService;
    private final RagService ragService;

    public ChatController(ChatService chatService, DashScopeService aiService,
                          RagService ragService) {
        this.chatService = chatService;
        this.aiService = aiService;
        this.ragService = ragService;
    }

    // 获取课程聊天记录
    @GetMapping("/{courseName}")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(
            @PathVariable String courseName, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(chatService.getMessages(courseName, userId));
    }

    // 发送消息
    @PostMapping("/send")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(chatService.sendMessage(
                body.get("courseName"), userId, body.get("content"),
                body.getOrDefault("senderRole", "student")));
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
}
