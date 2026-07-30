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

## Provider-Independent Knowledge Graph Infrastructure (Phase 3.4 — Batch 3.4.1)

### 1. Architectural Overview
The Atlas Knowledge Graph Infrastructure provides a reusable, provider-independent graph data layer that models domain relationships between `KnowledgeArtifact`s, `KnowledgeCollection`s, and structured campus entities (`Person`, `Course`, `Building`, `Department`, `Event`, `Service`, `Document`) without embedding reasoning logic.

The graph architecture is completely decoupled from underlying graph database providers, defining pure interfaces and in-memory structures compatible with Neo4j, JanusGraph, Apache TinkerPop, and Amazon Neptune.

### 2. Node & Edge Data Infrastructure

#### Nodes (`KnowledgeNode`)
- `NodeIdentifier`: Value object wrapper for unique node keys (e.g. `node_123`, `course:CS101`, `artifact:art_456`, `collection:col_789`).
- `NodeType`: Enum supporting `KnowledgeArtifact`, `KnowledgeCollection`, `Person`, `Course`, `Building`, `Department`, `Event`, `Service`, `Document`, and extensible `Custom` types.
- `NodeAttributes`: Type-safe attribute map supporting typed property access (`getString`, `getInteger`, `getDouble`, `getBoolean`, `getList`), merging, and immutability projections.
- `KnowledgeNode`: Core node entity maintaining identifier, node type, name/label, attributes, metadata, timestamps (`createdAt`, `updatedAt`), source collection ID, source artifact ID, and provenance tracking.

#### Edges (`KnowledgeEdge`)
- `RelationshipType`: Enum supporting standard relationship types:
  - `BELONGS_TO`, `PART_OF`, `LOCATED_IN`, `TEACHES`, `ENROLLED_IN`, `REFERENCES`, `DEPENDS_ON`, `RELATED_TO`, `PREREQUISITE`, `NEXT`, `PREVIOUS`, `USES`, `CONTAINS`, `CUSTOM`.
- `RelationshipStrength`: Value object modeling confidence/weight between 0.0 and 1.0 with standard constants (`WEAK` = 0.25, `MEDIUM` = 0.50, `STRONG` = 0.75, `DEFINITIVE` = 1.00) and max-weight combination rules.
- `EdgeMetadata`: Encapsulates extractor name, provenance string, source artifact ID, timestamps, and custom properties.
- `KnowledgeEdge`: Directed/undirected edge connecting source and target nodes with deterministic ID generation (`sourceId->relationshipType->targetId`).

### 3. Relationship Extraction Pipeline
- `RelationshipExtractor` Interface: Pluggable extraction strategies extracting structural and explicit relationships from artifacts, collections, and structured entities.
- `RelationshipBuilder`: Fluent builder for constructing `KnowledgeEdge` instances cleanly.
- `RelationshipRegistry`: Central component managing registered extractors in priority order and orchestrating multi-extractor execution.
- Extractor Implementations:
  - `ArtifactReferenceRelationshipExtractor`: Extracts explicit `REFERENCES`, `DEPENDS_ON`, `NEXT`, and `PREVIOUS` edges from artifact lineage and references.
  - `CollectionRelationshipExtractor`: Extracts `CONTAINS` and `BELONGS_TO` edges between collections and contained artifacts.
  - `MetadataRelationshipExtractor`: Extracts domain entity relationships (`TEACHES`, `ENROLLED_IN`, `LOCATED_IN`, `PREREQUISITE`, `USES`, `RELATED_TO`, `PART_OF`) from metadata fields (course codes, instructors, buildings, departments).

### 4. Graph Construction Pipeline
- `KnowledgeGraphBuilder`: Fluent builder for incremental `KnowledgeGraph` assembly.
- `GraphConstructionService`: Service component orchestrating graph construction from artifacts or collections:
  1. **Node Synthesis**: Transforms artifacts, collections, and metadata entity fields into `KnowledgeNode`s.
  2. **Node Merging**: Merges attributes and metadata when nodes with duplicate identifiers are encountered.
  3. **Edge Deduplication**: Combines parallel edges with identical source, target, and relationship type, upgrading relationship strength.
  4. **Provenance Preservation**: Preserves source artifact and collection lineage on all nodes and edges.
  5. **Consistency Validation**: Identifies and removes dangling edges pointing to missing nodes.

