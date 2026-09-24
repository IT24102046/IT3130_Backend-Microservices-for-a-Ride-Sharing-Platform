package com.ridelink.ride.client;

import com.ridelink.ride.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
    name = "account-service",
    url = "${services.account.url}",
    configuration = FeignConfig.class
)
public interface AccountServiceClient {
    
    @GetMapping("/api/v1/accounts/{id}")
    Map<String, Object> getAccountById(@PathVariable String id);
}