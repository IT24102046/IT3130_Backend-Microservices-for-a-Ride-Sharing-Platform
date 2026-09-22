package com.ridelink.payment.exception;

import com.ridelink.payment.model.PaymentStatus;

import java.util.UUID;

public class InvalidPaymentStateException extends RuntimeException {

    public InvalidPaymentStateException(UUID paymentId, PaymentStatus currentStatus) {
        super("Payment " + paymentId + " cannot be processed from status " + currentStatus);
    }
}
