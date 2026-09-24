package com.ridelink.driver_service.controller;

import com.ridelink.driver_service.dto.ApiResponse;
import com.ridelink.driver_service.dto.VehicleRegistrationRequest;
import com.ridelink.driver_service.dto.VehicleResponse;
import com.ridelink.driver_service.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> registerVehicle(
            @Valid @RequestBody VehicleRegistrationRequest request) {
        VehicleResponse vehicle = vehicleService.registerVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Vehicle registered", vehicle));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getByDriverId(
            @PathVariable String driverId) {
        return ResponseEntity.ok(
            ApiResponse.success("Vehicle fetched",
                vehicleService.getVehicleByDriverId(driverId)));
    }
}