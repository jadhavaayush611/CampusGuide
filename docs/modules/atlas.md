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
│   ├── metrics      # ContextMetrics (diagnostic stats, execution timing, query & retrieval diagnostics)
│   ├── model        # Typed domain models (UserContext, PlannerContext, CalendarContext, AcademicContext, CampusContext)
│   ├── query        # Semantic query understanding (QueryAnalyzer, IntentDetector, EntityExtractor, TemporalExpressionResolver, QueryNormalizer, QueryContext, QueryIntent, QueryDomain)
│   ├── ranking      # Context ranking engine (ContextRankingService, RelevanceScorer, ContextScore)
│   ├── retrieval    # Intelligent retrieval strategies & policy (ContextRetriever, RetrievalContext, RetrievalPolicy, RetrievalStrategy, UserRetrievalStrategy, AcademicRetrievalStrategy, PlannerRetrievalStrategy, CalendarRetrievalStrategy, CampusRetrievalStrategy)
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

## Semantic Query Understanding & Intelligent Context Retrieval Pipeline

### 1. Semantic Query Analysis
- `QueryAnalyzer`: Entry point for query analysis. Orchestrates `QueryNormalizer`, `TemporalExpressionResolver`, `EntityExtractor`, and `IntentDetector` into a structured `QueryContext`.
- `QueryNormalizer`: Standardizes abbreviations (e.g. `prof` -> `professor`, `assign` -> `assignment`, `lib` -> `library`, `sched` -> `schedule`), aliases, and conversational phrasing.
- `TemporalExpressionResolver`: Normalizes conversational temporal expressions (`tomorrow`, `next week`, `after lunch`, `this morning`, `tonight`, `this weekend`, `next month`) into structured temporal range representations with start/end bounds.
- `EntityExtractor`: Extracts campus-specific locations (`library`, `science hall`, `cafeteria`), academic concepts (`gpa`, `courses`, `homework`, `exams`), planner items (`tasks`, `deadlines`), dates/times, and aliases.
- `IntentDetector`: Provider-independent, deterministic, rule-based intent detector categorizing queries into `ACADEMIC_INQUIRY`, `PLANNER_LOOKUP`, `CALENDAR_EVENT`, `CAMPUS_NAVIGATION`, `USER_PROFILE`, or `GENERAL_CONVERSATION` with confidence scores.
- `QueryContext`: Contains intent, domain classification, entities, normalized query, temporal information, retrieval hints, and confidence score.

### 2. Selective Strategy Retrieval Architecture
- `ContextRetriever`: Executes selective retrieval strategies based on `QueryContext` analysis rather than executing all contributors unconditionally.
- `RetrievalPolicy`: Configurable policy defining confidence thresholds (`minConfidenceThreshold`), forced retrieval rules (`alwaysRetrieveUserProfile`), fallback mechanisms (`enableFallbackToAllIfLowConfidence`), and strategy limits.
- `RetrievalStrategy` Interface & Implementations:
  - `UserRetrievalStrategy`: Retrieves user profile context.
  - `AcademicRetrievalStrategy`: Selectively retrieves academic standing, courses, and department info.
  - `PlannerRetrievalStrategy`: Selectively retrieves active and overdue planner tasks.
  - `CalendarRetrievalStrategy`: Selectively retrieves schedule events and time slots.
  - `CampusRetrievalStrategy`: Selectively retrieves campus location, notices, and facilities data.

### 3. Context Ranking Engine
- `ContextRankingService`: Ranks retrieved context models before assembling into `AtlasContext`.
- `RelevanceScorer`: Evaluates 6 weighted dimensions for each retrieved domain context:
  1. `intentRelevance` (weight 0.25): Intent match alignment.
  2. `entityOverlap` (weight 0.20): Overlap with extracted query entities.
  3. `freshness` (weight 0.15): Recency of context snapshot.
  4. `sourcePriority` (weight 0.15): Domain source hierarchy (User > Academic > Planner > Calendar > Campus).
  5. `confidence` (weight 0.15): Extraction & retrieval confidence score.
  6. `completeness` (weight 0.10): Non-null context field ratio.
