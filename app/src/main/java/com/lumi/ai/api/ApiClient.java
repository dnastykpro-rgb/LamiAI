package com.lumi.ai.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://googleapis.com";
    private static volatile GeminiApiService service;

    public static GeminiApiService getService() {
        if (service == null) {
            synchronized (ApiClient.class) {
                if (service == null) {
                    // Увеличенные таймауты (до 3 минут), чтобы сложные задачи по урокам и фото не обрывались
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(180, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    service = retrofit.create(GeminiApiService.class);
                }
            }
        }
        return service;
    }
}

