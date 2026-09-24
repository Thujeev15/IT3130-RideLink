package com.example.ride_management_service.client;

import com.example.ride_management_service.exception.ExternalServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class HttpDriverServiceClient implements DriverServiceClient {

    private final RestClient restClient;
    private final String baseUrl;

    public HttpDriverServiceClient(RestClient.Builder restClientBuilder,
                                   @Value("${driver.service.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.build();
        this.baseUrl = baseUrl;
    }

    @Override
    public List<EligibleDriverCandidate> getEligibleDrivers(String pickupLocation) {
        try {
            List<EligibleDriverCandidate> candidates = restClient.get()
                    .uri(baseUrl + "/api/drivers/eligible?pickupLocation={pickupLocation}", pickupLocation)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return candidates == null ? List.of() : candidates;
        } catch (RestClientException ex) {
            throw new ExternalServiceUnavailableException("Driver service is unavailable", ex);
        }
    }
}