package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record FareEstimateResponse(
        @Schema(example = "10.0") BigDecimal distanceKm,
        @Schema(example = "200.00") BigDecimal baseFare,
        @Schema(example = "75.00") BigDecimal ratePerKm,
        @Schema(example = "950.00") BigDecimal estimatedFare,
        @Schema(example = "LKR") String currency
) {
}