package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentProcessingResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ProcessPaymentRequest(
        @Schema(
                description = "Simulated processing outcome",
                example = "SUCCESS",
                allowableValues = {"SUCCESS", "FAILED"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Processing result is required")
        PaymentProcessingResult result
) {
}
