# Atlas AI Gateway API Specifications

## Base URL
`/api/v1/atlas`

---

## 1. Provider-Agnostic Orchestrated Chat
- **Endpoint**: `POST /api/v1/atlas/chat`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Content-Type**: `application/json`
- **Validation Rules**:
  - `prompt`: Required, non-blank, max 4096 characters.
  - `conversationId`: Optional UUID/string. If omitted, a new conversation is automatically created.
  - `temperature`: Optional, range `[0.0, 2.0]`.
  - `maxTokens`: Optional, positive integer.

Note: The `userId` is resolved server-side from JWT authentication via `CurrentUserService` and is not passed in the request body.

### Request Body (`AtlasChatRequest`)
```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "prompt": "What courses should I take next semester for AI specialization?",
  "systemPrompt": "You are Atlas, academic advisor for {student_name}",
  "conversationHistory": [
    {
      "role": "user",
      "content": "I am interested in Artificial Intelligence."
    },
    {
      "role": "assistant",
      "content": "That is a great path! AI specialization requires solid foundations in Data Structures and Linear Algebra."
    }
  ],
  "contextPlaceholders": {
    "student_name": "Alex",
    "department": "Computer Science"
  },
  "model": "gpt-4o-mini",
  "temperature": 0.7,
  "maxTokens": 1024
}
```

### Response Body (`AtlasChatResponse`) - `200 OK`
```json
{
  "id": "atlas-mock-12345678-abcd-1234-abcd-123456789abc",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "content": "For an AI specialization, I recommend enrolling in Machine Learning (CS401) and Computer Vision (CS405) next semester.",
  "role": "assistant",
  "model": "gpt-4o-mini",
  "finishReason": "stop",
  "usage": {
    "promptTokens": 45,
    "completionTokens": 28,
    "totalTokens": 73
  },
  "timestamp": "2026-07-28T23:30:00",
  "metadata": {
    "provider": "OpenAI",
    "mode": "simulated"
  }
}
```

### Orchestration & Context Pipeline Details

1. **Orchestration**:
   - `ConversationOrchestrator` manages conversation creation, message persistence, history loading, context aggregation, context section assembly, prompt construction, model execution, assistant response persistence, and structured execution logging.
2. **Context Engine, Contributors & Services**:
   - Strongly-typed context aggregate model (`AtlasContext`) composed of `UserContext`, `PlannerContext`, `CalendarContext`, `AcademicContext`, `CampusContext`, and `ContextMetrics`.
   - Domain-aware contributors (`ContextContributor`) backed by dedicated domain context services (`ContextService`):
     - `userProfile` (`UserProfileContributor` -> `UserContextService`): User details & profile context summary.
     - `planner` (`PlannerContributor` -> `PlannerContextService`): Active, overdue, and completed task summaries.
     - `calendar` (`CalendarContributor` -> `CalendarContextService`): Today's schedule and calendar event summaries.
     - `academic` (`AcademicContributor` -> `AcademicContextService`): Department, degree program, GPA, and course summaries.
     - `campus` (`CampusContributor` -> `CampusContextService`): Campus location, active notices, and announcements.
3. **Prompt Engineering & Pipeline**:
   - `ContextSectionAssembler`: Converts `AtlasContext` into structured `List<ContextSection>` with priority levels, category tags, required flags, and token estimates.
   - `CampusGuideAssistantPersona` & `InstructionLayer`s: Provides structured persona instructions (`CoreIdentityInstruction`, `SafetyInstruction`, `CampusInstruction`, `FormattingInstruction`, `ResponsePolicyInstruction`).
   - `TokenBudgetManager`: Estimates prompt size, prioritizes required context sections, prunes optional sections deterministically when budget is exceeded, reserves completion tokens, and limits history length.
   - `PromptBuilder`: Consumes `List<ContextSection>` (decoupled from `AtlasContext`), applies `PromptTemplate` and `TokenBudgetManager`, and attaches `PromptVersion` metadata.
4. **Diagnostic Metrics & Prompt Versioning**:
   - `ContextEngine` records per-contributor execution timing (`executionTimeMs`), estimated context size in bytes and tokens, skipped contributors, and failure diagnostics in `ContextMetrics`.
   - `PromptVersion` records prompt version string (`1.0.0`), included sections, skipped sections, and estimated prompt token breakdown.
5. **Structured Logging**:
   - Automatically logs: `conversationId`, `promptVersion`, `budgetUsage`, `includedSections`, `skippedSections`, `estimatedPromptTokens`, `latencyMs`, `model`, `promptTokens`, `completionTokens`, `totalTokens`, and `providerStatus`.
   - **Privacy Guarantee**: Prompt and response contents are never written to log outputs.

### Failure Responses

- **`400 Bad Request`** (`AtlasPromptValidationException` / `@Valid` failure)
  ```json
  {
    "error": "Prompt message cannot be empty or blank"
  }
  ```

- **`401 Unauthorized`** (Missing or invalid JWT token)
  ```json
  {
    "error": "Unauthorized access"
  }
  ```

- **`502 Bad Gateway`** (`AtlasProviderException`)
  ```json
  {
    "error": "OpenAI API error: Rate limit reached"
  }
  ```

- **`503 Service Unavailable`** (`AtlasProviderUnavailableException`)
  ```json
  {
    "error": "OpenAI provider is currently disabled or unavailable"
  }
  ```

- **`504 Gateway Timeout`** (`AtlasTimeoutException`)
  ```json
  {
    "error": "OpenAI request timed out or network was unreachable"
  }
  ```

---

## 2. Legacy / Conversation Store Endpoints
Base URL: `/api/v1/ai/conversations`

- `POST /api/v1/ai/conversations`: Create conversation
- `GET /api/v1/ai/conversations`: List user conversations
- `GET /api/v1/ai/conversations/{id}/messages`: Fetch messages
- `POST /api/v1/ai/conversations/{id}/messages`: Append user message
