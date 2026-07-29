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

## Knowledge Ingestion & Vector Infrastructure (Phase 3.3 — Batch 3.3.1)

### 1. Universal Retrieval Abstraction (`KnowledgeArtifact`)
`KnowledgeArtifact` is the universal, provider-independent retrieval abstraction across all Atlas knowledge and RAG ingestion pipelines. All loaders, parsers, chunkers, embedding generators, and vector indices operate exclusively on `KnowledgeArtifact` instances.

Key components of `KnowledgeArtifact`:
- `id` (`ArtifactIdentifier`): Unique string identifier formatted as `art_<uuid>` or deterministic chunk ID `parentId_chk_<index>`.
- `type` (`ArtifactType`): Artifact classification (`DOCUMENT`, `CHUNK`, `PDF`, `DOCX`, `MARKDOWN`, `TEXT`, `FAQ`, `WEB_PAGE`, `API_SCHEMA`, `CUSTOM`).
- `content`: String representation of normalized text content.
- `metadata` (`ArtifactMetadata`): Preserves document name, category, domain, language, size in bytes, and key-value attributes.
- `source` (`ArtifactSource`): Preserves provenance including `sourceUri`, `sourceType`, `title`, `author`, `startOffset`, and `endOffset`.
- `version` (`ArtifactVersion`): Tracks `versionNumber`, SHA-256 `checksum`, and creation timestamp.
- `references` (`List<ArtifactReference>`): Preserves hierarchical and sequential lineage (`PARENT`, `CHILD`, `PREVIOUS_CHUNK`, `NEXT_CHUNK`, `RELATED`, `REPLACEMENT`).
- `embedding` (`ArtifactEmbedding`): Provider-independent vector representation (`float[] vector`, `provider`, `model`, `dimension`).
- `lifecycleState` (`ArtifactLifecycleState`): Tracks lifecycle state transitions (`DISCOVERED`, `INGESTING`, `PARSED`, `CHUNKED`, `EMBEDDED`, `INDEXED`, `FAILED`, `ARCHIVED`).

### 2. Knowledge Ingestion Pipeline
`KnowledgeIngestionService` orchestrates end-to-end document ingestion:
1. **Document Loading**: `DocumentLoader` loads raw content into `RawDocument` wrappers from text, byte arrays, files, or paths.
2. **Parsing**: `DocumentParser` strategy implementations (`PdfDocumentParser`, `DocxDocumentParser`, `MarkdownDocumentParser`, `TextDocumentParser`) extract normalized content, metadata, page boundaries, and section hierarchies.
3. **Artifact Construction**: `ArtifactBuilder` constructs root `KnowledgeArtifact` instances.
4. **Chunking**: `ChunkingEngine` applies requested chunking strategies (`FIXED_SIZE`, `SLIDING_WINDOW`, `SEMANTIC`) and produces deterministic chunk artifacts linked via lineage references.
5. **Embedding Generation**: `EmbeddingService` generates vector embeddings in batch via `EmbeddingProvider` implementations (`OpenAIEmbeddingProvider`, `MockEmbeddingProvider`, `LocalEmbeddingProvider`) with caching and retries.
6. **Vector Storage**: `VectorRepository` indexes chunk artifacts into `VectorStore` instances (`InMemoryVectorStore` and provider stubs).
7. **Knowledge Cataloging**: `KnowledgeCatalog` registers entries, tracks checksums, updates lifecycle states, and exposes catalog metrics.

### 3. Chunking Strategies
- `FixedSizeChunker`: Fixed character-size chunking with configurable window size and overlap.
- `SlidingWindowChunker`: Word token sliding window chunking.
- `SemanticChunker`: Section and heading aware semantic chunking preserving heading context and paragraph boundaries.
- **Deterministic Chunk IDs**: Generated using `ArtifactIdentifier.generateChunkId(parentId, index)` -> `<parentId>_chk_<index>`.

### 4. Embedding Infrastructure
- `EmbeddingProvider` interface: Provider-independent embedding interface (`getProviderName()`, `getDimension()`, `embed()`).
- `OpenAIEmbeddingProvider`: Provider implementation supporting `text-embedding-3-small` (1536 dim) and `text-embedding-3-large` (3072 dim), falling back to deterministic mock when API keys are unconfigured.
- `MockEmbeddingProvider`: Deterministic SHA-256 unit-vector generator for offline execution and zero-dependency unit/integration testing.
- `EmbeddingService`: Manages batch partitioning (default batch size 32), SHA-256 content caching, exponential backoff retries (3 attempts), and Micrometer latency metrics.

