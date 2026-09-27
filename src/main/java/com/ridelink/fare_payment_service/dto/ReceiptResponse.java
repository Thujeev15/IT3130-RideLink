package com.ridelink.fare_payment_service.dto;

import java.time.LocalDateTime;

public class ReceiptResponse {

    private String receiptId;
    private String paymentId;
    private String rideId;
    private double amount;
    private String paymentStatus;
    private LocalDateTime generatedAt;

    public ReceiptResponse() {
    }

    public ReceiptResponse(
            String receiptId,
            String paymentId,
            String rideId,
            double amount,
            String paymentStatus,
            LocalDateTime generatedAt) {

        this.receiptId = receiptId;
        this.paymentId = paymentId;
        this.rideId = rideId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.generatedAt = generatedAt;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}