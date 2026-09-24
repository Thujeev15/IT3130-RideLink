package com.ridelink.driverservice.service;

import com.ridelink.driverservice.dto.*;
import com.ridelink.driverservice.entity.AvailabilityStatus;
import com.ridelink.driverservice.entity.Driver;
import com.ridelink.driverservice.entity.VehicleType;
import com.ridelink.driverservice.exception.ConflictException;
import com.ridelink.driverservice.exception.ResourceNotFoundException;
import com.ridelink.driverservice.repository.DriverRepository;
import com.ridelink.driverservice.repository.VehicleRepository;
import com.ridelink.driverservice.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private DriverServiceImpl driverService;

    private UUID accountId;
    private DriverRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        registrationRequest = new DriverRegistrationRequest(
                accountId, "Kasun Perera", "+94771234567", "B1234567", "Colombo");
    }

    @Test
    void registerDriver_savesNewDriver_whenAccountAndLicenseAreUnique() {
        when(driverRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(driverRepository.existsByLicenseNumber("B1234567")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        DriverResponse response = driverService.registerDriver(registrationRequest);

        assertThat(response.fullName()).isEqualTo("Kasun Perera");
        assertThat(response.availabilityStatus()).isEqualTo(AvailabilityStatus.OFFLINE);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void registerDriver_throwsConflict_whenAccountAlreadyHasDriverProfile() {
        when(driverRepository.findByAccountId(accountId))
                .thenReturn(Optional.of(Driver.builder().id(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> driverService.registerDriver(registrationRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(driverRepository, never()).save(any());
    }

    @Test
    void getDriver_throwsNotFound_whenDriverDoesNotExist() {
        UUID missingId = UUID.randomUUID();
        when(driverRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.getDriver(missingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAvailability_rejectsAvailable_whenDriverHasNoVehicle() {
        UUID driverId = UUID.randomUUID();
        Driver driverWithoutVehicle = Driver.builder().id(driverId).vehicle(null).build();
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driverWithoutVehicle));

        AvailabilityUpdateRequest request = new AvailabilityUpdateRequest(AvailabilityStatus.AVAILABLE);

        assertThatThrownBy(() -> driverService.updateAvailability(driverId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("registered vehicle");
    }

    @Test
    void findEligibleDrivers_returnsEmptyList_whenNoneAvailable() {
        when(driverRepository.findEligibleDrivers(AvailabilityStatus.AVAILABLE, "Galle"))
                .thenReturn(List.of());

        List<DriverResponse> result = driverService.findEligibleDrivers("Galle");

        assertThat(result).isEmpty();
    }

    @Test
    void findEligibleDrivers_returnsOnlyAvailableDriversInArea() {
        Driver eligible = Driver.builder()
                .id(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .serviceArea("Colombo")
                .build();
        when(driverRepository.findEligibleDrivers(AvailabilityStatus.AVAILABLE, "Colombo"))
                .thenReturn(List.of(eligible));

        List<DriverResponse> result = driverService.findEligibleDrivers("Colombo");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).serviceArea()).isEqualTo("Colombo");
    }

    @Test
    void findEligibleDrivers_normalizesBlankServiceAreaToNull() {
        when(driverRepository.findEligibleDrivers(AvailabilityStatus.AVAILABLE, null))
                .thenReturn(List.of());

        List<DriverResponse> result = driverService.findEligibleDrivers("   ");

        assertThat(result).isEmpty();
        verify(driverRepository).findEligibleDrivers(AvailabilityStatus.AVAILABLE, null);
    }

    @Test
    void listDrivers_callsFindAllWithVehicle() {
        when(driverRepository.findAllWithVehicle()).thenReturn(List.of());

        List<DriverResponse> result = driverService.listDrivers();

        assertThat(result).isEmpty();
        verify(driverRepository).findAllWithVehicle();
    }
}
