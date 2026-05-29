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

    private String chatInternal(String systemPrompt, String userMessage,
                                 String mediaType, String mediaUrl) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);

            // 课表提取需要较大输出，设置 max_tokens
            ObjectNode params = mapper.createObjectNode();
            params.put("max_tokens", 4096);
            body.set("parameters", params);

            ObjectNode input = mapper.createObjectNode();
            ArrayNode messages = mapper.createArrayNode();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                ObjectNode sys = mapper.createObjectNode();
                sys.put("role", "system");
                ArrayNode sysContent = mapper.createArrayNode();
                ObjectNode sysText = mapper.createObjectNode();
                sysText.put("text", systemPrompt);
                sysContent.add(sysText);
                sys.set("content", sysContent);
                messages.add(sys);
            }

            ObjectNode user = mapper.createObjectNode();
            user.put("role", "user");
            ArrayNode userContent = mapper.createArrayNode();

            // 如果有图片，加入 image_url
            if ("image_url".equals(mediaType) && mediaUrl != null) {
                ObjectNode imagePart = mapper.createObjectNode();
                imagePart.put("image", mediaUrl);
                userContent.add(imagePart);
            }

            // 文本消息
            if (userMessage != null && !userMessage.isEmpty()) {
                ObjectNode userText = mapper.createObjectNode();
                userText.put("text", userMessage);
                userContent.add(userText);
            }

            user.set("content", userContent);
            messages.add(user);

            input.set("messages", messages);
            body.set("input", input);

            String reqJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
            System.out.println("=== DashScope 请求: " + (userMessage != null ? userMessage.substring(0, Math.min(50, userMessage.length())) : "[image]"));

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(reqJson, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String bodyStr = response.body().string();

                if (response.code() != 200) {
                    System.out.println("=== DashScope 错误: " + bodyStr.substring(0, Math.min(200, bodyStr.length())));
                    return "（AI 服务暂时不可用）";
                }

                JsonNode node = mapper.readTree(bodyStr);
                JsonNode choices = node.path("output").path("choices");
                if (!choices.isArray() || choices.size() == 0) {
                    return "（AI 未返回内容）";
                }

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
        } catch (Exception e) {
            System.out.println("=== DashScope 异常: " + e.getMessage());
        }
        return "（AI 服务异常，请稍后重试）";
    }
}
