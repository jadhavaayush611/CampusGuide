# Notice Board Domain Specifications

## Overview
The Notice Board module (`com.campusguide.campus.notice`) manages official campus announcements, departmental notices, and council-specific bulletins within the Campus domain of CampusGuide. It handles notice lifecycle state, category/priority tagging, targeting visibility, and custom pin/expiration ordering.

## Domain Model
- **Aggregate Root**: `Notice` (`notices` collection in MongoDB).
- **Primary Key**: `UUID` (`id`).
- **Unique Identifiers**: `slug` (indexed unique, URL-safe slug format).
- **Audit Fields**: `createdAt`, `updatedAt` (UTC timestamps).
- **State Flags**: `isPublished` (`Boolean`), `isPinned` (`Boolean`).
- **Optional External References**: `councilId` (`UUID`, validated reference to `Council` aggregate).

### Schema Definition
| Field | Type | Constraint / Validation | Description |
|---|---|---|---|
| `id` | UUID | Primary Key | Unique notice identifier |
| `title` | String | Required, Indexed | Title of the notice |
| `slug` | String | Unique, Required, URL-Safe Regex | Human-readable URL slug |
| `content` | String | Required | Full text content of the notice |
| `summary` | String | Optional | Short abstract summary |
| `category` | NoticeCategory | Enum (`ACADEMIC`, `EVENT`, `GENERAL`, `EXAM`, etc.) | Classification category |
| `priority` | NoticePriority | Enum (`LOW`(1), `MEDIUM`(2), `HIGH`(3), `URGENT`(4)) | Priority weighting |
| `visibility` | NoticeVisibility | Enum (`PUBLIC`, `STUDENTS`, `FACULTY`, etc.) | Target audience scope |
| `councilId` | UUID | Optional, Foreign Ref | Associated publishing council |
| `publishedAt` | LocalDateTime | Optional | Time notice becomes active |
| `expiresAt` | LocalDateTime | Optional | Auto-expiration cutoff time |
| `isPinned` | Boolean | Required (Default `false`) | Sticky display at top of board |
| `isPublished` | Boolean | Required (Default `false`) | Publication state flag |
| `createdAt` | LocalDateTime | System-managed | Record creation timestamp |
| `updatedAt` | LocalDateTime | System-managed | Record modification timestamp |

## Business Rules & Invariants
1. **Slug Format & Uniqueness**: Notice slugs must adhere to URL-safe regex (`^[a-z0-9]+(?:-[a-z0-9]+)*$`) and must be globally unique across all notices. Attempting to create or update with a non-unique slug raises `DuplicateNoticeSlugException` (`409 Conflict`).
2. **Council Reference Integrity**: If `councilId` is specified, the target Council must exist in the `councils` collection. Otherwise, raises `NoticeValidationException` (`400 Bad Request`).
3. **Publication & Expiration Chronology**: `expiresAt` must strictly occur after `publishedAt`. Violation throws `NoticeValidationException` (`400 Bad Request`).
4. **Public Query Filtering**: When `includeUnpublished=false`, notice listings return only notices where `isPublished=true`, `publishedAt <= now`, and `expiresAt > now` (or `expiresAt` is null).
5. **Notice Board Ordering**: Query results are ordered deterministically by:
   - Pinned status (`isPinned=true` first)
   - Priority weight (`URGENT` -> `HIGH` -> `MEDIUM` -> `LOW`)
   - Effective publication date (`publishedAt` descending)
   - Creation date (`createdAt` descending)

## Architectural Design Principles
- **Visibility vs. Authorization Separation**: `NoticeVisibility` specifies target audience semantics ("Who should see this notice?"). Authorization & access control rules ("Who is permitted to invoke endpoints or modify state?") are enforced exclusively in Spring Security (`@PreAuthorize`), keeping the domain model free of entitlement logic.
- **Encapsulated Priority Sorting**: Priority weighting and descending comparators (`NoticePriority.byWeightDesc()`) are encapsulated directly within the [NoticePriority](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/campus/notice/enums/NoticePriority.java) enum to prevent comparison logic leakage into application services.
