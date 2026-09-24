package com.ridelink.ride.service;

import com.ridelink.ride.dto.*;

import java.util.List;

public interface RideService {
    RideResponseDto requestRide(RideRequestDto request, String passengerId);
    RideResponseDto assignDriver(String rideId, AssignDriverDto request, String passengerId);
    RideResponseDto acceptRide(String rideId, String driverId);
    RideResponseDto startRide(String rideId, String driverId);
    RideResponseDto completeRide(String rideId, String driverId);
    RideResponseDto cancelRide(String rideId, String userId, String reason);
    RideResponseDto getRideById(String rideId, String userId, String role);
    List<RideResponseDto> getPassengerRides(String passengerId);
    List<RideResponseDto> getDriverRides(String driverId);
    List<DriverResponse> findAvailableDrivers(String serviceArea);
}