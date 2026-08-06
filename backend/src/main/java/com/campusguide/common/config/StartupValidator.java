package com.campusguide.common.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Validates the application configuration at startup to fail-fast if critical
 * settings are missing, invalid, or insecure.
 */
@Component
@Slf4j
public class StartupValidator {

    private final Environment environment;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${ai.gateway.base-url:}")
    private String aiGatewayBaseUrl;

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:0}")
    private int mailPort;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private List<String> allowedOrigins;

    public StartupValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validate() {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean isProduction = activeProfiles.contains("prod") || activeProfiles.contains("production");
        boolean isStaging = activeProfiles.contains("staging");

        log.info("Running Startup Configuration Validation. Active profiles: {}", activeProfiles);

        // 1. Validate JWT Secret
        if (jwtSecret == null || jwtSecret.isBlank()) {
            if (isProduction || isStaging) {
                throw new IllegalStateException("Startup Failed: JWT Secret (jwt.secret) must not be empty in production/staging.");
            } else {
                log.warn("JWT Secret is empty. This is acceptable only in development/testing.");
            }
        } else if ("default_jwt_secret_key_for_campusguide_development_32_chars_or_more".equals(jwtSecret)) {
            if (isProduction || isStaging) {
                throw new IllegalStateException("Startup Failed: Default development JWT secret cannot be used in production/staging.");
            } else {
                log.info("Using default development JWT secret.");
            }
        } else if (jwtSecret.length() < 32) {
            if (isProduction || isStaging) {
                throw new IllegalStateException("Startup Failed: JWT secret must be at least 256 bits (32 characters) long.");
            } else {
                log.warn("JWT secret is less than 32 characters. This is insecure for production.");
            }
        }

        // 2. Validate AI Gateway URL
        if (aiGatewayBaseUrl != null && !aiGatewayBaseUrl.isBlank()) {
            try {
                new URI(aiGatewayBaseUrl).toURL();
            } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
                throw new IllegalStateException("Startup Failed: Invalid AI Gateway base URL configuration: " + aiGatewayBaseUrl, e);
            }
        } else {
            log.warn("AI Gateway base URL (ai.gateway.base-url) is not configured.");
        }

        // 3. Validate MongoDB configuration
        if (mongoUri == null || mongoUri.isBlank()) {
            if (isProduction || isStaging) {
                throw new IllegalStateException("Startup Failed: MongoDB Connection URI (spring.data.mongodb.uri) must not be empty in production/staging.");
            } else {
                log.warn("MongoDB Connection URI is empty. Using Spring Boot defaults.");
            }
        }

        // 4. Validate Email configuration
        if (isProduction || isStaging) {
            if (mailHost == null || mailHost.isBlank()) {
                throw new IllegalStateException("Startup Failed: Email Host (spring.mail.host) is missing or incomplete for production/staging.");
            }
            if (mailPort <= 0) {
                throw new IllegalStateException("Startup Failed: Email Port (spring.mail.port) is invalid for production/staging.");
            }
        }

        // 5. Validate CORS allowed origins
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            if (isProduction || isStaging) {
                throw new IllegalStateException("Startup Failed: CORS allowed origins (app.cors.allowed-origins) must be configured in production/staging.");
            }
        } else {
            for (String origin : allowedOrigins) {
                if ("*".equals(origin)) {
                    if (isProduction || isStaging) {
                        throw new IllegalStateException("Startup Failed: CORS wildcard origin (*) is not allowed in production/staging when credentials are enabled.");
                    } else {
                        log.warn("CORS wildcard origin (*) is configured. This is insecure.");
                    }
                }
            }
        }
    }
}
