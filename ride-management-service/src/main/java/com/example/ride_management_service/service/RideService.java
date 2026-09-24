package com.example.ride_management_service.service;

import com.example.ride_management_service.dto.AssignDriverRequest;
import com.example.ride_management_service.dto.CancelRideRequest;
import com.example.ride_management_service.dto.CreateRideRequest;
import com.example.ride_management_service.dto.RideDriverActionRequest;
import com.example.ride_management_service.dto.RideResponse;

import java.util.List;
import java.util.UUID;

public interface RideService {

    RideResponse createRide(CreateRideRequest request);

    RideResponse getRide(UUID rideId);

    List<RideResponse> getRidesByPassenger(String passengerId);

    List<RideResponse> getRidesByDriver(String driverId);

    RideResponse assignDriver(UUID rideId, AssignDriverRequest request);

    RideResponse acceptRide(UUID rideId, RideDriverActionRequest request);

    RideResponse startRide(UUID rideId, RideDriverActionRequest request);

    RideResponse completeRide(UUID rideId, RideDriverActionRequest request);

    RideResponse cancelRide(UUID rideId, CancelRideRequest request);
}