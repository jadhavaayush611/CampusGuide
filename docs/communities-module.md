# Communities, Posts & Comments Module

The **Communities** module implements discussion forums, posts, and nested comments to drive student interaction.

---

## 1. Domain Overview

The discussion system is represented by three collections in MongoDB:
1. **Communities (`communities` collection)**: Topic-based or organization-based groups (e.g. "Computer Science Society"). Each community can optionally be linked to a student council.
2. **Posts (`posts` collection)**: User-generated discussion threads within a community. Supports markdown text, optional image URLs, pinning, and soft deletion.
3. **Comments (`comments` collection)**: Nested text replies associated with a post. Supports editing and soft deletion.

---

## 2. Authorization Rules

* **Any Authenticated User**:
  - Can browse communities, posts, and comments.
  - Can create posts and comments.
  - Can edit and delete their **own** posts and comments.
* **Council Admin / Moderator**:
  - Can moderate communities (updating banner, details).
  - Can moderate/delete any post or comment within their assigned community.
* **Super Admin**:
  - Global moderate authority across all communities, posts, and comments.

---

## 3. Implemented REST Endpoints

### 3.1 Communities
* **GET `/api/communities`**: List active communities.
* **GET `/api/communities/{id}`**: Get specific community details.
* **POST `/api/communities`**: Create community (requires authenticated user).
* **PUT `/api/communities/{id}`**: Update community banner or description (requires owner/admin).
* **GET `/api/communities/councils/{councilId}/communities`**: Get communities linked to a specific council.

### 3.2 Posts
* **POST `/api/posts`**: Create a new post.
* **GET `/api/posts`**: List all posts.
* **GET `/api/posts/{id}`**: Get detailed post with metadata.
* **PUT `/api/posts/{id}`**: Update post title/content (owner only).
* **DELETE `/api/posts/{id}`**: Soft delete post (owner or admin).
* **GET `/api/posts/community/{communityId}`**: List posts in a specific community.
* **GET `/api/posts/author/{authorId}`**: List posts authored by a specific user.

### 3.3 Comments
* **POST `/api/comments`**: Create comment under a post.
* **GET `/api/comments/{id}`**: Get specific comment.
* **PUT `/api/comments/{id}`**: Update comment content (owner only).
* **DELETE `/api/comments/{id}`**: Soft delete comment (owner or admin).
* **GET `/api/comments/post/{postId}`**: List comments for a specific post.
* **GET `/api/comments/author/{authorId}`**: List comments authored by a specific user.
