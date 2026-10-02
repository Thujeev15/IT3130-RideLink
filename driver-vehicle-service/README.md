# RideLink · Driver & Vehicle Service

Owner: **Anthujan** (Member 2)

Owns: driver operational profiles, vehicle details, availability status, simulated
current location / service area, and eligible-driver lookup used by the Ride
Management Service.

## Prerequisites
- Java 21+ (built on Spring Boot 4.1.1 / Spring Framework 7)
- Maven 3.9+
- MySQL 8+ running locally (or update `DB_URL` to point elsewhere)

## Configuration
Copy `.env.example` to `.env` (not committed) and fill in real values, or export
the same variables in your shell. `JWT_SECRET` **must** match the value used by
the Account Service so tokens it issues validate here.

```
cp .env.example .env
```
The datasource URL includes `createDatabaseIfNotExist=true`, so the
`ridelink_driver` schema is created automatically on first run - no manual
`CREATE DATABASE` step needed, just make sure MySQL itself is running.

## Running locally
```
mvn spring-boot:run
```
Service starts on `http://localhost:8082` by default (`SERVER_PORT`).

## API docs
- Swagger UI: `http://localhost:8082/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`

## Running tests
```
mvn test
```
Tests run against the MySQL database configured by the test properties. The CI workflow starts MySQL and creates the required test database.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | /api/drivers | DRIVER/ADMIN | Register driver operational profile |
| GET | /api/drivers/{id} | DRIVER/ADMIN | Get a driver profile |
| GET | /api/drivers | DRIVER/ADMIN | List all drivers |
| PUT | /api/drivers/{id}/vehicle | DRIVER/ADMIN | Add/update vehicle |
| PATCH | /api/drivers/{id}/availability | DRIVER/ADMIN | Change OFFLINE / AVAILABLE / BUSY |
| PATCH | /api/drivers/{id}/location | DRIVER/ADMIN | Update simulated lat/long + service area |
| GET | /api/drivers/eligible?serviceArea= | public (interservice) | Called by Ride Management Service to find an available driver |

## Eligibility rule (documented, simple)
A driver is eligible for assignment when:
1. `availabilityStatus == AVAILABLE`, **and**
2. their `serviceArea` matches the requested pickup area (case-insensitive; omit
   the parameter to search all areas), **and**
3. they have a registered vehicle.

If no drivers match, `GET /api/drivers/eligible` returns `200 OK` with an empty
list — this is the "no available driver" negative scenario used in the demo.

## Interservice contract (for Member 3 - Ride Management Service)
Call `GET http://<driver-service-host>:8082/api/drivers/eligible?serviceArea=Colombo`
to get a list of eligible drivers, each including `id`, `vehicleType`,
`seatCapacity` and current location, so Ride Management can pick one and record
the assignment using this service's driver `id` as the stable identifier —
never query this service's database directly.

## Sample test data
| accountId | licenseNumber | serviceArea |
|---|---|---|
| (any UUID from Account Service) | B1234567 | Colombo |
| (any UUID from Account Service) | B7654321 | Negombo |

## Notes on secrets
No secrets are committed. `JWT_SECRET`, `DB_USERNAME`, `DB_PASSWORD` are read
from environment variables at runtime (see `application.yml`).
