package com.ridelink.payment.repository;

import com.ridelink.payment.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {

    Optional<Receipt> findByPaymentId(UUID paymentId);

    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    boolean existsByPaymentId(UUID paymentId);
}
