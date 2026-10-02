package com.example.ride_management_service.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class HttpFarePaymentServiceClientTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void forwardsInboundBearerTokenAndMapsFinalFareResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpFarePaymentServiceClient client = new HttpFarePaymentServiceClient(builder, "http://fare-service:8083");
        UUID rideId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer account-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        server.expect(requestTo("http://fare-service:8083/api/fares/final"))
                .andExpect(method(POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer account-token"))
                .andExpect(content().json("{\"rideId\":\"00000000-0000-0000-0000-000000000001\",\"distanceKm\":12.5}"))
                .andRespond(withSuccess("""
                        {
                          "fareId": "fare-123",
                          "rideId": "00000000-0000-0000-0000-000000000001",
                          "distanceKm": 12.5,
                          "estimatedFare": 1100.0,
                          "finalFare": 1100.0
                        }
                        """, MediaType.APPLICATION_JSON));

        FareCalculationResult result = client.calculateFinalFare(new FareCalculationRequest(rideId, 12.5));

        assertThat(result.fareId()).isEqualTo("fare-123");
        assertThat(result.finalFare()).isEqualTo(1100.0);
        server.verify();
    }
}
