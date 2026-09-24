package com.ridelink.driverservice.repository;

import com.ridelink.driverservice.entity.AvailabilityStatus;
import com.ridelink.driverservice.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByAccountId(UUID accountId);

    boolean existsByLicenseNumber(String licenseNumber);

    /**
     * Simple, documented eligibility rule: a driver is eligible if they are
     * AVAILABLE and their declared service area matches the requested pickup
     * area (case-insensitive). Eagerly fetches vehicle to avoid N+1 queries.
     */
    @Query("SELECT d FROM Driver d JOIN FETCH d.vehicle "
            + "WHERE d.availabilityStatus = :status "
            + "AND (:serviceArea IS NULL OR LOWER(d.serviceArea) = LOWER(:serviceArea))")
    List<Driver> findEligibleDrivers(@Param("status") AvailabilityStatus status,
                                      @Param("serviceArea") String serviceArea);

    @Query("SELECT DISTINCT d FROM Driver d LEFT JOIN FETCH d.vehicle")
    List<Driver> findAllWithVehicle();
}
