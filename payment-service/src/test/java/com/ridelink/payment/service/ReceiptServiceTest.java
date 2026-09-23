package com.ridelink.payment.service;

import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.exception.ReceiptNotFoundException;
import com.ridelink.payment.exception.ReceiptUnavailableException;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.model.Receipt;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.repository.ReceiptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @InjectMocks
    private ReceiptService receiptService;

    @Test
    void generatesReceiptSnapshotForSuccessfulPayment() {
        Payment payment = payment(PaymentStatus.SUCCESS);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(receiptRepository.findByPaymentId(payment.getId())).thenReturn(Optional.empty());
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReceiptResponse response = receiptService.generateReceipt(payment.getId());

        assertThat(response.receiptId()).isNotNull();
        assertThat(response.receiptNumber()).matches("RCP-\\d{8}-[A-F0-9]{8}");
        assertThat(response.paymentId()).isEqualTo(payment.getId());
        assertThat(response.amount()).isEqualByComparingTo("950.00");
        assertThat(response.transactionReference()).isEqualTo(payment.getTransactionReference());
        assertThat(response.paidAt()).isEqualTo(payment.getPaidAt());
        assertThat(response.issuedAt()).isNotNull();
    }

    @Test
    void returnsExistingReceiptWithoutCreatingDuplicate() {
        Payment payment = payment(PaymentStatus.SUCCESS);
        Receipt existingReceipt = receipt(payment);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(receiptRepository.findByPaymentId(payment.getId())).thenReturn(Optional.of(existingReceipt));

        ReceiptResponse response = receiptService.generateReceipt(payment.getId());

        assertThat(response.receiptId()).isEqualTo(existingReceipt.getId());
        assertThat(response.receiptNumber()).isEqualTo(existingReceipt.getReceiptNumber());
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    void rejectsPendingPayment() {
        Payment payment = payment(PaymentStatus.PENDING);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> receiptService.generateReceipt(payment.getId()))
                .isInstanceOf(ReceiptUnavailableException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void rejectsFailedPayment() {
        Payment payment = payment(PaymentStatus.FAILED);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> receiptService.generateReceipt(payment.getId()))
                .isInstanceOf(ReceiptUnavailableException.class)
                .hasMessageContaining("FAILED");
    }

    @Test
    void throwsWhenPaymentDoesNotExist() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptService.generateReceipt(paymentId))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining(paymentId.toString());
    }

    @Test
    void retrievesReceiptByPaymentId() {
        Payment payment = payment(PaymentStatus.SUCCESS);
        Receipt receipt = receipt(payment);
        when(receiptRepository.findByPaymentId(payment.getId())).thenReturn(Optional.of(receipt));

        ReceiptResponse response = receiptService.getReceiptByPaymentId(payment.getId());

        assertThat(response.receiptId()).isEqualTo(receipt.getId());
        assertThat(response.paymentId()).isEqualTo(payment.getId());
    }

    @Test
    void throwsWhenReceiptNumberDoesNotExist() {
        when(receiptRepository.findByReceiptNumber("RCP-UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptService.getReceiptByNumber("RCP-UNKNOWN"))
                .isInstanceOf(ReceiptNotFoundException.class)
                .hasMessageContaining("RCP-UNKNOWN");
    }

    private Payment payment(PaymentStatus status) {
        Instant paidAt = status == PaymentStatus.SUCCESS ? Instant.parse("2026-09-22T13:35:00Z") : null;
        return new Payment(
                UUID.randomUUID(),
                "ride-7f3a",
                "passenger-42",
                new BigDecimal("950.00"),
                "LKR",
                PaymentMethod.CARD,
                status,
                "PAY-5CC9E7F2-35E7-457A-9740-4924190F94CA",
                Instant.parse("2026-09-22T13:30:00Z"),
                paidAt
        );
    }

    private Receipt receipt(Payment payment) {
        return new Receipt(
                UUID.randomUUID(),
                "RCP-20260922-7A24C930",
                payment.getId(),
                payment.getRideId(),
                payment.getPassengerId(),
                payment.getTransactionReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getPaidAt(),
                Instant.parse("2026-09-22T13:36:00Z")
        );
    }
}
