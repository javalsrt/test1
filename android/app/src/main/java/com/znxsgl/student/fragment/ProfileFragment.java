package com.znxsgl.student.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.znxsgl.student.CourseDetailActivity;
import com.znxsgl.student.R;
import com.znxsgl.student.model.StudentCourse;
import com.znxsgl.student.R;
import com.znxsgl.student.network.ApiService;
import com.znxsgl.student.network.RetrofitClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private RecyclerView rvCourses;
    private CourseAdapter adapter;
    private final List<StudentCourse> courseList = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private boolean isFirstResume = true;
    private TextView tvStatHours;

    // 学习时长实时刷新
    private final Runnable refreshFocusTotal = new Runnable() {
        @Override
        public void run() {
            loadFocusTotal();
            mainHandler.postDelayed(this, 10000); // 每10秒刷新
        }
    };

    private static final String[] ICONS = {"📖","💻","📱","🌐","🎨","🔧","✍️","🗣️","🎬","📋","👥","📜","🏛️","🛡️"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        prefs = requireActivity().getSharedPreferences("znxsgl", 0);

        // 显示用户信息
        String realName = prefs.getString("realName", "学生");
        String username = prefs.getString("username", "");

        TextView tvAvatar = view.findViewById(R.id.tv_avatar);
        TextView tvName = view.findViewById(R.id.tv_real_name);
        TextView tvInfo = view.findViewById(R.id.tv_student_info);

        tvAvatar.setText(String.valueOf(realName.charAt(0)));
        tvName.setText(realName);
        tvInfo.setText("学号: " + username);

        // 退出登录
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            prefs.edit().clear().apply();
            com.znxsgl.student.network.WebSocketManager.getInstance().disconnect();
            Intent intent = new Intent(getActivity(), com.znxsgl.student.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // 学习时长
        tvStatHours = view.findViewById(R.id.tv_stat_hours);
        loadFocusTotal();

        // 收藏题目入口
        view.findViewById(R.id.ll_bookmarks).setOnClickListener(v -> showBookmarks());
        loadBookmarkCount(view);

        // 课程列表
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

        loadCourses();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && prefs != null) {
            loadCourses();
            loadFocusTotal();
            loadBookmarkCount(getView());
        }
        // 实时轮询学习时长
        mainHandler.removeCallbacks(refreshFocusTotal);
        mainHandler.post(refreshFocusTotal);
    }

    @Override
    public void onPause() {
        super.onPause();
        mainHandler.removeCallbacks(refreshFocusTotal);
    }

    /** hide() 不触发 onPause，处理 Tab 切换 */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            mainHandler.removeCallbacks(refreshFocusTotal);
        } else {
            // 切回此 Tab，立即刷新 + 启动定时刷新
            loadFocusTotal();
            loadCourses();
            if (getView() != null) loadBookmarkCount(getView());
            mainHandler.removeCallbacks(refreshFocusTotal);
            mainHandler.post(refreshFocusTotal);
        }
    }

    /** 加载累计学习总时长（分钟） */
    private void loadFocusTotal() {
        String token = "Bearer " + prefs.getString("token", "");
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getFocusTotal(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> resp) {
                if (!isAdded() || tvStatHours == null) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    Object secs = resp.body().get("totalSeconds");
                    if (secs instanceof Number) {
                        mainHandler.post(() -> {
                            int minutes = ((Number) secs).intValue() / 60;
                            tvStatHours.setText(String.valueOf(minutes));
                        });
                    }
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    /** 供 MainActivity WebSocket 回调，刷新课程列表 */
    public void loadCoursesIfAdded() {
        if (isAdded()) loadCourses();
    }

    /** 加载收藏数量 */
    private void loadBookmarkCount(View view) {
        String token = "Bearer " + prefs.getString("token", "");
        RetrofitClient.getInstance().create(ApiService.class)
                .getBookmarks(token).enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override public void onResponse(Call<List<Map<String, Object>>> c,
                                                      Response<List<Map<String, Object>>> r) {
                        if (!isAdded()) return;
                        if (r.isSuccessful() && r.body() != null) {
                            TextView tv = view.findViewById(R.id.tv_bookmark_count);
                            if (tv != null) tv.setText(String.valueOf(r.body().size()));
                        }
                    }
                    @Override public void onFailure(Call<List<Map<String, Object>>> c, Throwable t) {}
                });
    }

    /** 展示收藏列表：ViewPager2 竖滑卡片（与错题分析一致） */
    private void showBookmarks() {
        if (!isAdded()) return;
        String token = "Bearer " + prefs.getString("token", "");
        android.app.ProgressDialog pd = new android.app.ProgressDialog(getContext());
        pd.setMessage("加载中...");
        pd.show();

        RetrofitClient.getInstance().create(ApiService.class)
                .getBookmarks(token).enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override public void onResponse(Call<List<Map<String, Object>>> c,
                                                      Response<List<Map<String, Object>>> r) {
                        pd.dismiss();
                        if (!isAdded()) return;
                        if (!r.isSuccessful() || r.body() == null || r.body().isEmpty()) {
                            Toast.makeText(getContext(), "暂无收藏", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<Map<String, Object>> list = r.body();

                        // inflate 错题分析式的全屏 Dialog
                        View root = LayoutInflater.from(getContext())
                                .inflate(R.layout.dialog_wrong_analysis, null);
                        ViewPager2 vp = root.findViewById(R.id.vp_questions);
                        root.findViewById(R.id.ll_subjects).setVisibility(View.GONE);
                        root.findViewById(R.id.tv_page).setVisibility(View.GONE);

                        vp.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
                        vp.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                            @NonNull @Override
                            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
                                return new RecyclerView.ViewHolder(LayoutInflater.from(p.getContext())
                                        .inflate(R.layout.item_wrong_question, p, false)) {};
                            }
                            @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
                                Map<String, Object> m = list.get(pos);
                                String question = m.get("question") != null ? m.get("question").toString() : "";
                                String error = m.get("errorReason") != null ? m.get("errorReason").toString() : "";
                                String improve = m.get("improve") != null ? m.get("improve").toString() : "";
                                // 把 question 显示在知识点的位置
                                ((TextView) h.itemView.findViewById(R.id.tv_question)).setText(question);
                                ((TextView) h.itemView.findViewById(R.id.tv_error)).setText(error);
                                ((TextView) h.itemView.findViewById(R.id.tv_improve)).setText(improve);
                            }
                            @Override public int getItemCount() { return list.size(); }
                        });

                        // 关闭按钮
                        root.findViewById(R.id.btn_close).setOnClickListener(v2 -> {
                            ViewParent p = root.getParent();
                            while (p != null && !(p instanceof android.app.Dialog)) p = p.getParent();
                            if (p instanceof android.app.Dialog) ((android.app.Dialog) p).dismiss();
                        });

                        // 隐藏底部的明白了/收藏按钮
                        root.findViewById(R.id.btn_understand).setVisibility(View.GONE);
                        root.findViewById(R.id.btn_bookmark).setVisibility(View.GONE);

                        // 页码
                        final TextView tvPage = root.findViewById(R.id.tv_page);
                        vp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                            @Override public void onPageSelected(int pos) {
                                String subj = list.get(pos).get("subject") != null
                                        ? list.get(pos).get("subject").toString() : "";
                                tvPage.setVisibility(View.VISIBLE);
                                tvPage.setText(subj + "  " + (pos + 1) + "/" + list.size());
                            }
                        });

                        android.app.Dialog dlg = new android.app.Dialog(getContext(),
                                android.R.style.Theme_Translucent_NoTitleBar);
                        dlg.setContentView(root);
                        dlg.setCanceledOnTouchOutside(true);
                        dlg.setOnShowListener(d -> {
                            android.view.Window w = dlg.getWindow();
                            if (w != null) {
                                w.getDecorView().setPadding(0,0,0,0);
                                w.getDecorView().setBackgroundColor(Color.TRANSPARENT);
                                w.getDecorView().setOutlineProvider(null);
                                w.getDecorView().setElevation(0);
                                android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
                                lp.copyFrom(w.getAttributes());
                                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                lp.gravity = Gravity.CENTER;
                                lp.dimAmount = 0f;
                                w.setAttributes(lp);
                                w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
                            }
                        });
                        dlg.show();
                    }
                    @Override public void onFailure(Call<List<Map<String, Object>>> c, Throwable t) {
                        pd.dismiss();
                    }
                });
    }

    private void loadCourses() {
        String token = prefs.getString("token", "");
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getStudentCourses("Bearer " + token).enqueue(new Callback<List<StudentCourse>>() {
            @Override
            public void onResponse(Call<List<StudentCourse>> call, Response<List<StudentCourse>> resp) {
                List<StudentCourse> items = (resp.isSuccessful() && resp.body() != null)
                        ? resp.body() : new ArrayList<>();
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    courseList.clear();
                    courseList.addAll(items);
                    restoreCourseOrder();
                    adapter.notifyDataSetChanged();
                    rvCourses.requestLayout();
                });
            }
            @Override
            public void onFailure(Call<List<StudentCourse>> call, Throwable t) {
                mainHandler.post(() ->
                    Toast.makeText(getContext(), "加载失败: " + t.getMessage(), Toast.LENGTH_LONG).show());
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

            if (c.isActive()) {
                holder.name.setText(c.getCourseName());
                holder.name.setAlpha(1.0f);
                holder.icon.setAlpha(1.0f);
                holder.status.setVisibility(View.GONE);
            } else if (c.isPublished()) {
                holder.name.setText(c.getCourseName());
                holder.name.setAlpha(0.55f);
                holder.icon.setAlpha(0.55f);
                holder.status.setVisibility(View.VISIBLE);
                holder.status.setText("无课");
            } else {
                holder.name.setText(c.getCourseName());
                holder.name.setAlpha(0.45f);
                holder.icon.setAlpha(0.45f);
                holder.status.setVisibility(View.VISIBLE);
                holder.status.setText("已下架");
            }

            if (c.getUnreadCount() > 0) {
                holder.badge.setVisibility(View.VISIBLE);
                holder.badge.setText(String.valueOf(c.getUnreadCount()));
            } else {
                holder.badge.setVisibility(View.GONE);
            }

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

    private void saveCourseOrder() {
        StringBuilder sb = new StringBuilder();
        for (StudentCourse c : courseList) {
            if (sb.length() > 0) sb.append(",");
            sb.append(c.getCourseId());
        }
        prefs.edit().putString("course_order", sb.toString()).apply();
    }

    private void restoreCourseOrder() {
        String saved = prefs.getString("course_order", "");
        if (saved.isEmpty() || courseList.size() <= 1) return;
        String[] ids = saved.split(",");
        List<StudentCourse> ordered = new ArrayList<>();
        for (String idStr : ids) {
            long cid = Long.parseLong(idStr);
            for (StudentCourse c : courseList) {
                if (c.getCourseId() == cid) { ordered.add(c); break; }
            }
        }
        for (StudentCourse c : courseList) {
            if (!ordered.contains(c)) ordered.add(c);
        }
        if (ordered.size() == courseList.size()) {
            courseList.clear();
            courseList.addAll(ordered);
        }
    }
}
