package com.example.ride_management_service.exception;

public class NoEligibleDriverException extends RuntimeException {

    public NoEligibleDriverException(String message) {
        super(message);
    }
}