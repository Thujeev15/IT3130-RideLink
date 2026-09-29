package com.example.ride_management_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RideDriverActionRequest(
        @NotBlank(message = "Driver ID is required")
        @Size(max = 100, message = "Driver ID must not exceed 100 characters")
        String driverId
) {
}