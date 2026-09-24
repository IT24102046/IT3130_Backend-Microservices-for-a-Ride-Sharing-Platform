package com.ridelink.driver_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class DriverRegistrationRequest {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100)
    private String fullName;

    @NotBlank @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotEmpty(message = "At least one service area required")
    private List<String> serviceAreas;
}
