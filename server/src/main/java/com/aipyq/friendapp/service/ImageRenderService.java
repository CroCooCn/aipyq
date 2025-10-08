package com.aipyq.friendapp.service;

import com.aipyq.friendapp.ai.ImageRenderProvider;
import com.aipyq.friendapp.api.dto.RenderRequest;
import com.aipyq.friendapp.api.dto.RenderResult;
import org.springframework.stereotype.Service;

@Service
public class ImageRenderService {

    private final ImageRenderProvider provider;
    public ImageRenderService(ImageRenderProvider provider) { this.provider = provider; }

    public RenderResult render(RenderRequest req) {
        return provider.render(req);
    }
}
