package com.example.ride_management_service.repository;

import com.example.ride_management_service.entity.Ride;
import com.example.ride_management_service.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, UUID> {

    List<Ride> findByPassengerId(String passengerId);

    List<Ride> findByDriverId(String driverId);

    List<Ride> findByStatus(RideStatus status);
}