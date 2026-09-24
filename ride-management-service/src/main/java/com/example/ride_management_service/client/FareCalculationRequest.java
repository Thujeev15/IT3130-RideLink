package com.example.ride_management_service.client;

import java.time.Instant;
import java.util.UUID;

public record FareCalculationRequest(
        UUID rideId,
        String passengerId,
        String driverId,
        String pickupLocation,
        String destination,
        Instant requestedAt,
        Instant assignedAt,
        Instant acceptedAt,
        Instant startedAt,
        Instant completedAt
) {
}