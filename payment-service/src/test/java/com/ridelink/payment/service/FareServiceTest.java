package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FareServiceTest {

    private final FareService fareService = new FareService();

    @Test
    void estimatesFareForTenKilometres() {
        FareEstimateResponse response = fareService.estimateFare(new BigDecimal("10.0"));

        assertThat(response.estimatedFare()).isEqualByComparingTo("950.00");
        assertThat(response.baseFare()).isEqualByComparingTo("200.00");
        assertThat(response.ratePerKm()).isEqualByComparingTo("75.00");
        assertThat(response.currency()).isEqualTo("LKR");
    }

    @Test
    void estimatesFareForDecimalDistance() {
        FareEstimateResponse response = fareService.estimateFare(new BigDecimal("12.5"));

        assertThat(response.estimatedFare()).isEqualByComparingTo("1137.50");
    }

    @Test
    void roundsCalculatedFareToTwoDecimalPlaces() {
        FareEstimateResponse response = fareService.estimateFare(new BigDecimal("0.333"));

        assertThat(response.estimatedFare()).isEqualByComparingTo("224.98");
        assertThat(response.estimatedFare().scale()).isEqualTo(2);
    }

    @Test
    void calculatesFinalFareForTenKilometres() {
        FinalFareResponse response = fareService.calculateFinalFare(
                "ride-7f3a",
                "passenger-42",
                new BigDecimal("10.0")
        );

        assertThat(response.rideId()).isEqualTo("ride-7f3a");
        assertThat(response.passengerId()).isEqualTo("passenger-42");
        assertThat(response.finalFare()).isEqualByComparingTo("950.00");
        assertThat(response.currency()).isEqualTo("LKR");
    }

    @Test
    void calculatesFinalFareForDecimalDistance() {
        FinalFareResponse response = fareService.calculateFinalFare(
                "ride-7f3a",
                "passenger-42",
                new BigDecimal("12.5")
        );

        assertThat(response.finalFare()).isEqualByComparingTo("1137.50");
    }

    @Test
    void roundsFinalFareToTwoDecimalPlaces() {
        FinalFareResponse response = fareService.calculateFinalFare(
                "ride-7f3a",
                "passenger-42",
                new BigDecimal("0.333")
        );

        assertThat(response.finalFare()).isEqualByComparingTo("224.98");
        assertThat(response.finalFare().scale()).isEqualTo(2);
    }
}
