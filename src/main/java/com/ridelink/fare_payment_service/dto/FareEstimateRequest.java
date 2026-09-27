package com.ridelink.fare_payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class FareEstimateRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @Positive(message = "Distance must be greater than 0")
    private double distanceKm;

    public FareEstimateRequest() {
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }
}