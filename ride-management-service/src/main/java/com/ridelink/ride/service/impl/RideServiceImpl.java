package com.ridelink.ride.service.impl;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.FareServiceClient;
import com.ridelink.ride.dto.*;
import com.ridelink.ride.event.RideEventPublisher;
import com.ridelink.ride.exception.*;
import com.ridelink.ride.model.*;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.service.RideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideServiceImpl implements RideService {
    
    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;
    private final FareServiceClient fareServiceClient;
    private final RideEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public RideResponseDto requestRide(RideRequestDto request, String passengerId) {
        log.info("Passenger {} requesting ride", passengerId);
        
        // Calculate estimated fare via Fare Service
        FareEstimateRequest fareRequest = new FareEstimateRequest(
            request.getPickupLocation(),
            request.getDestinationLocation()
        );
        
        FareEstimateResponse estimate = null;
        try {
            estimate = fareServiceClient.estimateFare(fareRequest);
        } catch (Exception e) {
            log.warn("Fare service unavailable: {}", e.getMessage());
        }
        
        FareDetails fareDetails = FareDetails.builder()
            .estimatedFare(estimate != null ? estimate.getEstimatedFare() : BigDecimal.ZERO)
            .distanceKm(estimate != null ? estimate.getDistanceKm() : BigDecimal.ZERO)
            .build();
        
        Ride ride = Ride.builder()
            .passengerId(passengerId)
            .pickupLocation(request.getPickupLocation())
            .destinationLocation(request.getDestinationLocation())
            .status(RideStatus.REQUESTED)
            .fareDetails(fareDetails)
            .requestedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Ride saved = rideRepository.save(ride);
        log.info("Ride created: {}", saved.getId());
        
        return mapToDto(saved);
    }
    
    @Override
    @Transactional
    public RideResponseDto assignDriver(String rideId, AssignDriverDto request, String passengerId) {
        Ride ride = rideRepository.findByIdAndPassengerId(rideId, passengerId)
            .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidStatusTransitionException(
                "Cannot assign driver. Current status: " + ride.getStatus()
            );
        }
        
        // Verify driver exists
        DriverResponse driver;
        try {
            driver = driverServiceClient.getDriverById(request.getDriverId());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Driver not found: " + request.getDriverId());
        }
        
        ride.setDriverId(request.getDriverId());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(LocalDateTime.now());
        ride.setUpdatedAt(LocalDateTime.now());
        
        Ride saved = rideRepository.save(ride);
        
        // Publish async event
        eventPublisher.publishRideAssigned(saved);
        eventPublisher.publishStatusChanged(saved);
        
        // Notify Driver Service (sync)
        try {
            driverServiceClient.updateDriverStatus(request.getDriverId(), "BUSY");
        } catch (Exception e) {
            log.warn("Could not update driver status: {}", e.getMessage());
        }
        
        log.info("Driver {} assigned to ride {}", request.getDriverId(), rideId);
        return mapToDto(saved);
    }
    
    @Override
    @Transactional
    public RideResponseDto acceptRide(String rideId, String driverId) {
        Ride ride = rideRepository.findByIdAndDriverId(rideId, driverId)
            .orElseThrow(() -> new ResourceNotFoundException("Ride not found for driver"));
        
        validateTransition(ride, RideStatus.ACCEPTED);
        
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(LocalDateTime.now());
        ride.setUpdatedAt(LocalDateTime.now());
        
        Ride saved = rideRepository.save(ride);
        eventPublisher.publishStatusChanged(saved);
        
        return mapToDto(saved);
    }
    
    @Override
    @Transactional
    public RideResponseDto startRide(String rideId, String driverId) {
        Ride ride = rideRepository.findByIdAndDriverId(rideId, driverId)
            .orElseThrow(() -> new ResourceNotFoundException("Ride not found for driver"));
        
        validateTransition(ride, RideStatus.IN_PROGRESS);
        
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        ride.setUpdatedAt(LocalDateTime.now());
        
        Ride saved = rideRepository.save(ride);
        eventPublisher.publishStatusChanged(saved);
        
        return mapToDto(saved);
    }
    
    @Override
    @Transactional
    public RideResponseDto completeRide(String rideId, String driverId) {
        Ride ride = rideRepository.findByIdAndDriverId(rideId, driverId)
            .orElseThrow(() -> new ResourceNotFoundException("Ride not found for driver"));
        
        validateTransition(ride, RideStatus.COMPLETED);
        
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setUpdatedAt(LocalDateTime.now());
        
        // Calculate final fare via Fare Service
        try {
            FareEstimateRequest fareRequest = new FareEstimateRequest(
                ride.getPickupLocation(),
                ride.getDestinationLocation()
            );
            FareEstimateResponse finalFare = fareServiceClient.estimateFare(fareRequest);
            ride.getFareDetails().setFinalFare(finalFare.getEstimatedFare());
            ride.getFareDetails().setDistanceKm(finalFare.getDistanceKm());
        } catch (Exception e) {
            log.warn("Could not calculate final fare: {}", e.getMessage());
            ride.getFareDetails().setFinalFare(ride.getFareDetails().getEstimatedFare());
        }
        
        Ride saved = rideRepository.save(ride);
        eventPublisher.publishStatusChanged(saved);
        
        // Free up driver
        try {
            driverServiceClient.updateDriverStatus(driverId, "AVAILABLE");
        } catch (Exception e) {
            log.warn("Could not update driver status: {}", e.getMessage());
        }
        
        log.info("Ride {} completed", rideId);
        return mapToDto(saved);
    }
    
    @Override
    @Transactional
    public RideResponseDto cancelRide(String rideId, String userId, String reason) {
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        
        // Only passenger or assigned driver can cancel
        boolean isPassenger = userId.equals(ride.getPassengerId());
        boolean isDriver = userId.equals(ride.getDriverId());
        
        if (!isPassenger && !isDriver) {
            throw new UnauthorizedException("You are not authorized to cancel this ride");
        }
        
        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                "Cannot cancel a " + ride.getStatus() + " ride"
            );
        }
        
        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(LocalDateTime.now());
        ride.setCancellationReason(reason);
        ride.setUpdatedAt(LocalDateTime.now());
        
        Ride saved = rideRepository.save(ride);
        eventPublisher.publishStatusChanged(saved);
        
        // Free up driver if assigned
        if (ride.getDriverId() != null) {
            try {
                driverServiceClient.updateDriverStatus(ride.getDriverId(), "AVAILABLE");
            } catch (Exception e) {
                log.warn("Could not update driver status: {}", e.getMessage());
            }
        }
        
        return mapToDto(saved);
    }
    
    @Override
    public RideResponseDto getRideById(String rideId, String userId, String role) {
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        
        if (!"ADMIN".equals(role)
            && !userId.equals(ride.getPassengerId())
            && !userId.equals(ride.getDriverId())) {
            throw new UnauthorizedException("You are not authorized to view this ride");
        }
        
        return mapToDto(ride);
    }
    
    @Override
    public List<RideResponseDto> getPassengerRides(String passengerId) {
        return rideRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId)
            .stream().map(this::mapToDto).toList();
    }
    
    @Override
    public List<RideResponseDto> getDriverRides(String driverId) {
        return rideRepository.findByDriverIdOrderByCreatedAtDesc(driverId)
            .stream().map(this::mapToDto).toList();
    }
    
        @Override
    public List<DriverResponse> findAvailableDrivers(String serviceArea) {
        try {
            List<DriverResponse> drivers = driverServiceClient.getAvailableDrivers(
                serviceArea, null, null
            );
            log.info("Found {} available drivers in area: {}", drivers.size(), serviceArea);
            return drivers;
        } catch (Exception e) {
            log.error("Error fetching available drivers: {}", e.getMessage());
            throw new ServiceUnavailableException("Driver service is currently unavailable");
        }
    }
    
    // ============ Helper Methods ============
    
    private void validateTransition(Ride ride, RideStatus target) {
        if (!ride.getStatus().canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(
                String.format("Invalid transition: %s -> %s", ride.getStatus(), target)
            );
        }
    }
    
    private RideResponseDto mapToDto(Ride ride) {
        return RideResponseDto.builder()
            .id(ride.getId())
            .passengerId(ride.getPassengerId())
            .driverId(ride.getDriverId())
            .pickupLocation(ride.getPickupLocation())
            .destinationLocation(ride.getDestinationLocation())
            .status(ride.getStatus())
            .fareDetails(ride.getFareDetails())
            .requestedAt(ride.getRequestedAt())
            .assignedAt(ride.getAssignedAt())
            .acceptedAt(ride.getAcceptedAt())
            .startedAt(ride.getStartedAt())
            .completedAt(ride.getCompletedAt())
            .cancelledAt(ride.getCancelledAt())
            .cancellationReason(ride.getCancellationReason())
            .build();
    }
}