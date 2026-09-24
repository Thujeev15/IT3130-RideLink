package com.example.ride_management_service.exception;

public class UnauthorizedRideActionException extends RuntimeException {

    public UnauthorizedRideActionException(String message) {
        super(message);
    }
}