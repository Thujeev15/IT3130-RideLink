package com.example.ride_management_service.dto;

import jakarta.validation.constraints.Size;

public record AssignDriverRequest(
        @Size(max = 100, message = "Driver ID must not exceed 100 characters")
        String driverId
) {
}