package com.aipyq.friendapp.service;

import com.aipyq.friendapp.api.dto.ImageInsights;
import com.aipyq.friendapp.config.AiProperties;
import com.aipyq.friendapp.ai.providers.VolcClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VisionService {
    private final AiProperties props;
    private final VolcClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public VisionService(AiProperties props) {
        this.props = props;
        this.client = new VolcClient(props.getVolcBaseUrl(), props.getVolcApiKey());
    }

    public ImageInsights analyze(String imageUrl) {
        ImageInsights out = new ImageInsights();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", nz(props.getVolcVisionModel()));
            List<Object> messages = new ArrayList<>();
            Map<String, Object> m = new HashMap<>();
            m.put("role", "user");
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> img = new HashMap<>();
            img.put("type", "image_url");
            Map<String, Object> imgObj = new HashMap<>();
            imgObj.put("url", imageUrl);
            img.put("image_url", imgObj);
            content.add(img);
            Map<String, Object> t = new HashMap<>();
            t.put("type", "text");
            t.put("text", PROMPT_JSON);
            content.add(t);
            m.put("content", content);
            messages.add(m);
            body.put("messages", messages);
            String resp = client.postJson(nz(props.getVolcVisionEndpoint()), mapper.writeValueAsString(body));
            parseResponse(resp, out);
        } catch (Exception e) {
            out.setSummary("图片解析失败，请稍后重试");
            out.setLabels(List.of());
        }
        return out;
    }

    private void parseResponse(String resp, ImageInsights out) throws Exception {
        JsonNode node = mapper.readTree(resp);
        // 根 choices.message.content 为 JSON 文本
        JsonNode choices = node.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            String content = choices.get(0).path("message").path("content").asText("");
            if (!content.isBlank()) {
                try {
                    JsonNode j = mapper.readTree(content);
                    String summary = j.path("summary").asText("");
                    List<String> labels = new ArrayList<>();
                    JsonNode arr = j.path("labels");
                    if (arr.isArray()) for (JsonNode it : arr) labels.add(it.asText());
                    if (labels.isEmpty()) labels = guessTags(summary);
                    out.setSummary(summary);
                    out.setLabels(labels);
                    out.setOcrText(null);
                    return;
                } catch (Exception ignored) {
                    // content 不是 JSON，退化为 summary
                    String summary = content.trim();
                    out.setSummary(summary);
                    out.setLabels(guessTags(summary));
                    out.setOcrText(null);
                    return;
                }
            }
        }
        // 兜底
        out.setSummary("未能识别图片内容");
        out.setLabels(List.of());
        out.setOcrText(null);
    }

    private static List<String> guessTags(String summary) {
        if (summary == null || summary.isBlank()) return List.of();
        String s = summary.replaceAll("[。！？，、,.!?:]", " ");
        String[] tokens = s.split("\\s+");
        List<String> out = new ArrayList<>();
        for (String tk : tokens) {
            if (tk.length() >= 2 && out.size() < 6) out.add(tk);
        }
        return out;
    }

    private static final String PROMPT_JSON = "请你分析图片内容，并严格输出 JSON：{\n  \"summary\": \"不超过30字的中文概括\",\n  \"labels\": [\"3-6个中文标签\"]\n}\n不要输出多余解释。";
    private static String nz(String s) { return s == null ? "" : s; }
}

