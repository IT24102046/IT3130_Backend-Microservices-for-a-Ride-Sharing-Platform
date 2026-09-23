package com.ridelink.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "receipt_number", nullable = false, unique = true, updatable = false, length = 30)
    private String receiptNumber;

    @Column(name = "payment_id", nullable = false, unique = true, updatable = false)
    private UUID paymentId;

    @Column(name = "ride_id", nullable = false, updatable = false, length = 100)
    private String rideId;

    @Column(name = "passenger_id", nullable = false, updatable = false, length = 100)
    private String passengerId;

    @Column(name = "transaction_reference", nullable = false, updatable = false, length = 50)
    private String transactionReference;

    @Column(nullable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, updatable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "paid_at", nullable = false, updatable = false)
    private Instant paidAt;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    protected Receipt() {
    }

    public Receipt(
            UUID id,
            String receiptNumber,
            UUID paymentId,
            String rideId,
            String passengerId,
            String transactionReference,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            Instant paidAt,
            Instant issuedAt
    ) {
        this.id = id;
        this.receiptNumber = receiptNumber;
        this.paymentId = paymentId;
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.transactionReference = transactionReference;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
        this.issuedAt = issuedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getRideId() {
        return rideId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
