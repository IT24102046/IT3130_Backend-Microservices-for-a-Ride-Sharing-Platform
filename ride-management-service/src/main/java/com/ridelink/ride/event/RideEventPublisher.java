package com.ridelink.ride.event;

import com.ridelink.ride.config.RabbitMQConfig;
import com.ridelink.ride.model.Ride;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishRideAssigned(Ride ride) {
        RideAssignedEvent event = RideAssignedEvent.builder()
            .rideId(ride.getId())
            .driverId(ride.getDriverId())
            .passengerId(ride.getPassengerId())
            .assignedAt(ride.getAssignedAt())
            .build();
        
        log.info("Publishing ride.assigned event: {}", event);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.RIDE_EXCHANGE,
            RabbitMQConfig.RIDE_ASSIGNED_ROUTING_KEY,
            event
        );
    }
    
    public void publishStatusChanged(Ride ride) {
        log.info("Publishing ride status change: {} -> {}", ride.getId(), ride.getStatus());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.RIDE_EXCHANGE,
            RabbitMQConfig.RIDE_STATUS_ROUTING_KEY,
            ride
        );
    }
}