package com.aipyq.friendapp.config;

import com.aipyq.friendapp.ai.ImageRenderProvider;
import com.aipyq.friendapp.ai.LLMProvider;
import com.aipyq.friendapp.ai.providers.MockImageRenderProvider;
import com.aipyq.friendapp.ai.providers.MockLLMProvider;
import com.aipyq.friendapp.ai.providers.VolcClient;
import com.aipyq.friendapp.ai.providers.VolcImageRenderProvider;
import com.aipyq.friendapp.ai.providers.VolcLlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AiProperties.class, QuotaProperties.class})
public class AiConfig {
    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);
    @Bean
    public LLMProvider llmProvider(AiProperties props) {
        if ("volc".equalsIgnoreCase(props.getProvider())) {
            VolcClient c = new VolcClient(nz(props.getVolcBaseUrl()), nz(props.getVolcApiKey()));
            log.info("AI provider: volc (textModel={}, visionModel={})", nz(props.getVolcTextModel()), nz(props.getVolcVisionModel()));
            if (startsWithEp(props.getVolcTextEndpoint())) {
                log.warn("ai.volc-text-endpoint looks like an endpoint_id ({}). It should be a PATH like /chat/completions. Move ep-* to ai.volc-text-model.", props.getVolcTextEndpoint());
            }
            if (startsWithEp(props.getVolcVisionEndpoint())) {
                log.warn("ai.volc-vision-endpoint looks like an endpoint_id ({}). It should be a PATH like /chat/completions. Move ep-* to ai.volc-vision-model.", props.getVolcVisionEndpoint());
            }
            if (!startsWithEp(props.getVolcTextModel())) {
                log.warn("ai.volc-text-model doesn't look like an endpoint_id ({}). Ensure it's correct (e.g., ep-xxxx).", props.getVolcTextModel());
            }
            if (!startsWithEp(props.getVolcVisionModel())) {
                log.warn("ai.volc-vision-model doesn't look like an endpoint_id ({}). Ensure it's correct (e.g., ep-xxxx).", props.getVolcVisionModel());
            }
            return new VolcLlmProvider(c, nz(props.getVolcTextEndpoint()), nz(props.getVolcVisionEndpoint()), nz(props.getVolcTextModel()), nz(props.getVolcVisionModel()));
        }
        log.info("AI provider: mock");
        return new MockLLMProvider();
    }

    @Bean
    public ImageRenderProvider imageRenderProvider(AiProperties props) {
        if ("volc".equalsIgnoreCase(props.getProvider())) {
            VolcClient c = new VolcClient(nz(props.getVolcBaseUrl()), nz(props.getVolcApiKey()));
            log.info("Image provider: volc (imageEditModel={})", nz(props.getVolcImageEditModel()));
            if (startsWithEp(props.getVolcImageEditEndpoint())) {
                log.warn("ai.volc-image-edit-endpoint looks like an endpoint_id ({}). It should be a PATH like /images/edits. Move ep-* to ai.volc-image-edit-model.", props.getVolcImageEditEndpoint());
            }
            if (!startsWithEp(props.getVolcImageEditModel())) {
                log.warn("ai.volc-image-edit-model doesn't look like an endpoint_id ({}). Ensure it's correct (e.g., ep-xxxx).", props.getVolcImageEditModel());
            }
            return new VolcImageRenderProvider(c, nz(props.getVolcImageEditEndpoint()), nz(props.getVolcImageEditModel()));
        }
        log.info("Image provider: mock");
        return new MockImageRenderProvider();
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static boolean startsWithEp(String s) { return s != null && s.startsWith("ep-"); }
}
