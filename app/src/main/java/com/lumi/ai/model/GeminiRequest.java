package com.lumi.ai.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeminiRequest {

    public static final String LUMI_SYSTEM_PROMPT =
            "Ты — LUMI AI (based at Google AI), гиперумный ИИ-ассистент и репетитор. " +
            "Строгие правила общения: " +
            "1. Никогда не задавай пустых, вежливых или дежурных вопросов в конце ответа (например: 'Чем еще помочь?', 'Нужно ли расписать подробнее?'). " +
            "2. Задавай уточняющий вопрос только в том случае, если без недостающей информации физически невозможно дать точный ответ или решить задачу (например, обрезано условие на фото или не указаны важные данные). В таком случае задай один конкретный вопрос по делу. " +
            "3. Всегда доводи решение любой учебной задачи до полного, законченного финала. Запрещено писать 'решается аналогично' или бросать решение на середине. " +
            "4. Сначала пиши краткий итоговый ответ, затем — понятный пошаговый разбор.";

    private SystemInstruction systemInstruction;
    private List<Content> contents;
    private GenerationConfig generationConfig;

    public GeminiRequest(List<Content> historyContents, boolean isImageGeneration) {
        if (!isImageGeneration) {
            this.systemInstruction = new SystemInstruction(LUMI_SYSTEM_PROMPT);
            this.generationConfig = new GenerationConfig(65536, 0.4, null);
        } else {
            this.generationConfig = new GenerationConfig(8192, 0.7, Collections.singletonList("IMAGE"));
        }
        this.contents = historyContents;
    }

    public static class SystemInstruction {
        private List<Part> parts;

        public SystemInstruction(String text) {
            this.parts = Collections.singletonList(new Part(text));
        }
    }

    public static class Content {
        private String role; // "user" или "model"
        private List<Part> parts;

        public Content(String role, List<Part> parts) {
            this.role = role;
            this.parts = parts;
        }

        public static Content fromUserText(String text) {
            return new Content("user", Collections.singletonList(new Part(text)));
        }

        public static Content fromUserTextAndImage(String text, String base64Image, String mimeType) {
            List<Part> list = new ArrayList<>();
            if (text != null && !text.isEmpty()) {
                list.add(new Part(text));
            }
            list.add(new Part(new InlineData(mimeType, base64Image)));
            return new Content("user", list);
        }

        public static Content fromModelText(String text) {
            return new Content("model", Collections.singletonList(new Part(text)));
        }
    }

    public static class Part {
        private String text;
        private InlineData inlineData;

        public Part(String text) {
            this.text = text;
        }

        public Part(InlineData inlineData) {
            this.inlineData = inlineData;
        }
    }

    public static class InlineData {
        private String mimeType;
        private String data;

        public InlineData(String mimeType, String data) {
            this.mimeType = mimeType;
            this.data = data;
        }
    }

    public static class GenerationConfig {
        private Integer maxOutputTokens;
        private Double temperature;
        private List<String> responseModalities;

        public GenerationConfig(Integer maxOutputTokens, Double temperature, List<String> responseModalities) {
            this.maxOutputTokens = maxOutputTokens;
            this.temperature = temperature;
            this.responseModalities = responseModalities;
        }
    }
}
