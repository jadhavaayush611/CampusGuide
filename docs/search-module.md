# Global Search Module

## Objective
The Global Search module provides a unified, secure, and performant API endpoint (`POST /api/search`) for authenticated users to search across multiple CampusGuide modules from a single query.

---

## Supported Search Modules
Search is performed across the following CampusGuide modules:
1. **Courses**: Matches on `courseName`, `courseCode`, and `description`.
2. **Roadmaps**: Matches on `title` and `description`.
3. **Communities**: Matches on `name` and `description`.
4. **Campus Events (Events)**: Matches on `title`, `description`, and `location`.
5. **Resources**: Matches on `title`, `description`, and `tags`.

---

## Search Flow
The search process flows as follows:
1. **Request Reception**: The `SearchController` receives a `GlobalSearchRequest` (containing the search query and optional search types) along with pagination parameters (`Pageable`).
2. **Validation & Normalization**: The query is validated to ensure it is not blank. The query string is normalized by trimming leading/trailing whitespace.
3. **Target Routing**: If specific `types` are specified in the request, only those modules are queried. Otherwise, all five modules are searched.
4. **Repository Queries**: Lightweight case-insensitive database searches are executed using derived Spring Data queries:
   - Course: checks active courses.
   - Roadmap: checks non-deleted roadmaps.
   - Community: checks active communities.
   - Event: checks non-deleted events.
   - Resource: checks non-deleted resources.
5. **Relevance Scoring**: Matched entities are processed to compute a deterministic relevance score between `0.0` and `1.0`.
6. **Result Aggregation**: Results are unified and mapped to `SearchResultResponse` DTOs containing the matches, matching scores, and lightweight metadata specific to the entity type.
7. **Sorting**: Unified results are sorted globally in descending order of their relevance scores.
8. **Pagination**: The sorted list is paginated in-memory based on the request's Pageable settings, ensuring fast response times without excessive payloads.

---

## Relevance Scoring Model
A simple, deterministic scoring model is used to rank search results without external search engines. The relevance score is computed as the maximum of matching conditions (normalized between `0.0` and `1.0`):

| Matching Condition | Relevance Score |
|---|---|
| **Exact Title Match** (case-insensitive title equals query) | `1.0` |
| **Title Contains Query** (case-insensitive title contains query) | `0.9` |
| **Description Contains Query** (case-insensitive description contains query) | `0.7` |
| **Tag Match** (case-insensitive exact or substring match in any tags) | `0.6` |

---

## Security Model
Search implements the following security constraints:
- **Authentication**: Restricted to authenticated users only. Requests must contain a valid JWT Bearer token in the `Authorization` header.
- **Deleted/Inactive Filtering**: Only visible, active, and non-deleted entities are returned:
  - Courses: `active == true`
  - Roadmaps: `isDeleted == false`
  - Communities: `isActive == true`
  - Events: `isDeleted == false`
  - Resources: `isDeleted == false`
