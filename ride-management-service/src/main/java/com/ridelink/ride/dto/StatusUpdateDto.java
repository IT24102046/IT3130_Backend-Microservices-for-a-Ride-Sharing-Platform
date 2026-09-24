package com.ridelink.ride.dto;

import com.ridelink.ride.model.RideStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateDto {
    
    @NotNull(message = "Status is required")
    private RideStatus status;
    
    private String cancellationReason;
}