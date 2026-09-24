package com.ridelink.driverservice.dto;

import com.ridelink.driverservice.entity.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

public record AvailabilityUpdateRequest(
        @NotNull(message = "availabilityStatus is required") AvailabilityStatus availabilityStatus
) {}
