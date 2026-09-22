package com.ridelink.payment.controller;

import com.ridelink.payment.dto.CreatePaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.exception.DuplicatePaymentException;
import com.ridelink.payment.exception.GlobalExceptionHandler;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    void createsPendingPaymentForValidRequest() throws Exception {
        when(paymentService.createPayment(any(CreatePaymentRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rideId": "ride-7f3a",
                                  "passengerId": "passenger-42",
                                  "amount": 950.00,
                                  "paymentMethod": "CARD"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rideId").value("ride-7f3a"))
                .andExpect(jsonPath("$.passengerId").value("passenger-42"))
                .andExpect(jsonPath("$.amount").value(950.00))
                .andExpect(jsonPath("$.currency").value("LKR"))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.transactionReference").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.paidAt").doesNotExist());
    }

    @Test
    void rejectsInvalidAmount() throws Exception {
        assertInvalidRequest(validJson().replace("950.00", "0"), "amount", "Amount must be greater than 0");
    }

    @Test
    void rejectsMissingRideId() throws Exception {
        assertInvalidRequest("{\"passengerId\":\"passenger-42\",\"amount\":950.00,\"paymentMethod\":\"CARD\"}",
                "rideId", "Ride ID is required");
    }

    @Test
    void rejectsMissingPassengerId() throws Exception {
        assertInvalidRequest("{\"rideId\":\"ride-7f3a\",\"amount\":950.00,\"paymentMethod\":\"CARD\"}",
                "passengerId", "Passenger ID is required");
    }

    @Test
    void rejectsMissingPaymentMethod() throws Exception {
        assertInvalidRequest("{\"rideId\":\"ride-7f3a\",\"passengerId\":\"passenger-42\",\"amount\":950.00}",
                "paymentMethod", "Payment method is required");
    }

    @Test
    void returnsConflictForDuplicateRidePayment() throws Exception {
        when(paymentService.createPayment(any(CreatePaymentRequest.class)))
                .thenThrow(new DuplicatePaymentException("ride-7f3a"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A payment already exists for ride ride-7f3a"))
                .andExpect(jsonPath("$.path").value("/api/payments"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void returnsNotFoundForUnknownPaymentId() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(paymentService.getPayment(paymentId))
                .thenThrow(new PaymentNotFoundException("Payment not found: " + paymentId));

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Payment not found: " + paymentId))
                .andExpect(jsonPath("$.path").value("/api/payments/" + paymentId))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void returnsNotFoundForUnknownRideId() throws Exception {
        when(paymentService.getPaymentByRideId("unknown-ride"))
                .thenThrow(new PaymentNotFoundException("Payment not found for ride: unknown-ride"));

        mockMvc.perform(get("/api/payments/ride/{rideId}", "unknown-ride"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Payment not found for ride: unknown-ride"))
                .andExpect(jsonPath("$.path").value("/api/payments/ride/unknown-ride"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    private void assertInvalidRequest(String body, String field, String fieldMessage) throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/payments"))
                .andExpect(jsonPath("$.fieldErrors." + field).value(fieldMessage));
    }

    private String validJson() {
        return """
                {
                  "rideId": "ride-7f3a",
                  "passengerId": "passenger-42",
                  "amount": 950.00,
                  "paymentMethod": "CARD"
                }
                """;
    }

    private PaymentResponse response() {
        return new PaymentResponse(
                UUID.fromString("c4577047-8374-4a43-9e68-b1f198776f3f"),
                "ride-7f3a",
                "passenger-42",
                new BigDecimal("950.00"),
                "LKR",
                PaymentMethod.CARD,
                PaymentStatus.PENDING,
                "PAY-5CC9E7F2-35E7-457A-9740-4924190F94CA",
                Instant.parse("2026-09-22T13:30:00Z"),
                null
        );
    }
}
