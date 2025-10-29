package com.aipyq.friendapp.ai.providers;

import com.aipyq.friendapp.ai.ImageRenderProvider;
import com.aipyq.friendapp.api.dto.RenderRequest;
import com.aipyq.friendapp.api.dto.RenderResult;
import com.aipyq.friendapp.config.PromptProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VolcImageRenderProvider implements ImageRenderProvider {

    private static final String DEFAULT_RENDER_TEMPLATE = String.join("\n",
            "Task: enhance the given image for a WeChat Moments post without adding any text.",
            "Guidelines:",
            "1) Maintain aspect ratio {{ratio}} with output resolution {{resolution}} while preserving the main subject.",
            "2) Boost exposure, contrast, and clarity slightly to make the subject stand out.",
            "3) Keep colors natural; only minor white balance or saturation tweaks if necessary.",
            "4) Apply light denoising while keeping texture and detail intact.",
            "5) Do not introduce new elements or change the original meaning.",
            "{{watermarkLine}}",
            "Output: provide the enhanced image URL (or Base64)."
    );
    private static final String DEFAULT_WATERMARK_INSTRUCTION =
            "6) If a watermark is required, place a tiny 20% opacity mark in the bottom-right without covering the subject.";

    private final VolcClient client;
    private final String imageEditEndpoint;
    private final String imageEditModel;
    private final PromptProperties promptProperties;
    private final ObjectMapper mapper = new ObjectMapper();

    public VolcImageRenderProvider(VolcClient client,
                                   String imageEditEndpoint,
                                   String imageEditModel,
                                   PromptProperties promptProperties) {
        this.client = client;
        this.imageEditEndpoint = imageEditEndpoint;
        this.imageEditModel = imageEditModel;
        this.promptProperties = promptProperties;
    }

    @Override
    public RenderResult render(RenderRequest request) {
        try {
            String instruction = buildInstruction(request);
            String size = resolveSize(request.getResolution());
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("model", imageEditModel);
            body.put("prompt", instruction);
            if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
                body.put("image", request.getImageUrl());
            }
            body.put("size", size);
            body.put("sequential_image_generation", "disabled");
            body.put("stream", false);
            body.put("response_format", "url");
            body.put("watermark", request.getWatermarkOn() != null && request.getWatermarkOn());
            String resp = client.postJson(imageEditEndpoint, mapper.writeValueAsString(body));
            JsonNode node = mapper.readTree(resp);
            List<String> urls = new ArrayList<>();
            JsonNode data = node.path("data");
            if (data.isArray()) {
                for (JsonNode it : data) {
                    JsonNode u = it.get("url");
                    if (u != null && u.isTextual()) {
                        urls.add(u.asText());
                    }
                }
            }
            RenderResult result = new RenderResult();
            if (urls.isEmpty()) {
                urls.add(request.getImageUrl());
            }
            result.setImages(urls);
            result.setGenerationId(UUID.randomUUID().toString());
            return result;
        } catch (Exception e) {
            RenderResult fallback = new RenderResult();
            List<String> fallbackImages = new ArrayList<>();
            if (request != null && request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
                fallbackImages.add(request.getImageUrl());
            }
            fallback.setImages(fallbackImages);
            fallback.setGenerationId(UUID.randomUUID().toString());
            return fallback;
        }
    }

    private String buildInstruction(RenderRequest req) {
        PromptProperties.Image imageConfig = promptProperties.getImage();
        String template = fallback(imageConfig.getRenderTemplate(), DEFAULT_RENDER_TEMPLATE);
        boolean watermarkOn = req.getWatermarkOn() != null && req.getWatermarkOn();
        String watermarkLine = watermarkOn
                ? fallback(imageConfig.getWatermarkInstruction(), DEFAULT_WATERMARK_INSTRUCTION)
                : "";
        return template
                .replace("{{ratio}}", or(req.getRatio(), "1:1"))
                .replace("{{resolution}}", or(req.getResolution(), "1080x1080"))
                .replace("{{watermarkLine}}", watermarkLine);
    }

    private static String or(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String fallback(String candidate, String def) {
        return candidate == null || candidate.isBlank() ? def : candidate;
    }

    private static String resolveSize(String resolution) {
        try {
            if (resolution != null && resolution.contains("x")) {
                String[] parts = resolution.split("x");
                int w = Integer.parseInt(parts[0]);
                int h = Integer.parseInt(parts[1]);
                int max = Math.max(w, h);
                if (max <= 1080) {
                    return "1K";
                }
                if (max <= 2048) {
                    return "2K";
                }
                return "4K";
            }
        } catch (Exception ignored) {
        }
        return "2K";
    }
}
