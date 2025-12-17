# Automatic Interview Scheduling System

A comprehensive Spring Boot application for managing interview scheduling with automatic slot generation, candidate booking, and availability management.

----
## Features

- ✅ Interviewer availability management
- ✅ Automatic time slot generation for next 2 weeks
- ✅ Candidate slot booking and updates
- ✅ Maximum interviews per week enforcement
- ✅ Cursor-based pagination for efficient slot browsing
- ✅ Optimistic locking for race condition handling
- ✅ Clean Architecture implementation
- ✅ Comprehensive error handling
- ✅ Modern UI with debouncing
- ✅ JUnit test coverage

- ----
## Technology Stack

- **Backend:** Java 17, Spring Boot 3.1.0
- **Database:** MySQL 8.0
- **ORM:** JPA/Hibernate
- **Build Tool:** Maven
- **Frontend:** HTML5, CSS3, JavaScript (Vanilla JS)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Setup Instructions

### 1. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE interview_scheduler;
```

### 2. Configuration

Update `src/main/resources/application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/interview_scheduler
spring.datasource.username= please enter your username
spring.datasource.password= please enter your password
```

### 3. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access the UI

Open your browser and navigate to:
```
http://localhost:8080
```

## API Endpoints

### Interviewer Management

#### Create/Update Interviewer Availability
```
POST /api/v1/interviewers/availability
Content-Type: application/json

{
  "name": "prakhar Kumar",
  "email": "prakhar@example.com",
  "maxInterviewsPerWeek": 5,
  "availabilitySlots": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "09:00:00",
      "endTime": "17:00:00",
      "slotDurationMinutes": 60
    }
  ]
}
```

#### Get Interviewer by ID
```
GET /api/v1/interviewers/{id}
```

#### Get Interviewer by Email
```
GET /api/v1/interviewers/email/{email}
```

### Time Slots

#### Get Available Slots (with cursor pagination)
```
GET /api/v1/time-slots/available?cursor={cursor}&pageSize=20
```

### Bookings

#### Book a Slot
```
POST /api/v1/bookings
Content-Type: application/json

{
  "timeSlotId": 1,
  "candidateName": "akshat srivastava",
  "candidateEmail": "akshu@example.com"
}
```

#### Update Booking
```
PUT /api/v1/bookings
Content-Type: application/json

{
  "bookingId": 1,
  "newTimeSlotId": 3,
  "candidateName": "Jane Smith",
  "candidateEmail": "jane@example.com"
}
```

#### Get Booking by ID
```
GET /api/v1/bookings/{bookingId}
```

For detailed API documentation, see [DESIGN_DOCUMENTATION.md](DESIGN_DOCUMENTATION.md)

## Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── domain/
│   │   │   ├── entity/          # JPA entities
│   │   │   └── repository/      # JPA repositories
│   │   ├── service/             # Business logic
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── exception/           # Exception handling
│   │   └── util/                # Utility classes
│   └── resources/
│       ├── application.properties
│       └── static/
│           └── index.html       # UI
└── test/
    └── java/com/example/demo/
        ├── service/             # Service tests
        └── controller/          # Controller tests
```

## Key Design Decisions

### Clean Architecture
The project follows Clean Architecture principles with clear separation of concerns:
- **Domain Layer**: Entities and repositories
- **Service Layer**: Business logic
- **Controller Layer**: API endpoints
- **DTO Layer**: Data transfer objects

### Race Condition Handling
- Uses **Optimistic Locking** with JPA `@Version`
- Transaction isolation level: `REPEATABLE_READ`
- Prevents double bookings and ensures data consistency

### Pagination
- **Cursor-based pagination** instead of offset-based
- Better performance for large datasets
- Consistent results even with concurrent modifications

### Error Handling
- Centralized exception handling with `GlobalExceptionHandler`
- Consistent error response format
- Proper HTTP status codes

## Testing

Run tests with:
```bash
mvn test
```

Test coverage includes:
- Service layer unit tests
- Controller integration tests
- Business logic validation
- Error scenario handling

## Documentation

Comprehensive documentation available in:
- [DESIGN_DOCUMENTATION.md](DESIGN_DOCUMENTATION.md) - Complete design documentation with:
  - Architecture overview
  - Database schema
  - API documentation
  - Flow diagrams
  - Error handling strategies
  - Race condition handling
  - Design patterns
  - Trade-off discussions

## Usage Example

### 1. Set Up Interviewer Availability

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

This will:
- Create/update the interviewer
- Generate time slots for the next 2 weeks based on availability

### 2. View Available Slots

```bash
curl http://localhost:8080/api/v1/time-slots/available?pageSize=20
```

### 3. Book a Slot

```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "timeSlotId": 1,
    "candidateName": "Priya Patel",
    "candidateEmail": "priya@example.com"
  }'
```

## UI Features

The web UI (`http://localhost:8080`) includes:
- **Debounced Search**: Search slots with 500ms debounce delay
- **Cursor Pagination**: Navigate through available slots efficiently
- **Real-time Booking**: Book slots directly from the UI
- **Error Handling**: User-friendly error messages
- **Responsive Design**: Works on desktop and mobile

## Design Patterns Used

1. **Repository Pattern**: Abstract data access
2. **Service Layer Pattern**: Business logic encapsulation
3. **DTO Pattern**: Data transfer between layers
4. **Builder Pattern**: Object construction (via Lombok)
5. **Exception Handling Pattern**: Centralized error handling

## Performance Optimizations

- Database indexes for efficient queries
- Cursor-based pagination for large datasets
- Optimistic locking (better for read-heavy workloads)
- Pre-generated time slots (faster queries)

## Security Considerations

For production deployment, consider:
- Authentication and authorization
- Input validation and sanitization
- SQL injection prevention (JPA handles this)
- Rate limiting
- HTTPS enforcement
- CORS configuration


