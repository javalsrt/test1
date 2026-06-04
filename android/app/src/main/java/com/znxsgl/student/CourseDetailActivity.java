package com.znxsgl.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.znxsgl.student.model.ChatMsgDto;
import com.znxsgl.student.network.ApiService;
import com.znxsgl.student.network.RetrofitClient;
import com.znxsgl.student.network.WebSocketManager;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class CourseDetailActivity extends AppCompatActivity {

    private static final int REQ_CAMERA = 1001;
    private static final int REQ_FILE = 1002;
    private static final String TAG = "CourseDetail";

    private EditText etInput;
    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private String courseName;
    private String token;
    private final List<Object> items = new ArrayList<>();
    private File cameraFile;

    // 流式输出相关
    private final Handler streamHandler = new Handler(Looper.getMainLooper());
    private int streamMsgPos = -1;
    private String streamFullText = "";
    private int streamCharIdx = 0;
    private static final int STREAM_INTERVAL_MS = 40; // 每字间隔
    private boolean isStreaming = false;

    // WebSocket 监听器
    private final WebSocketManager.OnScheduleUpdateListener wsListener = (cn, content, info) -> {
        if (courseName != null && courseName.equals(cn)) {
            runOnUiThread(() -> appendScheduleNotice(content));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        courseName = getIntent().getStringExtra("course_name");
        if (courseName == null) courseName = "课程详情";

        SharedPreferences prefs = getSharedPreferences("znxsgl", 0);
        token = "Bearer " + prefs.getString("token", "");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(courseName);

        rvChat = findViewById(R.id.rv_chat);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(items);
        rvChat.setAdapter(adapter);

        etInput = findViewById(R.id.et_input);
        findViewById(R.id.btn_send).setOnClickListener(v -> sendToAI());
        findViewById(R.id.btn_add).setOnClickListener(v -> showAddSheet());

        // 注册 WebSocket 监听，教师修改排课时实时同步到对话
        WebSocketManager.getInstance().addListener(wsListener);

        loadMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消当前页面的 WebSocket 监听
        WebSocketManager.getInstance().removeListener(wsListener);
        streamHandler.removeCallbacksAndMessages(null);
    }

    private void loadMessages() {
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getChatMessages(token, courseName).enqueue(new Callback<List<ChatMsgDto>>() {
            @Override
            public void onResponse(Call<List<ChatMsgDto>> call, retrofit2.Response<List<ChatMsgDto>> resp) {
                if (resp.isSuccessful() && resp.body() != null) buildItems(resp.body());
            }
            @Override
            public void onFailure(Call<List<ChatMsgDto>> call, Throwable t) {}
        });
        // 标记已读
        Map<String, String> body = new HashMap<>();
        body.put("courseName", courseName);
        api.markAsRead(token, body).enqueue(new Callback<Map<String, String>>() {
            @Override public void onResponse(Call<Map<String, String>> c, retrofit2.Response<Map<String, String>> r) {}
            @Override public void onFailure(Call<Map<String, String>> c, Throwable t) {}
        });
    }

    private void buildItems(List<ChatMsgDto> msgs) {
        items.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date prevTime = null;
        for (ChatMsgDto msg : msgs) {
            Date cur = null;
            try { cur = sdf.parse(msg.getCreatedAt()); } catch (ParseException ignored) {}
            if (prevTime == null && cur != null) {
                items.add(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(cur));
            } else if (prevTime != null && cur != null
                    && (cur.getTime() - prevTime.getTime()) > 10000) {
                items.add(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(cur));
            }
            // 解析 [image] / [file] 前缀
            String raw = msg.getContent();
            if (raw != null && raw.startsWith("[image]")) {
                msg.msgType = "image";
                msg.imageUrl = raw.substring(7);
                msg.setContent("[图片]");
            } else if (raw != null && raw.startsWith("[file]")) {
                msg.msgType = "file";
                String rest = raw.substring(6);
                int sep = rest.indexOf('|');
                if (sep > 0) {
                    msg.fileName = rest.substring(0, sep);
                    msg.fileUrl = rest.substring(sep + 1);
                } else {
                    msg.fileName = rest;
                    msg.fileUrl = "#";
                }
                msg.setContent("📄 " + msg.fileName);
            } else {
                msg.msgType = "text";
            }
            items.add(msg);
            prevTime = cur;
        }
        adapter.notifyDataSetChanged();
        if (!items.isEmpty()) rvChat.scrollToPosition(items.size() - 1);
    }

    // ===== 发送给 AI =====
    private void sendToAI() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;

        addStudentAndAiPlaceholder(text);

        Map<String, String> body = new HashMap<>();
        body.put("courseName", courseName);
        body.put("content", text);

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.ragChat(token, body).enqueue(aiCallback(items.size() - 1));
    }

    private void addStudentAndAiPlaceholder(String text) {
        int studentPos = items.size();
        items.add(makeLocalMsg(text, "student"));
        items.add(makeLocalMsg("思考中...", "ai"));
        adapter.notifyItemRangeInserted(studentPos, 2);
        rvChat.scrollToPosition(studentPos + 1);
        etInput.setText("");
    }

    private Callback<ChatMsgDto> aiCallback(int aiPos) {
        return new Callback<ChatMsgDto>() {
            @Override
            public void onResponse(Call<ChatMsgDto> call, retrofit2.Response<ChatMsgDto> resp) {
                runOnUiThread(() -> {
                    if (aiPos >= items.size()) return;
                    ChatMsgDto m = (ChatMsgDto) items.get(aiPos);
                    if (resp.isSuccessful() && resp.body() != null) {
                        m.setContent(resp.body().getContent());
                        m.setCreatedAt(resp.body().getCreatedAt());
                    } else {
                        m.setContent("（AI 回复失败）");
                    }
                    adapter.notifyItemChanged(aiPos);
                    rvChat.scrollToPosition(aiPos);
                });
            }
            @Override
            public void onFailure(Call<ChatMsgDto> call, Throwable t) {
                Log.e(TAG, "AI请求失败", t);
                runOnUiThread(() -> {
                    if (aiPos < items.size()) {
                        ChatMsgDto m = (ChatMsgDto) items.get(aiPos);
                        m.setContent("（网络超时，请重试）");
                        adapter.notifyItemChanged(aiPos);
                    }
                });
            }
        };
    }

    private ChatMsgDto makeLocalMsg(String content, String role) {
        ChatMsgDto d = new ChatMsgDto();
        d.setContent(content);
        d.setSenderRole(role);
        d.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        return d;
    }

    // ===== 底部弹出：拍照 / 资料上传 =====
    private void showAddSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_add_sheet, null);
        sheet.setContentView(view);

        view.findViewById(R.id.btn_camera).setOnClickListener(v -> {
            sheet.dismiss();
            openCameraOrGallery();
        });

        view.findViewById(R.id.btn_file).setOnClickListener(v -> {
            sheet.dismiss();
            openFilePicker();
        });

        sheet.show();
    }

    // 拍照或从相册选择
    private void openCameraOrGallery() {
        BottomSheetDialog d = new BottomSheetDialog(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(40, 24, 40, 24);

        View camBtn = makeOptionBtn("📷 拍照", v1 -> { d.dismiss(); dispatchTakePicture(); });
        View galBtn = makeOptionBtn("🖼 从相册选择", v2 -> { d.dismiss(); openGallery(); });
        ll.addView(camBtn);
        ll.addView(galBtn);
        d.setContentView(ll);
        d.show();
    }

    private View makeOptionBtn(String text, View.OnClickListener listener) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setTextColor(0xFF1D1D1F);
        tv.setPadding(24, 20, 24, 20);
        tv.setOnClickListener(listener);
        return tv;
    }

    // 拍照
    private void dispatchTakePicture() {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            cameraFile = File.createTempFile("camera_", ".jpg", dir);
            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", cameraFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, REQ_CAMERA);
        } catch (IOException e) {
            Toast.makeText(this, "无法创建照片文件", Toast.LENGTH_SHORT).show();
        }
    }

    // 从相册选择
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_CAMERA);
    }

    // 文件选择器
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimes = {"application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimes);
        startActivityForResult(intent, REQ_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQ_CAMERA) {
            Uri uri = (data != null && data.getData() != null)
                    ? data.getData()
                    : (cameraFile != null ? FileProvider.getUriForFile(this,
                            getPackageName() + ".fileprovider", cameraFile) : null);
            if (uri != null) uploadFile(uri);
        } else if (requestCode == REQ_FILE) {
            if (data != null && data.getData() != null) uploadFile(data.getData());
        }
    }

    private void uploadFile(Uri uri) {
        addStudentAndAiPlaceholder("📎 正在上传并分析文件...");

        new Thread(() -> {
            try {
                // 读取文件信息
                String fileName = "file";
                String mime = getContentResolver().getType(uri);
                if (mime == null) mime = "application/octet-stream";

                // 从 Uri 获取文件名
                android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIdx >= 0) fileName = cursor.getString(nameIdx);
                    cursor.close();
                }

                // 读取文件内容
                java.io.InputStream is = getContentResolver().openInputStream(uri);
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                is.close();

                // 构建 Multipart
                RequestBody fileBody = RequestBody.create(bytes, MediaType.parse(mime));
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, fileBody);
                RequestBody coursePart = RequestBody.create(courseName, MediaType.parse("text/plain"));

                // 调用上传 API
                ApiService api = RetrofitClient.getInstance().create(ApiService.class);
                retrofit2.Response<ChatMsgDto> resp = api.uploadFile(token, filePart, coursePart).execute();

                runOnUiThread(() -> {
                    int aiPos = items.size() - 1;
                    if (aiPos >= 0 && aiPos < items.size()) {
                        ChatMsgDto m = (ChatMsgDto) items.get(aiPos);
                        if (resp.isSuccessful() && resp.body() != null) {
                            m.setContent(resp.body().getContent());
                        } else {
                            m.setContent("（文件分析失败）");
                        }
                        adapter.notifyItemChanged(aiPos);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "文件上传失败", e);
                runOnUiThread(() -> {
                    int aiPos = items.size() - 1;
                    if (aiPos >= 0 && aiPos < items.size()) {
                        ChatMsgDto m = (ChatMsgDto) items.get(aiPos);
                        m.setContent("（上传失败：" + e.getMessage() + "）");
                        adapter.notifyItemChanged(aiPos);
                    }
                });
            }
        }).start();
    }

    // ===== WebSocket 实时通知 → 流式输出到对话 =====
    private void appendScheduleNotice(String content) {
        // 如果正在流式输出中，先结束上一次
        if (isStreaming) {
            streamHandler.removeCallbacksAndMessages(null);
            // 立即显示剩余文本
            if (streamMsgPos >= 0 && streamMsgPos < items.size()) {
                ChatMsgDto m = (ChatMsgDto) items.get(streamMsgPos);
                m.setContent(streamFullText);
                adapter.notifyItemChanged(streamMsgPos);
            }
            isStreaming = false;
        }

        // 插入新的通知消息（AI 角色，左对齐灰色气泡）
        int pos = items.size();
        ChatMsgDto msg = new ChatMsgDto();
        msg.setContent("");  // 先空，流式填充
        msg.setSenderRole("ai");
        msg.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        items.add(msg);
        adapter.notifyItemInserted(pos);
        rvChat.scrollToPosition(pos);

        // 启动流式输出
        streamMsgPos = pos;
        streamFullText = content;
        streamCharIdx = 0;
        isStreaming = true;
        streamNextChar();
    }

    private void streamNextChar() {
        if (!isStreaming || streamMsgPos < 0 || streamMsgPos >= items.size()) {
            isStreaming = false;
            return;
        }
        if (streamCharIdx < streamFullText.length()) {
            String partial = streamFullText.substring(0, streamCharIdx + 1);
            ChatMsgDto m = (ChatMsgDto) items.get(streamMsgPos);
            m.setContent(partial);
            adapter.notifyItemChanged(streamMsgPos);
            // 最后一项时自动滚动
            if (streamMsgPos == items.size() - 1) {
                rvChat.scrollToPosition(streamMsgPos);
            }
            streamCharIdx++;
            streamHandler.postDelayed(this::streamNextChar, STREAM_INTERVAL_MS);
        } else {
            isStreaming = false;
            // 流式输出完成 → 标记已读，消除红点
            markReadForCurrentCourse();
        }
    }

    /** 标记当前课程消息为已读（消除红点） */
    private void markReadForCurrentCourse() {
        Map<String, String> body = new HashMap<>();
        body.put("courseName", courseName);
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.markAsRead(token, body).enqueue(new Callback<Map<String, String>>() {
            @Override public void onResponse(Call<Map<String, String>> c, retrofit2.Response<Map<String, String>> r) {}
            @Override public void onFailure(Call<Map<String, String>> c, Throwable t) {}
        });
    }

    // ===== Adapter =====
    private static class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_TIME = 0, TYPE_TEXT = 1, TYPE_IMAGE = 2, TYPE_FILE = 3;
        private final List<Object> data;
        ChatAdapter(List<Object> d) { data = d; }

        @Override public int getItemViewType(int p) {
            Object o = data.get(p);
            if (o instanceof ChatMsgDto) {
                ChatMsgDto m = (ChatMsgDto) o;
                if ("image".equals(m.msgType)) return TYPE_IMAGE;
                if ("file".equals(m.msgType)) return TYPE_FILE;
                return TYPE_TEXT;
            }
            return TYPE_TIME;
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int vt) {
            if (vt == TYPE_TIME) {
                TextView tv = new TextView(parent.getContext());
                tv.setTextSize(12); tv.setTextColor(0xFF86868B);
                tv.setGravity(Gravity.CENTER); tv.setPadding(0, 12, 0, 4);
                tv.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
                return new TimeVH(tv);
            }
            if (vt == TYPE_IMAGE) {
                LinearLayout item = new LinearLayout(parent.getContext());
                item.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
                item.setOrientation(LinearLayout.VERTICAL); item.setPadding(0, 4, 0, 4);
                android.widget.ImageView iv = new android.widget.ImageView(parent.getContext());
                iv.setAdjustViewBounds(true);
                int maxW = dp(parent.getContext(), 260);
                iv.setMaxWidth(maxW);
                iv.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                iv.setLayoutParams(new LinearLayout.LayoutParams(maxW, -2));
                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(parent.getContext(), 12));
                bg.setColor(0xFFF0F0F0);
                iv.setBackground(bg);
                iv.setPadding(2, 2, 2, 2);
                item.addView(iv);
                return new ImgVH(item, iv);
            }
            if (vt == TYPE_FILE) {
                LinearLayout item = new LinearLayout(parent.getContext());
                item.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
                item.setOrientation(LinearLayout.VERTICAL); item.setPadding(0, 4, 0, 4);
                TextView tv = new TextView(parent.getContext());
                tv.setTextSize(14); tv.setPadding(24, 14, 24, 14);
                tv.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
                item.addView(tv);
                return new FileVH(item, tv);
            }
            // TYPE_TEXT
            LinearLayout item = new LinearLayout(parent.getContext());
            item.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
            item.setOrientation(LinearLayout.VERTICAL); item.setPadding(0, 4, 0, 4);
            TextView tv = new TextView(parent.getContext());
            tv.setTextSize(17); tv.setPadding(28, 18, 28, 18);
            tv.setMaxWidth(dp(parent.getContext(), 320));
            tv.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            item.addView(tv);
            return new TextVH(item, tv);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int p) {
            if (h instanceof TimeVH) { ((TimeVH)h).tv.setText((String)data.get(p)); return; }
            ChatMsgDto m = (ChatMsgDto) data.get(p);
            boolean me = "student".equals(m.getSenderRole());

            if (h instanceof ImgVH) {
                ImgVH vh = (ImgVH) h;
                String url = m.imageUrl;
                if (url != null && !url.startsWith("http")) {
                    url = RetrofitClient.getBaseUrl() + url;
                }
                // position chat bubble
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vh.iv.getLayoutParams();
                lp.gravity = me ? Gravity.END : Gravity.START;
                lp.setMargins(me ? 0 : 8, 0, me ? 8 : 0, 0);
                vh.iv.setLayoutParams(lp);
                // Load image in background (capture final url ref)
                final String finalUrl = url;
                vh.iv.setTag(finalUrl);
                new Thread(() -> {
                    try {
                        java.net.URL imgUrl = new java.net.URL(finalUrl);
                        java.io.InputStream is = imgUrl.openStream();
                        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(is);
                        is.close();
                        if (bmp != null) {
                            vh.iv.post(() -> {
                                if (finalUrl.equals(vh.iv.getTag())) vh.iv.setImageBitmap(bmp);
                            });
                        }
                    } catch (Exception ignored) {}
                }).start();
                vh.iv.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(finalUrl));
                    v.getContext().startActivity(intent);
                });
                return;
            }

            if (h instanceof FileVH) {
                FileVH vh = (FileVH) h;
                vh.tv.setText("📄 " + (m.fileName != null ? m.fileName : "文件"));
                vh.tv.setLineSpacing(4f, 1f);
                vh.tv.setOnClickListener(v -> {
                    String furl = m.fileUrl;
                    if (furl != null && !furl.startsWith("http")) furl = RetrofitClient.getBaseUrl() + furl;
                    Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(furl));
                    v.getContext().startActivity(intent);
                });
                applyBubbleStyle(vh.tv, me);
                return;
            }

            // TYPE_TEXT
            TextVH vh = (TextVH) h;
            vh.tv.setText(m.getContent());
            vh.tv.setLineSpacing(4f, 1f);
            applyBubbleStyle(vh.tv, me);
        }

        private void applyBubbleStyle(TextView tv, boolean me) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tv.getLayoutParams();
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(tv.getContext(), 16));
            if (me) { bg.setColor(0xFF0A84FF); tv.setTextColor(Color.WHITE); lp.gravity=Gravity.END; lp.setMargins(0,0,8,0); }
            else { bg.setColor(0xFFF5F5F7); tv.setTextColor(0xFF1D1D1F); lp.gravity=Gravity.START; lp.setMargins(8,0,0,0); }
            tv.setBackground(bg);
            tv.setLayoutParams(lp);
        }

        @Override public int getItemCount() { return data.size(); }
        static class TextVH extends RecyclerView.ViewHolder { TextView tv; TextVH(View v, TextView t) { super(v); tv=t; } }
        static class ImgVH extends RecyclerView.ViewHolder { android.widget.ImageView iv; ImgVH(View v, android.widget.ImageView i) { super(v); iv=i; } }
        static class FileVH extends RecyclerView.ViewHolder { TextView tv; FileVH(View v, TextView t) { super(v); tv=t; } }
        static class TimeVH extends RecyclerView.ViewHolder { TextView tv; TimeVH(TextView t) { super(t); tv=t; } }
        static int dp(android.content.Context c, int v) { return (int)(v*c.getResources().getDisplayMetrics().density+0.5f); }
    }
}
