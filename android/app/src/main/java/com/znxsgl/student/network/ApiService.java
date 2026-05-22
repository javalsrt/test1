package com.znxsgl.student.network;

import com.znxsgl.student.model.LoginRequest;
import com.znxsgl.student.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
