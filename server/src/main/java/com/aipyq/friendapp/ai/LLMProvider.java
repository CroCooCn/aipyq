package com.aipyq.friendapp.ai;

import com.aipyq.friendapp.api.dto.CopyCandidates;
import com.aipyq.friendapp.api.dto.CopyRequest;

public interface LLMProvider {
    CopyCandidates generateCopy(CopyRequest request);
    String rewrite(String text, String instruction);
}

