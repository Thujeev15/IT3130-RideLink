package com.example.ride_management_service.exception;

public class InvalidRideTransitionException extends RuntimeException {

    public InvalidRideTransitionException(String message) {
        super(message);
    }
}