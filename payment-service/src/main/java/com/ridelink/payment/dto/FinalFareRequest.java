package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FinalFareRequest(
        @Schema(description = "Stable identifier of the completed ride", example = "ride-7f3a", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Ride ID is required")
        String rideId,

        @Schema(description = "Stable identifier of the passenger", example = "passenger-42", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Passenger ID is required")
        String passengerId,

        @Schema(description = "Completed ride distance in kilometres", example = "10.0", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Distance is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Distance must be greater than 0")
        BigDecimal distanceKm
) {
}
