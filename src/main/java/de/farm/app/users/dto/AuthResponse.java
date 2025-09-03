package de.farm.app.users.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {}
