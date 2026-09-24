package com.ridelink.driverservice.controller;

import com.ridelink.driverservice.dto.*;
import com.ridelink.driverservice.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Driver & Vehicle", description = "Driver profiles, vehicles, availability and eligible-driver lookup")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @Operation(summary = "Register a driver's operational profile (called after account creation)")
    @PostMapping
    public ResponseEntity<DriverResponse> register(@Valid @RequestBody DriverRegistrationRequest request) {
        DriverResponse created = driverService.registerDriver(request);
        return ResponseEntity.created(URI.create("/api/drivers/" + created.id())).body(created);
    }

    @Operation(summary = "Get a driver's profile by id")
    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> getDriver(@PathVariable UUID driverId) {
        return ResponseEntity.ok(driverService.getDriver(driverId));
    }

    @Operation(summary = "List all drivers (admin/demo use)")
    @GetMapping
    public ResponseEntity<List<DriverResponse>> listDrivers() {
        return ResponseEntity.ok(driverService.listDrivers());
    }

    @Operation(summary = "Add or replace the driver's vehicle details")
    @PutMapping("/{driverId}/vehicle")
    public ResponseEntity<DriverResponse> attachVehicle(@PathVariable UUID driverId,
                                                          @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(driverService.attachVehicle(driverId, request));
    }

    @Operation(summary = "Update the driver's availability status (OFFLINE / AVAILABLE / BUSY)")
    @PatchMapping("/{driverId}/availability")
    public ResponseEntity<DriverResponse> updateAvailability(@PathVariable UUID driverId,
                                                               @Valid @RequestBody AvailabilityUpdateRequest request) {
        return ResponseEntity.ok(driverService.updateAvailability(driverId, request));
    }

    @Operation(summary = "Update the driver's simulated current location / service area")
    @PatchMapping("/{driverId}/location")
    public ResponseEntity<DriverResponse> updateLocation(@PathVariable UUID driverId,
                                                           @Valid @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(driverService.updateLocation(driverId, request));
    }

    @Operation(summary = "Retrieve eligible available drivers for a pickup area "
            + "(consumed by the Ride Management Service during driver assignment)")
    @GetMapping("/eligible")
    public ResponseEntity<List<DriverResponse>> findEligible(@RequestParam(required = false) String serviceArea) {
        List<DriverResponse> eligible = driverService.findEligibleDrivers(serviceArea);
        if (eligible.isEmpty()) {
            // Negative scenario #1 required by the brief: "no available driver"
            return ResponseEntity.status(HttpStatus.OK).body(List.of());
        }
        return ResponseEntity.ok(eligible);
    }
}
