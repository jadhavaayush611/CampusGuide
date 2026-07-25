# Mobile Atlas AI Interface Specification

This document details the mobile user interface, streaming chat responses, voice input UI, and contextual prompt suggestions for Atlas AI.

---

## 1. Feature Overview
Atlas on mobile gives students instant, conversational access to an AI academic advisor and campus guide with streaming response rendering and contextual prompt pills.

---

## 2. Interactive Features

- **Streaming Response Bubble**: Renders incoming token chunks progressively via WebSocket or Server-Sent Events (SSE).
- **Contextual Prompt Pills**: Horizontal scroll bar displaying suggested quick queries (e.g., *"What electives fit my roadmap?"*, *"Show upcoming hackathons"*).
- **Action Proposal Cards**: Non-mutating UI cards embedded within assistant responses allowing one-tap navigation to relevant platform screens.

---

## Cross-References
- [Atlas AI System Architecture](file:///D:/CampusGuide/docs/ai/atlas.md)
- [Atlas API Framework](file:///D:/CampusGuide/docs/api/atlas.md)
