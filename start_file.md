# Quick Start Guide

## 1. Database Setup (1 minute)

```sql
CREATE DATABASE interview_scheduler;
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=your_password
```

## 2. Build and Run (30 seconds)

```bash
mvn clean install
mvn spring-boot:run
```

## 3. Access the Application

**UI:** http://localhost:8080

**API Base URL:** http://localhost:8080/api/v1

## 4. Quick Test Flow

### Step 1: Create Interviewer Availability

```bash
curl -X POST http://localhost:8080/api/v1/interviewers/availability \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rajesh Kumar",
    "email": "rajesh@example.com",
    "maxInterviewsPerWeek": 5,
    "availabilitySlots": [
      {
        "dayOfWeek": "MONDAY",
        "startTime": "09:00:00",
        "endTime": "17:00:00",
        "slotDurationMinutes": 60
      },
      {
        "dayOfWeek": "TUESDAY",
        "startTime": "09:00:00",
        "endTime": "17:00:00",
        "slotDurationMinutes": 60
      }
    ]
  }'
```

This automatically generates time slots for the next 2 weeks!

### Step 2: View Available Slots

Open browser: http://localhost:8080

Or use API:
```bash
curl http://localhost:8080/api/v1/time-slots/available?pageSize=20
```

### Step 3: Book a Slot

Via UI: Select a slot and click "Book Selected Slot"

Or via API:
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "timeSlotId": 1,
    "candidateName": "Priya Patel",
    "candidateEmail": "priya@example.com"
  }'
```

## 5. Run Tests

```bash
mvn test
```

## That's It! 🎉

The system is ready to use. Check the full documentation:
- [README.md](README.md) - Complete guide
- [DESIGN_DOCUMENTATION.md](DESIGN_DOCUMENTATION.md) - Technical details

