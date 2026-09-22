package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @Schema(example = "ride-7f3a", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Ride ID is required")
        String rideId,

        @Schema(example = "passenger-42", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Passenger ID is required")
        String passengerId,

        @Schema(example = "950.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
        BigDecimal amount,

        @Schema(example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}
