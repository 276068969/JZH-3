package com.example.emission.dto;

public record StationStatus(
    long stationId,
    String stationName,
    String district,
    String address,
    String phone,
    int todayInspectionCount,
    int passedCount,
    int failedCount,
    double passRate,
    String lastInspectionTime,
    String runningStatus
) {}
