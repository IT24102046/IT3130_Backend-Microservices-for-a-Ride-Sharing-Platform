package com.ridelink.ride.model;

public enum RideStatus {
    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;
    
    public boolean canTransitionTo(RideStatus next) {
        return switch (this) {
            case REQUESTED -> next == ASSIGNED || next == CANCELLED;
            case ASSIGNED -> next == ACCEPTED || next == CANCELLED;
            case ACCEPTED -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == COMPLETED || next == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}