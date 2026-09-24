package com.ridelink.driverservice.dto;

import com.ridelink.driverservice.entity.VehicleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleRequest(
        @NotBlank(message = "make is required") String make,
        @NotBlank(message = "model is required") String model,
        @NotNull @Min(value = 1990, message = "year must be 1990 or later") Integer year,
        @NotBlank(message = "plateNumber is required") String plateNumber,
        @NotBlank(message = "color is required") String color,
        @NotNull(message = "vehicleType is required") VehicleType vehicleType,
        @NotNull @Min(1) @Max(15) Integer seatCapacity
) {}
