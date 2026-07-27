# Events Domain Specifications

## Overview
The Events module (`com.campusguide.campus.event`) provides the foundational aggregate root for campus activities, workshops, hackathons, and cultural events under the Campus domain. It manages event lifecycle status, scheduling boundaries, council ownership, registration constraints, and query access.

## Domain Model
- **Aggregate Root**: `Event` (`events` collection in MongoDB).
- **Primary Key**: `UUID` (`id`).
- **Unique Identifiers**: `slug` (indexed unique, URL-safe).
- **Foreign References**: `councilId` (`UUID`, indexed, referencing `Council`).
- **Enums**: `EventType` (`WORKSHOP`, `SEMINAR`, `HACKATHON`, `CULTURAL`, `SPORTS`, `WEBINAR`, `OTHER`), `EventStatus` (`DRAFT`, `PUBLISHED`, `CANCELLED`, `COMPLETED`).
- **Audit Fields**: `createdAt`, `updatedAt` (System-managed timestamps).

### Schema Definition
| Field | Type | Constraint / Validation | Description |
|---|---|---|---|
| `id` | UUID | Primary Key | Unique event identifier |
| `title` | String | Required | Full title of the event |
| `slug` | String | Unique, Indexed, Required | Human-readable URL slug |
| `description` | String | Required | Detailed event description |
| `summary` | String | Optional | Short summary snippet |
| `councilId` | UUID | Indexed, Required | ID of hosting Council aggregate |
| `venue` | String | Required | Physical or virtual location |
| `eventType` | EventType | Enum, Required | Category of event |
| `status` | EventStatus | Enum, Required | Operational state (`DRAFT`, `PUBLISHED`, `CANCELLED`, `COMPLETED`) |
| `registrationRequired` | Boolean | Optional | Registration requirement flag |
| `registrationStart` | LocalDateTime | Optional | Opening timestamp for registrations |
| `registrationEnd` | LocalDateTime | Optional | Closing timestamp for registrations |
| `capacity` | Integer | Positive | Maximum participant capacity |
| `startTime` | LocalDateTime | Required | Event start timestamp |
| `endTime` | LocalDateTime | Required | Event end timestamp |
| `bannerUrl` | String | Optional | URL to event promotional banner |
| `contactEmail` | String | Email | Organizer contact email |
| `contactNumber` | String | Optional | Organizer contact phone |
| `createdAt` | LocalDateTime | System-managed | Record creation timestamp |
| `updatedAt` | LocalDateTime | System-managed | Record modification timestamp |

## Business Rules & Invariants
1. **Title & Venue**: Required fields (`@NotBlank`).
2. **Slug Uniqueness**: Slugs must be unique across all events. Attempting to create or update an event with an existing slug throws `DuplicateEventSlugException` (`409 Conflict`).
3. **Council Reference Validation**: The `councilId` must correspond to an existing `Council` record, verified via `CouncilRepository.existsById`. Invalid references raise `CouncilNotFoundException` (`404 Not Found`).
4. **Temporal Ordering**:
   - `startTime` must strictly precede `endTime`.
   - If present, `registrationStart` must precede `registrationEnd`.
   - `registrationEnd` must be strictly before `startTime`. Violations throw `InvalidEventDataException` (`400 Bad Request`).
5. **Capacity**: Must be a positive integer (`capacity > 0`).
6. **Query Visibility**:
   - `/api/v1/events` queries `PUBLISHED` events where `endTime >= now()`.
   - Cancelled and completed events remain queryable via `/api/v1/events/{id}`, `/api/v1/events/slug/{slug}`, and `/api/v1/events/council/{councilId}`.
7. **Security Authorization**:
   - Mutation endpoints (`POST`, `PUT`, `PATCH /status`, `DELETE`) require `ROLE_SUPER_ADMIN` or `ROLE_COUNCIL_ADMIN`.
   - Read endpoints (`GET`) are open to all authenticated users (`isAuthenticated()`).