### 5. Storage Abstraction (`GraphStore`)
- `GraphStore` Interface: SPI defining graph storage operations (`save`, `findById`, `findAll`, `deleteById`, `existsById`, `createSnapshot`, `restoreSnapshot`).
- `InMemoryGraphStore`: Concurrent, thread-safe in-memory graph store implementation.
- `GraphSnapshot`: Point-in-time serializable snapshot wrapper for graph backup, export, and restoration.
- `GraphRepository`: High-level repository abstraction facilitating domain querying and node/edge lookup.
- Provider Compatibility: Storage SPI interfaces are designed for simple adapter mapping to Neo4j, JanusGraph, Apache TinkerPop, and Amazon Neptune.

### 6. Graph Registry & Cataloging
- `KnowledgeGraphRegistry`: Service tracking active knowledge graphs, graph lifecycle states, node/edge statistics, and catalog metadata.
- `GraphCatalogEntry`: Catalog summary object containing `graphId`, `name`, `version`, `lifecycleState`, `nodeCount`, `edgeCount`, `sourceCollectionIds`, and `lastDiagnostics`.

### 7. Traversal Engine & Policy Architecture
- `TraversalPolicy`: Policy configuration defining `maxDepth`, direction (`OUTGOING`, `INCOMING`, `BOTH`), `allowedRelationshipTypes`, `allowedNodeTypes`, `detectCycles`, `maxNodes`, `maxPaths`, and `minStrength`.
- `GraphTraversalEngine` Interface & Implementations:
  - `BreadthFirstTraversal`: Deterministic BFS path finding returning `KnowledgePath` objects.
  - `DepthFirstTraversal`: Deterministic DFS path finding with stack depth enforcement.
  - `NeighborhoodTraversal`: Localized k-hop neighborhood extraction returning `KnowledgeSubgraph` projections.

### 8. Graph Lifecycle Management
Tracks Knowledge Graph status across six discrete lifecycle states:
1. `DISCOVERED`: Initial declaration of graph requirement.
2. `BUILDING`: Construction, node synthesis, and edge extraction in progress.
3. `ACTIVE`: Graph ready for queries, traversals, and retrieval integration.
4. `UPDATING`: Incremental node/edge updates or re-indexing in progress.
5. `FAILED`: Construction or validation error state.
6. `ARCHIVED`: Archived graph instance.

Diagnostics (`GraphDiagnostics`) track build duration, node creation/merging counts, edge deduplication counts, and dangling edge cleanup stats.

### 9. Observability & Privacy
- `KnowledgeGraphMetrics`: Captures build latency (`atlas.graph.build.latency`), total nodes/edges (`atlas.graph.nodes.total`, `atlas.graph.edges.total`), traversal latency (`atlas.graph.traversal.latency`), and relationship extraction counters.
- **Privacy Guarantees**: Loggers NEVER emit raw node or document text content. Only graph IDs, node IDs, node types, relationship types, counts, and timing metrics are logged.

### 10. Extension Points (Batch 3.4.1)
- **Custom Relationship Extractors**: Implement `RelationshipExtractor` and register with `RelationshipRegistry`.
- **Custom Graph Store Adapters**: Implement `GraphStore` for Neo4j, JanusGraph, TinkerPop, or Neptune.
- **Custom Traversal Strategies**: Extend `GraphTraversalEngine` or `TraversalPolicy` for specialized graph algorithm execution.

---

## Graph Reasoning & Explainability Engine (Batch 3.4.2)

### 1. Overview & Architecture
The Graph Reasoning Engine extends Atlas Knowledge Graph infrastructure with deterministic graph slicing, rule-based inference, path discovery, structured explainability, and multi-factor confidence scoring. The reasoning engine operates **exclusively on `KnowledgeGraphView`** instances rather than mutating full `KnowledgeGraph` structures directly.

### 2. Graph Views & Projections
- `KnowledgeGraphView`: Read-only view interface defining graph query, node, edge, and adjacency access methods.
- `GraphProjection`: Immutable `KnowledgeGraphView` implementation representing a deterministic slice of a Knowledge Graph.
- `GraphProjectionPolicy`: Configurable policy encapsulating depth limits (`maxDepth`), edge strength thresholds (`minEdgeStrength`), collection boundaries (`allowedCollections`), node type filters (`allowedNodeTypes`), relationship type filters (`allowedRelationshipTypes`), and security permissions (`requiredPermissions`).
- `GraphProjectionBuilder`: Builder pattern executing deterministic graph slicing based on policy constraints and root seed nodes.
- `GraphProjectionEngine`: Service orchestrating projection strategies (`NeighborhoodProjection`, `CollectionProjection`, `TraversalProjection`), computing node/edge retention ratios, and tracking projection latency metrics.

