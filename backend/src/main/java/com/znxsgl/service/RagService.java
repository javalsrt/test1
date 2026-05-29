package com.znxsgl.service;

import com.znxsgl.entity.DocumentVector;
import com.znxsgl.mapper.DocumentVectorMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RagService {

    private final DocumentVectorMapper docMapper;
    private final DashScopeService ai;
    private final Path uploadDir;

    public RagService(DocumentVectorMapper docMapper, DashScopeService ai) {
        this.docMapper = docMapper;
        this.ai = ai;
        try {
            uploadDir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "znxsgl_uploads"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 上传并分析文件，存入向量库 */
    public String uploadAndAnalyze(String courseName, MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            Path saved = uploadDir.resolve(UUID.randomUUID() + "_" + fileName);
            file.transferTo(saved.toFile());

            String extractedText = null;

            if (isDocument(fileName)) {
                extractedText = extractDocumentText(saved);
            } else {
                // 图片：用 Qwen-VL 分析
                byte[] bytes = Files.readAllBytes(saved);
                String base64 = Base64.getEncoder().encodeToString(bytes);
                String mime = file.getContentType() != null ? file.getContentType() : "image/jpeg";
                String imageUrl = "data:" + mime + ";base64," + base64;
                extractedText = ai.analyzeImage("请描述这张图片的内容，提取其中的文字信息", imageUrl);
            }

            if (extractedText == null || extractedText.trim().isEmpty()) {
                return "（文件已上传，但未能提取到文字内容）";
            }

            // 分块存入向量库
            List<String> chunks = splitText(extractedText, 500);
            for (String chunk : chunks) {
                DocumentVector dv = new DocumentVector();
                dv.setCourseName(courseName);
                dv.setDocName(fileName);
                dv.setContentChunk(chunk.trim());
                dv.setCreatedAt(LocalDateTime.now());
                docMapper.insert(dv);
            }

            String summary = chunks.size() > 1
                    ? extractedText.substring(0, Math.min(200, extractedText.length())) + "..."
                    : extractedText;
            return "📎 已分析《" + fileName + "》，提取 " + extractedText.length()
                    + " 字，分 " + chunks.size() + " 块存入知识库：\n\n" + summary;
        } catch (Exception e) {
            e.printStackTrace();
            return "（文件处理失败：" + e.getMessage() + "）";
        }
    }

    /** RAG 检索：根据用户问题搜索相关上下文 */
    public String retrieveContext(String courseName, String question) {
        List<String> results = docMapper.search(courseName, question);
        if (results == null || results.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("以下是课程相关参考内容：\n");
        for (int i = 0; i < results.size(); i++) {
            sb.append("---\n参考").append(i + 1).append("：").append(results.get(i)).append("\n");
        }
        return sb.toString();
    }

    // ===== 工具方法 =====

    private boolean isDocument(String name) {
        if (name == null) return false;
        String l = name.toLowerCase();
        return l.endsWith(".pdf") || l.endsWith(".docx") || l.endsWith(".doc")
                || l.endsWith(".txt") || l.endsWith(".md");
    }

    /** 从文档中提取文本 */
    private String extractDocumentText(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        try {
            if (name.endsWith(".txt") || name.endsWith(".md")) {
                return new String(Files.readAllBytes(file));
            }
            if (name.endsWith(".docx")) {
                return extractDocxText(file);
            }
            if (name.endsWith(".pdf")) {
                return extractPdfText(file);
            }
            if (name.endsWith(".doc")) {
                // 旧版 .doc 格式不做复杂处理，用 AI 分析
                return ai.analyzeDocument("请提取这个文档的文本内容", file.toFile().getAbsolutePath(), null);
            }
        } catch (Exception e) {
            System.out.println("=== 文档提取失败: " + e.getMessage());
        }
        return null;
    }

    /** DOCX 文本提取（纯 ZIP + XML 解析，无需 POI） */
    private String extractDocxText(Path file) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(
                new FileInputStream(file.toFile()))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("word/document.xml")) {
                    String xml = new String(zis.readAllBytes());
                    // 简单提取 <w:t> 标签中的文本
                    String[] parts = xml.split("<w:t[^>]*>");
                    for (String part : parts) {
                        int end = part.indexOf("</w:t>");
                        if (end >= 0) sb.append(part, 0, end);
                    }
                }
            }
        }
        return sb.toString().trim();
    }

    /** PDF 文本提取（纯 Java，无需 PDFBox） */
    private String extractPdfText(Path file) throws Exception {
        byte[] bytes = Files.readAllBytes(file);
        String content = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        StringBuilder sb = new StringBuilder();

        // 提取 BT...ET 文本块
        int pos = 0;
        while ((pos = content.indexOf("BT", pos)) >= 0) {
            int end = content.indexOf("ET", pos);
            if (end < 0) break;
            String block = content.substring(pos + 2, end);
            // 提取 Tj 和 TJ 操作符中的文本
            int tjPos = 0;
            while ((tjPos = block.indexOf("Tj", tjPos)) >= 0) {
                int start = block.lastIndexOf("(", tjPos);
                int stop = block.indexOf(")", tjPos);
                if (start >= 0 && stop > start) {
                    sb.append(block, start + 1, stop);
                }
                tjPos += 2;
            }
            pos = end + 2;
        }
        return sb.toString().trim();
    }

    /** 文本分块 */
    private List<String> splitText(String text, int maxLen) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLen, text.length());
            // 尽量在句子边界处断开
            if (end < text.length()) {
                int breakPoint = text.lastIndexOf('。', end);
                if (breakPoint > start && breakPoint > end - 100) end = breakPoint + 1;
                else {
                    breakPoint = text.lastIndexOf('\n', end);
                    if (breakPoint > start && breakPoint > end - 100) end = breakPoint;
                }
            }
            chunks.add(text.substring(start, end));
            start = end;
        }
        return chunks;
    }
}
