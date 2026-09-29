package com.example.ride_management_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EligibleDriverCandidate(
        String driverId,
        String displayName,
        String currentLocation,
        Double distanceKm
) {
}