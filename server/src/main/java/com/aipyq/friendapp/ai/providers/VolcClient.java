package com.aipyq.friendapp.ai.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class VolcClient {
    private static final Logger log = LoggerFactory.getLogger(VolcClient.class);
    private final WebClient webClient;
    private final String baseUrl;
    public VolcClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public String postJson(String path, String jsonBody) {
        String normalizedPath = path;
        if (normalizedPath == null || normalizedPath.isBlank()) {
            normalizedPath = "/";
        } else if (!normalizedPath.startsWith("http") && !normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath; // 保证相对路径前有 '/'
        }
        String full = normalizedPath.startsWith("http") ? normalizedPath : baseUrl + normalizedPath;
        log.info("Volc POST Path: {}", full);
        log.debug("Volc POST body: {}", jsonBody);
        return webClient.post()
                .uri(normalizedPath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(jsonBody))
                .exchangeToMono(resp -> resp.bodyToMono(String.class)
                        .map(body -> {
                            if (resp.statusCode().isError()) {
                                log.error("Volc {} {}\nrequest={}\nresponse={}", resp.statusCode(), full, jsonBody, body);
                            }
                            return body;
                        }))
                .onErrorResume(e -> {
                    log.error("Volc error: {}", e.toString());
                    return Mono.just("{" + "\"error\":\"" + e.getMessage() + "\"}");
                })
                .block();
    }
}
