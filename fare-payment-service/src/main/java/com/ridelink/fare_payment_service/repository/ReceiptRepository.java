package com.ridelink.fare_payment_service.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ridelink.fare_payment_service.model.Receipt;

public interface ReceiptRepository extends MongoRepository<Receipt, String> {

    Optional<Receipt> findByPaymentId(String paymentId);
}