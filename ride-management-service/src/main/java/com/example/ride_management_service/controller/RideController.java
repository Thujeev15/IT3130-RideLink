package com.example.ride_management_service.controller;

import com.example.ride_management_service.dto.AssignDriverRequest;
import com.example.ride_management_service.dto.CancelRideRequest;
import com.example.ride_management_service.dto.CreateRideRequest;
import com.example.ride_management_service.dto.RideDriverActionRequest;
import com.example.ride_management_service.dto.RideResponse;
import com.example.ride_management_service.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @Operation(summary = "Create a ride request")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ride created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.createRide(request));
    }

    @Operation(summary = "Get a ride by ID")
    @GetMapping("/{id}")
    public RideResponse getRide(@PathVariable UUID id) {
        return rideService.getRide(id);
    }

    @Operation(summary = "Get rides for a passenger")
    @GetMapping("/passenger/{passengerId}")
    public List<RideResponse> getRidesByPassenger(@PathVariable String passengerId) {
        return rideService.getRidesByPassenger(passengerId);
    }

    @Operation(summary = "Get rides for a driver")
    @GetMapping("/driver/{driverId}")
    public List<RideResponse> getRidesByDriver(@PathVariable String driverId) {
        return rideService.getRidesByDriver(driverId);
    }

    @Operation(summary = "Assign a driver to a ride")
    @PutMapping("/{id}/assign")
    public RideResponse assignDriver(@PathVariable UUID id, @Valid @RequestBody AssignDriverRequest request) {
        return rideService.assignDriver(id, request);
    }

    @Operation(summary = "Accept an assigned ride")
    @PutMapping("/{id}/accept")
    public RideResponse acceptRide(@PathVariable UUID id, @Valid @RequestBody RideDriverActionRequest request) {
        return rideService.acceptRide(id, request);
    }

    @Operation(summary = "Start a ride")
    @PutMapping("/{id}/start")
    public RideResponse startRide(@PathVariable UUID id, @Valid @RequestBody RideDriverActionRequest request) {
        return rideService.startRide(id, request);
    }

    @Operation(summary = "Complete a ride")
    @PutMapping("/{id}/complete")
    public RideResponse completeRide(@PathVariable UUID id, @Valid @RequestBody RideDriverActionRequest request) {
        return rideService.completeRide(id, request);
    }

    @Operation(summary = "Cancel a ride")
    @PutMapping("/{id}/cancel")
    public RideResponse cancelRide(@PathVariable UUID id, @Valid @RequestBody CancelRideRequest request) {
        return rideService.cancelRide(id, request);
    }
}