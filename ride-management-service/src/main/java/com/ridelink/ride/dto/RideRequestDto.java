package com.ridelink.ride.dto;

import com.ridelink.ride.model.Location;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RideRequestDto {
    
    @NotNull(message = "Pickup location is required")
    private Location pickupLocation;
    
    @NotNull(message = "Destination location is required")
    private Location destinationLocation;
}
