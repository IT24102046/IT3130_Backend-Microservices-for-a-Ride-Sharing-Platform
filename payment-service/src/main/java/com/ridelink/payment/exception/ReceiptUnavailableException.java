package com.ridelink.payment.exception;

import com.ridelink.payment.model.PaymentStatus;

import java.util.UUID;

public class ReceiptUnavailableException extends RuntimeException {

    public ReceiptUnavailableException(UUID paymentId, PaymentStatus status) {
        super("Receipt is unavailable for payment " + paymentId + " with status " + status);
    }
}
