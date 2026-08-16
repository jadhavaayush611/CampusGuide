package com.campusguide.personal.ai.atlas.config;

import com.campusguide.personal.ai.atlas.provider.AIProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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

    @Bean(name = "groqRestClient")
    public RestClient groqRestClient() {
        return RestClient.builder()
                .baseUrl(atlasProperties.getProviders().getGroq().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + atlasProperties.getProviders().getGroq().getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "targetAiProvider")
    public AIProvider targetAiProvider(
            @Qualifier("openAIProvider") AIProvider openAiProvider,
            @Qualifier("groqProvider") AIProvider groqProvider) {
        if ("groq".equalsIgnoreCase(atlasProperties.getDefaultProvider())) {
            return groqProvider;
        }
        return openAiProvider;
    }
}
