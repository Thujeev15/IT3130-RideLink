package com.example.ride_management_service.exception;

public class RideNotCancelableException extends RuntimeException {

    public RideNotCancelableException(String message) {
        super(message);
    }
}