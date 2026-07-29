# Atlas AI Production Deployment Guide

## Overview

This guide provides operational and deployment documentation for running Atlas AI in production environments within CampusGuide.

---

## Deployment Checklist

- [ ] **API Keys & Credentials**: Ensure vendor API keys (e.g. `ATLAS_OPENAI_API_KEY`) are injected securely via environment variables or secret store.
- [ ] **Configuration Validation**: Verify `atlas.enabled=true`, timeouts, retry backoffs, and token budget caps in `application-prod.properties` / `application.yml`.
- [ ] **Health Endpoint Registration**: Verify `/actuator/health/atlas` or standard `/actuator/health` exposes Atlas readiness without external network pings.
- [ ] **Rate Limiting Policy**: Configure `atlas.rate-limit.requests-per-minute` and `atlas.rate-limit.capacity` per deployment scale.
- [ ] **Micrometer Metrics Export**: Ensure Prometheus/Datadog metrics export is active for `atlas.requests`, `atlas.latency`, `atlas.tokens`, `atlas.circuitbreaker.events`.
- [ ] **Structured Logging & MDC Correlation**: Verify log aggregators (e.g. ELK, Grafana Loki) parse `requestId`, `conversationId`, `model`, `provider`, `promptVersion`, `latencyMs`, and `circuitBreakerState`. Verify prompt/response body logging is disabled.

---

## Configuration Guide

Configuration parameters are mapped to `AtlasProperties` under prefix `atlas`:

```yaml
atlas:
  enabled: true
  default-provider: openai
  default-model: gpt-4o-mini
  max-prompt-length: 4096
  prompt-token-budget-cap: 4096
  timeout-ms: 30000

  providers:
    openai:
      api-key: ${ATLAS_OPENAI_API_KEY:}
      base-url: https://api.openai.com/v1
      model: gpt-4o-mini
      temperature: 0.7
      max-tokens: 1024

  retry:
    enabled: true
    max-attempts: 3
    initial-interval-ms: 500
    multiplier: 2.0
    max-interval-ms: 5000

  circuit-breaker:
    enabled: true
    failure-threshold: 5
    wait-duration-in-open-state-ms: 30000
    permitted-number-of-calls-in-half-open-state: 3

  rate-limit:
    enabled: true
    requests-per-minute: 60
    capacity: 60
```

---

## Resilience Behavior

Atlas implements multi-tiered provider resilience via `ResilientAIProvider`:

1. **Configurable Timeouts**: Request execution times out after `atlas.timeout-ms` (default 30 seconds), throwing `AtlasTimeoutException` (category `TIMEOUT`, HTTP 504).
2. **Exponential Backoff Retry**: Automatically retries transient failures (HTTP 503, HTTP 429, timeouts). Wait interval scales exponentially (`initial-interval-ms * (multiplier ^ attempt)`).
3. **Non-Retriable Fast Failure**: Retries **never** occur for validation errors (400), authentication failures (401/403), or permanent provider errors.
4. **Circuit Breaker**: When consecutive failures reach `failure-threshold` (default 5), circuit transitions to `OPEN` state. Sub-second requests fast-fail with `AtlasProviderUnavailableException` (category `CIRCUIT_BREAKER_OPEN`, HTTP 503). After `wait-duration-in-open-state-ms`, circuit enters `HALF_OPEN` state to probe provider recovery.
5. **Graceful Degradation**: Error categories isolate subsystem failures and preserve standard API contracts.

---

## Health Checks

Atlas exposes readiness via `AtlasHealthIndicator` registered under Spring Boot Health (`/actuator/health`).

Validations performed:
- Provider configuration & API key readiness.
- Prompt pipeline components (PromptBuilder, templates, persona baseline).
- Context pipeline components (ContextEngine, registered ContextContributors).
- Subsystem readiness & Circuit breaker state.

**CRITICAL**: `AtlasHealthIndicator` performs zero external network calls during health checks to guarantee fast, reliable readiness probes for Kubernetes/Cloud deployments.

---

## Operational Metrics (Micrometer)

Atlas exports standard Micrometer operational metrics:

| Metric Name | Type | Description / Tags |
|---|---|---|
| `atlas.requests` | Counter | Total requests tagged by `status` (success/failure), `provider`, `model`, `error_category` |
| `atlas.requests.retries` | Counter | Count of retry attempts tagged by `provider` |
| `atlas.requests.timeout` | Counter | Count of request timeouts tagged by `provider` |
| `atlas.circuitbreaker.events` | Counter | Count of circuit breaker state transitions tagged by `provider`, `state` (`OPEN`, `CLOSED`, `HALF_OPEN`) |
| `atlas.latency.orchestration` | Timer | Overall end-to-end orchestration latency |
| `atlas.latency.context_assembly` | Timer | Latency of context engine aggregation |
| `atlas.latency.prompt_assembly` | Timer | Latency of prompt construction and token budgeting |
| `atlas.latency.provider` | Timer | Network & model latency of downstream AI provider |
| `atlas.tokens.prompt` | Counter | Total prompt input tokens consumed |
| `atlas.tokens.completion` | Counter | Total output completion tokens generated |
| `atlas.tokens.total` | Counter | Total aggregate token usage |

---

## Troubleshooting

### 1. `AtlasConfigurationException` on Startup
- **Symptom**: Application fails startup with `AtlasConfigurationException`.
- **Cause**: Invalid timeout (<= 0), negative budget caps, missing API key, or zero registered context contributors.
- **Fix**: Check `application.yml` and environment variables. Ensure `atlas.enabled` and required fields are set correctly.

### 2. HTTP 429 `AtlasRateLimitException`
- **Symptom**: Client receives HTTP 429 Too Many Requests.
- **Cause**: Per-user or per-session rate limit exceeded `atlas.rate-limit.capacity`.
- **Fix**: Increase `atlas.rate-limit.requests-per-minute` or inspect user request patterns.

### 3. HTTP 503 `AtlasProviderUnavailableException` (Circuit Breaker OPEN)
- **Symptom**: Requests fast-fail with HTTP 503.
- **Cause**: Provider experienced consecutive failures exceeding `failure-threshold`.
- **Fix**: Check OpenAI upstream API status. Circuit will automatically attempt recovery in `HALF_OPEN` state after wait duration.
