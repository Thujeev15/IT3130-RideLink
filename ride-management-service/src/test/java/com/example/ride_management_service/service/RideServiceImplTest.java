package com.example.ride_management_service.service;

import com.example.ride_management_service.client.DriverServiceClient;
import com.example.ride_management_service.client.EligibleDriverCandidate;
import com.example.ride_management_service.client.FareCalculationResult;
import com.example.ride_management_service.client.FarePaymentServiceClient;
import com.example.ride_management_service.dto.AssignDriverRequest;
import com.example.ride_management_service.dto.CancelRideRequest;
import com.example.ride_management_service.dto.CreateRideRequest;
import com.example.ride_management_service.dto.RideDriverActionRequest;
import com.example.ride_management_service.entity.Ride;
import com.example.ride_management_service.entity.RideStatus;
import com.example.ride_management_service.exception.ExternalServiceUnavailableException;
import com.example.ride_management_service.exception.InvalidRideTransitionException;
import com.example.ride_management_service.exception.NoEligibleDriverException;
import com.example.ride_management_service.exception.RideNotFoundException;
import com.example.ride_management_service.exception.UnauthorizedRideActionException;
import com.example.ride_management_service.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-09-24T10:00:00Z");

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverServiceClient driverServiceClient;

    @Mock
    private FarePaymentServiceClient farePaymentServiceClient;

    @InjectMocks
    private RideServiceImpl rideService;

    @BeforeEach
    void setUp() {
        rideService = new RideServiceImpl(rideRepository, driverServiceClient, farePaymentServiceClient, Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
    }

    @Test
    void createRideSuccess() {
        CreateRideRequest request = new CreateRideRequest("passenger-1", "Campus", "Airport");
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.createRide(request);

        assertThat(response.passengerId()).isEqualTo("passenger-1");
        assertThat(response.status()).isEqualTo(RideStatus.REQUESTED);
        assertThat(response.requestedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void getRideSuccess() {
        UUID rideId = UUID.randomUUID();
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(sampleRide(rideId, RideStatus.REQUESTED)));

        var response = rideService.getRide(rideId);

        assertThat(response.id()).isEqualTo(rideId);
    }

    @Test
    void getRideNotFound() {
        UUID rideId = UUID.randomUUID();
        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.getRide(rideId)).isInstanceOf(RideNotFoundException.class);
    }

    @Test
    void assignDriverSuccess() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.REQUESTED);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(driverServiceClient.getEligibleDrivers(anyString())).thenReturn(List.of(new EligibleDriverCandidate("driver-1", "Driver One", "Zone A", 1.2)));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.assignDriver(rideId, new AssignDriverRequest(null));

        assertThat(response.driverId()).isEqualTo("driver-1");
        assertThat(response.status()).isEqualTo(RideStatus.ASSIGNED);
        assertThat(response.assignedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void noEligibleDriver() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.REQUESTED);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(driverServiceClient.getEligibleDrivers(anyString())).thenReturn(List.of());

        assertThatThrownBy(() -> rideService.assignDriver(rideId, new AssignDriverRequest(null)))
                .isInstanceOf(NoEligibleDriverException.class);
    }

    @Test
    void acceptSuccess() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.ASSIGNED);
        ride.setDriverId("driver-1");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.acceptRide(rideId, new RideDriverActionRequest("driver-1"));

        assertThat(response.status()).isEqualTo(RideStatus.ACCEPTED);
        assertThat(response.acceptedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void invalidAcceptTransition() {
        UUID rideId = UUID.randomUUID();
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(sampleRide(rideId, RideStatus.REQUESTED)));

        assertThatThrownBy(() -> rideService.acceptRide(rideId, new RideDriverActionRequest("driver-1")))
                .isInstanceOf(InvalidRideTransitionException.class);
    }

    @Test
    void startSuccess() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.ACCEPTED);
        ride.setDriverId("driver-1");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.startRide(rideId, new RideDriverActionRequest("driver-1"));

        assertThat(response.status()).isEqualTo(RideStatus.IN_PROGRESS);
        assertThat(response.startedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void invalidStartTransition() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.ASSIGNED);
        ride.setDriverId("driver-1");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.startRide(rideId, new RideDriverActionRequest("driver-1")))
                .isInstanceOf(InvalidRideTransitionException.class);
    }

    @Test
    void completeSuccess() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.IN_PROGRESS);
        ride.setDriverId("driver-1");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(farePaymentServiceClient.calculateFinalFare(any())).thenReturn(new FareCalculationResult(new BigDecimal("12.50"), "LKR", "fare-123"));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.completeRide(rideId, new RideDriverActionRequest("driver-1"));

        assertThat(response.status()).isEqualTo(RideStatus.COMPLETED);
        assertThat(response.completedAt()).isEqualTo(FIXED_NOW);
        assertThat(response.estimatedFare()).isEqualByComparingTo("12.50");
        assertThat(response.estimatedFareReference()).isEqualTo("fare-123");
    }

    @Test
    void invalidCompleteTransition() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.ACCEPTED);
        ride.setDriverId("driver-1");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.completeRide(rideId, new RideDriverActionRequest("driver-1")))
                .isInstanceOf(InvalidRideTransitionException.class);
    }

    @Test
    void cancellationSuccess() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.ACCEPTED);
        ride.setDriverId("driver-1");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.cancelRide(rideId, new CancelRideRequest("passenger-1", "Change of plans"));

        assertThat(response.status()).isEqualTo(RideStatus.CANCELLED);
        assertThat(response.cancelledAt()).isEqualTo(FIXED_NOW);
        assertThat(response.cancellationReason()).isEqualTo("Change of plans");
    }

    @Test
    void invalidCancellationAfterCompletion() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.COMPLETED);
        ride.setDriverId("driver-1");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.cancelRide(rideId, new CancelRideRequest("passenger-1", "Too late")))
                .isInstanceOf(InvalidRideTransitionException.class);
    }

    @Test
    void unauthorizedDifferentDriverAction() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.ACCEPTED);
        ride.setDriverId("driver-1");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.startRide(rideId, new RideDriverActionRequest("driver-2")))
                .isInstanceOf(UnauthorizedRideActionException.class);
    }

    @Test
    void externalServiceUnavailableDuringFareCalculationPropagates() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.IN_PROGRESS);
        ride.setDriverId("driver-1");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(farePaymentServiceClient.calculateFinalFare(any())).thenThrow(new ExternalServiceUnavailableException("Fare and payment service is unavailable", new RuntimeException("down")));

        assertThatThrownBy(() -> rideService.completeRide(rideId, new RideDriverActionRequest("driver-1")))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void assignSpecificEligibleDriverSuccess() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.REQUESTED);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(driverServiceClient.getEligibleDrivers(anyString())).thenReturn(List.of(
                new EligibleDriverCandidate("driver-1", "Driver One", "Zone A", 1.2),
                new EligibleDriverCandidate("driver-2", "Driver Two", "Zone B", 1.4)
        ));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.assignDriver(rideId, new AssignDriverRequest("driver-2"));

        assertThat(response.driverId()).isEqualTo("driver-2");
    }

    @Test
    void requestedDriverNotEligibleFails() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.REQUESTED);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(driverServiceClient.getEligibleDrivers(anyString())).thenReturn(List.of(new EligibleDriverCandidate("driver-1", "Driver One", "Zone A", 1.2)));

        assertThatThrownBy(() -> rideService.assignDriver(rideId, new AssignDriverRequest("driver-2")))
                .isInstanceOf(NoEligibleDriverException.class);
    }

    private Ride sampleRide(UUID rideId, RideStatus status) {
        return new Ride(rideId, "passenger-1", "Campus", "Airport", status, FIXED_NOW);
    }
}