package com.example.emission.model;

public record InspectionRecord(
    String inspectionNo,
    String plateNumber,
    String stationName,
    String inspectionTime,
    double coValue,
    double hcValue,
    double noxValue,
    double opacityValue,
    String result,
    String inspector,
    String reportStatus
) {}
