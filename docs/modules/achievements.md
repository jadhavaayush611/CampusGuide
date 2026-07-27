# Achievement & Progression Module

## Overview
The Achievement & Progression module (`com.campusguide.personal.achievement`) tracks student milestones, skill badges, academic accomplishments, and personal achievements.

## Aggregate Root
- **`AchievementProgress`**: Represents a student's progress toward a specific achievement milestone.
  - **Primary Key**: `UUID id`
  - **User Ownership**: `UUID userId` (indexed)
  - **Achievement Code**: `String achievementCode` (indexed)
  - **Compound Unique Index**: `(userId, achievementCode)` guarantees that a user cannot have duplicate entries for the same achievement code.

## Domain Enums
- **`AchievementCategory`**: `ACADEMIC`, `CAMPUS_LIFE`, `PERSONAL`, `SKILLS`, `COMMUNITY`, `CAREER`, `GENERAL`
- **`AchievementStatus`**: `LOCKED`, `IN_PROGRESS`, `EARNED`

## Business Rules & Invariants
1. **Authenticated Ownership**: Ownership is derived strictly from authentication (`UserDetails`). No client `userId` is accepted in request DTOs.
2. **Progress Bounds**: Progress must be an integer between `0` and `100` inclusive.
3. **Status Transitions**:
   - `0%`: `LOCKED`
   - `1% - 99%`: `IN_PROGRESS`
   - `100%`: `EARNED`
4. **Earned Immutability**: Once an achievement status reaches `EARNED`, it cannot be downgraded or transitioned back to `< 100%` (LOCKED or IN_PROGRESS).
5. **Automatic `earnedAt` Timestamping**: `earnedAt` is automatically set to `LocalDateTime.now()` when progress reaches `100%` / status becomes `EARNED`.
6. **Optional Fields**:
   - `evidenceUrl`: Optional link/proof of completion (e.g. transcript URL, certificate PDF).
   - `metadata`: Optional `Map<String, Object>` key-value pairs for flexible context tagging (e.g., term, GPA, issuer).

## Future Ecosystem Integration
The schema and domain design support future platform extensions without breaking compatibility:
- **Resume Builder**: Export earned achievements with `evidenceUrl` and `metadata` to dynamic resume generators.
- **Profile Showcase**: Public/peer showcase of verified earned badges and skills.
- **AI Personalization**: Atlas AI agent analyzes user achievement progression to provide customized study recommendations and career paths.
- **Event Participation**: Automatic progress increment upon event attendance confirmation.
- **Planner Milestones**: Automated achievement triggering upon completing linked planner tasks/milestones.
