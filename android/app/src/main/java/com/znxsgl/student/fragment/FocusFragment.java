package com.znxsgl.student.fragment;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.znxsgl.student.QuizPagerAdapter;
import com.znxsgl.student.R;
import com.znxsgl.student.model.QuizQuestion;
import com.znxsgl.student.network.ApiService;
import com.znxsgl.student.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FocusFragment extends Fragment {

    private TextView tvTimerCorner, tvToday, tvLast;
    private View llPanels, llQuizArea;
    private ViewPager2 viewPagerQuiz;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int seconds;
    private boolean running;
    private String startTime, token;

    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private QuizPagerAdapter quizAdapter;
    private long lastPageEntryTime;
    private int lastPageIndex = -1;
    private Long latestSessionId;

    private static final String[][] PANELS = {
            {"☕","Java基础设计"},{"📊","数据结构"},{"🌐","计算机网络"},
            {"🗄️","数据库"},{"🇨🇳","思政通识"},{"📚","人文通识"},
    };

    private final Runnable tick = () -> { seconds++; if (tvTimerCorner != null) tvTimerCorner.setText(formatDuration(seconds)); handler.postDelayed(this.tick, 1000); };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_focus, container, false);
        try {
            SharedPreferences prefs = requireActivity().getSharedPreferences("znxsgl", 0);
            token = "Bearer " + prefs.getString("token", "");

            View backBtn = view.findViewById(R.id.btn_back);
            if (backBtn != null) backBtn.setVisibility(View.GONE);

            tvTimerCorner = view.findViewById(R.id.tv_timer_corner);
            tvToday = view.findViewById(R.id.tv_today);
            tvLast = view.findViewById(R.id.tv_last);
            llPanels = view.findViewById(R.id.ll_panels);
            llQuizArea = view.findViewById(R.id.ll_quiz_area);
            viewPagerQuiz = view.findViewById(R.id.viewpager_quiz);

            View llInfo = view.findViewById(R.id.ll_info);
            if (llInfo != null) llInfo.setVisibility(View.GONE);

            // 初始化6面板（简化为文字）
            initPanels(view);

            // 初始化答题区（加 try-catch 保护）
            try {
                quizAdapter = new QuizPagerAdapter(quizQuestions);
                quizAdapter.setOnAnswerListener(new QuizPagerAdapter.OnAnswerListener() {
                    @Override public void onAnswered(int pos, String a) {
                        recordTime(pos);
                        if (viewPagerQuiz != null && pos + 1 < quizQuestions.size())
                            viewPagerQuiz.setCurrentItem(pos + 1, true);
                    }
                    @Override public void onAutoSkip(int pos) {}
                });
                if (viewPagerQuiz != null) {
                    viewPagerQuiz.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
                    viewPagerQuiz.setAdapter(quizAdapter);
                    viewPagerQuiz.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override public void onPageSelected(int pos) {
                            if (lastPageIndex >= 0) recordTime(lastPageIndex);
                            lastPageIndex = pos; lastPageEntryTime = System.currentTimeMillis();
                        }
                    });
                }
                View btnDone = view.findViewById(R.id.btn_quiz_done);
                if (btnDone != null) btnDone.setOnClickListener(v -> submitQuiz());
            } catch (Exception e) {
                // 答题组件初始化失败不影响基本功能
            }

            loadTodayTotal();
            showLastSession();
        } catch (Exception e) {
            Toast.makeText(getContext(), "初始化异常: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return view;
    }

    private void initPanels(View view) {
        int[] ids = {R.id.quiz_panel_1, R.id.quiz_panel_2, R.id.quiz_panel_3,
                R.id.quiz_panel_4, R.id.quiz_panel_5, R.id.quiz_panel_6};
        for (int i = 0; i < ids.length; i++) {
            FrameLayout frame = view.findViewById(ids[i]);
            if (frame == null) continue;
            LinearLayout c = new LinearLayout(getContext());
            c.setOrientation(LinearLayout.VERTICAL); c.setGravity(Gravity.CENTER);
            TextView ic = new TextView(getContext()); ic.setText(PANELS[i][0]); ic.setTextSize(28); ic.setGravity(Gravity.CENTER);
            TextView t = new TextView(getContext()); t.setText(PANELS[i][1]); t.setTextSize(12);
            t.setTextColor(Color.parseColor("#1D1D1F")); t.setGravity(Gravity.CENTER); t.setPadding(0, 6, 0, 0);
            c.addView(ic); c.addView(t);
            frame.addView(c, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            final String name = PANELS[i][1];
            frame.setOnClickListener(v -> startQuiz(name));
        }
    }

    private void recordTime(int pos) {
        if (pos >= 0 && pos < quizQuestions.size() && lastPageEntryTime > 0) {
            QuizQuestion q = quizQuestions.get(pos);
            q.setDurationSec(q.getDurationSec() + (int)((System.currentTimeMillis() - lastPageEntryTime) / 1000));
        }
    }

    private void startQuiz(String subject) {
        if (!isAdded()) return;
        Toast.makeText(getContext(), "正在生成 " + subject + " 题目...", Toast.LENGTH_SHORT).show();
        Map<String, String> b = new HashMap<>();
        b.put("subject", subject);
        b.put("subjectType", subject.contains("思政") || subject.contains("人文") ? "公共" : "专业");
        RetrofitClient.getInstance().create(ApiService.class).generateQuiz(token, b).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null && r.body().get("questions") != null) {
                    Object sid = r.body().get("sessionId");
                    if (sid instanceof Number) latestSessionId = ((Number) sid).longValue();
                    handler.post(() -> {
                        quizQuestions.clear();
                        for (Map<String, Object> qm : (List<Map<String, Object>>) r.body().get("questions")) {
                            QuizQuestion q = new QuizQuestion();
                            q.setQuestionType((String) qm.get("questionType"));
                            q.setSubject((String) qm.get("subject"));
                            q.setQuestion((String) qm.get("question"));
                            q.setOptions((List<String>) qm.get("options"));
                            q.setCorrectAnswer((String) qm.get("correctAnswer"));
                            quizQuestions.add(q);
                        }
                        quizAdapter.notifyDataSetChanged();
                        if (llPanels != null) llPanels.setVisibility(View.GONE);
                        if (llQuizArea != null) llQuizArea.setVisibility(View.VISIBLE);
                        lastPageIndex = 0; lastPageEntryTime = System.currentTimeMillis();
                    });
                } else {
                    handler.post(() -> Toast.makeText(getContext(), "题目生成失败", Toast.LENGTH_SHORT).show());
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {}
        });
    }

    private void submitQuiz() {
        recordTime(lastPageIndex);
        for (QuizQuestion q : quizQuestions) if (q.getUserAnswer() == null) q.setUserAnswer("不会");
        List<Map<String, Object>> answers = new ArrayList<>();
        for (QuizQuestion q : quizQuestions) {
            Map<String, Object> a = new HashMap<>();
            a.put("questionType", q.getQuestionType()); a.put("question", q.getQuestion());
            a.put("userAnswer", q.getUserAnswer() != null ? q.getUserAnswer() : "不会");
            a.put("correctAnswer", q.getCorrectAnswer());
            a.put("durationSec", q.getDurationSec()); a.put("modifiedCount", q.getModifiedCount());
            answers.add(a);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("sessionId", latestSessionId); body.put("answers", answers);
        RetrofitClient.getInstance().create(ApiService.class).evaluateQuiz(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {
                if (!isAdded()) return;
                handler.post(() -> {
                    quizQuestions.clear(); quizAdapter.notifyDataSetChanged();
                    if (llQuizArea != null) llQuizArea.setVisibility(View.GONE);
                    if (llPanels != null) llPanels.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "测评完成", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {}
        });
    }

    @Override public void onResume() {
        super.onResume();
        if (llQuizArea == null || llQuizArea.getVisibility() != View.VISIBLE)
            handler.postDelayed(this::autoStart, 3000);
    }

    @Override public void onPause() { super.onPause(); stopAndSave(); }

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) stopAndSave();
        else {
            loadTodayTotal(); showLastSession();
            if (llQuizArea == null || llQuizArea.getVisibility() != View.VISIBLE)
                handler.postDelayed(this::autoStart, 3000);
        }
    }

    private void autoStart() {
        if (running) return;
        running = true;
        if (seconds == 0) startTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
        View v = getView();
        if (v != null) { View li = v.findViewById(R.id.ll_info); if (li != null) li.setVisibility(View.VISIBLE); }
        handler.post(tick);
        updateStatus("focusing");
    }

    private void stopAndSave() {
        handler.removeCallbacks(this::autoStart);
        if (running) { running = false; handler.removeCallbacks(tick); updateStatus("idle"); }
        if (seconds >= 1) { saveToServer(seconds); }
    }

    private String formatDuration(int s) {
        if (s < 3600) return String.format(Locale.getDefault(), "%02d:%02d", s / 60, s % 60);
        return String.format(Locale.getDefault(), "%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    private void saveToServer(int sec) {
        Map<String, Object> b = new HashMap<>();
        b.put("durationSeconds", sec);
        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
        b.put("startedAt", startTime != null ? startTime : now);
        b.put("finishedAt", now);
        RetrofitClient.getInstance().create(ApiService.class).saveFocus(token, b).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    Object t = r.body().get("todayTotal");
                    if (t instanceof Number && tvToday != null)
                        handler.post(() -> tvToday.setText("今日专注 " + formatDuration(((Number) t).intValue())));
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
                    if (t instanceof Number && tvToday != null)
                        handler.post(() -> tvToday.setText("今日专注 " + formatDuration(((Number) t).intValue())));
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {}
        });
    }

    private void showLastSession() {
        RetrofitClient.getInstance().create(ApiService.class).getLastFocus(token).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {
                if (r.isSuccessful() && r.body() != null && tvLast != null) {
                    Object s = r.body().get("seconds");
                    if (s instanceof Number) {
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

    private Callback<Map<String, Object>> emptyCb() {
        return new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {}
            @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {}
        };
    }
}
