package com.ridelink.driver_service.repository;

import com.ridelink.driver_service.model.Driver;
import com.ridelink.driver_service.model.DriverStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {

    Optional<Driver> findByAccountId(String accountId);

    List<Driver> findByStatus(DriverStatus status);

    List<Driver> findByStatusAndServiceAreasContaining(
        DriverStatus status, String area);

    boolean existsByAccountId(String accountId);
}