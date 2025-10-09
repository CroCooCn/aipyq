package com.aipyq.friendapp.ai.providers;

import com.aipyq.friendapp.ai.LLMProvider;
import com.aipyq.friendapp.api.dto.CopyCandidates;
import com.aipyq.friendapp.api.dto.CopyRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class VolcLlmProvider implements LLMProvider {
    private final VolcClient client;
    private final String textEndpoint;
    private final String visionEndpoint;
    private final String textModel;
    private final String visionModel;
    private final ObjectMapper mapper = new ObjectMapper();

    public VolcLlmProvider(VolcClient client, String textEndpoint, String visionEndpoint, String textModel, String visionModel) {
        this.client = client;
        this.textEndpoint = textEndpoint;
        this.visionEndpoint = visionEndpoint;
        this.textModel = textModel;
        this.visionModel = visionModel;
    }

    @Override
    public CopyCandidates generateCopy(CopyRequest req) {
        try {
            String prompt = buildPrompt(req);
            String resp;
            if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) {
                Map<String, Object> body = new HashMap<>();
                body.put("model", visionModel);
                List<Object> messages = new ArrayList<>();
                Map<String, Object> m = new HashMap<>();
                m.put("role", "user");
                List<Map<String, Object>> content = new ArrayList<>();
                Map<String, Object> img = new HashMap<>();
                img.put("type", "image_url");
                Map<String, Object> imgObj = new HashMap<>();
                imgObj.put("url", req.getImageUrl());
                img.put("image_url", imgObj);
                content.add(img);
                Map<String, Object> t = new HashMap<>();
                t.put("type", "text");
                t.put("text", prompt);
                content.add(t);
                m.put("content", content);
                messages.add(m);
                body.put("messages", messages);
                resp = client.postJson(visionEndpoint, mapper.writeValueAsString(body));
            } else {
                Map<String, Object> body = new HashMap<>();
                body.put("model", textModel);
                List<Object> messages = new ArrayList<>();
                Map<String, Object> m = new HashMap<>();
                m.put("role", "user");
                // 文本对话可直接用字符串 content
                m.put("content", prompt);
                messages.add(m);
                body.put("messages", messages);
                resp = client.postJson(textEndpoint, mapper.writeValueAsString(body));
            }
            CopyCandidates parsed = parseCopy(resp);
            if (parsed.getItems() == null || parsed.getItems().isEmpty()) {
                String raw = extractFirstText(mapper.readTree(resp));
                if (raw != null && !raw.isBlank()) {
                    CopyCandidates c = new CopyCandidates();
                    List<CopyCandidates.Item> items = new ArrayList<>();
                    CopyCandidates.Item it = new CopyCandidates.Item();
                    it.setId(UUID.randomUUID().toString());
                    it.setText(raw.trim());
                    items.add(it);
                    c.setItems(items);
                    return c;
                }
            }
            return parsed;
        } catch (Exception e) {
            CopyCandidates fallback = new CopyCandidates();
            List<CopyCandidates.Item> items = new ArrayList<>();
            CopyCandidates.Item it = new CopyCandidates.Item();
            it.setId(UUID.randomUUID().toString());
            it.setText("生成失败，请稍后重试");
            items.add(it);
            fallback.setItems(items);
            return fallback;
        }
    }

    @Override
    public String rewrite(String text, String instruction) {
        String prompt = "将这段文案按指令重写：" + instruction + "\n文案：" + text;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", textModel);
            List<Object> messages = new ArrayList<>();
            Map<String, Object> m = new HashMap<>();
            m.put("role", "user");
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> t = new HashMap<>();
            t.put("type", "input_text");
            t.put("text", prompt);
            content.add(t);
            m.put("content", content);
            messages.add(m);
            body.put("messages", messages);
            String resp = client.postJson(textEndpoint, mapper.writeValueAsString(body));
            JsonNode node = mapper.readTree(resp);
            String out = extractFirstText(node);
            if (out != null && !out.isBlank()) {
                return out;
            }
        } catch (Exception ignored) {
        }
        return text;
    }

    private String buildPrompt(CopyRequest req) {
        String tone = req.getPersona() != null ? nz(req.getPersona().getTone()) : "";
        String role = req.getPersona() != null ? nz(req.getPersona().getRole()) : "";
        String audience = req.getAudienceTags() != null ? String.join("/", req.getAudienceTags()) : "";
        return "你是中文社交文案创作者。基于图片与要素，生成3-5条朋友圈文案，每条80-120字，适当使用emoji，风格" + tone + "，身份" + role + "，面向" + audience + "。输出纯文本。";
    }

    private CopyCandidates parseCopy(String resp) throws Exception {
        JsonNode node = mapper.readTree(resp);
        String text = extractFirstText(node);
        List<CopyCandidates.Item> items = new ArrayList<>();
        if (text != null && !text.isBlank()) {
            List<String> parsed = parseCandidateText(text);
            for (String s : parsed) {
                CopyCandidates.Item it = new CopyCandidates.Item();
                it.setId(UUID.randomUUID().toString());
                it.setText(s);
                items.add(it);
            }
        }
        CopyCandidates c = new CopyCandidates();
        c.setItems(items);
        return c;
    }

    // 兼容火山 Ark 的多种返回结构
    private String extractFirstText(JsonNode node) {
        if (node == null) return null;
        JsonNode txt = node.at("/output/text");
        if (txt.isTextual()) return txt.asText();
        JsonNode rootChoices = node.path("choices");
        if (rootChoices.isArray() && rootChoices.size() > 0) {
            JsonNode msg = rootChoices.get(0).path("message");
            JsonNode content = msg.path("content");
            if (content.isTextual()) return content.asText();
        }
        JsonNode choices = node.at("/output/choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode msg = choices.get(0).path("message");
            JsonNode content = msg.path("content");
            if (content.isTextual()) return content.asText();
            if (content.isArray()) {
                for (JsonNode it : content) {
                    JsonNode t = it.get("text");
                    if (t != null && t.isTextual()) return t.asText();
                }
            }
        }
        JsonNode dt = node.at("/data/text");
        if (dt.isTextual()) return dt.asText();
        JsonNode dchoices = node.at("/data/choices");
        if (dchoices.isArray() && dchoices.size() > 0) {
            JsonNode msg = dchoices.get(0).path("message");
            JsonNode content = msg.path("content");
            if (content.isTextual()) return content.asText();
            if (content.isArray()) {
                for (JsonNode it : content) {
                    JsonNode t = it.get("text");
                    if (t != null && t.isTextual()) return t.asText();
                }
            }
        }
        JsonNode any = node.findValue("text");
        if (any != null && any.isTextual()) return any.asText();
        return null;
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static List<String> parseCandidateText(String raw) {
        String normalized = raw.replace("\r", "");
        normalized = normalized.replace("`", "");
        String[] blocks = normalized.split("\\n\\s*\\n");
        List<String> result = new ArrayList<>();
        for (String block : blocks) {
            String cleaned = cleanLeadingMarkers(block.trim());
            cleaned = cleaned.replaceAll("\\n+", " ");
            cleaned = cleaned.replaceAll("\\s+", " ").trim();
            if (cleaned.length() < 6) continue;
            result.add(cleaned);
        }
        if (result.isEmpty()) {
            for (String line : normalized.split("\\n")) {
                String cleaned = cleanLeadingMarkers(line.trim());
                if (cleaned.length() < 6) continue;
                result.add(cleaned);
            }
        }
        List<String> dedup = new ArrayList<>();
        for (String s : result) {
            if (!dedup.contains(s)) dedup.add(s);
        }
        return dedup;
    }

    private static String cleanLeadingMarkers(String text) {
        String s = text;
        s = s.replaceAll("^#+\\s*", "");
        s = s.replaceAll("^[-*\\u2022]+\\s*", "");
        s = s.replaceAll("^(\\u6587\\u6848|\\u5185\\u5BB9|\\u65B9\\u6848|\\u9009\\u9879|\\u63A8\\u8350|\\u6807\\u9898|copy|Copy|COPY|\\u6BB5\\u843D|\\u6848\\u4F8B)[\\s:\\uFF1A-]*", "");
        s = s.replaceAll("^(\\u6587\\u6848)?[\\u4E00-\\u4E5D\\u5341\\d][\\s:\\uFF1A\\u3001.\\-]*", "");
        s = s.replaceAll("^(\\d+\\.|\\d+\\u3001|\\d+\\uFF1A)\\s*", "");
        s = s.replaceAll("^[A-Za-z]\\)\\s*", "");
        return s.trim();
    }
}
