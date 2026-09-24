package com.ridelink.ride.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareDetails {
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
    private BigDecimal distanceKm;
    private String paymentId;
    private String paymentStatus;
}