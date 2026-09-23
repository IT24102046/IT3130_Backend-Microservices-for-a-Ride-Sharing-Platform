package com.ridelink.payment.controller;

import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.exception.GlobalExceptionHandler;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.exception.ReceiptNotFoundException;
import com.ridelink.payment.exception.ReceiptUnavailableException;
import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.service.ReceiptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceiptController.class)
@Import(GlobalExceptionHandler.class)
class ReceiptControllerTest {

    private static final UUID PAYMENT_ID = UUID.fromString("c4577047-8374-4a43-9e68-b1f198776f3f");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptService receiptService;

    @Test
    void generatesReceiptForSuccessfulPayment() throws Exception {
        when(receiptService.generateReceipt(PAYMENT_ID)).thenReturn(response());

        mockMvc.perform(post("/api/payments/{paymentId}/receipt", PAYMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptNumber").value("RCP-20260922-7A24C930"))
                .andExpect(jsonPath("$.paymentId").value(PAYMENT_ID.toString()))
                .andExpect(jsonPath("$.amount").value(950.00))
                .andExpect(jsonPath("$.transactionReference").value(
                        "PAY-5CC9E7F2-35E7-457A-9740-4924190F94CA"));
    }

    @Test
    void retrievesReceiptByPaymentId() throws Exception {
        when(receiptService.getReceiptByPaymentId(PAYMENT_ID)).thenReturn(response());

        mockMvc.perform(get("/api/payments/{paymentId}/receipt", PAYMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptId").value("e956bdda-7aa8-4ab2-80f4-e3e178803adb"))
                .andExpect(jsonPath("$.paidAt").value("2026-09-22T13:35:00Z"));
    }

    @Test
    void returnsConflictForPendingPayment() throws Exception {
        when(receiptService.generateReceipt(PAYMENT_ID))
                .thenThrow(new ReceiptUnavailableException(PAYMENT_ID, PaymentStatus.PENDING));

        assertReceiptConflict("PENDING");
    }

    @Test
    void returnsConflictForFailedPayment() throws Exception {
        when(receiptService.generateReceipt(PAYMENT_ID))
                .thenThrow(new ReceiptUnavailableException(PAYMENT_ID, PaymentStatus.FAILED));

        assertReceiptConflict("FAILED");
    }

    @Test
    void returnsNotFoundForUnknownPayment() throws Exception {
        when(receiptService.generateReceipt(PAYMENT_ID))
                .thenThrow(new PaymentNotFoundException("Payment not found: " + PAYMENT_ID));

        mockMvc.perform(post("/api/payments/{paymentId}/receipt", PAYMENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Payment not found: " + PAYMENT_ID));
    }

    @Test
    void returnsNotFoundForUnknownReceiptNumber() throws Exception {
        when(receiptService.getReceiptByNumber("RCP-UNKNOWN"))
                .thenThrow(new ReceiptNotFoundException("Receipt not found: RCP-UNKNOWN"));

        mockMvc.perform(get("/api/receipts/{receiptNumber}", "RCP-UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Receipt not found: RCP-UNKNOWN"));
    }

    @Test
    void repeatedGenerationReturnsSameReceipt() throws Exception {
        when(receiptService.generateReceipt(PAYMENT_ID)).thenReturn(response());

        mockMvc.perform(post("/api/payments/{paymentId}/receipt", PAYMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptId").value("e956bdda-7aa8-4ab2-80f4-e3e178803adb"));
        mockMvc.perform(post("/api/payments/{paymentId}/receipt", PAYMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptId").value("e956bdda-7aa8-4ab2-80f4-e3e178803adb"));
    }

    private void assertReceiptConflict(String statusName) throws Exception {
        mockMvc.perform(post("/api/payments/{paymentId}/receipt", PAYMENT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Receipt is unavailable for payment " + PAYMENT_ID + " with status " + statusName))
                .andExpect(jsonPath("$.path").value("/api/payments/" + PAYMENT_ID + "/receipt"));
    }

    private ReceiptResponse response() {
        return new ReceiptResponse(
                UUID.fromString("e956bdda-7aa8-4ab2-80f4-e3e178803adb"),
                "RCP-20260922-7A24C930",
                PAYMENT_ID,
                "ride-7f3a",
                "passenger-42",
                "PAY-5CC9E7F2-35E7-457A-9740-4924190F94CA",
                new BigDecimal("950.00"),
                "LKR",
                PaymentMethod.CARD,
                Instant.parse("2026-09-22T13:35:00Z"),
                Instant.parse("2026-09-22T13:36:00Z")
        );
    }
}
