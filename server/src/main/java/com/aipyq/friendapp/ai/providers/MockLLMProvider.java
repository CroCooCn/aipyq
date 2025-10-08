package com.aipyq.friendapp.ai.providers;

import com.aipyq.friendapp.ai.LLMProvider;
import com.aipyq.friendapp.api.dto.CopyCandidates;
import com.aipyq.friendapp.api.dto.CopyRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MockLLMProvider implements LLMProvider {

    @Override
    public CopyCandidates generateCopy(CopyRequest req) {
        List<String> tags = req.getImageTags() != null ? req.getImageTags() : List.of();
        String tagLine = tags.stream().limit(3).collect(Collectors.joining("·"));
        String tone = req.getPersona() != null ? safe(req.getPersona().getTone()) : "";
        String style = req.getStylePreset() != null ? req.getStylePreset() : "lively";

        List<String> samples = new ArrayList<>();
        samples.add(fmt("%s %s %s", tagLine, pick(style, 0), emoji(style)));
        samples.add(fmt("%s %s", pick(style, 1), emoji(style)));
        samples.add(fmt("今天也要元气满满 %s %s", tagLine, emoji(style)));

        CopyCandidates out = new CopyCandidates();
        List<CopyCandidates.Item> items = new ArrayList<>();
        for (String s : samples) {
            CopyCandidates.Item it = new CopyCandidates.Item();
            it.setId(UUID.randomUUID().toString());
            it.setText(s.trim());
            items.add(it);
        }
        out.setItems(items);
        return out;
    }

    @Override
    public String rewrite(String text, String instruction) {
        if (instruction == null || instruction.isBlank()) return text;
        if (instruction.contains("更短")) {
            return text.length() > 30 ? text.substring(0, 30) + "…" : text;
        }
        if (instruction.contains("更专业")) {
            return text + " ｜ 专业视角 · 关键要点";
        }
        if (instruction.contains("更生活化")) {
            return text + " 就这么简单，开心就好。";
        }
        if (instruction.contains("少营销")) {
            return text.replaceAll("(?i)[#|｜|·].*$", "");
        }
        return text + "（已根据指令微调）";
    }

    private static String emoji(String style) {
        switch (style) {
            case "professional": return "📌";
            case "healing": return "🌿";
            case "humorous": return "😆";
            case "cold": return "·";
            default: return "✨";
        }
    }

    private static String pick(String style, int idx) {
        String[][] options = new String[][]{
                {"小确幸来了", "把喜欢过成日常"},
                {"认真对待每一杯", "把细节做到位"},
                {"温柔以待", "慢慢来会更好"},
                {"也就那回事", "无糖更清醒"},
                {"元气续上", "步履不停"}
        };
        int s = Math.abs(style.hashCode()) % options.length;
        int i = (s + idx) % options.length;
        return options[i][idx % options[i].length];
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static String fmt(String f, Object... args) {
        return String.format(f, args);
    }
}

