package com.aipyq.friendapp.ai;

import java.util.List;

public interface LLMProvider {
    List<String> generateCopy(String prompt, List<String> imageUrls, boolean reasoningOn);
}
