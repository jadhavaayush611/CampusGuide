# CampusGuide API Contracts (MVP)

## Authentication

POST /api/auth/register

POST /api/auth/login

GET /api/auth/me

PUT /api/auth/profile

---

## Users

GET /api/users/{id}

PUT /api/users/{id}

---

## Councils

GET /api/councils

GET /api/councils/{id}

POST /api/councils

PUT /api/councils/{id}

DELETE /api/councils/{id}

---

## Membership Applications

POST /api/memberships/apply

GET /api/memberships

PUT /api/memberships/{id}/approve

PUT /api/memberships/{id}/reject

---

## Communities

GET /api/communities

GET /api/communities/{id}

POST /api/communities

---

## Posts

POST /api/posts

GET /api/posts/{id}

PUT /api/posts/{id}

DELETE /api/posts/{id}

---

## Comments

POST /api/comments

PUT /api/comments/{id}

DELETE /api/comments/{id}

---

## Events

GET /api/events

GET /api/events/{id}

POST /api/events

PUT /api/events/{id}

DELETE /api/events/{id}

---

## Event Registrations

POST /api/events/{id}/register

GET /api/events/{id}/participants

---

## Event Results

POST /api/events/{id}/results

GET /api/events/{id}/results

---

## Resources

GET /api/resources

GET /api/resources/{id}

POST /api/resources

DELETE /api/resources/{id}

---

## Resource Requests

POST /api/resources/{id}/request

PUT /api/resource-requests/{id}/approve

PUT /api/resource-requests/{id}/reject

---

## Announcements

GET /api/announcements

POST /api/announcements

---

## Notices

GET /api/notices

POST /api/notices

---

## Notifications

GET /api/notifications

PUT /api/notifications/{id}/read

---

## Vault

GET /api/vault

POST /api/vault/upload

DELETE /api/vault/{id}

---

## Roadmaps

GET /api/roadmaps

GET /api/roadmaps/{id}

---

## Resume Builder

POST /api/resume

GET /api/resume

GET /api/resume/pdf
