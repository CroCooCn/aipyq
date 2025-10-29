package com.aipyq.friendapp.ai.providers;

import com.aipyq.friendapp.ai.LLMProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class VolcLlmProvider implements LLMProvider {

    private static final Logger log = LoggerFactory.getLogger(VolcLlmProvider.class);
    private static final Pattern ORDERED_PREFIX =
            Pattern.compile("^\\s*(?:\\d+\\.|[\\u2460-\\u2468]|[-*•])\\s*(.*)$");

    private final VolcClient client;
    private final String textEndpoint;
    private final String textModel;
    private final String visionEndpoint;
    private final String visionModel;
    private final ObjectMapper mapper = new ObjectMapper();

    public VolcLlmProvider(VolcClient client,
                           String textEndpoint,
                           String textModel,
                           String visionEndpoint,
                           String visionModel) {
        this.client = client;
        this.textEndpoint = textEndpoint;
        this.textModel = textModel;
        this.visionEndpoint = visionEndpoint;
        this.visionModel = visionModel;
    }

    @Override
    public List<String> generateCopy(String prompt, List<String> imageUrls, boolean reasoningOn) {
        List<String> sanitizedUrls = sanitizeImageUrls(imageUrls);
        boolean hasImage = !sanitizedUrls.isEmpty();
        List<Exception> errors = new ArrayList<>();

        if (hasImage && StringUtils.hasText(visionEndpoint) && StringUtils.hasText(visionModel)) {
            try {
                List<String> visionResult = callVision(prompt, sanitizedUrls, reasoningOn);
                if (!visionResult.isEmpty()) {
                    return visionResult;
                }
            } catch (Exception ex) {
                errors.add(ex);
                log.warn("Vision model call failed: {}", ex.toString());
            }
        }

        try {
            return callText(prompt, sanitizedUrls, reasoningOn);
        } catch (Exception ex) {
            errors.add(ex);
            log.error("Text model call failed: {}", ex.toString());
        }

        errors.forEach(e -> log.debug("volc llm error detail", e));
        return List.of("文案生成失败，请稍后重试。");
    }

    private List<String> callVision(String prompt, List<String> imageUrls, boolean reasoningOn) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", visionModel);

        List<Object> messages = new ArrayList<>();
        Map<String, Object> user = new HashMap<>();
        user.put("role", "user");
        List<Map<String, Object>> content = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            Map<String, Object> imageObj = new HashMap<>();
            imageObj.put("url", imageUrl);
            imageContent.put("image_url", imageObj);
            content.add(imageContent);
        }

        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", augmentPrompt(prompt, reasoningOn));
        content.add(textContent);

        user.put("content", content);
        messages.add(user);
        body.put("messages", messages);

        String response = client.postJson(visionEndpoint, mapper.writeValueAsString(body));
        return parseResponse(response);
    }

    private List<String> callText(String prompt, List<String> imageUrls, boolean reasoningOn) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", textModel);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "You are a helpful assistant that writes Chinese social media copy for WeChat Moments."
        ));
        StringBuilder userPrompt = new StringBuilder(prompt);
        if (!imageUrls.isEmpty()) {
            userPrompt.append("\n请结合以下图片内容进行创作：\n");
            for (String imageUrl : imageUrls) {
                userPrompt.append("- ").append(imageUrl).append('\n');
            }
        }
        if (reasoningOn) {
            userPrompt.append("\n\n请先在脑海中思考合适的创作方向，再给出最终文案（无需显式输出思考过程）。");
        }
        messages.add(Map.of("role", "user", "content", userPrompt.toString()));
        body.put("messages", messages);

        String response = client.postJson(textEndpoint, mapper.writeValueAsString(body));
        return parseResponse(response);
    }

    private String augmentPrompt(String prompt, boolean reasoningOn) {
        if (!reasoningOn) {
            return prompt;
        }
        return prompt + "\n\n请先在脑海中思考合适的创作方向，再给出最终文案（无需显式输出思考过程）。";
    }

    private List<String> parseResponse(String response) throws Exception {
        if (response == null || response.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(response);
        if (root.has("error")) {
            log.warn("LLM returned error: {}", response);
            return List.of();
        }
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return List.of();
        }
        JsonNode message = choices.get(0).path("message");
        String content = message.path("content").asText("");
        if (content.isBlank()) {
            return List.of();
        }
        return splitSuggestions(content);
    }

    private List<String> splitSuggestions(String raw) {
        String normalized = raw
                .replace("\r", "\n")
                .replaceAll("(?m)^Copy \\d+[:：]\\s*", "")
                .trim();

        String[] lines = normalized.split("\\n");
        List<String> results = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                flushCurrent(results, current);
                continue;
            }
            Matcher matcher = ORDERED_PREFIX.matcher(trimmed);
            if (matcher.matches()) {
                flushCurrent(results, current);
                current.append(matcher.group(1).trim());
            } else {
                if (current.length() > 0) {
                    current.append(' ');
                }
                current.append(trimmed);
            }
        }
        flushCurrent(results, current);

        if (results.isEmpty()) {
            results.add(normalized);
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String item : results) {
            String text = item.trim();
            if (!text.isEmpty()) {
                unique.add(text);
            }
            if (unique.size() >= 5) {
                break;
            }
        }
        if (unique.isEmpty()) {
            unique.add("文案生成失败，请稍后重试。");
        }
        return new ArrayList<>(unique);
    }

    private void flushCurrent(List<String> results, StringBuilder current) {
        if (current.length() == 0) {
            return;
        }
        results.add(current.toString().trim());
        current.setLength(0);
    }

    private List<String> sanitizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        List<String> sanitized = new ArrayList<>();
        for (String url : imageUrls) {
            if (StringUtils.hasText(url)) {
                sanitized.add(url.trim());
            }
        }
        return sanitized;
    }
}
