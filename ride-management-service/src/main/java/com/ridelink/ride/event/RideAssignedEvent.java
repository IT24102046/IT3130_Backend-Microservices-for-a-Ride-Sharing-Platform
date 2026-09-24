package com.ridelink.ride.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideAssignedEvent {
    private String rideId;
    private String driverId;
    private String passengerId;
    private LocalDateTime assignedAt;
}