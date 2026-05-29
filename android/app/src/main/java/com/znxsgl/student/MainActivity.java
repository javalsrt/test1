package com.znxsgl.student;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.znxsgl.student.fragment.LearnFragment;
import com.znxsgl.student.fragment.ScheduleFragment;
import com.znxsgl.student.fragment.ProfileFragment;
import com.znxsgl.student.network.RetrofitClient;
import com.znxsgl.student.network.WebSocketManager;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private Fragment currentFragment;

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
            if (id == R.id.nav_learn) {
                switchFragment(new LearnFragment());
                return true;
            } else if (id == R.id.nav_schedule) {
                switchFragment(new ScheduleFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_learn);
    }

    private void connectWebSocket() {
        SharedPreferences prefs = getSharedPreferences("znxsgl", 0);
        long userId = prefs.getLong("userId", 0);
        if (userId == 0) return;

        WebSocketManager ws = WebSocketManager.getInstance();
        ws.setListener((courseName, content, scheduleInfo) -> {
            runOnUiThread(() -> {
                showScheduleToast(content);
                // 通知当前 LearnFragment 刷新
                if (currentFragment instanceof LearnFragment) {
                    ((LearnFragment) currentFragment).refreshCourses();
                }
            });
        });
        ws.connect(RetrofitClient.BASE_URL, userId);
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
    }
}
