package com.aipyq.friendapp.ai.providers;

import com.aipyq.friendapp.ai.ImageRenderProvider;
import com.aipyq.friendapp.api.dto.RenderRequest;
import com.aipyq.friendapp.api.dto.RenderResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class VolcImageRenderProvider implements ImageRenderProvider {
    private final VolcClient client;
    private final String imageEditEndpoint;
    private final String imageEditModel;
    private final ObjectMapper mapper = new ObjectMapper();

    public VolcImageRenderProvider(VolcClient client, String imageEditEndpoint, String imageEditModel) {
        this.client = client;
        this.imageEditEndpoint = imageEditEndpoint;
        this.imageEditModel = imageEditModel;
    }

    @Override
    public RenderResult render(RenderRequest request) {
        try {
            String instruction = buildInstruction(request);
            // 遵循 images/generations 规范：image + prompt
            String size = resolveSize(request.getResolution());
            java.util.Map<String,Object> body = new java.util.HashMap<>();
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
                    if (u != null && u.isTextual()) urls.add(u.asText());
                }
            }
            RenderResult r = new RenderResult();
            if (urls.isEmpty()) urls.add(request.getImageUrl());
            r.setImages(urls);
            return r;
        } catch (Exception e) {
            RenderResult r = new RenderResult();
            r.setImages(List.of(request.getImageUrl()));
            return r;
        }
    }

    private String buildInstruction(RenderRequest req) {
        String ratio = or(req.getRatio(), "1:1");
        String resolution = or(req.getResolution(), "1080x1080");
        boolean watermark = req.getWatermarkOn() != null && req.getWatermarkOn();

        StringBuilder sb = new StringBuilder();
        sb.append("任务：将给定图片优化为更适合发布到微信朋友圈的风格，不叠加任何文字。\n");
        sb.append("要求：\n");
        sb.append("1) 画面比例 ").append(ratio).append("，输出分辨率 ").append(resolution).append("；尽量不裁掉关键主体。\n");
        sb.append("2) 色彩与光影：提升整体亮度/对比度与通透度，适度强调主体；可轻微调整白平衡与饱和度，让画面更有质感。\n");
        sb.append("3) 氛围：可添加自然、轻度的胶片/质感滤镜，避免过度锐化和过重特效；可以轻微暗角以聚焦主体。\n");
        sb.append("4) 降噪与细节：适度去噪与修复压缩痕迹，保持皮肤与材质自然。\n");
        sb.append("5) 不添加任何新元素或文字，不改变原有语义。\n");
        if (watermark) {
            sb.append("6) 在右下角添加极小号半透明水印（10% 不透明度），不干扰主体。\n");
        }
        sb.append("输出：返回优化后的图片URL（或Base64）。\n");
        return sb.toString();
    }

    private static String or(String v, String d) { return v == null || v.isBlank() ? d : v; }
    private static String escapeForPrompt(String s) { return s.replace("\n", "\\n"); }

    private static String esc(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static String resolveSize(String resolution) {
        // 粗略映射：<=1080 → 1K；<=2048 → 2K；其他 4K
        try {
            if (resolution != null && resolution.contains("x")) {
                String[] p = resolution.split("x");
                int w = Integer.parseInt(p[0]);
                int h = Integer.parseInt(p[1]);
                int max = Math.max(w, h);
                if (max <= 1080) return "1K";
                if (max <= 2048) return "2K";
                return "4K";
            }
        } catch (Exception ignored) {}
        return "2K";
    }
}
