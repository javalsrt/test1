package com.znxsgl.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 消息推送处理器
 * 连接格式：ws://host/ws/schedule?token=jwt_token&userId=123
 */
@Component
public class ScheduleWebSocketHandler extends TextWebSocketHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    // userId -> WebSocketSession 映射
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        Long userId = parseUserId(query);
        if (userId != null) {
            sessions.put(userId, session);
            System.out.println("WebSocket 连接: userId=" + userId);
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端发心跳，回复 pong
        String payload = message.getPayload();
        if ("ping".equals(payload)) {
            try {
                session.sendMessage(new TextMessage("pong"));
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().removeIf(s -> s.getId().equals(session.getId()));
        System.out.println("WebSocket 断开: " + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        sessions.values().removeIf(s -> s.getId().equals(session.getId()));
    }

    /** 给指定用户推送消息 */
    public void sendToUser(Long userId, String type, Map<String, Object> data) {
        WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) return;
        try {
            Map<String, Object> msg = new java.util.LinkedHashMap<>();
            msg.put("type", type);
            msg.put("data", data);
            String json = MAPPER.writeValueAsString(msg);
            synchronized (session) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            sessions.remove(userId);
        }
    }

    /** 给某个班级的所有在线学生推送消息 */
    public void sendToClass(Long classId, String type, Map<String, Object> data,
                             org.springframework.jdbc.core.JdbcTemplate jdbc) {
        try {
            java.util.List<Long> userIds = jdbc.queryForList(
                "SELECT id FROM user WHERE class_id = ? AND role = 1", Long.class, classId);
            for (Long uid : userIds) {
                sendToUser(uid, type, data);
            }
        } catch (Exception ignored) {}
    }

    private Long parseUserId(String query) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "userId".equals(kv[0])) {
                try { return Long.parseLong(kv[1]); } catch (NumberFormatException e) { return null; }
            }
        }
        return null;
    }
}
