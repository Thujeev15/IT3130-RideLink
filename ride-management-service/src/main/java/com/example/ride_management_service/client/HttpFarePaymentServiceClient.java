package com.example.ride_management_service.client;

import com.example.ride_management_service.exception.ExternalServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class HttpFarePaymentServiceClient implements FarePaymentServiceClient {

    private final RestClient restClient;
    private final String baseUrl;

    public HttpFarePaymentServiceClient(RestClient.Builder restClientBuilder,
                                        @Value("${fare-payment.service.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.build();
        this.baseUrl = baseUrl;
    }

    @Override
    public FareCalculationResult calculateFinalFare(FareCalculationRequest request) {
        try {
            FareCalculationResult result = restClient.post()
                    .uri(baseUrl + "/api/fares/final")
                    .headers(headers -> {
                        String authorization = currentAuthorizationHeader();
                        if (authorization != null) {
                            headers.set(HttpHeaders.AUTHORIZATION, authorization);
                        }
                    })
                    .body(request)
                    .retrieve()
                    .body(FareCalculationResult.class);
            if (result == null) {
                return new FareCalculationResult(null, null, null, null, null);
            }
            return result;
        } catch (RestClientException ex) {
            throw new ExternalServiceUnavailableException("Fare and payment service is unavailable", ex);
        }
    }

    private String currentAuthorizationHeader() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}