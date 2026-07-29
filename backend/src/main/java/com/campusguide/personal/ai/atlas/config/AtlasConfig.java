package com.campusguide.personal.ai.atlas.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class AtlasConfig {

    private final AtlasProperties atlasProperties;

    @Bean(name = "openAiRestClient")
    public RestClient openAiRestClient() {
        return RestClient.builder()
                .baseUrl(atlasProperties.getProviders().getOpenai().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + atlasProperties.getProviders().getOpenai().getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
