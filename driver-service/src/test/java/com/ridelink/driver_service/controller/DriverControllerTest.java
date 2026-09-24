package com.ridelink.driver_service.controller;

import com.ridelink.driver_service.dto.DriverResponse;
import com.ridelink.driver_service.model.DriverStatus;
import com.ridelink.driver_service.service.DriverService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DriverControllerTest {

    private final DriverService driverService = mock(DriverService.class);
    private final DriverController controller = new DriverController(driverService);

    @Test
    void getByIdReturns200WhenDriverIsFound() {
        DriverResponse response = DriverResponse.builder()
            .id("drv-1")
            .fullName("John")
            .status(DriverStatus.OFFLINE)
            .build();
        when(driverService.getDriverById("drv-1")).thenReturn(response);

        var result = controller.getById("drv-1");

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().isSuccess());
        assertEquals("drv-1", result.getBody().getData().getId());
    }
}