# Database Architecture, Auditing & Persistence Guidelines

This document details the MongoDB database configuration, auditing system, indexing strategy, repository patterns, migration framework, and guidelines for CampusGuide backend.

---

## 1. Database Collections

All MongoDB documents reside in collections with consistent pluralized names, managed via Spring Data MongoDB repositories.

| Collection Name | Entity Class | Primary Key | Description |
| :--- | :--- | :--- | :--- |
| `users` | `User` | `String` | User credentials, roles, profile flags, and audit timestamps. |
| `courses` | `Course` | `String` | Academic courses, prerequisite maps, and elective statuses. |
| `student_progress` | `StudentProgress` | `String` | Student GPA, academic roadmaps, and completed courses. |
| `roadmaps` | `Roadmap` | `String` | Degree roadmap path guides, credits, and visibility. |
| `semester_plans` | `SemesterPlan` | `String` | Individual semester plan layouts and finalized states. |
| `comments` | `Comment` | `String` | Post comments and soft-deletion tracking. |
| `communities` | `Community` | `String` | Student communities, member counts, and active status. |
| `councils` | `Council` | `UUID` | Student councils, faculty advisors, logo, and active state. |
| `events` | `Event` | `UUID` | Campus events, schedules, capacity, and registrations. |
| `notices` | `Notice` | `UUID` | Official notices, publishing states, visibility, and categories. |
| `posts` | `Post` | `String` | Community message posts, images, likes, and comment counts. |
| `resources` | `Resource` | `String` | File sharing repository, download links, types, and tags. |
| `achievement_progress` | `AchievementProgress` | `UUID` | User achievements, progress rates, and earned status. |
| `conversations` | `Conversation` | `String` | AI assistant chat history metadata and active state. |
| `messages` | `Message` | `String` | Chat messages under a specific conversation thread. |
| `calendar_entries` | `CalendarEntry` | `UUID` | Personal schedule calendar entries linked to tasks or events. |
| `notifications` | `Notification` | `String` | User-facing instant notifications and read tracking. |
| `scheduled_notifications` | `ScheduledNotification` | `UUID` | Scheduled push notifications to be dispatched. |
| `planner_tasks` | `PlannerTask` | `UUID` | Student to-do list tasks, categories, and due dates. |
| `db_migrations` | `DatabaseMigration` | `String` | DB migration log (stores executed change units). |

---

## 2. MongoDB Index Configuration

Indexes are optimized to minimize collection scans and maximize query performance across filtering, sorting, and lookup patterns. Unique constraints are made sparse or partial to support nullable/missing values gracefully.

### Index Auditing & Registry

1. **`users`**
   - Unique Index: `email` (Partial filter `{'email': {$type: 'string'}}`)
   - Unique Index: `username` (Partial filter `{'username': {$type: 'string'}}`)

2. **`courses`**
   - Unique Index: `courseCode`
   - Single Index: `department`, `semester`
   - Compound Indexes:
     - `dept_active_idx`: `{'department': 1, 'active': 1}`
     - `semester_active_idx`: `{'semester': 1, 'active': 1}`
     - `elective_active_idx`: `{'elective': 1, 'active': 1}`
     - `active_code_idx`: `{'active': 1, 'courseCode': 1}`

3. **`student_progress`**
   - Unique Index: `studentId`
   - Single Index: `roadmapId`
   - Compound Indexes:
     - `roadmap_created_idx`: `{'roadmapId': 1, 'createdAt': -1}`

4. **`roadmaps`**
   - Single Index: `degreeProgram`, `department`, `createdBy`
   - Compound Indexes:
     - `createdby_deleted_created_idx`: `{'createdBy': 1, 'isDeleted': 1, 'createdAt': -1}`
     - `degree_deleted_created_idx`: `{'degreeProgram': 1, 'isDeleted': 1, 'createdAt': -1}`
     - `dept_deleted_created_idx`: `{'department': 1, 'isDeleted': 1, 'createdAt': -1}`
     - `deleted_created_idx`: `{'isDeleted': 1, 'createdAt': -1}`

