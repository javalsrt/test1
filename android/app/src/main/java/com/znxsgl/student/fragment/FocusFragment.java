package com.znxsgl.student.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.znxsgl.student.R;
import com.znxsgl.student.network.ApiService;
import com.znxsgl.student.network.RetrofitClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FocusFragment extends Fragment {

    private TextView timerDisk, tvStatus, tvToday, tvLast;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int seconds = 0;
    private int frozenSeconds = 0;
    private boolean running = false;
    private boolean toggleCooldown = false;
    private String startTime;
    private String token;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            seconds++;
            frozenSeconds = seconds;
            updateDisplay();
            handler.postDelayed(this, 1000);
        }
    };

    private final Runnable refreshToday = new Runnable() {
        @Override
        public void run() {
            loadTodayTotal();
            handler.postDelayed(this, 15000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_focus, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("znxsgl", 0);
        token = "Bearer " + prefs.getString("token", "");

        timerDisk = view.findViewById(R.id.timer_disk);
        tvStatus = view.findViewById(R.id.tv_status);
        tvToday = view.findViewById(R.id.tv_today);
        tvLast = view.findViewById(R.id.tv_last);

        // 隐藏返回按钮（Fragment 不需要）
        View backBtn = view.findViewById(R.id.btn_back);
        if (backBtn != null) backBtn.setVisibility(View.GONE);

        timerDisk.setOnClickListener(v -> toggleTimer());

        loadTodayTotal();
        showLastSession();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (running) {
            pauseTimer();
        }
        if (frozenSeconds >= 1) {
            saveToServer(frozenSeconds);
        }
    }

    private void showLastSession() {
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getLastFocus(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    Object secObj = resp.body().get("seconds");
                    if (secObj instanceof Number) {
                        int sec = ((Number) secObj).intValue();
                        if (sec > 0) {
                            handler.post(() -> {
                                tvLast.setText("上次专注 " + formatDuration(sec));
                                tvLast.setVisibility(View.VISIBLE);
                            });
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void toggleTimer() {
        if (toggleCooldown) return;
        toggleCooldown = true;
        handler.postDelayed(() -> toggleCooldown = false, 500);

        timerDisk.animate().scaleX(1.08f).scaleY(1.08f).setDuration(120)
                .withEndAction(() -> timerDisk.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start())
                .start();

        if (!running) {
            startTimer();
        } else {
            pauseTimer();
        }
    }

    private void startTimer() {
        running = true;
        seconds = frozenSeconds;
        if (seconds == 0) {
            startTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        tvStatus.setText("专注中...");
        timerDisk.setTextColor(0xFF1D1D1F);
        timerDisk.setBackgroundResource(R.drawable.bg_timer_circle_active);
        handler.removeCallbacks(tick);
        handler.post(tick);
        handler.removeCallbacks(refreshToday);
        handler.post(refreshToday);
        updateStatus("focusing");
    }

    private void pauseTimer() {
        running = false;
        handler.removeCallbacks(tick);
        handler.removeCallbacks(refreshToday);
        tvStatus.setText("已暂停");
        timerDisk.setBackgroundResource(R.drawable.bg_timer_circle);
        updateDisplay();
        updateStatus("idle");
    }

    private void updateDisplay() {
        timerDisk.setText(formatDuration(frozenSeconds));
    }

    private String formatDuration(int totalSec) {
        if (totalSec < 3600) {
            int m = totalSec / 60;
            int s = totalSec % 60;
            return String.format(Locale.getDefault(), "%02d:%02d", m, s);
        }
        int h = totalSec / 3600;
        int m = (totalSec % 3600) / 60;
        int s = totalSec % 60;
        return String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s);
    }

    private void saveToServer(int finalSeconds) {
        String finishTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Map<String, Object> body = new HashMap<>();
        body.put("durationSeconds", finalSeconds);
        body.put("startedAt", startTime != null ? startTime :
                LocalDateTime.now().minusSeconds(finalSeconds).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        body.put("finishedAt", finishTime);

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.saveFocus(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    Object total = resp.body().get("todayTotal");
                    if (total instanceof Number) {
                        handler.post(() -> tvToday.setText("今日专注 " + formatDuration(((Number) total).intValue())));
                    }
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void updateStatus(String status) {
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.updateFocusStatus(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> resp) {}
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void loadTodayTotal() {
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getFocusToday(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    Object total = resp.body().get("totalSeconds");
                    if (total instanceof Number) {
                        handler.post(() -> tvToday.setText("今日专注 " + formatDuration(((Number) total).intValue())));
                    }
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }
}
