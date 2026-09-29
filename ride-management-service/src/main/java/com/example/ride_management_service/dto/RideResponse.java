package com.example.ride_management_service.dto;

import com.example.ride_management_service.entity.RideStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RideResponse(
        UUID id,
        String passengerId,
        String driverId,
        String pickupLocation,
        String destination,
        RideStatus status,
        BigDecimal estimatedFare,
        String estimatedFareReference,
        Instant requestedAt,
        Instant assignedAt,
        Instant acceptedAt,
        Instant startedAt,
        Instant completedAt,
        Instant cancelledAt,
        String cancellationReason
) {
}