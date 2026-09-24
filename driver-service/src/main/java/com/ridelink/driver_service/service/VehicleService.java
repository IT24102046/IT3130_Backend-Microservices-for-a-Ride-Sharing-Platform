package com.ridelink.driver_service.service;

import com.ridelink.driver_service.dto.VehicleRegistrationRequest;
import com.ridelink.driver_service.dto.VehicleResponse;
import com.ridelink.driver_service.exception.BusinessException;
import com.ridelink.driver_service.exception.ResourceNotFoundException;
import com.ridelink.driver_service.model.Driver;
import com.ridelink.driver_service.model.Vehicle;
import com.ridelink.driver_service.repository.DriverRepository;
import com.ridelink.driver_service.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public VehicleResponse registerVehicle(VehicleRegistrationRequest req) {
        if (vehicleRepository.existsByPlateNumber(req.getPlateNumber())) {
            throw new BusinessException("Vehicle with this plate already exists");
        }

        Driver driver = driverRepository.findById(req.getDriverId())
            .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (vehicleRepository.findByDriverId(req.getDriverId()).isPresent()) {
            throw new BusinessException("Driver already has a vehicle");
        }

        Vehicle vehicle = Vehicle.builder()
            .driverId(req.getDriverId())
            .plateNumber(req.getPlateNumber())
            .make(req.getMake())
            .model(req.getModel())
            .year(req.getYear())
            .color(req.getColor())
            .type(req.getType())
            .seatCapacity(req.getSeatCapacity())
            .active(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        Vehicle saved = vehicleRepository.save(vehicle);

        driver.setVehicleId(saved.getId());
        driver.setUpdatedAt(LocalDateTime.now());
        driverRepository.save(driver);

        log.info("Vehicle registered: {}", saved.getId());
        return toResponse(saved);
    }

    public VehicleResponse getVehicleByDriverId(String driverId) {
        Vehicle v = vehicleRepository.findByDriverId(driverId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Vehicle not found for driver: " + driverId));
        return toResponse(v);
    }

    private VehicleResponse toResponse(Vehicle v) {
        return VehicleResponse.builder()
            .id(v.getId())
            .driverId(v.getDriverId())
            .plateNumber(v.getPlateNumber())
            .make(v.getMake())
            .model(v.getModel())
            .year(v.getYear())
            .color(v.getColor())
            .type(v.getType())
            .seatCapacity(v.getSeatCapacity())
            .active(v.getActive())
            .createdAt(v.getCreatedAt())
            .build();
    }
}