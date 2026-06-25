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

* _id
* title
* description
* councilId
* eventType
* bannerUrl
* venue
* startDate
* endDate
* registrationDeadline
* maxParticipants
* status
* createdBy
* createdAt
* updatedAt

Status Values:

* UPCOMING
* ONGOING
* COMPLETED
* CANCELLED

---

# 8. event_registrations

Purpose:
Participant Tracking

Fields:

* _id
* eventId
* userId
* registrationStatus
* registeredAt

Registration Status Values:

* REGISTERED
* ATTENDED
* DISQUALIFIED

---

# 9. event_results

Purpose:
Event Result Publishing

Fields:

* _id
* eventId
* position
* userId
* remarks
* publishedAt

---

# 10. announcements

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

# 11. notices

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

# 12. resources

Purpose:
Notes, Assignments, Study Material

Fields:

* _id
* title
* description
* facultyId
* department
* year
* subject
* fileUrl
* resourceType
* visibility
* createdAt

Visibility Values:

* PUBLIC
* APPROVAL_REQUIRED
* PRIVATE_CLASS

---

# 13. resource_requests

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

# 14. notifications

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

# 15. vault_files

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

# 16. roadmaps

Purpose:
Career and Academic Roadmaps

Fields:

* _id
* title
* description
* isPremium
* steps
* createdAt

---

# 17. resumes

Purpose:
Resume Builder

Fields:

* _id
* userId
* resumeJson
* generatedPdfUrl
* updatedAt

---

# 18. notification_preferences

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

# Future Integrations

AWS S3

* resources.fileUrl
* vault_files.fileUrl
* resumes.generatedPdfUrl

Firebase Cloud Messaging

* notifications
* notification_preferences

AI Roadmaps

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
8. event_registrations
9. event_results
10. announcements
11. notices
12. resources
13. resource_requests
14. notifications
15. vault_files
16. roadmaps
17. resumes
18. notification_preferences
