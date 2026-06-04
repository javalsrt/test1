package com.znxsgl.student.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.znxsgl.student.LoginActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // 自动判断：本地10.0.2.2:8080开发用模拟器地址，部署时改为服务器地址你好
// 模拟器用 10.0.2.2，真机用电脑IP (192.168.0.146)
public static String getBaseUrl() {
    return "http://192.168.0.146:8080";
}

    private static Retrofit instance;
    private static Context appContext;

    /** 在 Application 中初始化，用于全局 401 拦截跳转 */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getInstance() {
        if (instance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 401/403 拦截器：token 过期或账号在其他设备登录时跳转登录页
            Interceptor authInterceptor = chain -> {
                Response response = chain.proceed(chain.request());
                int code = response.code();
                if ((code == 401 || code == 403) && appContext != null) {
                    // 在主线程提示并跳转登录页
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        Toast.makeText(appContext, "登录已过期，请重新登录", Toast.LENGTH_LONG).show();
                        // 清除本地 token
                        SharedPreferences prefs = appContext.getSharedPreferences("znxsgl", 0);
                        prefs.edit().remove("token").remove("userId").apply();
                        // 跳转登录页
                        Intent intent = new Intent(appContext, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        appContext.startActivity(intent);
                    });
                }
                return response;
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }
}
