package com.znxsgl.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.znxsgl.entity.QuizAnswer;
import com.znxsgl.entity.QuizSession;
import com.znxsgl.mapper.QuizAnswerMapper;
import com.znxsgl.mapper.QuizSessionMapper;
import com.znxsgl.mapper.UserMapper;
import com.znxsgl.service.DashScopeService;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final DashScopeService ai;
    private final QuizSessionMapper sessionMapper;
    private final QuizAnswerMapper answerMapper;
    private final UserMapper userMapper;
    private final JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();

    public QuizController(DashScopeService ai, QuizSessionMapper sessionMapper,
                          QuizAnswerMapper answerMapper, UserMapper userMapper, JdbcTemplate jdbc) {
        this.ai = ai;
        this.sessionMapper = sessionMapper;
        this.answerMapper = answerMapper;
        this.userMapper = userMapper;
        this.jdbc = jdbc;
    }

    /** 生成题目 */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String subject = body.get("subject");
        String subjectType = body.getOrDefault("subjectType", "专业");

        List<Map<String, Object>> courses = jdbc.queryForList(
                "SELECT DISTINCT c.course_name FROM course c " +
                "JOIN course_class cc ON cc.course_id = c.id " +
                "JOIN user u ON u.class_id = cc.class_id WHERE u.id = ?", userId);

        StringBuilder courseList = new StringBuilder();
        for (Map<String, Object> c : courses) {
            if (courseList.length() > 0) courseList.append("、");
            courseList.append(c.get("course_name"));
        }

        String prompt = buildGeneratePrompt(subject, subjectType, courseList.toString());
        String raw = ai.chat("你是专业出题专家，只输出纯JSON数组，不要任何解释文字。", prompt);
        System.out.println("=== AI出题原始返回: " + (raw != null ? raw.substring(0, Math.min(300, raw.length())) : "null"));

        List<Map<String, Object>> questions = parseQuestions(raw);
        if (questions.isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "题目生成失败", "questions", List.of()));
        }

        int sessionNo = sessionMapper.countByUser(userId) + 1;
        QuizSession session = new QuizSession();
        session.setUserId(userId); session.setSubject(subject);
        session.setSubjectType(subjectType); session.setSessionNo(sessionNo);
        session.setTotalQuestions(questions.size()); session.setStatus("pending");
        session.setCreatedAt(LocalDateTime.now());
        sessionMapper.insert(session);

        return ResponseEntity.ok(Map.of(
                "sessionId", session.getId(), "sessionNo", sessionNo,
                "subject", subject, "questions", questions));
    }

    /** 提交评估 */
    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, Object>> evaluate(@RequestBody Map<String, Object> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");

        QuizSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "无权提交"));
        }

        int answered = 0, correct = 0, skip = 0, totalSec = 0;
        int earlyCorrect = 0, earlyTotal = 0, midCorrect = 0, midTotal = 0, lateCorrect = 0, lateTotal = 0;
        int total = answers.size(), third = total / 3;

        for (int i = 0; i < answers.size(); i++) {
            Map<String, Object> a = answers.get(i);
            QuizAnswer qa = new QuizAnswer();
            qa.setSessionId(sessionId);
            qa.setQuestionIndex(i + 1);
            qa.setQuestionType(safeStr(a, "questionType"));
            qa.setSubject(safeStr(a, "subject"));
            qa.setQuestion(safeStr(a, "question"));
            qa.setOptions(a.get("options") != null ? jsonValue(a.get("options")) : null);
            qa.setUserAnswer(safeStr(a, "userAnswer"));
            qa.setCorrectAnswer(safeStr(a, "correctAnswer"));
            int dur = a.get("durationSec") instanceof Number ? ((Number) a.get("durationSec")).intValue() : 0;
            qa.setDurationSec(dur);
            totalSec += dur;

            String ua = qa.getUserAnswer();
            String ca = qa.getCorrectAnswer();
            int isCorrect = -2;
            if (ua != null && !ua.isEmpty() && !"不会".equals(ua)) {
                boolean ok = isAnswerCorrect(ua, ca, qa.getQuestionType());
                isCorrect = ok ? 1 : 0;
                if (ok) correct++;
                answered++;
            } else if ("不会".equals(ua)) {
                isCorrect = -1; skip++;
            } else {
                skip++;
            }

            qa.setIsCorrect(isCorrect);
            qa.setModifiedCount(a.get("modifiedCount") instanceof Number ? ((Number) a.get("modifiedCount")).intValue() : 0);
            qa.setCreatedAt(LocalDateTime.now());
            answerMapper.insert(qa);

            if (isCorrect == 1) {
                if (i < third) earlyCorrect++;
                else if (i >= total - third) lateCorrect++;
                else midCorrect++;
            }
            if (i < third) earlyTotal++;
            else if (i >= total - third) lateTotal++;
            else midTotal++;
        }

        session.setAnsweredCount(answered); session.setCorrectCount(correct);
        session.setSkipCount(skip); session.setTotalDurationSec(totalSec);
        session.setStatus("completed");
        sessionMapper.updateById(session);

        String r1 = earlyTotal > 0 ? (earlyCorrect * 100 / earlyTotal) + "%" : "0%";
        String r2 = midTotal > 0 ? (midCorrect * 100 / midTotal) + "%" : "0%";
        String r3 = lateTotal > 0 ? (lateCorrect * 100 / lateTotal) + "%" : "0%";

        String evalPrompt = buildEvaluatePrompt(answers, userId, totalSec, r1, r2, r3, skip);
        String evalRaw = ai.chat("你是大学生能力测评专家，只输出JSON，不要多余文字。评分严格稳定。", evalPrompt);
        System.out.println("=== AI评估返回: " + (evalRaw != null ? evalRaw.substring(0, Math.min(200, evalRaw.length())) : "null"));

        Map<String, Object> evalResult = parseEvalResult(evalRaw);
        if (evalResult != null) {
            session.setScores(jsonValue(evalResult.get("scores")));
            session.setStrengths(jsonValue(evalResult.get("strengths")));
            session.setWeaknesses(jsonValue(evalResult.get("weaknesses")));
            session.setSuggestion((String) evalResult.get("suggestion"));
            session.setStudyPlan(jsonValue(evalResult.get("study_plan")));
            session.setStatus("evaluated");
        }
        sessionMapper.updateById(session);
        vectorizeSummary(userId, session, evalResult);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId); result.put("correctCount", correct);
        result.put("skipCount", skip); result.put("totalDurationSec", totalSec);
        if (evalResult != null) result.putAll(evalResult);
        return ResponseEntity.ok(result);
    }

    // ===== Prompt =====

    private String buildGeneratePrompt(String subject, String type, String courseList) {
        int count = 15, choiceN = 7, judgeN = 3, analysisN = 3, fillN = 2;

        String scope;
        if (subject.contains("思政")) {
            scope = "出题范围：马克思主义基本原理、毛泽东思想和中国特色社会主义理论体系概论、中国近代史纲要。";
        } else if (subject.contains("人文")) {
            scope = "出题范围：大学英语（词汇、语法、阅读）、高等数学（微积分、线性代数）、大学生心理健康教育。";
        } else {
            scope = "学生所学课程：" + courseList + "。请重点围绕《" + subject + "》出题。";
        }

        return String.format(
                "请生成%d道题目：选择题%d题、判断题%d题、解析题%d题、填空题%d题。\n" +
                "科目：《%s》（%s类）\n%s\n" +
                "要求：难度中等偏上逐步递增；选择题4选项正确答案末尾加*；判断题正确选项加*；解析题只出题不设选项；填空题answer填答案；每道题subject字段填\"%s\"。\n" +
                "只输出JSON数组格式：[{\"type\":\"单选\",\"subject\":\"%s\",\"question\":\"...\",\"options\":[\"A.x\",\"B.y*\",...]}," +
                "{\"type\":\"判断\",\"subject\":\"%s\",\"question\":\"...\",\"options\":[\"正确*\",\"错误\"]}," +
                "{\"type\":\"解析\",\"subject\":\"%s\",\"question\":\"...\"}," +
                "{\"type\":\"填空\",\"subject\":\"%s\",\"question\":\"...\",\"answer\":\"答案\"}]",
                count, choiceN, judgeN, analysisN, fillN,
                subject, type, scope,
                subject, subject, subject, subject, subject);
    }

    private String buildEvaluatePrompt(List<Map<String, Object>> answers, Long userId,
                                        int totalSec, String r1, String r2, String r3, int skip) {
        String studentName = userMapper.selectById(userId).getRealName();
        StringBuilder ansText = new StringBuilder();
        for (int i = 0; i < answers.size(); i++) {
            Map<String, Object> a = answers.get(i);
            int dur = a.get("durationSec") instanceof Number ? ((Number) a.get("durationSec")).intValue() : 0;
            ansText.append(String.format("Q%d: %s → %s (%ds)\n",
                    i + 1,
                    a.getOrDefault("question", ""),
                    a.getOrDefault("userAnswer", "未作答"),
                    dur));
        }
        return String.format(
                "学生：%s | 总题数：%d | 总耗时：%ds | 跳过：%d | 正确率趋势：%s/%s/%s\n作答：\n%s\n" +
                "输出JSON：{\"scores\":{\"逻辑思维力\":N,\"判断决策力\":N,\"专注耐力\":N,\"专业学习力\":N,\"信息检索力\":N,\"自律执行力\":N}," +
                "\"strengths\":[],\"weaknesses\":[],\"suggestion\":\"\",\"study_plan\":[]}",
                studentName, answers.size(), totalSec, skip, r1, r2, r3, ansText);
    }

    // ===== 解析 =====

    private List<Map<String, Object>> parseQuestions(String raw) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return list;
        try {
            String clean = raw.trim();
            if (clean.startsWith("```")) {
                int s = clean.indexOf("["), e = clean.lastIndexOf("]");
                if (s >= 0 && e > s) clean = clean.substring(s, e + 1);
            }
            if (clean.startsWith("[") && !clean.endsWith("]")) {
                int last = clean.lastIndexOf("}");
                if (last > 0) clean = clean.substring(0, last + 1) + "]";
                else clean += "]";
            }
            JsonNode arr = json.readTree(clean);
            if (!arr.isArray()) return list;
            for (int i = 0; i < arr.size(); i++) {
                Map<String, Object> q = new LinkedHashMap<>();
                JsonNode n = arr.get(i);
                q.put("questionIndex", i + 1);
                q.put("questionType", n.path("type").asText());
                q.put("subject", n.path("subject").asText(""));
                q.put("question", n.path("question").asText());
                JsonNode opts = n.path("options");
                if (opts.isArray()) {
                    List<String> ol = new ArrayList<>();
                    for (JsonNode o : opts) {
                        String txt = o.asText();
                        ol.add(txt.replace("*", "")); // 去掉*显示
                        if (txt.endsWith("*")) q.put("correctAnswer", txt); // 存原始带*的作为正确答案
                    }
                    q.put("options", ol);
                }
                if (n.has("answer")) q.put("correctAnswer", n.path("answer").asText());
                list.add(q);
            }
        } catch (Exception e) {
            System.out.println("=== 题目解析失败: " + e.getMessage());
        }
        return list;
    }

    private Map<String, Object> parseEvalResult(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            String clean = raw.trim();
            if (clean.startsWith("```")) {
                int s = clean.indexOf("{"), e = clean.lastIndexOf("}");
                if (s >= 0 && e > s) clean = clean.substring(s, e + 1);
            }
            // 截断修复：补全未闭合的括号和字段
            if (!clean.endsWith("}")) {
                int lastQuote = clean.lastIndexOf("\"");
                clean = clean.substring(0, Math.max(lastQuote, clean.length() - 1)) + "]}";
            }
            if (!clean.startsWith("{")) clean = "{" + clean;
            return json.readValue(clean, Map.class);
        } catch (Exception e) {
            System.out.println("=== 评估解析失败: " + e.getMessage());
            return null;
        }
    }

    private boolean isAnswerCorrect(String user, String correct, String type) {
        if (user == null || correct == null) return false;
        String u = user.replace("*", "").replaceAll("[A-D]\\.\\s*", "").trim();
        String c = correct.replace("*", "").replaceAll("[A-D]\\.\\s*", "").trim();
        if ("判断".equals(type)) {
            u = u.replace("正确", "对").replace("错误", "错");
            c = c.replace("正确", "对").replace("错误", "错");
        }
        return u.equalsIgnoreCase(c);
    }

    private void vectorizeSummary(Long userId, QuizSession session, Map<String, Object> eval) {
        try {
            if (eval == null) return;
            String summary = String.format("学生第%d次测评（%s）：六维分已保存。%s",
                    session.getSessionNo(), session.getSubject(),
                    eval.getOrDefault("suggestion", ""));
            jdbc.update("INSERT INTO document_vector (course_name, doc_name, content_chunk, created_at) VALUES (?,?,?,NOW())",
                    "学生测评记录", "测评#" + session.getSessionNo(), summary);
        } catch (Exception ignored) {}
    }

    private String safeStr(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    private String jsonValue(Object obj) {
        try { return json.writeValueAsString(obj); } catch (Exception e) { return "null"; }
    }
}
