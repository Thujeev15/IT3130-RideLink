package com.example.ride_management_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRideRequest(
        @NotBlank(message = "Passenger ID is required")
        @Size(max = 100, message = "Passenger ID must not exceed 100 characters")
        String passengerId,

        @NotBlank(message = "Pickup location is required")
        @Size(max = 255, message = "Pickup location must not exceed 255 characters")
        String pickupLocation,

        @NotBlank(message = "Destination is required")
        @Size(max = 255, message = "Destination must not exceed 255 characters")
        String destination
) {
}