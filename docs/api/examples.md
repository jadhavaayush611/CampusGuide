# Atlas API Integration Examples

This document provides complete, copy-pasteable integration examples for Atlas API services, including cURL requests, Frontend SDK usage, conversation lifecycle management, workflow execution, and error handling.

---

## 1. Frontend Client SDK Integration

### Installation / Import
```javascript
import { AtlasClient } from './sdk';

const atlas = new AtlasClient({
  baseUrl: 'https://api.campusguide.edu',
  getToken: async () => localStorage.getItem('jwt_token'),
});
```

### Contextual Streaming Chat
```javascript
const stream = atlas.streaming.streamChat(
  {
    conversationId: 'c71a39f0-32b0-4f51-8664-92736bb6a100',
    prompt: 'Show me my degree progress and recommended courses for next semester.',
    model: 'gpt-4o-mini',
  },
  {
    onThinking: (data) => console.log('Thinking:', data.message),
    onReasoning: (data) => console.log('Reasoning:', data.message),
    onPlanning: (data) => console.log('Planning:', data.message),
    onResponseToken: (data) => process.stdout.write(data.token),
    onCompletion: (data) => console.log('\nStream completed. Tokens:', data.usage),
    onError: (err) => console.error('Stream error:', err),
  }
);

// To cancel mid-stream:
// stream.cancel();
```

---

## 2. Conversation Lifecycle Management

### Create Conversation
```javascript
const conv = await atlas.conversations.create({
  title: 'Fall 2026 Academic Planning',
  type: 'ACADEMIC_ADVISOR',
  metadata: { department: 'Computer Science' }
});
```

### Continue Conversation
```javascript
const response = await atlas.conversations.continue(conv.id, {
  prompt: 'Can you list prerequisites for CS401?'
});
```

### Rename, Archive, Restore & Summary
```javascript
// Rename
await atlas.conversations.rename(conv.id, 'Senior Year Course Plan');

// Archive
await atlas.conversations.archive(conv.id);

// Restore
await atlas.conversations.restore(conv.id);

// Get AI Summary
const summary = await atlas.conversations.getSummary(conv.id);
console.log('Summary:', summary.summary);
```

---

## 3. Workflow Execution

### Trigger Workflow Execution
```javascript
const execution = await atlas.workflows.execute({
  workflowId: 'academic_advising_workflow',
  parameters: { targetSemester: 'Fall 2026', creditLimit: 16 },
  async: false,
});
```

### Monitor & Cancel Execution
```javascript
// Query status
const status = await atlas.workflows.getStatus(execution.executionId);

// Cancel if needed
await atlas.workflows.cancel(execution.executionId, 'User navigated away');
```

---

## 4. Raw HTTP / cURL Examples

### Stream Chat Request
```bash
curl -X POST https://api.campusguide.edu/api/v1/atlas/chat/stream \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "prompt": "What building is CS Department located in?",
    "model": "gpt-4o-mini"
  }'
```

### Archive Conversation
```bash
curl -X POST https://api.campusguide.edu/api/v1/atlas/conversations/c71a39f0-32b0-4f51-8664-92736bb6a100/archive \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```
