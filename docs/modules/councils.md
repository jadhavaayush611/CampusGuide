# Councils Domain Specifications

## Overview
The Councils module (`com.campusguide.campus.council`) serves as the aggregate root for governance bodies within the Campus domain of CampusGuide. It manages council profiles, metadata, lifecycle statuses, and dependency relationships with Communities, Events, and Resources.

## Domain Model
- **Aggregate Root**: `Council` (`councils` collection in MongoDB).
- **Primary Key**: `UUID` (`id`).
- **Unique Identifiers**: `name` (indexed unique) and `slug` (indexed unique, URL-safe).
- **Audit Fields**: `createdAt`, `updatedAt` (UTC timestamps).
- **Status Flag**: `isActive` (`Boolean`, defaults to `true`).

### Schema Definition
| Field | Type | Constraint / Validation | Description |
|---|---|---|---|
| `id` | UUID | Primary Key | Unique council identifier |
| `name` | String | Unique, Required (2-100 chars) | Full official name of the council |
| `slug` | String | Unique, Required, URL-Safe Regex | Human-readable URL identifier |
| `description` | String | Required (Max 2000 chars) | Purpose and description of the council |
| `logoUrl` | String | Optional | URL to council logo asset |
| `email` | String | Valid Email format | Contact email address |
| `contactNumber` | String | Optional | Phone contact |
| `facultyAdvisor` | String | Optional | Name/ID of faculty advisor |
| `isActive` | Boolean | Required | Active operational status |
| `createdAt` | LocalDateTime | System-managed | Record creation timestamp |
| `updatedAt` | LocalDateTime | System-managed | Record modification timestamp |

## Business Rules & Invariants
1. **Slug Validation**: Slugs must contain only lowercase letters, numbers, and single hyphens (`^[a-z0-9]+(?:-[a-z0-9]+)*$`).
2. **Name & Slug Uniqueness**: Creating or updating a council with a name or slug matching an existing council (excluding itself) raises a `DuplicateCouncilException` (`409 Conflict`).
3. **Dependency Protection**: Deleting a council is blocked if dependent entities (Communities, active Events, or active Resources) reference its `councilId`. Raises `CouncilHasDependenciesException` (`409 Conflict`).
4. **Role Security**: Mutative operations (`POST`, `PUT`, `PATCH`, `DELETE`) require `ROLE_SUPER_ADMIN`. Read operations (`GET`) are accessible to all authenticated users (`isAuthenticated()`).
