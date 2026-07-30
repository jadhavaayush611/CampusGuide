# Atlas API Streaming Specification

## Overview

The Atlas API Platform provides real-time event-driven response streaming using Server-Sent Events (SSE) over HTTP (`text/event-stream`), with an optional WebSocket abstraction layer. Streaming enables immediate feedback for long-running context retrieval, multi-stage workflow planning, and token-by-token response emission.

---

## Endpoint

`POST /api/v1/atlas/chat/stream`

### Request Headers
- `Authorization`: `Bearer <JWT_TOKEN>`
- `Accept`: `text/event-stream`
- `Content-Type`: `application/json`
- `Last-Event-ID`: `<LAST_EVENT_ID>` (optional, for reconnection replay)

### Request Body (`AtlasChatRequest`)
```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "prompt": "What courses should I take next semester for AI specialization?",
  "model": "gpt-4o-mini",
  "temperature": 0.7
}
```

---

## Ordered Streaming Event Lifecycle

Every streaming session progresses through a deterministic, ordered sequence of event phases:

| Sequence | Event Type | Description | Payload Data |
| :--- | :--- | :--- | :--- |
| 1 | `CONNECTION_OPENED` | SSE connection established & session initialized | `{ "sessionId": "...", "status": "connected" }` |
| 2 | `THINKING` | Context retrieval & intent detection phase | `{ "phase": "intent_detection", "message": "..." }` |
| 3 | `REASONING` | Knowledge Graph reasoning & candidate generation | `{ "phase": "graph_reasoning", "message": "..." }` |
| 4 | `PLANNING` | Execution workflow formulation | `{ "phase": "plan_generation", "message": "..." }` |
| 5 | `EXECUTION_STARTED` | Workflow runtime execution initiated | `{ "executionId": "...", "status": "RUNNING" }` |
| 6 | `TOOL_STARTED` | Tool/Capability execution started | `{ "toolName": "...", "status": "IN_PROGRESS" }` |
| 7 | `TOOL_COMPLETED` | Tool/Capability execution completed | `{ "toolName": "...", "status": "SUCCESS" }` |
| 8 | `EXECUTION_COMPLETED` | Workflow runtime completed execution | `{ "executionId": "...", "status": "COMPLETED" }` |
| 9 | `RESPONSE_TOKEN` | Token chunk emission | `{ "token": "For ", "conversationId": "..." }` |
| 10 | `COMPLETION` | Final response metadata & usage statistics | `{ "id": "...", "finishReason": "stop", "usage": {} }` |
| 11 | `ERROR` | Emitted only when an exception occurs | `{ "error": "Error details..." }` |
| 12 | `CONNECTION_CLOSED` | Session closed gracefully | `{ "sessionId": "...", "status": "closed" }` |

---

## SSE Event Format

Events conform to standard W3C Server-Sent Events syntax:

```http
id: 550e8400-e29b-41d4-a716-446655440001
event: RESPONSE_TOKEN
data: {"id":"550e8400-e29b-41d4-a716-446655440001","type":"RESPONSE_TOKEN","conversationId":"550e8400-e29b-41d4-a716-446655440000","payload":{"token":"Data "},"timestamp":"2026-07-30T21:40:00Z","sequence":9}

```

---

## Resilience & Connection Management

1. **Reconnection & Replay**: Clients send `Last-Event-ID` on HTTP reconnect. `StreamPublisher` replays missed events from the session buffer.
2. **Duplicate Prevention**: Monotonic `sequence` numbers and event IDs allow clients to discard duplicate events.
3. **Heartbeat & Keepalive**: Periodically sends SSE ping comments to maintain connection through aggressive load balancer timeouts.
4. **Graceful Disconnect**: Emitters release resources cleanly on completion, error, client abort, or timeout (`300,000ms`).

---

## Observability & Privacy

Metrics recorded in `AtlasStreamingMetrics`:
- Active stream gauge (`atlas.streaming.active_streams`)
- Total reconnect counter (`atlas.streaming.reconnects.total`)
- Emitted events counter (`atlas.streaming.events`)
- Stream duration timer (`atlas.streaming.duration`)
- Failure counter (`atlas.streaming.failures`)

> [!IMPORTANT]
> **Privacy Invariant**: Prompts, response text, embeddings, and raw execution payloads are **NEVER** logged or included in metrics.
