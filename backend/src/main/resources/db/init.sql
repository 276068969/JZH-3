CREATE DATABASE IF NOT EXISTS emission_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE emission_platform;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plate_number VARCHAR(32) NOT NULL UNIQUE,
  vin VARCHAR(64) NOT NULL UNIQUE,
  vehicle_type VARCHAR(64) NOT NULL,
  fuel_type VARCHAR(32) NOT NULL,
  emission_standard VARCHAR(32) NOT NULL,
  owner_name VARCHAR(128) NOT NULL,
  register_date DATE NOT NULL,
  environmental_status VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS inspection_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inspection_no VARCHAR(64) NOT NULL UNIQUE,
  plate_number VARCHAR(32) NOT NULL,
  station_name VARCHAR(128) NOT NULL,
  inspection_time DATETIME NOT NULL,
  co_value DECIMAL(8, 3) NOT NULL,
  hc_value DECIMAL(8, 3) NOT NULL,
  nox_value DECIMAL(8, 3) NOT NULL,
  opacity_value DECIMAL(8, 3) NOT NULL,
  result VARCHAR(32) NOT NULL,
  inspector VARCHAR(64) NOT NULL,
  report_status VARCHAR(32) NOT NULL,
  auditor VARCHAR(64),
  audit_time DATETIME,
  audit_opinion TEXT
);

CREATE TABLE IF NOT EXISTS audit_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inspection_no VARCHAR(64) NOT NULL,
  audit_action VARCHAR(32) NOT NULL,
  audit_opinion TEXT,
  auditor VARCHAR(64) NOT NULL,
  audit_time DATETIME NOT NULL,
  INDEX idx_inspection_no (inspection_no)
);
