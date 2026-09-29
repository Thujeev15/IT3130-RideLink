package com.example.ride_management_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelRideRequest(
        @NotBlank(message = "Actor ID is required")
        @Size(max = 100, message = "Actor ID must not exceed 100 characters")
        String actorId,

        @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
        String reason
) {
}