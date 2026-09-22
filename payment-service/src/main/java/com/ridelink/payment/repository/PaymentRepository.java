package com.ridelink.payment.repository;

import com.ridelink.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByRideId(String rideId);

    boolean existsByRideId(String rideId);
}
