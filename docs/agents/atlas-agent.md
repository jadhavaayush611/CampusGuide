# Atlas Agent Operational Guide

This document defines operational rules, prompt engineering standards, and gateway interaction principles for AI coding agents developing Atlas AI features.

---

## 1. Project Vision
Atlas is the intelligent backbone of CampusGuide, providing personalized academic advising, career coaching, campus navigation, and recommendation strategies.

---

## 2. Architecture
- **Provider-Independent AI Gateway**: FastAPI external gateway orchestrating LLM providers (OpenAI, Gemini, Claude).
- **Prompt Engine**: Externalized system prompt resource templates (`src/main/resources/prompts/`).
- **Context Builder**: Truncated sliding window history manager (`ai.gateway.history-limit=20`).

---

## 3. Responsibilities
- Maintain prompt resource templates and intent classification rules.
- Implement recommendation strategies using the Strategy pattern (`AcademicRecommendationStrategy`, `EventRecommendationStrategy`, etc.).
- Ensure graceful fallback responses during AI gateway timeouts or service outages.

---

## 4. Coding Standards
- Externalize system prompt templates into `.txt` resource files. Never hardcode system prompts in Java services.
- Return standardized `AiGatewayRequest` and `AiGatewayResponse` DTOs.
- Keep recommendation scoring normalized between `0.0` and `1.0`.

---

## 5. Naming Conventions
- Strategies: PascalCase ending with `RecommendationStrategy` (`ResourceRecommendationStrategy`).
- Prompts: Lowercase snake_case files in `src/main/resources/prompts/` (`academic_advisor.txt`).

---

## 6. What NOT to Do

> [!CAUTION]
> **CRITICAL INVARIANTS**:
> - **Atlas never mutates data directly**: Atlas generates advisory proposals, recommendations, and deep links, but **NEVER** mutates user or platform data directly. All writes require user execution via standard REST APIs.
> - **Calendar owns no data**: Atlas must reference calendar schedules via the aggregation layer, not by modifying calendar models.
> - **Business logic belongs in services**: Do not put recommendation scoring logic in controllers or prompt builders.
> - **Councils and Communities are separate concepts**: Maintain distinct intent classification rules for council queries vs community queries.
> - **Shared resources should not be duplicated**: Reference external resources by ID in recommendations rather than duplicating resource payloads in prompt contexts.

---

## 7. Development Workflow
1. Review Atlas architecture and prompt engineering guidelines in `docs/ai/`.
2. Test prompt variations and intent classification rules.
3. Validate recommendation strategy scores against unit tests.
4. Verify fallback execution during gateway timeouts.

---

## 8. Expected Output Quality
- Grounded, accurate advisory responses without halluncinated course codes or fake campus rules.
- Deterministic, explainable recommendation scoring.
- Zero transient AI errors persisted to MongoDB databases.

---

## Cross-References
- [Atlas AI Architecture](file:///D:/CampusGuide/docs/ai/atlas.md)
- [Intent Engine Architecture](file:///D:/CampusGuide/docs/ai/intent-engine.md)
