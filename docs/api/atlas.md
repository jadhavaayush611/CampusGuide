# Atlas AI API Framework

## Overview
The Atlas AI API provides intelligent assistant capabilities for student academic guidance, career advice, campus navigation, and personalized recommendations. It proxies and orchestrates requests via an external FastAPI AI Gateway.

---

## Endpoint Specifications

<!-- PLACEHOLDER: Endpoints -->
*Endpoint paths for conversation session lifecycle (POST/GET /api/ai/conversations), streaming/sync chat interactions, and prompt type selection.*

---

## Data Transfer Objects (DTOs)

<!-- PLACEHOLDER: Request DTOs -->
*Request DTOs for starting conversations, submitting user prompts, sending feedback ratings, and requesting recommendations.*

<!-- PLACEHOLDER: Response DTOs -->
*Response DTOs for assistant replies, token usage metrics, conversation summaries, and structured recommendation payloads.*

---

## Security & Access Control

### Authentication
- Requires valid JWT Bearer token.

### Authorization
<!-- PLACEHOLDER: Authorization -->
*Users can only access and interact with their own AI conversation sessions. Non-mutating access ensures Atlas never directly mutates system data.*

---

## Validation & Error Handling

### Validation Rules
<!-- PLACEHOLDER: Validation -->
*User message length limits, supported conversation types (`GENERAL_CHAT`, `ACADEMIC_ADVISOR`, `CAREER_GUIDANCE`, `CAMPUS_ASSISTANT`), and valid session IDs.*

### Error Responses
<!-- PLACEHOLDER: Error Responses -->
*Error handling and graceful fallback responses when the AI Gateway experiences timeouts, rate limits, or connectivity failures.*

---

## Cross-References
- [Atlas System Overview](file:///D:/CampusGuide/docs/ai/atlas.md)
- [Atlas Agent Guide](file:///D:/CampusGuide/docs/agents/atlas-agent.md)
