package com.example.emission.dto;

import com.example.emission.model.UserAccount;

public record LoginResponse(
    String token,
    UserAccount user
) {}
