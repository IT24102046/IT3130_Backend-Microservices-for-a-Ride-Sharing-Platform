package com.ridelink.ride.repository;

import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends MongoRepository<Ride, String> {
    
    List<Ride> findByPassengerIdOrderByCreatedAtDesc(String passengerId);
    
    List<Ride> findByDriverIdOrderByCreatedAtDesc(String driverId);
    
    List<Ride> findByStatus(RideStatus status);
    
    Optional<Ride> findByIdAndPassengerId(String id, String passengerId);
    
    Optional<Ride> findByIdAndDriverId(String id, String driverId);
}