package com.aipyq.friendapp.ai.providers;

import com.aipyq.friendapp.ai.LLMProvider;
import com.aipyq.friendapp.api.dto.CopyCandidates;
import com.aipyq.friendapp.api.dto.CopyRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VolcLlmProvider implements LLMProvider {
    private final VolcClient client;
    private final String textEndpoint;
    private final String visionEndpoint;
    private final String textModel;
    private final String visionModel;
    private final ObjectMapper mapper = new ObjectMapper();

    public VolcLlmProvider(VolcClient client,
                           String textEndpoint,
                           String visionEndpoint,
                           String textModel,
                           String visionModel) {
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
            boolean hasImage = req.getImageUrl() != null && !req.getImageUrl().isBlank();
            if (hasImage) {
                try {
                    resp = callVisionModel(req, prompt);
                } catch (Exception visionError) {
                    System.err.println("Volc vision call failed, fallback to text: " + visionError.getMessage());
                    resp = callTextModel(req, prompt, true);
                }
            } else {
                resp = callTextModel(req, prompt, false);
            }

            CopyCandidates parsed = parseCopy(resp);
            if (parsed.getItems() == null || parsed.getItems().isEmpty()) {
                try {
                    String raw = extractFirstText(mapper.readTree(resp));
                    if (raw != null && !raw.isBlank()) {
                        CopyCandidates fallback = new CopyCandidates();
                        List<CopyCandidates.Item> items = new ArrayList<>();
                        CopyCandidates.Item it = new CopyCandidates.Item();
                        it.setId(UUID.randomUUID().toString());
                        it.setText(raw.trim());
                        items.add(it);
                        fallback.setItems(items);
                        return fallback;
                    }
                } catch (Exception ignored) {
                    // ignore and fall through
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
            System.err.println("Volc generateCopy error: " + e.getMessage());
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
        String instruction = req.getInstruction() != null ? req.getInstruction().trim() : "";
        String base = "你是中文社交文案创作者。基于图片与要素，生成3-5条朋友圈文案，每条80-120字，适当使用emoji，风格"
                + tone + "，身份" + role + "，面向" + audience + "。输出纯文本。";
        if (!instruction.isEmpty()) {
            base += " 修改指令：" + instruction;
        }
        return base;
    }

    private CopyCandidates parseCopy(String resp) throws Exception {
        String text = null;
        try {
            JsonNode node = mapper.readTree(resp);
            text = extractFirstText(node);
        } catch (Exception ignored) {
            if (resp != null && !resp.isBlank()) {
                text = resp;
            }
        }

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

    private String callVisionModel(CopyRequest req, String prompt) throws Exception {
        Map<String, Object> body = new HashMap<>();
        String model = (visionModel != null && !visionModel.isBlank()) ? visionModel : textModel;
        body.put("model", model);
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

        Map<String, Object> txt = new HashMap<>();
        txt.put("type", "text");
        txt.put("text", prompt);
        content.add(txt);

        m.put("content", content);
        messages.add(m);
        body.put("messages", messages);
        if (req.getReasoningOn() != null) {
            Map<String, Object> thinking = new HashMap<>();
            thinking.put("type", req.getReasoningOn() ? "enabled" : "disabled");
            body.put("thinking", thinking);
        }
        return client.postJson(visionEndpoint, mapper.writeValueAsString(body));
    }

    private String callTextModel(CopyRequest req, String prompt, boolean hasImage) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", textModel);
        List<Object> messages = new ArrayList<>();
        Map<String, Object> m = new HashMap<>();
        m.put("role", "user");
        String content = hasImage
                ? prompt + "\n请结合上述图片内容生成文案，图片地址：" + req.getImageUrl()
                : prompt;
        m.put("content", content);
        messages.add(m);
        body.put("messages", messages);
        if (req.getReasoningOn() != null) {
            Map<String, Object> thinking = new HashMap<>();
            thinking.put("type", req.getReasoningOn() ? "enabled" : "disabled");
            body.put("thinking", thinking);
        }
        return client.postJson(textEndpoint, mapper.writeValueAsString(body));
    }

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

        JsonNode dChoices = node.at("/data/choices");
        if (dChoices.isArray() && dChoices.size() > 0) {
            JsonNode msg = dChoices.get(0).path("message");
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

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static List<String> parseCandidateText(String raw) {
        String normalized = raw == null ? "" : raw;
        normalized = normalized.replace("\r", "").replace("`", "");
        normalized = normalized.replace("\r\n", "\n");
        normalized = normalized.replace("\\r\\n", "\n");
        normalized = normalized.replace("\\n", "\n");
        normalized = normalized.replace("\\r", "\n");
        normalized = normalized.replace('\u2028', '\n');
        normalized = normalized.replace('\u2029', '\n');

        normalized = normalized.replaceAll("(?<=\\S)[nN](?=\\d+[\\.．、:：)）])", "\n");
        normalized = normalized.replaceAll("(?<=\\S)[nN](?=\\d+\\s)", "\n");
        for (int i = 1; i <= 20; i++) {
            String num = String.valueOf(i);
            normalized = normalized.replace("n" + num + ".", "\n" + num + ".");
            normalized = normalized.replace("n" + num + "。", "\n" + num + "。");
            normalized = normalized.replace("n" + num + "、", "\n" + num + "、");
            normalized = normalized.replace("n" + num + ":", "\n" + num + ":");
            normalized = normalized.replace("n" + num + "：", "\n" + num + "：");
            normalized = normalized.replace("n" + num + ")", "\n" + num + ")");
            normalized = normalized.replace("n" + num + "）", "\n" + num + "）");
            normalized = normalized.replace("N" + num + ".", "\n" + num + ".");
            normalized = normalized.replace("N" + num + "。", "\n" + num + "。");
            normalized = normalized.replace("N" + num + "、", "\n" + num + "、");
            normalized = normalized.replace("N" + num + ":", "\n" + num + ":");
            normalized = normalized.replace("N" + num + "：", "\n" + num + "：");
            normalized = normalized.replace("N" + num + ")", "\n" + num + ")");
            normalized = normalized.replace("N" + num + "）", "\n" + num + "）");
        }

        normalized = normalized.replaceAll("(?m)^(以下是.*?(朋友圈文案|生成).*)[:：]\\s*$", "");

        final String inlineSepRegex = "([\\s\\u00A0]+)(?=([（(]?(?:[一二三四五六七八九十]|[0-9]|[①②③④⑤⑥⑦⑧⑨⑩])+[\\.．、:：)）]\\s+))";
        normalized = normalized.replaceAll(inlineSepRegex, "\n");
        normalized = normalized.replaceAll("(?m)^[\\u2022•]\\s*", "");

        String[] blocks = normalized.split("\\n\\s*\\n");
        List<String> results = new ArrayList<>();
        for (String block : blocks) {
            String trimmed = block.trim();
            if (trimmed.isEmpty()) continue;
            List<String> lines = new ArrayList<>();
            for (String line : trimmed.split("\\n")) {
                String l = line.trim();
                if (l.isEmpty()) continue;
                String preprocessed = l.replaceAll(inlineSepRegex, "\n");
                for (String piece : preprocessed.split("\\n")) {
                    String cleaned = cleanLeadingMarkers(piece);
                    cleaned = cleaned.replaceAll("\\s+", " ").trim();
                    if (cleaned.length() < 8 || isPreamble(cleaned)) {
                        continue;
                    }
                    lines.add(cleaned);
                }
            }
            if (!lines.isEmpty()) {
                results.addAll(lines);
            }
        }

        if (results.isEmpty()) {
            for (String line : normalized.split("\\n")) {
                String cleaned = cleanLeadingMarkers(line.trim());
                if (cleaned.length() < 8 || isPreamble(cleaned)) {
                    continue;
                }
                results.add(cleaned);
            }
        }

        List<String> dedup = new ArrayList<>();
        for (String s : results) {
            if (!dedup.contains(s)) {
                dedup.add(s);
            }
        }
        return dedup;
    }

    private static boolean isPreamble(String s) {
        String t = s.trim();
        if (t.isEmpty()) return true;
        return t.matches("^(以下是.*?(朋友圈文案|生成).*)[:：]?")
                || t.matches("^(候选文案|文案如下|输出如下)[:：]?")
                || t.equalsIgnoreCase("copy:")
                || t.equalsIgnoreCase("copies:");
    }

    private static String cleanLeadingMarkers(String text) {
        String s = text;
        s = s.replaceAll("^#+\\s*", "");
        s = s.replaceAll("^[-*\\u2022]+\\s*", "");
        s = s.replaceAll("^(以下是.*?(朋友圈文案|生成).*)[:：]?\\s*", "");
        s = s.replaceAll("^(\\u6587\\u6848|\\u5185\\u5BB9|\\u65B9\\u6848|\\u9009\\u9879|\\u63A8\\u8350|\\u6807\\u9898|copy|Copy|COPY|\\u6BB5\\u843D|\\u6848\\u4F8B)[\\s:\\uFF1A-]*", "");
        s = s.replaceAll("^(?:[（(]?[一二三四五六七八九十\\d①②③④⑤⑥⑦⑧⑨⑩]+[)）]?)[\\.．、:：)）]?\\s*", "");
        s = s.replaceAll("^[A-Za-z]\\)\\s*", "");
        return s.trim();
    }
}
