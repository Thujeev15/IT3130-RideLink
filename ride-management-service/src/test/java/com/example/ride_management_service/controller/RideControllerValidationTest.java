package com.example.ride_management_service.controller;

import com.example.ride_management_service.dto.CreateRideRequest;
import com.example.ride_management_service.dto.RideResponse;
import com.example.ride_management_service.entity.RideStatus;
import com.example.ride_management_service.exception.ApiExceptionHandler;
import com.example.ride_management_service.service.RideService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RideControllerValidationTest {

    private final RideService rideService = mock(RideService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new RideController(rideService))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    @Test
    void createRideValidationFailure() throws Exception {
        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "passengerId": "",
                                  "pickupLocation": " ",
                                  "destination": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createRideSuccessViaController() throws Exception {
        when(rideService.createRide(any())).thenReturn(new RideResponse(UUID.randomUUID(), "passenger-1", null, "Campus", "Airport", RideStatus.REQUESTED, null, null, null, null, null, null, null, null, null));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "passengerId": "passenger-1",
                                  "pickupLocation": "Campus",
                                  "destination": "Airport"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.passengerId").value("passenger-1"));
    }
}