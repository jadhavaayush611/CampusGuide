# Atlas API Platform Specifications

## Base URL
`/api/v1/atlas`

---

## Overview & Architecture

The Atlas API Platform exposes Atlas Core as a secure, versioned, production-ready REST service. Designed following Clean Architecture principles, all HTTP controllers delegate exclusively to application services (`AtlasService`, `AtlasConversationService`, `AtlasWorkflowService`, `AtlasHealthService`) and contain zero business logic.

- **API Versioning**: Enforces versioned routing under `/api/v1/atlas` while establishing contract boundaries for future `/api/v2` expansion.
- **Authentication**: Integrates with Spring Security, JWT tokens, and `UserPrincipal` context. All Atlas API endpoints require valid authentication.
- **Authorization**: Validates resource ownership for conversations and workflow execution instances.
- **DTO Immutability**: Uses strict, immutable request/response DTOs to ensure internal runtime models and domain entities are never exposed across API boundaries.
- **Security & Privacy**: Enforces rate limiting, concurrent execution quotas per user, and metadata-only audit logging (prompts and execution payload contents are **never** logged).

---

## Endpoints Summary

| Method | Endpoint | Description | Security |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/atlas/chat` | Provider-agnostic contextual chat | Authenticated |
| `POST` | `/api/v1/atlas/chat/stream` | Real-time SSE response streaming (12 event types) | Authenticated |
| `GET` | `/api/v1/atlas/capabilities` | Capability discovery & features | Authenticated |
| `GET` | `/api/v1/atlas/info` | Operational metadata & version info | Authenticated |
| `POST` | `/api/v1/atlas/conversations` | Create conversation | Authenticated |
| `GET` | `/api/v1/atlas/conversations` | List user's conversations | Authenticated |
| `GET` | `/api/v1/atlas/conversations/{id}` | Get conversation details | Authenticated (Owner/Admin) |
| `PUT` | `/api/v1/atlas/conversations/{id}` | Update conversation title/status | Authenticated (Owner/Admin) |
| `POST` | `/api/v1/atlas/conversations/{id}/rename` | Rename conversation title | Authenticated (Owner/Admin) |
| `POST` | `/api/v1/atlas/conversations/{id}/archive` | Archive conversation | Authenticated (Owner/Admin) |
| `POST` | `/api/v1/atlas/conversations/{id}/restore` | Restore archived conversation | Authenticated (Owner/Admin) |
| `GET` | `/api/v1/atlas/conversations/{id}/summary` | Generate conversation summary | Authenticated (Owner/Admin) |
| `POST` | `/api/v1/atlas/conversations/{id}/continue` | Continue conversation session | Authenticated (Owner/Admin) |
| `DELETE` | `/api/v1/atlas/conversations/{id}` | Delete conversation & history | Authenticated (Owner/Admin) |
| `GET` | `/api/v1/atlas/conversations/{id}/history` | Fetch conversation message history | Authenticated (Owner/Admin) |
| `POST` | `/api/v1/atlas/workflows/execute` | Execute workflow asynchronously/sync | Authenticated |
| `GET` | `/api/v1/atlas/workflows/executions/{id}`| Query workflow execution status | Authenticated (Owner/Admin) |
| `GET` | `/api/v1/atlas/workflows/history` | List workflow execution history | Authenticated |
| `POST` | `/api/v1/atlas/workflows/executions/{id}/cancel` | Cancel running workflow execution | Authenticated (Owner/Admin) |
| `GET` | `/api/v1/atlas/health` | Comprehensive subsystem health | Public/Authenticated |
| `GET` | `/api/v1/atlas/ready` | Readiness probe (8 subsystems) | Public/Authenticated |
| `GET` | `/api/v1/atlas/live` | Liveness probe | Public/Authenticated |

---

## 1. Atlas Gateway (`AtlasController`)

### `POST /api/v1/atlas/chat`
Executes contextual multi-turn chat using Atlas Orchestration and Context Engine.

#### Request Body (`ChatRequest` / `AtlasChatRequest`)
```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "prompt": "What courses should I take next semester for AI specialization?",
  "systemPrompt": "You are Atlas, academic advisor for Alex",
  "conversationHistory": [
    {
      "role": "user",
      "content": "I am interested in Artificial Intelligence."
    },
    {
      "role": "assistant",
      "content": "AI specialization requires solid foundations in Data Structures and Linear Algebra."
    }
  ],
  "contextPlaceholders": {
    "department": "Computer Science"
  },
  "model": "gpt-4o-mini",
  "temperature": 0.7,
  "maxTokens": 1024,
  "attachments": []
}
```

#### Response Body (`ChatResponse` / `AtlasChatResponse`) — `200 OK`
```json
{
  "id": "atlas-mock-12345678-abcd-1234-abcd-123456789abc",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "content": "For an AI specialization, I recommend enrolling in Machine Learning (CS401) and Computer Vision (CS405).",
  "role": "assistant",
  "model": "gpt-4o-mini",
  "finishReason": "stop",
  "usage": {
    "promptTokens": 45,
    "completionTokens": 28,
    "totalTokens": 73
  },
  "timestamp": "2026-07-30T21:30:00",
  "metadata": {
    "provider": "OpenAI",
    "mode": "simulated"
  }
}
```

---

### `GET /api/v1/atlas/capabilities`
Exposes registered capabilities, available workflows, supported models, and operational limits.

#### Response Body (`CapabilityResponse`) — `200 OK`
```json
{
  "atlasVersion": "1.0.0",
  "apiVersion": "v1",
  "status": "OPERATIONAL",
  "registeredCapabilities": [
    "PROVIDER_AGNOSTIC_CHAT",
    "CONTEXT_INTELLIGENCE",
    "HYBRID_RAG",
    "KNOWLEDGE_GRAPH_REASONING",
    "DECISION_INTELLIGENCE",
    "WORKFLOW_ORCHESTRATION"
  ],
  "availableWorkflows": [
    "academic_advising_workflow",
    "course_recommendation_workflow",
    "campus_navigation_workflow",
    "default_workflow"
  ],
  "supportedFeatures": [
    "multi_turn_conversations",
    "selective_strategy_retrieval",
    "evidence_fusion",
    "token_budgeting",
    "circuit_breaker",
    "rate_limiting"
  ],
  "supportedModels": [
    "gpt-4o-mini",
    "gpt-4o",
    "mock-model"
  ],
  "provider": "OpenAI Resilient Provider",
  "limits": {
    "maxPromptLength": 4096,
    "maxTokens": 32000,
    "rateLimitPerMinute": 60
  }
}
```

---

## 2. Conversation Management (`AtlasConversationController`)

### `POST /api/v1/atlas/conversations`
Creates a new conversation session for the authenticated user.

#### Request Body (`ConversationCreateRequest`)
```json
{
  "title": "Fall Semester Degree Planning",
  "type": "ACADEMIC_ADVISOR",
  "metadata": {
    "department": "Computer Science"
  }
}
```

#### Response Body (`ConversationResponse`) — `201 Created`
```json
{
  "id": "c71a39f0-32b0-4f51-8664-92736bb6a100",
  "userId": "usr_98765",
  "title": "Fall Semester Degree Planning",
  "type": "ACADEMIC_ADVISOR",
  "status": "ACTIVE",
  "messageCount": 0,
  "createdAt": "2026-07-30T21:30:00Z",
  "updatedAt": "2026-07-30T21:30:00Z",
  "metadata": {
    "department": "Computer Science"
  }
}
```

---

### `GET /api/v1/atlas/conversations/{id}/history`
Fetches chronological message history for a specific conversation owned by the authenticated user.

#### Response Body (`ConversationHistoryResponse`) — `200 OK`
```json
{
  "conversationId": "c71a39f0-32b0-4f51-8664-92736bb6a100",
  "userId": "usr_98765",
  "messages": [
    {
      "role": "user",
      "content": "What electives are open for enrollment?"
    },
    {
      "role": "assistant",
      "content": "You have open seats in Advanced Database Systems and Data Privacy."
    }
  ],
  "totalMessages": 2
}
```

---

## 3. Workflow Execution (`AtlasWorkflowController`)

### `POST /api/v1/atlas/workflows/execute`
Executes an Atlas Workflow plan asynchronously or synchronously.

#### Request Body (`WorkflowExecutionRequest`)
```json
{
  "workflowId": "academic_advising_workflow",
  "parameters": {
    "targetSemester": "Fall 2026",
    "creditLimit": 16
  },
  "async": false,
  "timeoutSeconds": 60,
  "priority": "HIGH"
}
```

#### Response Body (`WorkflowExecutionResponse`) — `202 Accepted`
```json
{
  "executionId": "exec_4a91b2c3",
  "workflowId": "academic_advising_workflow",
  "status": "RUNNING",
  "result": {
    "message": "Workflow executed successfully",
    "workflowId": "academic_advising_workflow"
  },
  "startedAt": "2026-07-30T21:30:00Z",
  "completedAt": "2026-07-30T21:30:01Z",
  "executionTimeMs": 1050,
  "errorMessage": null
}
```

---

### `GET /api/v1/atlas/workflows/executions/{executionId}`
Monitors execution status and progress of a specific workflow instance.

#### Response Body (`ExecutionStatusResponse`) — `200 OK`
```json
{
  "executionId": "exec_4a91b2c3",
  "workflowId": "academic_advising_workflow",
  "userId": "usr_98765",
  "status": "COMPLETED",
  "progressPercent": 100,
  "currentStep": "COMPLETED",
  "startedAt": "2026-07-30T21:30:00Z",
  "updatedAt": "2026-07-30T21:30:01Z",
  "completedAt": "2026-07-30T21:30:01Z",
  "result": {
    "workflowId": "academic_advising_workflow"
  },
  "errorMessage": null
}
```

---

## 4. Health & Operational Probes (`AtlasHealthController`)

Atlas Health Probes evaluate 8 Core Subsystems:
1. **Runtime** (`WorkflowRuntime`)
2. **Orchestrator** (`ConversationOrchestrator`)
3. **Knowledge** (`KnowledgeCatalog`)
4. **Memory** (`ContextCache`)
5. **Vector Store** (`VectorStore`)
6. **LLM Provider** (`AIProvider` & `CircuitBreaker`)
7. **Database** (`ConversationRepository`)
8. **Cache** (`ContextCache`)

### `GET /api/v1/atlas/health`

#### Response Body (`AtlasHealthResponse`) — `200 OK`
```json
{
  "status": "UP",
  "subsystemReadiness": "READY",
  "timestamp": "2026-07-30T21:30:00Z",
  "components": {
    "runtime": { "status": "UP", "details": { "available": true } },
    "orchestrator": { "status": "UP", "details": { "available": true } },
    "knowledge": { "status": "UP", "details": { "available": true, "catalogEntries": 12 } },
    "memory": { "status": "UP", "details": { "available": true } },
    "vectorStore": { "status": "UP", "details": { "available": true } },
    "llmProvider": { "status": "UP", "details": { "available": true, "circuitBreakerState": "CLOSED", "providerName": "OpenAI Provider" } },
    "database": { "status": "UP", "details": { "available": true, "repository": "ConversationRepository" } },
    "cache": { "status": "UP", "details": { "available": true } }
  }
}
```

---

## Global Error Handling (`AtlasExceptionHandler`)

Standardized API error responses are returned for all client and server errors via `AtlasErrorResponse`:

```json
{
  "timestamp": "2026-07-30T21:30:00Z",
  "status": 400,
  "error": "Prompt message cannot be empty or blank",
  "message": "Request payload validation failed",
  "path": "/api/v1/atlas/chat",
  "details": {
    "prompt": "Prompt message cannot be empty or blank"
  }
}
```

### Standard Error Status Codes

| Code | Exception Type | Description |
| :--- | :--- | :--- |
| `400 Bad Request` | `MethodArgumentNotValidException`, `AtlasPromptValidationException` | Validation failure or malformed payload |
| `401 Unauthorized` | `AtlasAuthenticationException` | Missing or invalid JWT authentication token |
| `403 Forbidden` | `AtlasForbiddenException`, `AccessDeniedException` | User does not own requested conversation or workflow execution |
| `404 Not Found` | `AtlasNotFoundException` | Resource (conversation, execution) not found |
| `429 Too Many Requests` | `AtlasRateLimitException` | User rate limit or concurrent execution quota exceeded |
| `502 Bad Gateway` | `AtlasProviderException` | Upstream AI provider returned an unexpected error |
| `503 Service Unavailable` | `AtlasProviderUnavailableException` | AI provider disabled or circuit breaker state OPEN |
| `504 Gateway Timeout` | `AtlasTimeoutException` | Request timed out during provider invocation |
