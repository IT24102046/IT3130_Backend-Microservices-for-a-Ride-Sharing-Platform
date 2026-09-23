package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReceiptResponse(
        @Schema(example = "e956bdda-7aa8-4ab2-80f4-e3e178803adb") UUID receiptId,
        @Schema(example = "RCP-20260922-7A24C930") String receiptNumber,
        @Schema(example = "c4577047-8374-4a43-9e68-b1f198776f3f") UUID paymentId,
        @Schema(example = "ride-7f3a") String rideId,
        @Schema(example = "passenger-42") String passengerId,
        @Schema(example = "PAY-5CC9E7F2-35E7-457A-9740-4924190F94CA") String transactionReference,
        @Schema(example = "950.00") BigDecimal amount,
        @Schema(example = "LKR") String currency,
        @Schema(example = "CARD") PaymentMethod paymentMethod,
        @Schema(example = "2026-09-22T13:35:00Z") Instant paidAt,
        @Schema(example = "2026-09-22T13:36:00Z") Instant issuedAt
) {
}
