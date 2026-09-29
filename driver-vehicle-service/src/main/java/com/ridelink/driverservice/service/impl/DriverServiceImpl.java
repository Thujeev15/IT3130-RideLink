package com.ridelink.driverservice.service.impl;

import com.ridelink.driverservice.dto.*;
import com.ridelink.driverservice.entity.AvailabilityStatus;
import com.ridelink.driverservice.entity.Driver;
import com.ridelink.driverservice.entity.Vehicle;
import com.ridelink.driverservice.exception.ConflictException;
import com.ridelink.driverservice.exception.ResourceNotFoundException;
import com.ridelink.driverservice.repository.DriverRepository;
import com.ridelink.driverservice.repository.VehicleRepository;
import com.ridelink.driverservice.service.DriverService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public DriverServiceImpl(DriverRepository driverRepository, VehicleRepository vehicleRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public DriverResponse registerDriver(DriverRegistrationRequest request) {
        if (driverRepository.findByAccountId(request.accountId()).isPresent()) {
            throw new ConflictException("A driver profile already exists for this account");
        }
        if (driverRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new ConflictException("licenseNumber is already registered");
        }

        Driver driver = Driver.builder()
                .accountId(request.accountId())
                .fullName(request.fullName())
                .phoneNumber(request.phoneNumber())
                .licenseNumber(request.licenseNumber())
                .serviceArea(request.serviceArea())
                .availabilityStatus(AvailabilityStatus.OFFLINE)
                .build();

        return DriverResponse.from(driverRepository.save(driver));
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getDriver(UUID driverId) {
        return DriverResponse.from(findDriverOrThrow(driverId));
    }

    @Override
    public DriverResponse attachVehicle(UUID driverId, VehicleRequest request) {
        Driver driver = findDriverOrThrow(driverId);

        if (vehicleRepository.existsByPlateNumber(request.plateNumber())
                && (driver.getVehicle() == null || !driver.getVehicle().getPlateNumber().equalsIgnoreCase(request.plateNumber()))) {
            throw new ConflictException("plateNumber is already registered to another vehicle");
        }

        Vehicle vehicle = driver.getVehicle();
        if (vehicle == null) {
            vehicle = Vehicle.builder().driver(driver).build();
        }
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());
        vehicle.setPlateNumber(request.plateNumber());
        vehicle.setColor(request.color());
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setSeatCapacity(request.seatCapacity());

        driver.setVehicle(vehicle);
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Override
    public DriverResponse updateAvailability(UUID driverId, AvailabilityUpdateRequest request) {
        Driver driver = findDriverOrThrow(driverId);

        if (request.availabilityStatus() == AvailabilityStatus.AVAILABLE && driver.getVehicle() == null) {
            throw new IllegalStateException("Driver cannot go AVAILABLE without a registered vehicle");
        }

        driver.setAvailabilityStatus(request.availabilityStatus());
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Override
    public DriverResponse updateLocation(UUID driverId, LocationUpdateRequest request) {
        Driver driver = findDriverOrThrow(driverId);
        driver.setCurrentLatitude(request.latitude());
        driver.setCurrentLongitude(request.longitude());
        if (request.serviceArea() != null && !request.serviceArea().isBlank()) {
            driver.setServiceArea(request.serviceArea());
        }
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> findEligibleDrivers(String serviceArea) {
        String normalizedArea = (serviceArea != null && !serviceArea.isBlank()) ? serviceArea.trim() : null;
        return driverRepository.findEligibleDrivers(AvailabilityStatus.AVAILABLE, normalizedArea)
                .stream()
                .map(DriverResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> listDrivers() {
        return driverRepository.findAllWithVehicle().stream().map(DriverResponse::from).toList();
    }

    private Driver findDriverOrThrow(UUID driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));
    }
}
