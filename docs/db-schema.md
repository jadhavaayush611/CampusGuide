# CampusGuide Database Schema (MVP)

## Database

MongoDB Atlas

---

# 1. users

Purpose:
Authentication, Profiles, Roles, Premium Access

Fields:

* _id
* email
* password
* firstName
* lastName
* role
* department
* year
* profilePictureUrl
* phoneNumber
* bio
* isPremium
* isVerified
* createdAt
* updatedAt

Role Values:

* STUDENT
* FACULTY
* COUNCIL_ADMIN
* SUPER_ADMIN

---

# 2. councils

Purpose:
Council Information and Management

Fields:

* _id
* name (unique index)
* description
* logoUrl
* category
* facultyAdvisorId (reference to User)
* councilAdminIds (Array of references to User)
* memberCount
* isActive
* createdAt
* updatedAt

---

# 3. membership_applications

Purpose:
Council Recruitment and Membership Drives

Fields:

* _id
* userId
* councilId
* status
* applicationAnswers
* submittedAt
* reviewedAt
* reviewedBy

Status Values:

* PENDING
* APPROVED
* REJECTED

---

# 4. communities

Purpose:
Student Communities

Fields:

* _id
* name (unique index)
* description
* bannerUrl
* councilId (reference to Council)
* memberCount
* isActive
* createdAt
* updatedAt

---

# 5. posts

Purpose:
Community Discussions

Fields:

* _id
* communityId (reference to Community)
* authorId (reference to User)
* title
* content
* imageUrls (Array of Strings)
* likeCount
* commentCount
* isPinned
* isEdited
* isDeleted (soft delete)
* createdAt
* updatedAt

---

# 6. comments

Purpose:
Post Comments

Fields:

* _id
* postId (reference to Post)
* authorId (reference to User)
* content
* isEdited
* isDeleted (soft delete)
* createdAt
* updatedAt

---

# 7. events

Purpose:
Events and Hackathons

Fields:

* _id (String, required): Unique identifier of the event.
* title (String, required): Title of the event.
* description (String, required): Detailed description of the event.
* councilId (String, required): ID of the council organizing the event.
* organizerId (String, required): ID of the user who created the event.
* location (String, required): Venue/location of the event.
* startTime (Date/Time, required): Start time of the event.
* endTime (Date/Time, required): End time of the event.
* registrationDeadline (Date/Time, required): Deadline to register for the event.
* maxParticipants (Integer, optional): Maximum number of participants.
* attendeeCount (Integer, required): Current count of registered users (defaults to 0).
* registeredUserIds (Array of Strings, optional): List of User IDs registered for this event.
* imageUrl (String, optional): URL for the event image/banner.
* isCancelled (Boolean, required): Flag indicating if the event is cancelled (defaults to false).
* isDeleted (Boolean, required): Flag indicating if the event has been soft-deleted (defaults to false).
* createdAt (Date/Time, required): Timestamp when the event was created.
* updatedAt (Date/Time, required): Timestamp when the event was last updated.

---

# 8. announcements

Purpose:
Council Announcements

Fields:

* _id
* councilId
* title
* content
* createdBy
* createdAt

---

# 9. notices

Purpose:
Official Academic Notices

Fields:

* _id
* title
* content
* publishedBy
* targetAudience
* attachments
* createdAt

---

# 10. resources

Purpose:
Notes, Assignments, Study Material

Fields:

* _id (String / ObjectId, required): Unique identifier of the resource.
* title (String, required): Title of the academic resource.
* description (String, optional): Detailed description of the resource.
* uploaderId (String, required): ID of the user who uploaded the resource. (Indexed)
* councilId (String, optional): ID of the council associated with this resource. (Indexed)
* communityId (String, optional): ID of the community associated with this resource. (Indexed)
* tags (Array of Strings, optional): List of tags/keywords for searching and categorizing.
* fileName (String, required): Unique stored filename on the physical storage system (UUID-based).
* originalFileName (String, required): Original filename uploaded by the client.
* fileType (String, required): MIME type of the file.
* fileSize (Int64/Long, required): Size of the file in bytes.
* downloadUrl (String, required): Relative API download path (`/api/resources/download/{resourceId}`).
* isDeleted (Boolean, required): Soft delete flag (defaults to false).
* createdAt (Date/Time, required): Timestamp when the resource was created.
* updatedAt (Date/Time, required): Timestamp when the resource was last updated.

Note:
* Physical files are stored separately on the local filesystem (in `uploads/resources` directory during the MVP via LocalStorageService) rather than inside MongoDB (GridFS or cloud storage is not implemented in the current MVP phase).

---

# 11. resource_requests (Planned / Future Phase)

Purpose:
Faculty Controlled Resource Access

Fields:

* _id
* resourceId
* studentId
* status
* requestedAt
* reviewedAt
* reviewedBy

Status Values:

* PENDING
* APPROVED
* REJECTED

---

# 12. notifications

Purpose:
System Notifications

Fields:

* _id
* userId
* title
* message
* notificationType
* isRead
* createdAt

---

# 13. vault_files

Purpose:
Personal Document Vault

Fields:

* _id
* ownerId
* fileName
* fileUrl
* fileSize
* uploadedAt

---

# 14. roadmaps

Purpose:
Career and Academic Roadmaps (Academic path mapping per degree/department)

