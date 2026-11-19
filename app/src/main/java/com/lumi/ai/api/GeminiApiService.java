package com.lumi.ai.api;

import com.google.gson.JsonObject;
import com.lumi.ai.model.GeminiRequest;
import com.lumi.ai.model.GeminiResponse;
import com.lumi.ai.model.VideoOperationResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface GeminiApiService {

    // 1. Супер-интеллект (быстрая и умная модель с максимальным контекстом)
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    Call<GeminiResponse> generateSmartContent(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );

    // 2. Генерация изображений
    @POST("v1beta/models/gemini-2.0-flash-exp-image-generation:generateContent")
    Call<GeminiResponse> generateImage(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );

    // 3. Запуск генерации видео
    @POST("v1beta/models/veo-2.0-generate-001:predictLongRunning")
    Call<VideoOperationResponse> startVideoGeneration(
            @Query("key") String apiKey,
            @Body JsonObject videoRequestBody
    );

    // 4. Проверка статуса рендеринга видео
    @GET("v1beta/{operationName}")
    Call<VideoOperationResponse> checkVideoOperation(
            @Path(value = "operationName", encoded = true) String operationName,
            @Query("key") String apiKey
    );

    // 5. Скачивание готового MP4-файла
    @Streaming
    @GET
    Call<ResponseBody> downloadVideoFile(
            @Url String fileUrl,
            @Query("key") String apiKey
    );
}
