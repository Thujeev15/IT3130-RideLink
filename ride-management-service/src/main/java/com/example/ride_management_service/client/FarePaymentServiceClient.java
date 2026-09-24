package com.example.ride_management_service.client;

public interface FarePaymentServiceClient {

    FareCalculationResult calculateFinalFare(FareCalculationRequest request);
}