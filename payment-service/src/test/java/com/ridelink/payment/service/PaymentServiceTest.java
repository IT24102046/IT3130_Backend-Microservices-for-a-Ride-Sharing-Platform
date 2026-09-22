package com.ridelink.payment.service;

import com.ridelink.payment.dto.CreatePaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.exception.DuplicatePaymentException;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createsPendingPaymentWithGeneratedValues() {
        CreatePaymentRequest request = request();
        when(paymentRepository.existsByRideId(request.rideId())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.rideId()).isEqualTo("ride-7f3a");
        assertThat(response.passengerId()).isEqualTo("passenger-42");
        assertThat(response.amount()).isEqualByComparingTo("950.00");
        assertThat(response.currency()).isEqualTo("LKR");
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(response.transactionReference()).startsWith("PAY-");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.paidAt()).isNull();

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void rejectsDuplicatePaymentForRide() {
        CreatePaymentRequest request = request();
        when(paymentRepository.existsByRideId(request.rideId())).thenReturn(true);

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(DuplicatePaymentException.class)
                .hasMessageContaining(request.rideId());
    }

    @Test
    void getsPaymentById() {
        Payment payment = payment();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPayment(payment.getId());

        assertThat(response.id()).isEqualTo(payment.getId());
        assertThat(response.rideId()).isEqualTo(payment.getRideId());
    }

    @Test
    void getsPaymentByRideId() {
        Payment payment = payment();
        when(paymentRepository.findByRideId(payment.getRideId())).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByRideId(payment.getRideId());

        assertThat(response.id()).isEqualTo(payment.getId());
        assertThat(response.rideId()).isEqualTo(payment.getRideId());
    }

    @Test
    void throwsWhenPaymentIdDoesNotExist() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(paymentId))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining(paymentId.toString());
    }

    @Test
    void throwsWhenRidePaymentDoesNotExist() {
        when(paymentRepository.findByRideId("unknown-ride")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByRideId("unknown-ride"))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("unknown-ride");
    }

    private CreatePaymentRequest request() {
        return new CreatePaymentRequest(
                "ride-7f3a",
                "passenger-42",
                new BigDecimal("950.00"),
                PaymentMethod.CARD
        );
    }

    private Payment payment() {
        return new Payment(
                UUID.randomUUID(),
                "ride-7f3a",
                "passenger-42",
                new BigDecimal("950.00"),
                "LKR",
                PaymentMethod.CARD,
                PaymentStatus.PENDING,
                "PAY-" + UUID.randomUUID().toString().toUpperCase(),
                Instant.parse("2026-09-22T13:30:00Z"),
                null
        );
    }
}
