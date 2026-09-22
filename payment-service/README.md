# RideLink Fare & Payment Service

Standalone Spring Boot service for RideLink fare and payment capabilities. Domain features are intentionally not implemented yet.

## Requirements

- Java 17
- Maven 3.6.3 or later
- MySQL for local development/runtime

## Configuration

The datasource is configured with environment variables. Do not commit credentials.

| Variable | Purpose | Development default |
| --- | --- | --- |
| `DB_URL` | JDBC URL for the service-owned MySQL database | `jdbc:mysql://localhost:3306/ridelink_payment` |
| `DB_USERNAME` | Database username | None |
| `DB_PASSWORD` | Database password | None |

The service runs on port `8084`. Swagger UI is available at `/swagger-ui.html` when the application is running.

## Build

```bash
mvn test
mvn package
```