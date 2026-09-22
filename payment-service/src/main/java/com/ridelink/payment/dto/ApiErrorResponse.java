package com.ridelink.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "Structured API error response")
public record ApiErrorResponse(
        @Schema(example = "2026-09-22T18:30:00+05:30") OffsetDateTime timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Validation failed") String message,
        @Schema(example = "/api/fares/estimate") String path,
        @Schema(example = "{\"distanceKm\": \"Distance must be greater than 0\"}") Map<String, String> fieldErrors
) {
}
