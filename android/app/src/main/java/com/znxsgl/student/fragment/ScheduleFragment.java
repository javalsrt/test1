package com.znxsgl.student.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.znxsgl.student.R;
import com.znxsgl.student.model.ScheduleItem;
import com.znxsgl.student.network.ApiService;
import com.znxsgl.student.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleFragment extends Fragment {

    private TextView tvWeekLabel, tvDateInfo, btnToday;
    private LinearLayout containerWeeks, gridBody;
    private LinearLayout rowHeader;

    private int currentWeek = 13;
    private List<ScheduleItem> apiScheduleData = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ========== 常量 ==========
    private static final String[] DAY_NAMES = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final int[] SLOT_WEIGHTS = {2, 2, 1, 2, 2, 3};

    private static final String[][] SLOTS = {
        {"第一大节", "8:30", "9:55"},
        {"第二大节", "10:10", "11:35"},
        {"第三大节", "11:40", "12:20"},
        {"第四大节", "14:20", "15:45"},
        {"第五大节", "16:00", "17:25"},
        {"第六大节", "19:10", "21:30"},
    };

    private static final int[] SLOT_COLORS = {
        0xFFE8F0FE, 0xFFE8F8ED, 0xFFFFF3E0,
        0xFFF3E8FF, 0xFFFCE4EC, 0xFFE0F7FA,
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        tvWeekLabel = view.findViewById(R.id.tv_week_label);
        tvDateInfo = view.findViewById(R.id.tv_date_info);
        btnToday = view.findViewById(R.id.btn_today);
        containerWeeks = view.findViewById(R.id.container_weeks);
        gridBody = view.findViewById(R.id.grid_body);
        rowHeader = view.findViewById(R.id.row_header);

        tvDateInfo.setText(String.format(Locale.CHINA, "%d月%d日", 5, 24));

        tvWeekLabel.setOnClickListener(v -> showWeekPicker());
        btnToday.setOnClickListener(v -> { currentWeek = 13; refreshAll(); });

        buildHeader();
        buildWeekSelector();
        fetchSchedule(currentWeek);

        view.setOnTouchListener(new View.OnTouchListener() {
            private float downX;
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: downX = event.getX(); return true;
                    case MotionEvent.ACTION_UP:
                        float dx = event.getX() - downX;
                        if (Math.abs(dx) > 80) {
                            if (dx < 0 && currentWeek < 18) currentWeek++;
                            else if (dx > 0 && currentWeek > 1) currentWeek--;
                            refreshAll();
                        }
                        return true;
                }
                return false;
            }
        });
        return view;
    }

    // ========== API 请求 ==========
    private void fetchSchedule(int week) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("znxsgl", 0);
        String token = prefs.getString("token", "");

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getStudentSchedule("Bearer " + token, week).enqueue(new Callback<List<ScheduleItem>>() {
            @Override
            public void onResponse(Call<List<ScheduleItem>> call, Response<List<ScheduleItem>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    apiScheduleData = resp.body();
                } else {
                    apiScheduleData = new ArrayList<>();
                }
                mainHandler.post(() -> buildScheduleGrid());
            }
            @Override
            public void onFailure(Call<List<ScheduleItem>> call, Throwable t) {
                apiScheduleData = new ArrayList<>();
                mainHandler.post(() -> {
                    Toast.makeText(getContext(), "无法连接服务器，显示静态课表", Toast.LENGTH_SHORT).show();
                    buildScheduleGrid();
                });
            }
        });
    }

    // ========== 表头 ==========
    private void buildHeader() {
        rowHeader.removeAllViews();
        TextView timeHeader = createHeaderCell("节次", 0xFF86868B, dp(36));
        timeHeader.setBackgroundColor(0xFFF5F5F7);
        rowHeader.addView(timeHeader);
        for (String day : DAY_NAMES) {
            rowHeader.addView(createHeaderCell(day, 0xFF1D1D1F, 0));
        }
    }

    private TextView createHeaderCell(String text, int color, int fixedWidth) {
        TextView tv = new TextView(getContext());
        if (fixedWidth > 0) {
            tv.setLayoutParams(new LinearLayout.LayoutParams(fixedWidth, dp(36)));
        } else {
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, dp(36), 1));
        }
        tv.setText(text); tv.setTextSize(11);
        tv.setTextColor(color); tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    // ========== 周选择器 ==========
    private void buildWeekSelector() {
        containerWeeks.removeAllViews();
        tvWeekLabel.setText("第" + currentWeek + "周");
        for (int w = 1; w <= 18; w++) {
            final int week = w;
            TextView tv = new TextView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(36), dp(30));
            lp.setMargins(dp(2), 0, dp(2), 0);
            tv.setLayoutParams(lp);
            tv.setText(String.valueOf(w)); tv.setTextSize(13); tv.setGravity(Gravity.CENTER);
            if (w == currentWeek) {
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(0xFF0A84FF); bg.setCornerRadius(dp(15));
                tv.setBackground(bg); tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackground(null); tv.setTextColor(0xFF86868B);
            }
            tv.setOnClickListener(v -> { currentWeek = week; refreshAll(); });
            containerWeeks.addView(tv);
        }
    }

    // ========== 课表网格 ==========
    private void buildScheduleGrid() {
        gridBody.removeAllViews();

        // 上午
        for (int s = 0; s < 3; s++) {
            gridBody.addView(buildSlotRow(s));
            gridBody.addView(createHairline());
        }
        // 午休分隔
        gridBody.addView(buildBreakRow());
        gridBody.addView(createHairline());
        // 下午+晚
        for (int s = 3; s < 6; s++) {
            gridBody.addView(buildSlotRow(s));
            gridBody.addView(createHairline());
        }
    }

    private LinearLayout buildSlotRow(int slotIndex) {
        String start = SLOTS[slotIndex][1];
        String end = SLOTS[slotIndex][2];
        int weight = SLOT_WEIGHTS[slotIndex];

        LinearLayout row = new LinearLayout(getContext());
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, weight);
        rp.setMargins(0, dp(2), 0, dp(2));
        row.setLayoutParams(rp); row.setOrientation(LinearLayout.HORIZONTAL);

        // 时间列
        LinearLayout timeCol = new LinearLayout(getContext());
        timeCol.setLayoutParams(new LinearLayout.LayoutParams(dp(36),
                LinearLayout.LayoutParams.MATCH_PARENT));
        timeCol.setOrientation(LinearLayout.VERTICAL);
        timeCol.setGravity(Gravity.CENTER);
        timeCol.setPadding(dp(2), dp(2), dp(2), dp(2));
        GradientDrawable timeBg = new GradientDrawable();
        timeBg.setColor(0xFFF5F5F7); timeBg.setCornerRadius(dp(6));
        timeCol.setBackground(timeBg);

        TextView tvStart = new TextView(getContext());
        tvStart.setText(start); tvStart.setTextSize(9);
        tvStart.setTextColor(0xFF1D1D1F); tvStart.setGravity(Gravity.CENTER);
        timeCol.addView(tvStart);

        TextView tvEnd = new TextView(getContext());
        tvEnd.setText(end); tvEnd.setTextSize(8);
        tvEnd.setTextColor(0xFF86868B); tvEnd.setGravity(Gravity.CENTER);
        timeCol.addView(tvEnd);
        row.addView(timeCol);

        // 7天课程格
        for (int d = 1; d <= 7; d++) {
            row.addView(buildCourseCell(d, start, end, slotIndex));
        }
        return row;
    }

    private LinearLayout buildCourseCell(int dayOfWeek, String slotStart, String slotEnd, int slotIndex) {
        LinearLayout cell = new LinearLayout(getContext());
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        cp.setMargins(dp(1), 0, dp(1), 0);
        cell.setLayoutParams(cp);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(2), dp(2), dp(2), dp(2));

        // 查找匹配的课程
        String courseName = null;
        String classroom = null;
        for (ScheduleItem item : apiScheduleData) {
            if (item.getDayOfWeek() == dayOfWeek && item.matchesTimeSlot(slotStart, slotEnd)) {
                courseName = item.getCourseName();
                classroom = item.getClassroom();
                break;
            }
        }

        if (courseName != null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(SLOT_COLORS[slotIndex % SLOT_COLORS.length]);
            bg.setCornerRadius(dp(6));
            cell.setBackground(bg);

            TextView tvName = new TextView(getContext());
            tvName.setText(courseName); tvName.setTextSize(10);
            tvName.setTextColor(0xFF1D1D1F); tvName.setGravity(Gravity.CENTER);
            tvName.setMaxLines(2);
            cell.addView(tvName);

            if (classroom != null && !classroom.isEmpty()) {
                TextView tvRoom = new TextView(getContext());
                // 截短教室名
                String shortRoom = classroom.length() > 10 ? classroom.substring(0, 9) + "…" : classroom;
                tvRoom.setText(shortRoom); tvRoom.setTextSize(8);
                tvRoom.setTextColor(0xFF86868B); tvRoom.setGravity(Gravity.CENTER);
                tvRoom.setMaxLines(1);
                cell.addView(tvRoom);
            }
        } else {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFFFAFAFA); bg.setCornerRadius(dp(6));
            cell.setBackground(bg);
        }
        return cell;
    }

    private LinearLayout buildBreakRow() {
        LinearLayout row = new LinearLayout(getContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(26)));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), 0, dp(8), 0);

        View spacer = new View(getContext());
        spacer.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(1)));
        row.addView(spacer);

        LinearLayout sep = new LinearLayout(getContext());
        sep.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        sep.setGravity(Gravity.CENTER);
        sep.setOrientation(LinearLayout.HORIZONTAL);

        View lineL = new View(getContext());
        lineL.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(1)));
        lineL.setBackgroundColor(0xFFFF9500);
        sep.addView(lineL);

        TextView tv = new TextView(getContext());
        tv.setText(" 午餐·午休 12:20 — 14:15 ");
        tv.setTextSize(10); tv.setTextColor(0xFFFF9500); tv.setGravity(Gravity.CENTER);
        sep.addView(tv);

        View lineR = new View(getContext());
        lineR.setLayoutParams(new LinearLayout.LayoutParams(0, dp(1), 1));
        lineR.setBackgroundColor(0xFFFF9500);
        sep.addView(lineR);

        row.addView(sep);
        return row;
    }

    private View createHairline() {
        View v = new View(getContext());
        v.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
        v.setBackgroundColor(0xFFF0F0F3);
        return v;
    }

    // ========== 周数选择弹窗 ==========
    private void showWeekPicker() {
        GridLayout grid = new GridLayout(getContext());
        grid.setColumnCount(6);
        grid.setPadding(dp(16), dp(16), dp(16), dp(16));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("选择周数").setView(grid)
                .setPositiveButton("关闭", null).create();

        for (int w = 1; w <= 18; w++) {
            final int week = w;
            TextView tv = new TextView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dp(44); params.height = dp(40);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            tv.setLayoutParams(params);
            tv.setText(String.valueOf(w)); tv.setTextSize(16); tv.setGravity(Gravity.CENTER);
            if (w == currentWeek) {
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(0xFF0A84FF); bg.setCornerRadius(dp(20));
                tv.setBackground(bg); tv.setTextColor(Color.WHITE);
            } else {
                tv.setTextColor(0xFF1D1D1F);
            }
            final AlertDialog d = dialog;
            tv.setOnClickListener(v -> { currentWeek = week; d.dismiss(); refreshAll(); });
            grid.addView(tv);
        }
        dialog.show();
    }

    private void refreshAll() {
        buildWeekSelector();
        fetchSchedule(currentWeek);
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
