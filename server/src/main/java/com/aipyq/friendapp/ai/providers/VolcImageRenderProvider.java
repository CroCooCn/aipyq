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
        String copy = req.getCopyText() == null ? "" : req.getCopyText();
        String color = or(req.getPrimaryColor(), "#111111");
        String font = or(req.getFontFamily(), "system-ui");
        String ratio = or(req.getRatio(), "1:1");
        String resolution = or(req.getResolution(), "1080x1080");
        int fontSize = req.getFontSize() != null ? req.getFontSize().intValue() : 36;
        double lineHeight = req.getLineHeight() != null ? req.getLineHeight() : 1.4;
        int margin = req.getMargin() != null ? req.getMargin().intValue() : 24;
        boolean grid = req.getGrid() != null && Boolean.TRUE.equals(req.getGrid().getEnabled());
        boolean watermark = req.getWatermarkOn() != null && req.getWatermarkOn();

        StringBuilder sb = new StringBuilder();
        sb.append("任务：在给定图片上，排版并叠加以下中文文案，用于发布微信朋友圈。\n");
        sb.append("文案：\n").append(escapeForPrompt(copy)).append("\n\n");

        sb.append("要求：\n");
        sb.append("1) 不裁切主体，优先保留原图构图；输出分辨率为 ").append(resolution).append("，画面比例 ").append(ratio).append("。\n");
        sb.append("2) 自动选择不遮挡主体的空白区域放置文字；若对比度不足，添加半透明深色文字背景条（圆角），保证可读性。\n");
        sb.append("3) 字体：").append(font).append("，字号约 ").append(fontSize).append("px（随分辨率等比），行距 ").append(lineHeight).append(" 倍，颜色 ").append(color).append("；确保与背景对比度 ≥ 4.5:1，必要时自动反色或加描边。\n");
        sb.append("4) 段落优化：按中文阅读习惯自动换行，避免过长一行；保留表情与标点；不新增无关口号。\n");
        sb.append("5) 页面安全边距：四周预留 ≥ ").append(margin).append("px，避免贴边。\n");
        sb.append("6) 仅使用提供的文案内容，不额外添加文字或水印；");
        if (watermark) {
            sb.append("另外在右下角添加极小号半透明水印（10% 不透明度），不影响主体与文案阅读；");
        }
        sb.append("保持整体干净、现代、可读。\n");

        if (grid) {
            sb.append("7) 额外输出九宫格版本（3x3）用于朋友圈，确保主体不被分割，按左到右、上到下顺序返回9张切片。\n");
        }

        sb.append("输出：返回编辑后的图片URL（或Base64）数组；若包含九宫格，请先返回完整图，再返回九宫格9张。\n");
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
