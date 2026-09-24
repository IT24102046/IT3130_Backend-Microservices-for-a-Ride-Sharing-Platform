package com.ridelink.driver_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LocationUpdateRequest {

    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double latitude;

    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double longitude;
}
