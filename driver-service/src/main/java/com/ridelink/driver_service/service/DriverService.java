package com.ridelink.driver_service.service;

import com.ridelink.driver_service.dto.*;
import com.ridelink.driver_service.exception.BusinessException;
import com.ridelink.driver_service.exception.ResourceNotFoundException;
import com.ridelink.driver_service.model.Driver;
import com.ridelink.driver_service.model.DriverStatus;
import com.ridelink.driver_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverResponse registerDriver(DriverRegistrationRequest request) {
        if (driverRepository.existsByAccountId(request.getAccountId())) {
            throw new BusinessException("Driver already registered for this account");
        }

        Driver driver = Driver.builder()
            .accountId(request.getAccountId())
            .fullName(request.getFullName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .licenseNumber(request.getLicenseNumber())
            .serviceAreas(request.getServiceAreas())
            .status(DriverStatus.OFFLINE)
            .rating(5.0)
            .totalRides(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        Driver saved = driverRepository.save(driver);
        log.info("Driver registered: {}", saved.getId());
        return toResponse(saved);
    }

    public DriverResponse getDriverById(String id) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
        return toResponse(driver);
    }

    public DriverResponse getDriverByAccountId(String accountId) {
        Driver driver = driverRepository.findByAccountId(accountId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Driver not found for account: " + accountId));
        return toResponse(driver);
    }

    public DriverResponse updateAvailability(String driverId, AvailabilityUpdateRequest req) {
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));

        driver.setStatus(req.getStatus());
        driver.setUpdatedAt(LocalDateTime.now());
        driver.setLastActiveAt(LocalDateTime.now());

        return toResponse(driverRepository.save(driver));
    }

    public DriverResponse updateLocation(String driverId, LocationUpdateRequest req) {
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));

        driver.setCurrentLocation(
            new GeoJsonPoint(req.getLongitude(), req.getLatitude()));
        driver.setUpdatedAt(LocalDateTime.now());
        driver.setLastActiveAt(LocalDateTime.now());

        return toResponse(driverRepository.save(driver));
    }

    public DriverResponse updateServiceAreas(String driverId, List<String> areas) {
        if (areas == null || areas.isEmpty()) {
            throw new BusinessException("At least one service area required");
        }
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));

        driver.setServiceAreas(areas);
        driver.setUpdatedAt(LocalDateTime.now());
        return toResponse(driverRepository.save(driver));
    }

    public List<DriverResponse> getEligibleDrivers(String area) {
        List<Driver> drivers;
        if (area != null && !area.isBlank()) {
            drivers = driverRepository.findByStatusAndServiceAreasContaining(
                DriverStatus.AVAILABLE, area);
        } else {
            drivers = driverRepository.findByStatus(DriverStatus.AVAILABLE);
        }
        return drivers.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private DriverResponse toResponse(Driver d) {
        return DriverResponse.builder()
            .id(d.getId())
            .accountId(d.getAccountId())
            .fullName(d.getFullName())
            .email(d.getEmail())
            .phone(d.getPhone())
            .licenseNumber(d.getLicenseNumber())
            .status(d.getStatus())
            .serviceAreas(d.getServiceAreas())
            .currentLat(d.getCurrentLocation() != null ? d.getCurrentLocation().getY() : null)
            .currentLng(d.getCurrentLocation() != null ? d.getCurrentLocation().getX() : null)
            .rating(d.getRating())
            .totalRides(d.getTotalRides())
            .createdAt(d.getCreatedAt())
            .build();
    }
}
