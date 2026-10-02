package com.example.ride_management_service.client;

import java.util.UUID;

public record FareCalculationRequest(
        UUID rideId,
        double distanceKm
) {
}