package com.znxsgl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class DashScopeService {

    @Value("${dashscope.api-key}")
    private String apiKey;

    @Value("${dashscope.model}")
    private String model;

    @Value("${dashscope.url}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 同步调用 AI，返回完整回复文本
     */
    public String chat(String systemPrompt, String userMessage) {
        return chatInternal(systemPrompt, userMessage, null, null);
    }

    /** 图片分析（Qwen-VL） */
    public String analyzeImage(String prompt, String imageUrl) {
        return chatInternal(prompt, null, "image_url", imageUrl);
    }

    /** 文档分析 */
    public String analyzeDocument(String prompt, String filePath, String mimeType) {
        return chatInternal(prompt, "请分析以下文件内容", null, null);
    }

    /**
     * 判断是否为 OpenAI 兼容接口（/compatible-mode/）
     */
    private boolean isCompatibleMode() {
        return apiUrl != null && apiUrl.contains("compatible-mode");
    }

    private String chatInternal(String systemPrompt, String userMessage,
                                 String mediaType, String mediaUrl) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);

            // 有图片时用多模态格式，纯文本时用兼容模式格式
            boolean hasImage = "image_url".equals(mediaType) && mediaUrl != null;

            if (hasImage) {
                // 多模态格式
                body.putObject("parameters").put("max_tokens", 4096);
                ObjectNode input = body.putObject("input");
                ArrayNode messages = input.putArray("messages");

                if (systemPrompt != null && !systemPrompt.isEmpty()) {
                    ObjectNode sys = mapper.createObjectNode();
                    sys.put("role", "system");
                    sys.putArray("content").addObject().put("text", systemPrompt);
                    messages.add(sys);
                }

                ObjectNode user = mapper.createObjectNode();
                user.put("role", "user");
                ArrayNode userContent = user.putArray("content");
                userContent.addObject().put("image", mediaUrl);
                if (userMessage != null && !userMessage.isEmpty()) {
                    userContent.addObject().put("text", userMessage);
                }
                messages.add(user);
            } else {
                // OpenAI 兼容格式：messages 直接放顶层，content 是字符串
                ArrayNode messages = body.putArray("messages");

                if (systemPrompt != null && !systemPrompt.isEmpty()) {
                    ObjectNode sys = mapper.createObjectNode();
                    sys.put("role", "system");
                    sys.put("content", systemPrompt);
                    messages.add(sys);
                }

                ObjectNode user = mapper.createObjectNode();
                user.put("role", "user");
                user.put("content", userMessage != null ? userMessage : "");
                messages.add(user);
            }

            String reqJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
            System.out.println("=== DashScope 请求: " + (userMessage != null ? userMessage.substring(0, Math.min(80, userMessage.length())) : "[image]"));

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(reqJson, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String bodyStr = response.body().string();

                if (response.code() != 200) {
                    System.out.println("=== DashScope 错误[" + response.code() + "]: " + bodyStr.substring(0, Math.min(300, bodyStr.length())));
                    return "（AI 服务暂时不可用）";
                }

                JsonNode node = mapper.readTree(bodyStr);

                // 兼容模式响应：choices[0].message.content（字符串）
                JsonNode choices = node.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    String result = choices.get(0).path("message").path("content").asText("");
                    if (!result.isEmpty()) {
                        System.out.println("=== DashScope 回复: " + result.substring(0, Math.min(100, result.length())));
                        return result;
                    }
                }

                // 多模态响应：output.choices[0].message.content（数组）
                choices = node.path("output").path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode content = choices.get(0).path("message").path("content");
                    if (content.isArray()) {
                        StringBuilder sb = new StringBuilder();
                        for (JsonNode part : content) {
                            String text = part.path("text").asText("");
                            if (!text.isEmpty()) sb.append(text);
                        }
                        String result = sb.toString();
                        System.out.println("=== DashScope 回复: " + result.substring(0, Math.min(100, result.length())));
                        return result;
                    } else if (content.isTextual()) {
                        String result = content.asText();
                        System.out.println("=== DashScope 回复: " + result.substring(0, Math.min(100, result.length())));
                        return result;
                    }
                }

                System.out.println("=== DashScope 未识别的响应格式: " + bodyStr.substring(0, Math.min(200, bodyStr.length())));
                return "（AI 返回格式异常）";
            }
        } catch (Exception e) {
            System.out.println("=== DashScope 异常: " + e.getMessage());
            e.printStackTrace();
        }
        return "（AI 服务异常，请稍后重试）";
    }
}
