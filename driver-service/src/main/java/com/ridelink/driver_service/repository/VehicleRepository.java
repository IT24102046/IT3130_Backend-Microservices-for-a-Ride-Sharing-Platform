package com.ridelink.driver_service.repository;

import com.ridelink.driver_service.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    Optional<Vehicle> findByDriverId(String driverId);
    boolean existsByPlateNumber(String plateNumber);
}