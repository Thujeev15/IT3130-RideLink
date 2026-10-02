package com.example.ride_management_service.service;

import com.example.ride_management_service.client.DriverServiceClient;
import com.example.ride_management_service.client.EligibleDriverCandidate;
import com.example.ride_management_service.client.FareCalculationRequest;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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
        authenticate("passenger-1", "PASSENGER");
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRideSuccess() {
        CreateRideRequest request = new CreateRideRequest("passenger-1", "Campus", "Airport", 12.5);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.createRide(request);

        assertThat(response.passengerId()).isEqualTo("passenger-1");
        assertThat(response.status()).isEqualTo(RideStatus.REQUESTED);
        assertThat(response.requestedAt()).isEqualTo(FIXED_NOW);
        ArgumentCaptor<Ride> savedRide = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository).save(savedRide.capture());
        assertThat(savedRide.getValue().getDistanceKm()).isEqualTo(12.5);
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
        when(driverServiceClient.getEligibleDrivers(anyString())).thenReturn(List.of(new EligibleDriverCandidate(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.randomUUID(), "Driver One", "0712345678", "LIC-1", "Zone A", 6.9, 79.8, "AVAILABLE", "Toyota", "Prius", "ABC-1234", "SEDAN", 4)));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.assignDriver(rideId, new AssignDriverRequest(null));

        assertThat(response.driverId()).isEqualTo("00000000-0000-0000-0000-000000000001");
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
        authenticate("driver-account-1", "DRIVER");
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
        authenticate("driver-account-1", "DRIVER");
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
        authenticate("driver-account-1", "DRIVER");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(farePaymentServiceClient.calculateFinalFare(any())).thenReturn(new FareCalculationResult("fare-123", rideId.toString(), 12.5, 1100.0, 1100.0));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.completeRide(rideId, new RideDriverActionRequest("driver-1"));

        ArgumentCaptor<FareCalculationRequest> fareRequest = ArgumentCaptor.forClass(FareCalculationRequest.class);
        verify(farePaymentServiceClient).calculateFinalFare(fareRequest.capture());
        assertThat(fareRequest.getValue().rideId()).isEqualTo(rideId);
        assertThat(fareRequest.getValue().distanceKm()).isEqualTo(12.5);
        assertThat(response.status()).isEqualTo(RideStatus.COMPLETED);
        assertThat(response.completedAt()).isEqualTo(FIXED_NOW);
        assertThat(response.estimatedFare()).isEqualByComparingTo("1100.0");
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
        authenticate("driver-account-2", "DRIVER");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.startRide(rideId, new RideDriverActionRequest("driver-2")))
                .isInstanceOf(UnauthorizedRideActionException.class);
    }

    @Test
    void externalServiceUnavailableDuringFareCalculationPropagates() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.IN_PROGRESS);
        ride.setDriverId("driver-1");
        authenticate("driver-account-1", "DRIVER");
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
            candidate("00000000-0000-0000-0000-000000000001", "Driver One", "Zone A"),
            candidate("00000000-0000-0000-0000-000000000002", "Driver Two", "Zone B")
        ));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.assignDriver(rideId, new AssignDriverRequest("00000000-0000-0000-0000-000000000002"));

        assertThat(response.driverId()).isEqualTo("00000000-0000-0000-0000-000000000002");
    }

    @Test
    void requestedDriverNotEligibleFails() {
        UUID rideId = UUID.randomUUID();
        Ride ride = sampleRide(rideId, RideStatus.REQUESTED);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(driverServiceClient.getEligibleDrivers(anyString())).thenReturn(List.of(candidate(
            "00000000-0000-0000-0000-000000000001", "Driver One", "Zone A")));

        assertThatThrownBy(() -> rideService.assignDriver(rideId, new AssignDriverRequest("00000000-0000-0000-0000-000000000002")))
                .isInstanceOf(NoEligibleDriverException.class);
    }

    private EligibleDriverCandidate candidate(String id, String fullName, String serviceArea) {
        return new EligibleDriverCandidate(UUID.fromString(id), UUID.randomUUID(), fullName, "0712345678",
            "LIC-1", serviceArea, 6.9, 79.8, "AVAILABLE", "Toyota", "Prius", "ABC-1234", "SEDAN", 4);
        }

    private Ride sampleRide(UUID rideId, RideStatus status) {
        Ride ride = new Ride(rideId, "passenger-1", "Campus", "Airport", 12.5, status, FIXED_NOW);
        ride.setDriverAccountId("driver-account-1");
        return ride;
    }

    private void authenticate(String accountId, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(accountId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}