### 5. Vector Store Architecture
- `VectorStore` Interface: Core operations (`index`, `indexAll`, `search`, `get`, `delete`, `clear`, `count`).
- `InMemoryVectorStore`: Concurrent, thread-safe in-memory vector store with cosine similarity ranking and metadata filtering.
- Extension Provider Interfaces: Pluggable stubs for `PgVectorStore`, `PineconeVectorStore`, `QdrantVectorStore`, `WeaviateVectorStore`, and `MilvusVectorStore`.

### 6. Knowledge Catalog & Observability
- `KnowledgeCatalog`: Tracks document lifecycle, versioning, indexing status, total document/chunk counts, and checksum deduplication (skips ingestion if checksum already indexed).
- **Privacy & Observability**:
  - `AtlasMetrics` records latency (`atlas.latency.orchestration`, `atlas.latency.provider`), token usage, and request counts.
  - Zero raw document content logging: Loggers emit only document URIs, checksums, document IDs, chunk counts, durations, and error messages.

### 7. Extension Points
### 8. Extension Points (Batch 3.3.1)
- **Custom Document Parsers**: Implement `DocumentParser` and register as Spring `@Component`.
- **Custom Chunking Strategies**: Implement `ChunkingStrategy` and register with `ChunkingEngine`.
- **Custom Embedding Providers**: Implement `EmbeddingProvider` and register with `EmbeddingService`.
- **Custom Vector Stores**: Implement `VectorStore` and register with `VectorRepository`.

---

## Semantic Retrieval & Hybrid Knowledge Engine (Phase 3.3 — Batch 3.3.2)

### 1. Knowledge Collections Boundary Architecture
KnowledgeCollections serve as the provider-independent retrieval boundary across Atlas, replacing global corpus searching with scoped, permission-aware boundaries. Every `KnowledgeArtifact` belongs to exactly one `KnowledgeCollection`.

Key components:
- `KnowledgeCollection`: Core boundary domain model encapsulating `collectionId`, `name`, `type`, `scope`, `lifecycleState`, `version`, `metadata`, `statistics`, and `updateHistory`.
- `KnowledgeCollectionMetadata`: Defines ownership (`ownerId`, `ownerType`), access authorization (`allowedRoles`, `allowedUsers`, `isPublic`), domain classification, and collection weights.
- `KnowledgeCollectionType`: Structural classification enum (`PUBLIC_KNOWLEDGE`, `ACADEMIC`, `DEPARTMENTAL`, `FACULTY_ONLY`, `USER_MEMORY`, `PRIVATE_USER`, `SYSTEM`).
- `KnowledgeCollectionScope`: Access scope enum (`GLOBAL`, `SYSTEM`, `PUBLIC`, `DEPARTMENT`, `FACULTY`, `USER`, `PRIVATE`, `ROLE_BASED`).
- `KnowledgeCollectionRegistry`: Thread-safe Spring `@Component` holding active collections, managing lifecycle transitions, and pre-seeding default collections (`public_campus_knowledge`, `academic_catalog`, `department_docs`, `user_memories`, `default_collection`).

### 2. Collection Lifecycle Management
Tracks collection evolution and status transitions across six states:
1. `DISCOVERED`: Collection identified or declared.
2. `INDEXING`: Vector embedding generation or storage indexing in progress.
3. `ACTIVE`: Ready for semantic and hybrid retrieval queries.
4. `UPDATING`: Incremental re-indexing or version upgrade in progress.
5. `ARCHIVED`: Archived from active search boundary.
6. `FAILED`: Ingestion or indexing failure.

Collection statistics (`totalArtifactCount`, `totalChunkCount`, `totalVectorCount`, `byteSize`, `lastIndexedAt`, `lastUpdatedAt`) and update records (`CollectionUpdateRecord`) provide full auditability of version history and updates.

### 3. Collection-Aware Retrieval
- `CollectionSelector`: Evaluates `QueryContext` (intent, domain, extracted entities), user identity, and roles against registered collections to select active retrieval candidates with dynamic relevance weighting.
- `CollectionRetrievalPolicy`: Configurable policy setting default scopes, max candidate bounds, confidence thresholds, and fallback collection strategies (`fallbackCollectionIds`).
- `CollectionFilter`: Flexible predicate filtering collections by allowed types, scopes, role access, and priority thresholds.

### 4. Semantic Retrieval Infrastructure
- `SemanticRetriever`: Executes vector similarity search using `EmbeddingService` query vector generation and `VectorRetriever`, enforcing configurable cosine similarity thresholds and metadata filters.
- `VectorRetriever`: Executes dense vector searches against `VectorStore` implementations.
- `ArtifactRetriever`: Resolves full `KnowledgeArtifact` payloads by identifier or batch lookup.

