package com.ridelink.driver_service.dto;

import com.ridelink.driver_service.model.DriverStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DriverResponse {
    private String id;
    private String accountId;
    private String fullName;
    private String email;
    private String phone;
    private String licenseNumber;
    private DriverStatus status;
    private List<String> serviceAreas;
    private Double currentLat;
    private Double currentLng;
    private Double rating;
    private Integer totalRides;
    private LocalDateTime createdAt;
}