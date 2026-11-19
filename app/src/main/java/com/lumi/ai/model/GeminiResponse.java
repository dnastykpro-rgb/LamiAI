package com.lumi.ai.model;

import java.util.List;

public class GeminiResponse {
    private List<Candidate> candidates;

    public String getText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate first = candidates.get(0);
            if (first.content != null && first.content.parts != null) {
                StringBuilder sb = new StringBuilder();
                for (Part part : first.content.parts) {
                    if (part.text != null) {
                        sb.append(part.text);
                    }
                }
                if (sb.length() > 0) {
                    return sb.toString().trim();
                }
            }
        }
        return null;
    }

    public String getBase64Image() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate first = candidates.get(0);
            if (first.content != null && first.content.parts != null) {
                for (Part part : first.content.parts) {
                    if (part.inlineData != null && part.inlineData.data != null) {
                        return part.inlineData.data;
                    }
                }
            }
        }
        return null;
    }

    public static class Candidate {
        public Content content;
    }

    public static class Content {
        public List<Part> parts;
    }

    public static class Part {
        public String text;
        public InlineData inlineData;
    }

    public static class InlineData {
        public String mimeType;
        public String data;
    }
}
