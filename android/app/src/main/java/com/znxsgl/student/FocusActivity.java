package com.znxsgl.student;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.znxsgl.student.model.QuizQuestion;
import com.znxsgl.student.network.ApiService;
import com.znxsgl.student.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FocusActivity extends AppCompatActivity {

    private TextView tvTimerCorner, tvToday, tvLast;
    private View llPanels, llQuizArea;
    private ViewPager2 viewPagerQuiz;
    private Button btnQuizDone;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int seconds = 0;
    private boolean running = false;
    private String startTime;
    private String token;

    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private QuizPagerAdapter quizAdapter;
    private long lastPageEntryTime;
    private int lastPageIndex = -1;
    private Long latestSessionId;

    // 面板数据由学生课程动态生成，不再写死

    private final Runnable tick = () -> { seconds++; updateTimerDisplay(); handler.postDelayed(this.tick, 1000); };
    private final Runnable refreshToday = new Runnable() {
        @Override public void run() { loadTodayTotal(); handler.postDelayed(this, 15000); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);

        SharedPreferences prefs = getSharedPreferences("znxsgl", 0);
        token = "Bearer " + prefs.getString("token", "");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        tvTimerCorner = findViewById(R.id.tv_timer_corner);
        tvToday = findViewById(R.id.tv_today);
        tvLast = findViewById(R.id.tv_last);
        llPanels = findViewById(R.id.ll_panels);
        llQuizArea = findViewById(R.id.ll_quiz_area);
        viewPagerQuiz = findViewById(R.id.viewpager_quiz);
        btnQuizDone = findViewById(R.id.btn_quiz_done);

        findViewById(R.id.ll_info).setVisibility(View.GONE);
        initQuizPanels();
        setupQuizPager();
        btnQuizDone.setOnClickListener(v -> submitQuiz());

        loadTodayTotal();
        showLastSession();
    }

    @Override protected void onResume() {
        super.onResume();
        if (llQuizArea.getVisibility() != View.VISIBLE) handler.postDelayed(this::autoStartTimer, 3000);
    }
    @Override protected void onPause() { super.onPause(); stopAndSave(); }

    private void autoStartTimer() {
        if (running) return;
        running = true;
        if (seconds == 0) startTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
        findViewById(R.id.ll_info).setVisibility(View.VISIBLE);
        handler.post(tick); handler.post(refreshToday); updateStatus("focusing");
    }

    private void stopAndSave() {
        handler.removeCallbacks(this::autoStartTimer);
        if (running) { running = false; handler.removeCallbacks(tick); handler.removeCallbacks(refreshToday); updateStatus("idle"); }
        if (seconds >= 1) { saveToServer(seconds); }
    }

    private void updateTimerDisplay() { tvTimerCorner.setText(formatDuration(seconds)); }
    private String formatDuration(int s) {
        if (s < 3600) return String.format(Locale.getDefault(), "%02d:%02d", s / 60, s % 60);
        return String.format(Locale.getDefault(), "%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    // ==== 6面板（Activity版用默认面板，主流程走Fragment）====
    private void initQuizPanels() {
        String[][] panels = {{"☕","Java"},{"📊","数据结构"},{"🌐","计算机网络"},
                {"🗄️","数据库"},{"🇨🇳","思政通识"},{"📚","人文通识"}};
        int[] ids = {R.id.quiz_panel_1, R.id.quiz_panel_2, R.id.quiz_panel_3,
                R.id.quiz_panel_4, R.id.quiz_panel_5, R.id.quiz_panel_6};
        for (int i = 0; i < ids.length; i++) {
            FrameLayout frame = findViewById(ids[i]);
            if (frame == null) continue;
            String[] p = panels[i];
            LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setGravity(Gravity.CENTER);
            TextView ic = new TextView(this); ic.setText(p[0]); ic.setTextSize(28); ic.setGravity(Gravity.CENTER);
            TextView t = new TextView(this); t.setText(p[1]); t.setTextSize(13);
            t.setTextColor(Color.parseColor("#1D1D1F")); t.setGravity(Gravity.CENTER); t.setPadding(0, 8, 0, 0);
            c.addView(ic); c.addView(t);
            frame.addView(c, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            final String name = p[1];
            frame.setOnClickListener(v -> showStartConfirm(name));
        }
    }

    // ==== 答题 ====
    private void setupQuizPager() {
        quizAdapter = new QuizPagerAdapter(quizQuestions);
        quizAdapter.setOnAnswerListener(new QuizPagerAdapter.OnAnswerListener() {
            @Override public void onAnswered(int pos, String a) {
                recordTime(pos);
                if (pos + 1 < quizQuestions.size()) viewPagerQuiz.setCurrentItem(pos + 1, true);
            }
            @Override public void onAutoSkip(int pos) {}
        });
        viewPagerQuiz.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPagerQuiz.setAdapter(quizAdapter);
        viewPagerQuiz.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int pos) {
                if (lastPageIndex >= 0) recordTime(lastPageIndex);
                lastPageIndex = pos;
                lastPageEntryTime = System.currentTimeMillis();
                QuizQuestion prev = pos > 0 ? quizQuestions.get(pos - 1) : null;
                if (prev != null && prev.getUserAnswer() == null && prev.getDurationSec() > 30)
                    prev.setUserAnswer("不会");
            }
        });
    }

    private void recordTime(int pos) {
        if (pos >= 0 && pos < quizQuestions.size() && lastPageEntryTime > 0) {
            quizQuestions.get(pos).setDurationSec(quizQuestions.get(pos).getDurationSec() + (int)((System.currentTimeMillis() - lastPageEntryTime) / 1000));
        }
    }

    private void showStartConfirm(String subject) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("开始学习")
                .setMessage("是否开始学习「" + subject + "」？")
                .setPositiveButton("是", (d, w) -> startQuiz(subject))
                .setNegativeButton("否", null)
                .show();
    }

    private void startQuiz(String subject) {
        android.app.ProgressDialog loading = new android.app.ProgressDialog(this);
        loading.setMessage("正在生成「" + subject + "」题目...");
        loading.setCancelable(false);
        loading.show();
        Map<String, String> body = new HashMap<>();
        body.put("subject", subject);
        body.put("subjectType", subject.contains("思政") || subject.contains("人文") ? "公共" : "专业");
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.generateQuiz(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> resp) {
                loading.dismiss();
                if (resp.isSuccessful() && resp.body() != null && resp.body().get("questions") != null) {
                    Long sid = resp.body().get("sessionId") instanceof Number
                            ? ((Number) resp.body().get("sessionId")).longValue() : null;
                    latestSessionId = sid;
                    handler.post(() -> {
                        quizQuestions.clear();
                        List<Map<String, Object>> qList = (List<Map<String, Object>>) resp.body().get("questions");
                        for (Map<String, Object> qm : qList) {
                            QuizQuestion q = new QuizQuestion();
                            q.setQuestionIndex(qList.indexOf(qm) + 1);
                            q.setQuestionType((String) qm.get("questionType"));
                            q.setSubject((String) qm.get("subject"));
                            q.setQuestion((String) qm.get("question"));
                            q.setOptions((List<String>) qm.get("options"));
                            q.setCorrectAnswer((String) qm.get("correctAnswer"));
                            quizQuestions.add(q);
                        }
                        quizAdapter.notifyDataSetChanged();
                        llPanels.setVisibility(View.GONE);
                        llQuizArea.setVisibility(View.VISIBLE);
                        lastPageIndex = 0; lastPageEntryTime = System.currentTimeMillis();
                    });
                } else {
                    handler.post(() -> Toast.makeText(FocusActivity.this, "题目生成失败", Toast.LENGTH_SHORT).show());
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                loading.dismiss();
                handler.post(() -> Toast.makeText(FocusActivity.this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void submitQuiz() {
        recordTime(lastPageIndex);
        for (QuizQuestion q : quizQuestions) if (q.getUserAnswer() == null) q.setUserAnswer("不会");
        List<Map<String, Object>> answers = new ArrayList<>();
        for (QuizQuestion q : quizQuestions) {
            Map<String, Object> a = new HashMap<>();
            a.put("questionIndex", q.getQuestionIndex());
            a.put("questionType", q.getQuestionType());
            a.put("subject", q.getSubject());
            a.put("question", q.getQuestion());
            a.put("options", q.getOptions());
            a.put("userAnswer", q.getUserAnswer() != null ? q.getUserAnswer() : "不会");
            a.put("correctAnswer", q.getCorrectAnswer());
            a.put("durationSec", q.getDurationSec());
            a.put("modifiedCount", q.getModifiedCount());
            answers.add(a);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("sessionId", latestSessionId);
        body.put("answers", answers);
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.evaluateQuiz(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> resp) {
                handler.post(() -> {
                    quizQuestions.clear(); quizAdapter.notifyDataSetChanged();
                    llQuizArea.setVisibility(View.GONE); llPanels.setVisibility(View.VISIBLE);
                    loadTodayTotal(); Toast.makeText(FocusActivity.this, "测评完成！", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                handler.post(() -> Toast.makeText(FocusActivity.this, "提交失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ==== 服务器交互 ====
    private void saveToServer(int sec) {
        Map<String, Object> b = new HashMap<>();
        b.put("durationSeconds", sec);
        b.put("startedAt", startTime != null ? startTime
                : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        .format(new Date(System.currentTimeMillis() - sec * 1000L)));
        b.put("finishedAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date()));
        RetrofitClient.getInstance().create(ApiService.class).saveFocus(token, b).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    Object t = r.body().get("todayTotal");
                    if (t instanceof Number) handler.post(() -> tvToday.setText("今日专注 " + formatDuration(((Number) t).intValue())));
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {}
        });
        seconds = 0;
    }

    private void loadTodayTotal() {
        RetrofitClient.getInstance().create(ApiService.class).getFocusToday(token).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    Object t = r.body().get("totalSeconds");
                    if (t instanceof Number) handler.post(() -> tvToday.setText("今日专注 " + formatDuration(((Number) t).intValue())));
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {}
        });
    }

    private void showLastSession() {
        RetrofitClient.getInstance().create(ApiService.class).getLastFocus(token).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    Object s = r.body().get("seconds");
                    if (s instanceof Number && tvLast != null) {
                        int sec = ((Number) s).intValue();
                        if (sec > 0) handler.post(() -> tvLast.setText("上次 " + formatDuration(sec)));
                    }
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {}
        });
    }

    private void updateStatus(String s) {
        Map<String, String> b = new HashMap<>(); b.put("status", s);
        RetrofitClient.getInstance().create(ApiService.class).updateFocusStatus(token, b).enqueue(emptyCb());
    }

    private Callback<Map<String, Object>> emptyCb() { return new Callback<Map<String, Object>>() {
        @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {}
        @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {}
    };}
}
