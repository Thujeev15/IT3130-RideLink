package com.ridelink.fare_payment_service.service;

import org.springframework.stereotype.Service;

import com.ridelink.fare_payment_service.model.Fare;
import com.ridelink.fare_payment_service.repository.FareRepository;

@Service
public class FareService {

    private final FareRepository fareRepository;

    private static final double BASE_FARE = 100.0;
    private static final double RATE_PER_KM = 80.0;

    public FareService(FareRepository fareRepository) {
        this.fareRepository = fareRepository;
    }

    // Estimate fare
    public Fare estimateFare(String rideId, double distanceKm) {

        if (rideId == null || rideId.isBlank()) {
            throw new IllegalArgumentException("Ride ID is required");
        }

        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be greater than 0");
        }

        double estimatedFare = BASE_FARE + (distanceKm * RATE_PER_KM);

        Fare fare = new Fare(
                rideId,
                distanceKm,
                estimatedFare
        );

        return fareRepository.save(fare);
    }

    // Calculate final fare
    public Fare calculateFinalFare(String rideId, double distanceKm) {

        if (rideId == null || rideId.isBlank()) {
            throw new IllegalArgumentException("Ride ID is required");
        }

        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be greater than 0");
        }

        double finalFare = BASE_FARE + (distanceKm * RATE_PER_KM);

        Fare fare = new Fare(
                rideId,
                distanceKm,
                finalFare
        );

        fare.setFinalFare(finalFare);

        return fareRepository.save(fare);
    }

    // Get fare by ID
    public Fare getFareById(String fareId) {

        if (fareId == null || fareId.isBlank()) {
            throw new IllegalArgumentException("Fare ID is required");
        }

        return fareRepository.findById(fareId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Fare not found: " + fareId
                        )
                );
    }
}