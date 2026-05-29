package com.znxsgl.student.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.znxsgl.student.CourseDetailActivity;
import com.znxsgl.student.FocusActivity;
import com.znxsgl.student.R;
import com.znxsgl.student.model.StudentCourse;
import com.znxsgl.student.network.ApiService;
import com.znxsgl.student.network.RetrofitClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearnFragment extends Fragment {

    private RecyclerView rvCourses;
    private CourseAdapter adapter;
    private final List<StudentCourse> courseList = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private boolean isFirstResume = true;

    private static final String[] ICONS = {"📖","💻","📱","🌐","🎨","🔧","✍️","🗣️","🎬","📋","👥","📜","🏛️","🛡️"};

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learn, container, false);
        prefs = requireActivity().getSharedPreferences("znxsgl", 0);

        rvCourses = view.findViewById(R.id.rv_courses);
        rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CourseAdapter();
        rvCourses.setAdapter(adapter);

        // 长按拖拽排序
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                int from = vh.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(courseList, from, to);
                adapter.notifyItemMoved(from, to);
                saveCourseOrder();
                return true;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {}
        });
        helper.attachToRecyclerView(rvCourses);

        view.findViewById(R.id.btn_focus).setOnClickListener(v ->
                startActivity(new Intent(getContext(), FocusActivity.class)));
        view.findViewById(R.id.btn_homework).setOnClickListener(v ->
                showToast("作业功能开发中"));
        view.findViewById(R.id.btn_ext1).setOnClickListener(v ->
                showToast("资料功能开发中"));
        view.findViewById(R.id.btn_ext2).setOnClickListener(v ->
                showToast("AI问答功能开发中"));

        loadCourses();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 跳过首次（onCreateView 已加载），之后每次从 CourseDetailActivity 返回时刷新
        if (isFirstResume) {
            isFirstResume = false;
            return;
        }
        if (isAdded() && prefs != null) {
            loadCourses();
        }
    }

    /** 供 MainActivity 调用，刷新课程列表 */
    public void refreshCourses() {
        if (isAdded()) loadCourses();
    }

    private void loadCourses() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("znxsgl", 0);
        String token = prefs.getString("token", "");

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getStudentCourses("Bearer " + token).enqueue(new Callback<List<StudentCourse>>() {
            @Override
            public void onResponse(Call<List<StudentCourse>> call, Response<List<StudentCourse>> resp) {
                List<StudentCourse> items = (resp.isSuccessful() && resp.body() != null)
                        ? resp.body() : new ArrayList<>();
                mainHandler.post(() -> {
                    courseList.clear();
                    courseList.addAll(items);
                    restoreCourseOrder();
                    adapter.notifyDataSetChanged();
                    if (courseList.isEmpty()) {
                        Toast.makeText(getContext(), "暂无课程数据", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(Call<List<StudentCourse>> call, Throwable t) {
                mainHandler.post(() ->
                    Toast.makeText(getContext(), "无法连接服务器", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ========== Adapter ==========
    private class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_course, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(@NonNull VH holder, int pos) {
            StudentCourse c = courseList.get(pos);
            holder.icon.setText(ICONS[pos % ICONS.length]);

            // 课程状态显示
            if (c.isActive()) {
                // 在线且有排课：正常显示
                holder.name.setText(c.getCourseName());
                holder.name.setAlpha(1.0f);
                holder.icon.setAlpha(1.0f);
                holder.status.setVisibility(View.GONE);
            } else if (c.isPublished()) {
                // 已上架但无排课：低亮 + "无课"
                holder.name.setText(c.getCourseName());
                holder.name.setAlpha(0.55f);
                holder.icon.setAlpha(0.55f);
                holder.status.setVisibility(View.VISIBLE);
                holder.status.setText("无课");
            } else {
                // 真正下架：低亮 + "已下架"
                holder.name.setText(c.getCourseName());
                holder.name.setAlpha(0.45f);
                holder.icon.setAlpha(0.45f);
                holder.status.setVisibility(View.VISIBLE);
                holder.status.setText("已下架");
            }

            // 红点：有新消息时显示
            if (c.getUnreadCount() > 0) {
                holder.badge.setVisibility(View.VISIBLE);
                holder.badge.setText(String.valueOf(c.getUnreadCount()));
            } else {
                holder.badge.setVisibility(View.GONE);
            }

            // 显示排课信息或教师
            if (c.getScheduleInfo() != null && !c.getScheduleInfo().isEmpty()) {
                holder.info.setText(c.getScheduleInfo());
            } else if (c.getTeacherName() != null && !c.getTeacherName().isEmpty()) {
                holder.info.setText("教师：" + c.getTeacherName());
            } else {
                holder.info.setText("暂无排课信息");
            }
        }
        @Override
        public int getItemCount() { return courseList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView icon, name, info, status, badge;
            VH(View v) {
                super(v);
                icon = v.findViewById(R.id.tv_course_icon);
                name = v.findViewById(R.id.tv_course_name);
                info = v.findViewById(R.id.tv_course_info);
                status = v.findViewById(R.id.tv_course_status);
                badge = v.findViewById(R.id.tv_badge);
                v.setOnClickListener(v2 -> {
                    StudentCourse c = courseList.get(getAdapterPosition());
                    Intent intent = new Intent(getContext(), CourseDetailActivity.class);
                    intent.putExtra("course_name", c.getCourseName());
                    intent.putExtra("course_id", c.getCourseId());
                    startActivity(intent);
                });
            }
        }
    }

    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /** 保存课程排序到 SharedPreferences（按 courseId 逗号分隔） */
    private void saveCourseOrder() {
        StringBuilder sb = new StringBuilder();
        for (StudentCourse c : courseList) {
            if (sb.length() > 0) sb.append(",");
            sb.append(c.getCourseId());
        }
        prefs.edit().putString("course_order", sb.toString()).apply();
    }

    /** 从 SharedPreferences 恢复课程排序 */
    private void restoreCourseOrder() {
        String saved = prefs.getString("course_order", "");
        if (saved.isEmpty() || courseList.size() <= 1) return;
        String[] ids = saved.split(",");
        List<StudentCourse> ordered = new ArrayList<>();
        // 按保存的顺序排列
        for (String idStr : ids) {
            long cid = Long.parseLong(idStr);
            for (StudentCourse c : courseList) {
                if (c.getCourseId() == cid) {
                    ordered.add(c);
                    break;
                }
            }
        }
        // 把新增的课程追加到末尾
        for (StudentCourse c : courseList) {
            if (!ordered.contains(c)) ordered.add(c);
        }
        if (ordered.size() == courseList.size()) {
            courseList.clear();
            courseList.addAll(ordered);
        }
    }
}
