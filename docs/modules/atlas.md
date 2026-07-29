# Atlas AI Foundation & Conversation Orchestration Architecture

## Overview

Atlas is the provider-agnostic AI platform for CampusGuide built following Domain-Driven Design (DDD) and Clean Architecture principles. It encapsulates AI model interactions behind abstract interfaces and segregates orchestration logic into dedicated pipelines (`ConversationOrchestrator`, `ContextEngine`, `PromptBuilder`, `ResilientAIProvider`). This design ensures the application domain remains decoupled from vendor SDKs while supporting contextual intelligence, pluggable domain context contributors backed by dedicated domain context services, strongly-typed context aggregates (`AtlasContext`), modular prompt pipelines built around `ContextSection`s, personas, ordered instruction layers, token budgeting, prompt versioning, rate limiting, provider resilience (exponential retries, circuit breaker, request timeouts), Micrometer operational metrics, health indicators, startup readiness validation, and persistent multi-turn conversation lifecycles.

---

## Architectural Principles

1. **Domain-Driven & Clean Architecture**:
   - Core domain models (`AtlasPrompt`, `AtlasNormalizedResponse`, `AtlasChatMessage`, `AtlasContext`, `ContextSection`, `PromptVersion`) remain pure and independent of vendor SDKs or external DTOs.
   - Core orchestration logic depends strictly on abstract interfaces (`AIProvider`, `ContextContributor`, `InstructionLayer`, `RateLimitPolicy`).

2. **Dedicated Conversation Orchestration & Rate Limiting**:
   - `ConversationOrchestrator` manages the end-to-end conversation pipeline, isolating orchestration responsibilities from high-level service interfaces (`AtlasService`).
   - Enforces per-user rate limiting via `RateLimitPolicy` before context aggregation.
   - Handles conversation creation, user/assistant message persistence, chronological history loading, context engine execution, prompt pipeline invocation, latency measurement, and MDC structured logging.

3. **Provider Resilience & Circuit Breaker**:
   - `ResilientAIProvider` decorates underlying providers (e.g. `OpenAIProvider`) to provide:
     - Configurable request timeouts.
     - Exponential backoff retry for transient errors (e.g. 503, 429, timeouts). Non-transient errors (400 validation, 401/403 authentication, permanent provider errors) fast-fail immediately without retry.
     - Circuit breaker pattern via `CircuitBreaker` tracking state (`CLOSED`, `OPEN`, `HALF_OPEN`). When `OPEN`, fast-fails with `AtlasProviderUnavailableException`.
     - Automatic recovery in `HALF_OPEN` state.

4. **Micrometer Operational Metrics**:
   - `AtlasMetrics` integrates with Micrometer `MeterRegistry` capturing:
     - Request metrics (`atlas.requests` with tags `status`, `provider`, `model`, `error_category`).
     - Retries counter (`atlas.requests.retries`).
     - Timeouts counter (`atlas.requests.timeout`).
     - Circuit breaker event counter (`atlas.circuitbreaker.events` with state tag).
     - Latency timers (`atlas.latency.orchestration`, `atlas.latency.context_assembly`, `atlas.latency.prompt_assembly`, `atlas.latency.provider`).
     - Usage token counters (`atlas.tokens.prompt`, `atlas.tokens.completion`, `atlas.tokens.total`).

5. **Non-Blocking Subsystem Health Check**:
   - `AtlasHealthIndicator` implements Spring Boot `HealthIndicator` checking provider configuration, prompt pipeline, context pipeline, and circuit breaker state.
   - Avoids live network API calls during health check invocations.

6. **Fail-Fast Startup Validation**:
   - `AtlasStartupValidator` and `AtlasProperties.validate()` validate configuration, API keys, models, token budget caps, prompt templates, personas, and registered context contributors at startup.

---

## Module Package Structure

