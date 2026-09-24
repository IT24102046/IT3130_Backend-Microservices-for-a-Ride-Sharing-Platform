package com.ridelink.ride.dto;

import lombok.Data;

@Data
public class DriverResponse {
    private String id;
    private String accountId;
    private String fullName;
    private String vehicleNumber;
    private String vehicleModel;
    private String serviceArea;
    private double currentLatitude;
    private double currentLongitude;
    private String availabilityStatus;
}