- `ContextScore`: Encapsulates total composite score and guarantees deterministic ordering (`totalScore` desc, `sourcePriority` desc, `contributorName` asc).

### 4. Retrieval Diagnostics & Privacy
- Captured in `ContextMetrics`:
  - `detectedIntent`: Intent category.
  - `normalizedQuery`: Standardized query string.
  - `extractedEntities`: List of extracted entity names/types.
  - `executedStrategies`: List of executed strategy names.
  - `skippedStrategies`: List of skipped strategy names.
  - `retrievalLatencyMs`: Latency of context retrieval in milliseconds.
  - `relevanceScores`: Map of contributor names to composite ranking scores.
  - `retrievalConfidence`: Query understanding confidence score.
- Privacy Guarantees: PII is kept strictly within session context and excluded from unencrypted logs.

### 5. Extension Points
- **New Retrieval Strategy**: Implement `RetrievalStrategy` and register as a Spring `@Component`.
- **Custom Entity Types**: Add terms to `EntityExtractor` dictionary or implement custom entity rules.
- **Custom Intent Rules**: Extend `IntentDetector` pattern matching or keyword density rules.

---

## Context Intelligence Layer Architecture (Phase 3.2 — Batch 3.2.2)

### 1. Evidence Model
Every retrieved context carries structured evidence describing why it was selected:
- `RetrievalEvidence`: Captures `id`, `type` (`EvidenceType`), `source` (`EvidenceSource`), `entityKey`, `contentSnippet`, `rationale`, `timestamp`, `score` (`EvidenceScore`), and `metadata`.
- `EvidenceBundle`: Groups evidence for a target domain with aggregate scoring, confidence rating, source summary, and creation timestamp.
- `EvidenceType`: Enum supporting `SQL`, `VECTOR`, `RAG`, `MEMORY`, `EXTERNAL_API`, `DIRECT`, `HEURISTIC`, `KEYWORD`, `RULE_BASED`, `DOMAIN_SERVICE`, `CAMPUS_KNOWLEDGE`.
- `EvidenceSource`: Enum supporting `DATABASE`, `VECTOR_DB`, `CAMPUS_SERVICE`, `ACADEMIC_SERVICE`, `KNOWLEDGE_BASE`, `MEMORY_STORE`, `CACHE`, `DOMAIN_CONTRIBUTOR`, `EXTERNAL_SERVICE`, `HEURISTIC` with priority weighting.
- `EvidenceScore`: Multi-dimensional quality composite evaluating `relevanceScore` (30%), `confidenceScore` (25%), `sourceAuthorityScore` (20%), `freshnessScore` (15%), and `qualityScore` (10%).

### 2. Context Intelligence Engine
`ContextIntelligenceEngine` orchestrates context intelligence before `AtlasContext` is finalized:
1. **Evidence Analysis**: Scans retrieved domain context and synthesizes structured `RetrievalEvidence` bundles.
2. **Context Fusion**: Executes `ContextFusionEngine` to remove duplicates and merge overlapping context.
3. **Conflict Resolution**: Executes `ConflictResolver` to resolve conflicting context entries based on freshness, confidence, source priority, and evidence quality.
4. **Prioritization**: Executes `ContextPrioritizer` to rank context entries deterministically.
5. **Optimization & Budgeting**: Executes `ContextCache` and `LatencyBudgetManager` for TTL caching, parallel retrieval budget tracking, graceful degradation, and token size optimization.
6. **Observability**: Populates `IntelligenceMetrics` and `ContextMetrics` with full decision logs.

### 3. Context Fusion & Conflict Resolution
- `ContextMerger`: Merges evidence bundles, deduplicates identical snippets, combines placeholders, and maintains deterministic ordering.
- `ConflictResolver`: Resolves conflicting context items using composite conflict score:
  - Source Priority (40%)
  - Freshness Decay (25%)
  - Extraction Confidence (20%)
  - Evidence Quality (15%)
