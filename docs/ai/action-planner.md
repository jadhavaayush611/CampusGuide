# Action Planner Architecture

## Overview
The Action Planner translates Atlas AI recommendations and intent classifications into structured, non-mutating action suggestions and UI navigation deep links for the client application.

---

## 1. Non-Mutating Execution Pipeline

> [!IMPORTANT]
> **SAFETY BOUNDARY**: The Action Planner generates action payloads (e.g., "Add CS201 to Semester 3 Plan"), but **never executes data mutations on behalf of the user directly**.

```mermaid
graph TD
    Atlas[Atlas AI Response] --> ActionPlanner[Action Planner]
    ActionPlanner --> RecommendationPayload[Structured Action Proposal DTO]
    RecommendationPayload --> ClientUI[Client UI Action Card]
    ClientUI -- User Clicks "Approve & Execute" --> StandardAPI[Standard Domain REST API Endpoint]
```

---

## 2. Action Types & Strategy Pattern Integration

The Action Planner operates in harmony with the **Recommendation Engine**, leveraging domain strategies:
- `AcademicRecommendationStrategy`: Suggests prerequisite fulfillment courses.
- `EventRecommendationStrategy`: Suggests upcoming council events and deadline alerts.
- `CommunityRecommendationStrategy`: Suggests relevant interest groups.
- `ResourceRecommendationStrategy`: Suggests study notes for active course enrollments.

---

## Cross-References
- [Atlas AI Architecture](file:///D:/CampusGuide/docs/ai/atlas.md)
- [Intent Engine Architecture](file:///D:/CampusGuide/docs/ai/intent-engine.md)
