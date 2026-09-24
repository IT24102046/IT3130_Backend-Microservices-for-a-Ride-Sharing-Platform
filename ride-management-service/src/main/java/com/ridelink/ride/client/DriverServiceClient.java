package com.ridelink.ride.client;

import com.ridelink.ride.config.FeignConfig;
import com.ridelink.ride.dto.DriverResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
    name = "driver-service",
    url = "${services.driver.url}",
    configuration = FeignConfig.class
)
public interface DriverServiceClient {
    
    @GetMapping("/api/v1/drivers/available")
    List<DriverResponse> getAvailableDrivers(
        @RequestParam(required = false) String serviceArea,
        @RequestParam(required = false) Double latitude,
        @RequestParam(required = false) Double longitude
    );
    
    @GetMapping("/api/v1/drivers/{id}")
    DriverResponse getDriverById(@PathVariable String id);
    
    @PutMapping("/api/v1/drivers/{id}/status")
    void updateDriverStatus(
        @PathVariable String id,
        @RequestParam String status
    );
}