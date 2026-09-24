package com.ridelink.driver_service.dto;

import com.ridelink.driver_service.model.DriverStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvailabilityUpdateRequest {
    @NotNull
    private DriverStatus status;
}