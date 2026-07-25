# Resources Module Architecture

## 1. Purpose
The Resources module functions as a centralized academic study repository, facilitating the storage, sharing, indexing, and voting of study materials, lecture notes, syllabus guides, and past exam papers.

---

## 2. Responsibilities
- Allow students and faculty to upload and categorize academic study materials.
- Associate resources with specific courses, departments, and topic tags.
- Provide community upvoting and quality ratings for uploaded materials.
- Prevent duplicate resource creation across student groups.

---

## 3. Entities
- `Resource`: Stores resource title, description, course mapping, file URL / S3 key, uploader reference, upvote count, and tag list.
- `ResourceVote`: Tracks student upvote/downvote actions on resources to prevent multi-voting.

---

## 4. Services
- `ResourceService`: Coordinates file upload storage, metadata indexing, search query matching, and vote tally updates.

---

## 5. APIs
- `GET /api/resources`: Query study resources filtered by course, department, or search query.
- `POST /api/resources`: Upload and index a new study resource.
- `POST /api/resources/{id}/vote`: Cast an upvote or downvote on a study resource.

---

## 6. Future Improvements
- AI-driven resource summarization via Atlas AI Gateway integration.
- Full-text optical character recognition (OCR) for handwritten uploaded PDF notes.

---

## Cross-References
- [Campus Domain Architecture](file:///D:/CampusGuide/docs/architecture/domain-architecture.md)
- [Campus API Framework](file:///D:/CampusGuide/docs/api/campus.md)
