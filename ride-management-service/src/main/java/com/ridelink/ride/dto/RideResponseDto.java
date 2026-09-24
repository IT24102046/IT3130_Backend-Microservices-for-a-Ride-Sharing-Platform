package com.ridelink.ride.dto;

import com.ridelink.ride.model.FareDetails;
import com.ridelink.ride.model.Location;
import com.ridelink.ride.model.RideStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RideResponseDto {
    private String id;
    private String passengerId;
    private String driverId;
    private Location pickupLocation;
    private Location destinationLocation;
    private RideStatus status;
    private FareDetails fareDetails;
    private LocalDateTime requestedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
}
