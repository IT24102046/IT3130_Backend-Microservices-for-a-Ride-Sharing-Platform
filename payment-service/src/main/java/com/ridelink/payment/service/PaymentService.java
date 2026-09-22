package com.ridelink.payment.service;

import com.ridelink.payment.dto.CreatePaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.exception.DuplicatePaymentException;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.repository.PaymentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private static final String CURRENCY = "LKR";
    private static final String TRANSACTION_REFERENCE_PREFIX = "PAY-";

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        if (paymentRepository.existsByRideId(request.rideId())) {
            throw new DuplicatePaymentException(request.rideId());
        }

        Payment payment = new Payment(
                UUID.randomUUID(),
                request.rideId(),
                request.passengerId(),
                request.amount().setScale(2, RoundingMode.HALF_UP),
                CURRENCY,
                request.paymentMethod(),
                PaymentStatus.PENDING,
                TRANSACTION_REFERENCE_PREFIX + UUID.randomUUID().toString().toUpperCase(),
                Instant.now(),
                null
        );

        try {
            return toResponse(paymentRepository.save(payment));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicatePaymentException(request.rideId());
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByRideId(String rideId) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for ride: " + rideId));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getRideId(),
                payment.getPassengerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionReference(),
                payment.getCreatedAt(),
                payment.getPaidAt()
        );
    }
}
