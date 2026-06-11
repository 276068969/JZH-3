package com.example.emission.model;

public record Vehicle(
    String plateNumber,
    String vin,
    String vehicleType,
    String fuelType,
    String emissionStandard,
    String owner,
    String registerDate,
    String environmentalStatus
) {}
