package com.ridelink.fare_payment_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ridelink.fare_payment_service.model.Fare;

public interface FareRepository extends MongoRepository<Fare, String> {
}