package io.github.kuonjiarisu.backend.auth.model;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    AuthUserView user
) {
}
