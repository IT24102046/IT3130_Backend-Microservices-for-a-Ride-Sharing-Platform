package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.FareServiceClient;
import com.ridelink.ride.dto.*;
import com.ridelink.ride.event.RideEventPublisher;
import com.ridelink.ride.exception.InvalidStatusTransitionException;
import com.ridelink.ride.exception.ResourceNotFoundException;
import com.ridelink.ride.exception.UnauthorizedException;
import com.ridelink.ride.model.*;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.service.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {
    
    @Mock private RideRepository rideRepository;
    @Mock private DriverServiceClient driverServiceClient;
    @Mock private FareServiceClient fareServiceClient;
    @Mock private RideEventPublisher eventPublisher;
    
    @InjectMocks private RideServiceImpl rideService;
    
    private RideRequestDto requestDto;
    private Ride requestedRide;
    
    @BeforeEach
    void setUp() {
        requestDto = new RideRequestDto();
        requestDto.setPickupLocation(Location.builder()
            .placeName("Colombo Fort").latitude(6.9271).longitude(79.8612).build());
        requestDto.setDestinationLocation(Location.builder()
            .placeName("Kandy").latitude(7.2906).longitude(80.6337).build());
        
        requestedRide = Ride.builder()
            .id("ride-123")
            .passengerId("passenger-1")
            .pickupLocation(requestDto.getPickupLocation())
            .destinationLocation(requestDto.getDestinationLocation())
            .status(RideStatus.REQUESTED)
            .fareDetails(FareDetails.builder()
                .estimatedFare(BigDecimal.valueOf(1500)).build())
            .requestedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    void requestRide_ShouldCreateRideWithRequestedStatus() {
        FareEstimateResponse estimate = new FareEstimateResponse();
        estimate.setEstimatedFare(BigDecimal.valueOf(1500));
        estimate.setDistanceKm(BigDecimal.valueOf(115));
        
        when(fareServiceClient.estimateFare(any())).thenReturn(estimate);
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> {
            Ride r = i.getArgument(0);
            r.setId("ride-123");
            return r;
        });
        
        RideResponseDto result = rideService.requestRide(requestDto, "passenger-1");
        
        assertNotNull(result);
        assertEquals(RideStatus.REQUESTED, result.getStatus());
        assertEquals("passenger-1", result.getPassengerId());
        verify(rideRepository).save(any(Ride.class));
    }
    
    @Test
    void assignDriver_ShouldThrow_WhenRideNotFound() {
        when(rideRepository.findByIdAndPassengerId(anyString(), anyString()))
            .thenReturn(Optional.empty());
        
        AssignDriverDto dto = new AssignDriverDto();
        dto.setDriverId("driver-1");
        
        assertThrows(ResourceNotFoundException.class,
            () -> rideService.assignDriver("ride-123", dto, "passenger-1"));
    }
    
    @Test
    void assignDriver_ShouldThrow_WhenRideNotInRequestedState() {
        requestedRide.setStatus(RideStatus.COMPLETED);
        when(rideRepository.findByIdAndPassengerId(anyString(), anyString()))
            .thenReturn(Optional.of(requestedRide));
        
        AssignDriverDto dto = new AssignDriverDto();
        dto.setDriverId("driver-1");
        
        assertThrows(InvalidStatusTransitionException.class,
            () -> rideService.assignDriver("ride-123", dto, "passenger-1"));
    }
    
    @Test
    void acceptRide_ShouldThrow_WhenInvalidTransition() {
        requestedRide.setDriverId("driver-1");
        when(rideRepository.findByIdAndDriverId(anyString(), anyString()))
            .thenReturn(Optional.of(requestedRide));
        
        assertThrows(InvalidStatusTransitionException.class,
            () -> rideService.acceptRide("ride-123", "driver-1"));
    }
    
    @Test
    void completeRide_ShouldCalculateFinalFare() {
        requestedRide.setStatus(RideStatus.IN_PROGRESS);
        requestedRide.setDriverId("driver-1");
        when(rideRepository.findByIdAndDriverId(anyString(), anyString()))
            .thenReturn(Optional.of(requestedRide));
        
        FareEstimateResponse finalFare = new FareEstimateResponse();
        finalFare.setEstimatedFare(BigDecimal.valueOf(1600));
        finalFare.setDistanceKm(BigDecimal.valueOf(115));
        when(fareServiceClient.estimateFare(any())).thenReturn(finalFare);
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));
        
        RideResponseDto result = rideService.completeRide("ride-123", "driver-1");
        
        assertEquals(RideStatus.COMPLETED, result.getStatus());
        assertEquals(BigDecimal.valueOf(1600), result.getFareDetails().getFinalFare());
    }
    
    @Test
    void cancelRide_ShouldThrow_WhenUserNotAuthorized() {
        requestedRide.setPassengerId("passenger-1");
        requestedRide.setDriverId("driver-1");
        when(rideRepository.findById(anyString())).thenReturn(Optional.of(requestedRide));
        
        assertThrows(UnauthorizedException.class,
            () -> rideService.cancelRide("ride-123", "stranger", "test"));
    }
}