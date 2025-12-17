# Automatic Interview Scheduling System - Design Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Database Schema](#database-schema)
4. [API Documentation](#api-documentation)
5. [Flow Diagrams](#flow-diagrams)
6. [Error Handling](#error-handling)
7. [Race Condition Handling](#race-condition-handling)
8. [Design Patterns](#design-patterns)
9. [Pagination Strategy](#pagination-strategy)
10. [Trade-offs](#trade-offs)

---

## System Overview

The Automatic Interview Scheduling System allows interviewers to set their weekly availability, which is used to generate time slots for the next two weeks. Candidates can then browse and book available slots, with the system ensuring that maximum interviews per week constraints are respected.

**Key Features:**
- Interviewer availability management
- Automatic time slot generation for 2 weeks
- Candidate slot booking and updates
- Maximum interviews per week enforcement
- Cursor-based pagination
- Optimistic locking for concurrency control

---

## Architecture

The system follows **Clean Architecture** principles with clear layer separation:

```
com.example.demo
├── domain/          # Entities and repositories
├── service/         # Business logic
├── controller/      # REST APIs
├── dto/             # Request/Response DTOs
├── exception/       # Exception handling
└── util/            # Utilities
```

**Design Principles:**
- Separation of Concerns
- Dependency Inversion
- Single Responsibility
- Repository Pattern
- Service Layer Pattern

---

## Database Schema

### Tables

**interviewers**
- `id` (PK), `name`, `email` (unique), `max_interviews_per_week`, `version` (for optimistic locking)

**availability_slots**
- `id` (PK), `interviewer_id` (FK), `day_of_week`, `start_time`, `end_time`, `slot_duration_minutes`

**time_slots**
- `id` (PK), `interviewer_id` (FK), `slot_date_time`, `status` (AVAILABLE/BOOKED/CANCELLED), `version` (for optimistic locking)

**candidate_bookings**
- `id` (PK), `time_slot_id` (FK, unique), `candidate_name`, `candidate_email`, `booking_date_time`, `updated_at`

### Relationships
- Interviewer 1:N AvailabilitySlot
- Interviewer 1:N TimeSlot
- TimeSlot 1:1 CandidateBooking

### Indexes
- `idx_interviewer_date` - For interviewer queries
- `idx_status_date` - For available slot filtering
- `idx_date_cursor` - For cursor pagination

---

## API Documentation

### Base URL: `http://localhost:8080/api/v1`

#### 1. Create/Update Interviewer Availability
```
POST /interviewers/availability
Body: {
  "name": "Rajesh Kumar",
  "email": "rajesh@example.com",
  "maxInterviewsPerWeek": 5,
  "availabilitySlots": [{
    "dayOfWeek": "MONDAY",
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "slotDurationMinutes": 60
  }]
}
```

#### 2. Get Available Time Slots
```
GET /time-slots/available?cursor={cursor}&pageSize=20
Response: {
  "timeSlots": [...],
  "nextCursor": "encoded_cursor",
  "hasNextPage": true,
  "pageSize": 20
}
```

#### 3. Book a Slot
```
POST /bookings
Body: {
  "timeSlotId": 1,
  "candidateName": "Priya Patel",
  "candidateEmail": "priya@example.com"
}
```

#### 4. Update Booking
```
PUT /bookings
Body: {
  "bookingId": 1,
  "newTimeSlotId": 3,
  "candidateName": "Priya Patel",
  "candidateEmail": "priya@example.com"
}
```

#### 5. Get Booking
```
GET /bookings/{bookingId}
```

---

## Flow Diagrams

### Interviewer Availability Setup
```
Interviewer → POST /interviewers/availability → InterviewerService 
→ TimeSlotGenerationService → Generates slots for next 2 weeks → Database
```

### Candidate Booking Flow
```
Candidate → GET /time-slots/available → Browse slots (cursor pagination)
→ POST /bookings → BookingService (with optimistic lock)
→ Validates availability & max interviews → Creates booking → Database
```

### Booking Update Flow
```
Candidate → PUT /bookings → BookingService
→ Releases old slot → Validates new slot → Updates booking → Database
```

---

## Error Handling

**Error Response Format:**
```json
{
  "error": "Error Type",
  "message": "Detailed message",
  "timestamp": "2024-01-15T10:30:00",
  "status": 400
}
```

**Error Types:**
- `404 Not Found` - ResourceNotFoundException (Interviewer, TimeSlot, Booking not found)
- `400 Bad Request` - BusinessException (Slot not available, max interviews exceeded, validation errors)
- `409 Conflict` - ConcurrentModificationException (Optimistic lock failure)
- `500 Internal Server Error` - Generic exceptions

**Strategy:** Centralized exception handling via `GlobalExceptionHandler` with `@RestControllerAdvice`

---

## Race Condition Handling

### Problem
Multiple candidates trying to book the same slot simultaneously can cause double bookings.

### Solution: Optimistic Locking

**Implementation:**
1. `@Version` field in `TimeSlot` and `Interviewer` entities
2. `@Lock(LockModeType.OPTIMISTIC)` on critical queries
3. Transaction isolation: `REPEATABLE_READ`
4. Exception handling for `ObjectOptimisticLockingFailureException`

**How it works:**
- Each entity has a version field that increments on update
- When booking, version is checked
- If version changed (another transaction modified it), `ObjectOptimisticLockingFailureException` is thrown
- Returns 409 Conflict with retry suggestion

**Benefits:**
- Better performance than pessimistic locking (no locks on read)
- Prevents lost updates
- Scalable for high read/write ratios

---

## Design Patterns

1. **Repository Pattern** - Abstract data access (JPA repositories)
2. **Service Layer Pattern** - Encapsulate business logic
3. **DTO Pattern** - Transfer data between layers
4. **Builder Pattern** - Object construction (Lombok)
5. **Exception Handling Pattern** - Centralized error handling

---

## Pagination Strategy

### Cursor-Based Pagination

**Why Cursor over Offset?**
- Better performance (uses index, O(log n) vs O(n))
- Consistent results even with concurrent modifications
- Scalable for large datasets

**Implementation:**
- Cursor encoded as Base64: `{dateTime, id}`
- Query uses: `WHERE (slot_date_time > :cursorDateTime OR (slot_date_time = :cursorDateTime AND id > :cursorId))`
- Index on `(slot_date_time, id)` for performance

**Trade-offs:**
- ✅ Better performance and consistency
- ❌ No direct page number access
- ❌ More complex implementation

---

## Trade-offs

### 1. Optimistic vs Pessimistic Locking
**Chosen: Optimistic**
- Better for read-heavy workloads (many slot views, fewer bookings)
- Higher performance, lower contention
- Requires retry logic on 409 errors

### 2. Cursor vs Offset Pagination
**Chosen: Cursor**
- Better performance for large datasets
- Consistent results
- Trade-off: No page numbers in UI

### 3. Transaction Isolation
**Chosen: REPEATABLE_READ**
- Prevents non-repeatable reads (important for max interviews check)
- Better performance than SERIALIZABLE
- Acceptable phantom read risk

### 4. Pre-generate vs On-demand Slots
**Chosen: Pre-generate (2 weeks ahead)**
- Faster queries (simple SELECT vs complex calculation)
- Trade-off: More storage needed

---

## Conclusion

This system implements a robust interview scheduling solution with Clean Architecture, optimistic locking for race conditions, cursor-based pagination, comprehensive error handling, and proper design patterns. The system is production-ready and scalable.
