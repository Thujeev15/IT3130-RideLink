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
import com.example.ride_management_service.dto.RideResponse;
import com.example.ride_management_service.entity.Ride;
import com.example.ride_management_service.entity.RideStatus;
import com.example.ride_management_service.exception.InvalidRideTransitionException;
import com.example.ride_management_service.exception.NoEligibleDriverException;
import com.example.ride_management_service.exception.RideNotFoundException;
import com.example.ride_management_service.exception.UnauthorizedRideActionException;
import com.example.ride_management_service.repository.RideRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;
    private final FarePaymentServiceClient farePaymentServiceClient;
    private final Clock clock;

    public RideServiceImpl(RideRepository rideRepository,
                           DriverServiceClient driverServiceClient,
                           FarePaymentServiceClient farePaymentServiceClient,
                           Clock clock) {
        this.rideRepository = rideRepository;
        this.driverServiceClient = driverServiceClient;
        this.farePaymentServiceClient = farePaymentServiceClient;
        this.clock = clock;
    }

    @Override
    public RideResponse createRide(CreateRideRequest request) {
        assertPassengerAccess(request.passengerId());
        Instant now = clock.instant();
        Ride ride = new Ride(UUID.randomUUID(), request.passengerId().trim(), request.pickupLocation().trim(), request.destination().trim(), request.distanceKm(), RideStatus.REQUESTED, now);
        return toResponse(rideRepository.save(ride));
    }

    @Override
    @Transactional(readOnly = true)
    public RideResponse getRide(UUID rideId) {
        Ride ride = findRide(rideId);
        assertRideReadAccess(ride);
        return toResponse(ride);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RideResponse> getRidesByPassenger(String passengerId) {
        assertPassengerAccess(passengerId);
        return rideRepository.findByPassengerId(passengerId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RideResponse> getRidesByDriver(String driverId) {
        List<Ride> rides = rideRepository.findByDriverId(driverId);
        if (isAdmin()) {
            return rides.stream().map(this::toResponse).toList();
        }
        String accountId = authenticatedAccountId();
        return rides.stream()
                .filter(ride -> Objects.equals(ride.getDriverAccountId(), accountId))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public RideResponse assignDriver(UUID rideId, AssignDriverRequest request) {
        Ride ride = findRide(rideId);
        assertPassengerAccess(ride.getPassengerId());
        assertStatus(ride, RideStatus.REQUESTED, "Ride can only be assigned from REQUESTED state");
        if (ride.getDriverId() != null) {
            throw new InvalidRideTransitionException("Ride already has an assigned driver");
        }

        List<EligibleDriverCandidate> candidates = driverServiceClient.getEligibleDrivers(ride.getPickupLocation());
        if (candidates.isEmpty()) {
            throw new NoEligibleDriverException("No eligible drivers are currently available");
        }

        EligibleDriverCandidate selected = selectCandidate(request.driverId(), candidates);
        ride.setDriverId(selected.id().toString());
        ride.setDriverAccountId(selected.accountId().toString());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(clock.instant());
        return toResponse(rideRepository.save(ride));
    }

    @Override
    public RideResponse acceptRide(UUID rideId, RideDriverActionRequest request) {
        Ride ride = findRide(rideId);
        assertStatus(ride, RideStatus.ASSIGNED, "Ride can only be accepted from ASSIGNED state");
        assertAssignedDriver(ride, request.driverId());
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(clock.instant());
        return toResponse(rideRepository.save(ride));
    }

    @Override
    public RideResponse startRide(UUID rideId, RideDriverActionRequest request) {
        Ride ride = findRide(rideId);
        assertStatus(ride, RideStatus.ACCEPTED, "Ride can only start from ACCEPTED state");
        assertAssignedDriver(ride, request.driverId());
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(clock.instant());
        return toResponse(rideRepository.save(ride));
    }

    @Override
    public RideResponse completeRide(UUID rideId, RideDriverActionRequest request) {
        Ride ride = findRide(rideId);
        assertStatus(ride, RideStatus.IN_PROGRESS, "Ride can only complete from IN_PROGRESS state");
        assertAssignedDriver(ride, request.driverId());

        Instant completedAt = clock.instant();
        FareCalculationResult fareResult = farePaymentServiceClient.calculateFinalFare(new FareCalculationRequest(
                ride.getId(),
            ride.getDistanceKm()
        ));

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(completedAt);
        ride.setEstimatedFare(BigDecimal.valueOf(fareResult.finalFare()));
        ride.setEstimatedFareReference(fareResult.fareId());
        return toResponse(rideRepository.save(ride));
    }

    @Override
    public RideResponse cancelRide(UUID rideId, CancelRideRequest request) {
        Ride ride = findRide(rideId);
        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new InvalidRideTransitionException("Ride cannot be cancelled from status " + ride.getStatus());
        }
        if (!isAdmin()
            && !Objects.equals(ride.getPassengerId(), authenticatedAccountId())
            && !Objects.equals(ride.getDriverAccountId(), authenticatedAccountId())) {
            throw new UnauthorizedRideActionException("Actor is not permitted to cancel this ride");
        }
        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(clock.instant());
        ride.setCancellationReason(request.reason());
        return toResponse(rideRepository.save(ride));
    }

    private EligibleDriverCandidate selectCandidate(String requestedDriverId, List<EligibleDriverCandidate> candidates) {
        if (requestedDriverId == null || requestedDriverId.isBlank()) {
            return candidates.get(0);
        }
        return candidates.stream()
            .filter(candidate -> requestedDriverId.equals(candidate.id().toString()))
                .findFirst()
                .orElseThrow(() -> new NoEligibleDriverException("Requested driver is not eligible for this ride"));
    }

    private Ride findRide(UUID rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found: " + rideId));
    }

    private void assertStatus(Ride ride, RideStatus expectedStatus, String message) {
        if (ride.getStatus() != expectedStatus) {
            throw new InvalidRideTransitionException(message + ". Current status: " + ride.getStatus());
        }
    }

    private void assertAssignedDriver(Ride ride, String driverId) {
        if (!Objects.equals(ride.getDriverId(), driverId)
                || (!isAdmin() && !Objects.equals(ride.getDriverAccountId(), authenticatedAccountId()))) {
            throw new UnauthorizedRideActionException("Only the assigned driver can perform this action");
        }
    }

    private void assertPassengerAccess(String passengerId) {
        if (!isAdmin() && !Objects.equals(passengerId, authenticatedAccountId())) {
            throw new UnauthorizedRideActionException("Only the passenger can perform this action");
        }
    }

    private void assertRideReadAccess(Ride ride) {
        if (isAdmin()) {
            return;
        }
        String accountId = authenticatedAccountId();
        if (!Objects.equals(ride.getPassengerId(), accountId)
                && !Objects.equals(ride.getDriverAccountId(), accountId)) {
            throw new UnauthorizedRideActionException("Caller is not permitted to view this ride");
        }
    }

    private boolean isAdmin() {
        return currentAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private String authenticatedAccountId() {
        return currentAuthentication().getName();
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedRideActionException("Authentication is required");
        }
        return authentication;
    }

    private RideResponse toResponse(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getPassengerId(),
                ride.getDriverId(),
                ride.getPickupLocation(),
                ride.getDestination(),
                ride.getStatus(),
                ride.getEstimatedFare(),
                ride.getEstimatedFareReference(),
                ride.getRequestedAt(),
                ride.getAssignedAt(),
                ride.getAcceptedAt(),
                ride.getStartedAt(),
                ride.getCompletedAt(),
                ride.getCancelledAt(),
                ride.getCancellationReason()
        );
    }
}