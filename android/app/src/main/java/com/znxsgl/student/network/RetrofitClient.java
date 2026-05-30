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

    // 开发环境使用 10.0.2.2:8080 访问宿主机 localhost服务器8.166.118.19
    // 如果用真机测试，请改为电脑的局域网 IP你好


    public static final String BASE_URL = "http://10.0.2.2:8080";

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

            // 401 拦截器：账号在其他设备登录时踢出
            Interceptor authInterceptor = chain -> {
                Response response = chain.proceed(chain.request());
                if (response.code() == 401 && appContext != null) {
                    // 在主线程提示并跳转登录页
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        Toast.makeText(appContext, "账号已在其他设备登录，请重新登录", Toast.LENGTH_LONG).show();
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
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }
}
