package com.aipyq.friendapp.ai.providers;

import com.aipyq.friendapp.ai.LLMProvider;
import java.util.List;

public class MockLlmProvider implements LLMProvider {
    @Override
    public List<String> generateCopy(String prompt, List<String> imageUrls, boolean reasoningOn) {
        return List.of(
                "文案生成暂不可用，请稍后重试。",
                "Mock 文案：今天的朋友圈要不要来点不一样的？",
                "Mock 文案：按下暂停键，收藏生活里的小确幸。"
        );
    }
}
