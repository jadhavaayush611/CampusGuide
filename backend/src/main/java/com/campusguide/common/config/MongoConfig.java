package com.campusguide.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.mongodb.MongoMetricsCommandListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {
    // Programmatic index validation is performed by MigrationRunner.
    // JSR-380 validation constraints are defined directly on entities and validated at the API/Service layers.

    @Bean
    public MongoMetricsCommandListener mongoMetricsCommandListener(MeterRegistry meterRegistry) {
        return new MongoMetricsCommandListener(meterRegistry);
    }
}

