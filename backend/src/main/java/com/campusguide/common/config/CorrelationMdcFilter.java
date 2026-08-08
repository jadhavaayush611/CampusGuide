package com.campusguide.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that establishes request correlation by generating or propagating a correlation ID,
 * populates the MDC context with logging fields (correlationId, requestPath, httpMethod),
 * measures request duration, and logs requests exceeding the configured slow threshold.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorrelationMdcFilter extends OncePerRequestFilter {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";
    private static final String ALTERNATIVE_CORRELATION_HEADER = "X-Request-ID";
    
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_REQUEST_PATH = "requestPath";
    private static final String MDC_HTTP_METHOD = "httpMethod";

    @Value("${monitoring.slow-request-threshold-ms:1000}")
    private long slowRequestThresholdMs;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // 1. Resolve Correlation ID
        String correlationId = request.getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = request.getHeader(ALTERNATIVE_CORRELATION_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }
        }

        // Add correlation ID to response header for client diagnostics
        response.setHeader(CORRELATION_HEADER, correlationId);

        // 2. Populate MDC variables
        String requestPath = request.getRequestURI();
        String httpMethod = request.getMethod();

        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_REQUEST_PATH, requestPath);
        MDC.put(MDC_HTTP_METHOD, httpMethod);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > slowRequestThresholdMs) {
                log.warn("Slow request detected: {} {} took {} ms (threshold: {} ms) [Correlation ID: {}]",
                        httpMethod, requestPath, duration, slowRequestThresholdMs, correlationId);
            }
            // Ensure MDC is cleared after every request
            MDC.clear();
        }
    }
}
