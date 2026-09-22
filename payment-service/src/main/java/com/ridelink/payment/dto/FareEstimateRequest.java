package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FareEstimateRequest(
        @Schema(description = "Estimated ride distance in kilometres", example = "10.0", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Distance is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Distance must be greater than 0")
        BigDecimal distanceKm
) {
}
