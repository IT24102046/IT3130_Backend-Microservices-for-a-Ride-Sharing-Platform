package com.ridelink.payment.service;

import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.exception.ReceiptNotFoundException;
import com.ridelink.payment.exception.ReceiptUnavailableException;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.model.Receipt;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.repository.ReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ReceiptService {

    private static final DateTimeFormatter RECEIPT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;

    public ReceiptService(PaymentRepository paymentRepository, ReceiptRepository receiptRepository) {
        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
    }

    @Transactional
    public ReceiptResponse generateReceipt(UUID paymentId) {
        Payment payment = findPayment(paymentId);
        verifyReceiptAvailable(payment);

        return receiptRepository.findByPaymentId(paymentId)
                .map(this::toResponse)
                .orElseGet(() -> createReceipt(payment));
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByPaymentId(UUID paymentId) {
        Receipt receipt = receiptRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ReceiptNotFoundException("Receipt not found for payment: " + paymentId));
        return toResponse(receipt);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByNumber(String receiptNumber) {
        Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ReceiptNotFoundException("Receipt not found: " + receiptNumber));
        return toResponse(receipt);
    }

    private Payment findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
    }

    private void verifyReceiptAvailable(Payment payment) {
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new ReceiptUnavailableException(payment.getId(), payment.getStatus());
        }
    }

    private ReceiptResponse createReceipt(Payment payment) {
        Instant issuedAt = Instant.now();
        UUID receiptId = UUID.randomUUID();
        String uniqueSuffix = receiptId.toString().substring(0, 8).toUpperCase();
        String receiptNumber = "RCP-" + RECEIPT_DATE_FORMAT.format(issuedAt) + "-" + uniqueSuffix;

        Receipt receipt = new Receipt(
                receiptId,
                receiptNumber,
                payment.getId(),
                payment.getRideId(),
                payment.getPassengerId(),
                payment.getTransactionReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getPaidAt(),
                issuedAt
        );

        return toResponse(receiptRepository.save(receipt));
    }

    private ReceiptResponse toResponse(Receipt receipt) {
        return new ReceiptResponse(
                receipt.getId(),
                receipt.getReceiptNumber(),
                receipt.getPaymentId(),
                receipt.getRideId(),
                receipt.getPassengerId(),
                receipt.getTransactionReference(),
                receipt.getAmount(),
                receipt.getCurrency(),
                receipt.getPaymentMethod(),
                receipt.getPaidAt(),
                receipt.getIssuedAt()
        );
    }
}
