package com.aipyq.friendapp.service;

import com.aipyq.friendapp.ai.LLMProvider;
import com.aipyq.friendapp.api.dto.CopyCandidates;
import com.aipyq.friendapp.api.dto.CopyRequest;
import org.springframework.stereotype.Service;

@Service
public class GenerateService {
    private final LLMProvider provider;
    public GenerateService(LLMProvider provider) { this.provider = provider; }

    public CopyCandidates generate(CopyRequest request) {
        return provider.generateCopy(request);
    }

    public String rewrite(String text, String instruction) {
        return provider.rewrite(text, instruction);
    }
}

