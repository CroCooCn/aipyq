package com.aipyq.friendapp.config;

import com.aipyq.friendapp.ai.ImageRenderProvider;
import com.aipyq.friendapp.ai.LLMProvider;
import com.aipyq.friendapp.ai.providers.VolcClient;
import com.aipyq.friendapp.ai.providers.VolcImageRenderProvider;
import com.aipyq.friendapp.ai.providers.VolcLlmProvider;
import com.aipyq.friendapp.ai.providers.MockLlmProvider;
import com.aipyq.friendapp.api.dto.RenderResult;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AiProperties.class, PromptProperties.class})
public class AiConfig {
    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Bean
    public LLMProvider llmProvider(AiProperties props) {
        if ("volc".equalsIgnoreCase(props.getProvider())) {
            VolcClient c = new VolcClient(nz(props.getVolcBaseUrl()), nz(props.getVolcApiKey()));
            log.info("AI text provider: volc (textModel={})", nz(props.getVolcTextModel()));
            return new VolcLlmProvider(
                    c,
                    nz(props.getVolcTextEndpoint() != null ? props.getVolcTextEndpoint() : "/chat/completions"),
                    nz(props.getVolcTextModel() != null ? props.getVolcTextModel() : "doubao-1-5-pro-32k-250115"),
                    nz(props.getVolcVisionEndpoint()),
                    nz(props.getVolcVisionModel())
            );
        }
        log.info("AI text provider: mock");
        return new MockLlmProvider();
    }

    @Bean
    public ImageRenderProvider imageRenderProvider(AiProperties props, PromptProperties promptProperties) {
        if ("volc".equalsIgnoreCase(props.getProvider())) {
            VolcClient c = new VolcClient(nz(props.getVolcBaseUrl()), nz(props.getVolcApiKey()));
            log.info("Image provider: volc (imageEditModel={})", nz(props.getVolcImageEditModel()));
            if (startsWithEp(props.getVolcImageEditEndpoint())) {
                log.warn("ai.volc-image-edit-endpoint looks like an endpoint_id ({}). It should be a PATH like /images/edits. Move ep-* to ai.volc-image-edit-model.", props.getVolcImageEditEndpoint());
            }
            if (!startsWithEp(props.getVolcImageEditModel())) {
                log.warn("ai.volc-image-edit-model doesn't look like an endpoint_id ({}). Ensure it's correct (e.g., ep-xxxx).", props.getVolcImageEditModel());
            }
            return new VolcImageRenderProvider(
                    c,
                    nz(props.getVolcImageEditEndpoint()),
                    nz(props.getVolcImageEditModel()),
                    promptProperties
            );
        }
        log.info("Image provider: mock");
        return request -> {
            RenderResult fallback = new RenderResult();
            if (request != null && request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
                fallback.setImages(List.of(request.getImageUrl()));
            } else {
                fallback.setImages(List.of());
            }
            fallback.setGenerationId(UUID.randomUUID().toString());
            return fallback;
        };
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static boolean startsWithEp(String s) { return s != null && s.startsWith("ep-"); }
}



