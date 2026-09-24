package com.ridelink.ride.dto;

import com.ridelink.ride.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateRequest {
    private Location pickupLocation;
    private Location destinationLocation;
}