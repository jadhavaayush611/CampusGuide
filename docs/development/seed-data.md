# CampusGuide — MVP Realistic Seed Dataset Documentation

> [!IMPORTANT]
> **Synthetic Data Disclaimer**
> This dataset is synthetic and intended for MVP development, QA, demonstrations, and local testing. It is not an authoritative institutional dataset. Do not use real credentials or personal information in this environment.

---

## 1. Overview & Active Profiles

CampusGuide includes a comprehensive database seeder implemented as a startup database migration (`V1_1__MVPSeedDataset`).

- **Active Profile**: The seeder executes automatically on application startup when running under the `dev` or `test` profiles.
- **Safety**: In `prod` or other profiles, the migration checks the environment and skips database insertion to prevent accidental data contamination in production.

---

## 2. Seeded Account Credentials

All accounts are seeded with the common password: **`Password123`**.

### Golden QA Student (Te CMPN, Batch D12A)

This is the primary user account designed for manual testing, featuring dense connections across all modules:

- **Email**: `golden.student@ves.ac.in`
- **Username**: `golden.student`
- **Department**: `CMPN` (Computer Engineering)
- **Year**: `TE` (Third Year - Semester 5)
- **Batch**: `D12A`
- **Current GPA**: `9.20`
- **Course Progress**: 12 completed courses (Semesters 1-4) and 6 active/planned courses (Semester 5).
- **Council/Community interactions**: Multiple posts and comments.
- **Planner**: 7 planner tasks (completed and pending) linked to calendar entries.
- **Achievements/Goals**: 5 mock achievement goals with varying progress percentages (0%, 20%, 50%, 75%, 100%).
- **Notifications**: 8 notifications (unread/read) covering exam timetable, club invitations, and overdue reminders.
- **Atlas AI Conversation**: Pre-seeded conversation about HOD offices and library locations.

### Administrative Accounts (12 Total)

Covering security roles and organizational categories:

- **Super Admin**: `super.admin@ves.ac.in` (Username: `super.admin`)
- **Academic Staff**: `admin.staff@ves.ac.in` (Username: `admin.staff`)
- **Council Admins**:
  - `council.admin@ves.ac.in`
  - `csi.admin@ves.ac.in`
  - `ieee.admin@ves.ac.in`
  - `cc.admin@ves.ac.in`
  - `sort.admin@ves.ac.in`
  - `sports.admin@ves.ac.in`
  - `veslang.admin@ves.ac.in`
  - `veslit.admin@ves.ac.in`

### Faculty Accounts (36 Total)

Each of the 6 departments has exactly **1 HOD** and **5 faculty members**.

- **ECS HOD**: `hod.ecs@ves.ac.in` (Dr. Manish Trivedi)
- **AURO HOD**: `hod.auro@ves.ac.in` (Dr. Deepak Mishra)
- **AIDS HOD**: `hod.aids@ves.ac.in` (Dr. Sanjay Patel)
- **CMPN HOD**: `hod.cmpn@ves.ac.in` (Dr. Asha Bharambe)
- **EXTC HOD**: `hod.extc@ves.ac.in` (Dr. Raj Reddy)
- **IT HOD**: `hod.it@ves.ac.in` (Dr. Shreya Mukherjee)
- **General Faculty**: `[firstname].[lastname]@ves.ac.in` (e.g. `rajesh.kulkarni@ves.ac.in`, `aanchal.joshi@ves.ac.in`).

### General Student Accounts (72 Total)

Distributed across all 6 departments, 4 academic years (FE/SE/TE/BE), and all authoritative batch identifiers (e.g., `D1EC`, `D6ADA`, `D12A`, `D20C`).
- **Email**: `[firstname].[lastname][index]@ves.ac.in` (e.g. `aarav.sharma0@ves.ac.in`, `aditya.verma3@ves.ac.in`)
- **Password**: `Password123`

---

## 3. Dataset Coverage

| Entity | Target Count | Seeded Count | Details |
| :--- | :--- | :--- | :--- |
| **Users (Students)** | 60–80 | **73** | Golden Student + 72 general students across all batches |
| **Users (Faculty)** | 35–45 | **36** | 6 HODs + 30 Faculty members |
| **Users (Admin)** | 10–15 | **12** | Super Admins and Council Admins |
| **Courses** | ~40 | **45** | 9 Foundation + 36 Departmental courses |
| **Councils** | 9 | **9** | VESLANG, VESLIT, SORT, CC, Sports, IEEE, iSTE, ISA, CSI |
| **Communities** | 5 | **5** | GDG, AI & ML, Cybersecurity, Web Dev, Photography |
| **Resources** | 30–50 | **35** | Reference guides, maps, lab manuals, and calendars |
| **Notices** | ~30 | **30** | Academic schedules, club workshops, maintenance alerts |
| **Events** | ~25 | **25** | Hackathons, webinars, cultural fests, sports, with overlapping slots |
| **Planner Tasks** | ~40 | **43** | 7 for Golden Student (incl. 2 study goals) + 36 across other students |
| **Achievements/Goals** | - | **5** | Academic progress goals with varied percentage completions |
| **Notifications** | ~50 | **51** | 8 for Golden Student + 43 across other students |
| **Posts** | - | **22** | Disk-seeded community feed posts |
| **Comments** | - | **24** | Replies to posts |
| **Atlas Conversations** | - | **1** | Seeded chat history (4 messages) for the Golden QA Student |

