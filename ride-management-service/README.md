# Ride Management Service

Ride Management Service owns ride requests, lifecycle transitions, driver assignment references, and ride retrieval for the RideLink assignment.

## Responsibility

This service manages:

- ride creation
- pickup and destination details
- driver assignment
- acceptance, start, completion, and cancellation
- ride retrieval by id, passenger, and driver

## Tech Stack

- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Spring Validation
- MySQL 8
- Springdoc OpenAPI

## Run

Set database and dependent-service environment variables if needed:

- `DB_USERNAME`
- `DB_PASSWORD`
- `DRIVER_SERVICE_BASE_URL`
- `FARE_PAYMENT_SERVICE_BASE_URL`

Then run:

```bash
./mvnw spring-boot:run
```

The service listens on port `8080`.

## Test

```bash
./mvnw clean test
```

## API Endpoints

- `POST /api/rides`
- `GET /api/rides/{id}`
- `GET /api/rides/passenger/{passengerId}`
- `GET /api/rides/driver/{driverId}`
- `PUT /api/rides/{id}/assign`
- `PUT /api/rides/{id}/accept`
- `PUT /api/rides/{id}/start`
- `PUT /api/rides/{id}/complete`
- `PUT /api/rides/{id}/cancel`

Swagger UI is available at `/swagger-ui.html`.

## Lifecycle

Valid transitions:

- `REQUESTED -> ASSIGNED`
- `REQUESTED -> CANCELLED`
- `ASSIGNED -> ACCEPTED`
- `ASSIGNED -> CANCELLED`
- `ACCEPTED -> IN_PROGRESS`
- `ACCEPTED -> CANCELLED`
- `IN_PROGRESS -> COMPLETED`
- `IN_PROGRESS -> CANCELLED`

No transitions are allowed from `COMPLETED` or `CANCELLED`.

## Interservice Dependencies

- Driver & Vehicle Service at `DRIVER_SERVICE_BASE_URL`
- Fare & Payment Service at `FARE_PAYMENT_SERVICE_BASE_URL`

Driver assignment queries eligible drivers from the Driver & Vehicle Service, and ride completion forwards ride details to the Fare & Payment Service.

## Negative Scenarios

- No eligible driver available returns a conflict response.
- Invalid ride status transitions return a conflict response.
- Unauthorized driver actions return a forbidden response.
- Missing rides return a not-found response.

## Notes

The service does not access other microservices' databases directly. External contracts are isolated behind client interfaces.