5. **`semester_plans`**
   - Single Index: `studentId`, `roadmapId`, `semesterNumber`
   - Compound Indexes:
     - `student_semester_idx`: `{'studentId': 1, 'semesterNumber': 1}` (Unique)
     - `roadmap_semester_idx`: `{'roadmapId': 1, 'semesterNumber': 1}`
     - `finalized_semester_idx`: `{'finalized': 1, 'semesterNumber': 1}`

6. **`comments`**
   - Single Index: `postId`, `authorId`
   - Compound Indexes:
     - `post_deleted_created_idx`: `{'postId': 1, 'isDeleted': 1, 'createdAt': 1}`
     - `author_deleted_idx`: `{'authorId': 1, 'isDeleted': 1}`
     - `deleted_created_idx`: `{'isDeleted': 1, 'createdAt': -1}`

7. **`communities`**
   - Unique Index: `name`
   - Single Index: `councilId`
   - Compound Indexes:
     - `council_active_idx`: `{'councilId': 1, 'isActive': 1}`

8. **`councils`**
   - Unique Index: `name`
   - Unique Index: `slug`

9. **`events`**
   - Unique Index: `slug`
   - Single Index: `councilId`
   - Compound Indexes:
     - `status_endtime_starttime_idx`: `{'status': 1, 'endTime': 1, 'startTime': 1}`

10. **`notices`**
    - Unique Index: `slug`
    - Single Index: `title`, `councilId`, `isPublished`
    - Compound Indexes:
      - `council_published_idx`: `{'councilId': 1, 'isPublished': 1}`

11. **`posts`**
    - Single Index: `authorId`, `communityId`
    - Compound Indexes:
      - `community_deleted_created_idx`: `{'communityId': 1, 'isDeleted': 1, 'createdAt': -1}`
      - `author_deleted_created_idx`: `{'authorId': 1, 'isDeleted': 1, 'createdAt': -1}`
      - `deleted_created_idx`: `{'isDeleted': 1, 'createdAt': -1}`

12. **`resources`**
    - Single Index: `uploaderId`, `councilId`, `communityId`
    - Compound Indexes:
      - `uploader_deleted_created_idx`: `{'uploaderId': 1, 'isDeleted': 1, 'createdAt': -1}`
      - `council_deleted_created_idx`: `{'councilId': 1, 'isDeleted': 1, 'createdAt': -1}`
      - `community_deleted_created_idx`: `{'communityId': 1, 'isDeleted': 1, 'createdAt': -1}`
      - `deleted_created_idx`: `{'isDeleted': 1, 'createdAt': -1}`

13. **`achievement_progress`**
    - Single Index: `userId`, `achievementCode`
    - Compound Indexes:
      - `user_achievement_code_idx`: `{'userId': 1, 'achievementCode': 1}` (Unique)
      - `user_category_idx`: `{'userId': 1, 'category': 1}`
      - `user_status_idx`: `{'userId': 1, 'status': 1}`
      - `user_category_status_idx`: `{'userId': 1, 'category': 1, 'status': 1}`

14. **`conversations`**
    - Single Index: `userId`
    - Compound Indexes:
      - `user_status_idx`: `{'userId': 1, 'status': 1}`

15. **`messages`**
    - Single Index: `conversationId`
    - Compound Indexes:
      - `conv_timestamp_idx`: `{'conversationId': 1, 'timestamp': 1}`

16. **`calendar_entries`**
    - Single Index: `userId`, `linkedPlannerTaskId`, `linkedEventId`, `startTime`, `endTime`
    - Compound Indexes:
      - `user_start_end_idx`: `{'userId': 1, 'startTime': 1, 'endTime': 1}`

17. **`notifications`**
    - Single Index: `userId`
    - Compound Indexes:
      - `user_read_idx`: `{'userId': 1, 'read': 1}`
      - `user_created_idx`: `{'userId': 1, 'createdAt': -1}`

18. **`scheduled_notifications`**
    - Single Index: `userId`, `status`, `scheduledFor`, `linkedPlannerTaskId`, `linkedCalendarEntryId`, `linkedEventId`, `linkedAchievementId`
    - Compound Indexes:
      - `user_status_scheduled_idx`: `{'userId': 1, 'status': 1, 'scheduledFor': 1}`
      - `status_scheduled_idx`: `{'status': 1, 'scheduledFor': 1}`