```
com.campusguide.personal.ai.atlas
├── config           # AtlasProperties, AtlasConfig, AtlasStartupValidator
├── context          # AtlasContext, ContextEngine, ContextContributor
│   ├── contributor  # UserProfileContributor, PlannerContributor, CalendarContributor, AcademicContributor, CampusContributor
│   ├── metrics      # ContextMetrics (diagnostic stats, execution timing, context size)
│   ├── model        # Typed domain models (UserContext, PlannerContext, CalendarContext, AcademicContext, CampusContext)
│   └── service      # UserContextService, PlannerContextService, CalendarContextService, AcademicContextService, CampusContextService
├── controller       # AtlasController (POST /api/v1/atlas/chat)
├── dto              # AtlasChatRequest, AtlasChatResponse, AtlasChatMessageDto, AtlasUsageDto
├── exception        # AtlasErrorCategory, AtlasException, AtlasPromptValidationException, AtlasRateLimitException, AtlasAuthenticationException, AtlasProviderUnavailableException, AtlasTimeoutException, AtlasProviderException, AtlasConfigurationException
├── health           # AtlasHealthIndicator
├── mapper           # AtlasMapper (converts DTOs to/from Domain Models)
├── metrics          # AtlasMetrics (Micrometer counters, timers, distribution summaries)
├── model            # AtlasPrompt, AtlasNormalizedResponse, AtlasRole, AtlasUsageInfo, ProviderMetadata
├── orchestration    # ConversationOrchestrator (orchestrates conversation lifecycle & context)
├── prompt           # Prompt pipeline framework
│   ├── budget       # TokenBudgetManager, TokenBudgetResult
│   ├── instruction  # InstructionLayer, CoreIdentityInstruction, SafetyInstruction, CampusInstruction, FormattingInstruction, ResponsePolicyInstruction
│   ├── model        # ContextSection, PromptVersion
│   ├── persona      # CampusGuideAssistantPersona
│   ├── ContextSectionAssembler.java # Transforms AtlasContext -> List<ContextSection>
│   ├── PromptTemplate.java         # System prompt template & versioning renderer
│   └── PromptBuilder.java          # Assembles final system prompt & AtlasPrompt
├── provider         # AIProvider interface and OpenAIProvider implementation
├── ratelimit        # RateLimitPolicy, InMemoryRateLimitPolicy, RateLimitStatus
├── resilience       # ResilientAIProvider, CircuitBreaker
├── service          # AtlasService interface & AtlasServiceImpl (delegates to orchestrator)
├── validation       # AtlasPromptValidator
└── util             # AtlasUtils (string sanitization, token estimation)
```

---

## Error Handling Matrix

| Exception Class | Cause | Category | HTTP Status | Response Payload |
|---|---|---|---|---|
| `AtlasPromptValidationException` | Blank prompt, prompt length > 4096, invalid temperature | `VALIDATION` | `400 Bad Request` | `{"error": "<message>"}` |
| `AtlasAuthenticationException` | Invalid provider API key, authentication failure | `AUTHENTICATION` | `401 Unauthorized` | `{"error": "<message>"}` |
| `AtlasRateLimitException` | User or session request rate limit exceeded | `RATE_LIMIT` | `429 Too Many Requests` | `{"error": "<message>"}` |
| `AtlasProviderUnavailableException` | Provider disabled, 503 from provider, circuit breaker OPEN | `PROVIDER_TRANSIENT` / `CIRCUIT_BREAKER_OPEN` | `533 / 503 Service Unavailable` | `{"error": "<message>"}` |
| `AtlasTimeoutException` | Request execution time exceeds timeout threshold | `TIMEOUT` | `504 Gateway Timeout` | `{"error": "<message>"}` |
| `AtlasProviderException` | General API error from model provider | `PROVIDER_PERMANENT` | `502 Bad Gateway` | `{"error": "<message>"}` |
| `AtlasConfigurationException` | Invalid startup configuration | `SYSTEM_ERROR` | `500 Internal Server Error` | `{"error": "<message>"}` |
