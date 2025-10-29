package com.aipyq.friendapp.ai;

import com.aipyq.friendapp.api.dto.RenderRequest;
import com.aipyq.friendapp.api.dto.RenderResult;

public interface ImageRenderProvider {
    RenderResult render(RenderRequest request);
}

