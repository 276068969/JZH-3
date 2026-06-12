package com.example.emission.model;

public record AuditRecord(
    Long id,
    String inspectionNo,
    String auditAction,
    String auditOpinion,
    String auditor,
    String auditTime
) {}
