package com.ridelink.driver_service.dto;

import com.ridelink.driver_service.model.VehicleType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VehicleResponse {
    private String id;
    private String driverId;
    private String plateNumber;
    private String make;
    private String model;
    private Integer year;
    private String color;
    private VehicleType type;
    private Integer seatCapacity;
    private Boolean active;
    private LocalDateTime createdAt;
}
