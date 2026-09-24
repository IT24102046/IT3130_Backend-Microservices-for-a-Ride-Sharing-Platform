package com.ridelink.driver_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vehicles")
public class Vehicle {

    @Id
    private String id;

    @Indexed(unique = true)
    private String driverId;

    @Indexed(unique = true)
    private String plateNumber;

    private String make;
    private String model;
    private Integer year;
    private String color;
    private VehicleType type;
    private Integer seatCapacity;
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}