# RideLink Fare & Payment Service

Standalone Spring Boot service for RideLink fare and payment capabilities.

Fare Estimation and Final Fare Calculation are implemented. Payment recording, receipts, and Ride Service integration are not implemented yet.

## Fare Estimation

Estimate a fare with:

```http
POST /api/fares/estimate
Content-Type: application/json
```

The fare rule is deliberately simple:

```text
Estimated Fare = LKR 200.00 + (distanceKm x LKR 75.00)
```

Money is calculated with `BigDecimal` and rounded to two decimal places using `HALF_UP` rounding.

Example request:

```json
{
  "distanceKm": 10.0
}
```

Example response:

```json
{
  "distanceKm": 10.0,
  "baseFare": 200.00,
  "ratePerKm": 75.00,
  "estimatedFare": 950.00,
  "currency": "LKR"
}
```

`distanceKm` is required and must be greater than zero. Invalid input returns HTTP `400` with a structured response containing the request path and field-level validation messages.

## Final Fare Calculation

Calculate the final fare for a completed ride with:

```http
POST /api/fares/final
Content-Type: application/json
```

Until Ride Service integration is available, the endpoint accepts the ride ID, passenger ID, and completed distance directly.

The final fare uses the same rule as the estimate:

```text
Final Fare = LKR 200.00 + (distanceKm x LKR 75.00)
```

Example request:

```json
{
  "rideId": "ride-7f3a",
  "passengerId": "passenger-42",
  "distanceKm": 10.0
}
```

Example response:

```json
{
  "rideId": "ride-7f3a",
  "passengerId": "passenger-42",
  "distanceKm": 10.0,
  "baseFare": 200.00,
  "ratePerKm": 75.00,
  "finalFare": 950.00,
  "currency": "LKR"
}
```

`rideId` and `passengerId` are required and must not be blank. `distanceKm` is required and must be greater than zero. Validation failures return the same structured HTTP `400` response used by Fare Estimation.

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

The service runs on port `8084`. Swagger UI is available at:

```text
http://localhost:8084/swagger-ui.html
```

## Build

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```
