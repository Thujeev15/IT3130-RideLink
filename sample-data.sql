-- ====================================================================
-- RideLink - Driver & Vehicle Service Sample Seed Data
-- ====================================================================

USE ridelink_driver;

-- Driver 1 (Colombo - B1234567)
SET @driver1_id = UUID_TO_BIN(UUID());
SET @account1_id = UUID_TO_BIN(UUID());
SET @now = NOW(6);

INSERT INTO drivers (
    id, account_id, full_name, phone_number, license_number,
    service_area, current_latitude, current_longitude,
    availability_status, created_at, updated_at
) VALUES (
    @driver1_id, @account1_id, 'Kasun Perera', '+94771234567', 'B1234567',
    'Colombo', 6.9271, 79.8612, 'AVAILABLE', @now, @now
) ON DUPLICATE KEY UPDATE updated_at = @now;

INSERT INTO vehicles (
    id, driver_id, make, model, year, plate_number, color, vehicle_type, seat_capacity
) VALUES (
    UUID_TO_BIN(UUID()), @driver1_id, 'Toyota', 'Prius', 2020, 'CAB-1234', 'White', 'SEDAN', 4
) ON DUPLICATE KEY UPDATE make = VALUES(make);

-- Driver 2 (Negombo - B7654321)
SET @driver2_id = UUID_TO_BIN(UUID());
SET @account2_id = UUID_TO_BIN(UUID());

INSERT INTO drivers (
    id, account_id, full_name, phone_number, license_number,
    service_area, current_latitude, current_longitude,
    availability_status, created_at, updated_at
) VALUES (
    @driver2_id, @account2_id, 'Nimal Silva', '+94777654321', 'B7654321',
    'Negombo', 7.2008, 79.8736, 'AVAILABLE', @now, @now
) ON DUPLICATE KEY UPDATE updated_at = @now;

INSERT INTO vehicles (
    id, driver_id, make, model, year, plate_number, color, vehicle_type, seat_capacity
) VALUES (
    UUID_TO_BIN(UUID()), @driver2_id, 'Suzuki', 'Alto', 2019, 'WP-CAE-5678', 'Red', 'SEDAN', 4
) ON DUPLICATE KEY UPDATE make = VALUES(make);