- `FusionDecision` & `ConflictResolution`: Audit records tracking winning and losing sources, values, scores, and resolution rationale for full observability.

### 4. Context Prioritization
- `ContextPrioritizer`: Evaluates context items using:
  $$\text{RankScore} = (R \times 0.25) + (C \times 0.20) + (ES \times 0.20) + (SA \times 0.15) + (F \times 0.10) + (Comp \times 0.10)$$
  where $R$ = Relevance, $C$ = Confidence, $ES$ = Evidence Strength, $SA$ = Source Authority, $F$ = Freshness, $Comp$ = Completeness.
- Ranks candidate context entries deterministically and prunes low-priority items when size/token budgets are constrained.

### 5. Campus Knowledge Services
- `CampusKnowledgeService`: Provider-independent service offering reusable access to campus data:
  - Buildings (`BuildingInfo`)
  - Departments (`DepartmentInfo`)
  - Faculty (`FacultyInfo`)
  - Office Hours (`OfficeHoursInfo`)
  - Laboratories (`LaboratoryInfo`)
  - Classrooms (`ClassroomInfo`)
  - Student Services (`StudentServiceInfo`)
  - Announcements (`CampusAnnouncementInfo`)
  - Events (`CampusEventInfo`)
  - Navigation Metadata (`NavigationMetadataInfo`)
  - Emergency Contacts (`EmergencyContactInfo`)
- `CampusKnowledgeProvider`: Decoupled provider interface (default `InMemoryCampusKnowledgeProvider` seeded with realistic campus data).

### 6. Retrieval Optimization & Caching
- `ContextCache`: Thread-safe cache storing evidence bundles with configurable TTL, domain-specific TTL policies, invalidation (`invalidateKey`, `invalidateDomain`, `clear`), and diagnostic hit/miss counters.
- `RetrievalCachePolicy`: Policy controlling cache enablement, TTLs, and max entry bounds.
- `LatencyBudgetManager`: Tracks real-time latency budgets, measures execution durations per strategy, and triggers graceful degradation if latency limits are approached.

### 7. Future RAG Readiness
Plug-and-play RAG extension points in `com.campusguide.personal.ai.atlas.context.retrieval.rag`:
- `VectorRetrievalProvider`
- `SemanticRetrievalProvider`
- `HybridRetrievalProvider`
- `MemoryRetrievalProvider`
- `DocumentRetrievalProvider`
- `ExternalKnowledgeProvider`
- `RagRetrievalStrategy`: Implements `RetrievalStrategy` to bridge RAG providers into `AtlasContext` without altering prompt assembly or conversation orchestration.

---

## Error Handling Matrix

| Exception Class | Cause | Category | HTTP Status | Response Payload |
|---|---|---|---|---|
| `AtlasPromptValidationException` | Blank prompt, prompt length > 4096, invalid temperature | `VALIDATION` | `400 Bad Request` | `{"error": "<message>"}` |
| `AtlasAuthenticationException` | Invalid provider API key, authentication failure | `AUTHENTICATION` | `401 Unauthorized` | `{"error": "<message>"}` |
| `AtlasRateLimitException` | User or session request rate limit exceeded | `RATE_LIMIT` | `429 Too Many Requests` | `{"error": "<message>"}` |
| `AtlasProviderUnavailableException` | Provider disabled, 503 from provider, circuit breaker OPEN | `PROVIDER_TRANSIENT` / `CIRCUIT_BREAK_OPEN` | `533 / 503 Service Unavailable` | `{"error": "<message>"}` |
| `AtlasTimeoutException` | Request execution time exceeds timeout threshold | `TIMEOUT` | `504 Gateway Timeout` | `{"error": "<message>"}` |
| `AtlasProviderException` | General API error from model provider | `PROVIDER_PERMANENT` | `502 Bad Gateway` | `{"error": "<message>"}` |
| `AtlasConfigurationException` | Invalid startup configuration | `SYSTEM_ERROR` | `500 Internal Server Error` | `{"error": "<message>"}` |
