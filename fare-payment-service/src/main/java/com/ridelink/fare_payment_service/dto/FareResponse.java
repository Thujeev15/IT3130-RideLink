package com.ridelink.fare_payment_service.dto;

public class FareResponse {

    private String fareId;
    private String rideId;
    private double distanceKm;
    private double estimatedFare;
    private double finalFare;

    public FareResponse() {
    }

    public FareResponse(
            String fareId,
            String rideId,
            double distanceKm,
            double estimatedFare,
            double finalFare) {

        this.fareId = fareId;
        this.rideId = rideId;
        this.distanceKm = distanceKm;
        this.estimatedFare = estimatedFare;
        this.finalFare = finalFare;
    }

    public String getFareId() {
        return fareId;
    }

    public void setFareId(String fareId) {
        this.fareId = fareId;
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

    public double getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public double getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(double finalFare) {
        this.finalFare = finalFare;
    }
}