package com.ridelink.fare_payment_service.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "receipts")
public class Receipt {

    @Id
    private String id;

    private String paymentId;
    private String rideId;
    private double amount;
    private String paymentStatus;
    private LocalDateTime generatedAt;

    public Receipt() {
    }

    public Receipt(
            String paymentId,
            String rideId,
            double amount,
            String paymentStatus) {

        this.paymentId = paymentId;
        this.rideId = rideId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.generatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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