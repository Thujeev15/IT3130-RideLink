package com.ridelink.fare_payment_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.dto.FareResponse;
import com.ridelink.fare_payment_service.dto.FinalFareRequest;
import com.ridelink.fare_payment_service.model.Fare;
import com.ridelink.fare_payment_service.service.FareService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    // Estimate fare
    @PostMapping("/estimate")
    public ResponseEntity<FareResponse> estimateFare(
            @Valid @RequestBody FareEstimateRequest request) {

        Fare fare = fareService.estimateFare(
                request.getRideId(),
                request.getDistanceKm()
        );

        FareResponse response = new FareResponse(
                fare.getId(),
                fare.getRideId(),
                fare.getDistanceKm(),
                fare.getEstimatedFare(),
                fare.getFinalFare()
        );

        return ResponseEntity.ok(response);
    }

    // Calculate final fare
    @PostMapping("/final")
    public ResponseEntity<FareResponse> calculateFinalFare(
            @Valid @RequestBody FinalFareRequest request) {

        Fare fare = fareService.calculateFinalFare(
                request.getRideId(),
                request.getDistanceKm()
        );

        FareResponse response = new FareResponse(
                fare.getId(),
                fare.getRideId(),
                fare.getDistanceKm(),
                fare.getEstimatedFare(),
                fare.getFinalFare()
        );

        return ResponseEntity.ok(response);
    }

    // Get fare by ID
    @GetMapping("/{fareId}")
    public ResponseEntity<FareResponse> getFare(
            @PathVariable String fareId) {

        Fare fare = fareService.getFareById(fareId);

        FareResponse response = new FareResponse(
                fare.getId(),
                fare.getRideId(),
                fare.getDistanceKm(),
                fare.getEstimatedFare(),
                fare.getFinalFare()
        );

        return ResponseEntity.ok(response);
    }
}