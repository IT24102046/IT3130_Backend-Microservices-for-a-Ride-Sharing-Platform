package com.ridelink.payment.controller;

import com.ridelink.payment.exception.GlobalExceptionHandler;
import com.ridelink.payment.service.FareService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FareController.class)
@Import({FareService.class, GlobalExceptionHandler.class})
class FareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsFareEstimateForValidRequest() throws Exception {
        mockMvc.perform(post("/api/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distanceKm\":10.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm").value(10.0))
                .andExpect(jsonPath("$.baseFare").value(200.00))
                .andExpect(jsonPath("$.ratePerKm").value(75.00))
                .andExpect(jsonPath("$.estimatedFare").value(950.00))
                .andExpect(jsonPath("$.currency").value("LKR"));
    }

    @Test
    void rejectsZeroDistance() throws Exception {
        assertInvalidDistance("{\"distanceKm\":0}", "Distance must be greater than 0");
    }

    @Test
    void rejectsNegativeDistance() throws Exception {
        assertInvalidDistance("{\"distanceKm\":-2.5}", "Distance must be greater than 0");
    }

    @Test
    void rejectsMissingDistance() throws Exception {
        assertInvalidDistance("{}", "Distance is required");
    }

    @Test
    void rejectsNullDistance() throws Exception {
        assertInvalidDistance("{\"distanceKm\":null}", "Distance is required");
    }

    @Test
    void returnsFinalFareForValidRequest() throws Exception {
        mockMvc.perform(post("/api/fares/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rideId": "ride-7f3a",
                                  "passengerId": "passenger-42",
                                  "distanceKm": 10.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value("ride-7f3a"))
                .andExpect(jsonPath("$.passengerId").value("passenger-42"))
                .andExpect(jsonPath("$.distanceKm").value(10.0))
                .andExpect(jsonPath("$.baseFare").value(200.00))
                .andExpect(jsonPath("$.ratePerKm").value(75.00))
                .andExpect(jsonPath("$.finalFare").value(950.00))
                .andExpect(jsonPath("$.currency").value("LKR"));
    }

    @Test
    void rejectsBlankRideIdForFinalFare() throws Exception {
        assertInvalidFinalFare("{\"rideId\":\"   \",\"passengerId\":\"passenger-42\",\"distanceKm\":10}",
                "rideId", "Ride ID is required");
    }

    @Test
    void rejectsMissingRideIdForFinalFare() throws Exception {
        assertInvalidFinalFare("{\"passengerId\":\"passenger-42\",\"distanceKm\":10}",
                "rideId", "Ride ID is required");
    }

    @Test
    void rejectsBlankPassengerIdForFinalFare() throws Exception {
        assertInvalidFinalFare("{\"rideId\":\"ride-7f3a\",\"passengerId\":\"   \",\"distanceKm\":10}",
                "passengerId", "Passenger ID is required");
    }

    @Test
    void rejectsMissingPassengerIdForFinalFare() throws Exception {
        assertInvalidFinalFare("{\"rideId\":\"ride-7f3a\",\"distanceKm\":10}",
                "passengerId", "Passenger ID is required");
    }

    @Test
    void rejectsZeroDistanceForFinalFare() throws Exception {
        assertInvalidFinalFare("{\"rideId\":\"ride-7f3a\",\"passengerId\":\"passenger-42\",\"distanceKm\":0}",
                "distanceKm", "Distance must be greater than 0");
    }

    @Test
    void rejectsNegativeDistanceForFinalFare() throws Exception {
        assertInvalidFinalFare("{\"rideId\":\"ride-7f3a\",\"passengerId\":\"passenger-42\",\"distanceKm\":-1}",
                "distanceKm", "Distance must be greater than 0");
    }

    @Test
    void rejectsNullDistanceForFinalFare() throws Exception {
        assertInvalidFinalFare("{\"rideId\":\"ride-7f3a\",\"passengerId\":\"passenger-42\",\"distanceKm\":null}",
                "distanceKm", "Distance is required");
    }

    private void assertInvalidDistance(String requestBody, String expectedFieldError) throws Exception {
        mockMvc.perform(post("/api/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/fares/estimate"))
                .andExpect(jsonPath("$.fieldErrors.distanceKm").value(expectedFieldError));
    }

    private void assertInvalidFinalFare(String requestBody, String field, String expectedFieldError) throws Exception {
        mockMvc.perform(post("/api/fares/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/fares/final"))
                .andExpect(jsonPath("$.fieldErrors." + field).value(expectedFieldError));
    }
}