Fields:

* _id (String, required): Unique identifier of the roadmap.
* title (String, required): Title of the roadmap.
* description (String, optional): Description of the roadmap.
* degreeProgram (String, required): Target degree program. (Indexed)
* department (String, required): Department hosting the program. (Indexed)
* totalCredits (Integer, required): Total credits required to complete the roadmap.
* expectedGraduationYear (Integer, required): Target graduation year.
* createdBy (String, required): User ID of the roadmap creator. (Indexed)
* isDeleted (Boolean, required): Flag indicating if the roadmap has been soft-deleted (defaults to false).
* createdAt (Date/Time, required): Timestamp when the roadmap was created.
* updatedAt (Date/Time, required): Timestamp when the roadmap was last updated.

Indexes:
* createdBy
* degreeProgram
* department

---

# 15. courses

Purpose:
Mandatory and elective course catalog management.

Fields:

* _id (String, required): Unique identifier of the course.
* courseCode (String, required): Unique code of the course (e.g. CS101). (Unique Index)
* courseName (String, required): Name of the course.
* description (String, optional): Course details and syllabus outline.
* department (String, required): Department offering the course. (Indexed)
* credits (Integer, required): Academic credit value of the course.
* semester (Integer, required): Target semester number in which the course is offered. (Indexed)
* prerequisiteCourseIds (Array of Strings, optional): IDs of prerequisite courses that must be completed first.
* elective (Boolean, required): Flag indicating if the course is an elective (true) or mandatory (false).
* active (Boolean, required): Flag indicating if the course is active/enabled in the catalog.
* createdAt (Date/Time, required): Timestamp when the course was created.
* updatedAt (Date/Time, required): Timestamp when the course was last updated.

Indexes:
* unique courseCode
* department
* semester

---

# 16. student_progress

Purpose:
Tracks completed courses, credits earned, and GPA for individual students.

Fields:

* _id (String, required): Unique identifier of the progress record.
* studentId (String, required): User ID of the student. (Unique Index)
* roadmapId (String, required): Associated academic roadmap ID. (Indexed)
* completedCourseIds (Array of Strings, optional): List of Course IDs that the student has marked completed.
* currentSemester (Integer, required): Current semester level of the student.
* totalCreditsEarned (Integer, required): Cumulative credits earned by completing courses.
* currentGpa (Double, required): Cumulative GPA of the student (0.0 to 10.0 scale).
* graduationEligible (Boolean, required): Evaluated flag indicating if graduation criteria are met.
* createdAt (Date/Time, required): Timestamp when the progress record was created.
* updatedAt (Date/Time, required): Timestamp when the progress record was last updated.

Indexes:
* unique studentId
* roadmapId

---

# 17. semester_plans

Purpose:
Tracks student planned courses and credits for individual semesters.

Fields:

* _id (String, required): Unique identifier of the semester plan.
* studentId (String, required): User ID of the student. (Indexed)
* roadmapId (String, required): Associated academic roadmap ID. (Indexed)
* semesterNumber (Integer, required): The target semester number. (Indexed)
* plannedCourseIds (Array of Strings, optional): List of Course IDs planned for this semester.
* totalPlannedCredits (Integer, required): Total credits of all planned courses.
* finalized (Boolean, required): Flag indicating if the plan is finalized (read-only).
* createdAt (Date/Time, required): Timestamp when the semester plan was created.
* updatedAt (Date/Time, required): Timestamp when the semester plan was last updated.

Indexes:
* studentId
* roadmapId
* semesterNumber

---

# 18. resumes

Purpose:
Resume Builder

Fields:

* _id
* userId
* resumeJson
* generatedPdfUrl
* updatedAt

---

# 19. notification_preferences

Purpose:
User Notification Settings

Fields:

* _id
* userId
* eventNotifications
* announcementNotifications
* resourceNotifications
* communityNotifications

---

# 20. conversations

Purpose:
AI Assistant conversations configuration and metadata

Fields:

* _id
* userId (indexed)
* title
* type (ConversationType)
* metadata (Map<String, Object>)
* status (ConversationStatus: ACTIVE, ARCHIVED, DELETED)
* createdAt
* updatedAt

---

# 21. messages

Purpose:
AI Assistant message logs for conversations

Fields:

* _id
* conversationId (indexed)
* role (MessageRole)
* content
* metadata (Map<String, Object>)
* timestamp

---


# Note on Academic Dashboard
The **Academic Dashboard** does not persist any data. It dynamically aggregates metrics, statistics, and course status listings by query-combining information from existing collections (`student_progress`, `semester_plans`, `courses`, and `roadmaps`).

---

# Future Integrations

AWS S3

* resources.fileName / resources.downloadUrl
* vault_files.fileUrl
* resumes.generatedPdfUrl

Firebase Cloud Messaging

* notifications
* notification_preferences

AI Roadmaps (Planned / Future Phase)

* roadmaps

Premium Subscription System

* users.isPremium

---

# Total Collections

1. users
2. councils
3. membership_applications
4. communities
5. posts
6. comments
7. events
8. announcements
9. notices
10. resources
11. resource_requests
12. notifications
13. vault_files
14. roadmaps
15. courses
16. student_progress
17. semester_plans
18. resumes
19. notification_preferences
20. conversations
21. messages

