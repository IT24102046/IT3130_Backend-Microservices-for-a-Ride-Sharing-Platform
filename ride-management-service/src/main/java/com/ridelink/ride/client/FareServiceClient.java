package com.ridelink.ride.client;

import com.ridelink.ride.config.FeignConfig;
import com.ridelink.ride.dto.FareEstimateRequest;
import com.ridelink.ride.dto.FareEstimateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "fare-service",
    url = "${services.fare.url}",
    configuration = FeignConfig.class
)
public interface FareServiceClient {
    
    @PostMapping("/api/v1/fares/estimate")
    FareEstimateResponse estimateFare(@RequestBody FareEstimateRequest request);
}