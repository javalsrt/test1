package com.znxsgl.student.network;

import com.znxsgl.student.model.ChatMsgDto;
import com.znxsgl.student.model.LoginRequest;
import com.znxsgl.student.model.LoginResponse;
import com.znxsgl.student.model.ScheduleItem;
import com.znxsgl.student.model.StudentCourse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("/api/schedule/student/courses")
    Call<List<StudentCourse>> getStudentCourses(
            @Header("Authorization") String token);

    @GET("/api/schedule/student/my")
    Call<List<ScheduleItem>> getStudentSchedule(
            @Header("Authorization") String token,
            @Query("week") int week);

    // 聊天
    @GET("/api/chat/{courseName}")
    Call<List<ChatMsgDto>> getChatMessages(
            @Header("Authorization") String token,
            @Path("courseName") String courseName);

    @POST("/api/chat/send")
    Call<ChatMsgDto> sendChatMessage(
            @Header("Authorization") String token,
            @Body Map<String, String> body);

    @POST("/api/chat/read")
    Call<Map<String, String>> markAsRead(
            @Header("Authorization") String token,
            @Body Map<String, String> body);

    @POST("/api/chat/rag")
    Call<ChatMsgDto> ragChat(
            @Header("Authorization") String token,
            @Body Map<String, String> body);

    @Multipart
    @POST("/api/chat/upload")
    Call<ChatMsgDto> uploadFile(
            @Header("Authorization") String token,
            @Part MultipartBody.Part file,
            @Part("courseName") RequestBody courseName);

    // 专注模式
    @POST("/api/focus/save")
    Call<Map<String, Object>> saveFocus(
            @Header("Authorization") String token,
            @Body Map<String, Object> body);

    @GET("/api/focus/today")
    Call<Map<String, Object>> getFocusToday(
            @Header("Authorization") String token);

    @POST("/api/focus/status")
    Call<Map<String, Object>> updateFocusStatus(
            @Header("Authorization") String token,
            @Body Map<String, String> body);

    @GET("/api/focus/students/{classId}")
    Call<List<Map<String, Object>>> getStudentFocusStatus(
            @Header("Authorization") String token,
            @Path("classId") long classId);

    @GET("/api/focus/last")
    Call<Map<String, Object>> getLastFocus(
            @Header("Authorization") String token);
}
