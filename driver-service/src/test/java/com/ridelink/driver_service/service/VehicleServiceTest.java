package com.ridelink.driver_service.service;

import com.ridelink.driver_service.dto.VehicleRegistrationRequest;
import com.ridelink.driver_service.dto.VehicleResponse;
import com.ridelink.driver_service.exception.BusinessException;
import com.ridelink.driver_service.exception.ResourceNotFoundException;
import com.ridelink.driver_service.model.Driver;
import com.ridelink.driver_service.model.Vehicle;
import com.ridelink.driver_service.model.VehicleType;
import com.ridelink.driver_service.repository.DriverRepository;
import com.ridelink.driver_service.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleRepository, driverRepository);
    }

    @Test
    void registerVehicleReturnsSavedVehicleAndLinksDriver() {
        VehicleRegistrationRequest request = request();
        Driver driver = new Driver();
        Vehicle savedVehicle = Vehicle.builder()
            .id("vehicle-1")
            .driverId("driver-1")
            .plateNumber("CAB-1234")
            .make("Toyota")
            .model("Prius")
            .year(2024)
            .color("Blue")
            .type(VehicleType.CAR)
            .seatCapacity(4)
            .active(true)
            .build();

        when(vehicleRepository.existsByPlateNumber("CAB-1234")).thenReturn(false);
        when(driverRepository.findById("driver-1")).thenReturn(Optional.of(driver));
        when(vehicleRepository.findByDriverId("driver-1")).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        VehicleResponse response = vehicleService.registerVehicle(request);

        assertEquals("vehicle-1", response.getId());
        assertEquals("CAB-1234", response.getPlateNumber());
        assertEquals("vehicle-1", driver.getVehicleId());
        verify(driverRepository).save(driver);
    }

    @Test
    void registerVehicleRejectsDuplicatePlate() {
        when(vehicleRepository.existsByPlateNumber("CAB-1234")).thenReturn(true);

        assertThrows(BusinessException.class,
            () -> vehicleService.registerVehicle(request()));
    }

    @Test
    void registerVehicleRejectsMissingDriver() {
        when(vehicleRepository.existsByPlateNumber("CAB-1234")).thenReturn(false);
        when(driverRepository.findById("driver-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> vehicleService.registerVehicle(request()));
    }

    @Test
    void getVehicleByDriverIdReturnsVehicle() {
        Vehicle vehicle = Vehicle.builder()
            .id("vehicle-1")
            .driverId("driver-1")
            .plateNumber("CAB-1234")
            .build();
        when(vehicleRepository.findByDriverId("driver-1")).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getVehicleByDriverId("driver-1");

        assertEquals("vehicle-1", response.getId());
        assertEquals("driver-1", response.getDriverId());
    }

    @Test
    void getVehicleByDriverIdThrowsWhenVehicleDoesNotExist() {
        when(vehicleRepository.findByDriverId("driver-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> vehicleService.getVehicleByDriverId("driver-1"));
    }

    private VehicleRegistrationRequest request() {
        VehicleRegistrationRequest request = new VehicleRegistrationRequest();
        request.setDriverId("driver-1");
        request.setPlateNumber("CAB-1234");
        request.setMake("Toyota");
        request.setModel("Prius");
        request.setYear(2024);
        request.setColor("Blue");
        request.setType(VehicleType.CAR);
        request.setSeatCapacity(4);
        return request;
    }
}