# Events & Event Registrations Module

The **Events** module facilitates scheduling campus events, workshops, hackathons, and handling participant registrations.

---

## 1. Domain Overview

The event catalog consists of events organized by student councils:
* **Events (`events` collection)**: Details event title, description, timings, registration deadline, organizer reference, location, max participants limit, attendee count, registration status, and soft delete flags.
* **Organizer Ownership**: Any authenticated user who creates an event is designated the `organizerId` (owner) of that event.

---

## 2. Authorization Rules

* **General Authenticated Users**:
  - Can view events, register for upcoming events (before deadline and capacity limits), and cancel their registration.
  - Can view the attendee IDs list for any event.
  - Can create new events (becoming the organizer).
* **Organizer (Owner)**:
  - Can update or cancel/soft-delete their own events.
* **Super Admin**:
  - Global override access to update/cancel/delete any event in the system.

---

## 3. Implemented REST Endpoints

### 3.1 Events Management
* **POST `/api/events`**: Create a new event.
* **GET `/api/events`**: Retrieve all active (non-deleted) events.
* **GET `/api/events/upcoming`**: Retrieve active and non-cancelled events starting in the future.
* **GET `/api/events/{eventId}`**: View details of a specific event.
* **GET `/api/events/council/{councilId}`**: View events organized by a specific council.
* **PUT `/api/events/{eventId}`**: Update event details (Organizer or `SUPER_ADMIN`).
* **DELETE `/api/events/{eventId}`**: Soft delete an event (Organizer or `SUPER_ADMIN`).

### 3.2 Event Registrations
* **POST `/api/events/{eventId}/register`**: Register authenticated user for an event (checks capacity limits, past times, and deadlines).
* **DELETE `/api/events/{eventId}/register`**: Cancel registration.
* **GET `/api/events/{eventId}/registration-status`**: Check if authenticated user is registered (returns `{ "registered": true/false }`).
* **GET `/api/events/{eventId}/registrations`**: Retrieve user IDs list of all registered attendees.
