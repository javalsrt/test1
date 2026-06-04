package com.znxsgl.student.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.HorizontalScrollView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private HorizontalScrollView scrollGrid;

    private int currentWeek = 13;
    private int todayDayOfWeek = 1; // 1=周一 ... 7=周日
    private boolean isAnimating = false;
    private List<ScheduleItem> apiScheduleData = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ========== 常量 ==========
    private static final String[] DAY_NAMES = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final int[] SLOT_WEIGHTS = {2, 2, 1, 2, 2, 2, 1};

    private static final String[][] SLOTS = {
        {"第一大节", "08:30", "09:55"},   // (01,02小节)
        {"第二大节", "10:10", "11:35"},   // (03,04小节)
        {"第三大节", "11:40", "12:20"},   // (05小节)
        {"第四大节", "14:20", "15:45"},   // (06,07小节)
        {"第五大节", "16:00", "17:25"},   // (08,09小节)
        {"第六大节", "19:10", "20:40"},   // (10,11,12小节)
        {"第七大节", "20:50", "21:30"},   // (13小节)
    };

    private static final int[] COURSE_COLORS = {
        0xFFE8F0FE, // 浅蓝
        0xFFE8F8ED, // 浅绿
        0xFFFFF3E0, // 浅橙
        0xFFF3E8FF, // 浅紫
        0xFFFCE4EC, // 浅粉
        0xFFE0F7FA, // 浅青
        0xFFF9F0E0, // 浅黄
        0xFFEDE7F6, // 淡紫
        0xFFE8EAF6, // 靛蓝
        0xFFFBE9E7, // 淡红
        0xFFE0F2F1, // 浅墨绿
        0xFFFFF8E1, // 奶油黄
    };

    private static final int[] SLOT_COLORS = {
        0xFFE8F0FE, 0xFFE8F8ED, 0xFFFFF3E0,
        0xFFF3E8FF, 0xFFFCE4EC, 0xFFE0F7FA,
        0xFFF5F5F7,
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
        scrollGrid = view.findViewById(R.id.scroll_grid);

        // 计算真实日期和周次
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        todayDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 6) % 7; // 周日=0, 周一=1...
        if (todayDayOfWeek == 0) todayDayOfWeek = 7; // 周日=7
        // 学期起始: 2026-02-16 (周一)
        Calendar startCal = Calendar.getInstance();
        startCal.set(2026, 2, 9); // 3月9日(学期第一周周一)
        long diffMs = cal.getTimeInMillis() - startCal.getTimeInMillis();
        int diffDays = (int) (diffMs / (1000 * 60 * 60 * 24));
        currentWeek = Math.max(1, Math.min(18, (diffDays / 7) + 1));
        
        tvDateInfo.setText(String.format(Locale.CHINA, "%d月%d日", month, day));

        tvWeekLabel.setOnClickListener(v -> showWeekPicker());
        btnToday.setOnClickListener(v -> { currentWeek = getCurrentWeek(); fetchSchedule(currentWeek); });

        buildHeader();
        buildWeekSelector();
        fetchSchedule(currentWeek);

        // 左右滑动切换周次（在 ScrollView 上检测，优先级高于滚动）
        scrollGrid.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            private boolean isSwiping = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        isSwiping = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(event.getX() - startX);
                        float dy = Math.abs(event.getY() - startY);
                        // 确认是横向滑动后，禁止 ScrollView 拦截
                        if (!isSwiping && dx > dy && dx > 20) {
                            isSwiping = true;
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        if (isSwiping) return true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        float totalDx = event.getX() - startX;
                        if (isSwiping && Math.abs(totalDx) > 60) {
                            if (totalDx < 0 && currentWeek < 18) animateWeekChange(1);
                            else if (totalDx > 0 && currentWeek > 1) animateWeekChange(-1);
                        }
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        isSwiping = false;
                        break;
                }
                return isSwiping;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // ========== 滑动切换动画 ==========
    private void animateWeekChange(int direction) {
        if (isAnimating || scrollGrid == null || scrollGrid.getWidth() <= 0) {
            // 降级：无动画
            currentWeek += direction;
            refreshAll();
            return;
        }
        isAnimating = true;
        View content = scrollGrid.getChildAt(0);
        int w = scrollGrid.getWidth();

        content.animate()
            .translationX(-direction * w * 0.5f)
            .alpha(0.3f)
            .setDuration(120)
            .setListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator a) {
                    currentWeek += direction;
                    buildHeader();
                    buildWeekSelector();
                    // 预置到对面
                    content.setTranslationX(direction * w * 0.5f);
                    fetchScheduleAnimated(currentWeek, content, direction, w);
                }
            }).start();
    }

    // ========== API 请求 ==========
    private void fetchSchedule(int week) {
        fetchScheduleAnimated(week, null, 0, 0);
    }

    private void fetchScheduleAnimated(int week, View content, int direction, int width) {
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
                mainHandler.post(() -> {
                    buildScheduleGrid();
                    if (content != null) {
                        content.animate()
                            .translationX(0).alpha(1f)
                            .setDuration(150)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override public void onAnimationEnd(Animator a) {
                                    isAnimating = false;
                                }
                            }).start();
                    }
                });
            }
            @Override
            public void onFailure(Call<List<ScheduleItem>> call, Throwable t) {
                apiScheduleData = new ArrayList<>();
                mainHandler.post(() -> {
                    Toast.makeText(getContext(), "无法连接服务器", Toast.LENGTH_SHORT).show();
                    buildScheduleGrid();
                    if (content != null) {
                        content.animate().translationX(0).alpha(1f).setDuration(150)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override public void onAnimationEnd(Animator a) { isAnimating = false; }
                            }).start();
                    }
                });
            }
        });
    }

    // ========== 计算当前周 ==========
    private int getCurrentWeek() {
        Calendar cal = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.set(2026, 2, 9); // 3月9日
        long diff = cal.getTimeInMillis() - start.getTimeInMillis();
        int days = (int) (diff / (1000 * 60 * 60 * 24));
        return Math.max(1, Math.min(18, (days / 7) + 1));
    }

    // ========== 表头 ==========
    private void buildHeader() {
        rowHeader.removeAllViews();
        int todayIdx = todayDayOfWeek - 1; // 0索引
        // 节次列
        TextView timeHeader = createHeaderCell("节次", 0xFF86868B, dp(36));
        timeHeader.setBackgroundColor(0xFFF5F5F7);
        rowHeader.addView(timeHeader);
        
        // 周一到周日，带日期和高亮
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("M/d", Locale.CHINA);
        // 找到本周一的日期
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_MONTH, -(dow == 1 ? 6 : dow - 2)); // 移到周一
        
        for (int i = 0; i < 7; i++) {
            String dateStr = sdf.format(cal.getTime());
            String label = DAY_NAMES[i] + "\n" + dateStr;
            boolean isToday = (currentWeek == getCurrentWeek()) && (i == todayIdx);
            
            LinearLayout cell = new LinearLayout(getContext());
            cell.setLayoutParams(new LinearLayout.LayoutParams(0, dp(36), 1));
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setBackgroundColor(isToday ? 0xFFE8F0FE : 0x00000000);
            cell.setPadding(0, dp(2), 0, dp(2));
            
            TextView tvDay = new TextView(getContext());
            tvDay.setText(DAY_NAMES[i]);
            tvDay.setTextSize(11);
            tvDay.setTextColor(isToday ? 0xFF5E6AD2 : 0xFF1D1D1F);
            tvDay.setGravity(Gravity.CENTER);
            tvDay.setTypeface(null, Typeface.BOLD);
            cell.addView(tvDay);
            
            TextView tvDate = new TextView(getContext());
            tvDate.setText(dateStr);
            tvDate.setTextSize(8);
            tvDate.setTextColor(isToday ? 0xFF5E6AD2 : 0xFF86868B);
            tvDate.setGravity(Gravity.CENTER);
            cell.addView(tvDate);
            
            rowHeader.addView(cell);
            cal.add(Calendar.DAY_OF_MONTH, 1);
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

        // 上午 (slot 0-2)
        for (int s = 0; s < 3; s++) {
            gridBody.addView(buildSlotRow(s));
            gridBody.addView(createHairline());
        }
        // 午休分隔
        gridBody.addView(buildBreakRow(" 午餐·午休 12:20 — 14:15 "));
        gridBody.addView(createHairline());
        // 下午 (slot 3-4)
        for (int s = 3; s < 5; s++) {
            gridBody.addView(buildSlotRow(s));
            gridBody.addView(createHairline());
        }
        // 晚餐分隔
        gridBody.addView(buildBreakRow(" 晚餐·晚休 17:25 — 19:10 "));
        gridBody.addView(createHairline());
        // 晚上 (slot 5-6)
        for (int s = 5; s < 7; s++) {
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

        // 只在当前周高亮今天列
        boolean isToday = (currentWeek == getCurrentWeek()) && (dayOfWeek == todayDayOfWeek);
        if (isToday) {
            GradientDrawable todayBg = new GradientDrawable();
            todayBg.setColor(0xFFF0F4FF);
            todayBg.setCornerRadius(dp(4));
            cell.setBackground(todayBg);
        }

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
            int baseColor = COURSE_COLORS[Math.abs(courseName.hashCode()) % COURSE_COLORS.length];
            
            if (currentWeek == getCurrentWeek() && dayOfWeek == todayDayOfWeek) {
                // 今天列：左边加蓝条 + 课程颜色卡片
                cell.setOrientation(LinearLayout.HORIZONTAL);
                View strip = new View(getContext());
                strip.setLayoutParams(new LinearLayout.LayoutParams(dp(3), LinearLayout.LayoutParams.MATCH_PARENT));
                strip.setBackgroundColor(0xFF5E6AD2);
                cell.addView(strip);
                // 内嵌卡片
                LinearLayout card = new LinearLayout(getContext());
                card.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
                card.setOrientation(LinearLayout.VERTICAL);
                card.setGravity(Gravity.CENTER);
                GradientDrawable cardBg = new GradientDrawable();
                cardBg.setColor(baseColor); cardBg.setCornerRadius(dp(6));
                card.setBackground(cardBg);
                
                TextView tvName = new TextView(getContext());
                tvName.setText(courseName); tvName.setTextSize(10);
                tvName.setTextColor(0xFF1D1D1F); tvName.setGravity(Gravity.CENTER);
                tvName.setMaxLines(2);
                card.addView(tvName);
                if (classroom != null && !classroom.isEmpty()) {
                    String shortRoom = classroom.length() > 10 ? classroom.substring(0, 9) + "…" : classroom;
                    TextView tvRoom = new TextView(getContext());
                    tvRoom.setText(shortRoom); tvRoom.setTextSize(8);
                    tvRoom.setTextColor(0xFF86868B); tvRoom.setGravity(Gravity.CENTER);
                    tvRoom.setMaxLines(1);
                    card.addView(tvRoom);
                }
                cell.addView(card);
            } else {
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(baseColor); bg.setCornerRadius(dp(6));
                cell.setBackground(bg);
                TextView tvName = new TextView(getContext());
                tvName.setText(courseName); tvName.setTextSize(10);
                tvName.setTextColor(0xFF1D1D1F); tvName.setGravity(Gravity.CENTER);
                tvName.setMaxLines(2);
                cell.addView(tvName);
                if (classroom != null && !classroom.isEmpty()) {
                    String shortRoom = classroom.length() > 10 ? classroom.substring(0, 9) + "…" : classroom;
                    TextView tvRoom = new TextView(getContext());
                    tvRoom.setText(shortRoom); tvRoom.setTextSize(8);
                    tvRoom.setTextColor(0xFF86868B); tvRoom.setGravity(Gravity.CENTER);
                    tvRoom.setMaxLines(1);
                    cell.addView(tvRoom);
                }
            }
        } else {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFFFAFAFA); bg.setCornerRadius(dp(6));
            cell.setBackground(bg);
        }
        return cell;
    }

    private LinearLayout buildBreakRow(String text) {
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
        tv.setText(text);
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
        Calendar cal = Calendar.getInstance();
        todayDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 6) % 7;
        if (todayDayOfWeek == 0) todayDayOfWeek = 7;
        buildWeekSelector();
        fetchSchedule(currentWeek);
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