### 3. GraphContext & Scope
- `GraphContext`: Encapsulates root seed nodes, graph projection view (`KnowledgeGraphView`), traversal policy, reasoning objective (`ReasoningObjective`), active collection boundaries, security permission context (`PermissionContext`), maximum reasoning depth, and confidence threshold.
- `ReasoningObjective`: Encapsulates objective type (`EXPLAIN_RELATIONSHIP`, `FIND_PATH`, `DISCOVER_INDIRECT_CONNECTIONS`, `VERIFY_INVARIANT`, `INFER_MISSING_LINKS`, `CONTEXT_GENERATION`, `CUSTOM`), description, and target node sets.
- `ReasoningConstraints` & `ReasoningScope`: Encapsulate execution bounds, time budget, and domain scope boundaries.

### 4. Non-Mutating Rule-Based Inference Engine
- `InferenceEngine`: Evaluates declarative inference rules over a `KnowledgeGraphView` without mutating the underlying graph.
- `InferenceRegistry`: Dynamic registry maintaining active `InferenceRule` instances.
- `InferenceRule` Interface & Built-In Rules:
  - `TransitiveRelationshipRule`: Infers indirect relationships (e.g. A -> C via `INFERRED_ACADEMIC_PEER` when A -> B and B -> C share compatible department relationships).
  - `HierarchicalPrerequisiteRule`: Infers multi-hop course prerequisite dependencies (`INDIRECT_PREREQUISITE`).
- `InferenceResult`: Returns non-persisted virtual inferred edges, applied rule counts, and execution metrics.

### 5. Multi-Strategy Reasoning Path Finder
- `ReasoningPathFinder`: Discovers candidate reasoning paths on a `KnowledgeGraphView` (including virtual inferred edges) across four strategies:
  - `SHORTEST`: BFS hop minimization.
  - `STRONGEST`: Maximizing cumulative edge relationship strength.
  - `HIGHEST_CONFIDENCE`: Maximizing path confidence scores.
  - `COLLECTION_AWARE`: Enforcing strict active collection boundaries.
- `EvidencePath`: Ordered node/edge path representation with hop count, cumulative score, and confidence.
- `ReasoningChain`: Step-by-step logical reasoning sequence with rationale and overall chain confidence score.

### 6. Explainability Pipeline
- `ExplanationEngine`: Transforms reasoning paths and inference outputs into structured human/LLM readable explanations.
- `ReasoningExplanation`: Complete explanation result containing:
  - Reasoning chain (`ReasoningChain`)
  - Primary evidence path (`EvidencePath`)
  - Deterministic confidence (`ReasoningConfidence`)
  - Explicit assumptions (`List<String>`)
  - Cited source artifacts (`List<String>`)
  - Cited graph edges (`List<String>`)
  - Step breakdown (`List<ExplanationStep>`)
  - Evidence summary (`EvidenceSummary`)

### 7. Multi-Factor Deterministic Confidence Model
- `ConfidenceCalculator`: Deterministic scoring model combining:
  - Relationship Strength (35% weight)
  - Retrieval Confidence (25% weight)
  - Evidence Quality (25% weight)
  - Inference Confidence (15% weight)
  - Traversal Depth Penalty (0.95^(depth-1))
- `ReasoningConfidence`: Overall score, confidence level (`HIGH`, `MEDIUM`, `LOW`, `UNCERTAIN`), factor breakdown (`ConfidenceFactors`), and narrative explanation.

### 8. Context Intelligence Layer Integration
- `ReasoningEvidence`: Formatted evidence output containing objective description, confidence score, summary narrative, cited node names, and cited relationship types.
- `GraphContextContributor`: Implements `ContextContributor` to enrich `AtlasContext` with `graphReasoning` evidence bundles for prompt pipeline assembly.

### 9. Future Expansion & Extension Points
Modular extension interfaces designed for zero-redesign future extension:
- `PlanningReasoningExtension`: Goal-oriented multi-step planning.
- `RecommendationReasoningExtension`: Personalized entity recommendations.
- `TemporalReasoningExtension`: Temporal graph snapshot and validity reasoning.
- `CausalReasoningExtension`: Root-cause and counterfactual analysis.
- `MultiAgentReasoningExtension`: Multi-agent federated graph context consensus.

### 10. Observability & Privacy Guarantees
- `GraphReasoningMetrics`: Captures projection latency, reasoning latency, inference latency, path finder latency, explanation latency, path length distributions, and confidence score stats.
- **Privacy Enforcement**: NEVER logs raw graph node payload text or user data. Only view IDs, node IDs, edge IDs, metric counts, and timing statistics are logged.

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


