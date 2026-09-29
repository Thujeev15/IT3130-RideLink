package com.example.ride_management_service.client;

import java.math.BigDecimal;

public record FareCalculationResult(
        BigDecimal amount,
        String currency,
        String referenceId
) {
}