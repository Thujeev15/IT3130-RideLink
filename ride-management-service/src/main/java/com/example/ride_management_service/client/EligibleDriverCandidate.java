package com.example.ride_management_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EligibleDriverCandidate(
        UUID id,
        UUID accountId,
        String fullName,
        String phoneNumber,
        String licenseNumber,
        String serviceArea,
        Double currentLatitude,
        Double currentLongitude,
        String availabilityStatus,
        String vehicleMake,
        String vehicleModel,
        String plateNumber,
        String vehicleType,
        Integer seatCapacity
) {
}