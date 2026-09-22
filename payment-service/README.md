# RideLink Fare & Payment Service

Standalone Spring Boot service for RideLink fare and payment capabilities.

Fare Estimation, Final Fare Calculation, payment persistence/retrieval, and simulated payment processing are implemented. Receipts and Ride Service integration are not implemented yet. No real payment gateway is used.

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

## Payment Persistence

The Payment Service owns payment records in the `ridelink_payment` database. A payment contains:

- An internal UUID
- External ride and passenger IDs
- Amount and `LKR` currency
- Payment method (`CARD`, `CASH`, or `WALLET`)
- Status (`PENDING`, `SUCCESS`, or `FAILED`)
- A generated transaction reference
- Creation and paid timestamps

Creating a record sets its status to `PENDING` and leaves `paidAt` as `null`.

### Create a payment

```http
POST /api/payments
Content-Type: application/json
```

Example request:

```json
{
  "rideId": "ride-7f3a",
  "passengerId": "passenger-42",
  "amount": 950.00,
  "paymentMethod": "CARD"
}
```

Example `201 Created` response:

```json
{
  "id": "c4577047-8374-4a43-9e68-b1f198776f3f",
  "rideId": "ride-7f3a",
  "passengerId": "passenger-42",
  "amount": 950.00,
  "currency": "LKR",
  "paymentMethod": "CARD",
  "status": "PENDING",
  "transactionReference": "PAY-5CC9E7F2-35E7-457A-9740-4924190F94CA",
  "createdAt": "2026-09-22T13:30:00Z",
  "paidAt": null
}
```

The client supplies only the ride ID, passenger ID, amount, and payment method. The service controls the payment ID, currency, status, transaction reference, and timestamps.

Only one payment record is allowed per ride. Creating another payment for the same `rideId` returns HTTP `409 Conflict`.

### Retrieve payments

Retrieve a payment by its internal UUID:

```http
GET /api/payments/{paymentId}
```

Retrieve a payment by its external ride ID:

```http
GET /api/payments/ride/{rideId}
```

Both retrieval endpoints return HTTP `404 Not Found` when no matching payment exists.

### Simulate payment processing

Process an existing pending payment with:

```http
POST /api/payments/{paymentId}/process
Content-Type: application/json
```

Successful simulated outcome:

```json
{
  "result": "SUCCESS"
}
```

This changes the status from `PENDING` to `SUCCESS` and sets `paidAt` to the current timestamp. The response retains the original payment ID, ride ID, passenger ID, amount, currency, payment method, transaction reference, and creation time.

Example response fields:

```json
{
  "status": "SUCCESS",
  "paidAt": "2026-09-22T13:35:00Z"
}
```

Failed simulated outcome:

```json
{
  "result": "FAILED"
}
```

This changes the status from `PENDING` to `FAILED` and leaves `paidAt` as `null`.

Valid transitions are:

```text
PENDING -> SUCCESS
PENDING -> FAILED
```

`SUCCESS` and `FAILED` are final states. Attempting to process either state again returns HTTP `409 Conflict`. An unknown payment returns HTTP `404 Not Found`. A missing or unsupported result returns HTTP `400 Bad Request`.

This endpoint only simulates an academic payment outcome. It does not contact a bank, card processor, wallet provider, or other real payment gateway.

### Payment validation

- `rideId` is required and must not be blank.
- `passengerId` is required and must not be blank.
- `amount` is required and must be greater than zero.
- `paymentMethod` is required and must be `CARD`, `CASH`, or `WALLET`.

Validation, not-found, and duplicate errors use the service's structured API error response.

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