### 5. Hybrid Retrieval & Multi-Search Fusion
- `HybridRetriever`: Combines vector similarity search, structured metadata matching (category, domain, source type), keyword matching (BM25 token overlap + title matching bonus), and collection-scoped retrieval.
- `HybridRankingEngine`: Fuses vector similarity ($50\%$), keyword overlap ($35\%$), and structured metadata match ($15\%$) into a unified hybrid score.

### 6. Multi-Dimensional Artifact Ranking Engine
- `ArtifactRankingService`: Evaluates candidate artifacts across 7 weighted dimensions:
  1. `semanticSimilarity` ($35\%$): Dense vector cosine similarity score.
  2. `keywordOverlap` ($20\%$): Normalized query term overlap + title match bonus.
  3. `freshnessScore` ($10\%$): Recency decay based on artifact update timestamp.
  4. `evidenceQuality` ($10\%$): Content completeness, heading metadata, and lineage references.
  5. `sourceAuthority` ($10\%$): Source type priority (e.g. PDF/Official Catalog > Docx/Text > Web/FAQ).
  6. `collectionPriority` ($10\%$): Priority weight assigned to the target `KnowledgeCollection`.
  7. `retrievalConfidence` ($5\%$): Query understanding confidence score.
- `ArtifactScore`: Encapsulates total score and enforces deterministic ordering: `totalScore` descending $\rightarrow$ `collectionPriority` descending $\rightarrow$ `artifactId` ascending.

### 7. Citation Engine
- `CitationGenerator`: Transforms top ranked artifacts into structured `Citation` instances carrying citation marks (e.g., `[1]`, `[2]`), source references (`SourceReference`), document references (`DocumentReference`), section/chunk references (`SectionReference`), and content snippets.
- Structured Citation Models:
  - `Citation`: Unique `citationId`, `citationMark`, snippet, and confidence score.
  - `SourceReference`: URI, sourceType, title, author.
  - `DocumentReference`: documentId, title, collectionId, category.
  - `SectionReference`: sectionTitle, start/end byte offsets, chunkIndex.

### 8. Context Intelligence Integration Pipeline
- `KnowledgeRetrievalEngine`: Orchestrates the complete end-to-end RAG pipeline:
  $$\text{QueryContext} \rightarrow \text{CollectionSelector} \rightarrow \text{HybridRetriever} \rightarrow \text{ArtifactRankingService} \rightarrow \text{CitationGenerator} \rightarrow \text{KnowledgeRetrievalResult}$$
- Evidence Conversion: Transforms ranked `KnowledgeArtifact`s and `Citation`s into `RetrievalEvidence` objects (`EvidenceType.RAG`, `EvidenceSource.KNOWLEDGE_BASE`) with citation metadata, bridging seamlessly into the Context Intelligence Layer (`AtlasContext`, `ContextIntelligenceEngine`, `ContextFusionEngine`, `ConflictResolver`, `ContextPrioritizer`).
- `AtlasKnowledgeRetrievalAdapter`: Adapts `KnowledgeRetrievalEngine` to legacy RAG strategy interfaces (`VectorRetrievalProvider`, `SemanticRetrievalProvider`, `HybridRetrievalProvider`).

### 9. Future Memory & Permissions Architecture
Knowledge Collections naturally support future memory and security expansion without modifying retrieval architecture:
- `USER_MEMORY`: User-scoped memories (`scope = USER`, `ownerId = userId`).
- `DEPARTMENTAL`: Department-scoped knowledge (`scope = DEPARTMENT`, `allowedRoles = {"CS_FACULTY", "STUDENT"}`).
- `FACULTY_ONLY`: Restrictive faculty guides (`scope = FACULTY`, `allowedRoles = {"FACULTY", "ADMIN"}`).
- `PRIVATE_USER`: Private personal notes (`scope = PRIVATE`, `ownerId = userId`).
- `PUBLIC_KNOWLEDGE`: Open campus guides and public FAQs (`scope = PUBLIC`).

### 10. Extension Points (Batch 3.3.2)
- **Custom Collection Filters**: Extend `CollectionFilter` or supply custom predicates to `CollectionSelector`.
- **Custom Hybrid Scoring Rules**: Extend `HybridRankingEngine` to include domain-specific keyword algorithms.
- **Custom Citation Formatters**: Implement custom citation marking strategies in `CitationGenerator`.

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

