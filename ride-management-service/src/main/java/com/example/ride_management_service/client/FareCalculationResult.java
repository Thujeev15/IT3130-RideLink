package com.example.ride_management_service.client;

public record FareCalculationResult(
        String fareId,
        String rideId,
        Double distanceKm,
        Double estimatedFare,
        Double finalFare
) {
}