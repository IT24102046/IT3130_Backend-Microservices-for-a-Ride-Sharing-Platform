package com.ridelink.ride.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignDriverDto {
    
    @NotBlank(message = "Driver ID is required")
    private String driverId;
}