19. **`planner_tasks`**
    - Single Index: `userId`, `linkedEventId`
    - Compound Indexes:
      - `user_status_due_idx`: `{'userId': 1, 'status': 1, 'dueAt': 1}`
      - `user_due_idx`: `{'userId': 1, 'dueAt': 1}`

---

## 3. Spring Data Repository Conventions

All repositories extend Spring Data's `MongoRepository` interface and adhere to the following clean conventions:

*   **Derived Query Naming**: Clean derived queries using standard properties (e.g. `findByUserIdAndRead`). Unused query methods have been cleaned.
*   **Projections / Pageable**: Pagination and Sorting parameters (`Pageable`, `Sort`) are strictly used for paginated resources like Notifications to prevent memory bloat and database scans.
*   **Optional Wrapper**: Single entity lookups (e.g. `findByEmail`, `findBySlug`) return `Optional<T>` to force null-safe checks.
*   **Case Sensitivity**: Query names matches case exactly. Case-insensitive lookups use `IgnoreCase` suffix or regex filtering.

---

## 4. Custom Database Migration Framework

To keep deployment portable and database evolution completely reproducible without adding heavy third-party dependencies, a lightweight custom migration framework is configured:

1. **`Migration` Interface**: Defines the contract for all schema migrations.
   ```java
   public interface Migration {
       String getVersion();
       String getDescription();
       void execute(MongoTemplate mongoTemplate) throws Exception;
   }
   ```
2. **`MigrationRunner`**: Automatically runs during startup (`afterSingletonsInstantiated` phase). It:
   - Scans and resolves all Spring Beans implementing `Migration`.
   - Sorts them deterministically based on their version (e.g., version strings).
   - Queries `db_migrations` collection to guarantee idempotency (runs only pending migrations).
   - If any migration fails, it logs execution time, marks status as failed, and throws a `RuntimeException` to halt context startup immediately (**fail-fast**).
3. **Programmatic Index Creation**: Right after executing migrations, `MigrationRunner` scans all `@Document` entities in the Spring application context, resolves their indexes (both field-level `@Indexed` and class-level `@CompoundIndex` annotations), and ensures they are safely created and validated. Any index build errors trigger a startup termination.

---

## 5. Persistence Guidelines & Standards

To ensure database consistency, schema audit integrity, and optimistic concurrency control, developers must comply with the following standards:

### Concurrency (Optimistic Locking)
*   All persistent entities include an `@org.springframework.data.annotation.Version private Long version;` field.
*   This protects against concurrent write overwrite anomalies by throwing an `OptimisticLockingFailureException` if a document was modified by another thread.

### Document Auditing
*   Every collection has automated auditing fields:
    - `@CreatedDate private Instant createdAt;`
    - `@LastModifiedDate private Instant updatedAt;`
*   Do not modify these fields manually. Spring Data MongoDB automatically manages these timestamps via the registered `MongoAuditingConfig`.

### Date/Time representation
*   **Audit Fields**: Always use `java.time.Instant` for timezone-neutral timestamp auditing.
*   **Business Dates**: Use `java.time.LocalDateTime` for business-specific temporal dates (e.g. event start time, task due date) which are timezone-specific or local to the campus.

### Validation Constraints
*   Persistence objects use Jakarta Bean Validation annotations (like `@NotBlank`, `@Min`, `@Size`, `@Email`) to document properties.
*   Validation is enforced at the REST/DTO boundary level and validated before entering the services to keep test data creation flexible while ensuring production inputs remain strictly valid.

---

## 6. Future Migration Workflow

To add a new database migration:

1. Create a new Java class implementing `Migration` in `com.campusguide.common.migration`.
2. Annotate the class with `@Component` to make it eligible for Spring DI scanning.
3. Choose a version string that keeps ordering correct (e.g. `V1_1__add_new_fields.java`).
4. Implement the `execute(MongoTemplate mongoTemplate)` method to write index builds, schema upgrades, or data migrations.

**Example Migration**:
```java
package com.campusguide.common.migration;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class V1_1__AddDefaultSystemSettings implements Migration {

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    public String getDescription() {
        return "Add default system configuration properties.";
    }

    @Override
    public void execute(MongoTemplate mongoTemplate) throws Exception {
        // Implement migration logic using MongoTemplate
    }
}
```
