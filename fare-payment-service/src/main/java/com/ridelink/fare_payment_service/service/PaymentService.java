package com.ridelink.fare_payment_service.service;

import org.springframework.stereotype.Service;

import com.ridelink.fare_payment_service.model.Payment;
import com.ridelink.fare_payment_service.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Record a simulated payment
    public Payment recordPayment(
            String rideId,
            String fareId,
            double amount,
            String paymentMethod) {

        if (rideId == null || rideId.isBlank()) {
            throw new IllegalArgumentException("Ride ID is required");
        }

        if (fareId == null || fareId.isBlank()) {
            throw new IllegalArgumentException("Fare ID is required");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than 0"
            );
        }

        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment method is required"
            );
        }

        Payment payment = new Payment(
                rideId,
                fareId,
                amount,
                paymentMethod
        );

        // Simulated payment processing
        payment.setStatus("SUCCESS");

        return paymentRepository.save(payment);
    }

    // Get payment by ID
    public Payment getPaymentById(String paymentId) {

        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment ID is required"
            );
        }

        return paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found: " + paymentId
                        )
                );
    }

    // Update payment status
    public Payment updatePaymentStatus(
            String paymentId,
            String status) {

        Payment payment = getPaymentById(paymentId);

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment status is required"
            );
        }

        String updatedStatus = status.toUpperCase();

        if (!updatedStatus.equals("PENDING")
                && !updatedStatus.equals("SUCCESS")
                && !updatedStatus.equals("FAILED")) {

            throw new IllegalArgumentException(
                    "Invalid payment status. Allowed values: PENDING, SUCCESS, FAILED"
            );
        }

        payment.setStatus(updatedStatus);

        return paymentRepository.save(payment);
    }
}