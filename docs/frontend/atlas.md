# Atlas Frontend Module Integration Architecture

## Overview

The Atlas module in CampusGuide connects the user interface to the production Atlas AI Workflow Orchestration platform (`/api/v1/atlas/*`). Designed strictly following Clean Architecture and TanStack React Query patterns, components contain zero direct API `fetch`/`axios` calls. All server communications flow through the production SDK layer (`AtlasClient`, `ConversationClient`, `StreamingClient`, `WorkflowClient`).

Atlas is a **workflow orchestrator**, NOT a generic LLM chat clone. The UI highlights the end-to-end execution pipeline:
$$\text{Conversation} \longrightarrow \text{Streaming Response} \longrightarrow \text{Thinking Timeline} \longrightarrow \text{Planning} \longrightarrow \text{Tool Execution} \longrightarrow \text{Campus Result}$$

---

## Component Hierarchy

```
[AtlasPage]
 ├── [AtlasSidebar] (Conversation List, Filtering, Pagination, Search, Sorting, Create, Rename, Archive, Restore, Delete)
 ├── [AtlasHeader] (Active Conversation Metadata, Engine Controls, Capabilities Trigger)
 ├── [Message Canvas]
 │    ├── [MessageBubble] (User & Assistant bubbles)
 │    │    ├── [MarkdownRenderer] (Markdown & Code Block Syntax Highlighting + Copy)
 │    │    └── [CampusResultCard] (Deep-linked cards to owning campus modules)
 │    └── [MessageComposer] (Prompt input, Model selector, Keyboard shortcuts, Stop stream)
 ├── [Workflow Inspector]
 │    ├── [ThinkingTimeline] (Real-time visualization of 12 SSE streaming event phases)
 │    └── [ToolExecutionPanel] (Privacy-preserving tool execution status & duration)
 └── [AtlasCapabilitiesModal] (Subsystem readiness & operational health)
```

---

## SDK & Server State Management Architecture

All server state is managed through TanStack React Query hooks using centralized query key definitions:

| Hook | Type | Purpose | Cache Key Hierarchy |
| :--- | :--- | :--- | :--- |
| `useAtlasConversations` | Query | Paginated, searchable, sorted user conversation list | `['conversations', 'list', params]` |
| `useConversationDetails` | Query | Single conversation details by ID | `['conversations', 'detail', id]` |
| `useConversationHistory` | Query | Chronological message history for a conversation | `['conversations', 'detail', id, 'history']` |
| `useConversationSummary` | Query | Generated summary of key conversation topics | `['conversations', 'detail', id, 'summary']` |
| `useCreateConversation` | Mutation | Create a new conversation session | Invalidates `['conversations']` |
| `useRenameConversation` | Mutation | Update title of an existing conversation | Invalidates `['conversations']` & detail |
| `useArchiveConversation` | Mutation | Move active conversation to archived state | Invalidates `['conversations']` & detail |
| `useRestoreConversation` | Mutation | Restore archived conversation to active state | Invalidates `['conversations']` & detail |
| `useDeleteConversation` | Mutation | Permanently delete conversation & history | Invalidates `['conversations']` & removes detail |
| `useAtlasCapabilities` | Query | Registered engine capabilities & limit metadata | `['atlas', 'capabilities']` |
| `useAtlasStreamChat` | Custom Hook | Handles SSE response streaming via `StreamingClient` | Real-time state management |

---

## SSE Streaming Lifecycle (12 Event Phases)

`StreamingClient` processes W3C Server-Sent Events emitted by `/api/v1/atlas/chat/stream`. `useAtlasStreamChat` updates the `ThinkingTimeline` and `ToolExecutionPanel` upon receiving each phase:

1. `CONNECTION_OPENED`: SSE HTTP connection established & session initialized.
2. `THINKING`: Intent detection & contextual constraint extraction.
3. `REASONING`: Knowledge Graph navigation & evidence retrieval.
4. `PLANNING`: Multi-stage workflow plan formulation.
5. `EXECUTION_STARTED`: Atlas workflow runtime execution launched.
6. `TOOL_STARTED`: Capability tool execution initiated (e.g. `AcademicGraphTool`, `PlannerScheduleTool`, `NoticeSearchTool`).
7. `TOOL_COMPLETED`: Capability execution finished with duration & summary.
8. `EXECUTION_COMPLETED`: Atlas workflow runtime completed execution.
9. `RESPONSE_TOKEN`: Token-by-token text chunk emission.
10. `COMPLETION`: Final response emission with metadata and usage statistics.
11. `ERROR`: Emitted when upstream AI provider or pipeline exception occurs.
12. `CONNECTION_CLOSED`: Stream session gracefully closed or cancelled by client.

---

## Privacy & Security Guarantees

In strict alignment with backend security guidelines:
- **No Payload Exposure**: Raw prompts, vector embeddings, and internal API request payloads are **never** rendered or logged in execution panels.
- **Metadata Only**: `ToolExecutionPanel` displays tool names, execution state (`IN_PROGRESS`, `SUCCESS`, `FAILED`), and execution time (`durationMs`).
- **Resource Ownership**: Conversations are scoped strictly to the authenticated `UserPrincipal`.

---

## Deep-Link Architecture & Module Ownership

Atlas **orchestrates** modules without taking ownership or duplicating module business logic. Structured `CampusResult` objects emitted by workflow tools deep-link directly into owning pages:

- **Campus Navigation / Buildings / Rooms**: Deep-link to `/academic` or campus map view.
- **Communities**: Deep-link to `/communities/:id` or `/communities`.
- **Councils**: Deep-link to `/councils/:id` or `/councils`.
- **Planner (Schedules / Tasks)**: Deep-link to `/planner`.
- **Resource Center**: Deep-link to `/resources`.
- **Notice Board**: Deep-link to `/notices`.
- **Academic Degree Plan**: Deep-link to `/academic`.

---

## Definition of Done Verification

- ✅ Conversation Management: list, create, continue, rename, archive, restore, delete, history, summary.
- ✅ Support for pagination, search, and sorting.
- ✅ Streaming Chat: Markdown rendering with code blocks, copy button, message composer.
- ✅ All 12 SSE streaming lifecycle phases visualized in real time.
- ✅ Dedicated `ThinkingTimeline` first-class UI component.
- ✅ Dedicated `ToolExecutionPanel` respecting backend privacy guarantees.
- ✅ Deep-linked structured `CampusResultCard` components.
- ✅ Section-level error boundaries (`AtlasErrorBoundary`) for graceful error recovery.
