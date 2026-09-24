package com.ridelink.driverservice.service;

import com.ridelink.driverservice.dto.*;

import java.util.List;
import java.util.UUID;

public interface DriverService {

    DriverResponse registerDriver(DriverRegistrationRequest request);

    DriverResponse getDriver(UUID driverId);

    DriverResponse attachVehicle(UUID driverId, VehicleRequest request);

    DriverResponse updateAvailability(UUID driverId, AvailabilityUpdateRequest request);

    DriverResponse updateLocation(UUID driverId, LocationUpdateRequest request);

    List<DriverResponse> findEligibleDrivers(String serviceArea);

    List<DriverResponse> listDrivers();
}
