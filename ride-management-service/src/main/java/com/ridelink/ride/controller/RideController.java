package com.ridelink.ride.controller;

import com.ridelink.ride.dto.*;
import com.ridelink.ride.exception.UnauthorizedException;
import com.ridelink.ride.security.UserPrincipal;
import com.ridelink.ride.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
@Tag(name = "Ride Management", description = "Ride booking and lifecycle APIs")
@SecurityRequirement(name = "bearerAuth")
public class RideController {
    
    private final RideService rideService;
    
    // ============ Passenger Endpoints ============
    
    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Request a new ride")
    public ResponseEntity<RideResponseDto> requestRide(
            @Valid @RequestBody RideRequestDto request,
            Authentication auth) {
        String passengerId = extractUserId(auth);
        RideResponseDto ride = rideService.requestRide(request, passengerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ride);
    }
    
    @GetMapping("/available-drivers")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Find available drivers in a service area")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers(
            @RequestParam(required = false) String serviceArea) {
        return ResponseEntity.ok(rideService.findAvailableDrivers(serviceArea));
    }
    
    @PostMapping("/{rideId}/assign")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Assign a driver to a ride")
    public ResponseEntity<RideResponseDto> assignDriver(
            @PathVariable String rideId,
            @Valid @RequestBody AssignDriverDto request,
            Authentication auth) {
        String passengerId = extractUserId(auth);
        return ResponseEntity.ok(rideService.assignDriver(rideId, request, passengerId));
    }
    
    @GetMapping("/my-rides")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Get all rides for the current passenger")
    public ResponseEntity<List<RideResponseDto>> getMyRides(Authentication auth) {
        return ResponseEntity.ok(rideService.getPassengerRides(extractUserId(auth)));
    }
    
    // ============ Driver Endpoints ============
    
    @PostMapping("/{rideId}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver accepts an assigned ride")
    public ResponseEntity<RideResponseDto> acceptRide(
            @PathVariable String rideId,
            Authentication auth) {
        return ResponseEntity.ok(rideService.acceptRide(rideId, extractUserId(auth)));
    }
    
    @PostMapping("/{rideId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver starts the ride")
    public ResponseEntity<RideResponseDto> startRide(
            @PathVariable String rideId,
            Authentication auth) {
        return ResponseEntity.ok(rideService.startRide(rideId, extractUserId(auth)));
    }
    
    @PostMapping("/{rideId}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver completes the ride")
    public ResponseEntity<RideResponseDto> completeRide(
            @PathVariable String rideId,
            Authentication auth) {
        return ResponseEntity.ok(rideService.completeRide(rideId, extractUserId(auth)));
    }
    
    @GetMapping("/driver/my-rides")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Get all rides for the current driver")
    public ResponseEntity<List<RideResponseDto>> getDriverRides(Authentication auth) {
        return ResponseEntity.ok(rideService.getDriverRides(extractUserId(auth)));
    }
    
    // ============ Shared Endpoints ============
    
    @GetMapping("/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    @Operation(summary = "Get ride details by ID")
    public ResponseEntity<RideResponseDto> getRide(
            @PathVariable String rideId,
            Authentication auth) {
        String userId = extractUserId(auth);
        String role = extractRole(auth);
        return ResponseEntity.ok(rideService.getRideById(rideId, userId, role));
    }
    
    @PostMapping("/{rideId}/cancel")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER')")
    @Operation(summary = "Cancel a ride")
    public ResponseEntity<RideResponseDto> cancelRide(
            @PathVariable String rideId,
            @RequestParam(required = false) String reason,
            Authentication auth) {
        String userId = extractUserId(auth);
        return ResponseEntity.ok(rideService.cancelRide(rideId, userId, reason));
    }
    
    // ============ Helper Methods ============
    
    private String extractUserId(Authentication auth) {
        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        throw new UnauthorizedException("Invalid authentication");
    }
    
    private String extractRole(Authentication auth) {
        return auth.getAuthorities().stream()
            .findFirst()
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .orElse("UNKNOWN");
    }
}