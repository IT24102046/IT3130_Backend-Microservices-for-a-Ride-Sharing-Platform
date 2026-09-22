package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareEstimateResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareService {

    /**
     * Provisional assignment fare rule: LKR 200.00 plus LKR 75.00 for each kilometre.
     * These constants can later move to external configuration without changing the calculation contract.
     */
    public static final BigDecimal BASE_FARE = new BigDecimal("200.00");
    public static final BigDecimal RATE_PER_KM = new BigDecimal("75.00");
    public static final String CURRENCY = "LKR";

    public FareEstimateResponse estimateFare(BigDecimal distanceKm) {
        BigDecimal estimatedFare = BASE_FARE
                .add(distanceKm.multiply(RATE_PER_KM))
                .setScale(2, RoundingMode.HALF_UP);

        return new FareEstimateResponse(
                distanceKm,
                BASE_FARE,
                RATE_PER_KM,
                estimatedFare,
                CURRENCY
        );
    }
}