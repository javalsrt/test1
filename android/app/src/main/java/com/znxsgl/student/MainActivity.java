package com.znxsgl.student;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.znxsgl.student.fragment.FocusFragment;
import com.znxsgl.student.fragment.ScheduleFragment;
import com.znxsgl.student.fragment.ProfileFragment;
import com.znxsgl.student.network.RetrofitClient;
import com.znxsgl.student.network.WebSocketManager;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private FocusFragment focusFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 在 MainActivity 生命周期管理 WebSocket，确保整个 App 期间保持连接
        connectWebSocket();

        fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // 答题中切换：先弹窗确认
            if (id != R.id.nav_focus && focusFragment != null && focusFragment.isQuizActive()) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("取消答题？")
                        .setMessage("正在答题中，切换界面将取消本次测试，不记录成绩。")
                        .setPositiveButton("确定取消", (d, w) -> {
                            focusFragment.cancelQuiz();
                            switchFragment(getFragmentById(id));
                        })
                        .setNegativeButton("继续答题", (d, w) -> {
                            // 恢复导航栏选中状态为专注
                            bottomNav.setSelectedItemId(R.id.nav_focus);
                        })
                        .setOnDismissListener(d -> {
                            // 点击外部关闭时也要恢复
                            if (focusFragment != null && focusFragment.isQuizActive()) {
                                bottomNav.setSelectedItemId(R.id.nav_focus);
                            }
                        })
                        .show();
                return true;
            }
            switchFragment(getFragmentById(id));
            return true;
        });

        // 突出"专注"按钮：放大图标 + 上浮效果
        enhanceFocusTab(bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_schedule);
    }

    /** 将"专注"Tab 放大突出显示 —— App 核心功能 */
    private void enhanceFocusTab(BottomNavigationView bottomNav) {
        bottomNav.post(() -> {
            // BottomNavigationView 内部是 BottomNavigationMenuView
            android.view.ViewGroup menuView = (android.view.ViewGroup) bottomNav.getChildAt(0);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                android.view.View tab = menuView.getChildAt(i);
                // 找到"专注"对应的 tab（索引1）
                if (i == 1) {
                    // 整体放大
                    tab.setScaleX(1.12f);
                    tab.setScaleY(1.12f);
                    tab.setTranslationY(-2f);

                    // 给专注 tab 添加圆形背景
                    android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                    bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                    bg.setColor(0xFF0A84FF);
                    bg.setSize(dp(44), dp(44));

                    // 找到 tab 内的图标
                    if (tab instanceof android.view.ViewGroup) {
                        android.view.ViewGroup tabGroup = (android.view.ViewGroup) tab;
                        for (int j = 0; j < tabGroup.getChildCount(); j++) {
                            android.view.View child = tabGroup.getChildAt(j);
                            if (child instanceof android.widget.ImageView) {
                                child.setBackground(bg);
                                ((android.widget.ImageView) child).setColorFilter(0xFFFFFFFF);
                                child.setPadding(dp(12), dp(12), dp(12), dp(12));
                            }
                        }
                    }
                }
            }
        });
    }

    private int dp(int px) {
        return (int) (px * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void connectWebSocket() {
        SharedPreferences prefs = getSharedPreferences("znxsgl", 0);
        long userId = prefs.getLong("userId", 0);
        if (userId == 0) return;

        WebSocketManager ws = WebSocketManager.getInstance();
        ws.setListener((courseName, content, scheduleInfo) -> {
            runOnUiThread(() -> {
                showScheduleToast(content);
                // 通知 ProfileFragment 刷新课程列表（红点状态）
                if (currentFragment instanceof ProfileFragment) {
                    ((ProfileFragment) currentFragment).loadCoursesIfAdded();
                }
            });
        });
        ws.connect(RetrofitClient.getBaseUrl(), userId);
    }

    /** 显示排课通知 —— 自定义多行 Toast，一目了然 */
    private void showScheduleToast(String content) {
        // 用自定义 View 替代默认 Toast，支持多行显示
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(content);
        tv.setTextSize(15);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setPadding(36, 24, 36, 24);
        tv.setLineSpacing(6f, 1f);

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xE61D1D1F);
        bg.setCornerRadius(24f);
        tv.setBackground(bg);

        android.widget.Toast toast = new android.widget.Toast(this);
        toast.setDuration(android.widget.Toast.LENGTH_LONG);
        toast.setView(tv);
        toast.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL, 0, 160);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketManager.getInstance().disconnect();
    }

    private Fragment getFragmentById(int id) {
        if (id == R.id.nav_schedule) return new ScheduleFragment();
        if (id == R.id.nav_profile) return new ProfileFragment();
        if (id == R.id.nav_focus) {
            if (focusFragment == null) focusFragment = new FocusFragment();
            return focusFragment;
        }
        return new FocusFragment();
    }

    private void switchFragment(Fragment fragment) {
        if (fragment == currentFragment) return;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
        }
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        transaction.commit();
        currentFragment = fragment;
        // 跟踪 FocusFragment
        if (fragment instanceof FocusFragment) focusFragment = (FocusFragment) fragment;
    }
}
