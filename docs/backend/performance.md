# Backend Performance Audits and Optimizations

This document outlines the performance audits, analysis, and production-grade optimizations implemented in the CampusGuide backend to minimize resource consumption, eliminate bottlenecks, and improve request/response latencies.

---

## 1. Caching Reference Data

### Optimization Strategy
Static and frequently read campus reference metadata was previously re-instantiated and copied in memory on every request. We introduced the Spring Cache abstraction with a configured cache manager to cache these lookups.

### Implemented Caches
- **Campus Reference Caches**: Configured in [CacheConfig.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/common/config/CacheConfig.java).
  - `buildings` (maps building info requests)
  - `departments` (maps department info requests)
  - `faculty` (maps faculty profile/office hours search queries)
  - `laboratories` (maps laboratory info requests)
  - `classrooms` (maps classroom/room details)
  - `studentServices` (maps categorized student services queries)
  - `navigation` (caches pre-computed navigation routes between origin/destination pairs)
  - `emergencyContacts` (caches critical campus emergency details)
- **Student Councils Cache**:
  - `councils` (caches all councils list and individual councils by ID/slug).
  - Evicted dynamically using `@CacheEvict(value = "councils", allEntries = true)` upon any write operation (`create`, `update`, `delete`).

### Cache Configuration
Implemented in [CacheConfig.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/common/config/CacheConfig.java):
```java
@Configuration
@EnableCaching
public class CacheConfig {
    // Declares ConcurrentMapCacheManager with defined caches
}
```

---

## 2. Asynchronous Background Execution

### Optimization Strategy
Database-bound notification logging was previously performed synchronously during business logic updates. This blocked client requests during high-latency operations such as roadmaps publication, semester planning finalizations, event registrations, and recommendation triggers. We offloaded these tasks to background execution.

### Implemented Async Processing
- **Async Execution Configuration**: Created [AsyncConfig.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/common/config/AsyncConfig.java) configuring a managed `ThreadPoolTaskExecutor` thread pool.
- **Notification Async Dispatcher**: Introduced [NotificationAsyncDispatcher.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/personal/notification/service/impl/NotificationAsyncDispatcher.java) annotated with `@Async("taskExecutor")`.
- **Refactored Services**: Decoupled notification generation in the following services:
  - [RoadmapService.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/campus/academic/roadmap/service/RoadmapService.java)
  - [SemesterPlanService.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/campus/academic/semesterplanner/service/SemesterPlanService.java)
  - [EventRegistrationService.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/campus/event/service/EventRegistrationService.java)
  - [RecommendationService.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/personal/ai/recommendation/service/RecommendationService.java)

---

## 3. Database Layer Filtering (No In-Memory Filters)

### Optimization Strategy
Notice retrieval pulled all published notices from the database and filtered them in the JVM heap based on publication and expiration dates. This caused unnecessary memory allocation and CPU overhead.

### Implemented Query Optimization
- **MongoDB Query Pushdown**: Added a custom `@Query` to [NoticeRepository.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/campus/notice/repository/NoticeRepository.java):
  ```java
  @Query("{ 'isPublished': true, '$and': [ " +
         "  { '$or': [ { 'publishedAt': null }, { 'publishedAt': { '$lte': ?0 } } ] }, " +
         "  { '$or': [ { 'expiresAt': null }, { 'expiresAt': { '$gt': ?0 } } ] } " +
         "] }")
  List<Notice> findActiveNotices(LocalDateTime now);
  ```
- **Notice Service Update**: Updated [NoticeService.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/campus/notice/service/NoticeService.java) to query only active notices from the database using index `isPublished`.

---

## 4. Query Index Auditing & Verification

All standard/compound indices mapped across collections align perfectly with the querying and sorting criteria:
- `Roadmap`: `deleted_created_idx` (`{'isDeleted': 1, 'createdAt': -1}`) and `createdby_deleted_created_idx` (`{'createdBy': 1, 'isDeleted': 1, 'createdAt': -1}`).
- `StudentProgress`: `roadmap_created_idx` (`{'roadmapId': 1, 'createdAt': -1}`).
- `Notification`: Indexed on `userId` and query conditions.
