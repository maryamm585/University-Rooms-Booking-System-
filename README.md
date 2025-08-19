# University-Rooms-Booking-System

A **Spring Boot** based University Room Booking System with role-based access, JWT security, and a complete booking workflow including approval, cancellation, and booking.

## Project Overview

The system allows:  

- **Students** to search room availability, request bookings, view, and cancel their own bookings.  
- **Faculty members** to request bookings for lectures or departmental events and manage their bookings.  
- **Admins** to manage rooms, approve/reject bookings, enforce booking policies, and maintain audit trails.  

------------------------------------------------------------------------------------------------

## Technology Stack

- **Spring Boot 3.5.4**.  
- **Spring Web, Spring Data JPA, Spring Security**.  
- **JWT (JSON Web Tokens)**.  
- **Validation (Jakarta Bean Validation)**.  
- **Lombok**.  
- **MySQL**.  
- **SLF4J + Logback**.  
- **JUnit 5 + Mockito**.
- **H2 Database** for integration testing.  

------------------------------------------------------------------------------------------------

## Setup & Configuration

1. Clone the repository:
  
- git clone <repo-url>

2. Configure application.yml/.properties with environment-specific profiles:
 
- application-dev.yml
- application-prod.yml

3. Create a MySQL database and configure connection in your profile files.

4. Run the project.

------------------------------------------------------------------------------------------------

## Domain Model

### Entities include:

- User → student, faculty, admin.
- Role → STUDENT, FACULTY, ADMIN.
- Building.
- Room.
- RoomFeature.
- Booking.
- BookingHistory.
- Holiday.
- Department.

### Relationships:

`@OneToMany`, `@ManyToOne`, `@ManyToMany`, `@JoinTable` used for ORM mapping.

### Enums:

- BookingStatus → APPROVED, PENDING, CANCELLED, REJECTED.

------------------------------------------------------------------------------------------------

## Validation

- DTO-level: `@NotNull`, `@NotBlank`, `@Future`.

------------------------------------------------------------------------------------------------

## Service Layer

- Interfaces + implementations.
- Business logic for:
  - Booking creation, approval, rejection.
  - Overlap checks.
  - Cancellation rules.
- Swappable strategies with `@Primary` / `@Qualifier`.

------------------------------------------------------------------------------------------------

## Web Layer (REST API)

- Controllers with `@RestController`, `@RequestMapping`.
- Endpoints for authentication, bookings, rooms, features.
- DTOs for request/response.

------------------------------------------------------------------------------------------------

## Security & JWT

- JWT authentication & authorization.
- Roles enforced: STUDENT, FACULTY, ADMIN.
- SecurityFilterChain validates JWT and attaches authentication to context.

------------------------------------------------------------------------------------------------

## Exception Handling

- `@ControllerAdvice` + `@ExceptionHandler`.
- Custom exceptions: ResourceNotFoundException, UnauthorizedActionException, BookingConflictException.

------------------------------------------------------------------------------------------------

## Logging

- SLF4J + Logback configured via application.yml/.properties.
- Log levels: INFO, WARN, ERROR.
- Request logging via filter/interceptor for debugging.

## Testing

- Unit Tests:
  - Services (overlap logic, approval/cancellation rules)
  - Validators (`@NoOverlap`)
- Integration Tests:
  - `@WebMvcTest` with MockMvc
  - `@SpringBootTest` with H2 + TestEntityManager
