package com.example.emission.model;

public record WarningRecord(
    String plateNumber,
    String pollutant,
    String level,
    String description
) {}
