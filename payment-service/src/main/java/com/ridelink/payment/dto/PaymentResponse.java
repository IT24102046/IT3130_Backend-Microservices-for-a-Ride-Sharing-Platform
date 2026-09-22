package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        @Schema(example = "c4577047-8374-4a43-9e68-b1f198776f3f") UUID id,
        @Schema(example = "ride-7f3a") String rideId,
        @Schema(example = "passenger-42") String passengerId,
        @Schema(example = "950.00") BigDecimal amount,
        @Schema(example = "LKR") String currency,
        @Schema(example = "CARD") PaymentMethod paymentMethod,
        @Schema(example = "PENDING") PaymentStatus status,
        @Schema(example = "PAY-5CC9E7F2-35E7-457A-9740-4924190F94CA") String transactionReference,
        @Schema(example = "2026-09-22T13:30:00Z") Instant createdAt,
        @Schema(nullable = true, example = "null") Instant paidAt
) {
}
