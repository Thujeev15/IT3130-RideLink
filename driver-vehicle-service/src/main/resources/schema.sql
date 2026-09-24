-- ====================================================================
-- RideLink - Driver & Vehicle Service Database Schema
-- Schema Name: ridelink_driver
-- Database: MySQL 8.0+
-- ====================================================================

CREATE DATABASE IF NOT EXISTS ridelink_driver
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE ridelink_driver;

-- --------------------------------------------------------------------
-- Table: drivers
-- Stores driver operational profiles, service area & location
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS drivers (
    id BINARY(16) NOT NULL,
    account_id BINARY(16) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    license_number VARCHAR(255) NOT NULL,
    service_area VARCHAR(255) DEFAULT NULL,
    current_latitude DOUBLE DEFAULT NULL,
    current_longitude DOUBLE DEFAULT NULL,
    availability_status ENUM('AVAILABLE', 'BUSY', 'OFFLINE') NOT NULL DEFAULT 'OFFLINE',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_drivers_account_id UNIQUE (account_id),
    CONSTRAINT uk_drivers_license_number UNIQUE (license_number),
    KEY idx_drivers_status_area (availability_status, service_area)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------------------
-- Table: vehicles
-- Stores vehicle details associated with each driver
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicles (
    id BINARY(16) NOT NULL,
    driver_id BINARY(16) NOT NULL,
    make VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    plate_number VARCHAR(255) NOT NULL,
    color VARCHAR(255) NOT NULL,
    vehicle_type ENUM('MOTORBIKE', 'SEDAN', 'SUV', 'TUK_TUK', 'VAN') NOT NULL,
    seat_capacity INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_vehicles_plate_number UNIQUE (plate_number),
    CONSTRAINT uk_vehicles_driver_id UNIQUE (driver_id),
    CONSTRAINT fk_vehicles_driver FOREIGN KEY (driver_id)
        REFERENCES drivers (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
