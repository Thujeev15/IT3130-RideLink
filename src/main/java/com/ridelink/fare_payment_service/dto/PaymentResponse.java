package com.ridelink.fare_payment_service.dto;

import java.time.LocalDateTime;

public class PaymentResponse {

    private String paymentId;
    private String rideId;
    private String fareId;
    private double amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentDate;

    public PaymentResponse() {
    }

    public PaymentResponse(
            String paymentId,
            String rideId,
            String fareId,
            double amount,
            String paymentMethod,
            String status,
            LocalDateTime paymentDate) {

        this.paymentId = paymentId;
        this.rideId = rideId;
        this.fareId = fareId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentDate = paymentDate;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getFareId() {
        return fareId;
    }

    public void setFareId(String fareId) {
        this.fareId = fareId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
}