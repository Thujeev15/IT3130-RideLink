package com.ridelink.driverservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record DriverRegistrationRequest(

        @NotNull(message = "accountId is required")
        UUID accountId,

        @NotBlank(message = "fullName is required")
        String fullName,

        @NotBlank(message = "phoneNumber is required")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "phoneNumber must be a valid number")
        String phoneNumber,

        @NotBlank(message = "licenseNumber is required")
        String licenseNumber,

        String serviceArea
) {}