---

## 4. Seeding Operations

### How to Seed the Database
The seeding migration runs automatically on application startup. Simply start the Spring Boot application under the `dev` profile:
```bash
# From the backend directory
./mvnw.cmd spring-boot:run
```

### Idempotency Strategy
Repeated application startup will **not** create duplicate records. The migration implements the following duplicate checks:
- **Users**: Checked via `userRepository.findByEmail(email)`.
- **Courses**: Checked via `courseRepository.findByCourseCode(code)`.
- **Councils**: Checked via `councilRepository.findBySlug(slug)`.
- **Communities**: Checked via `communityRepository.findByName(name)`.
- **Notices**: Checked via `noticeRepository.existsBySlug(slug)`.
- **Events**: Checked via `eventRepository.existsBySlug(slug)`.
- **Resources, Planner Tasks, Notifications, Achievements, Conversations**: Skip logic guards database insertion if existing records exceed baseline quantities.

### Reset / Reseed the Dataset
To perform a complete clean reset of the seed dataset:
1. Stop the backend server.
2. Connect to your MongoDB instance (e.g., via `mongosh` or MongoDB Compass).
3. Drop the database or delete the `db_migrations` document representing version `"1.1"` to re-run the seeder:
   ```javascript
   use campusguide;
   db.db_migrations.deleteOne({ _id: "1.1" });
   // To wipe all data and start completely fresh:
   db.dropDatabase();
   ```
4. Restart the backend server. The database migration system will re-apply `1.0` and `1.1` migrations.

---

## 5. Atlas AI Search Invariants

The seeded dataset integrates with the Atlas context intelligence engine, allowing it to answer queries such as:

- *"Where is the AIDS department?"* $\rightarrow$ **2nd floor of VESIT**.
- *"What batches does CMPN have?"* $\rightarrow$ **D2A/B/C, D7A/B/C, D12A/B/C, D17A/B/C**.
- *"What technical councils are available?"* $\rightarrow$ **IEEE, iSTE, ISA, CSI**.
- *"Are there any AI communities?"* $\rightarrow$ **AI & ML Club and GDG**.
- *"Where is the library?"* $\rightarrow$ **1st floor of VESIT**.
- *"Find resources for DBMS"* $\rightarrow$ retrieves **DBMS Relational Algebra notes**.

---

## 6. Batch Structure Verification & Reconciliation

### Locked Specification Reconciliation
There was a textual discrepancy in the original specification stating a total of "42 batches" while enumerating a structure that lists **48 distinct batch codes**. 

Upon audit of the database models, we verify:
1. Neither the `User` nor the `StudentProgress` database collections contain a persistable `batch` field. Batch memberships are conceptual.
2. The seeder code (`V1_1__MVPSeedDataset.java`) generates student records distributed deterministically across 24 representative batch slots (using the local `batches` matrix representing 1 batch code per department per year).
3. The RAG context intelligence layer (`ContextIntelligenceEngine.java`) is integrated with the complete, authoritative list of **48 distinct batch codes** to resolve query requests (such as *"What batches does CMPN have?"*) and match them to the correct academic department.

### Complete List of Authoritative Batches (48 Total)
The 48 verified batch codes integrated into the Atlas RAG search context are:
* **FE (12 Batches)**: `D1EC`, `D1ADA`, `D1ADB`, `D2A`, `D2B`, `D2C`, `D3`, `D4A`, `D4B`, `D5A`, `D5B`, `D5C`
* **SE (12 Batches)**: `D6EC`, `D6ADA`, `D6ADB`, `D7A`, `D7B`, `D7C`, `D8`, `D9A`, `D9B`, `D10A`, `D10B`, `D10C`
* **TE (12 Batches)**: `D11EC`, `D11ADA`, `D11ADB`, `D12A`, `D12B`, `D12C`, `D13`, `D14A`, `D14B`, `D15A`, `D15B`, `D15C`
* **BE (12 Batches)**: `D16EC`, `D16ADA`, `D16ADB`, `D17A`, `D17B`, `D17C`, `D18`, `D19A`, `D19B`, `D20A`, `D20B`, `D20C`
