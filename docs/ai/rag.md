# Retrieval-Augmented Generation (RAG) Architecture

## Overview
The RAG subsystem provides Atlas with domain-grounded knowledge from the CampusGuide single source of truth (MongoDB Atlas), ensuring AI responses accurately reflect institutional policies, course prerequisites, council rules, and campus events.

---

## 1. RAG Conceptual Flow

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant SpringBoot as Spring Boot Context Builder
    database Mongo as MongoDB Atlas
    participant Gateway as FastAPI AI Gateway

    User->>SpringBoot: Send Query ("What prerequisites do I need for CS301?")
    SpringBoot->>Mongo: Query Academic & Course Catalog Metadata
    Mongo-->>SpringBoot: Return Course Prerequisite Docs & Degree Rules
    SpringBoot->>SpringBoot: Augment Prompt Metadata with Institutional Knowledge
    SpringBoot->>Gateway: Dispatch Grounded Prompt
    Gateway-->>User: Return Accurate, Grounded Advisory Response
```

---

## 2. Core Knowledge Sources

1. **Academic Catalog**: Course descriptions, credit units, prerequisite constraints, and degree roadmaps.
2. **Campus Directory**: Council descriptions, community channels, active events, and resources.
3. **Institutional Notices**: Broadcast announcements, academic calendar dates, and campus policies.

---

## Cross-References
- [Atlas AI Architecture](file:///D:/CampusGuide/docs/ai/atlas.md)
- [Intent Engine Architecture](file:///D:/CampusGuide/docs/ai/intent-engine.md)
