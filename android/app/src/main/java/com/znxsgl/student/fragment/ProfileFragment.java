package com.znxsgl.student.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.znxsgl.student.LoginActivity;
import com.znxsgl.student.R;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 从 SharedPreferences 读取登录信息
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("znxsgl", requireContext().MODE_PRIVATE);

        String realName = prefs.getString("realName", "学生");
        String username = prefs.getString("username", "");

        // 显示用户信息
        TextView tvAvatar = view.findViewById(R.id.tv_avatar);
        TextView tvName = view.findViewById(R.id.tv_real_name);
        TextView tvInfo = view.findViewById(R.id.tv_student_info);

        tvAvatar.setText(String.valueOf(realName.charAt(0)));
        tvName.setText(realName);
        tvInfo.setText("学号: " + username);

        // 退出登录
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            prefs.edit().clear().apply();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
            Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
