package com.ridelink.payment.controller;

import com.ridelink.payment.dto.ApiErrorResponse;
import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Receipts", description = "Generate and retrieve receipts for successful simulated payments")
public class ReceiptController {

    private static final String RECEIPT_EXAMPLE = """
            {
              "receiptId": "e956bdda-7aa8-4ab2-80f4-e3e178803adb",
              "receiptNumber": "RCP-20260922-7A24C930",
              "paymentId": "c4577047-8374-4a43-9e68-b1f198776f3f",
              "rideId": "ride-7f3a",
              "passengerId": "passenger-42",
              "transactionReference": "PAY-5CC9E7F2-35E7-457A-9740-4924190F94CA",
              "amount": 950.00,
              "currency": "LKR",
              "paymentMethod": "CARD",
              "paidAt": "2026-09-22T13:35:00Z",
              "issuedAt": "2026-09-22T13:36:00Z"
            }
            """;

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping(value = "/api/payments/{paymentId}/receipt", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Generate a receipt",
            description = "Generates a receipt snapshot for a successful simulated payment, or returns its existing receipt."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Receipt generated or existing receipt returned",
                    content = @Content(
                            schema = @Schema(implementation = ReceiptResponse.class),
                            examples = @ExampleObject(value = RECEIPT_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Payment is pending or failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ReceiptResponse generateReceipt(@PathVariable UUID paymentId) {
        return receiptService.generateReceipt(paymentId);
    }

    @GetMapping(value = "/api/payments/{paymentId}/receipt", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get a receipt by payment ID",
            description = "Retrieves the receipt associated with a successful simulated payment."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Receipt found",
                    content = @Content(
                            schema = @Schema(implementation = ReceiptResponse.class),
                            examples = @ExampleObject(value = RECEIPT_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Receipt not found for the payment",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ReceiptResponse getReceiptByPaymentId(@PathVariable UUID paymentId) {
        return receiptService.getReceiptByPaymentId(paymentId);
    }

    @GetMapping(value = "/api/receipts/{receiptNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get a receipt by receipt number",
            description = "Retrieves a receipt directly by its generated human-readable receipt number."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Receipt found",
                    content = @Content(
                            schema = @Schema(implementation = ReceiptResponse.class),
                            examples = @ExampleObject(value = RECEIPT_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Receipt not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ReceiptResponse getReceiptByNumber(@PathVariable String receiptNumber) {
        return receiptService.getReceiptByNumber(receiptNumber);
    }
}
