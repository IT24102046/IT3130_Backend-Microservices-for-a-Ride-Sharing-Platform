package com.ridelink.ride.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FareEstimateResponse {
    private BigDecimal estimatedFare;
    private BigDecimal distanceKm;
    private String currency;
}