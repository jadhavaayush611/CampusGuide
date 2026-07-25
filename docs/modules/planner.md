# Planner Module Architecture

## 1. Purpose
The Planner module enables students to structure their multi-year academic trajectory, track course prerequisite trees, organize term course loads, and monitor degree completion progress.

---

## 2. Responsibilities
- Provide multi-semester course sequence planning for undergraduate and graduate programs.
- Validate prerequisite dependencies before allowing course additions to a term plan.
- Calculate completed vs remaining credit hours against degree roadmap requirements.
- Estimate semester and cumulative GPA progression scenarios.

---

## 3. Entities
- `Course`: Academic course definition, credit count, department, and prerequisite rules.
- `DegreeRoadmap`: Institutional curriculum requirements for a specific major/degree program.
- `StudentProgress`: Persistent student transcript record, completed courses, and active semester plans.
- `SemesterPlan`: Planned course enrollment list for a target academic term.

---

## 4. Services
- `PlannerService`: Evaluates degree roadmaps, validates prerequisite fulfillment, and manages semester plan revisions.
- `AcademicProgressService`: Computes overall GPA, credit tallies, and graduation eligibility metrics.

---

## 5. APIs
- `GET /api/academic/planner`: Fetch current student's multi-semester plan.
- `POST /api/academic/planner/semester`: Add or update a planned term schedule.
- `POST /api/academic/planner/validate`: Validate plan against course prerequisites.

---

## 6. Future Improvements
- Interactive visual drag-and-drop prerequisite dependency graph visualization.
- Automated scheduling clash detector for course time slots.
- AI-recommended optimal semester course combination generator.

---

## Cross-References
- [Academic Domain Architecture](file:///D:/CampusGuide/docs/architecture/domain-architecture.md)
- [Academic API Framework](file:///D:/CampusGuide/docs/api/academic.md)
