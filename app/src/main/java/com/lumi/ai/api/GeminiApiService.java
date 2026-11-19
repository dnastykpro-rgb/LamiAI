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

    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    Call<GeminiResponse> generateSmartContent(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );

    @POST("v1beta/models/gemini-2.5-flash-image:generateContent")
    Call<GeminiResponse> generateImage(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );

    @POST("v1beta/models/veo-2.0-generate-001:predictLongRunning")
    Call<VideoOperationResponse> startVideoGeneration(
            @Query("key") String apiKey,
            @Body JsonObject videoRequestBody
    );

    @GET("v1beta/{operationName}")
    Call<VideoOperationResponse> checkVideoOperation(
            @Path(value = "operationName", encoded = true) String operationName,
            @Query("key") String apiKey
    );

    @Streaming
    @GET
    Call<ResponseBody> downloadVideoFile(
            @Url String fileUrl,
            @Query("key") String apiKey
    );
}
