# Frontend SDK Architecture

## Overview

CampusGuide enforces a strict architectural boundary: **Feature pages and components must never communicate directly with the API client or execute direct `fetch` calls.** All backend interaction must pass through the production SDK layer located at `src/sdk/`.

The SDK layer encapsulates endpoint URLs, manages request payload construction, parses raw backend Data Transfer Objects (DTOs), transforms them into clean Frontend UI Models, and propagates strongly typed `SdkError` instances.

---

## SDK Directory Structure

```
src/sdk/
├── common/
│   ├── BaseSdk.ts        # Abstract base class providing HTTP execution & error wrapping
│   ├── SdkError.ts       # Typed error class extending AppError
│   └── types.ts          # Common pagination & response envelope types
├── auth/
│   ├── auth.dto.ts       # Backend DTO schemas for Auth
│   ├── auth.mapper.ts    # DTO-to-UI model transformers
│   └── AuthSdk.ts        # Production Auth SDK class & singleton instance
├── campus/
│   ├── campus.dto.ts     # Backend DTO schemas for Campus & Facilities
│   ├── campus.mapper.ts  # Campus DTO-to-UI model transformers
│   └── CampusSdk.ts      # Production Campus SDK class & singleton instance
├── planner/
│   ├── planner.dto.ts    # Backend DTO schemas for Academic Planner
│   ├── planner.mapper.ts # Planner DTO-to-UI model transformers
│   └── PlannerSdk.ts     # Production Planner SDK class & singleton instance
├── atlas/
│   ├── atlas.dto.ts      # Backend DTO schemas for Atlas Maps & Wayfinding
│   ├── atlas.mapper.ts   # Atlas DTO-to-UI model transformers
│   └── AtlasSdk.ts      # Production Atlas SDK class & singleton instance
├── queryKeys.ts          # Centralized, domain-grouped query keys
└── index.ts              # Unified SDK export entrypoint
```

---

## Key Principles & Design Conventions

1. **DTO vs UI Model Separation**:
   - Backend DTOs (`*.dto.ts`) reflect raw server schemas (nullable fields, snake_case or specific JSON formats).
   - Frontend Models (`src/models/*.model.ts`) provide clean, predictable UI objects used across React components.
   - Domain Mappers (`*.mapper.ts`) convert DTOs to UI models upon response parsing.

2. **Single Responsibility & Endpoint Encapsulation**:
   - Component developers never need to know endpoint paths (e.g. `/api/events/upcoming`).
   - SDK methods (e.g. `campusSdk.getUpcomingEvents()`) handle URL formatting and query parameters.

3. **Unified Error Propagation**:
   - All network, timeout, or HTTP status errors thrown during request execution are caught by `BaseSdk` and transformed into `SdkError` instances containing `statusCode`, `code`, `details`, and `correlationId`.

---

## SDK Modules Reference

### 1. Authentication SDK (`AuthSdk`)
- `login(credentials: LoginCredentials): Promise<AuthSession>`
- `register(payload: RegisterPayload): Promise<AuthSession>`
- `getCurrentUser(): Promise<User>`
- `refreshToken(refreshToken: string): Promise<AuthSession>`
- `updateProfile(userId: string, payload: UpdateProfileDto): Promise<User>`
- `changePassword(payload: PasswordChangePayload): Promise<void>`
- `logout(): Promise<void>`

### 2. Campus SDK (`CampusSdk`)
- `getBuildings(): Promise<Building[]>`
- `getBuildingById(id: string): Promise<Building>`
- `getLocations(buildingId?: string): Promise<Location[]>`
- `getFloorPlans(buildingId: string): Promise<FloorPlan[]>`
- `getEvents(): Promise<CampusEvent[]>`
- `getUpcomingEvents(): Promise<CampusEvent[]>`
- `getEventById(eventId: string): Promise<CampusEvent>`
- `createEvent(payload: CreateEventDto): Promise<CampusEvent>`
- `updateEvent(eventId: string, payload: UpdateEventDto): Promise<CampusEvent>`
- `deleteEvent(eventId: string): Promise<void>`
- `registerForEvent(eventId: string): Promise<CampusEvent>`
- `cancelEventRegistration(eventId: string): Promise<CampusEvent>`
- `getCouncils(): Promise<Council[]>`
- `getResources(): Promise<Resource[]>`

### 3. Academic Planner SDK (`PlannerSdk`)
- `getSchedules(): Promise<Schedule[]>`
- `getScheduleById(id: string): Promise<Schedule>`
- `createSchedule(payload: CreateScheduleDto): Promise<Schedule>`
- `updateSchedule(id: string, payload: UpdateScheduleDto): Promise<Schedule>`
- `deleteSchedule(id: string): Promise<void>`
- `getCourses(department?: string): Promise<Course[]>`
- `getTimetable(scheduleId?: string): Promise<TimetableSlot[]>`
- `getStudyGoals(): Promise<StudyGoal[]>`
- `createStudyGoal(payload: CreateStudyGoalDto): Promise<StudyGoal>`

### 4. Atlas Maps SDK (`AtlasSdk`)
- `searchSpatial(query: string, category?: string, userLat?: number, userLng?: number): Promise<SpatialSearchResult[]>`
- `calculateRoute(request: RouteRequestDto): Promise<CalculatedRoute>`
- `getLandmarks(category?: string): Promise<Landmark[]>`
- `getLandmarkById(id: string): Promise<Landmark>`
- `getMapLayers(): Promise<MapLayer[]>`
