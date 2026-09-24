package com.ridelink.driver_service.dto;

import com.ridelink.driver_service.model.VehicleType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VehicleRegistrationRequest {

    @NotBlank
    private String driverId;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{2,3}-?[0-9]{4}$",
             message = "Invalid plate (e.g., CAB-1234)")
    private String plateNumber;

    @NotBlank
    private String make;

    @NotBlank
    private String model;

    @NotNull @Min(1990) @Max(2030)
    private Integer year;

    private String color;

    @NotNull
    private VehicleType type;

    @NotNull @Min(1) @Max(20)
    private Integer seatCapacity;
}
