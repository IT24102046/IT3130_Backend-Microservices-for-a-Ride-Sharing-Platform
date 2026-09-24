package com.ridelink.driver_service.controller;

import com.ridelink.driver_service.dto.*;
import com.ridelink.driver_service.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<ApiResponse<DriverResponse>> registerDriver(
            @Valid @RequestBody DriverRegistrationRequest request) {
        DriverResponse driver = driverService.registerDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Driver registered", driver));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(
            ApiResponse.success("Driver fetched", driverService.getDriverById(id)));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<DriverResponse>> getByAccountId(
            @PathVariable String accountId) {
        return ResponseEntity.ok(
            ApiResponse.success("Driver fetched",
                driverService.getDriverByAccountId(accountId)));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<DriverResponse>> updateAvailability(
            @PathVariable String id,
            @Valid @RequestBody AvailabilityUpdateRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success("Availability updated",
                driverService.updateAvailability(id, request)));
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<ApiResponse<DriverResponse>> updateLocation(
            @PathVariable String id,
            @Valid @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success("Location updated",
                driverService.updateLocation(id, request)));
    }

    @PatchMapping("/{id}/service-areas")
    public ResponseEntity<ApiResponse<DriverResponse>> updateServiceAreas(
            @PathVariable String id,
            @RequestBody List<String> areas) {
        return ResponseEntity.ok(
            ApiResponse.success("Service areas updated",
                driverService.updateServiceAreas(id, areas)));
    }

    @GetMapping("/eligible")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getEligibleDrivers(
            @RequestParam(required = false) String area) {
        List<DriverResponse> drivers = driverService.getEligibleDrivers(area);
        return ResponseEntity.ok(
            ApiResponse.success("Eligible drivers fetched", drivers));
    }
}
