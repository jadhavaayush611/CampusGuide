# Atlas AI Gateway API Specifications

## Base URL
`/api/v1/ai`

---

## 1. Create Conversation
- **Endpoint**: `POST /api/v1/ai/conversations`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Request Body**: `CreateConversationRequest`
  ```json
  {
    "title": "Course Planning Assistance",
    "context": {
      "semester": 4,
      "department": "Computer Science"
    }
  }
  ```
- **Response**: `201 Created` (`ConversationResponse`)

---

## 2. Get User Conversations
- **Endpoint**: `GET /api/v1/ai/conversations`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`List<ConversationSummaryResponse>`)

---

## 3. Get Conversation Messages
- **Endpoint**: `GET /api/v1/ai/conversations/{conversationId}/messages`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`List<MessageResponse>`)

---

## 4. Send Message to AI Assistant
- **Endpoint**: `POST /api/v1/ai/conversations/{conversationId}/messages`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Request Body**: `SendMessageRequest`
  ```json
  {
    "content": "What courses should I take next semester for AI specialization?"
  }
  ```
- **Response**: `200 OK` (`MessageResponse`)
- **Note**: The Atlas AI Gateway processes prompt intent, executes read-only domain queries, and returns synthesized recommendations.

---

## 5. Direct Query / Single-Turn Chat
- **Endpoint**: `POST /api/v1/ai/query`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Request Body**: `AtlasQueryRequest`
- **Response**: `200 OK` (`AtlasQueryResponse`)
