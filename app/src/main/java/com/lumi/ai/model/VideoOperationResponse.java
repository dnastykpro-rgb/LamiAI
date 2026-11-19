package com.lumi.ai.model;

import java.util.List;

public class VideoOperationResponse {
    public String name; // ID операции для проверки готовности
    public Boolean done;
    public OperationError error;
    public ResponseBody response;

    public String getVideoUri() {
        if (response != null && response.generateVideoResponse != null
                && response.generateVideoResponse.generatedSamples != null
                && !response.generateVideoResponse.generatedSamples.isEmpty()) {
            GeneratedSample sample = response.generateVideoResponse.generatedSamples.get(0);
            if (sample.video != null) {
                return sample.video.uri;
            }
        }
        return null;
    }

    public static class OperationError {
        public int code;
        public String message;
    }

    public static class ResponseBody {
        public GenerateVideoResponse generateVideoResponse;
    }

    public static class GenerateVideoResponse {
        public List<GeneratedSample> generatedSamples;
    }

    public static class GeneratedSample {
        public VideoFile video;
    }

    public static class VideoFile {
        public String uri;
    }
}

