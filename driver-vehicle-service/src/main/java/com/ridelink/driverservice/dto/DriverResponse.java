package com.ridelink.driverservice.dto;

import com.ridelink.driverservice.entity.AvailabilityStatus;
import com.ridelink.driverservice.entity.Driver;
import com.ridelink.driverservice.entity.VehicleType;

import java.util.UUID;

public record DriverResponse(
        UUID id,
        UUID accountId,
        String fullName,
        String phoneNumber,
        String licenseNumber,
        String serviceArea,
        Double currentLatitude,
        Double currentLongitude,
        AvailabilityStatus availabilityStatus,
        String vehicleMake,
        String vehicleModel,
        String plateNumber,
        VehicleType vehicleType,
        Integer seatCapacity
) {
    public static DriverResponse from(Driver d) {
        var v = d.getVehicle();
        return new DriverResponse(
                d.getId(),
                d.getAccountId(),
                d.getFullName(),
                d.getPhoneNumber(),
                d.getLicenseNumber(),
                d.getServiceArea(),
                d.getCurrentLatitude(),
                d.getCurrentLongitude(),
                d.getAvailabilityStatus(),
                v != null ? v.getMake() : null,
                v != null ? v.getModel() : null,
                v != null ? v.getPlateNumber() : null,
                v != null ? v.getVehicleType() : null,
                v != null ? v.getSeatCapacity() : null
        );
    }
}
