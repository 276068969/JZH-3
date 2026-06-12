package com.example.emission.model;

public record Station(
    long id,
    String name,
    String district,
    String address,
    String phone,
    String status
) {}
