# Communities Module Architecture

## 1. Purpose
The Communities module drives student engagement by providing interest-based groups, clubs, discussion forums, and collaborative spaces. Note that Communities are separate structural concepts from Councils.

---

## 2. Responsibilities
- Support community creation, categories (e.g., Coding, Robotics, Arts, Sports), and membership rosters.
- Host community-specific discussion forums, posts, and nested comments.
- Facilitate peer-to-peer student networking based on shared interests.

---

## 3. Entities
- `Community`: Stores community details, category tags, banner media, and member count.
- `CommunityMember`: Connects a student to a community with a specific role (`MEMBER`, `MODERATOR`, `LEAD`).
- `Post`: Forum discussion topic created within a community.
- `Comment`: User response associated with a discussion post.

---

## 4. Services
- `CommunityService`: Handles community creation, membership joins/leaves, and settings.
- `PostService`: Manages post publishing, editing, and comment threads.

---

## 5. APIs
- `GET /api/communities`: List or search student communities.
- `POST /api/communities`: Create a new interest community.
- `POST /api/communities/{id}/join`: Join or leave a community.
- `GET /api/communities/{id}/posts`: Fetch community discussion feed.

---

## 6. Future Improvements
- Media attachment uploads for posts and comments.
- Real-time websocket chat channels for community members.
- Automated moderation algorithms for post safety.

---

## Cross-References
- [Campus Domain Architecture](file:///D:/CampusGuide/docs/architecture/domain-architecture.md)
- [Campus API Framework](file:///D:/CampusGuide/docs/api/campus.md)
