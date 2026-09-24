package com.example.ride_management_service.client;

import java.util.List;

public interface DriverServiceClient {

    List<EligibleDriverCandidate> getEligibleDrivers(String pickupLocation);
}