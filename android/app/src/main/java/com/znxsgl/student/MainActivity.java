package com.znxsgl.student;

import android.content.Intent;
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
import com.znxsgl.student.fragment.NotifyFragment;
import com.znxsgl.student.fragment.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            } else if (id == R.id.nav_notify) {
                switchFragment(new NotifyFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        // 默认选中"学习"
        bottomNav.setSelectedItemId(R.id.nav_learn);
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
