# Personal Domain Overview

## Overview
The Personal domain (`com.campusguide.personal`) manages user-centric sub-systems, including AI conversations, notifications, achievements, recommendations, and student task planning.

## Modules

### 1. Planner (`com.campusguide.personal.planner`)
Manages personal tasks, study goals, assignment tracking, and event-linked planning.
- **Aggregate Root**: `PlannerTask`
- **Key Features**: Task creation, due date validation, reminder boundaries, status transitions, completed task immutability, ownership isolation.

### 2. Notifications (`com.campusguide.personal.notification`)
Delivers user notifications across campus events, academic updates, and personal reminders.

### 3. AI Gateway (`com.campusguide.personal.ai`)
Manages Atlas AI assistant conversations, message histories, and context generation.

### 4. Achievements (`com.campusguide.personal.achievement`)
Tracks student milestones, badges, and academic accomplishments.
